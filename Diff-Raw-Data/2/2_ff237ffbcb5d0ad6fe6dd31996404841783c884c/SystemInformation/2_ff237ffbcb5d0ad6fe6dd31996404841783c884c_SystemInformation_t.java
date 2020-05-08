 package com.iopixel;
 
 /*
             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                     Version 2, December 2011
 
  Copyright (C) 2011 <laurent.mallet_at_gmail.com>
 
  Everyone is permitted to copy and distribute verbatim or modified
  copies of this license document, and changing it is allowed as long
  as the name is changed.
 
             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 
   0. You just DO WHAT THE FUCK YOU WANT TO.
 */
 
 import android.app.Activity;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 /* Support us on http://www.iopixel.com */
 public class SystemInformation {
 
     public static String getCurFreq(int id) {
         String maxFreq = "";
 
         try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + id + "/cpufreq/scaling_cur_freq"), 256);
             try {
                 maxFreq = reader.readLine();
             } finally {
                 reader.close();
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         long freq = Long.parseLong(maxFreq);
         freq /= 1000;
         return "" + freq + "Mhz";
     }
 
     public static int getNbCore() {
         File cpu3 = new File("/sys/devices/system/cpu/cpu3");
         if (cpu3.exists() && cpu3.isDirectory()) {
             return 4;
         }
         File cpu2 = new File("/sys/devices/system/cpu/cpu2");
         if (cpu3.exists() && cpu3.isDirectory()) {
             return 3;
         }
         File cpu1 = new File("/sys/devices/system/cpu/cpu1");
         if (cpu2.exists() && cpu2.isDirectory()) {
             return 2;
         }
         return 1;
     }
 
     
 }
