 package org.sidl.herenow.db;
 
 import org.sidl.herenow.Venue;
 
 import java.sql.*;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Writes the Foursquare data
  */
 public class DBStatementWriter
 {
   public DBStatementWriter(DBConnection connection, String tableName)
   {
     this.connection = connection;
     this.table = tableName;
   }
 
   public void logMetaData() throws SQLException
   {
     Statement stmt = connection.pipe().createStatement();
     ResultSet rs = stmt.executeQuery("select * from " + table + " limit 0, 1;");
     ResultSetMetaData md = rs.getMetaData();
     StringBuilder builder = new StringBuilder("Column Types: ");
 
     for (int count = 1; count <= md.getColumnCount(); count++)
     {
       builder.append(md.getColumnTypeName(count)).append(", ");
     }
 
     LOG.info(builder.toString());
   }
 
   public void resetTable() throws SQLException
   {
     Statement stmt = connection.pipe().createStatement();
     stmt.executeUpdate("drop table if exists " + table + ";");
     stmt.executeUpdate(
         "create table " + table + " ("
         + DB_ID_ATTR           + " varchar(30) not null primary key, "
         + DB_NAME_ATTR         + " text not null, "
         + DB_X_ATTR            + " decimal(12,5) not null, "
         + DB_Y_ATTR            + " decimal(12,5) not null, "
         + DB_CATEGORIES_1_ATTR + " text,"
         + DB_CATEGORIES_2_ATTR + " text,"
        + DB_HERE_NOW_ATTR     + " text not null,"
         + DB_TIMESTAMP_ATTR    + " timestamp);");
   }
 
   public int[] write(List<Venue> venues, long timestamp) throws SQLException
   {
     int [] update;
     String hereNow;
     PreparedStatement prep;
     Timestamp t = new Timestamp(timestamp);
 
     connection.pipe().setAutoCommit(false);
 
     prep = connection.pipe().prepareStatement(
         "replace into " + table + " values (?,?,?,?,?,?,?,?);");
 
     for (Venue v : venues)
     {
       // written out manually to make the schema obvious
       int i = 1;
       hereNow = Integer.toString(v.getHereNowCount());
 
       prep.setString(i++, v.getId());           // vid
       prep.setString(i++, v.getName());         // name
       prep.setString(i++, format(v.getX()));    // x
       prep.setString(i++, format(v.getY()));    // y
       prep.setString(i++, v.getCategory1());    // cat1
       prep.setString(i++, v.getCategory2());    // cat2
       prep.setString(i++, hereNow);             // herenow
       prep.setTimestamp(i, t);                  // epoch timestamp
 
       // get statement ready for another venue
       prep.addBatch();
     }
 
     update = prep.executeBatch();
     connection.pipe().setAutoCommit(true);
 
     if (LOG.isLoggable(Level.FINE))
       logDBStats(connection, timestamp);
 
     return update;
   }
 
   private static String format(float f)
   {
     return String.format("%12.5f", f);
   }
 
   private void logDBStats(DBConnection connection, long timestamp) throws SQLException
   {
     Statement stmt;
     ResultSet rs;
 
     stmt = connection.pipe().createStatement();
     rs = stmt.executeQuery("select * from " + table +
         " where "+DB_TIMESTAMP_ATTR+"=" + timestamp + ";");
 
     while (rs.next())
     {
       LOG.fine(String.format("%s %s %s %s", rs.getString(DB_ID_ATTR), rs.getString(DB_NAME_ATTR),
           rs.getString(DB_TIMESTAMP_ATTR), rs.getString(DB_HERE_NOW_ATTR)));
     }
 
     rs = stmt.executeQuery("select count(" + DB_ID_ATTR + ") from " + table);
     while (rs.next())
     {
       LOG.fine("Rows in table: " + rs.getString("count(" + DB_ID_ATTR + ")"));
     }
 
   }
   
   
   private final DBConnection connection;
   private final String table;
   
   //
   private static final String DB_ID_ATTR = "vid";
   private static final String DB_NAME_ATTR = "name";
   private static final String DB_X_ATTR = "x";
   private static final String DB_Y_ATTR = "y";
   private static final String DB_HERE_NOW_ATTR = "herenow";
   private static final String DB_CATEGORIES_1_ATTR = "cat1";
   private static final String DB_CATEGORIES_2_ATTR = "cat2";
   private static final String DB_TIMESTAMP_ATTR = "timestamp";
 
   private static final Logger LOG = Logger.getLogger(DBStatementWriter.class.getName());
 }
