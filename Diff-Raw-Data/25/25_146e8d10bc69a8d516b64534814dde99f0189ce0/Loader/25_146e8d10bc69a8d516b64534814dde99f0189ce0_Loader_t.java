 package com.md_5.district;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 public class Loader {
 
     private static final HashMap<String, Region> cache = new HashMap<String, Region>();
 
     public static Region load(final String name) {
         if (cache.containsKey(name)) {
             return loadFromCache(name);
         } else {
             return loadFromDisk(name);
         }
     }
 
     private static Region loadFromDisk(final String name) {
         Region region = null;
         try {
             Connection conn = DriverManager.getConnection(Config.connectionString);
            
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ds_regions WHERE name = ?");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
             if (!rs.next()) {
                 return null;
             }
             final World world = Bukkit.getServer().getWorld(rs.getString("world"));
             final int start_x = rs.getInt("start_x");
             final int start_y = rs.getInt("start_y");
             final int start_z = rs.getInt("start_z");
             final Location l1 = new Location(world, start_x, start_y, start_z);
             final int end_x = rs.getInt("end_x");
             final int end_y = rs.getInt("end_y");
             final int end_z = rs.getInt("end_z");
             final Location l2 = new Location(world, end_x, end_y, end_z);
             final String owner = rs.getString("owner");
            
            stmt = conn.prepareStatement("SELECT playerName FROM ds_friends WHERE regionName = ?");
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            
             ArrayList<String> friends = new ArrayList<String>();
            if (rs != null) {
                while (rs.next()) {
                    friends.add(rs.getString(0));
                }
             }
            
            conn.close();
            
             region = new Region(world, l1, l2, owner, friends, name);
             putCache(region);
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return region;
     }
 
     private static Region loadFromCache(final String name) {
         return cache.get(name);
     }
 
     private static synchronized void putCache(final Region region) {
         if (cache.size() > Config.cache) {
             cache.clear();
         }
         cache.put(region.getName(), region);
     }
 
     private static void delCache(final String region) {
         cache.remove(region);
     }
 
     public static void save(final Region r) {
         final String name = r.getName();
         remove(name);
         String sql = "INSERT INTO ds_regions (`name`, `world`, `start_x`, `start_y`, `start_z`, `end_x`, `end_y`, `end_z`, `owner`) VALUES(?,?,?,?,?,?,?,?,?);";
         ArrayList<String> args = new ArrayList<String>();
         args.add(name);
         args.add(r.getWorld().getName());
         args.add(String.valueOf(r.start_x));
         args.add(String.valueOf(r.start_y));
         args.add(String.valueOf(r.start_z));
         args.add(String.valueOf(r.end_x));
         args.add(String.valueOf(r.end_y));
         args.add(String.valueOf(r.end_z));
         args.add(r.getOwner());
         Database.write(sql, args);
         for (String f : r.getMembers()) {
             final ArrayList<String> friends = new ArrayList<String>();
             friends.add(name);
             friends.add(f);
             Database.write("INSERT INTO ds_friends VALUES(?,?);", friends);
         }
         putCache(r);
     }
 
     public static void remove(final String regionName) {
         ArrayList<String> args = new ArrayList<String>();
         args.add(regionName);
         Database.write("DELETE FROM ds_regions WHERE name = ?;", args);
         Database.write("DELETE FROM ds_friends WHERE regionName = ?;", args);
         delCache(regionName);
     }
 
     public static ArrayList<Region> byOwner(final String playerName) {
         ArrayList<Region> regions = new ArrayList<Region>();
         for (final ArrayList<String> name : Database.readRaw("SELECT name FROM ds_regions WHERE owner='" + playerName + "'").values()) {
             regions.add(load(name.get(0)));
         }
         return regions;
     }
 
     public static ArrayList<String> listAll() {
         ArrayList<String> names = new ArrayList<String>();
         for (final ArrayList<String> name : Database.readRaw("SELECT name FROM ds_regions").values()) {
             names.add(name.get(0));
         }
         return names;
     }
 }
