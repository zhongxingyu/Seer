 package com.ebaytools.util;
 
 import com.ebay.services.finding.SearchItem;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class FileUtil {
 
     private static final Logger log = Logger.getLogger(FileUtil.class);
 
     /**
      * This method reads text from a file.
      *
      * @param file input file
      * @return List output data
      */
     public static List<String> readFile(File file) {
         List<String> list = new ArrayList<String>();
         try {
             FileInputStream fstream = new FileInputStream(file);
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             while ((strLine = br.readLine()) != null)   {
                 if (strLine.contains(";")) {
                     list.add(strLine.split(";")[0]);
                 } else {
                     list.add(strLine);
                 }
             }
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
         return list;
     }
 
     public static void saveResult(Map<Pair, Map<SearchItem, Boolean>> map, String part) {
         File file = new File(part);
         if (map != null) {
             StringBuilder sb = new StringBuilder();
             for (Map.Entry<Pair, Map<SearchItem, Boolean>> entry : map.entrySet()) {
                 sb.append(entry.getKey().getKey()).append(" (").append(entry.getKey().getValue()).append(")").append("\n\n");
                 if (entry.getValue() != null) {
                     for (Map.Entry<SearchItem, Boolean> item : entry.getValue().entrySet()) {
                         sb.append(item.getKey().getItemId()).append("\n");
                     }
                 }
             }
             save(file, sb.toString());
            log.debug("Save was done. file : " + file.getAbsolutePath() + "\n"); ;
         } else {
         }
     }
 
     public static void save(File file, String text) {
         try{
             FileWriter fstream = new FileWriter(file);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(text);
             out.close();
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
     }
 }
