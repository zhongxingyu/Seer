 package com.protocollabs.android.cellperf;
 
 import java.util.Locale;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 import android.os.Bundle;
 
 
 public class Pinger {
 
     public static String ping(String url) {
 
         int count = 0;
         String str = "";

         try {
             int i;
             char[] buffer = new char[4096];
 
             Process process = Runtime.getRuntime().exec( "/system/bin/ping -c 8 " + url);
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                         process.getInputStream()));
             StringBuffer output = new StringBuffer();
 
             while ((i = reader.read(buffer)) > 0)
                 output.append(buffer, 0, i);
             reader.close();
 
             str = output.toString();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return str;
     }
 }
