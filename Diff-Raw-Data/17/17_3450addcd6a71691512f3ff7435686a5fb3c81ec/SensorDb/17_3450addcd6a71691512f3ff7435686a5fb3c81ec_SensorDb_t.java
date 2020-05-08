 /**
  * 
  */
 package org.georepublic.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import org.georepublic.bean.*;
 
 
 /**
  * @author mbasa
  *
  */
 public class SensorDb {
     
     private String host    = null;
     private String port    = null;
     private String db      = null;
     private String user    = null;
     private String password= null;
 
     /**
      * 
      */
     public SensorDb() {
         ResourceBundle rb =
                 ResourceBundle.getBundle("properties.postgres");
         
         this.host     = rb.getString("host");
         this.port     = rb.getString("port");
         this.db       = rb.getString("db");
         this.user     = rb.getString("user");
         this.password = rb.getString("password");
     }
 
     public Connection getConnection() {
         
         try {
             Class.forName("org.postgresql.Driver");
         } catch (ClassNotFoundException cnfe) {
             System.out.println("Couldn't find the driver!");
             cnfe.printStackTrace();
         }
 
         Connection conn = null;
 
         String url = "jdbc:postgresql://" + this.host 
                 + ":"+ this.port + "/"
                 + this.db;
 
         Properties props = new Properties();
 
         props.setProperty("user", this.user);
         props.setProperty("password", this.password);
 
         try {
             conn = DriverManager.getConnection(url, props);
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return conn;
     }
 
     public void insertAsgData( StreamGauge fd ) {
         Connection conn = this.getConnection();
         PreparedStatement stmt = null;
         
         String pt  = "ST_PointFromText('POINT("+fd.getLon()+
                 " "+fd.getLat()+")',4326)";
         
         String sql = "insert into asg (name,time,value,"+
                 "water_level_change,time_difference,"+
                 "lon,lat,the_geom) "+
                 "values(?,to_timestamp(?,'Mon DD, YYYY HH:MI AM'),?,?,?,?,?,"+
                 pt+") ";
         
         try {
             stmt = conn.prepareStatement(sql);
             
             stmt.setString(1, fd.getName());
             stmt.setString(2, fd.getTime());
             stmt.setDouble(3, fd.getValue());
             stmt.setDouble(4, fd.getWater_level_change());
             stmt.setDouble(5, fd.getTime_difference());
             stmt.setDouble(6, fd.getLon());
             stmt.setDouble(7, fd.getLat());
             
             stmt.executeUpdate();
             
         } catch (SQLException e) {
            //e.printStackTrace();
         }
         
         try {
             if( stmt != null )
                 stmt.close();
         }
         catch (Exception e) {
             ;
         }
         
         try {
             if( conn != null )
                 conn.close();
         }
         catch (Exception e) {
             ;
         }
 
     }
 
     public void insertArgData( RainGauge fd ) {
         Connection conn = this.getConnection();
         PreparedStatement stmt = null;
         
         String pt  = "ST_PointFromText('POINT("+fd.getLon()+
                 " "+fd.getLat()+")',4326)";
         
         String sql = "insert into arg (name,time,value,"+
                 "lon,lat,the_geom) "+
                 "values(?,to_timestamp(?,'Mon DD, YYYY HH:MI AM'),?,?,?,"+
                 pt+") ";
         
         try {
             stmt = conn.prepareStatement(sql);
             
             stmt.setString(1, fd.getName());
             stmt.setString(2, fd.getTime());
             stmt.setDouble(3, fd.getValue());
             stmt.setDouble(4, fd.getLon());
             stmt.setDouble(5, fd.getLat());
             
             stmt.executeUpdate();
         } catch (SQLException e) {
           // e.printStackTrace();
         }
         
         try {
             if( stmt != null )
                 stmt.close();
         }
         catch (Exception e) {
             ;
         }
         
         try {
             if( conn != null )
                 conn.close();
         }
         catch (Exception e) {
             ;
         }
 
     }
     
     public void insertAwsData( WaterGauge fd ) {
         Connection conn = this.getConnection();
         PreparedStatement stmt = null;
         
         String pt  = "ST_PointFromText('POINT("+fd.getLon()+
                 " "+fd.getLat()+")',4326)";
         
         String sql = "insert into aws (name,time,"+
                 "temp,humi,pres,rain,"+
                 "lon,lat,the_geom) "+
                 "values(?,to_timestamp(?,'Mon DD, YYYY HH:MI AM'),?,?,?,?,?,?,"+
                 pt+") ";
         
         try {
             stmt = conn.prepareStatement(sql);
             
             stmt.setString(1, fd.getName());
             stmt.setString(2, fd.getTime());
             stmt.setDouble(3, fd.getTemp());
             stmt.setDouble(4, fd.getHumi());
             stmt.setDouble(5, fd.getPres());
             stmt.setDouble(6, fd.getRain());
             stmt.setDouble(7, fd.getLon());
             stmt.setDouble(8, fd.getLat());
             
             stmt.executeUpdate();
             
         } catch (SQLException e) {
            //e.printStackTrace();
         }
         
         try {
             if( stmt != null )
                 stmt.close();
         }
         catch (Exception e) {
             ;
         }
         
         try {
             if( conn != null )
                 conn.close();
         }
         catch (Exception e) {
             ;
         }
 
     }
     
 
     public void insertTdData( TideGauge fd ) {
         Connection conn = this.getConnection();
         PreparedStatement stmt = null;
         
         String pt  = "ST_PointFromText('POINT("+fd.getLon()+
                 " "+fd.getLat()+")',4326)";
         
         String sql = "insert into td (name,time,"+
                "sunrise,sunset,moonrise,moonset,"+
                 "value,water_level_change,time_difference,"+
                 "lon,lat,the_geom) "+
                 "values(?,to_timestamp(?,'Mon DD, YYYY HH:MI AM'),"+
                 "to_timestamp(?,'Mon DD, YYYY HH:MI AM'),"+
                 "to_timestamp(?,'Mon DD, YYYY HH:MI AM'),"+
                 "to_timestamp(?,'Mon DD, YYYY HH:MI AM'),"+
                 "to_timestamp(?,'Mon DD, YYYY HH:MI AM'),"+
                 "?,?,?,?,?,"+
                 pt+") ";
         
         try {
             stmt = conn.prepareStatement(sql);
             
             stmt.setString(1, fd.getName());
             stmt.setString(2, fd.getTime());
             stmt.setString(3, fd.getSunrise());
             stmt.setString(4, fd.getSunset());
             stmt.setString(5, fd.getMoonrise());
             stmt.setString(6, fd.getMoonset());
             stmt.setDouble(7, fd.getValue());
             stmt.setDouble(8, fd.getWater_level_change());
             stmt.setDouble(9, fd.getTime_difference());
             stmt.setDouble(10,fd.getLon());
             stmt.setDouble(11,fd.getLat());
             
             stmt.executeUpdate(); 
             
         } catch (SQLException e) {
          // e.printStackTrace();
         }
         
         try {
             if( stmt != null )
                 stmt.close();
         }
         catch (Exception e) {
             ;
         }
         
         try {
             if( conn != null )
                 conn.close();
         }
         catch (Exception e) {
             ;
         }
 
     }
 }
