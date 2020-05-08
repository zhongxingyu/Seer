 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sig13.sensor.sc4.method;
 
 import org.apache.log4j.*;
 import org.json.*;
 
 import java.io.*;
 import java.sql.*;
 import javax.naming.*;
 import javax.servlet.*;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.*;
 
 /**
  *
  * @author pee
  */
 public class Report extends Method {
 
     private final static Logger logger = Logger.getLogger(Report.class);
 
     public void handle(HttpServletResponse response, String sID, JSONObject job) throws ServletException, IOException {
 
         logger.debug("Report received from :" + sID + ":");
 
         JSONArray readings;
         try {
 
             if (job.has("readings") == false) {
                 String em = "readings not found in report";
                 logger.error(em);
                 response.sendError(700, em);
                 return;
             }
 
             readings = job.getJSONArray("readings");
             logger.debug("Readings length: " + readings.length());
 
             if (readings.length() < 1) {
                 String em = "Report length of " + readings.length() + " is too short";
                 response.sendError(500, em);
                 return;
             }
 
             JSONObject reading;
             for (int i = 0; i < readings.length(); i++) {
                 reading = readings.getJSONObject(i);
                 if (readings == null) {
                     response.sendError(500, "Reading " + i + " was a null object get buggered");
                 }
                 // {"type":"inspeedD2","units":"meters/second","value":"0.8939"}]
 
                 String type = reading.getString("type");
                 logger.debug("Type: " + type);
                 String units = reading.getString("units");
                 logger.debug("Units: " + units);
                 String value = reading.getString("value");
                 logger.debug("Value: " + value);
 
                 storeReading(response, sID, type, units, value);
 
             }
 
         } catch (Exception e) {
             logger.error(e, e);
             response.sendError(500, e.getMessage());
             return;
         }
 
 
     }
 
     private void storeReading(HttpServletResponse response, String sID, String type, String units, String value) throws IOException {
 
 
         if (sID == null || sID.length() == 0) {
             response.sendError(500, "Sensor ID was null or empty");
             return;
         }
 
         if (type == null || type.length() == 0) {
             response.sendError(500, "Type was null or empty");
             return;
         }
 
         if (units == null || units.length() == 0) {
             response.sendError(500, "Units was null or empty");
             return;
         }
 
         if (value == null || value.length() == 0) {
             response.sendError(500, "Value was null or empty");
             return;
         }
 
         logger.debug("SensorID:" + sID);
         logger.debug("Type: " + type);
         logger.debug("Units: " + units);
         logger.debug("Value: " + value);
 
         String readingKey = sID + "_" + type;
 
         logger.debug("Reading key is " + readingKey);
         addKeyTable(response, readingKey, units);
 
         try {
 
             Context initCtx = new InitialContext();
             Context envCtx = (Context) initCtx.lookup("java:comp/env");
             DataSource ds = (DataSource) envCtx.lookup("jdbc/SC4");
 
             if (ds == null) {
                 logger.fatal("DataSource came back null");
                 response.sendError(500, "DataSource came backnull");
                 return;
             }
 
             Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO " + readingKey + " (time, data) VALUES(?,?)");
 
             java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
             logger.debug("Timestamp:" + ts);
 
             ps.setTimestamp(1, ts);
 
             logger.debug("Value:" + Float.parseFloat(value));
             ps.setFloat(2, Float.parseFloat(value));
 
             int uc = ps.executeUpdate();
             logger.debug("UpdateCount: " + uc);
             ps.close();
 
             logger.info("A:" + sID + ":" + value);
 
 
             conn.close();
            //envCtx.close();
             initCtx.close();
 
         } catch (Exception ex) {
             logger.error(ex, ex);
         }
 
     }
 
     private void addKeyTable(HttpServletResponse response, String readingKey, String units) {
 
         Context initCtx = null;
         Context envCtx = null;
         DataSource ds = null;
         Connection conn = null;
 
         try {
 
             initCtx = new InitialContext();
             envCtx = (Context) initCtx.lookup("java:comp/env");
             ds = (DataSource) envCtx.lookup("jdbc/SC4");
 
             if (ds == null) {
                 throw new Exception("DataSource came back null");
             }
 
             conn = ds.getConnection();
             DatabaseMetaData metaData = conn.getMetaData();
 
             ResultSet rs = metaData.getTables(null, null, readingKey, null);
             if (rs.next() == true) {
                 logger.debug("Table " + readingKey + " was found");
                 conn.close();
                 envCtx.close();
                 initCtx.close();
                 return;
             }
             rs.close();
 
             logger.info("Creating table " + readingKey);
             Statement s = conn.createStatement();
             boolean execute = s.execute("CREATE TABLE " + readingKey + " (time DATETIME , data FLOAT)");
 
             if (execute) {
                 throw new Exception("Execute returned true");
             }
 
             int uc = s.getUpdateCount();
             s.close();
 
             logger.info("getUpdateCount:" + uc);
 
             s = conn.createStatement();
             execute = s.execute("INSERT INTO variables (name, units) VALUES ('" + readingKey + "','" + units + "')");
             if (execute) {
                 logger.warn("Hmm execute returned true for insert");
                 return;
             }
 
             uc = s.getUpdateCount();
             logger.debug("second getUpdateCount:" + uc);
 
             conn.close();
             envCtx.close();
             initCtx.close();
 
 
         } catch (Exception ex) {
 
             logger.error(ex, ex);
 
         } finally {
 
             try {
 
                 if (conn != null) {
                     conn.close();
                 }
                 envCtx.close();
                 initCtx.close();
 
             } catch (Exception e2) {
                 logger.error(e2, e2);
 
             }
         }
 
     }
     //
     //
     //
 }
