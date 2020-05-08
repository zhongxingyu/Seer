 package ru.smartislav.clock.rdtsc;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public final class RdTscSource {
   static {
     String os = System.getProperty("os.name", "");
     String arch = System.getProperty("os.arch", "");
 
     String suffix = "";
     if (os.equalsIgnoreCase("Mac OS X") && arch.equalsIgnoreCase("x86_64")) {
       suffix = "macosx.jnilib";
    } else if (os.equalsIgnoreCase("Linux") && arch.equalsIgnoreCase("x86_64")) {
       suffix = "linux-64.so";
     } else if (os.equalsIgnoreCase("Linux") && arch.equalsIgnoreCase("x86")) {
       suffix = "linux-32.so";
     }
 
     InputStream in = RdTscSource.class.getClassLoader().getResourceAsStream("jni/clock-native-" + suffix);
     if (in != null) {
       try {
         File tempFile = File.createTempFile("clock-native-", suffix);
         tempFile.deleteOnExit();
         OutputStream tempLib = new FileOutputStream(tempFile);
         try {
           byte[] buf = new byte[4096];
           int len;
           do {
             len = in.read(buf);
             tempLib.write(buf, 0, len);
           } while (len == buf.length);
         } finally {
           tempLib.close();
         }
         System.load(tempFile.getPath());
       } catch (IOException e) {
         e.printStackTrace();
       } finally {
         try {
           in.close();
         } catch (IOException e) {
           e.printStackTrace();
         }
       }
     }
   }
 
   public static native boolean isRdtscConstant();
 
   public static native long rdtsc();
 }
