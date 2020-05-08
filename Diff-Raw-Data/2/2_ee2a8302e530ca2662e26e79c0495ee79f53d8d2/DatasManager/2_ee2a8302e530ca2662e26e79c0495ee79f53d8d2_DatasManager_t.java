 package brutes.server.db;
 
 import brutes.server.ui;
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
         } catch (SQLException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return DatasManager.con;
     }
 
     public static void populate() throws IOException {
         try {
             Connection c = DatasManager.getInstance();
             c.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, pseudo TEXT, password TEXT, token TEXT, date_created DATETIME DEFAULT current_timestamp)");
             c.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Bots', 'WTF')");
             c.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Thiktak', 'root1')");
             c.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Kirauks', 'root2')");
             c.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('Bruno', 'mdp')");
            c.createStatement().executeUpdate("INSERT INTO users (pseudo, password) VALUES ('User', 'user')");
 
             c.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS brutes (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, name TEXT, level INTEGER, life INTEGER, strength INTEGER, speed INTEGER, image_id INTEGER, date_created DATETIME DEFAULT current_timestamp)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 1, 'Rukia', 5, 34, 8, 13)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 2, 'Skitt', 1, 10, 4, 3)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 3, 'Tulipe', 3, 22, 19, 6)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 4, 'Zazardify', 2, 16, 14, 3)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 5, 'Gwenn', 3, 26, 5, 21)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 6, 'Ruelle', 2, 16, 4, 10)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 7, 'Sybelle', 3, 24, 10, 4)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 8, 'Sheldon', 1, 10, 1, 6)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 9, 'Hassen', 10, 67, 32, 17)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (1, 10, 'Krossork', 7, 48, 21, 11)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (2, 11, 'Thik', 1, 10, 3, 4)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (3, 12, 'Rauks', 1, 10, 4, 3)");
             c.createStatement().executeUpdate("INSERT INTO brutes (user_id, image_id, name, level, life, strength, speed) VALUES (4, 13, 'Brubru', 1, 10, 5, 2)");
 
             c.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bonus (id INTEGER PRIMARY KEY AUTOINCREMENT, brute_id INTEGER, name TEXT, level INTEGER, life INTEGER, strength INTEGER, speed INTEGER, image_id INTEGER)");
             
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 1, 31, 'Mouton', 1, 10, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 2, 32, 'Amulette', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 3, 33, 'Dagues Gha', 1, 0, 15, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 4, 34, 'Parchemin', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 4, 35, 'Arc Bricolo', 1, 0, 5, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 5, 36, 'Chien', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 6, 37, 'Troll', 1, 0, 5, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 7, 38, 'Epouventail', 1, 20, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 8, 39, 'Squelette', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 9, 40, 'Excalibur', 1, 0, 20, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 10, 41, 'Faux de Sang', 1, 0, 15, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 42, 'Couteau', 1, 0, 5, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 43, 'Koala Rasta', 1, 0, 0, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 44, 'Sceptre', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 45, 'Tortue Luth', 1, 15, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 46, 'Loup', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 47, 'Firechat', 1, 0, 10, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 48, 'Martouïe', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 49, 'Martlave', 1, 0, 20, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 50, 'Casque', 1, 30, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 51, 'Grimoire', 1, 0, 0, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 52, 'Vaudou', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 53, 'Vaudou', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 54, 'Vaudou', 1, 10, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 55, 'Mineur', 1, 0, 5, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 56, 'Zebrarc', 1, 0, 15, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 57, 'Singe', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 58, 'Démon Loup', 1, 5, 5, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 59, 'Mage', 1, 0, 0, 15)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 60, 'Roc enchanté', 1, 20, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 61, 'Canidomme', 1, 0, 20, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 62, 'Myosotis', 1, 5, 0, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 63, 'Scorpion', 1, 0, 15, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 64, 'Aragog', 1, 10, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 65, 'Pelle', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 66, 'Bâton de glace', 1, 0, 0, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 67, 'Bâton de feu', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 68, 'Peluche', 1, 15, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 69, 'Epée', 1, 0, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 70, 'Foxeur', 1, 25, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 71, 'Ecureil', 1, 0, 0, 20)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 72, 'Lapin affamé', 1, 0, 15, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 73, 'Arbre', 1, 20, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 74, 'Gros Lapin', 1, 10, 0, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 75, 'Koala de Sang', 1, 20, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 76, 'Sanglier', 1, 0, 15, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 77, 'Carotte', 1, 30, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 78, 'Crocodile', 1, 0, 10, 10)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 79, 'Epée double', 1, 0, 20, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 80, 'Tortue guerrière', 1, 10, 10, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 81, 'Pingu', 1, 10, 0, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 82, 'Kipik', 1, 0, 15, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 83, 'Gelée verte', 1, 15, 0, 0)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 84, 'Gelée rouge', 1, 0, 5, 5)");
             c.createStatement().executeUpdate("INSERT INTO bonus (brute_id, image_id, name, level, life, strength, speed) VALUES ( 0, 85, 'Poupée', 1, 20, 0, 0)");
             
             c.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS fights (id INTEGER PRIMARY KEY AUTOINCREMENT, brute_id1 INTEGER, brute_id2 INTEGER, winner_id INTEGER, date_created DATETIME DEFAULT current_timestamp)");
         } catch (SQLException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
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
             Class classObj = Class.forName(ui.getClassPath(DatasManager.class) + ".entity." + obj.getClass().getSimpleName() + "Entity");
 
             Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call *.entity.{0}Entity::save", obj.getClass().getSimpleName());
 
             classObj.getMethod("save", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);
 
         } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static <T> T insert(T obj) throws IOException {
         try {
             Class classObj = Class.forName(ui.getClassPath(DatasManager.class) + ".entity." + obj.getClass().getSimpleName() + "Entity");
 
             Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call *.entity.{0}Entity::insert", obj.getClass().getSimpleName());
 
             return (T) classObj.getMethod("insert", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);
 
         } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public static <T> void delete(T obj) throws IOException {
         try {
             Class classObj = Class.forName(ui.getClassPath(DatasManager.class) + ".entity." + obj.getClass().getSimpleName() + "Entity");
 
             Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call *.entity.{0}Entity::delete", obj.getClass().getSimpleName());
 
             classObj.getMethod("delete", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);
 
         } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
             Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
