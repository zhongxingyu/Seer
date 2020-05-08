 package net.pixomania.minebackup;
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.SwingWorker;
 import org.apache.commons.io.FileUtils;
 
 /**
  * This SwingWorker will make the copying of files, and stats generation
  * @author vigge_sWe
  */
 public class BackupWorker extends SwingWorker<Void, Void> {
     private String src;
     private String dest;
     private int number;
 
     /**
      * The constructor of the worker. Here we set the passed variables to some
      * local vars
      * @param src Sourcefiles to copy
      * @param dest Destination of the backupped files
      * @param number Current number of backup we are on, for concurrent backups
      */
     public BackupWorker(String src, String dest, int number){
         // Set the params
         this.src = src;
         this.dest = dest+File.separator+"backup"+number;
         this.number = number;
     }
     
     @Override
     protected Void doInBackground() throws Exception {
         try {
             setProgress(0);
 
             // Count the time taken for backup, and ofc copy the files
             long now = System.currentTimeMillis();
             copyDirectory(new File(src), new File(dest));
             long elapsed = System.currentTimeMillis() - now;
 
             // Which is the latest backup?
             File latest = new File(dest.substring(0,dest.length()-8)+File.separator+"latest.txt");
             if(!latest.exists())latest.createNewFile();
             FileWriter fstream = new FileWriter(latest);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write("backup"+number);
             out.close();
 
             // Write stats
             File stats = new File("stats.txt");
            if(!stats.exists())stats.createNewFile();
             Scanner sc = new Scanner(stats);
 
             int backupsThisTime = sc.nextInt();
             int backupsTotal = sc.nextInt();
             long dataThisTime = sc.nextLong();
             long dataTotal = sc.nextLong();
             long time = sc.nextLong();
 
             backupsThisTime++;
             backupsTotal++;
 
             long fileSize = FileUtils.sizeOfDirectory(new File(src));
             dataThisTime += fileSize;
             dataTotal += fileSize;
 
             time += elapsed;
 
             sc.close();
 
             fstream = new FileWriter(stats);
             out = new BufferedWriter(fstream);
 
             out.write(backupsThisTime+" "+ backupsTotal+" "+dataThisTime+" "+dataTotal+" "+time);
             out.close();
 
 
             setProgress(100);
         } catch (Exception ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     
     public void copyDirectory(File sourceLocation , File targetLocation)
     throws IOException {
 
         if (sourceLocation.isDirectory()) {
             if (!targetLocation.exists()) {
                 targetLocation.mkdir();
             }
 
             String[] children = sourceLocation.list();
             for (int i=0; i<children.length; i++) {
                 copyDirectory(new File(sourceLocation, children[i]),
                         new File(targetLocation, children[i]));
             }
         } else {
 
             BufferedInputStream in = new  BufferedInputStream(new FileInputStream(sourceLocation));
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetLocation));
 
             // Copy the bits from instream to outstream
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             
             in.close();
             out.close();
         }
     }
 }
