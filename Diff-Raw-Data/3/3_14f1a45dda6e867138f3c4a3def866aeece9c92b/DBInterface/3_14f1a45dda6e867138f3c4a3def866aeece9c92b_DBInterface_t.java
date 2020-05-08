 package db;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 
 import models.Entertainment;
 
 public class DBInterface {
     private Properties props = new Properties();
     private String driver = "com.mysql.jdbc.Driver";
     private Connection conn = null;
     private static DBInterface instance = new DBInterface();
 
     public DBInterface() {
         try {
             props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         try {
             try {
                 Class.forName(driver);
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
 
             conn = DriverManager.getConnection(props.getProperty("dbURL"), props.getProperty("dbUSERNAME"), props.getProperty("dbPASSWD"));
             if(!conn.isClosed()){
                 System.out.println("Succeeded connecting to the Database");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public Connection getConnection() {
         return conn;
     }
 
     public ArrayList<Entertainment> getEntertainmentByName(String query) {
         ArrayList<Entertainment> result = new ArrayList<Entertainment>();
 
         try {
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT entertainment_list.*, keyword.keyword from entertainment_list join keyword on entertainment_list.title = keyword.target where title LIKE '%" + query + "%';");
 
             while(rs.next()) {
                 Entertainment e = new Entertainment();
                 e.id = rs.getString("id");
                 e.name = rs.getString("title");
                 e.address = rs.getString("address");
                 e.price = rs.getInt("price");
                 e.rate = rs.getInt("remark");
                if (rs.getString("keyword").isEmpty()) {
                    continue;
                }
                 e.setKeyword(rs.getString("keyword"));
 
                 if (result.contains(e) == false) {
                     result.add(e);
                 }
             }
 
             statement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     public ArrayList<Entertainment> getEntertainmentByKeyword(String query) {
         ArrayList<Entertainment> result = new ArrayList<Entertainment>();
 
         try {
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT entertainment_list.*, keyword.keyword from entertainment_list join keyword on entertainment_list.title = keyword.target where keyword.keyword LIKE '%" + query + "::=%';");
 
             while(rs.next()) {
                 Entertainment e = new Entertainment();
                 e.id = rs.getString("id");
                 e.name = rs.getString("title");
                 e.address = rs.getString("address");
                 e.price = rs.getInt("price");
                 e.rate = rs.getInt("remark");
                 e.setKeyword(rs.getString("keyword"));
 
                 result.add(e);
             }
 
             statement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     public ArrayList<Entertainment> getRandomEntertainment(int limit) {
         ArrayList<Entertainment> result = new ArrayList<Entertainment>();
 
         try {
             while (result.size() < limit) {
                 Statement statement = conn.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT r1.*, keyword.keyword FROM entertainment_list AS r1" +
                         " JOIN (SELECT (RAND() * (SELECT MAX(id) FROM entertainment_list)) AS id) AS r2" +
                         " JOIN keyword ON r1.title = keyword.target" +
                         " WHERE r1.id >= r2.id  AND r1.remark > 35 ORDER BY r1.id ASC  LIMIT 1;");
 
                 while(rs.next()) {
                     Entertainment e = new Entertainment();
                     e.id = rs.getString("id");
                     e.name = rs.getString("title");
                     e.address = rs.getString("address");
                     e.price = rs.getInt("price");
                     e.rate = rs.getInt("remark");
                     e.setKeyword(rs.getString("keyword"));
 
                     result.add(e);
                 }
 
                 statement.close();
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     public Entertainment getEntertainmentById(String id) {
         Entertainment result = new Entertainment();
 
         try {
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT entertainment_list.* from entertainment_list where id = '" + id + "';");
 
             while(rs.next()) {
                 result.id = rs.getString("id");
                 result.name = rs.getString("title");
                 result.address = rs.getString("address");
                 result.price = rs.getInt("price");
                 result.rate = rs.getInt("remark");
             }
 
             statement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     public static DBInterface getInstance() {
         return instance;
     }
 }
