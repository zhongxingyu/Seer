 /**
  * Copyright (c) 2007-2012 Wave2 Limited. All rights reserved.
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
 
 package org.dbinterrogator.nagios;
 
 import oracle.jdbc.pool.OracleDataSource;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import java.io.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Author: Alan Snelson
  */
 public class CheckDB {
 
     @Option(name = "--help")
     private boolean help;
     @Option(name = "-h", usage = "Hostname")
     private String hostname;
     @Option(name = "-d", usage = "DBMS")
     private String dbms;
     @Option(name = "-s", usage = "SID / Instance")
     private String sid;
     @Option(name = "-u", usage = "username")
     private String username;
     @Option(name = "-P", usage = "Password")
     private String password;
     @Option(name = "-f", usage = "Query file to execute")
     private String queryFile;
     @Option(name = "-q", usage = "Query string to execute")
     private String queryString;
     @Option(name = "-c", usage = "Critical Threshold")
     private Integer criticalThreshold;
     @Option(name = "-w", usage = "Warning Threshold")
     private Integer warningThreshold;
     @Option(name = "-t", usage = "Timeout")
     private String timeout;
     @Option(name = "-V")
     public static boolean verbose;
     // receives other command line parameters than options
     @Argument
     private List<String> arguments = new ArrayList<String>();
     private Properties properties;
 
     private final int NAGIOS_OK = 0;
     private final int NAGIOS_WARNING = 1;
     private final int  NAGIOS_CRITICAL = 2;
     private final int  NAGIOS_UNKNOWN = 3;
 
     public CheckDB(){
         //Load properties
         properties = new Properties();
         try {
             properties.load(getClass().getResourceAsStream("/application.properties"));
         }
         catch (IOException e) {
             System.err.println(e.getMessage());
         }
     }
 
     public static void main(String[] args) {
         new CheckDB().doMain(args);
     }
 
     void doMain(String[] args) {
         Integer queryResult = null;
         String queryMessage = "";
         String usage = "Usage: java -jar CheckDB.jar [-v]\nOptions:\n    -h  hostname\n    -p  port\n    -s  SID / Instance\n    -u  username\n    -P  password\n    -f  Path to query file\n    -v  Generate verbose output on standard output\n    -c  Critical threshold\n    -w  Warning threshold";
         CmdLineParser parser = new CmdLineParser(this);
 
         // if you have a wider console, you could increase the value;
         // here 80 is also the default
         parser.setUsageWidth(80);
 
         try {
             // parse the arguments.
             parser.parseArgument(args);
 
             if (help) {
                 throw new CmdLineException(parser,"Print Help");
             }
 
             // after parsing arguments, you should check
             // if enough arguments are given.
             //if( arguments.isEmpty() ){
             //throw new CmdLineException("No argument is given");
             //}
 
             if (dbms == null) {
                 throw new CmdLineException(parser,"Database type missing\n");
             }
             if (hostname == null) {
                 throw new CmdLineException(parser,"Hostname missing\n");
             }
             if ("oracle".equals(dbms.toLowerCase()) && sid == null) {
                 throw new CmdLineException(parser,"SID missing\n");
             }
             if (username == null) {
                 throw new CmdLineException(parser,"Username missing\n");
             }
             if (password == null) {
                 throw new CmdLineException(parser,"Password missing\n");
             }
             if (queryString == null) {
                 if (queryFile == null) {
                     throw new CmdLineException(parser,"Query file missing\n");
                 }
             }
             if (criticalThreshold == null) {
                     throw new CmdLineException(parser,"Critical threshold missing\n");
             }
             if (warningThreshold == null) {
                 throw new CmdLineException(parser,"Warning threshold missing\n");
             }
 
 
         } catch (CmdLineException e) {
             if (e.getMessage().equalsIgnoreCase("Print Help")) {
                 System.err.println("CheckDB.java Ver " + getVersion() + "\nThis software comes with ABSOLUTELY NO WARRANTY. This is free software,\nand you are welcome to modify and redistribute it under the BSD license" + "\n\n" + usage);
 
             }
             // if there's a problem in the command line,
             // you'll get this exception. this will report
             // an error message.
             System.err.println(e.getMessage());
             // print usage.
             System.err.println(usage);
             System.exit(NAGIOS_CRITICAL);
         }
 
 
         if ("mssql".equals(dbms.toLowerCase())) {
 
             try {
 
                 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                 String URL = "jdbc:sqlserver://" + hostname + ";database=" + sid + ";user=" + username + ";password=" + password;
                 Connection conn = DriverManager.getConnection(URL);
                 conn.setAutoCommit(false);
                 conn.setReadOnly(true);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(convertStreamToString(new FileInputStream(queryFile)));
                 rs.next();
                 queryResult = rs.getInt(1);
                 queryMessage = rs.getString(2);
                 rs.close();
                 stmt.close();
                 conn.close();
             } catch (SQLException e) {
                 System.out.println(e.getMessage());
             } catch (java.lang.Exception jle) {
                 System.out.println(jle.getMessage());
             }
         } else {
 
             try {
                 OracleDataSource ods = new OracleDataSource();
                 int port = 1521;
                 String URL = "jdbc:oracle:thin:" + username + "/" + password + "@" + hostname + ":" + port + ":" + sid;
                 ods.setURL(URL);
                 Connection conn = ods.getConnection();
                 conn.setAutoCommit(false);
                 conn.setReadOnly(true);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(parseQuery());
                 rs.next();
                 queryResult = rs.getInt(1);
                 queryMessage = rs.getString(2);
                 rs.close();
                 stmt.close();
                 conn.close();
             } catch (SQLException e) {
                 System.out.println(e.getMessage());
             } catch (java.lang.Exception jle) {
                 System.out.println(jle.getMessage());
             }
         }
         if (queryResult != null){
             System.out.print(queryMessage);
             System.exit(checkThresholds(queryResult));
         } else {
                 System.exit(NAGIOS_UNKNOWN);
             }
 
     }
 
     private int checkThresholds(int queryResult){
         if (queryResult > criticalThreshold){
             return NAGIOS_CRITICAL;
         }
         if (queryResult > warningThreshold){
             return NAGIOS_WARNING;
         }
         return NAGIOS_OK;
     }
 
 
     private static String convertStreamToString(InputStream is) throws Exception {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
             sb.append(line).append("\n");
         }
         is.close();
         return sb.toString();
     }
 
     String getVersion() {
         return properties.getProperty("application.version");
     }
 
     private String parseQuery() {
         String parsedQuery;
         parsedQuery = "SELECT 0";
         if (queryFile != null) {
             try {
                return convertStreamToString(new FileInputStream(queryFile));
             } catch (java.lang.Exception jle) {
                 System.out.println(jle.getMessage());
             }
         } else {
             parsedQuery = queryString;
         }
         return parsedQuery;
     }
 }
