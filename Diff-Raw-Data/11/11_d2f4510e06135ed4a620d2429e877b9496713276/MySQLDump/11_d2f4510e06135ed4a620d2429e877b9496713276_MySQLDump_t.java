 /**
  * Copyright (c) 2007-2011 Wave2 Limited. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of Wave2 Limited nor the names of its contributors may be
  * used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.binarystor.mysql;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.Map;
 import org.kohsuke.args4j.*;
 
 /**
 *
 * @author Alan Snelson
 */
 public class MySQLDump {
     
     //Command Line Arguments
     @Option(name="--help")
     private boolean help;
     @Option(name="-h",usage="MySQL Server Hostname")
     private String hostname;
     @Option(name="-u",usage="MySQL Username")
     private String username;
     @Option(name="-p",usage="MySQL Password")
     private String password;
     @Option(name="-v")
     private boolean verbose;
     // receives other command line parameters than options
     @Argument
     private List<String> arguments = new ArrayList<String>();
     
     private static String version = "0.3";
     private String schema = null;
     private Connection conn = null;
     private DatabaseMetaData databaseMetaData;
     private String databaseProductVersion = null;
     private int databaseProductMajorVersion = 0;
     private int databaseProductMinorVersion = 0;
     private String mysqlVersion = null;
     
     /**
     * Default contructor for MySQLDump.
     */
     public MySQLDump() {
         
     }
     
     /**
     * Create a new instance of MySQLDump using default database.
     *
     * @param  host      MySQL Server Hostname
     * @param  username  MySQL Username
     * @param  password  MySQL Password
     */
     public MySQLDump(String host, String username, String password) throws SQLException {
         try{
             connect(host, username, password, "mysql");
         }
         catch (SQLException se){
             throw se;
         }
         
     }
     
     /**
     * Create a new instance of MySQLDump using supplied database.
     *
     * @param  host      MySQL Server Hostname
     * @param  username  MySQL Username
     * @param  password  MySQL Password
     * @param  db        Default database
     */
     public MySQLDump(String host, String username, String password, String db) throws SQLException{
         try{
             connect(host, username, password, db);
         }
         catch (SQLException se){
             throw se;
         }
     }
     
     /**
     * Connect to MySQL server
     *
     * @param  host      MySQL Server Hostname
     * @param  username  MySQL Username
     * @param  password  MySQL Password
     * @param  db        Default database
     */
     public void connect(String host, int port, String username, String password, String db) throws SQLException{
         try
         { 
             Class.forName ("com.mysql.jdbc.Driver").newInstance ();
             conn = DriverManager.getConnection ("jdbc:mysql://" + host + ":" + port + "/" + db, username, password);
             databaseMetaData = conn.getMetaData();
             databaseProductVersion = databaseMetaData.getDatabaseProductVersion();
             databaseProductMajorVersion = databaseMetaData.getDatabaseMajorVersion();
             databaseProductMinorVersion = databaseMetaData.getDatabaseMinorVersion();
             hostname = host;
             schema = db;
             if (verbose){
                 System.out.println ("Database connection established");
             }
         }
         catch (SQLException se){
             throw se;
         }
         catch (Exception e)
         {
             System.err.println ("Cannot connect to database server");
         }
     }
     
     /**
     * Connect to MySQL server
     *
     * @param  host      MySQL Server Hostname
     * @param  username  MySQL Username
     * @param  password  MySQL Password
     * @param  db        Default database
     */
     public void connect(String host, String username, String password, String db) throws SQLException{
         connect(host,3306,username,password,db);
     }
     
     public File dumpAllDatabases(){
         return null;
     }
     
     public String dumpCreateDatabase(String database) {
         String createDatabase = null;
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW CREATE DATABASE `" + database + "`");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 createDatabase = rs.getString("Create Database") + ";";
             }
         } catch (SQLException e) {
             
         }
         return createDatabase;
     } 
     
     public File dumpDatabase(String database){   
         return null;
     }
     
     public File dumpAllTables(String database){
         return null;
     }
     
     public String dumpCreateEvent(String schema, String event) {
         String createEvent = "--\n-- Event structure for event `" + event + "`\n--\n\n";
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW CREATE EVENT " + schema + "." + event);
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 createEvent += rs.getString("Create Event") + ";";
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         return createEvent;
     }
     
     public String dumpCreateTable(String table) {
        return dumpCreateTable(schema,table);
     }
     
     public String dumpCreateTable(String schema, String table) {
         String createTable = "--\n-- Table structure for table `" + table + "`\n--\n\n";
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW CREATE TABLE `" + schema + "`.`" + table + "`");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 createTable += rs.getString("Create Table") + ";";
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
             createTable = "";
         }
         return createTable;
     }
 
     public String dumpCreateView(String view) {
        return dumpCreateView(schema,view);
     }
 
     public String dumpCreateView(String schema, String view) {
         String createView = "--\n-- View definition for view `" + view + "`\n--\n\n";
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW CREATE VIEW `" + schema + "`.`" + view + "`");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 createView += rs.getString("Create View") + ";";
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
             createView = "";
         }
         return createView;
     }
     
     public String dumpCreateEvent(String event) {
        return dumpCreateEvent(schema,event);
     }
     
     public String dumpCreateRoutine(String schema, String routine) {
         String createRoutine = "--\n-- Routine structure for routine `" + routine + "`\n--\n\n";
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SELECT ROUTINE_DEFINITION FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_NAME='" + routine + "'");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 createRoutine += rs.getString("ROUTINE_DEFINITION") + ";";
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
             createRoutine = "";
         }
         return createRoutine;
     }
     
     public String dumpCreateRoutine(String routine) {
        return dumpCreateRoutine(schema,routine);
     }
     
     public String dumpCreateTrigger(String schema, String trigger) {
         String createTrigger = "--\n-- Trigger structure for trigger `" + trigger + "`\n--\n\n";
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW CREATE TRIGGER " + schema + "." + trigger);
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 createTrigger += rs.getString("SQL Original Statement") + ";";
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
             createTrigger = "";
         }
         return createTrigger;
     }
     
     public String dumpCreateTrigger(String trigger) {
        return dumpCreateTrigger(schema,trigger);
     }
     

    /**
    * Create script with insert statements: NOTE THIS IS INCOMPLETE AND ANY CONTRIBUTIONS WOULD BE WELCOME
    *
    * @param  out    BufferedWriter
    * @param  table  Table Name
    */
     public void dumpTable(BufferedWriter out, String table){
          try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SELECT /*!40001 SQL_NO_CACHE */ * FROM `" + table + "`");
             ResultSet rs = s.getResultSet ();
             ResultSetMetaData rsMetaData = rs.getMetaData();
             if (rs.last()){
                 out.write("--\n-- Dumping data for table `" + table + "`\n--\n\n");
                rs.beforeFirst();
             }
             int columnCount = rsMetaData.getColumnCount();
             String prefix = new String("INSERT INTO `" + table + "` (");
             for (int i = 1; i <= columnCount; i++) {
                 if (i == columnCount){
                     prefix += rsMetaData.getColumnName(i) + ") VALUES(";
                 }else{
                     prefix += rsMetaData.getColumnName(i) + ",";       
                 }
             }
             String postfix = new String();
             int count = 0;
             while (rs.next ())
             {
                 postfix = "";
                 for (int i = 1; i <= columnCount; i++) {
                     if (i == columnCount){
                         //System.err.println(rs.getMetaData().getColumnClassName(i));
                         postfix += "'" + rs.getString(i) + "');\n";
                     }else{                   
                         //System.err.println(rs.getMetaData().getColumnTypeName(i));
                         if (rs.getMetaData().getColumnTypeName(i).equalsIgnoreCase("LONGBLOB")){
                             try{   
                                 postfix += "'" + escapeString(rs.getBytes(i)).toString() + "',"; 
                             }catch (Exception e){
                                 postfix += "NULL,"; 
                             }
                         }else{
                             try{
                                 postfix += "'" + escapeString(rs.getBytes(i)).toString() + "',"; 
                             }catch (Exception e){
                                 postfix += "NULL,";
                             }       
                     }   }
                 }
                 out.write(prefix + postfix);
                 ++count;
             }
             rs.close ();
             s.close();
         }catch(IOException e){
             System.err.println (e.getMessage());
         }catch(SQLException e){
             System.err.println (e.getMessage());            
         }
     }
     
     public Map<String, String> dumpGlobalVariables() {
         Map<String, String> variables = new TreeMap(); 
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW GLOBAL VARIABLES");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 variables.put(rs.getString(1),rs.getString(2));
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         return variables;
     }
     
     public File dumpAllViews(String database) {
         return null;
     }
     
     public File dumpView(String view) {
         return null;
     }
     
     public ArrayList<String> listSchemata() {
         ArrayList<String> schemata = new ArrayList();
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SHOW DATABASES");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 //Skip Information Schema
                 if (!rs.getString("Database").equals("information_schema")) {
                     schemata.add(rs.getString("Database"));
                 }
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         
         return schemata;
     }
     
     public ArrayList<String> listRoutines(String schema) {
         ArrayList<String> routines = new ArrayList();
         //Triggers were included beginning with MySQL 5.0.2
         if (databaseProductMajorVersion < 5){
             return routines;
         }
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA='" + schema + "'");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 routines.add(rs.getString(1));
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         
         return routines;
     }
     
     public ArrayList<String> listTriggers(String schema) {
         ArrayList<String> triggers = new ArrayList();
         //Triggers were included beginning with MySQL 5.0.2
         if (databaseProductMajorVersion < 5){
             return triggers;
         }
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             s.executeQuery ("SELECT TRIGGER_NAME FROM INFORMATION_SCHEMA.TRIGGERS WHERE TRIGGER_SCHEMA='" + schema + "'");
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 triggers.add(rs.getString(1));
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         
         return triggers;
     }
     
     public ArrayList<String> listGrantTables() {
         ArrayList<String> grantTables = new ArrayList();
         grantTables.add("user");
         grantTables.add("db");
         grantTables.add("tables_priv");
         grantTables.add("columns_priv");
         //The procs_priv table exists as of MySQL 5.0.3.
         if (databaseProductMajorVersion > 4){
             grantTables.add("procs_priv");
         }     
         return grantTables;
     }
     
     public ArrayList<String> listTables(String schema) {
         ArrayList<String> tables = new ArrayList();
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             //Version 5 upwards use information_schema
             if (databaseProductMajorVersion < 5){
                 s.executeQuery ("SHOW TABLES FROM `" + schema + "`");
             }else{
                 s.executeQuery ("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = '" + schema + "'");
             }
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 tables.add(rs.getString(1));
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         
         return tables;
     }
 
     /**
     * List views
     *
     * @param  schema        Schema to list views for
     */
     public ArrayList<String> listViews(String schema) {
         ArrayList<String> views = new ArrayList();
         try{
             Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             //Version 5 upwards use information_schema
             if (databaseProductMajorVersion < 5){
                 //Views were introduced in version 5.0.1
                 return views;
             }else{
                 s.executeQuery ("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'VIEW' AND TABLE_SCHEMA = '" + schema + "'");
             }
             ResultSet rs = s.getResultSet ();
             while (rs.next ())
             {
                 views.add(rs.getString(1));
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
 
         return views;
     }
     
     public ArrayList<String> listEvents(String schema) {
         ArrayList<String> events = new ArrayList();
         try{
             //Version 5 upwards use information_schema
             if (databaseProductMajorVersion == 5 && databaseProductMinorVersion == 1 ){
                 Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                 s.executeQuery ("SELECT EVENT_NAME FROM INFORMATION_SCHEMA.EVENTS WHERE EVENT_SCHEMA='" + schema + "'");
                 ResultSet rs = s.getResultSet ();
                 while (rs.next ())
                 {
                     events.add(rs.getString(1));
                 }
             }
         } catch (SQLException e) {
             System.err.println (e.getMessage());
         }
         
         return events;
     }
     
     public String getSchema(){
         return schema;
     }
     
     public void setSchema(String schema){
         this.schema = schema;
     }
     
     
 
     /**
     * Escape string ready for insert via mysql client
     *
     * @param  bIn       String to be escaped passed in as byte array
     * @return bOut      MySQL compatible insert ready ByteArrayOutputStream
     */
     private ByteArrayOutputStream escapeString(byte[] bIn){
         int numBytes = bIn.length;
         ByteArrayOutputStream bOut = new ByteArrayOutputStream(numBytes+ 2);
         for (int i = 0; i < numBytes; ++i) {
             byte b = bIn[i];
 
             switch (b) {
             case 0: /* Must be escaped for 'mysql' */
                     bOut.write('\\');
                     bOut.write('0');
                     break;
 
             case '\n': /* Must be escaped for logs */
                     bOut.write('\\');
                     bOut.write('n');
                     break;
 
             case '\r':
                     bOut.write('\\');
                     bOut.write('r');
                     break;
 
             case '\\':
                     bOut.write('\\');
                     bOut.write('\\');
 
                     break;
 
             case '\'':
                     bOut.write('\\');
                     bOut.write('\'');
 
                     break;
 
             case '"': /* Better safe than sorry */
                     bOut.write('\\');
                     bOut.write('"');
                     break;
 
             case '\032': /* This gives problems on Win32 */
                     bOut.write('\\');
                     bOut.write('Z');
                     break;
 
             default:
                     bOut.write(b);
             }
         }
         return bOut;
     }
     
     private String getHeader(){
         //return Dump Header        
         return "-- BinaryStor MySQL Dump " + version + "\n--\n-- Host: " + hostname + "    " + "Database: " + schema + "\n-- ------------------------------------------------------\n-- Server Version: " + databaseProductVersion + "\n--";
     }
     
     /**
     * Main entry point for MySQLDump when run from command line
     *
     * @param  args  Command line arguments
     */
     public static void main (String[] args) throws IOException {
         new MySQLDump().doMain(args);
     }
     
     /**
     * Parse command line arguments and run MySQLDump
     *
     * @param  args  Command line arguments
     */
     public void doMain(String[] args) throws IOException {
         
         String usage = "Usage: java -jar MySQLDump.jar [OPTIONS] database [tables]\nOR     java -jar MySQLDump.jar [OPTIONS] --databases [OPTIONS] DB1 [DB2 DB3...]\nOR     java -jar MySQLDump.jar [OPTIONS] --all-databases [OPTIONS]\nFor more options, use java -jar MySQLDump.jar --help";        
         CmdLineParser parser = new CmdLineParser(this);
         
         // if you have a wider console, you could increase the value;
         // here 80 is also the default
         parser.setUsageWidth(80);
 
         try {
             // parse the arguments.
             parser.parseArgument(args);
             
             if (help) {
                 throw new CmdLineException("Print Help");
             }
 
             // after parsing arguments, you should check
             // if enough arguments are given.
             if( arguments.isEmpty() )
                 throw new CmdLineException("No argument is given");
 
         } catch( CmdLineException e ) {
             if (e.getMessage().equalsIgnoreCase("Print Help")){
                 System.err.println("MySQLDump.java Ver " + version + "\nThis software comes with ABSOLUTELY NO WARRANTY. This is free software,\nand you are welcome to modify and redistribute it under the BSD license" + "\n\n" + usage);
                 return;
             }
             // if there's a problem in the command line,
             // you'll get this exception. this will report
             // an error message.
             System.err.println(e.getMessage());
             // print usage.
             System.err.println(usage);
             return;
         }
         
 
         //Do we have a hostname? if not use localhost as default
         if (hostname == null){
             hostname = "localhost";
         }
         //First argument here should be database
         schema = arguments.remove(0);
         
         try{
             //Create temporary file to hold SQL output.
             File temp = File.createTempFile(schema, ".sql");
             BufferedWriter out = new BufferedWriter(new FileWriter(temp));
             this.connect(hostname, username, password, schema);
             out.write(getHeader());
             for( String arg : arguments ){
                 System.out.println(arg);
                 out.write(dumpCreateTable(arg));
                 this.dumpTable(out,arg);
             }
             out.flush();
             out.close();
             this.cleanup();
         }
         catch (SQLException se){
             System.err.println (se.getMessage());
         }
     }
     
     public int cleanup(){
         try
         {
             conn.close ();
             if (verbose){
                 System.out.println ("Database connection terminated");
             }
         }
         catch (Exception e) { /* ignore close errors */ }
         return 1;
     }
             
     
 }
