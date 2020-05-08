 package com.herocraftonline.dev.heroes.persistance;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 /**
  * Player management
  * 
  * @author Herocraft's Plugin Team
  */
 public class PlayerManager {
 
     /**
      * Grab the given Players current Experience.
      * @param p
      * @return
      */
     public static int getExp(Player p) {
         String name = p.getName(); // Grab the players name.
         int value = -1;
         try {
             Connection conn = Heroes.sql.getConnection();
             Statement s = conn.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM players WHERE name='" + name + "'");
             r.next();
             value = r.getInt("exp");
             r.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return value;
     }
     
     /**
      * Set the given Players Exp to the value given.
      * @param p
      * @param e
      * @throws Exception
      */
     public static void setExp(Player p, Integer e) throws Exception {
         String n = p.getName();
         Heroes.sql.tryUpdate("UPDATE players SET `exp`=" + e + " WHERE `name`=" + n);
     }
 
     /**
      * Grab the given Players Class.
      * @param p
      * @return
      */
     public static String getClass(Player p) {
         String pClass = null;
         String name = p.getName();
         try {
             Connection conn = Heroes.sql.getConnection();
             Statement s = conn.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM players WHERE name='" + name + "'");
             r.next();
             pClass = r.getString("class");
             r.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return pClass;
     }
 
     /**
      * Change the Players Class to the specified Class.
      * @param p
      * @param c
      */
     public static void setClass(Player p, String c) {
         String n = p.getName();
         Heroes.sql.tryUpdate("UPDATE players SET class='" + c + "' WHERE name='" + n + "'");
     }
 
     /**
      * Insert the Player into the Database with the default class.
      * @param p
      */
     public static void newPlayer(Player p) {
         String n = p.getName();
         Heroes.sql.tryUpdate("INSERT INTO 'players' (name, class, exp, mana) VALUES ('" + n + "','Vagrant', '0', '0') "); // Vargrant needs to change, *customizeable*
     }
 
     /**
      * Check if the Player already exists within the Database.
      * @param n
      * @return
      */
     public static boolean checkPlayer(String n) {
        String query = "SELECT COUNT(*) AS rowcount FROM players WHERE name='" + n + "'";
         if (Heroes.sql.rowCount(query) > 0) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Convert the given Exp into the correct Level.
      * @param exp
      * @return
      */
     public static int getLevel(Integer exp) {
         int n = 0;
         for (Integer i : Properties.level) {
             if (!(exp >= i)) {
                 return n;
             }
             n++;
         }
         return -1;
     }
 
 }
