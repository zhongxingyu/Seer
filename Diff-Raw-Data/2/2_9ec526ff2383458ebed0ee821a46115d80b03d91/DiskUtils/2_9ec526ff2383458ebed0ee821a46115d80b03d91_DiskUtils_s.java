 package eu.stratuslab.storage.disk.utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 
 import eu.stratuslab.storage.disk.main.PersistentDiskApplication;
 
 public class DiskUtils {
 
     // Template for an iSCSI target entry. Fields passed to the formatter should
     // be the path for the disk store and the uuid.
     private static final String TARGET_TEMPLATE = "Target iqn.2011-01.eu.stratuslab:%2$s\n"
             + "\tLun 0 Path=%1$s%2$s/contents,Type=fileio\n\n";
 
     public static String createTargetConfiguration() {
 
         File store = PersistentDiskApplication.DISK_STORE;
         String storePath = store.getAbsolutePath();
 
         StringBuilder sb = new StringBuilder();
 
         String[] files = store.list();
         if (files != null) {
             for (String uuid : files) {
                 sb.append(String.format(TARGET_TEMPLATE, storePath, uuid));
             }
         }
 
         return sb.toString();
     }
 
     public static void writeStringToFile(String contents, File file)
             throws IOException {
 
         Writer writer = null;
 
         try {
 
             writer = new FileWriter(file);
             writer.append(contents);
 
         } catch (IOException e) {
             throw e;
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException consumed) {
                     // TODO: Log this.
                 }
             }
         }
     }
 
     public static void zeroFile(File file, int sizeInGB) throws IOException {
 
         OutputStream ostream = null;
 
         try {
 
             ostream = new FileOutputStream(file);
 
             // Create 1 MB buffer of zeros.
             byte[] buffer = new byte[1024000];
 
             for (int i = 0; i < 1000 * sizeInGB; i++) {
                 ostream.write(buffer);
             }
 
         } catch (IOException e) {
             throw e;
         } finally {
             if (ostream != null) {
                 try {
                     ostream.close();
                 } catch (IOException consumed) {
                     // TODO: Log this.
                 }
             }
         }
 
     }
 
     public static void restartServer() throws IOException {
 
        File cfgFile = new File("/etc/iet/iet.conf");
         File initFile = new File("/etc/init.d/iscsi-target");
 
         if (cfgFile.canWrite()) {
 
             String contents = DiskUtils.createTargetConfiguration();
             DiskUtils.writeStringToFile(contents, cfgFile);
 
             if (initFile.canExecute()) {
                 ProcessBuilder pb = new ProcessBuilder(initFile
                         .getAbsolutePath(), "restart");
                 Process process = pb.start();
 
                 boolean blocked = true;
                 while (blocked) {
                     try {
                         process.waitFor();
                         blocked = false;
                     } catch (InterruptedException consumed) {
                         // Just continue with the loop.
                     }
                 }
                 int rc = process.exitValue();
 
                 if (rc != 0) {
                     // FIXME: Log this.
                     System.err.println("iscsi-target restart failed: " + rc);
                 }
             } else {
                 // FIXME: Log this.
                 System.err.println("cannot run iscsi-target script");
             }
 
         } else {
             // FIXME: Log this.
             System.err.println("cannot write to iet.conf file");
         }
 
     }
 
 }
