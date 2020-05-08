 package zwapi;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import zwapi.events.ZWBasicListener;
 import zwapi.events.ZWEvent;
 import zwapi.events.ZWNodeInfoListener;
 import zwapi.nodes.ZWEZMotionSensor;
 import zwapi.nodes.ZWNode;
 
 public class ZWSQLLogger implements ZWBasicListener, ZWNodeInfoListener {
     
     Connection conn = null;
     Statement stmt;
     DateFormat df;
     
     public ZWSQLLogger() {
         try {
             Class.forName("com.mysql.jdbc.Driver");//load the mysql driver
             conn = DriverManager.getConnection("jdbc:mysql://localhost/kiiib?user=KIIIB&password=42");//connect to the database
             stmt = conn.createStatement();
             
             df = new SimpleDateFormat("HH:mm:ss");
         }
         catch (SQLException se){
             System.out.println("SQLException: " + se.getMessage());
             System.out.println("SQLState: " + se.getSQLState());
             System.out.println("VendorError: " + se.getErrorCode());
  
         }
         catch (Exception e){
              e.printStackTrace();
        }
     }
 
     @Override
     public void ZWBasicEventOccurred(ZWEvent event) {
         ZWEZMotionSensor sensor = (ZWEZMotionSensor) event.getSource();
         int id = sensor.getID();
         System.out.printf("[%s] Sensor%d fired!\n", df.format(new Date()), id);        
         sensorEvent(id);
     }
 
     private void sensorEvent(int sensorId){
         try{
             stmt.executeUpdate("INSERT INTO sensor_events VALUES("+sensorId+",NOW())");
         }catch(SQLException se){
             System.out.println("SQLException: " + se.getMessage());
             System.out.println("SQLState: " + se.getSQLState());
             System.out.println("VendorError: " + se.getErrorCode());
         }
     }
     
 
     @Override
     public void ZWNodeInfoEventOccurred(ZWEvent event) {
         ZWNode source = (ZWNode) event.getSource();
         int id = source.getID();
         System.out.printf("[%s] Switch%d ", df.format(new Date()), id);
         switchEvent(id);
     }
     
     public void switchEvent(int switchId) {
         try {
             ResultSet result = stmt.executeQuery("SELECT id, status, timestamp, "
                     + "TIMESTAMPDIFF(SECOND, timestamp, NOW()) AS diff "
                     + "FROM switch_events " + "WHERE id = " + switchId
                     + " " + "ORDER BY diff " + "LIMIT 1");
            result.next();
          
            if (result.getLong("diff") < 5) {
                 String query = String.format("UPDATE switch_events SET status = 0 WHERE id = %d AND timestamp = '%s'", 
                         switchId, result.getTimestamp("timestamp").toString());
                 System.out.println("off!");
                 stmt.execute(query);        
             } else {
                  String query = String.format("INSERT INTO switch_events VALUES (%d, 1, NOW())",switchId);
                  System.out.println("on!");
                  stmt.execute(query);
             }
         } catch (SQLException se) {
             System.out.println("SQLException: " + se.getMessage());
             System.out.println("SQLState: " + se.getSQLState());
             System.out.println("VendorError: " + se.getErrorCode());
         }
          
     }
 
 }
