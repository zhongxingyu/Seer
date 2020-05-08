 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nijiko.simpleshop;
 
 import com.nijiko.simpleshop.util.FileManager;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  *
  * @author Nijiko
  */
 public class Wrapper {
 
     public enum Type {
         SQLITE,
         MYSQL,
         FLATFILE;
     };
     
     public String[][] SQLITE_UPDATES = new String[][] {
         new String[] {
             "ALTER TABLE SimpleShop ADD stock INTEGER(255)"
         }
     };
     
     public String[][] MYSQL_UPDATES = new String[][] {
         new String[] {
             "ALTER TABLE `SimpleShop` ADD `stock` INT( 255 ) NOT NULL AFTER  `per`"
         }
     };
 
     public Type database = null;
 
     /*
      * Tip array for less database usage
      */
     public ArrayList<String> Tips = new ArrayList<String>();
     public int i = 0;
 
     public Wrapper(Type database) {
         this.database = database;
         this.initialize();
     }
 
     private void initialize() {
         if (!checkTable()) {
             SimpleShop.log.info("[" + SimpleShop.name + "] Creating database.");
             createTable();
             SimpleShop.log.info("[" + SimpleShop.name + "] Database Created.");
         }
 
         update();
     }
 
     private Connection connection() throws ClassNotFoundException, SQLException {
         if (this.database.equals(database.SQLITE)) {
             Class.forName("org.sqlite.JDBC");
             return DriverManager.getConnection(SimpleShop.sqlite);
         } else {
             Class.forName("com.mysql.jdbc.Driver");
             return DriverManager.getConnection(SimpleShop.mysql, SimpleShop.mysql_user, SimpleShop.mysql_pass);
         }
     }
 
     private boolean checkTable() {
         Connection conn = null;
         ResultSet rs = null;
 
         try {
             conn = this.connection();
             DatabaseMetaData dbm = conn.getMetaData();
             rs = dbm.getTables(null, null, "SimpleShop", null);
             return rs.next();
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Table check for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + " Failed: " + ex);
             return false;
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
             return false;
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
     }
 
     private void createTable() {
         Connection conn = null;
         Statement st = null;
 
         try {
             conn = this.connection();
             st = conn.createStatement();
 
             if (this.database.equals(database.SQLITE)) {
                 st.executeUpdate("CREATE TABLE `SimpleShop` ( `id` INT ( 255 ) PRIMARY KEY , `item` INT ( 255 ) NOT NULL, `type` INT ( 255 ) NOT NULL, `buy` INT ( 255 ) NOT NULL, `sell` INT ( 255 ) NOT NULL, `per` INT ( 255 ) NOT NULL, `stock` INT ( 255 ) NOT NULL);CREATE INDEX itemIndex on balances (item);CREATE INDEX typeIndex on balances (type);CREATE INDEX buyIndex on iBalances (buy);CREATE INDEX sellIndex on iBalances (sell);CREATE INDEX perIndex on iBalances (per);");
             } else {
                 st.executeUpdate("CREATE TABLE `SimpleShop` ( `id` INT( 255 ) NOT NULL AUTO_INCREMENT, `item` INT( 255 ) NOT NULL, `type` INT( 255 ) NOT NULL, `buy` INT( 255 ) NOT NULL, `sell` INT( 255 ) NOT NULL, `per` INT( 255 ) NOT NULL, `stock` INT( 255 ) NOT NULL, PRIMARY KEY ( `id` ), INDEX ( `item`, `type`, `buy`, `sell`, `per` )) ENGINE = MYISAM;");
             }
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not create table for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
             return;
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
             return;
         } finally {
             try {
                 if (st != null) {
                     st.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
     }
 
     private void update() {
         FileManager FileManager = new FileManager(SimpleShop.directory, "VERSION.txt", false);
         boolean update = false;
        double version = 0.0;

        try {
            version = Double.valueOf(SimpleShop.version);
        } catch(NumberFormatException e) {
            version = 1.4;
        }
 
         if(!FileManager.exists()) {
             update = true;
             version = 1.2;
         } else {
             FileManager.read();
 
             if(!FileManager.getSource().equalsIgnoreCase(SimpleShop.version)) {
                 update = true;
                 version = Double.valueOf(FileManager.getSource().trim());
             }
         }
 
         if(update) {
             SimpleShop.log.info("[" + SimpleShop.name + "] Update to version " + SimpleShop.version + " required.");
 
             Connection conn = null;
             Statement st = null;
 
             try {
                 conn = this.connection();
                 st = conn.createStatement();
                 String[][] UPDATES = (this.database.equals(database.SQLITE)) ? SQLITE_UPDATES : MYSQL_UPDATES;
 
                 if(version < 1.3) {
                     SimpleShop.log.info(" - Updating " + this.database.toString() + " for 1.3");
                         for(String line : UPDATES[0]) { st.executeUpdate(line); }
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe(" x Update failed : " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                 return;
             } catch (ClassNotFoundException e) {
                 SimpleShop.log.severe(" x Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                 return;
             } finally {
                 try {
                     if (st != null) {
                         st.close();
                     }
                     if (conn != null) {
                         conn.close();
                     }
                 } catch (SQLException ex) {
                     SimpleShop.log.severe(" x Failed to close connection");
                 }
             }
             
             SimpleShop.log.info(" - Updating VERSION.txt");
                 FileManager.write(SimpleShop.version);
 
             SimpleShop.log.info("[" + SimpleShop.name + "] + Finished.");
         }
     }
 
     public void add(int itemId, int type, int buy, int sell, int per, int stock) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("INSERT INTO SimpleShop (item,type,buy,sell,per,stock) VALUES (?,?,?,?,?,?)");
             ps.setInt(1, itemId);
             ps.setInt(2, type);
             ps.setInt(3, buy);
             ps.setInt(4, sell);
             ps.setInt(5, per);
             ps.setInt(6, stock);
             ps.executeUpdate();
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not add item for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
             return;
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
             return;
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
     }
 
     public void update(int itemId, int oldType, int type, int buy, int sell, int per, int stock) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("UPDATE SimpleShop SET type = ?, buy = ?, sell = ?, per = ?, stock = ? WHERE item = ? AND type = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
             ps.setInt(1, type);
             ps.setInt(2, buy);
             ps.setInt(3, sell);
             ps.setInt(4, per);
             ps.setInt(5, stock);
             ps.setInt(6, itemId);
             ps.setInt(7, oldType);
             ps.executeUpdate();
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not update item for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
             return;
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
             return;
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
     }
 
     public void remove(int itemId) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("DELETE FROM SimpleShop WHERE item = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
             ps.setInt(1, itemId);
             ps.executeUpdate();
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not remove item for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
             return;
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
             return;
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
     }
 
     public ArrayList<int[]> list() {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         ArrayList<int[]> data = new ArrayList<int[]>();
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("SELECT * FROM SimpleShop");
             rs = ps.executeQuery();
 
             while (rs.next()) {
                 data.add(new int[]{rs.getInt("item"), rs.getInt("type"), rs.getInt("buy"), rs.getInt("sell"), rs.getInt("per"), rs.getInt("stock")});
             }
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not remove item for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
 
         return data;
     }
 
     public int[] data(int itemId, int type) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         int[] data = new int[]{-1};
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("SELECT * FROM SimpleShop WHERE item = ? AND type = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
             ps.setInt(1, itemId);
             ps.setInt(2, type);
             rs = ps.executeQuery();
 
             if (rs.next()) {
                 data = new int[]{rs.getInt("item"), rs.getInt("type"), rs.getInt("buy"), rs.getInt("sell"), rs.getInt("per")};
             }
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not grab item data for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
 
         return data;
     }
 
     public int cost(int itemId, int type, int swap) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         int cost = -1;
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("SELECT cost FROM SimpleShop WHERE item = ? AND type = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
             ps.setInt(1, itemId);
             ps.setInt(2, type);
             rs = ps.executeQuery();
 
             if (rs.next()) {
                 cost = (swap == 0) ? rs.getInt("buy") : rs.getInt("sell");
             }
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not grab item data for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
 
         return cost;
     }
 
     public int per(int itemId, int type) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         int per = -1;
 
         try {
             conn = this.connection();
             ps = conn.prepareStatement("SELECT per FROM SimpleShop WHERE item = ? AND type = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
             ps.setInt(1, itemId);
             ps.setInt(2, type);
             rs = ps.executeQuery();
 
             if (rs.next()) {
                 per = rs.getInt("per");
             }
         } catch (SQLException ex) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Could not grab item data for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
         } catch (ClassNotFoundException e) {
             SimpleShop.log.severe("[" + SimpleShop.name + "]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
                 SimpleShop.log.severe("[" + SimpleShop.name + "]: Failed to close connection");
             }
         }
 
         return per;
     }
 }
