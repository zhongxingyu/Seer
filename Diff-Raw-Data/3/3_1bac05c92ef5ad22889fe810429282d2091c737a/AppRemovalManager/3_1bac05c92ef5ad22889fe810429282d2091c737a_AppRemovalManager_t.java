 package com.caspian.android.removal;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class AppRemovalManager
 {
     private static final String BACKUP_DIR = "/sdcard/sdx/backup/app";
     private File backupDir;
     private String appDir = "/system/app/";
     
     public AppRemovalManager()
     {
         // specify the backup directory
         backupDir = new File(BACKUP_DIR);
     }
     
     public void remountSystemDir(boolean writeable) throws IOException, InterruptedException
     {
         String cmd[] = new String[3];
         cmd[0] = "su";
         cmd[1] = "-c";
         cmd[2]= "mount -t rfs -o remount,";
         cmd[2] += (writeable ? "rw" : "ro");
         cmd[2] += " /dev/stl5 /system";
 
         // get the runtime object
         Runtime r = Runtime.getRuntime();
         Process p = r.exec(cmd);     
         
         if (p.waitFor() != 0)
         {
             throw new IOException("Error could not mount /system: \n" + 
                 getProcessError(p));
         }
     }
 
     /**
      * Make sure the backup dir exists.
      */
     public void createBackupDir()
     {
         if (!backupDir.exists())
         {
             backupDir.mkdirs();
         }
     }
 
     /**
      * Check if this file has been backed up.
      * 
      * @param fileName
      * @return true if there's a backup
      */
     public boolean backupExists(String fileName)
     {
         File testFile = new File(backupDir.getAbsolutePath() + File.separator
             + fileName);
 
         return testFile.exists();
     }
     
 
     /**
      * Backup the given filename to the backup dir
      * 
      * @param filename
      *            The name of the file to backup
      * @throws Exception 
      * @throws InterruptedException 
      */
     public void backupFile(String fileName) throws Exception
     {
        // make sure backup dir exists
        createBackupDir();
        
         // get the runtime object
         Runtime r = Runtime.getRuntime();
 
         String cmd = "busybox cp /system/app/" + fileName + " "
             + backupDir.getAbsolutePath();
         Process p = r.exec(cmd);
 
         if (p.waitFor() != 0)
         {
             throw new Exception("Error could not delete file " + 
                 fileName + ": \n" + getProcessError(p));
         }
     }
 
     /**
      * Delete the list of files
      * 
      * @param files
      * @throws IOException
      */
     public void deleteFiles(ArrayList<String> files) throws Exception
     {
         remountSystemDir(true);
 
         try 
         {
             String fileName;
             for (String f : files)
             {
                 fileName = f;
                     deleteFile(fileName);
                 
             }
         }
         finally
         {
             remountSystemDir(false);
         }
     }
 
 
     /**
      * 
      * @param fileName
      * @throws Exception 
      * @throws Exception 
      * @throws InterruptedException
      */
     public void deleteFile(String fileName) throws Exception 
     {
         // get the runtime object
         Runtime r = Runtime.getRuntime();
 
         String cmd[] = new String[3];
         cmd[0] = "su";
         cmd[1] = "-c";
         cmd[2]= "rm /system/app/" + fileName;
         Process p = r.exec(cmd);
 
         if (p.waitFor() != 0)
         {
             throw new Exception("Error could not delete file " + 
                 fileName + ": \n" + getProcessError(p));
         }
     }
 
     public static String getProcessOutput(Process p) throws IOException
     {
         BufferedReader br = new BufferedReader(
             new InputStreamReader(p.getErrorStream()));
         StringBuffer sb = new StringBuffer();
         String line;
         while ((line = br.readLine()) != null)
         {
             sb.append(line).append("\n");
         }
         return sb.toString();
     }
 
     public static String getProcessError(Process p) throws IOException
     {
         BufferedReader br = new BufferedReader(
             new InputStreamReader(p.getErrorStream()));
         StringBuffer sb = new StringBuffer();
         String line;
         while ((line = br.readLine()) != null)
         {
             sb.append(line).append("\n");
         }
         return sb.toString();
     }
 }
