 package com.sunnyd.database.fixtures;
 
 import com.google.common.base.Throwables;
 import com.sunnyd.database.Connector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sun.print.PSStreamPrinterFactory;
 
 import java.sql.*;
 
 public class Prep
 {
   final static Logger logger = LoggerFactory.getLogger(Prep.class);
 
   private static Connection conn;
   private static Statement stmt;
 
   static
   {
     try
     {
       conn = Connector.getConnection();
       stmt = conn.createStatement();
     }
     catch (SQLException e)
     {
       logger.error("Error while initiate static JDBC connector.");
       Throwables.propagate(e);
     }
   }
 
   public static void init(String tableName) throws SQLException
   {
     // Check if table exists
     DatabaseMetaData meta = conn.getMetaData();
     ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
     if (!rs.first())
     {
 
       String msg = String.format("Could not find table %s", tableName);
       logger.error(msg);
       logger.error("You probably need to create the table %s", tableName);
       throw new SQLException(msg);
     }
     else
     {
       logger.info(String.format(" Found table %s", tableName));
     }
   }
 
   public static void resetPrimaryKey(String tableName)
       throws SQLException
   {
     logger.info(String.format(" Resetting primary key id for table %s", tableName));
     stmt.executeUpdate(String.format("ALTER TABLE `%s` AUTO_INCREMENT = 1", tableName));
 
   }
 
  public static void purgeAllRecord(String tableName)
       throws SQLException
   {
     logger.info(String.format("Removing all record from %s", tableName));
     stmt.executeUpdate(String.format("DELETE FROM `%s`", tableName));
   }
 
   public static void insertTestRecord(int amount, String tableName, boolean withPurge, boolean withResetIncrement)
       throws SQLException
   {
     if (withPurge) Prep.purgeAllRecord(tableName);
     if (withResetIncrement) Prep.resetPrimaryKey(tableName);
 
     logger.info("Inserting %i elements into table %s", amount, tableName);
     for (int i = 0; i < amount; i++)
     {
       stmt.executeUpdate(
           String.format("INSERT INTO `%s` (`name`) VALUES ('foobar')", tableName));
     }
   }
 }
