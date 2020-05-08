 package com.devortex.vortextoolbox.helper;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;

 import org.apache.commons.io.IOUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Environment;
 import android.os.RemoteException;

 import com.devortex.vortextoolbox.R;
 import com.devortex.vortextoolbox.Activity.VorteXToolBox;
 import com.koushikdutta.rommanager.api.IROMManagerAPIService;
 
 public class commandRunner {
 	public final static String SCRIPT_NAME = "surunner.sh";
 	private static String _filesDir;
 
 	public static Process runSuCommand(Context context, String command) throws IOException, InterruptedException
 	{
 		DataOutputStream fout = new DataOutputStream(context.openFileOutput(SCRIPT_NAME, 0));
 		fout.writeBytes(command);
 		fout.close();
 		
 		String[] args = new String[] { "su", "-c", ". " + context.getFilesDir().getAbsolutePath() + "/" + SCRIPT_NAME };
 		Process proc = Runtime.getRuntime().exec(args);
 		return proc;
 	}
 	
 	private static void DeleteRecursive(File dir)
 	{
 		if (dir.isDirectory())
 			for (File child : dir.listFiles())
 				DeleteRecursive(child);
 		dir.delete();
 	}
 	
 	public static void setCarrierLabel(Context context, String carrierLabel) throws IOException, InterruptedException
 	{
 		File eri = new File(context.getFilesDir().getAbsolutePath(), "eri.xml");
 		File workingDir = new File(context.getFilesDir().getAbsolutePath() + "/tmp");
 		File workingZipDir = new File(context.getFilesDir().getAbsolutePath() + "/tmp2");
 		File workingZipDirFramework = new File(workingZipDir + "/system/framework");
 		if (!workingDir.exists())
 		{
 			workingDir.mkdirs();
 		}
 		else
 		{
 			DeleteRecursive(workingDir);
 			workingDir.mkdirs();
 		}
 		if (!workingZipDirFramework.exists())
 		{
 			workingZipDirFramework.mkdirs();
 		}
 		else
 		{
 			DeleteRecursive(workingZipDir);
 			workingZipDirFramework.mkdirs();
 		}
 
 		String origText = String.format("%-16s", context.getString(R.string.eri_originaltext));
 		carrierLabel = String.format("%-16s", carrierLabel);
 		
 		ZipUtils.UnZip(workingDir, "/system/framework/framework-res.apk");
 		File outFramework = new File(workingZipDirFramework, "framework-res.apk");
 		
 		File workingEriDir = new File(workingDir.getAbsolutePath() + "/res/xml");
 		
 		File workingEri = new File(workingEriDir, "eri.xml");
 		
 		IOUtils.copy(new FileInputStream(eri), new FileOutputStream(workingEri));
 		
 		Command cmd = new Command();
 		
 		cmd.Add("$BB sed -i \"s|" + origText + "|" + carrierLabel + "|\" " + workingEri.getAbsolutePath());
 		
 		runSuCommand(context, cmd.get());
 		
 		File zipfile = new File(context.getFilesDir(), context.getString(R.string.zipupdate_name));
 		File sdPath = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name).replace(' ', '_') + "Downloads");
 		if (!sdPath.exists())
 			sdPath.mkdirs();
 		
 		File finalZip = new File(sdPath, context.getString(R.string.zipupdate_name));
 		finalZip.delete();
 		IOUtils.copy(new FileInputStream(zipfile), new FileOutputStream(finalZip));
 		
 		ZipUtils.UnZip(workingZipDir, zipfile.getAbsolutePath());
 		
 		ZipUtils.Zip(context, workingDir, outFramework);
 		
 		ZipUtils.Zip(context, workingZipDir, finalZip);
 		
 		DeleteRecursive(workingDir);
 		DeleteRecursive(workingZipDir);
 	}
 	
 	public static void swapServicesJar(Context context, String fileName) throws InterruptedException, IOException
 	{
 		String zipfile = context.getFilesDir().getAbsolutePath() + "/VorteXUpdater.zip";
 		File sdPath = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name).replace(' ', '_') + "Downloads");
 		if (!sdPath.exists())
 			sdPath.mkdirs();
 		
 		File outZip = new File(sdPath.getAbsolutePath() + "/" + context.getString(R.string.zipupdate_name));
 		outZip.delete();
 		IOUtils.copy(new FileInputStream(zipfile), new FileOutputStream(outZip));
 		
 		File workingDir = new File(context.getFilesDir().getAbsolutePath() + "/tmp");
 		if (!workingDir.exists())
 		{
 			workingDir.mkdirs();
 		}
 		else
 		{
 			DeleteRecursive(workingDir);
 			workingDir.mkdirs();
 		}
 		File frameworkDir = new File(workingDir, "system/framework/");
 		if (!frameworkDir.exists())
 			frameworkDir.mkdirs();
 		FileInputStream fis = new FileInputStream(fileName);
 		FileOutputStream fos = new FileOutputStream(frameworkDir + "/services.jar");
 		IOUtils.copy(fis, fos);
 		ZipUtils.Zip(context, workingDir, outZip);
 		
 		DeleteRecursive(workingDir);
 	}
 	
 	public static void swapBatteryIcons(Context context, String fileName) throws IOException, InterruptedException
 	{
 		File workingDir = new File(context.getFilesDir().getAbsolutePath() + "/tmp");
 		File workingZipDir = new File(context.getFilesDir().getAbsolutePath() + "/tmp2");
 		File workingZipDirFramework = new File(context.getFilesDir().getAbsolutePath() + "/tmp2/system/framework");
 		if (!workingDir.exists())
 		{
 			workingDir.mkdirs();
 		}
 		else
 		{
 			DeleteRecursive(workingDir);
 			workingDir.mkdirs();
 		}
 		if (!workingZipDirFramework.exists())
 		{
 			workingZipDirFramework.mkdirs();
 		}
 		else
 		{
 			DeleteRecursive(workingZipDir);
 			workingZipDirFramework.mkdirs();
 		}
 		
 		ZipUtils.UnZip(workingDir, fileName);
 		
 		File inServices = new File(workingDir, "services.jar");
 		File outServices = new File(workingZipDirFramework, "services.jar");
 		
 		IOUtils.copy(new FileInputStream(workingDir.getAbsolutePath() + "/services.jar"), new FileOutputStream(outServices));
 		
 		inServices.delete();
 		
 		File outFramework = new File(workingZipDirFramework, "framework-res.apk");
 		
 		IOUtils.copy(new FileInputStream("/system/framework/framework-res.apk"), new FileOutputStream(outFramework));
 		
 		String zipfile = context.getFilesDir().getAbsolutePath() + "/VorteXUpdater.zip";
 		File sdPath = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name).replace(' ', '_') + "Downloads");
 		if (!sdPath.exists())
 			sdPath.mkdirs();
 		
 		File outZip = new File(sdPath.getAbsolutePath() + "/" + context.getString(R.string.zipupdate_name));
 		outZip.delete();
 		
 		ZipUtils.UnZip(workingZipDir, zipfile);
 		
 		ZipUtils.Zip(context, workingDir, outFramework);
 		
 		ZipUtils.Zip(context, workingZipDir, outZip);
 		
 		DeleteRecursive(workingDir);
 		DeleteRecursive(workingZipDir);
 	}
 	
 	public static void enableTweaks(Context context, String[] tweaks) throws InterruptedException, IOException
 	{
 		Command command = new Command();
 		
 		command = enableScripts(context, command);
 				
 		for (String s : tweaks)
 		{
 			command.Add("$BB cp -f " + _filesDir + "/" + s + " /system/etc/init.d");
 		}
 		command.Add("$BB chmod -R 555 /system/etc/init.d/");
 		
 		runSuCommand(context, command.get()).waitFor();
 		
 	}
 	
 	public static void disableTweaks(Context context, String[] tweaks) throws InterruptedException, IOException
 	{
 		Command command = new Command();
 		
 		command = enableScripts(context, command);
 		
 		for (String s : tweaks)
 		{
 			command.Add("$BB rm /system/etc/init.d/" + s);
 		}
 		
 		runSuCommand(context, command.get()).waitFor();
 	}
 	
 	public static void enablePBGovernor(Context context) throws InterruptedException, IOException
 	{
 		Command command = new Command();
 		
 		command = enableScripts(context, command);
 		
 		command.Add("$BB cp -f " + _filesDir + "/" + context.getString(R.string.pbgov_script) + " /system/etc/init.d/01gov");
 		command.Add("$BB cp -f " + _filesDir + "/cpufreq_conservative.ko /system/lib/modules/");
 		command.Add("$BB cp -f " + _filesDir + "/symsearch.ko /system/lib/modules/");
 		command.Add("$BB chmod -R 555 /system/etc/init.d/");
 		command.Add("chmod 644 /system/lib/modules/cpufreq_interactive.ko");
 		command.Add("chmod 644 /system/lib/modules/cpufreq_conservative.ko");
 		command.Add("chmod 644 /system/lib/modules/symsearch.ko");
 		command.Add("insmod /system/lib/modules/symsearch.ko");
 		command.Add("insmod /system/lib/modules/cpufreq_conservative.ko");
 		command.Add("echo conservative > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
 		command.Add("$BB echo 40 > /sys/devices/system/cpu/cpu0/cpufreq/conservative/down_threshold");
 		command.Add("$BB echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/conservative/ignore_nice_load");
 		command.Add("$BB echo 160000 > /sys/devices/system/cpu/cpu0/cpufreq/conservative/sampling_rate");
 		command.Add("$BB echo 16 > /sys/devices/system/cpu/cpu0/cpufreq/conservative/freq_step");
 		command.Add("SAMPLING_RATE=$($BB expr `cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_transition_latency` \\* 750 / 1000)");
 		command.Add("echo 95 > /sys/devices/system/cpu/cpufreq/ondemand/up_threshold");
 		command.Add("echo $SAMPLING_RATE > /sys/devices/system/cpu/cpu0/cpufreq/ondemand/sampling_rate");
 		runSuCommand(context, command.get()).waitFor();
 	}
 	
 	public static void enableInteractiveGovernor(Context context) throws InterruptedException, IOException
 	{
 		Command command = new Command();
 		
 		command = enableScripts(context, command);
 		
 		command.Add("$BB cp -f " + _filesDir + "/" + context.getString(R.string.defaultgov_script) + " /system/etc/init.d/01gov");
 		command.Add("$BB cp -f " + _filesDir + "/cpufreq_conservative.ko /system/lib/modules/");
 		command.Add("$BB cp -f " + _filesDir + "/symsearch.ko /system/lib/modules/");
 		command.Add("$BB chmod -R 555 /system/etc/init.d/");
 		command.Add("chmod 644 /system/lib/modules/cpufreq_interactive.ko");
 		command.Add("chmod 644 /system/lib/modules/cpufreq_conservative.ko");
 		command.Add("chmod 644 /system/lib/modules/symsearch.ko");
 		command.Add("insmod /system/lib/modules/symsearch.ko");
 		command.Add("insmod /system/lib/modules/cpufreq_interactive.ko");
 		command.Add("echo interactive > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
 		runSuCommand(context, command.get()).waitFor();
 	}
 	
 	public static void disableGovernors(Context context) throws InterruptedException, IOException
 	{
 		Command command = new Command();
 		
 		command = enableScripts(context, command);
 		
 		command.Add("$BB rm /system/etc/init.d/01gov");
 		runSuCommand(context, command.get()).waitFor();
 		
 		
 	}
 	
 	private static Command enableScripts(Context context, Command command)
 	{
 		File folder = new File("/system/etc/init.d");
 		if (!folder.exists())
 		{
 			folder.mkdir();
 		}
 		
 		_filesDir = context.getFilesDir().getAbsolutePath();
 
 		command.Add("$BB cp -f " + _filesDir + "/pm_init.sh /system/usr/bin/");
 		command.Add("$BB cp -f " + _filesDir + "/sysctl.conf /system/etc/");
 		
 		return command;
 	}
 	
 	public static void cwrDialog(final Context context)
 	{
 		String updateZip = "/" + context.getString(R.string.app_name).replace(' ', '_') + "Downloads/" + context.getString(R.string.zipupdate_name);
 		final String updateZipPath = Environment.getExternalStorageDirectory() + updateZip;
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 	 	   builder.setMessage(R.string.cwr_notice)
 	 	          .setCancelable(false)
 	 	          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	 	              public void onClick(DialogInterface dialog, int id) {
 	 	                   doCWRStuff(context, updateZipPath);
 	 	              }
 	 	          });
 	 	   AlertDialog alert = builder.create();
 	 	   alert.show();
 	}
 	
 	public static void warnNoSD(final Context context)
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage(R.string.nosd_warning)
 			.setCancelable(false)
 			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					((Activity)context).finish();
 				}
 			});
 		AlertDialog alert = builder.create();
 	 	alert.show();
 	}
 	
 	public static void doCWRStuff(Context context, String updateZipPath)
 	{
 		IROMManagerAPIService mService = VorteXToolBox.getService();
 		if (mService != null)
 		{
 			try {
 				mService.installZip(updateZipPath);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void rebootDialog(final Context context, final boolean closeActivity)
     {
 	
  	   AlertDialog.Builder builder = new AlertDialog.Builder(context);
  	   builder.setMessage(R.string.reboot_notice)
  	          .setCancelable(false)
  	          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
  	              public void onClick(DialogInterface dialog, int id) {
  	                   try {
 							runSuCommand(context, "reboot");
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
  	              }
  	          })
  	          .setNegativeButton("No", new DialogInterface.OnClickListener() {
  	              public void onClick(DialogInterface dialog, int id) {
  	            	  if (closeActivity)
  	            	  {
  	            		  ((Activity) context).finish();
  	            	  }
  	            	  else
  	            	  {
  	            		  dialog.cancel();
  	            	  }
  	                   
  	              }
  	          });
  	   AlertDialog alert = builder.create();
  	   alert.show();
     }
 
 	public static void calibrateBattery(Context _context) {
 		Command cmd = new Command();
 		cmd.Add("rm -f /data/system/batterystats.bin");
 		try {
 			runSuCommand(_context, cmd.get()).waitFor();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	public static void fixCWR(Context _context) {
 		Command cmd = new Command();
 		cmd.Add("rm -f /data/.recovery_mode");
 		cmd.Add("rm -f /system/etc/init.d/08cwrboot");
 		try {
 			runSuCommand(_context, cmd.get()).waitFor();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static boolean isSdPresent() {
 
 		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 
 		}
 }
