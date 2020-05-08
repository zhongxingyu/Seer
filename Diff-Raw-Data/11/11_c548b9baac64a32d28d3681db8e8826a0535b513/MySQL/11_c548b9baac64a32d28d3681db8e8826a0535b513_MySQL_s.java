 /*
  * MySQL.java
  * 
  * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
  * 
  * Sortal is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
  */
 
 package nl.lolmewn.sortal;
 
 import java.sql.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 
 public class MySQL {
 
     private String prefix;
     private boolean fault;
     private Main plugin;
     
     private MySQLConnectionPool pool;
 
     public MySQL(Main main, String host, int port, String username, String password, String database, String prefix) {
         this.plugin = main;
         this.prefix = prefix;
         try {
             this.pool = new MySQLConnectionPool("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.setupDatabase();
         this.validateTables();
     }
 
     private void setupDatabase() {
         if (this.isFault()) {
             return;
         }
         this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.prefix + "warps"
                 + "(name varchar(255) PRIMARY KEY NOT NULL, "
                 + "world varchar(255) NOT NULL, "
                 + "x DOUBLE NOT NULL, "
                 + "y DOUBLE NOT NULL, "
                 + "z DOUBLE NOT NULL, "
                 + "yaw float, "
                 + "pitch float, "
                 + "price int, "
                 + "uses int,"
                 + "used int,"
                 + "usedTotalBased boolean,"
                 + "owner varchar(255))");
         this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.prefix + "signs"
                 + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                 + "world varchar(255) NOT NULL, "
                 + "x int NOT NULL, "
                 + "y int NOT NULL, "
                 + "z int NOT NULL, "
                 + "warp varchar(255) NOT NULL, "
                 + "price int,"
                 + "uses int,"
                 + "used int,"
                 + "usedTotalBased boolean,"
                 + "owner varchar(255))");
         this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.prefix + "users"
                 + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                 + "player varchar(255) NOT NULL,"
                 + "used int NOT NULL,"
                 + "warp varchar(255),"
                 + "x int, y int, z int, world varchar(255))");
                 //Example query: [Lolmewn, 2, test, 0,0,0,null], [Lolmewn, 3, null, 50,80,50,world]
     }
 
     public boolean isFault() {
         return fault;
     }
 
     private void setFault(boolean fault) {
         this.fault = fault;
     }
 
     public int executeStatement(String statement) {
         if (isFault()) {
             System.out.println("[Sortal] Can't execute statement, something wrong with connection");
             return 0;
         }
         if(this.plugin.getSettings().isDebug()){
             this.plugin.debug("Executing query: " + statement);
         }
         try {
            Statement state = this.pool.getConnection().createStatement();
             int re = state.executeUpdate(statement);
             state.close();
             return re;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return 0;
     }
 
     public ResultSet executeQuery(String statement) {
         if (isFault()) {
             System.out.println("[Sortal] Can't execute query, something wrong with connection");
             return null;
         }
         if (statement.toLowerCase().startsWith("update") || statement.toLowerCase().startsWith("insert") || statement.toLowerCase().startsWith("delete")) {
             this.executeStatement(statement);
             return null;
         }
         if(this.plugin.getSettings().isDebug()){
             this.plugin.debug("Executing query: " + statement);
         }
         try {
            Statement state = this.pool.getConnection().createStatement();
             ResultSet set = state.executeQuery(statement);
             return set;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public void close() {
         if (isFault()) { 
            System.out.println("[Sortal] Can't close connection, something wrong with it");
             return;
         }
         this.pool.close();
     }
 
     private void validateTables() {
         if (this.isFault()) {
             return;
         }
         this.checkColumn(this.prefix + "warps", "uses", "int");
         this.checkColumn(this.prefix + "warps", "used", "int");
         this.checkColumn(this.prefix + "warps", "owner", "varchar(255)");
         this.checkColumn(this.prefix + "warps", "usedTotalBased", "boolean");
         this.checkColumn(this.prefix + "signs", "uses", "int");
         this.checkColumn(this.prefix + "signs", "used", "int");
         this.checkColumn(this.prefix + "signs", "owner", "varchar(255)");
         this.checkColumn(this.prefix + "signs", "usedTotalBased", "boolean");
         this.checkColumn(this.prefix + "signs", "isPrivate", "boolean");
         this.checkColumn(this.prefix + "signs", "privateUsers", "text");
         this.executeStatement("ALTER TABLE " + this.prefix + "warps MODIFY COLUMN x DOUBLE NOT NULL");
         this.executeStatement("ALTER TABLE " + this.prefix + "warps MODIFY COLUMN y DOUBLE NOT NULL");
         this.executeStatement("ALTER TABLE " + this.prefix + "warps MODIFY COLUMN z DOUBLE NOT NULL");
     }
     
     private void checkColumn(String table, String column, String type){
         ResultSet set = this.executeQuery("SELECT * FROM " + table + " LIMIT 1");
         if(set == null){
             return;
         }
         try {
             while(set.next()){
                 set.getObject(column);
             }
         } catch (SQLException ex) {
             System.out.println("Adding column " + column + ",type " + type + " to table " + table + "..");
             this.executeStatement("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
             System.out.println("Added column " + column + ",type " + type + " to table " + table + " succesfully");
         }
     }
 }
