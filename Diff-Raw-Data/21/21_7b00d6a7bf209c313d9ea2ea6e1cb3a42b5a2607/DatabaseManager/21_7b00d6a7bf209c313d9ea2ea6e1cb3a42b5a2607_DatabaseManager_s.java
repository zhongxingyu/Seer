 /*
  *    Copyright 2011-2012 University of Toronto
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 /*
  *    Copyright 2011-2012 University of Toronto
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.ut.biolab;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Manager of the database responsible for operations affecting database as a
  * whole.
  * 
  * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
  * 
  */
 public class DatabaseManager {
     private static DatabaseManager instance = null;
 
     protected DatabaseManager() {
         // exists only to defeat instantiation.
     }
 
     public static DatabaseManager getInstance() {
         if (instance == null) {
             instance = new DatabaseManager();
         }
 
         return instance;
     }
 
     /**
      * Constructs file name to store a table in.
      * 
      * @param table
      *            table name
      * @return file name
      */
     public static String getFileForTable(String table) {
         return "/tmp/" + table + ".out";
     }
 
     /**
      * Retrieves the list of tables in a database.
      * 
      * @param c
      *            connection
      * @return list of tables
      */
     public List<String> getTables(Connection c) {
         List<String> tables = new ArrayList<String>();
 
         try {
             PreparedStatement p = c.prepareStatement("SHOW TABLES");
             ResultSet rs = p.executeQuery();
 
             while (rs.next()) {
                 tables.add(rs.getString(1));
             }
         } catch (SQLException e) {
             System.err.println("Could not retrieve tables.");
         }
 
         return tables;
     }
 
     /**
      * Saves table to a file.
      * 
      * @param table
      *            table to save
      */
     public void exportTable(Connection c, String table) {
         System.out.println("Exporting table '" + table + "' into file '" + getFileForTable(table) + "'.");
 
         PreparedStatement p;
         try {
            p = c.prepareStatement("SELECT * FROM " + table + " INTO OUTFILE '" + getFileForTable(table) + "' fields terminated by '\t'");
             p.execute();
         } catch (SQLException e) {
             System.err.println("Failed to export table '" + table + "'.");
         }

        System.out.println("Export of table '" + table + "' finished.");
     }
 
     /**
      * Exports all the tables in the database to files.
      * 
      * @param c
      *            connection
      */
     public void exportDb(Connection c) {
         List<String> tables = getTables(c);
 
         for (String s : tables) {
             exportTable(c, s);
         }
     }
 }
