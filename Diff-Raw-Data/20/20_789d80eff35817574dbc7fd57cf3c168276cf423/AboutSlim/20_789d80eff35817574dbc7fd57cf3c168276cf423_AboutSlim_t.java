 /*=========================================================================
  *
  *  PROJECT:  SlimRoms
  *            Team Slimroms (http://www.slimroms.net)
  *
  *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
  *            All rights reserved
  *
  *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
  *
  *  AUTHORS:     fronti90, blk_jack
  *  DESCRIPTION: SlimCenter: manage your ROM
  *
  *=========================================================================
  */
 package com.slim.center;
 
 import com.slim.ota.R;
 import com.slim.sizer.SlimSizer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class AboutSlim extends Fragment{
 
     private LinearLayout website;
     private LinearLayout source;
     private LinearLayout donate;
     private LinearLayout irc;
     private LinearLayout report;
     private String mStrDevice;
     private boolean su=false;
     private static final String FILENAME_PROC_VERSION = "/proc/version";
     private static final String LOG_TAG = "DeviceInfoSettings";
     private static Intent IRC_INTENT = new Intent(Intent.ACTION_VIEW, Uri.parse("ccircslim:1"));
     public File path;
     public String zipfile;
     public String logfile;
     public String last_kmsgfile;
     public String kmsgfile;
     public String systemfile;
     Process superUser;
     DataOutputStream ds;
     byte[] buf = new byte[1024];
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.slim_about, container, false);
         return view;
     }
 
     private final View.OnClickListener mActionLayouts = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if (v == website) {
                 launchUrl("http://slimroms.net/");
             } else if (v == source) {
                 launchUrl("http://github.com/SlimRoms");
             } else if (v == donate) {
                 launchUrl("http://www.slimroms.net/index.php/donations");
             } else if (v == irc) {
                 if (isCallable(IRC_INTENT)){
                     startActivity(IRC_INTENT);
                 } else {
                     toast(getResources().getString(R.string.no_irc));
                 }
             } else if (v == report) {
                 bugreport();
             }
         }
     };
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         //set LinearLayouts and onClickListeners
 
         website = (LinearLayout) getView().findViewById(R.id.slim_website);
         website.setOnClickListener(mActionLayouts);
 
         source = (LinearLayout) getView().findViewById(R.id.slim_source);
         source.setOnClickListener(mActionLayouts);
 
         donate = (LinearLayout) getView().findViewById(R.id.slim_donate);
         donate.setOnClickListener(mActionLayouts);
 
         irc = (LinearLayout) getView().findViewById(R.id.slim_irc);
         irc.setOnClickListener(mActionLayouts);
 
         report = (LinearLayout) getView().findViewById(R.id.slim_bugreport);
         report.setOnClickListener(mActionLayouts);
         //request su
         try {
             if (!su){
                 superUser = Runtime.getRuntime().exec("su");
                 ds = new DataOutputStream(superUser.getOutputStream());
                 ds.writeBytes("mount -o remount,rw /system" + "\n");
                 ds.flush();
                 su = true;
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void launchUrl(String url) {
         Uri uriUrl = Uri.parse(url);
         Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
         getActivity().startActivity(donate);
     }
 
     private void toast(String text) {
         // easy toasts for all!
         Toast toast = Toast.makeText(getView().getContext(), text,
                 Toast.LENGTH_SHORT);
         toast.show();
     }
 
     private boolean isCallable(Intent intent) {
         List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(
                 intent, PackageManager.MATCH_DEFAULT_ONLY);
         return list.size() > 0;
     }
     //bugreport
     private void bugreport(){
         try {
          //collect system information
          FileInputStream fstream = new FileInputStream("/system/build.prop");
          DataInputStream in = new DataInputStream(fstream);
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          String strLine;
          while ((strLine = br.readLine()) != null) {
              String[] line = strLine.split("=");
              if (line[0].equalsIgnoreCase("ro.modversion")) {
                  mStrDevice = line[1];
              }
          }
          in.close();
          } catch (Exception e) {
              Toast.makeText(getView().getContext(), getString(R.string.system_prop_error),
                      Toast.LENGTH_LONG).show();
              e.printStackTrace();
          }
         String kernel=getFormattedKernelVersion();
         //check if sdcard is available
         SlimSizer sizer=new SlimSizer();
         short state = sizer.sdAvailable();
         //initialize logfiles
        File extdir = Environment.getExternalStorageDirectory();
        path = new File(extdir.getAbsolutePath().replace("emulated/0", "emulated/legacy") + "/Slim/Bugreport");
         File savefile = new File(path + "/system.log");
         File logcat = new File(path + "/logcat.log");
         File last_kmsg = new File(path + "/last_kmsg.log");
         File kmsg = new File(path + "/kmsg.log");
         File zip = new File(Environment.getExternalStorageDirectory() + "/Slim/bugreport.zip");
         systemfile = savefile.toString();
         logfile = logcat.toString();
         last_kmsgfile = last_kmsg.toString();
         kmsgfile = kmsg.toString();
         zipfile = zip.toString();
         //cleanup old logs
         if (state == 2) {
             try {
                 // create directory if it doesnt exist
                 if (!path.exists()) {
                     path.mkdirs();
                 }
                 if (savefile.exists()) {
                     savefile.delete();
                 }
                 if (logcat.exists()) {
                     logcat.delete();
                 }
                 if (zip.exists()) {
                     zip.delete();
                 }
                 if (last_kmsg.exists()) {
                     last_kmsg.delete();
                 }
                 if (kmsg.exists()) {
                     kmsg.delete();
                 }
              // create savefile and output lists to it
                 FileWriter outstream = new FileWriter(
                         savefile);
                 BufferedWriter save = new BufferedWriter(
                         outstream);
                 save.write("Device: "+mStrDevice+'\n'+"Kernel: "+kernel);
                 save.close();
                 outstream.close();
                 //get logcat and write to file
                 getLogs("logcat -d -f " + logcat + " *:V\n");
                 getLogs("cat /proc/last_kmsg > " + last_kmsgfile + "\n");
                 getLogs("cat /proc/kmsg > " + kmsgfile + "\n");
                 try {
                     Thread.sleep(2000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 //create zip file
                 if (savefile.exists()&&logcat.exists()&&last_kmsg.exists()&&kmsg.exists()) {
                     boolean zipcreated=zip();
                     if (zipcreated==true){
                     dialog(true);
                     } else {
                         dialog(false);
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         } else {
             toast(getResources().getString(
                     R.string.sizer_message_sdnowrite));
         }
     }

     //get kernel information
     private static String getFormattedKernelVersion() {
         try {
             return formatKernelVersion(readLine(FILENAME_PROC_VERSION));
 
         } catch (IOException e) {
             Log.e(LOG_TAG,
                 "IO Exception when getting kernel version for Device Info screen",
                 e);
 
             return "Unavailable";
         }
     }

     public static String formatKernelVersion(String rawKernelVersion) {
 
         final String PROC_VERSION_REGEX =
             "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
             "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
             "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
             "(#\\d+) " +              /* group 3: "#1" */
             "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
             "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */
 
         Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
         if (!m.matches()) {
             Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
             return "Unavailable";
         } else if (m.groupCount() < 4) {
             Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                     + " groups");
             return "Unavailable";
         }
         return m.group(1) + " " +                 // 3.0.31-g6fb96c9
             m.group(2) + " " + m.group(3);
     }
 
     private static String readLine(String filename) throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
         try {
             return reader.readLine();
         } finally {
             reader.close();
         }
     }
     //zipping!
     private boolean zip (){
         String[] source = {systemfile, logfile, last_kmsgfile, kmsgfile};
         try {
             ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
             for (int i=0; i < source.length; i++) {
                 String file = source[i].substring(source[i].lastIndexOf("/"), source[i].length());
                 FileInputStream in = new FileInputStream(source[i]);
                 out.putNextEntry(new ZipEntry(file));
                 int len;
                 while((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
                 out.closeEntry();
                 in.close();
             }
             out.close();
         } catch(Exception e) {
             e.printStackTrace();
             return false;
         }
           return true;
     }
 
     private void getLogs(String command) {
         try {
            Process process = Runtime.getRuntime().exec("su");
             DataOutputStream os = new DataOutputStream(process.getOutputStream());
             os.writeBytes(command);
             os.writeBytes("exit\n");
             os.flush();
             os.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     private void dialog (boolean success){
         final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
         if (success==true){
             alert.setMessage(R.string.report_infosuccess)
                  .setPositiveButton(R.string.ok,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     // action for ok
                                     dialog.cancel();
                                 }
                             });
         } else {
             alert.setMessage(R.string.report_infofail)
                  .setPositiveButton(R.string.ok,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     // action for ok
                                     dialog.cancel();
                                 }
                             });
         }
         alert.show();
     }
}
