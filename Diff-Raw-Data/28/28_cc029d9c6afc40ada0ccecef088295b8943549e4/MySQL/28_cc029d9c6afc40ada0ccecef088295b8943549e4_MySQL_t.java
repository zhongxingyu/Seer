 package com.guntherdw.bukkit.TweakWarp.DataSource;
 
 import com.guntherdw.bukkit.TweakWarp.TweakWarp;
 import com.guntherdw.bukkit.TweakWarp.Warp;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author GuntherDW
  */
 public class MySQL {
 
     private TweakWarp plugin;
     private String db, user, pass, dbhost;
 
     public MySQL(TweakWarp instance) {
         this.plugin = instance;
         this.loadDriver();
         this.setupConnection();
     }
 
     public void initConfig()
     {
         try{
             plugin.getConfiguration().setProperty("database", "databasename");
             plugin.getConfiguration().setProperty("username", "database-username");
             plugin.getConfiguration().setProperty("password", "database-password");
         } catch (Throwable e)
         {
             plugin.getLogger().severe("[TweakWarp] There was an exception while we were saving the config, be sure to doublecheck!");
         }
     }
 
     private void loadDriver() {
         final String driverName = "com.mysql.jdbc.Driver";
         try {
             Class.forName(driverName);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             //return null;
         }
     }
 
     private Connection getConnection()
     {
         try {
             String url = "jdbc:mysql://"+dbhost+":3306/" + db;
             return DriverManager.getConnection(url + "?autoReconnect=true&user=" + user + "&password=" + pass);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
     public void setupConnection() {
         this.dbhost = plugin.getConfiguration().getString("dbhost");
         this.db =  plugin.getConfiguration().getString("database");
         this.user = plugin.getConfiguration().getString("username");
         this.pass = plugin.getConfiguration().getString("password");
     }
 
     public List<Warp> getAllWarps() {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         List<Warp> warps = null;
         Warp temp = null;
         try {
             int count = 0;
             st = conn.prepareStatement("SELECT id,name,x,y,z,pitch,yaw,world,warpgroup,accessgroup FROM `warps`");
             rs = st.executeQuery();
             while(rs.next()) {
                 temp = new Warp(rs.getDouble("x"),
                         rs.getDouble("y"),
                         rs.getDouble("z"),
                         rs.getFloat("pitch"),
                         rs.getFloat("yaw"),
                         rs.getString("name"),
                         rs.getString("world"),
                         rs.getString("warpgroup"),
                         rs.getString("accessgroup")
                 );
                 temp.setId(rs.getInt("id"));
 
                 if(warps==null)
                     warps = new ArrayList<Warp>();
                 warps.add(temp);
 
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return warps;
     }
 
     public Warp getWarp(int id) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         Warp temp = null;
         try {
             int count = 0;
             st = conn.prepareStatement("SELECT id,name,x,y,z,pitch,yaw,world,warpgroup,accessgroup FROM `warps` WHERE `id` = ?");
             st.setInt(1, id);
             rs = st.executeQuery();
             if(rs.next()) {
                 temp = new Warp(rs.getDouble("x"),
                         rs.getDouble("y"),
                         rs.getDouble("z"),
                         rs.getFloat("pitch"),
                         rs.getFloat("yaw"),
                         rs.getString("name"),
                         rs.getString("world"),
                         rs.getString("warpgroup"),
                         rs.getString("accessgroup")
                 );
                 temp.setId(rs.getInt("id"));
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return temp;
     }
 
     public Warp getWarp(String name, String warpgroup) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         Warp temp = null;
         try {
             int count = 0;
             st = conn.prepareStatement("SELECT id,name,x,y,z,pitch,yaw,world,warpgroup,accessgroup FROM `warps` WHERE `name` = ? AND `warpgroup` = ?");
             st.setString(1, name);
             st.setString(2, warpgroup);
             rs = st.executeQuery();
             if(rs.next()) {
                 temp = new Warp(rs.getDouble("x"),
                         rs.getDouble("y"),
                         rs.getDouble("z"),
                         rs.getFloat("pitch"),
                         rs.getFloat("yaw"),
                         rs.getString("name"),
                         rs.getString("world"),
                         rs.getString("warpgroup"),
                         rs.getString("accessgroup")
                 );
                 temp.setId(rs.getInt("id"));
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return temp;
     }
 
     public int addWarp(Warp warp) { // Returns the id for the newly generated warp
         if(warp==null) return -1;
         //boolean isUpdate = warp.getId()!=-1?true:false;
 
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         Warp temp = null;
         try {
             // Delete any warp that was there beforehand
             this.deleteWarp(warp.getName(), warp.getWarpgroup());
 
            st = conn.prepareStatement("INSERT INTO `warps` (name,x,y,z,pitch,yaw,world,warpgroup,accessgroup) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
             st.setString(1, warp.getName());
             st.setDouble(2, warp.getX());
             st.setDouble(3, warp.getY());
             st.setDouble(4, warp.getZ());
             st.setFloat (5, warp.getPitch());
             st.setFloat (6, warp.getYaw());
             st.setString(7, warp.getWorld());
             st.setString(8, warp.getWarpgroup());
             st.setString(9, warp.getAccessgroup());
 
             // st.executeQuery();
             rs = st.getGeneratedKeys();
            if(rs.next())
                 return rs.getInt(0);
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return -1;
     }
 
     public void deleteWarp(int id) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         try{
             st = conn.prepareStatement("DELETE FROM `warps` WHERE `id` = ?");
             st.setInt(1, id);
            st.executeUpdate();
             return;
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void deleteWarp(String warpname, String warpgroup) {
         if(warpname==null||warpname.trim().equals("")) return;
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         try{
             st = conn.prepareStatement("DELETE FROM `warps` WHERE `name` = ? AND `warpgroup` = ?");
             st.setString(1, warpname);
             st.setString(2, warpgroup);
            st.executeUpdate();
             return;
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
 }
