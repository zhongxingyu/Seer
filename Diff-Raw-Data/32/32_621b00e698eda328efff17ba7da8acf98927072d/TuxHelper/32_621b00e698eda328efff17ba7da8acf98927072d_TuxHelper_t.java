 package com.grimmvarg.android.tuxility;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Map;
 import java.util.TreeMap;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.util.Log;
 import android.widget.Toast;
 
 public class TuxHelper {
 	private String settingsDBPath = "/dbdata/databases/com.android.providers.settings/settings.db";
 	private String backupPath = "/sdcard/.tuxility/backup/";
 	private String tuxilityPath = "/sdcard/.tuxility/";
 	private Process suShell = null;
 	private Process userShell = null;
 	private Context tuxilityContext;
 	private static TuxHelper instance = null;
 	private String choosenFile = "";
 	private DatabaseHandler dbHandler;
 //	private String settingsTemp = "/data/data/com.grimmvarg.android.tuxility/databases/settings.db.temp";
 	private String settingsTemp = tuxilityPath + ".settings.db.temp";
 
 	private TuxHelper(Context context) {
 		tuxilityContext = context;
 		dbHandler = new DatabaseHandler(tuxilityContext);
 		File tuxilityDir = new File(tuxilityPath);
 		File backupDir = new File(backupPath);
 		// getting root and cleaning up our mess in cache
 		execute("rm -r /cache/tuxility-*", 1);
 
 		if (!tuxilityDir.exists()) {
 			Log.v("<--- CLIHandler - Setup() --->", "Creating our folders in " + tuxilityDir.toString());
 			tuxilityDir.mkdir();
 			backupDir.mkdir();
 			AssetManager assetManager = context.getAssets();
 			InputStream inputStream = null;
 			try {
 				inputStream = assetManager.open("redbend_ua");
 				OutputStream out = new FileOutputStream(tuxilityPath
 						+ "redbend_ua");
 				byte buf[] = new byte[1024];
 				int len;
 				while ((len = inputStream.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				out.close();
 				inputStream.close();
 				showMessage("Setup Completed");
 			} catch (IOException e) {
 				Log.v("<--- CLIHandler - Setup() --->", e.toString());
 			}
 
 		} else {
 			Log.v("<--- CLIHandler - Setup() --->", "Found file: "
 					+ tuxilityDir.toString());
 		}
 
 	}
 
 	public void backupSettingsDB() {
 		File settingsBackup = new File( backupPath + "settings.db");
 		if(settingsBackup.exists()){
 			showMessage("Backup exist!");
 		}else{
 			execute("cp " + settingsDBPath + " " + backupPath + "settings.db", 1);
 			if(settingsBackup.exists()) showMessage("Success!");
 		}
 	}
 
 	public void restoreSettingsDB() {
 		File settingsBackup = new File( backupPath + "settings.db");
 		if(settingsBackup.exists()){
 			execute("cp " + settingsDBPath + " " + backupPath + "settings.db", 1);
 			showMessage("Backing up /efs");
 		} else {
 			showMessage("No backup found!");
 		}
 
 	}
 
 	public void backupEFS() {
 		File efsBackup = new File( backupPath + "efs-backup.tar.gz");
 		if(efsBackup.exists()){
 			showMessage("Backup exist!");
 		} else {
 			execute("tar zcvf " + backupPath + "efs-backup.tar.gz /efs", 1);
 			if(efsBackup.exists()) showMessage("Success!");
 		}
 	}
 
 	public void restoreEFS() {
 		File efsBackup = new File( backupPath + "efs-backup.tar.gz");
 		if(efsBackup.exists()){
 			execute("tar xvvf " + backupPath + "efs-backup.tar.gz /efs", 1);
			showMessage("Restoring /efs");
 		} else {
 			showMessage("No backup found!");
 		}
 
 	}
 
 	public String unTar(String tarFile) {
 		String timeStamp = now();
 		String path = "/cache/tuxility-" + timeStamp + "/";
 		execute("mkdir " + path, 1);
 		execute("tar " + "-C " + path + " -xf " + tarFile, 1);
 		return path;
 	}
 
 	public void installKernel(String kernelPath) {
 		showMessage("Preparing for flash..");
 		if (kernelPath.contains(".tar")){
 			kernelPath = unTar(kernelPath) + "zImage";
 		}
 		execute("cat " + tuxilityPath + "redbend_ua > /data/redbend_ua", 2);
 		execute("chmod 755 /data/redbend_ua", 2);
 		showMessage("Flashing and Rebooting..");
 		execute("/data/redbend_ua restore " + kernelPath + " /dev/block/bml7", 2);
 	}
 
 	public void backupKernel(String name) {
 		name = now();
 		File kernelBackup = new File(backupPath + name + "-kernelBackup");
 		execute("cat /dev/block/bml7 > " + backupPath + name + "-kernelBackup", 1);
 		if(kernelBackup.exists()){
 			showMessage("Backed up as: " + name + "-kernelBackup");
 		}
 		else {
 			showMessage("Backed up failed!");
 		}
 	}
 	
 	private synchronized void execute(String command, int mode) {
 		Process shell;
 		DataOutputStream toProcess = null;
 		DataInputStream fromProcess = null;
 
 		try {
 			if (mode == 0) {
 				if (userShell == null)
 					userShell = Runtime.getRuntime().exec("sh");
 				shell = userShell;
 			} else {
 				if (suShell == null)
 					suShell = Runtime.getRuntime().exec("su");
 				shell = suShell;
 			}
 			toProcess = new DataOutputStream(shell.getOutputStream());
 			fromProcess = new DataInputStream(shell.getInputStream());
 
 			if(mode != 2) command += " > /dev/null";
 			
 			Log.v("<--- CLIHandler - Execute() --->", "Executing: " + command);
 			
 			toProcess.writeBytes(command + "\n");
 			toProcess.flush();
 			toProcess.writeBytes("echo $?\n");
 			toProcess.flush();
 			Log.v("<--- CLIHandler - Execute() --->", command + " result " + fromProcess.readLine());
 
 		} catch (IOException e) {
 			Log.v("<--- CLIHandler - Execute() --->", e.toString()); 
 		} 
 	}
 
 	public void reboot(String type) {
 		execute("reboot " + type, 1);
 	}
 
 	public static TuxHelper getInstance(Context applicationContext) {
 		if (instance == null) {
 			instance = new TuxHelper(applicationContext);
 		}
 
 		return instance;
 	}
 
 	public static TuxHelper getInstance() {
 		return instance;
 	}
 
 	public void showMessage(String message) {
 		CharSequence text = message;
 		int duration = Toast.LENGTH_SHORT;
 
 		Toast toast = Toast.makeText(tuxilityContext, text, duration);
 		toast.show();
 	}
 
 	public String getChoosenFile() {
 		return choosenFile;
 	}
 
 	public void setChoosenFile(String choosenFile) {
 		this.choosenFile = choosenFile;
 	}
 
 	public static String now() {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHHmmss");
 		return sdf.format(cal.getTime());
 
 	}
 
 	public void clearBatteryStats() {
 		File batteryStats = new File("/data/system/batterystats.bin");
 		execute("rm /data/system/batterystats.bin", 1);
 		if(!batteryStats.exists()) showMessage("Batterystats cleared!");
 
 	}
 
 	public void toggleMediaScanner(Boolean state) {
 		if (state) {
 			execute("pm disable com.android.providers.media/com.android.providers.media.MediaScannerReceiver",1);
 		} else {
 			execute("pm enable com.android.providers.media/com.android.providers.media.MediaScannerReceiver", 1);
 		}
 	}
 
 
 	public void checkoutSettings() {
 		execute("cp " + settingsDBPath + " " + settingsTemp, 1);
 		execute("chown 1001.1015 " + settingsTemp, 1);
 		execute("chmod 777 " + settingsTemp, 1);
 		settingsOpen();
 	}
 	
 	public void checkinSettings(){
 		settingsClose();
 		execute("cp " + settingsTemp + " " + settingsDBPath, 1);
 		execute("chmod 660 " + settingsDBPath, 1);
 		execute("chown system.system " + settingsDBPath, 1);
 		execute("rm " + settingsTemp, 1);
 	}
 
 	public Cursor getSettingsCursor() {
 		Cursor resultCursor = dbHandler.selectAll("secure");
 		return resultCursor;
 	}
 
 	public void updateSettings(String table, String id, String name, String value) {
 		String sql = "UPDATE " + table + " SET " + name + "=\"" + value + "\" WHERE _id=\"" + id + "\"";
 		dbHandler.doUpdate(sql);
 	}
 	
 	public void addSettingsValues(){
 		String sql = "insert into secure (name, value) values(\"wifi_idle_ms\", \"10000\")";
 		dbHandler.doInsert(sql);
 	}
 
 	public void settingsClose() {
 		dbHandler.close();
 		
 	}
 	
 	public void settingsOpen(){
 		dbHandler.open();
 	}
 
 }
