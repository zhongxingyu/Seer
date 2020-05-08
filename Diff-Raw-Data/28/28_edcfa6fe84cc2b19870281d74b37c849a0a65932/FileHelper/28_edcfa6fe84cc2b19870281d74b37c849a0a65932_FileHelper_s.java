 package me.galaran.mctranslit;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class FileHelper {
 
     public static void extractFileFromJarRoot(String fileName, File targetDir) throws IOException {
 
         InputStream is = FileHelper.class.getResourceAsStream('/' + fileName);
         if (!targetDir.exists())
             targetDir.mkdir();
         OutputStream os = new FileOutputStream(targetDir + File.separator + fileName);
         byte[] buffer = new byte[512];
         int length;
         while ((length = is.read(buffer)) > 0) {
             os.write(buffer, 0, length);
         }
         os.close();
         is.close();
     }
 }
