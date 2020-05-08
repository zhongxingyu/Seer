 package es.jiayu.jiayuid;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.DownloadManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.hardware.Camera.Size;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.Html;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 public class App extends Activity implements AsyncResponse {
 
     static long downloadREF = -1;
     static HashMap<String, String> listaDescargas = new HashMap<String, String>();
     String nversion = "";
     String version = "";
     ImageButton imageButton;
     Button descargas;
     Button foro;
     Button about;
     Button videotutoriales;
     Button driversherramientas;
     Button herramientasROM;
     Button salir;
     String modelo = "";
     String model = "";
     String urlActualizacion = "";
     String fabricante = "";
     String compilacion = "";
     String newversion = "";
     /*static NotificationManager mNotificationManagerUpdate=null;
     static NotificationManager mNotificationManagerNews=null;
     private int SIMPLE_NOTFICATION_UPDATE=8888;
     private int SIMPLE_NOTFICATION_NEWS=8889;
     SharedPreferences ajustes=null;
     SharedPreferences.Editor editorAjustes=null;*/
     protected void onCreate(Bundle savedInstanceState) {
         try {
             super.onCreate(savedInstanceState);
            nversion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
             /*mNotificationManagerUpdate = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
             mNotificationManagerUpdate.cancel(SIMPLE_NOTFICATION_UPDATE);
             ajustes=getSharedPreferences("JiayuesAjustes",Context.MODE_PRIVATE);
             editorAjustes=ajustes.edit();
             String tmpFecha="";
             tmpFecha=ajustes.getString("fechaUltimoAccesoDescargas", "");
             if("".equals(tmpFecha)){
                 editorAjustes.putString("fechaUltimoAccesoDescargas", asignaFecha());
                 editorAjustes.commit();
             }
 
             Calendar calc = Calendar.getInstance();
             calc.add(Calendar.SECOND,20);
             Intent intent2 = new Intent(getBaseContext(), NotifyService.class);
             PendingIntent pintent = PendingIntent.getService(getBaseContext(), 0, intent2,
                     0);
             AlarmManager alarm = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
 
             alarm.setRepeating(AlarmManager.RTC_WAKEUP, calc.getTimeInMillis(),20*1000, pintent);
             getBaseContext().startService(new Intent(getBaseContext(),NotifyService.class));
 
             Calendar calc2 = Calendar.getInstance();
             calc2.add(Calendar.SECOND,20);
             Intent intent3 = new Intent(getBaseContext(), NotifyNewsService.class);
             PendingIntent pintent2 = PendingIntent.getService(getBaseContext(), 0, intent3,
                     0);
             AlarmManager alarm2 = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
 
             alarm2.setRepeating(AlarmManager.RTC_WAKEUP, calc2.getTimeInMillis(),20*1000, pintent2);
             getBaseContext().startService(new Intent(getBaseContext(),NotifyNewsService.class));*/
             version = "Jiayu.es ";
             version = version + nversion;
             File f1 = new File(Environment.getExternalStorageDirectory() + "/JIAYUES/APP/");
             if (!f1.exists()) {
                 f1.mkdirs();
             }
             File f2 = new File(Environment.getExternalStorageDirectory() + "/JIAYUES/ROMS/");
             if (!f2.exists()) {
                 f2.mkdirs();
             }
             File f3 = new File(Environment.getExternalStorageDirectory() + "/JIAYUES/RECOVERY/");
             if (!f3.exists()) {
                 f3.mkdirs();
             }
             File f4 = new File(Environment.getExternalStorageDirectory() + "/JIAYUES/DOWNLOADS/");
             if (!f4.exists()) {
                 f4.mkdirs();
             }
             File f5 = new File(Environment.getExternalStorageDirectory() + "/JIAYUES/IMEI/");
             if (!f5.exists()) {
                 f5.mkdirs();
             }
             File f6 = new File(Environment.getExternalStorageDirectory() + "/JIAYUES/BOOTANIMATION/");
             if (!f6.exists()) {
                 f6.mkdirs();
             }
             Resources res = this.getResources();
 
             setContentView(R.layout.activity_app);
             addListenerOnButton();
             comprobarVersionInicio(version);
             descargas = (Button) findViewById(R.id.button1);
             about = (Button) findViewById(R.id.button2);
             salir = (Button) findViewById(R.id.button11);
 
             videotutoriales = (Button) findViewById(R.id.button3);
             foro = (Button) findViewById(R.id.button4);
             driversherramientas = (Button) findViewById(R.id.button9);
             herramientasROM = (Button) findViewById(R.id.button10);
             descargas.setEnabled(false);
             //accesorios.setEnabled(false);
             videotutoriales.setEnabled(false);
             driversherramientas.setEnabled(false);
             herramientasROM.setEnabled(false);
             foro.setEnabled(false);
             ImageButton img = new ImageButton(this);
             img = (ImageButton) findViewById(R.id.imageButton1);
             TextView t = new TextView(this);
             TextView t2 = new TextView(this);
             TextView t4 = new TextView(this);
             TextView t5 = new TextView(this);
             t4 = (TextView) findViewById(R.id.textView4);
             t5 = (TextView) findViewById(R.id.textView5);
             t4.setText(version);
             compilacion = Build.DISPLAY;
             fabricante = infoBrand();
             t = (TextView) findViewById(R.id.textView1);
             t2 = (TextView) findViewById(R.id.textView2);
             if ("".equals(modelo)) {
                 calcularTelefono();
                 modelo = model;
             } else {
                 recalcularTelefono();
                 descargas.setEnabled(true);
                 herramientasROM.setEnabled(true);
                 //accesorios.setEnabled(true);
                 foro.setEnabled(true);
                 driversherramientas.setEnabled(true);
                 videotutoriales.setEnabled(true);
 
             }
             if (modelo.length() < 8) {
                 /*Calendar cal=Calendar.getInstance();
                 editorAjustes = ajustes.edit();
                 editorAjustes.putString("modelo", modelo);
                 editorAjustes.commit();*/
 
                 descargas.setEnabled(true);
                 //accesorios.setEnabled(true);
                 foro.setEnabled(true);
                 driversherramientas.setEnabled(true);
                 videotutoriales.setEnabled(true);
                 herramientasROM.setEnabled(true);
                 if (!"JIAYU".equals(fabricante.toUpperCase().trim())) {
                     t5.setTextColor(Color.RED);
                     t5.setText(res.getString(R.string.msgIdentificado1) +" "+ modelo + res.getString(R.string.msgIdentificado2));
                 }else{
                     t5.setVisibility(View.INVISIBLE);
                 }
             }
             t.setText(res.getString(R.string.msgModelo) + modelo);
             t2.setText(res.getString(R.string.msgCompilacion) + compilacion);
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     private String infoBrand() throws IOException {
         String fabricante = Build.BRAND;
         String buildprop = "";
         if (fabricante.toUpperCase().indexOf("JIAYU") == -1) {
             FileInputStream fis = new FileInputStream(new File("/system/build.prop"));
             byte[] input = new byte[fis.available()];
             while (fis.read(input) != -1) {
                 buildprop += new String(input);
             }
             if (buildprop.toUpperCase().indexOf("JIAYU") != -1) {
                 fabricante = "JIAYU";
             } else {
                 fabricante = "TERMINAL NO JIAYU";
             }
         }
         return fabricante.toUpperCase();
     }
 
     private void comprobarVersion(String version2) {
         try {
             VersionThread asyncTask = new VersionThread();
             asyncTask.delegate = this;
             asyncTask.execute(version2);
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     private void comprobarVersionInicio(String version2) {
         try {
             VersionThread asyncTask = new VersionThread();
             asyncTask.delegate = this;
             asyncTask.execute(version2, "inicio");
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     private void ActualizarVersion() {
         try {
             String nombreFichero = "";
             nombreFichero = urlActualizacion.split("/")[urlActualizacion.split("/").length - 1];
             DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlActualizacion));
             request.setDescription(nombreFichero);
             request.setTitle(nombreFichero);
             if (Build.VERSION.SDK_INT >= 11) {
                 request.allowScanningByMediaScanner();
                 request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                 if (".apk".equals(nombreFichero.substring(nombreFichero.length() - 4, nombreFichero.length()).toLowerCase())) {
                     request.setMimeType("application/vnd.android.package-archive");
                     new File(Environment.getExternalStorageDirectory() + "/JIAYUES/APP/Jiayu.apk").delete();
                 }
 
             }
             request.setDestinationInExternalPublicDir("/JIAYUES/APP/", nombreFichero);
 
             DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgIniciandoDescarga) + " " + nombreFichero, Toast.LENGTH_SHORT).show();
             downloadREF = manager.enqueue(request);
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     private void recalcularTelefono() {
         calcularTelefono();
         /*if(modelo.equals(model)){*/
         modelo = model;
 		/*}else{
 			modelo="Rom incorrecta para tu terminal, tu modelo real es: "+model;
 		}*/
     }
 
     private void calcularTelefono() {
         Resources res = this.getResources();
         try {
 
             int height = 0;
             int width = 0;
             String procesador = "";
             String ram = "";
             String chip = "";
             String buildprop = "";
 
             try {
                 DisplayMetrics dm = new DisplayMetrics();
                 getWindowManager().getDefaultDisplay().getMetrics(dm);
                 height = dm.heightPixels;
                 width = dm.widthPixels;
                 //procesador=Build.HARDWARE;
                 procesador = getInfoCPU();
                 int orientation = getResources().getConfiguration().orientation;
                 FileInputStream fis = new FileInputStream(new File("/system/build.prop"));
                 byte[] input = new byte[fis.available()];
                 while (fis.read(input) != -1) {
                     buildprop += new String(input);
                 }
                 if (buildprop.toLowerCase().lastIndexOf("mt6620") != -1) {
                     chip = "MT6620";
                 } else if (buildprop.toLowerCase().lastIndexOf("mt6628") != -1) {
                     chip = "MT6628";
                 } else {
                     chip = "INDEFINIDO";
                 }
 
 
                 boolean levantadoB = levantarBlueTooth();
                 boolean levantadoW = LevantarWifi();
                 if ("MT6628".equals(chip)) {
                     if (!levantadoB && !levantadoW) {
                         chip = "MT6620";
                     }
 
                 } else if ("MT6620".equals(chip)) {
                     if (!levantadoB && !levantadoW) {
                         chip = "MT6628";
                     }
                 }
 
                 ram = getTotalRAM();
                 int ramInt = (Integer.parseInt(ram) / 1000);
                 if (ramInt <= 290 && ramInt >= 200) {
                     ram = "256MB";
                 } else if (ramInt <= 530 && ramInt >= 300) {
                     ram = "512MB";
                 } else if (ramInt <= 1100 && ramInt >= 900) {
                     ram = "1GB";
                 } else if (ramInt <= 2100 && ramInt >= 1700) {
                     ram = "2GB";
                 }
                 if (width == 720 || (orientation == 2 && height == 720)) {
                     if ("mt6577".equals(procesador.toLowerCase())) {
                         if ("MT6628".equals(chip)) {
                             model = "G3DCN";
                         } else if ("MT6620".equals(chip)) {
                             model = "G3DC";
                         } else {
                             model = "";
                         }
                     } else if ("mt6589".equals(procesador.toLowerCase())) {
                         if ("1GB".equals(ram)) {
                             String modelo = Build.MODEL;
                             String disp = Build.DISPLAY;
                             android.hardware.Camera cam = android.hardware.Camera.open(1);
                             List<Size> supportedPictureSizes = cam.getParameters().getSupportedPictureSizes();
                             int result = -1;
                             for (Iterator iterator = supportedPictureSizes
                                     .iterator(); iterator
                                          .hasNext(); ) {
                                 Size sizes = (Size) iterator.next();
                                 result = sizes.width;
 
                             }
                             cam.release();
                             //if(modelo.indexOf("G3")!=-1 || disp.indexOf("G3")!=-1 || "1200X1600".equals(result)){
                             if (result != -1 && result <= 1600) {
                                 model = getCPUFreqG3();
                             } else {
                                 model = getCPUFreqG4();
                             }
                         } else if ("2GB".equals(ram)) {
                             model = "G4A";
                         } else {
                             model = "";
                         }
                     } else if ("mt6589t".equals(procesador.toLowerCase())) {
                         if ("1GB".equals(ram)) {
                             model = "G4T";
                         } else if ("2GB".equals(ram)) {
                             model = "G4A";
                         } else {
                             model = "";
                         }
                     }
                 } else if (width == 540 || (orientation == 2 && height == 540)) {
 						    		/*if("mt6577".equals(procesador.toLowerCase())){
 						    			if("MT6628".equals(chip)){
 						    				model="G2S";
 						    			}else{
 						    				model="";
 						    			}
 						    		}else{
 					    				model="";
 					    			}*/
                     if ("mt6577t".equals(procesador.toLowerCase())) {
                         model = "G2S";
                     }
                 } else if (width == 480 || (orientation == 2 && height == 480)) {
                     if ("mt6575".equals(procesador.toLowerCase())) {
                         if ((Build.DISPLAY.toUpperCase()).indexOf("G16B") != -1) {
                             model = "G2SCN";
                         } else {
                             model = "G2SC";
                         }
                     } else if ("mt6577".equals(procesador.toLowerCase())) {
                         //FALTA EL Jiayu G2TD
                         if ("512MB".equals(ram)) {
                             if ("MT6628".equals(chip)) {
                                 model = "G2DCPVN";
                             } else if ("MT6620".equals(chip)) {
                                 model = "G2DCPV";
                             } else {
                                 model = "";
                             }
                         } else if ("1GB".equals(ram)) {
                             if ("MT6628".equals(chip)) {
                                 model = "G2DCN";
                             } else if ("MT6620".equals(chip)) {
                                 model = "G2DC";
                             } else {
                                 model = "";
                             }
 
                         } else {
                             model = "";
                         }
                     } else {
                         model = "";
                     }
                 } else if (width == 320 || (orientation == 2 && height == 320)) {
                     if ("256MB".equals(ram)) {
                         model = "G1";
                     }
                 } else {
                     model = res.getString(R.string.msgTerminalNoJiayu);
                 }
 
             } catch (Exception e) {
                 model = res.getString(R.string.msgErrorIdentificar);
             }
 
             if ("".equals(model.trim())) {
                 model = res.getString(R.string.msgTerminalNoJiayu);
             }
 
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }/*
 					}
 				});
 		dialog.show();*/
     }
 
     private boolean levantarBlueTooth() {
         BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         boolean total = false;
         try {
             if (mBluetoothAdapter == null) {
                 total = false;
             } else {
                 if (mBluetoothAdapter.isEnabled()) {
                     total = true;
                 } else {
                     mBluetoothAdapter.enable();
                     synchronized (mBluetoothAdapter) {
                         mBluetoothAdapter.wait(2000);
                     }
                     if (mBluetoothAdapter.isEnabled()) {
                         total = true;
                     } else {
                         total = false;
                     }
                     while (!mBluetoothAdapter.disable()) {
                         mBluetoothAdapter.disable();
                     }
 
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return total;
     }
 
     private boolean LevantarWifi() {
         WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
         boolean total = false;
         try {
 
 
             if (wifiManager.isWifiEnabled()) {
                 total = true;
             } else {
                 wifiManager.setWifiEnabled(true);
                 synchronized (wifiManager) {
                     wifiManager.wait(2000);
                 }
                 if (wifiManager.isWifiEnabled()) {
                     total = true;
                 } else {
                     total = false;
                 }
                 while (wifiManager.isWifiEnabled()) {
                     wifiManager.setWifiEnabled(false);
                 }
             }
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
         return total;
     }
 
     public void addListenerOnButton() {
         try {
 
             imageButton = (ImageButton) findViewById(R.id.imageButton1);
             imageButton.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
 
                     Uri uri = Uri.parse("http://www.jiayu.es");
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(intent);
                 }
 
             });
             descargas = (Button) findViewById(R.id.button1);
             descargas.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     openBrowser(arg0, "downloads");
                 }
 
             });
             about = (Button) findViewById(R.id.button2);
             about.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     try {
                         Intent intent = new Intent(getBaseContext(), AboutActivity.class);
                         startActivity(intent);
                     } catch (Exception e) {
                         Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
                     }
                     /*Uri uri = Uri.parse("http://www.jiayu.es/4-jiayu-accesorios");
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(intent);*/
                 }
 
             });
             salir = (Button) findViewById(R.id.button11);
             salir.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     try {
                         finish();
                     } catch (Exception e) {
                         Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
                     }
                     /*Uri uri = Uri.parse("http://www.jiayu.es/4-jiayu-accesorios");
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(intent);*/
                 }
 
             });
             foro = (Button) findViewById(R.id.button4);
             foro.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     Toast.makeText(getBaseContext(), getResources().getString(R.string.msgTapaTalk), Toast.LENGTH_LONG).show();
                     Uri uri = Uri.parse("http://www.foro.jiayu.es");
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(intent);
                 }
 
             });
             videotutoriales = (Button) findViewById(R.id.button3);
             videotutoriales.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     openBrowserVideo(arg0);
                 }
 
             });
             driversherramientas = (Button) findViewById(R.id.button9);
             driversherramientas.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     openBrowser(arg0, "drivers");
                 }
 
             });
             herramientasROM = (Button) findViewById(R.id.button10);
             herramientasROM.setOnClickListener(new View.OnClickListener() {
 
                 public void onClick(View arg0) {
                     try {
                         Intent intent = new Intent(getBaseContext(), ROMTools.class);
                         intent.putExtra("modelo",modelo);
                         startActivity(intent);
                     } catch (Exception e) {
                         Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
                     }
                 }
 
             });
 
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private String asignaFecha() {
         String fecha_mod=null;
         Calendar cal=Calendar.getInstance();
         String day=String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
         String month=String.valueOf((cal.get(Calendar.MONTH)+1));
         String year=String.valueOf(cal.get(Calendar.YEAR));
         if(day.length()<2){
             day="0"+day;
         }
         if(month.length()<2){
             month="0"+month;
         }
         fecha_mod=(day+"/"+month+"/"+year);
         return fecha_mod;
     }
     private int[] descomponerFecha(String fechaPasada) {
         int day=Integer.parseInt(fechaPasada.trim().split("/")[0]);
         int month=Integer.parseInt(fechaPasada.trim().split("/")[1])-1;
         int year=Integer.parseInt(fechaPasada.trim().split("/")[2]);
         int fecha[]=new int[3];
         fecha[0]=day;
         fecha[1]=month;
         fecha[2]=year;
         return fecha;
     }
 
     public void openBrowser(View v, String tipo) {
         try {
             Intent intent = new Intent(this, BrowserActivity.class);
             intent.putExtra("modelo", modelo);
             intent.putExtra("tipo", tipo);
             startActivity(intent);
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     public void openBrowserVideo(View v) {
 
         try {
             Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/channel/UCL1i90sCYqJhehj45dM2Qhg/videos"));
             startActivity(myIntent);
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     public static String getTotalRAM() throws Exception {
         RandomAccessFile reader = null;
         String load = null;
         try {
             reader = new RandomAccessFile("/proc/meminfo", "r");
             load = reader.readLine();
             load = load.replaceAll(" ", "");
             int indexOf = load.indexOf(":");
             int indexOf2 = load.toLowerCase().indexOf("kb");
             load = load.substring(indexOf + 1, indexOf2);
             load = load.trim();
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         return load;
     }
 
     public static String getInfoCPU() throws Exception {
         RandomAccessFile reader = null;
         String load = "";
         try {
             reader = new RandomAccessFile("/proc/cpuinfo", "r");
             while (load.toLowerCase().indexOf("hardware") == -1) {
                 load = reader.readLine();
             }
 
             load = load.replaceAll(" ", "");
             load = load.replaceAll("\t", "");
             load = load.toLowerCase();
             int indexOf = load.indexOf(":");
             int indexOf2 = load.length();
             load = load.substring(indexOf + 1, indexOf2);
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         return load.trim();
     }
 
     public static String getCPUFreqG4() throws Exception {
         RandomAccessFile reader = null;
         String load = "";
         try {
             reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
             load = reader.readLine();
             int cpufreq = Integer.parseInt(load.trim());
             if (cpufreq > 1300000) {
                 load = "G4T";
             } else {
                 load = "G4B";
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         return load.trim();
     }
 
     public static String getCPUFreqG3() throws Exception {
         RandomAccessFile reader = null;
         String load = "";
         try {
             reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
             load = reader.readLine();
             int cpufreq = Integer.parseInt(load.trim());
             if (cpufreq > 1300000) {
                 load = "G3QCT";
             } else {
                 load = "G3QC";
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         return load.trim();
     }
 
     @Override
     public void processFinish(String output) {
         try {
             if (output != null && !"TIMEOUT----".equals(output)) {
 
                 String inicio = output.split("-;-")[0];
                 output = output.split("-;-")[1];
                 String[] split = output.split("----");
                 newversion = split[0].split(" ")[1];
                 urlActualizacion = split[1];
                 if (!"".equals(urlActualizacion) && !nversion.equals(newversion) && (Float.parseFloat(nversion.replaceAll("Jiayu.es ", "")) < Float.parseFloat(newversion.replaceAll("Jiayu.es ", "")))) {
                     Resources res = this.getResources();
                     AlertDialog dialog = new AlertDialog.Builder(this).create();
                     dialog.setMessage(res.getString(R.string.msgComprobarVersion) + " " + nversion + "->" + newversion + " " + res.getString(R.string.msgPreguntaVersion));
                     dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                             res.getString(R.string.cancelarBtn),
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int witch) {
                                 }
                             });
                     dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                             res.getString(R.string.aceptarBtn),
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int witch) {
                                     try {
                                         ActualizarVersion();
                                     } catch (Exception e) {
                                         Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             });
                     dialog.show();
                 } else {
                     if ("".equals(inicio)) {
                         Resources res = this.getResources();
                         AlertDialog dialog = new AlertDialog.Builder(this).create();
                         dialog.setMessage(res.getString(R.string.msgLastVersion));
                         dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                                 res.getString(R.string.aceptarBtn),
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int witch) {
                                     }
                                 });
                         dialog.show();
                     }
                     if((split.length-2)>0){
                         String fecha=null;
                         String model=null;
                         boolean modeloEncontrado=false;
                         for (int x =2;x<split.length;x++){
                             model=split[x].split("->")[0];
                             fecha=split[x].split("->")[1];
                             if(modelo.equals(model)){
                                 modeloEncontrado=true;
                                 break;
                             }
                         }
                         /*if(modeloEncontrado){
                             String fechaAcceso=ajustes.getString("fechaUltimoAccesoDescargas",fecha);
 
                             int[] ints = descomponerFecha(fechaAcceso);
                             Calendar calAcceso=Calendar.getInstance();
                             calAcceso.set(Calendar.DAY_OF_MONTH,ints[0]);
                             calAcceso.set(Calendar.MONTH,ints[1]);
                             calAcceso.set(Calendar.YEAR,ints[2]);
                             calAcceso.set(Calendar.HOUR,0);
                             calAcceso.set(Calendar.MINUTE,0);
                             calAcceso.set(Calendar.SECOND,0);
                             calAcceso.set(Calendar.MILLISECOND,0);
                             int[] ints1 = descomponerFecha(fecha);
                             Calendar calModificacion=Calendar.getInstance();
                             calModificacion.set(Calendar.DAY_OF_MONTH,ints1[0]);
                             calModificacion.set(Calendar.MONTH,ints1[1]);
                             calModificacion.set(Calendar.YEAR,ints1[2]);
                             calModificacion.set(Calendar.HOUR,0);
                             calModificacion.set(Calendar.MINUTE,0);
                             calModificacion.set(Calendar.SECOND,0);
                             calModificacion.set(Calendar.MILLISECOND,0);
                             if(calModificacion.after(calAcceso)){
                                 mNotificationManagerNews = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                 final Notification notifyDetails = new Notification(R.drawable.ic_launcher,getBaseContext().getResources().getString(R.string.ntfMinTxt),System.currentTimeMillis());
                                 CharSequence contentTitle = getBaseContext().getResources().getString(R.string.ntfTituloTxt);
                                 CharSequence contentText = getBaseContext().getResources().getString(R.string.ntfDetallesTxt);
                                 Intent launch_intent = new Intent();
                                 launch_intent.setComponent(new ComponentName("es.jiayu.jiayuid", "es.jiayu.jiayuid.BrowserActivity"));
                                 launch_intent.putExtra("modelo", modelo);
                                 launch_intent.putExtra("tipo", "downloads");
                                 PendingIntent intent2;
                                 intent2 = PendingIntent.getActivity(getBaseContext(), 0,
                                         launch_intent, Intent.FLAG_ACTIVITY_NEW_TASK);
 
                                 notifyDetails.setLatestEventInfo(getBaseContext(), contentTitle, contentText, intent2);
                                 mNotificationManagerNews.notify(SIMPLE_NOTFICATION_NEWS, notifyDetails);
                             }
 
                         }*/
                     }
                 }
             }
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
         }
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_update:
                 comprobarVersion(nversion);
                 return true;
             case R.id.action_about:
                 try {
                     Intent intent = new Intent(this, AboutActivity.class);
                     startActivity(intent);
                 } catch (Exception e) {
                     Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
                 }
                 return true;
             case R.id.action_config:
                 try {
                     Intent intent = new Intent(this, ConfigActivity.class);
                     startActivity(intent);
                 } catch (Exception e) {
                     Toast.makeText(getBaseContext(), getResources().getString(R.string.msgGenericError), Toast.LENGTH_SHORT).show();
                 }
                 return true;
             case R.id.action_exit:
                 finish();
             default:
                 return super.onMenuItemSelected(featureId, item);
 
         }
     }
 }
