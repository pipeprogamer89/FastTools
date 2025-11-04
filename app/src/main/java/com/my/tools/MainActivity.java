package com.my.tools;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.my.tools.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private MainBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		setSupportActionBar(binding.Toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		binding.Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		
		binding.button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// Diálogo de confirmación
				android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
				builder.setTitle("¿Limpiar caché del sistema?");
				builder.setMessage("Esta acción borrará todos los archivos temporales en /cache. ¿Continuar?");
				builder.setPositiveButton("Sí, limpiar", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(android.content.DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									java.lang.Process p = Runtime.getRuntime().exec("su"); // ✅ Aquí va la corrección
									java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
									os.writeBytes("mount -o rw,remount /cache\n");
									os.writeBytes("rm -rf /cache/*\n");
									os.writeBytes("echo 'Caché limpiada correctamente.'\n");
									os.writeBytes("exit\n");
									os.flush();
									
									java.io.BufferedReader reader = new java.io.BufferedReader(
									new java.io.InputStreamReader(p.getInputStream())
									);
									StringBuilder output = new StringBuilder();
									String line;
									while ((line = reader.readLine()) != null) {
										output.append(line).append("\n");
									}
									p.waitFor();
									
									final String result = output.toString();
									
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											android.widget.Toast.makeText(MainActivity.this, result, android.widget.Toast.LENGTH_LONG).show();
										}
									});
									
								} catch (final Exception e) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											android.widget.Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
										}
									});
								}
							}
						}).start();
					}
				});
				builder.setNegativeButton("Cancelar", null);
				builder.show();
			}
		});
		
		binding.button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean hasRoot = false;
						String resultMessage = "Root no disponible.";
						
						try {
							java.lang.Process p = Runtime.getRuntime().exec("su");
							java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
							java.io.BufferedReader reader = new java.io.BufferedReader(
							new java.io.InputStreamReader(p.getInputStream())
							);
							
							os.writeBytes("id\n");
							os.writeBytes("exit\n");
							os.flush();
							
							StringBuilder output = new StringBuilder();
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							p.waitFor();
							
							if (output.toString().contains("uid=0") || output.toString().contains("root")) {
								hasRoot = true;
								resultMessage = "✅ Root activo: SÍ\n" + output.toString().trim();
							} else {
								resultMessage = "❌ Root denegado o no disponible.";
							}
							
						} catch (Exception e) {
							resultMessage = "❌ Error al verificar root:\n" + e.getMessage();
						}
						
						final String finalMessage = resultMessage;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								android.widget.Toast.makeText(MainActivity.this, finalMessage, android.widget.Toast.LENGTH_LONG).show();
							}
						});
					}
				}).start();
			}
		});
		
		binding.button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
				builder.setTitle("¿Bloquear anuncios del sistema?");
				builder.setMessage("Se reemplazará el archivo hosts del sistema. Esto puede mejorar el rendimiento y privacidad. ¿Continuar?");
				builder.setPositiveButton("Sí, bloquear", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(android.content.DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									java.io.InputStream is = getAssets().open("hosts");
									java.io.File tempFile = new java.io.File(getCacheDir(), "hosts");
									java.io.OutputStream os = new java.io.FileOutputStream(tempFile);
									byte[] buffer = new byte[1024];
									int length;
									while ((length = is.read(buffer)) > 0) {
										os.write(buffer, 0, length);
									}
									os.close();
									is.close();
									
									java.lang.Process p = Runtime.getRuntime().exec("su");
									java.io.DataOutputStream suOut = new java.io.DataOutputStream(p.getOutputStream());
									suOut.writeBytes("mount -o rw,remount /system\n");
									suOut.writeBytes("cp " + tempFile.getAbsolutePath() + " /system/etc/hosts\n");
									suOut.writeBytes("chmod 644 /system/etc/hosts\n");
									suOut.writeBytes("chown root:root /system/etc/hosts\n");
									suOut.writeBytes("echo 'Anuncios bloqueados correctamente.'\n");
									suOut.writeBytes("exit\n");
									suOut.flush();
									
									java.io.BufferedReader reader = new java.io.BufferedReader(
									new java.io.InputStreamReader(p.getInputStream())
									);
									StringBuilder output = new StringBuilder();
									String line;
									while ((line = reader.readLine()) != null) {
										output.append(line).append("\n");
									}
									p.waitFor();
									
									final String result = output.toString();
									
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											android.widget.Toast.makeText(MainActivity.this, result, android.widget.Toast.LENGTH_LONG).show();
										}
									});
									
								} catch (final Exception e) {
									final String errorMsg = e.getMessage();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											android.widget.Toast.makeText(MainActivity.this, "Error: " + errorMsg, android.widget.Toast.LENGTH_LONG).show();
										}
									});
								}
							}
						}).start();
					}
				});
				builder.setNegativeButton("Cancelar", null);
				builder.show();
			}
		});
		
		binding.button4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							java.lang.Process p = Runtime.getRuntime().exec("su");
							java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
							os.writeBytes("setprop service.adb.root 1\n");
							os.writeBytes("stop adbd\n");
							os.writeBytes("start adbd\n");
							os.writeBytes("echo 'ADB reiniciado como root.'\n");
							os.writeBytes("exit\n");
							os.flush();
							
							java.io.BufferedReader reader = new java.io.BufferedReader(
							new java.io.InputStreamReader(p.getInputStream())
							);
							StringBuilder output = new StringBuilder();
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							p.waitFor();
							
							final String result = output.toString();
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									android.widget.Toast.makeText(MainActivity.this, result, android.widget.Toast.LENGTH_SHORT).show();
								}
							});
							
						} catch (final Exception e) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									android.widget.Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				}).start();
			}
		});
		
		binding.button5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
				builder.setTitle("¿Borrar caché de Dalvik/ART?");
				builder.setMessage("Esto borrará la caché de optimización de apps. El próximo arranque será más lento. ¿Continuar?");
				builder.setPositiveButton("Sí, borrar", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(android.content.DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									java.lang.Process p = Runtime.getRuntime().exec("su");
									java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
									os.writeBytes("mount -o rw,remount /data\n");
									os.writeBytes("rm -rf /data/dalvik-cache/*\n");
									os.writeBytes("echo 'Caché de Dalvik/ART borrada.'\n");
									os.writeBytes("exit\n");
									os.flush();
									
									java.io.BufferedReader reader = new java.io.BufferedReader(
									new java.io.InputStreamReader(p.getInputStream())
									);
									StringBuilder out = new StringBuilder();
									String line;
									while ((line = reader.readLine()) != null) out.append(line).append("\n");
									p.waitFor();
									
									final String result = out.toString();
									runOnUiThread(() -> {
										android.widget.Toast.makeText(MainActivity.this, result, android.widget.Toast.LENGTH_LONG).show();
									});
								} catch (Exception e) {
									runOnUiThread(() -> {
										android.widget.Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
									});
								}
							}
						}).start();
					}
				});
				builder.setNegativeButton("Cancelar", null);
				builder.show();
			}
		});
		
		binding.button6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean magiskFound = false;
						try {
							// Método 1: buscar binario
							java.lang.Process p1 = Runtime.getRuntime().exec(new String[]{"sh", "-c", "which magisk"});
							p1.waitFor();
							if (p1.exitValue() == 0) magiskFound = true;
							
							// Método 2: buscar directorio oculto
							if (!magiskFound) {
								java.io.File magiskDir = new java.io.File("/sbin/.magisk");
								if (magiskDir.exists()) magiskFound = true;
							}
							
							// Método 3: buscar en mounts
							if (!magiskFound) {
								java.lang.Process p2 = Runtime.getRuntime().exec("mount");
								java.io.BufferedReader r = new java.io.BufferedReader(
								new java.io.InputStreamReader(p2.getInputStream())
								);
								String line;
								while ((line = r.readLine()) != null) {
									if (line.contains("magisk")) {
										magiskFound = true;
										break;
									}
								}
								p2.waitFor();
							}
							
						} catch (Exception ignored) {}
						
						final String msg = magiskFound ? "✅ Magisk está instalado." : "❌ Magisk no detectado.";
						runOnUiThread(() -> {
							android.widget.Toast.makeText(MainActivity.this, msg, android.widget.Toast.LENGTH_SHORT).show();
						});
					}
				}).start();
			}
		});
		
		binding.button7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				CharSequence[] refreshRates = {"15 Hz", "30 Hz", "60 Hz", "90 Hz", "120 Hz"};
				int[] rateValues = {15, 30, 60, 90, 120};
				
				new android.app.AlertDialog.Builder(MainActivity.this)
				.setTitle("Seleccionar tasa de refresco")
				.setItems(refreshRates, new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(android.content.DialogInterface dialog, int which) {
						final int selectedRate = rateValues[which];
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									java.lang.Process p = Runtime.getRuntime().exec("su");
									java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
									// Comando común para forzar refresh rate (varía por kernel)
									os.writeBytes("settings put system peak_refresh_rate " + selectedRate + "\n");
									os.writeBytes("settings put system min_refresh_rate " + selectedRate + "\n");
									os.writeBytes("echo 'Tasa de refresco forzada a " + selectedRate + " Hz.'\n");
									os.writeBytes("exit\n");
									os.flush();
									
									java.io.BufferedReader reader = new java.io.BufferedReader(
									new java.io.InputStreamReader(p.getInputStream())
									);
									StringBuilder out = new StringBuilder();
									String line;
									while ((line = reader.readLine()) != null) out.append(line).append("\n");
									p.waitFor();
									
									final String result = out.toString();
									runOnUiThread(() -> {
										android.widget.Toast.makeText(MainActivity.this, result, android.widget.Toast.LENGTH_SHORT).show();
									});
								} catch (Exception e) {
									runOnUiThread(() -> {
										android.widget.Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
									});
								}
							}
						}).start();
					}
				})
				.show();
			}
		});
		
		binding.button8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							java.lang.Process p = Runtime.getRuntime().exec("su");
							java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
							os.writeBytes("settings put global low_power 1\n");
							os.writeBytes("settings put global low_power_sticky 1\n"); // Se mantiene tras reinicio
							os.writeBytes("echo 'Modo ahorro de batería activado.'\n");
							os.writeBytes("exit\n");
							os.flush();
							
							java.io.BufferedReader reader = new java.io.BufferedReader(
							new java.io.InputStreamReader(p.getInputStream())
							);
							StringBuilder out = new StringBuilder();
							String line;
							while ((line = reader.readLine()) != null) out.append(line).append("\n");
							p.waitFor();
							
							final String result = out.toString();
							runOnUiThread(() -> {
								android.widget.Toast.makeText(MainActivity.this, result, android.widget.Toast.LENGTH_SHORT).show();
							});
						} catch (Exception e) {
							runOnUiThread(() -> {
								android.widget.Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
							});
						}
					}
				}).start();
			}
		});
	}
	
	private void initializeLogic() {
	}
	
}