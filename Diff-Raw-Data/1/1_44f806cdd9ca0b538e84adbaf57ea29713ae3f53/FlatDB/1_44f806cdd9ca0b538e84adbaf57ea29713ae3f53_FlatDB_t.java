 package com.pvminecraft.FlatDB;
 
 import com.pvminecraft.FlatDB.utils.FileManager;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author s0lder
  */
 public class FlatDB {
     private String path;
     private String name;
     private FileManager fm;
     private LinkedHashMap<String,Row> rows;
     
     public static String sep = " ";
     public static String vSep = ":";
     
     public FlatDB(String p, String f) {
         path = p;
         name = f;
         fm = new FileManager(path, f);
        fm.createNotExists();
         fm.readLines();
         this.getAllRows();
     }
     
     public Row getRow(String index) {
         return (Row) rows.get(index);
     }
     
     private void getAllRows() {
         LinkedHashMap<String,Row> table = new LinkedHashMap<String,Row>();
         LinkedList<String> lines = fm.getLines();
         Row row;
         for(String line : lines) {
             row = Row.fromString(line);
             table.put(row.getIndex(), row);
         }
         this.rows = table;
     }
     
     public void addRow(Row val) {
         rows.put(val.getIndex(), val);
     }
     
     public void removeRow(String in) {
         if(rows.containsKey(in))
             rows.remove(in);
     }
     
     public void update() {
         String output;
         String rowString;
         Set<String> keys = rows.keySet();
         fm.clear();
         for(String key : keys) {
             Row r = rows.get(key);
             rowString = r.toString();
             output = key + FlatDB.sep + rowString;
             fm.appendLine(output);            
         }
         fm.write();
     }
     
     public List<Row> getAll() {
         List<Row> ret = new ArrayList<Row>();
         Set<String> keys = rows.keySet();
         for(String key : keys) {
             Row r = rows.get(key);
             ret.add(r);
         }
         return ret;
     }
 }
