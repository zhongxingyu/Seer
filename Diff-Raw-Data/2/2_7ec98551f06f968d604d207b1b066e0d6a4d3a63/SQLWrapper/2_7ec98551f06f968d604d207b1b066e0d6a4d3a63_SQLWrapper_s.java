 package net.LoadingChunks.SyncingFeeling.util;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import net.LoadingChunks.SyncingFeeling.SyncingFeeling;
 import net.LoadingChunks.SyncingFeeling.Inventory.SerializableInventory;
 
 public class SQLWrapper {
 	static private SyncingFeeling plugin;
 	static private Connection con;
 	static private boolean success;
 	
 	static private String user;
 	static private String password;
 	static private String host;
 	static private String db;
 	
 	static public void setPlugin(SyncingFeeling plugin) {
 		SQLWrapper.plugin = plugin;
 	}
 	
 	static public void setConfig(String user, String password, String host, String db) {
 		SQLWrapper.user = user;
 		SQLWrapper.password = password;
 		SQLWrapper.host = host;
 		SQLWrapper.db = db;
 	}
 	
 	public static SyncingFeeling getPlugin() {
 		return plugin;
 	}
 	
 	static public boolean connect() {
 		success = true;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			SQLWrapper.con = DriverManager.getConnection("jdbc:mysql://" + SQLWrapper.host + ":3306/" + SQLWrapper.db, SQLWrapper.user, SQLWrapper.password);
 		} catch(SQLException e) {
 			e.printStackTrace();
 			SQLWrapper.success = false;
 		} catch (ClassNotFoundException e) { e.printStackTrace(); SQLWrapper.success = false; }
 		
 		return success;
 	}
 	
 	static public boolean reconnect() {
 		try {
 			if(con.isClosed()) {
 				return connect();
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	static public void commitSlot(Player p, Integer slot, ItemStack stack) {
 		try {
 			reconnect();
 			PreparedStatement stat = con.prepareStatement("REPLACE INTO `inv_slots` (`server`,`player`,`json`,`slot`) VALUES (?,?,?,?)");
 			
 			stat.setString(1, SQLWrapper.plugin.getConfig().getString("general.server.name"));
 			stat.setString(2, p.getName());
 			JSONObject obj = new JSONObject();
 			obj.putAll(stack.serialize());
 			stat.setString(3, obj.toJSONString());
 			stat.setInt(4, slot);
 			
 			stat.execute();
 		} catch (SQLException e) { e.printStackTrace(); }
 	}
 	
 	static public void commitSlots(Player p, SerializableInventory inv) {
 		try {
 			reconnect();
 			String sqlstring = "REPLACE INTO `inv_slots` (`server`,`player`,`json`,`slot`) VALUES ";
 			
 			if(inv.getSlots().size() == 0)
 				return;
 			
 			for(int i = 0; i < inv.getSlots().size(); i++) {
 				sqlstring = sqlstring.concat("(?,?,?,?)");
 				
 				if(i < inv.getSlots().size()-1)
 					sqlstring = sqlstring.concat(",");
 			}
 			
 			PreparedStatement stat = con.prepareStatement(sqlstring);
 			
 			int i = 0;
 			String name = SQLWrapper.plugin.getConfig().getString("general.server.name");
 			for(Entry<Integer, ItemStack> slot : inv.getSlots().entrySet()) {
 				stat.setString((i*4) + 1, name);
 				stat.setString((i*4) + 2, p.getName());
 				
 				JSONObject obj = new JSONObject();
 				obj.putAll(SerializableInventory.serialize(slot.getValue()));
 				
 				stat.setString((i*4) + 3, obj.toJSONString());
 				stat.setInt((i*4) + 4, slot.getKey());
 				i++;
 			}
 			
 			if(plugin.isDebugMode)
 				plugin.getLogger().info("Committing Slot Array: " + stat.toString());
 			
 			stat.execute();
 		} catch (SQLException e) { e.printStackTrace(); }
 	}
 	
 	static public void commitInventory(SerializableInventory inv, Player p, boolean clear) {
 		try {
 			reconnect();
 			PreparedStatement stat = con.prepareStatement("REPLACE INTO `inv_inventories` (`server`,`player`) VALUES (?,?)");
 			
 			stat.setString(1, SQLWrapper.plugin.getConfig().getString("general.server.name"));
 			stat.setString(2, p.getName());
 			
 			stat.execute();
 			
 			if(clear) {
 				PreparedStatement statclear = con.prepareStatement("DELETE FROM `inv_slots` WHERE `server` = ? AND `player` = ?");
 				statclear.setString(1, SQLWrapper.plugin.getConfig().getString("general.server.name"));
 				statclear.setString(2, p.getName());
 				statclear.execute();
 			}
 		} catch(SQLException e) { e.printStackTrace(); }
 	}
 	
 	public static String checkLatest(Player p) {
 		try {
 			reconnect();
 			PreparedStatement stat = con.prepareStatement("SELECT `server` FROM `inv_inventories` WHERE `player` = ? ORDER BY `timestamp` DESC LIMIT 1");
 			stat.setString(1, p.getName());
 			stat.execute();
 			ResultSet set = stat.getResultSet();
 			set.last();
 			if(set.getRow() == 0)
 				return "";
 			
 			return set.getString("server");
 		} catch(SQLException e) { e.printStackTrace(); return SQLWrapper.plugin.getConfig().getString("general.server.name"); }
 	}
 
 	public static void recoverLatest(Player p, String server) {
 		try {
 			reconnect();
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `inv_slots` WHERE `server` = ? AND `player` = ?");
 			stat.setString(1, server);
 			stat.setString(2, p.getName());
 			
 			stat.execute();
 			
 			ResultSet result = stat.getResultSet();
 			
 			while(result.next()) {
 				int slot = result.getInt("slot");
 				JSONParser parser = new JSONParser();
 				try {
 					Map<String, Object> map = (Map<String, Object>) parser.parse(result.getString("json"));
 					ItemStack stack = (ItemStack) SerializableInventory.deserialize(map);
 					
 					if(slot == Slots.HELMET.slotNum()) {
 						p.getInventory().setHelmet(stack);
 					} else if(slot == Slots.CHEST.slotNum()) {
 						p.getInventory().setChestplate(stack);
 					} else if(slot == Slots.LEGGINGS.slotNum()) {
 						p.getInventory().setLeggings(stack);
 					} else if(slot == Slots.BOOTS.slotNum()) {
 						p.getInventory().setBoots(stack);
 					} else {
 						p.getInventory().setItem(slot, stack);
 					}
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch(SQLException e) { e.printStackTrace(); }
 	}
 }
