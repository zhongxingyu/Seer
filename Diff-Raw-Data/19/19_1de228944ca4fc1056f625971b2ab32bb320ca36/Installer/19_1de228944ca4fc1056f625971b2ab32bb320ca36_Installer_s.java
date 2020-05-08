 package de.skuzzle.polly.installer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 
 import de.skuzzle.polly.installer.util.Environment;
 import de.skuzzle.polly.installer.util.FileUtil;
 import de.skuzzle.polly.installer.util.PollyConfiguration;
 import de.skuzzle.polly.process.JavaProcessExecutor;
 import de.skuzzle.polly.process.ProcessExecutor;
 
 
 
 public class Installer {   
     
     
     public static void main(String[] args) {        
         boolean runPolly = true;
         String pollyParams = "";
         List<String> fileNames = null;
         TreeStream log = null;
         try {
             log = new TreeStream(new FileOutputStream("update.log"), 4);
             System.setOut(log);
         } catch (FileNotFoundException e1) {
             log = new TreeStream(System.out, 4);
         }
         log.println("INSTALL LOG FROM: " + (new Date()).toString());
         
         log.println("ENVIRONMENT");
         log.indent();
         log.println("ARGS: " + Arrays.toString(args));
         log.println("JAVA VERSION: " + System.getProperty("java.version"));
         log.println("JAVA HOME: " + System.getProperty("java.home"));
         log.println("POLLY_HOME: " + Environment.POLLY_HOME.getAbsolutePath());
         log.println("POLLY_CONFIG: " + Environment.POLLY_CONFIG_DIR.getAbsolutePath());
         log.println("POLLY_CFG: " + Environment.POLLY_CONFIG_FILE.getAbsolutePath());
         log.println("POLLY_PLUGINS: " + Environment.POLLY_PLUGIN_DIR.getAbsolutePath());
         log.unindent();
         
         for (int i = 0; i < args.length; ++i) {
             try {
                 if (args[i].equals("-nopolly")) {
                     runPolly = false;
                 } else if (args[i].equals("-pp")) {
                     pollyParams = args[++i];
                 } else if (args[i].equals("-f")) {
                     fileNames = Arrays.asList((args[++i].split(";")));
                 } else {
                     log.println("Unbekannter Parameter: " + args[i]);
                     return;
                 }
             } catch (ArrayIndexOutOfBoundsException e) {
                 log.println("Fehlender Parameter für " + args[i - 1]);
                 return;
             }
         }
 
         if (!FileUtil.waitFor("polly.jar")) {
             log.println("polly.jar is not writable. Aborting update.");
             if (fileNames != null) {
                 FileUtil.safeDeletePaths(fileNames, 3);
             }
             return;
         }
         
         try {
             Environment.validate();
             if (PollyConfiguration.getInstance() == null) {
                 throw new IOException("polly configuration error");
             }
         } catch (IOException e) {
             e.printStackTrace(log);
             return;
         }
         
         String updateInfo = "";
         if (fileNames != null && !fileNames.isEmpty()) {
             updateInfo = installAll(fileNames, log);
         } else {
             log.println("NO FILES TO INSTALL");
             return;
         }
         
         if (runPolly) {
             Installer.runPolly(log, updateInfo, pollyParams);
         }
         System.exit(0);
     }
     
     
     
     private static void runPolly(TreeStream log, String updateInfo, String pollyParam) {
         PollyConfiguration cfg = PollyConfiguration.getInstance();
         if (cfg == null) {
             log.println("COULD NOT READ POLLY CFG");
             return;
         }
         
         try {  
             ProcessExecutor pe = JavaProcessExecutor.getOsInstance(false);            
             pe.addCommandsFromString("-jar polly.jar -update false");
             if (!updateInfo.equals("")) {
                 pe.addCommand("-returninfo");
                 pe.addCommand(updateInfo);
             }
             
             if (!pollyParam.equals("")) {
                 pe.addCommandsFromString(pollyParam);
             }
             
             pe.start();
             log.println("EXECUTING: " + pe.toString());
         } catch (Exception e) {
             e.printStackTrace(log);
         }
     }
 
 
     
     
     private static String installAll(List<String> fileNames, TreeStream log) {
         File backup = null;
         try {
             log.println("CREATING BACKUP");
             log.indent();
             backup = FileUtil.createTempDirectory();
             log.println("BACKUP DIRECTORY: " + backup.getAbsolutePath());
             FileUtil.copyContent(Environment.POLLY_HOME, backup);
         } catch (IOException e) {
             log.println("ERROR WHILE CREATING BACKUP");
             e.printStackTrace(log);
             log.println("DELETING DOWNLOADS");
             FileUtil.safeDeletePaths(fileNames, 3);
             return "Error while updating: " + e.getMessage();
         } finally {
             log.unindent();
         }
         
         String updateInfo = "";
         int i = 0;
         for (String file : fileNames) {
             try {
                 installSingle(new File(file), log);
                 ++i;
             } catch (IOException e) {
                 updateInfo = "Error while updating: " + e.getMessage();
                 log.println("ERROR WHILE INSTALLING UPDATE FROM FILE: " + file);
                 log.indent();
                 e.printStackTrace(log);
                 log.unindent();
                 log.println("ROLLING BACK ALL CHANGES");
                 try {
                     FileUtil.copyContent(backup, Environment.POLLY_HOME);
                     FileUtil.deleteRecursive(backup);
                 } catch (IOException e1) {
                     updateInfo += "; Error while restoring backup: " + e.getMessage();
                     log.println("ERROR WHILE RESTORING BACKUP");
                     e1.printStackTrace(log);
                     return updateInfo;
                 } finally {
                     log.println("DELETING TEMP FILES");
                     FileUtil.safeDeletePaths(fileNames, 3);
                 }
             }
         }
         log.println("DELETING BACKUP");
         FileUtil.deleteRecursive(backup);
         if (PollyConfiguration.getInstance() != null) {
             PollyConfiguration.getInstance().store();
         }
         
         updateInfo = "" + i + " items updated";
         return updateInfo;
     }
     
     
     
     private static void installSingle(File file, TreeStream log) throws IOException {
         log.println("INSTALLING " + file);
         File temp = null;
         try {
             log.indent();
             log.println("CREATING TEMP DIR");
             temp = FileUtil.createTempDirectory();
             log.println("EXTRACTING " + file.getAbsolutePath() + " INTO " + temp.getAbsolutePath());
             printFileList(FileUtil.unzip(file, temp), log);
             parseUpdateFile(temp, log);
             updateConfiguration(temp, log);
             log.println("COPYING FROM " + temp.getAbsolutePath() + " TO " + Environment.POLLY_HOME.getAbsolutePath());
             printFileList(FileUtil.copy(temp, Environment.POLLY_HOME), log);
             log.println("DONE");
         } finally {
             log.println("DELETING TEMP DIR");
             FileUtil.deleteRecursive(temp);
             log.println("DELETING ZIP FILE");
             file.delete();
             log.unindent();
         }
     }
     
     
     
     private static void parseUpdateFile(File tempdir, TreeStream log) throws IOException {
         File outdated = new File(tempdir, "update.dat");
         
         if (!outdated.exists()) {
             return;
         }
         
         log.println("PARSING update.dat");
         BufferedReader r = null;
         try {
             r = new BufferedReader(new InputStreamReader(
                     new FileInputStream(outdated)));
 
             String line = null;
             while ((line = r.readLine()) != null) {
                 File f = new File(Environment.POLLY_HOME, line);
                 log.println("OUTDATED: " + f.toString());
                 log.indent();
                 if (!f.exists()) {
                     log.println("NOT FOUND, SKIPPING!");
                 } else {
                     log.println("DELETED: " + f.delete());
                 }
                 log.unindent();
             }
             r.close();
             outdated.delete();
         } finally {
             if (r != null) {
                 r.close();
             }
         }
     }
     
     
     
     private static void updateConfiguration(File tempdir, TreeStream log) throws IOException {
         File cfgUpdate = new File(tempdir, "cfgupdate.cfg");
         
         if (!cfgUpdate.exists()) {
             return;
         }
         
         log.println("UPDATING CONFIGURATION");
         Properties updateCfg = new Properties();
         FileInputStream in = null;
         try {
             in = new FileInputStream(cfgUpdate);
             updateCfg.load(in);
             PollyConfiguration.getInstance().putAll(updateCfg);
         } finally {
             if (in != null) {
                 in.close();
             }
         }
 
        cfgUpdate.delete();
     }
     
     
     
     private static void printFileList(List<File> files, TreeStream log) {
         log.indent();
         for (File file : files) {
             log.println(file.getAbsolutePath());
         }
         log.unindent();
     }
 }
