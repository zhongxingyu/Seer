 /*
  GossipNetSim
  Copyright (C) 2012  michael theodorides <mc.theodorides@gmail.com>
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package us.elfua.gossipnetsim.helpers.data;
 
 import java.sql.*;
 
 /**
  *
  * @author michael theodorides
  */
 public class CustomSQLConnection {
 
    private String url = "jdbc:mysql://localhost:3306/";
     private String dbName = "gossipdb";
    private String driver = "com.mysql.jdbc.Driver";
    private String userName = "root";
     private String password = "admin";
     //connection vars
     private Connection conn = null;
     private Statement stat = null;
     private ResultSet resu = null;
     private DataManager dataMan = null;
 
     public CustomSQLConnection(DataManager dm) {
 
         super();
         this.dataMan = dm;
         this.createConnection();
 
     }
 
     public ResultSet Query(String SQL) throws SQLException {
 
         this.createConnection();
 
         this.stat = this.conn.createStatement();
         return this.stat.executeQuery(SQL);
 
     }
 
     public int nonQuery(String SQL) throws SQLException {
         this.createConnection();
 
         this.stat = this.conn.createStatement();
         return this.stat.executeUpdate(SQL);
 
     }
 
     public Connection getConn() {
         this.createConnection();
         return this.conn;
     }
 
     private void createConnection() {
         try {
 
             if (this.conn == null) {
                 Class.forName(driver).newInstance();
                 conn = DriverManager.getConnection(url + dbName, userName, password);
             }
 
         } catch (Exception ex) {
 
             Event e = new Event();
             e.sender = "" + this.toString();
             e.status = "SQL connection error in CustomeSQLconnection";
             e.type = "log";
             e.Description = ex.toString();
             this.dataMan.addEvent(e);
 
         }
 
     }
 
     public void closeConnection() {
         try {
             if (this.resu != null) {
                 this.resu.close();
             }
             if (this.stat != null) {
                 this.stat.close();
             }
             if (this.conn != null) {
                 this.conn.close();
             }
 
         } catch (SQLException ex) {
             Event e = new Event();
             e.sender = "" + this.toString();
             e.status = "SQL connection after error closing (finalizing) connection error";
             e.type = "log";
             e.Description = ex.toString();
             this.dataMan.addEvent(e);
             System.exit(0);
         }
     }
 }
