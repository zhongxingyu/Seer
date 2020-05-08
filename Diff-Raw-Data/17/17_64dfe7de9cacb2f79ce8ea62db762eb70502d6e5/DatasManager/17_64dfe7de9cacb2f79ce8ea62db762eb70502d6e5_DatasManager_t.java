 package brutes.net.server.db;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Thiktak
  */
 public class DatasManager {
 
     static private Connection con;
 
     public static Connection getInstance(String type, String dbpath) throws IOException {
         Class classType;
         switch (type) {
             case "sqlite":
                 try {
                     classType = Class.forName("org.sqlite.JDBC");
                     dbpath = "jdbc:sqlite:" + dbpath;
                 } catch (ClassNotFoundException e) {
                     throw new IOException(e);
                 }
                 break;
             default:
                 throw new IOException(type + " SQL support not exists");
         }
         try {
             DatasManager.con = DriverManager.getConnection(dbpath);
 
             DatasManager.con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, pseudo TEXT, password TEXT, token TEXT, date_created DATETIME DEFAULT current_timestamp)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Bots', 'WTF')");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Thiktak', 'root1')");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Kirauks', 'root2')");
 
             DatasManager.con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS brutes (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, name TEXT, level INTEGER, life INTEGER, strength INTEGER, speed INTEGER, date_created DATETIME DEFAULT current_timestamp)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO brutes (user_id, name, level, life, strength, speed) VALUES (1, 'Bot_1 : Kikou', 3, 10, 2, 4)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO brutes (user_id, name, level, life, strength, speed) VALUES (1, 'Bot_2 : Ultimate', 10, 250, 23, 18)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO brutes (user_id, name, level, life, strength, speed) VALUES (1, 'Bot_3 : 贝努瓦', 1, 100, 0, 0)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO brutes (user_id, name, level, life, strength, speed) VALUES (2, 'Yéti', 2, 62, 5, 5)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO brutes (user_id, name, level, life, strength, speed) VALUES (3, 'Rauks', 1, 50, 3, 8)");
 
             DatasManager.con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bonus (id INTEGER PRIMARY KEY AUTOINCREMENT, brute_id INTEGER, name TEXT, level INTEGER, strength INTEGER, speed INTEGER)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO bonus (brute_id, name, level, strength, speed) VALUES (1, 'Hache', 1, 15, 5)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO bonus (brute_id, name, level, strength, speed) VALUES (2, 'Robe de lin', 1, 1, 10)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO bonus (brute_id, name, level, strength, speed) VALUES (3, 'Anneau de la forge', 10, 100, 30)");
             DatasManager.con.createStatement().executeUpdate("INSERT INTO bonus (brute_id, name, level, strength, speed) VALUES (5, 'Vif d''or', 5, 0, 60)");
 
             DatasManager.con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS fights (id INTEGER PRIMARY KEY AUTOINCREMENT, brute_id1 INTEGER, brute_id2 INTEGER, winner_id INTEGER, date_created DATETIME DEFAULT current_timestamp)");
         } catch (SQLException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return DatasManager.con;
     }
 
     public static Connection getInstance() throws IOException {
         if (DatasManager.con == null) {
             throw new IOException("No instance of dataManager");
         }
         return DatasManager.con;
     }
 
     public static ResultSet exec(String query) throws IOException, SQLException {
         return DatasManager.getInstance().createStatement().executeQuery(query);
     }
 
     public static PreparedStatement prepare(String query) throws IOException, SQLException {
         return DatasManager.getInstance().prepareStatement(query);
     }
 
     public static Statement getStatement() throws IOException, SQLException {
         return DatasManager.getInstance().createStatement();
     }
     
     public static <T> void save(T obj) throws IOException {
         try {
            Class classObj = Class.forName("brutes.net.server.db.entity." + obj.getClass().getSimpleName() + "Entity");
             
            Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call brutes.net.server.db.entity.{0}Entity::save", obj.getClass().getSimpleName());
             
             classObj.getMethod("save", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);
             
         } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static <T> T insert(T obj) throws IOException {
         try {
            Class classObj = Class.forName("brutes.net.server.db.entity." + obj.getClass().getSimpleName() + "Entity");
             
            Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call brutes.net.server.db.entity.{0}Entity::insert", obj.getClass().getSimpleName());
             
             return (T) classObj.getMethod("insert", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);
             
         } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public static <T> void delete(T obj) throws IOException {
         try {
            Class classObj = Class.forName("brutes.net.server.db.entity." + obj.getClass().getSimpleName() + "Entity");
             
            Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call brutes.net.server.db.entity.{0}Entity::delete", obj.getClass().getSimpleName());
             
             classObj.getMethod("delete", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);
             
         } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
