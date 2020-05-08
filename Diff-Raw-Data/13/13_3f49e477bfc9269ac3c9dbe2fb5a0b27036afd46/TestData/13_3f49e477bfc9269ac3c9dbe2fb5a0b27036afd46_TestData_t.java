 package com.dymczyk;
 
import java.io.InputStream;
 import java.net.URL;
 import java.nio.file.Paths;
 
 public class TestData {
 
   public static int FEW_BLOBS = 100;
   public static int MEDIUM_BLOBS = 1000;
   public static int MANY_BLOBS = 10000;
 
   public static byte[] getBlob() {
     return getBlob(1024);
   }
 
   public static byte[] getBlob(int size) {
     StringBuilder sb = new StringBuilder("");
     for (int i = 0; i < 1024*size; i++){
       sb.append("x");
     }
     return sb.toString().getBytes();
   }
 
   public static String currDir() {
     return Paths.get("").toAbsolutePath().toString();
   }
 
  public static InputStream getResource(String name) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
   }
 }
