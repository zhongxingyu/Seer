 package core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Model {
 
   public Map<String,List<Map<String,String>>> tables = 
     new HashMap<String,List<Map<String,String>>>();
   
   private Map<String,List<String>> schemas = 
     new HashMap<String,List<String>>();
 
   public Model(File infile) {
     try {
       // 1st pass for schemas.
       BufferedReader br = new BufferedReader(new InputStreamReader(
           new FileInputStream(infile)));
       String line = null;
       while ((line = br.readLine()) != null) {
         line = line.trim();
        if (line.matches("%\\s*schema:.*")) {
           String[] names = line.split(":")[1].split(",");
           List<String> columnNames = new ArrayList<String>();
           for (String name: names) columnNames.add(name.trim());
           schemas.put(columnNames.remove(0).trim(), columnNames);
         }
       }
       // Fill in tables from schema.
       for (String tableName: schemas.keySet()) {
         tables.put(tableName + "S", new ArrayList<Map<String,String>>());
       }
       // 2nd pass for table entries.
       br = new BufferedReader(new InputStreamReader(
         new FileInputStream(infile)));
       line = null;
       while ((line = br.readLine()) != null) {
         line = line.trim();
         if (line.matches("\\s*\\w+\\s*(.*)\\.")) {
           String tableName = line.split("\\(")[0].trim();
           String[] columnNames = line.substring(
             line.indexOf("(") + 1, line.lastIndexOf(")")).split(",");
           if (!schemas.containsKey(tableName)) continue;
           List<Map<String,String>> entries = tables.get(tableName+"S");
           Map<String,String> newEntry = new HashMap<String,String>();
           for (int i = 0; i < schemas.get(tableName).size(); i++) {
             newEntry.put(
               schemas.get(tableName).get(i), columnNames[i].trim());
           }
           entries.add(newEntry);
         }
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 }
