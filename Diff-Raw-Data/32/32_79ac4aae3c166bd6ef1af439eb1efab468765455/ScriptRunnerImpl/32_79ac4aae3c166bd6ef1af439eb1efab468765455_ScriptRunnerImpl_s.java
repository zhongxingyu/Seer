 package com.carbonfive.db.jdbc;
 
 import static org.apache.commons.lang.StringUtils.*;
 import org.slf4j.*;
 
 import java.io.*;
 import java.sql.*;
 
 /** Tool to run database scripts, largely copied from iBatis 2.3.0. */
 class ScriptRunnerImpl
 {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private static final String DEFAULT_DELIMITER = ";";
 
     public void execute(Connection connection, Reader reader) throws IOException, SQLException
     {
         try
         {
             final boolean originalAutoCommit = connection.getAutoCommit();
             try
             {
                 if (originalAutoCommit)
                 {
                     connection.setAutoCommit(false);
                 }
                 doExecute(connection, reader);
             }
             finally
             {
                 connection.setAutoCommit(originalAutoCommit);
             }
         }
         catch (IOException e)
         {
             throw e;
         }
         catch (SQLException e)
         {
             throw e;
         }
         catch (Exception e)
         {
             throw new RuntimeException("Error running script.  Cause: " + e, e);
         }
     }
 
     private void doExecute(Connection conn, Reader reader) throws IOException, SQLException
     {
         StringBuffer command = null;
         try
         {
             String delimiter = DEFAULT_DELIMITER;
             LineNumberReader lineReader = new LineNumberReader(reader);
             String line;
             while ((line = lineReader.readLine()) != null)
             {
                 if (command == null)
                 {
                     command = new StringBuffer();
                 }
                 String trimmedLine = line.trim(); // Strip extra whitespace too?
                 if (trimmedLine.startsWith("--") || trimmedLine.startsWith("#"))
                 {
                     log.debug(trimmedLine);
                 }
                 else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//") || trimmedLine.startsWith("--") || trimmedLine.startsWith("#"))
                 {
                     //Do nothing
                 }
                 else if (startsWithIgnoreCase(trimmedLine, "DELIMITER"))
                 {
                     delimiter = trimmedLine.substring(10).trim();
                 }
                 else
                 {
                     if (trimmedLine.endsWith(delimiter))
                     {
                         command.append(line.substring(0, line.lastIndexOf(delimiter)));
 
                        // Reset the delimiter back to the default.
                        delimiter = DEFAULT_DELIMITER;

                         Statement statement = conn.createStatement();
 
                         log.debug(command.toString());
 
                         boolean hasResults = statement.execute(command.toString());
 
                         ResultSet rs = statement.getResultSet();
 
                         if (hasResults && rs != null)
                         {
                             ResultSetMetaData md = rs.getMetaData();
                             int cols = md.getColumnCount();
                             for (int i = 1; i <= cols; i++)
                             {
                                 String name = md.getColumnName(i);
                                 log.debug(name + "\t");
                             }
                             while (rs.next())
                             {
                                 for (int i = 1; i <= cols; i++)
                                 {
                                     String value = rs.getString(i);
                                     log.debug(value + "\t");
                                 }
                             }
                         }
 
                         command = null;
                         try
                         {
                             statement.close();
                         }
                         catch (Exception e)
                         {
                             // Ignore to workaround a bug in Jakarta DBCP
                         }
                         Thread.yield();
                     }
                     else
                     {
                         command.append(line);
                         command.append(" ");
                     }
                 }
             }
         }
         catch (SQLException e)
         {
             e.fillInStackTrace();
             log.error("Error executing: " + command, e);
             throw e;
         }
         catch (IOException e)
         {
             e.fillInStackTrace();
             log.error("Error executing: " + command, e);
             throw e;
         }
     }
 }
