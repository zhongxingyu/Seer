 package me.chaseoes.timingsparser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class TimingsFile {
 
     File file;
 
     public TimingsFile(File f) {
         file = f;
     }
 
     public String parse() throws IOException {
         BufferedReader in = new BufferedReader(new FileReader(file));
         StringBuilder pluginTimes = new StringBuilder();
         StringBuilder pluginNames = new StringBuilder();
 
         String currentPlugin = null;
         long totalTime = 0;
         pluginTimes.append("https://chart.googleapis.com/chart?cht=p3&chd=t:");
 
         while (in.ready()) {
             String line = in.readLine();
             if (line.contains("Total time ")) {
                 totalTime = Long.parseLong(getWord(line, 3)) + totalTime;
             }
         }
 
         in.close();
         in = new BufferedReader(new FileReader(file));
 
         while (in.ready()) {
             String line = in.readLine();
             if (currentPlugin == null) {
                 currentPlugin = getWord(line, 0);
             }
 
             if (line.contains("Total time ")) {
                 int percent = Math.round((float) Long.parseLong(getWord(line, 3)) * 100 / totalTime);
                 if (percent != 0) {
                    pluginNames.append(currentPlugin);
                     pluginNames.append(" (" + percent + "%)|");
                     pluginTimes.append(percent + ",");
                 }
                 currentPlugin = null;
             }
 
         }
 
         in.close();
         return pluginTimes.toString().substring(0, pluginTimes.toString().length() - 1) + "&chs=750x300&chl=" + pluginNames.toString().substring(0, pluginNames.toString().length() - 1);
     }
 
     public String getWord(String s, int index) {
         String[] words = s.split("\\s+");
         for (int i = 0; i < words.length; i++) {
             if (i == index) {
                 return words[i];
             }
         }
         return null;
     }
 
 }
