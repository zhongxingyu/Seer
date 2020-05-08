 package com.nigorojr.typebest;
 
 import java.sql.SQLException;
 import java.util.LinkedHashMap;
 
 public class Tester {
 
     public static void main(String[] args) {
         // This is to test the behavior of the Database class
         try {
             Records rec = new Records();
             System.out.println("exist?\n" + rec.isTableExist());
             LinkedHashMap<String, String> pair = new LinkedHashMap<String, String>();
             pair.put("foo", "int");
             pair.put("hoge", "varchar(50)");
             if (!rec.isTableExist()) {
                 rec.createTable(pair);
             }
             System.out.println("finished creating...");
             LinkedHashMap<String, String> ins = new LinkedHashMap<String, String>();
             ins.put("foo", "50");
             ins.put("hoge", "'barbar'");
             rec.insert(ins);

             // Test for Preferences class
             Preferences p = new Preferences("test_user");
             p.readPreferencesForID(1);
             System.out.println(p.getKeyboardLayout());
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
 }
