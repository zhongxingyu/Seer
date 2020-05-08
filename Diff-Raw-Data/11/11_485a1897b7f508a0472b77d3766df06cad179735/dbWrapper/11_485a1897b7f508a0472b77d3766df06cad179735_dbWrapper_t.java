 /* 
  * Muni 
  * Copyright (C) 2013 bobbshields <https://github.com/xiebozhi/Muni> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Binary releases are available freely at <http://dev.bukkit.org/server-mods/muni/>.
 */
 package com.teamglokk.muni.utilities;
 
 import com.teamglokk.muni.Citizen;
 import com.teamglokk.muni.Muni;
 import com.teamglokk.muni.Town;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 //import com.teamglokk.muni;
 /**
  * Wraps the database functions to provide simple functions
  * @author BobbShields
  */
 public class dbWrapper extends Muni {
 
     private Muni plugin;
     
     private Connection conn = null;
     private Statement stmt = null;
     public ResultSet rs = null;
     
     public dbWrapper( Muni instance ){
         plugin = instance;
     }
     
     /**
      * Opens a connection to the database
      * @throws SQLException 
      */
     public void db_open() throws SQLException {
         if(plugin.isSQLdebug()){
             String temp = plugin.useMysql() ? "Opening DB (mysql)":"Opening DB (sqlite)" ;
             plugin.getLogger().info(temp);
         }
         String driver = plugin.useMysql() ?"com.mysql.jdbc.Driver" :"org.sqlite.JDBC" ;
         try {
                 Class.forName(driver).newInstance();
             
             } catch (Exception ex){
                 plugin.getLogger().severe("db_open: driver " +driver+ " not found");
                 plugin.getLogger().severe("db_open: "+ex.getMessage() );
             }
         conn = DriverManager.getConnection(plugin.getDB_URL(),plugin.getDB_user(),plugin.getDB_pass());
         // MySQL is not yet tested!!! 31 Jan 2013 RJS
         
         stmt = conn.createStatement();
     }
     
     /**
      * Closes the connection to the database
      * @throws SQLException 
      */
     public void db_close() throws SQLException {
         if ( rs != null) { rs.close(); }
         if ( stmt != null) { stmt.close(); }
         if ( conn != null ) { conn.close();}
         if(plugin.isSQLdebug()){plugin.getLogger().info("Closed DB");}
     }
     
     /**
      * Checks to see if a field exists in a certain table
      * @param table     the table to be used
      * @param col       column name 
      * @param value     check to see if this value exists in the column of the table
      * @return true if exists, false if not
      */
     public boolean checkExistence ( String table, String col, String value ){
         boolean rtn = false;
         String SQL = "SELECT "+col+" FROM "+plugin.getDB_prefix()+table+
                     " WHERE "+col+"='"+value +"';";
         try {
             
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             //rs.next();
             String temp = rs.getString(1);
             plugin.getLogger().info("Here: " + temp ) ;
             if (temp == null || temp.equals("")||temp.equals(null)) {return false; } //NPE fix?
             if (value.equalsIgnoreCase(temp) ){ //NPE on null player
             rtn = true ;
             if(this.isSQLdebug() ){plugin.getLogger().info("checkExistence: value = "+temp);}
             } 
         } catch (SQLException ex){
             if(this.isSQLdebug() ){plugin.getLogger().info( "checkExistence: Value not found: "+table+"."+col+"="+value ); }
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "checkExistence: "+ex.getMessage() ); 
                 rtn = false;
             } finally{}
         }
         return rtn;
     }
     
     /**
      * Get an ArrayList of strings for a whole column 
      * @param table     the table to be used
      * @param column    the column name for the values to return
      * @return          all the results for the specified column, in an array list
      */
     public ArrayList<String> getSingleCol (String table, String column ){
         ArrayList<String> rtn = new ArrayList<String>();
         String SQL = "SELECT "+column+" FROM "+plugin.getDB_prefix()+table+" ORDER BY "+column+";";
         try {
             
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             
             while ( rs.next() ){
                String temp = rs.getString(column);
                rtn.add( temp );
                if (plugin.isSQLdebug()) {plugin.getLogger().info("getSingleCol getting: "+temp);}
            }
             
         } catch (SQLException ex){
             plugin.getLogger().severe( "getSingleCol: "+ex.getMessage() ); 
             rtn = null;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "checkExistence: "+ex.getMessage() ); 
                 rtn = null;
             } finally{}
         }
         return rtn;
     }
     
     /**
      * Gets a list of all the citizens in the specified town
      * @param townName
      * @return      array list of all the citizens in the town
      */
     public ArrayList<String> getTownCits ( String townName ){
         ArrayList<String> rtn = new ArrayList<String>();
         String SQL = "SELECT playerName FROM "+plugin.getDB_prefix()+"citizens WHERE townName='"+townName+"' ORDER BY playerName DESC;";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             
             while ( rs.next() ){
                String temp = rs.getString( "playerName" );
                rtn.add( temp );
                if (plugin.isSQLdebug()) { plugin.getLogger().info("getSingleCol getting: "+temp); }
            }
             
         } catch (SQLException ex){
             plugin.getLogger().severe( "getSingleCol: "+ex.getMessage() ); 
             rtn = null;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "checkExistence: "+ex.getMessage() ); 
                 rtn = null;
             } finally{}
         }
         return rtn;
     }
     /**
      * Gets a list of all the citizens in the specified town in the specified role
      * @param townName
      * @return      array list of all the citizens in the town
      */
     public ArrayList<String> getTownCits ( String townName, String role ){
         ArrayList<String> rtn = new ArrayList<String>();
         String srch = "";
         if (role.equalsIgnoreCase( "mayor" ) ) {
             srch = "mayor";
         } else if (role.equalsIgnoreCase( "deputy" ) ) {
             srch = "deputy";
         } else if (role.equalsIgnoreCase( "citizen" ) ) {
             srch = "citizen";
         } else if (role.equalsIgnoreCase( "invitee" ) ) {
             srch = "invitee";
         } else if (role.equalsIgnoreCase( "applicant" ) ) {
             srch = "applicant";
         } else {plugin.getLogger().warning("getTownCits: failure to specify role"); }
         
         String SQL = "SELECT playerName FROM "+plugin.getDB_prefix()+
                 "citizens WHERE townName='"+townName+"' AND role='"+srch+"' "+
                 "ORDER BY playerName DESC;";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().warning(SQL);}
             rs = stmt.executeQuery(SQL); 
             
             while ( rs.next() ){
                String temp = rs.getString( "playerName" );
                rtn.add( temp );
                if (plugin.isSQLdebug()) { plugin.getLogger().info("getTownCits getting: "+temp); }
            }
             
         } catch (SQLException ex){
             plugin.getLogger().severe( "getTownCits: "+ex.getMessage() ); 
             rtn = null;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "getTownCits: "+ex.getMessage() ); 
                 rtn = null;
             } finally{}
         }
         return rtn;
     }
     /**
      * Gets a list of all the citizens in the specified town in the specified role
      * @param townName
      * @return      array list of all the citizens in the town
      */
     public boolean updateRole ( Town t, String cit ){
         boolean rtn = false;
         
         String SQL = "UPDATE "+plugin.getDB_prefix()+
                 "citizens SET role='"+t.getRole(cit)+
                 "' WHERE playername='"+cit +"';";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().warning(SQL);}
             int resultCount = stmt.executeUpdate(SQL); 
             if (resultCount == 1){
                 return true;
             } 
             return false;
             
         } catch (SQLException ex){
             plugin.getLogger().severe( "updateRole: "+ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "updateRole: "+ex.getMessage() ); 
                 rtn = false;
             } finally{}
         }
         return rtn;
     }
     /**
      * Gets a list of all the citizens in the specified town in the specified role
      * @param townName
      * @return      array list of all the citizens in the town
      */
     public boolean deleteCitizen ( String cit ){
         boolean rtn = false;
         
         String SQL = "DELETE FROM  "+plugin.getDB_prefix()+
                 "citizens WHERE playername='"+cit +"';";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().warning(SQL);}
             int resultCount = stmt.executeUpdate(SQL); 
             if (resultCount == 1){
                 rtn = true;
             } else { rtn =  false; }
         } catch (SQLException ex){
             plugin.getLogger().severe( "deleteCitizen: "+ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "deleteCitizen: "+ex.getMessage() ); 
                 rtn = false;
             } finally{}
         }
         return rtn;
     }
     /**
      * Get all the town data from only the town name 
      * @param townName
      * @return          copy of the specified town
      */
     public Town getTown(String townName){
         Town temp = new Town (plugin) ;
         String SQL = "SELECT "+temp.toDB_Cols()+" FROM "+plugin.getDB_prefix()+"towns WHERE townName='"+townName+"';";
         try {
             db_open();
             if (plugin.isSQLdebug() ){plugin.getLogger().info(SQL); }
             rs = stmt.executeQuery(SQL);
             temp = new Town(plugin,rs.getString("townName"),rs.getString("mayor"), rs.getString("world"),
                     rs.getInt("expansions"), rs.getBoolean("democracy"),
                     rs.getInt("townRank"),rs.getDouble("bankBal"), rs.getDouble("taxRate"),  
                     rs.getInt("itemBal"), rs.getInt("itemTaxRate") );
         } catch (SQLException ex){
             plugin.getLogger().info ( "getTown: "+ townName+" not found in database" );
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "getTown: "+ ex.getMessage() );
             } finally{}
         }
         return temp;
     }
     
     public boolean saveTowns(Collection<Town> towns){
         boolean rtn = false; 
         List<String> updates = new ArrayList<String>();
         List<String> inserts = new ArrayList<String>();
         
         for (Town t : towns){
             if (checkExistence("towns","townName",t.getName() ) ){
                 updates.add("UPDATE "+plugin.getDB_prefix()+"towns SET "+
                     t.toDB_UpdateRowVals()+" WHERE townName='"+t.getName()+"';");
             } else {
                 inserts.add("INSERT INTO "+plugin.getDB_prefix()+"towns ("+
                         t.toDB_Cols()+") VALUES ("+t.toDB_Vals()+");");
             }
         }
         
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info("DB: Saving all towns");}
             if (!updates.isEmpty() ){
                 for (String SQL : updates ) {
                     stmt.executeUpdate(SQL); 
                     if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
                 }
             } 
             if (!inserts.isEmpty() ){
                 for (String SQL : inserts){
                     stmt.executeQuery(SQL);
                     if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
                 }
             }
         } catch (SQLException ex){
             plugin.getLogger().severe("db_saveTowns: "+ ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_saveTowns: "+ ex.getMessage() ); 
                 rtn = false;
             } 
         }
         return rtn; 
     }
     
     /**
      * Gets a citizen from the playerName
      * @param playerName
      * @return              copy of the specified citizen 
      */
     public Citizen getCitizen(String playerName){
         Citizen temp = new Citizen (plugin);
         String SQL = "SELECT "+temp.toDB_Cols() +" FROM "+plugin.getDB_prefix()+"citizens WHERE playerName='"+playerName+"';";
         try {
             db_open();
             rs = stmt.executeQuery(SQL);
             if (plugin.isSQLdebug() ){plugin.getLogger().info(SQL); }
             temp = new Citizen(plugin, rs.getString("townName"), rs.getString("playerName"),
                     rs.getString("role"), rs.getString("invitedBy") ); //,rs.getDate("sentDate") ); also missing lastLogin
         } catch (SQLException ex){
             plugin.getLogger().info( "getCitzien: "+playerName+" not found in database" );
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "getCitzien: "+ ex.getMessage() );
             } finally{}
         }
         return temp;
     }
     
     public boolean saveCitizens(Collection<Citizen> cits){
         boolean rtn = false; 
         List<String> updates = new ArrayList<String>();
         List<String> inserts = new ArrayList<String>();
         
         for (Citizen c : cits){
             if (checkExistence("citizens","playerName",c.getName() ) ){
                 updates.add("UPDATE "+plugin.getDB_prefix()+"citizens SET "+
                     c.toDB_UpdateRowVals()+" WHERE playerName='"+c.getName()+"';");
             } else {
                 inserts.add("INSERT INTO "+plugin.getDB_prefix()+"citizens ("+
                         c.toDB_Cols()+") VALUES ("+c.toDB_Vals()+");");
             }
         }
         
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info("DB: Saving all towns");}
             if (!updates.isEmpty() ){
                 for (String SQL : updates ) {
                     stmt.executeUpdate(SQL); 
                     if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
                 }
             } 
             if (!inserts.isEmpty() ){
                 for (String SQL : inserts){
                     stmt.executeQuery(SQL);
                     if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
                 }
             }
         } catch (SQLException ex){
             plugin.getLogger().severe("db_saveTowns: "+ ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_saveTowns: "+ ex.getMessage() ); 
                 rtn = false;
             } 
         }
         return rtn; 
     }
     /**
      * Insert a single row into the database
      * @param table
      * @param cols
      * @param values
      * @return          true if worked properly
      */
     public boolean insert(String table, String cols, String values) {
         boolean rtn = true;
         String SQL = "INSERT INTO "+plugin.getDB_prefix()+table+" ("+cols+
                 ") VALUES ("+values+");";
             
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             stmt.executeUpdate(SQL); 
         } catch (SQLException ex){
             plugin.getLogger().severe( "dbInsert: "+ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "dbInsert: "+ex.getMessage() ); 
                 rtn = false;
             } finally{}
         }
         return rtn;
     }
     
     /**
      * Updates a single row for a single key
      * @param table
      * @param key_col
      * @param key
      * @param col
      * @param value
      * @return 
      */
     public boolean update(String table, String key_col, String key, String col, String value) {
         
         boolean rtn = true;
         String SQL = "UPDATE "+plugin.getDB_prefix()+table+" SET "+col+"="+value+" WHERE "
                 +key_col+"='"+key+"';";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             stmt.executeUpdate(SQL); 
         } catch (SQLException ex){
             plugin.getLogger().severe("db_update: "+ ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_update: "+ ex.getMessage() ); 
                 rtn = false;
             } 
         }
         return rtn;
     }
     
     /**
      * Updates a row, requires special colsANDvals in SQL format
      * @param table
      * @param key_col
      * @param key
      * @param colsANDvals
      * @return 
      */
     public boolean updateRow(String table, String key_col, String key, String colsANDvals) {
         
         boolean rtn = true;
         String SQL = "UPDATE "+plugin.getDB_prefix()+table+" SET "+colsANDvals+" WHERE "
                 +key_col+"='"+key+"';";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             stmt.executeUpdate(SQL); 
         } catch (SQLException ex){
             plugin.getLogger().severe("db_updateRow: "+ ex.getMessage() ); 
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_updateRow: "+ ex.getMessage() ); 
                 rtn = false;
             } 
         }
         return rtn;
     }
 
     public ArrayList<Transaction> getTaxHistory (Town t, String player){
         ArrayList<Transaction> rtn = new ArrayList<Transaction>();
         
         Transaction temp = new Transaction(plugin);
         String SQL = "SELECT "+temp.toDB_Cols()+" FROM "+plugin.getDB_prefix()+"transactions "+
                 " WHERE playerName='"+player+"' AND townName='"+ t.getName()+"' AND type='taxes' " +
                 "ORDER BY id DESC";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             while (rs.next() ){
                 temp = new Transaction(plugin, rs.getString("townName"), rs.getString("playerName"), 
                         rs.getString("type"), rs.getDouble("amount"),
                         rs.getInt("item_amount") ,rs.getTimestamp("timestamp")); //, rs.getString("notes")
                 rtn.add(temp);
             }
             
         } catch (SQLException ex){
             plugin.getLogger().severe("db_getTaxHistory "+ ex.getMessage() ); 
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_getTaxHistory: "+ ex.getMessage() ); 
             } 
         }
         
         return rtn;
     }
     
     /**
      * Returns the banking history of the specified officer
      * @param t
      * @param officer
      * @return 
      */
     public ArrayList<Transaction> getTownBankHistory (Town t, String officer){
         ArrayList<Transaction> rtn = new ArrayList<Transaction>();
         
         Transaction temp = new Transaction(plugin);
         String SQL = "SELECT "+temp.toDB_Cols()+" FROM "+plugin.getDB_prefix()+"transactions "+
                 " WHERE playerName='"+officer+"' AND townName='"+ t.getName()+"' AND type='bank' " +
                 "ORDER BY id DESC";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             while (rs.next() ){
                 temp = new Transaction(plugin, rs.getString("townName"), rs.getString("playerName"), 
                         rs.getString("type"), rs.getDouble("amount"),
                         rs.getInt("item_amount") ,rs.getTimestamp("timestamp")); //, rs.getString("notes")
                 rtn.add(temp);
             }
             
         } catch (SQLException ex){
             plugin.getLogger().severe("db_getBankHistory "+ ex.getMessage() ); 
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_getBankHistory: "+ ex.getMessage() ); 
             } 
         }
         
         return rtn;
     }
     
     /**
      * Returns a list of regions that belong to the specified town
      * @param t
      * @return 
      */
     public ArrayList<MuniWGRegion> getSubRegions (Town t){
         ArrayList<MuniWGRegion> rtn = new ArrayList<MuniWGRegion>();
         
         MuniWGRegion temp;
         String SQL = "SELECT world,region,type FROM "+plugin.getDB_prefix()+"subregions "+
                " WHERE town='"+ t.getName()+"' ORDER BY id DESC";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             while (rs.next() ){
                 temp = new MuniWGRegion(rs.getString("world"), rs.getString("region"), 
                         rs.getString("type") );
                 rtn.add(temp);
             }
             
         } catch (SQLException ex){
             plugin.getLogger().severe("db_getSubRegions "+ ex.getMessage() ); 
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_getSubRegions: "+ ex.getMessage() ); 
             } 
         }
         return rtn;
     }
     
     public int getNumSubRegions(Town t, String type){
         int rtn = 0;
         
         String SQL = "SELECT COUNT(type) FROM "+plugin.getDB_prefix()+"subregions "+
                 " AS count WHERE townName='"+ t.getName()+"' AND type='"+type+"'";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             rs = stmt.executeQuery(SQL); 
             rtn = rs.getInt("count");
             
         } catch (SQLException ex){
             plugin.getLogger().severe("db_getSubRegions "+ ex.getMessage() ); 
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_getSubRegions: "+ ex.getMessage() ); 
             } 
         }
         return rtn;
     }
     
     /**
      * Deletes a specific subregion from the database
      * @param t
      * @param region
      * @return 
      */
     public boolean deleteSubRegion(Town t, String region){
         boolean rtn = false;
         String SQL = "DELETE FROM "+plugin.getDB_prefix()+"subregions "+
                 "WHERE townName='"+ t.getName()+"' AND region='"+region+"'";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             int resultCount = stmt.executeUpdate(SQL); 
             if (resultCount == 1){
                 rtn = true;
             } else { rtn =  false; }
             
         } catch (SQLException ex){
             plugin.getLogger().severe("db_getSubRegions "+ ex.getMessage() ); 
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_getSubRegions: "+ ex.getMessage() ); 
             } 
         }
         return rtn;
     }
     
     public boolean deleteAllSubRegions(Town t){
         boolean rtn = false;
         String SQL = "DELETE FROM "+plugin.getDB_prefix()+"subregions "+
                 "WHERE townName='"+ t.getName()+"'";
         try {
             db_open();
             if(plugin.isSQLdebug() ){plugin.getLogger().info(SQL);}
             int resultCount = stmt.executeUpdate(SQL); 
             if (resultCount > 0){
                 rtn = true;
             } else { rtn =  false; }
             
         } catch (SQLException ex){
             plugin.getLogger().severe("db_getSubRegions "+ ex.getMessage() ); 
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning("db_getSubRegions: "+ ex.getMessage() ); 
             } 
         }
         return rtn;
     }
     
     /**
      * Creates the database specifically for Muni 
      * @param drops true means the tables will be dropped before creating them again
      * @return      false if there was a problem
      */
     public boolean createDB (boolean drops) { 
         boolean rtn = false;
         String serial;
         String mpk = "";
         String spk = "";
         if (plugin.useMysql() ){
             serial = "AUTO_INCREMENT " ;
             mpk = ", Primary Key (id) " ;
         } else{
             serial = "AUTOINCREMENT " ;
             spk = " Primary Key ";
         }
         String prefix = plugin.getDB_prefix();
         String DROP1 = "DROP TABLE IF EXISTS "+prefix+"towns;";
         String DROP2 = "DROP TABLE IF EXISTS "+prefix+"citizens;";
         String DROP3 = "DROP TABLE IF EXISTS "+prefix+"transactions;";
         String DROP4 = "DROP TABLE IF EXISTS "+prefix+"votes;";
         String DROP4a = "DROP TABLE IF EXISTS "+prefix+"ballots;";
         String DROP5 = "DROP TABLE IF EXISTS "+prefix+"contractors;";
         String DROP6 = "DROP TABLE IF EXISTS "+prefix+"subregions;";
         String SQL0 = "CREATE DATABASE IF NOT EXISTS minecraft;";
         //String SQL00= "GRANT ALL ON "+plugin.getDB_dbName()+".* TO '"+
         //        plugin.getDB_user()+"'@'"+plugin.getDB_host()+"'" ;//+"' IDENTIFY BY '"+plugin.getDB_pass()+"';";
         String SQL1 = "CREATE TABLE IF NOT EXISTS "+prefix+"towns ( " + 
             "id INTEGER "+ spk + serial +", " + 
             "townName VARCHAR(30) UNIQUE NOT NULL, mayor VARCHAR(16), townRank INTEGER, democracy BOOLEAN, " + 
             "bankBal DOUBLE, taxRate DOUBLE, itemBal INTEGER, itemTaxRate INTEGER, "+
             "world VARCHAR(30), expansions INTEGER, tcX INTEGER, tcY INTEGER, tcZ INTEGER "+ mpk + ");";
         String SQL2 = "CREATE TABLE IF NOT EXISTS "+prefix+"citizens ( " + 
             "id INTEGER "+ spk + serial +", " + 
             "playerName VARCHAR(16) UNIQUE NOT NULL, " +"townName VARCHAR(25), " +
             "role VARCHAR(10), invitedBy VARCHAR(16), "+
             "sentDate DATETIME, lastLogin DATETIME, homeX INTEGER, homeY INTEGER, "+
             "homeZ INTEGER "+ mpk +");";
         String SQL3 = "CREATE TABLE IF NOT EXISTS "+prefix+"transactions ( "  + 
             "id INTEGER "+ spk + serial +", " + 
             "playerName VARCHAR(16) NOT NULL, " +
             "townName VARCHAR(30), timestamp DATETIME,  " +
             "type VARCHAR(30), amount DOUBLE, item_amount INTEGER, " +
             "notes VARCHAR(350) "+ mpk +");";
         String SQL4 = "CREATE TABLE IF NOT EXISTS "+prefix+"votes ( "  + 
             "id INTEGER "+ spk + serial +", " + 
             "playerName VARCHAR(16) NOT NULL, townName VARCHAR(30) NOT NULL, timestamp DATETIME,  " +
             "ballotID INTEGER NOT NULL, answer BOOLEAN NOT NULL "+ mpk +");";
         String SQL4a = "CREATE TABLE IF NOT EXISTS "+prefix+"ballots ( "  + 
             "id INTEGER "+ spk + serial +", " + "townName VARCHAR(30) NOT NULL, endTime DATETIME,  " +
             "action VARCHAR(30), description VARCHAR(80), status BOOLEAN NOT NULL, result BOOLEAN "+ mpk +");";
         String SQL5 = "CREATE TABLE IF NOT EXISTS "+prefix+"contractors ( "  + 
             "id INTEGER "+ spk + serial +", " + 
                 "playerName VARCHAR(16) NOT NULL, townName VARCHAR(30) NOT NULL, endTime DATETIME " 
                 +mpk +");";
         String SQL6 = "CREATE TABLE IF NOT EXISTS "+prefix+"subregions ( "  + 
             "id INTEGER "+ spk + serial +", " + 
                 "town VARCHAR(30) NOT NULL , regionName VARCHAR(30) NOT NULL UNIQUE, "+
                 "world VARCHAR(30) NOT NULL, type VARCHAR(15) " 
                 +mpk +");";
         try {
             db_open();
             if (drops){ 
                 plugin.getLogger().warning("Dropping all tables");
                 stmt.executeUpdate(DROP1);
                 stmt.executeUpdate(DROP2);
                 stmt.executeUpdate(DROP3);
                 stmt.executeUpdate(DROP4);
                 stmt.executeUpdate(DROP4a);
                 stmt.executeUpdate(DROP5);
                 stmt.executeUpdate(DROP6);
             } // could do an else: check existence here
             if (plugin.useMysql() ){ 
                 stmt.executeUpdate(SQL0); 
                 //plugin.getLogger().warning(SQL00);
                 //stmt.executeUpdate(SQL00); 
                 if(plugin.isSQLdebug()){plugin.getLogger().info("Made the DB (mysql)");}
             }
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making towns table if doesn't exist. ");}
             stmt.executeUpdate(SQL1);
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making citizens table if doesn't exist. ");}
             stmt.executeUpdate(SQL2);
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making transactions table if doesn't exist. ");}
             stmt.executeUpdate(SQL3);
             rtn = true;
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making voting table if doesn't exist. ");}
             stmt.executeUpdate(SQL4);
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making ballots table if doesn't exist. ");}
             stmt.executeUpdate(SQL4a);
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making contractor table if doesn't exist. ");}
             stmt.executeUpdate(SQL5);
             if(plugin.isSQLdebug()){plugin.getLogger().info("Making sub regions table if doesn't exist. ");}
             stmt.executeUpdate(SQL6);
             rtn = true;
             
         } catch (SQLException ex){
             plugin.getLogger().severe( "createDB: "+ex.getMessage() );
             rtn = false;
         } finally {
             try { db_close();
             } catch (SQLException ex) {
                 plugin.getLogger().warning( "createDB: "+ex.getMessage() );
                 rtn = false;
             } finally{}
         }
         return rtn;
     }
 }
