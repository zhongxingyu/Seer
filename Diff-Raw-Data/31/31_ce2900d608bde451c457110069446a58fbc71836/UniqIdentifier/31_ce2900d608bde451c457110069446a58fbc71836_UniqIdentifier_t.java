 package com.eightdigits.sdk.utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.UUID;
 
 import android.content.Context;
 
 public class UniqIdentifier {
   private static String sID = null;
   private static final String INSTALLATION = "INSTALLATION";
 
   public synchronized static String id(String trackingCode, Context context) {
       if (sID == null) {  
          System.out.println("file creating");
           File installation = new File(context.getFilesDir(), INSTALLATION + "-" + trackingCode);
           try {
               if (!installation.exists())
                   writeInstallationFile(installation);
               sID = readInstallationFile(installation);
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
      } else {
        System.out.println("file exists");
       }
       return sID;
   }
 
   private static String readInstallationFile(File installation) throws IOException {
       RandomAccessFile f = new RandomAccessFile(installation, "r");
       byte[] bytes = new byte[(int) f.length()];
       f.readFully(bytes);
       f.close();
       return new String(bytes);
   }
 
   private static void writeInstallationFile(File installation) throws IOException {
       FileOutputStream out = new FileOutputStream(installation);
       String id = UUID.randomUUID().toString();
       out.write(id.getBytes());
       out.close();
   }
 }
