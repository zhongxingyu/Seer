 package com.tyzoid.jailr.models;
 
 import com.tyzoid.jailr.util.ExceptionPrinter;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Prisoner extends Model {
     int id;
     String player;
     int created_time;
     int sentence_time;
     int served_time;
     String reason;
     String jailer;
     String usergroup;
     String inventory;
 
     private Prisoner(int id, String player, int created_time, int sentence_time, int served_time, String reason, String jailer, String usergroup, String inventory) {
         this.saved = true;
         this.tableName = this.config.get("prefix") + "prisoners";
 
         this.id = id;
         this.player = player;
         this.created_time = created_time;
         this.sentence_time = sentence_time;
         this.served_time = served_time;
         this.reason = reason;
         this.jailer = jailer;
         this.usergroup = usergroup;
         this.inventory = inventory;
     }
 
     public Prisoner(int created_time, String player, int sentence_time, int served_time, String reason, String jailer, String usergroup, String inventory) {
         this.saved = false;
         this.tableName = this.config.get("prefix") + "prisoners";
 
         this.player = player;
         this.created_time = created_time;
         this.sentence_time = sentence_time;
         this.served_time = served_time;
         this.reason = reason;
         this.jailer = jailer;
         this.usergroup = usergroup;
         this.inventory = inventory;
     }
 
     @Override
     public String toString() {
         return String.format("#%s:%s:%s:%s:%s:%s:%s", this.created_time, this.player, this.sentence_time, this.served_time, this.reason, this.jailer, this.inventory);
     }
 
     public void save() {
         switch (this.engine) {
             case H2:
                 try {
                     if (!this.saved) {
                         this.conn = DatabaseCommon.getConnection();
                        this.pst = this.conn.prepareStatement("INSERT INTO " + this.tableName + " (player, created_time, sentence_time, served_time, reason, jailer, usergroup, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                        this.pst.setString(1, this.player);
                        this.pst.setInt(2, this.created_time);
                        this.pst.setInt(3, this.sentence_time);
                        this.pst.setInt(4, this.served_time);
                        this.pst.setString(5, this.reason);
                        this.pst.setString(6, this.jailer);
                        this.pst.setString(7, this.usergroup);
                        this.pst.setString(8, this.inventory);
 
                         this.pst.executeUpdate();
 
                         this.saved = true;
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (this.rs != null) {
                             this.rs.close();
                         }
 
                         if (this.pst != null) {
                             this.pst.close();
                         }
 
                         if (this.conn != null) {
                             this.conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
             case MYSQL:
                 try {
                     if (!this.saved) {
                         this.conn = DatabaseCommon.getConnection();
                         this.pst = this.conn.prepareStatement("INSERT INTO " + this.tableName + " (player, created_time, sentence_time, served_time, reason, jailer, usergroup, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                         this.pst.setString(1, this.player);
                         this.pst.setInt(2, this.created_time);
                         this.pst.setInt(3, this.sentence_time);
                         this.pst.setInt(4, this.served_time);
                         this.pst.setString(5, this.reason);
                         this.pst.setString(6, this.jailer);
                         this.pst.setString(7, this.usergroup);
                         this.pst.setString(8, this.inventory);
 
                         this.pst.executeUpdate();
 
                         this.saved = true;
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (this.rs != null) {
                             this.rs.close();
                         }
 
                         if (this.pst != null) {
                             this.pst.close();
                         }
 
                         if (this.conn != null) {
                             this.conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
         }
     }
 
     public static List<Prisoner> selectAll() {
         List<Prisoner> prisoners = new ArrayList<Prisoner>();
         String tableName = DatabaseCommon.getDatabaseConfig().get("prefix") + "prisoners";
 
         Connection conn = null;
         Statement st = null;
         ResultSet rs = null;
 
         switch (DatabaseCommon.getEngine()) {
             case H2:
                 try {
                     conn = DatabaseCommon.getConnection();
                     st = conn.createStatement();
                     rs = st.executeQuery("SELECT * FROM " + tableName);
 
                     while (rs.next()) {
                         prisoners.add(new Prisoner(rs.getInt("id"), rs.getString("player"), rs.getInt("created_time"), rs.getInt("sentence_time"), rs.getInt("served_time"), rs.getString("reason"), rs.getString("jailer"), rs.getString("usergroup"), rs.getString("inventory")));
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (rs != null) {
                             rs.close();
                         }
 
                         if (st != null) {
                             st.close();
                         }
 
                         if (conn != null) {
                             conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
             case MYSQL:
                 try {
                     conn = DatabaseCommon.getConnection();
                     st = conn.createStatement();
                     rs = st.executeQuery("SELECT * FROM " + tableName);
 
                     while (rs.next()) {
                         prisoners.add(new Prisoner(rs.getInt("id"), rs.getString("player"), rs.getInt("created_time"), rs.getInt("sentence_time"), rs.getInt("served_time"), rs.getString("reason"), rs.getString("jailer"), rs.getString("usergroup"), rs.getString("inventory")));
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (rs != null) {
                             rs.close();
                         }
 
                         if (st != null) {
                             st.close();
                         }
 
                         if (conn != null) {
                             conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
         }
 
         return prisoners;
     }
 
     public static List<Prisoner> selectWhere(String where) {
         List<Prisoner> prisoners = new ArrayList<Prisoner>();
         String tableName = DatabaseCommon.getDatabaseConfig().get("prefix") + "prisoners";
 
         Connection conn = null;
         Statement st = null;
         ResultSet rs = null;
 
         switch (DatabaseCommon.getEngine()) {
             case H2:
                 try {
                     conn = DatabaseCommon.getConnection();
                     st = conn.createStatement();
                     rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE " + where);
 
                     while (rs.next()) {
                         prisoners.add(new Prisoner(rs.getInt("id"), rs.getString("player"), rs.getInt("created_time"), rs.getInt("sentence_time"), rs.getInt("served_time"), rs.getString("reason"), rs.getString("jailer"), rs.getString("usergroup"), rs.getString("inventory")));
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (rs != null) {
                             rs.close();
                         }
 
                         if (st != null) {
                             st.close();
                         }
 
                         if (conn != null) {
                             conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
             case MYSQL:
                 try {
                     conn = DatabaseCommon.getConnection();
                     st = conn.createStatement();
                     rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE " + where);
 
                     while (rs.next()) {
                         prisoners.add(new Prisoner(rs.getInt("id"), rs.getString("player"), rs.getInt("created_time"), rs.getInt("sentence_time"), rs.getInt("served_time"), rs.getString("reason"), rs.getString("jailer"), rs.getString("usergroup"), rs.getString("inventory")));
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (rs != null) {
                             rs.close();
                         }
 
                         if (st != null) {
                             st.close();
                         }
 
                         if (conn != null) {
                             conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
         }
 
         return prisoners;
     }
 
     public static void removeWhere(String where) {
         String tableName = DatabaseCommon.getDatabaseConfig().get("prefix") + "prisoners";
 
         Connection conn = null;
         Statement st = null;
         ResultSet rs = null;
 
         switch (DatabaseCommon.getEngine()) {
             case H2:
                 try {
                     conn = DatabaseCommon.getConnection();
                     st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                     rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE " + where);
 
                     while (rs.next()) {
                         rs.deleteRow();
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (rs != null) {
                             rs.close();
                         }
 
                         if (st != null) {
                             st.close();
                         }
 
                         if (conn != null) {
                             conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
             case MYSQL:
                 try {
                     conn = DatabaseCommon.getConnection();
                     st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                     rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE " + where);
 
                     while (rs.next()) {
                         rs.deleteRow();
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (rs != null) {
                             rs.close();
                         }
 
                         if (st != null) {
                             st.close();
                         }
 
                         if (conn != null) {
                             conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
         }
     }
 
     public int getId() {
         return id;
     }
 
     public String getPlayer() {
         return player;
     }
 
     public void setPlayer(String player) {
         this.player = player;
     }
 
     public int getCreated_time() {
         return created_time;
     }
 
     public void setCreated_time(int created_time) {
         this.created_time = created_time;
     }
 
     public int getSentence_time() {
         return sentence_time;
     }
 
     public void setSentence_time(int sentence_time) {
         this.sentence_time = sentence_time;
     }
 
     public int getServed_time() {
         return served_time;
     }
 
     public void setServed_time(int served_time) {
         this.served_time = served_time;
     }
 
     public String getReason() {
         return reason;
     }
 
     public void setReason(String reason) {
         this.reason = reason;
     }
 
     public String getJailer() {
         return jailer;
     }
 
     public void setJailer(String jailer) {
         this.jailer = jailer;
     }
 
     public String getUsergroup() {
         return usergroup;
     }
 
     public void setUsergroup(String usergroup) {
         this.usergroup = usergroup;
     }
 
     public String getInventory() {
         return inventory;
     }
 
     public void setInventory(String inventory) {
         this.inventory = inventory;
     }
 }
