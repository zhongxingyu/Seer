 package ru.shutoff.caralarm;
 
 import android.os.Environment;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Date;
 
 public class State {

     static public void appendLog(String text) {
         File logFile = Environment.getExternalStorageDirectory();
         logFile = new File(logFile, "car.log");
         if (!logFile.exists()) {
             try {
                 if (!logFile.createNewFile())
                     return;
             } catch (IOException e) {
                 // ignore
             }
         }
         try {
             //BufferedWriter for performance, true to set append to file flag
             BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
             Date d = new Date();
             buf.append(d.toLocaleString());
             buf.append(" ");
             buf.append(text);
             buf.newLine();
             buf.close();
         } catch (IOException e) {
             // ignore
         }
     }
 
     static public void print(Throwable ex) {
         ex.printStackTrace();
         appendLog("Error: " + ex.toString());
         StringWriter sw = new StringWriter();
         ex.printStackTrace(new PrintWriter(sw));
         String s = sw.toString();
         appendLog(s);
     }

 }
