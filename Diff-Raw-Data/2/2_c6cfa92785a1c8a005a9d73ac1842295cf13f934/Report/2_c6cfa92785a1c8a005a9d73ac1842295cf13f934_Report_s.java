 package com.carlgo11.preventip;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class Report {
 
 
     public static String Main(Main Main)
     {
             String topic = "Help-log for PreventIp v" + Main.getDescription().getVersion() + " created. The following info is gathered from the config.yml & latest.log.\n\n";
         return topic + "CONFIG: \n{\n" + config(Main).toString() + "}\n\nLatest Log:\n{\n" + latestlog(Main).toString() + "}";
     }
 
     static StringBuilder config(Main Main)
     {
         BufferedReader br = null;
         StringBuilder txt = new StringBuilder();
         try {
             String line;
             br = new BufferedReader(new FileReader("" + Main.getDataFolder() + File.separatorChar + "config.yml"));
             while ((line = br.readLine()) != null) {
                 txt.append(line);
                 txt.append("\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (br != null) {
                     br.close();
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
         return txt;
     }
 
     static StringBuilder latestlog(Main Main)
     {
 
         BufferedReader br = null;
         StringBuilder txt = new StringBuilder();
         if (Main.getConfig().getBoolean("send-log")) {
             try {
                 String line;
                 br = new BufferedReader(new FileReader("logs" + File.separatorChar + "latest.log"));
                 while ((line = br.readLine()) != null) {
                     txt.append(line);
                     txt.append("\n");
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 try {
                     if (br != null) {
                         br.close();
                     }
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
         } else {
            txt.append("Access denied for latest.log. Contact the Server Owner");
         }
         return txt;
     }
 }
