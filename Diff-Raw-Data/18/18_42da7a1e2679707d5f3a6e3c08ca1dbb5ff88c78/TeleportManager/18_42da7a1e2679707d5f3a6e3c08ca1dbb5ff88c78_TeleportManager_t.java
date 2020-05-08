 /*
 	Copyright (C) 2011 by NetherLink
 	
 	Permission is hereby granted, free of charge, to any person obtaining a copy
 	of this software and associated documentation files (the "Software"), to deal
 	in the Software without restriction, including without limitation the rights
 	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the Software is
 	furnished to do so, subject to the following conditions:
 	
 	The above copyright notice and this permission notice shall be included in
 	all copies or substantial portions of the Software.
 	
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 	THE SOFTWARE.
 */
 
 package netherlink.minecraft.netherpoints;
 
 import java.sql.*;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.*;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class TeleportManager{
 	
 	/*
 	 * Variables
 	 */
 	
 	private HashMap<Block, Waypoint> waypointList; // The list of waypoints.
 	private netherpoints plugin; // The plugin.
 	
 	/*
 	 * Constructor / Useful Functions
 	 */
 	
 	public TeleportManager(netherpoints pluginx){
 		plugin = pluginx;
 		waypointList = new HashMap<Block, Waypoint>();
 		load(); // Load the waypoints from the database.
 	}
 	
 	public void Finalize(){
 		save(); // Save the waypoints to the database.
 		waypointList.clear();
 	}
 	
 	public HashMap<Block, Waypoint> getList(){ return waypointList;}
 	
 	/*
 	 * Load / Save
 	 */
 	
 	public void checkTable(){
 		if(!plugin.isSqlEnabled()) // Make sure the database is enabled first.
 			return;
 		if(!plugin.doesTableExist("waypoints"))
 			plugin.executeUpdate("CREATE TABLE `waypoints` ("
 				+ "`id` INTEGER PRIMARY KEY,"
 				+ "`name` varchar(32) NOT NULL DEFAULT '0',"
 				+ "`network` varchar(32) NOT NULL DEFAULT '0',"
 				+ "`world` varchar(32) NOT NULL DEFAULT '0',"
 				+ "`x` INTEGER NOT NULL DEFAULT '0',"
 				+ "`y` INTEGER NOT NULL DEFAULT '0',"
 				+ "`z` INTEGER NOT NULL DEFAULT '0',"
 				+ "`material` INTEGER NOT NULL DEFAULT '0'"
 				+ ")");
 	}
 	
 	public void load(){
 		plugin.enableSql("waypoints.db");
 		checkTable(); // Make sure the table exists.
 		ResultSet resultSet = plugin.executeQuery("SELECT * FROM 'waypoints'");
 		try {
 			while(resultSet.next()){
 				Location l = new Location(plugin.getServer().getWorld(resultSet.getString("world")), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));
 				Waypoint w = new Waypoint(resultSet.getString("name"), resultSet.getString("network"), l, Material.getMaterial(resultSet.getInt("material")));
 				if(w.verify()) // Make sure that the waypoint is verified before adding it to the list.
 					waypointList.put(w.getBlock(), w);
 			}
 			resultSet.close();
 		} catch (SQLException e) {
 			plugin.warning("SQLite Error (" + e.getErrorCode() + ": " + e.getMessage() + ")");
 		}
 		plugin.disableSql();
 	}
 	
 	public void save(){
 		plugin.enableSql("waypoints.db");
 		checkTable(); // Make sure the table exists.
 		plugin.executeUpdate("DELETE FROM 'waypoints';"); // Empty the current database.
 		if(waypointList.size() == 0){ // If there are no waypoints do not continue.
 			plugin.disableSql();
 			return;
 		}
 		PreparedStatement p = plugin.createPreparedStatement("INSERT INTO 'waypoints' ('name', 'network', 'world', 'x', 'y', 'z', 'material') VALUES (?, ?, ?, ?, ?, ?, ?);");
 		for(Block b : waypointList.keySet()){
 			Waypoint w = waypointList.get(b);
 			if(w.verify()){
 				try{
 					p.setString(1, w.getName());
 					p.setString(2, w.getNetwork());
 					p.setString(3, w.getLocation().getWorld().getName());
 					p.setInt(4, w.getLocation().getBlockX());
 					p.setInt(5, w.getLocation().getBlockY());
 					p.setInt(6, w.getLocation().getBlockZ());
 					p.setInt(7, w.getKey().getId());
 					p.addBatch();
 				} catch(SQLException e){
 					plugin.warning("SQLite Error (" + e.getErrorCode() + ": " + e.getMessage() + ")");
 				}
 			}
 		}
 		try{
 			p.executeBatch();
 		} catch(SQLException e){
 			System.out.println("SQLite Error: " + e.getErrorCode() + " : " + e.getMessage());
 		}
 		plugin.disableSql();
 	}
 	
 	/*
 	 * Waypoint / Teleport
 	 */
 	
 	public void teleport(Player p, Block b, String network, String destination, String parameters){
 		// Variables for important stuff.
 		boolean leaveSign = false;
		boolean leaveKey = false;
 		String dest = destination.replaceAll("[^a-zA-Z0-9\\s]", "");
 		String net = network.replaceAll("[^a-zA-Z0-9\\s]", "");
 		String paramsx = parameters.replaceAll("[^a-zA-Z0-9\\s]", "");
 		
 		// Verify stuff.
 		if(destination.equalsIgnoreCase("")){ // If blank don't do anything.
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "You need to provide a destination.");
 			return;
 		}
 		if(b.getRelative(-1, 0, 0).getType() != Material.OBSIDIAN || 
 			b.getRelative(1, 0, 0).getType() != Material.OBSIDIAN || 
 			b.getRelative(0, 0, 1).getType() != Material.OBSIDIAN || 
 			b.getRelative(0, 0, -1).getType() != Material.OBSIDIAN){
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "Incorrect teleporter pattern.");
 			return;
 		}
 		
 		// Parse the parameters.
 		String[] params = paramsx.split(" "); // Get the parameters.
 		for(int i = 0; i < params.length; i++){ // Iterate through the split strings.
 			if(params[i].equalsIgnoreCase("LS")) // LS leaves the sign.
 				leaveSign = true;
 			if(params[i].equalsIgnoreCase("LK")) // LK leaves the key.
 				leaveKey = true;
 		}
 		
 		// Get the Waypoint
 		Waypoint w = null;
 		for(Block block : waypointList.keySet()){
 			Waypoint way = waypointList.get(block);
 			if(way.getName().equalsIgnoreCase(dest) && way.getNetwork().equalsIgnoreCase(net)){
 				w = way;
 			}
 		}
 		if(w == null){ // If no waypoint was found.
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "No waypoint with that name was found.");
 			b.getRelative(0, 1, 0).setTypeId(0); // Replace the sign with air.
 			p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.SIGN, 1)); // Give the user their sign back.
 			if(b.getType() != Material.AIR) // Don't give them air though.
 				p.getWorld().dropItem(p.getLocation(), new ItemStack(b.getType(), 1)); // Give the user their item back.
			b.setTypeId(0); // Replace the sign with air.
 			return;
 		}
 		
 		// Compare the keys.
 		if(w.getKey() != b.getType()){ // If keys do not match.
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "You have an incorrect key.");
 			if(!leaveSign){ // Do not leave the sign.
 				b.getRelative(0, 1, 0).setTypeId(0); // Replace the sign with air.
 				p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.SIGN, 1)); // Give the user their sign back.
 			}
 			if(!leaveKey){ // Do not leave the key.
 				if(b.getType() != Material.AIR)
 					p.getWorld().dropItem(p.getLocation(), new ItemStack(b.getType(), 1)); // Give the user their key back.
				b.setTypeId(0); // Replace the key with air.
 			}
 			return;
 		}
 		
 		// Verify Teleporter
 		if(!w.verify()){
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint was obliterated by a giant zombie.");
 			waypointList.remove(w.getBlock());
 			b.getRelative(0, 1, 0).setTypeId(0); // Replace the sign with air.
 			p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.SIGN, 1)); // Give the user their sign back.
 			if(b.getType() != Material.AIR)
 				p.getWorld().dropItem(p.getLocation(), new ItemStack(b.getType(), 1)); // Give the user their key back.
			b.setTypeId(0); // Replace the key with air.
 			return;
 		}
 		
 		// Attempt to teleport.
 		w.teleport(p);
 		if(!leaveSign){ // Do not leave the sign.
 			b.getRelative(0, 1, 0).setTypeId(0); // Replace the sign with air.
 			p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.SIGN, 1)); // Give the user their sign back.
 		}
 		if(!leaveKey){ // Do not leave the key.
 			if(b.getType() != Material.AIR)
 				p.getWorld().dropItem(p.getLocation(), new ItemStack(b.getType(), 1)); // Give the user their key back.
			b.setTypeId(0); // Replace the key with air.
 		}
 		
 	}
 	
 	public void waypoint(Player p, Block b, String network, String name){
 		// Variables for important stuff.
 		String dest = name.replaceAll("[^a-zA-Z0-9\\s]", "");
 		String net = network.replaceAll("[^a-zA-Z0-9\\s]", "");
 		
 		// Verify stuff.
 		if(dest.equalsIgnoreCase("")){ // If blank don't do anything.
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "You need to provide a waypoint name!.");
 			return;
 		}
 		if(b.getRelative(-1, 0, 0).getType() != Material.OBSIDIAN || 
 			b.getRelative(1, 0, 0).getType() != Material.OBSIDIAN || 
 			b.getRelative(0, 0, 1).getType() != Material.OBSIDIAN || 
 			b.getRelative(0, 0, -1).getType() != Material.OBSIDIAN){
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "Incorrect teleporter pattern.");
 			return;
 		}
 		
 		// Check for already existing waypoint.
 		if(waypointList.containsKey(b)){
 			if(waypointList.get(b).verify()){
 				p.sendMessage(ChatColor.LIGHT_PURPLE + "There is already a waypoint in this location.");
 				return;
 			}
 			waypointList.remove(b); // If the waypoint isn't verified, remove it.
 		}
 		for(Block block : waypointList.keySet()){
 			Waypoint way = waypointList.get(block);
 			if(way.getName().equalsIgnoreCase(dest) && way.getNetwork().equalsIgnoreCase(net)){
 				if(way.verify()){
 					p.sendMessage(ChatColor.LIGHT_PURPLE + "The provided waypoint name is already in use.");
 					return;
 				}
 				waypointList.remove(b); // If the waypoint isn't verified, remove it.
 			}
 		}
 		
 		// Create the waypoint.
 		Waypoint w = new Waypoint(dest, net, b.getLocation(), b.getType());
 		waypointList.put(w.getBlock(), w);
 		w.printInfo(p);
 		b.setTypeId(20);
 		b.getRelative(0, 1, 0).setTypeId(0);
 		p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.SIGN, 1));
 		if(w.getKey() != Material.AIR)
 			p.getWorld().dropItem(p.getLocation(), new ItemStack(w.getKey(), 1));
 		return;
 		
 	}
 
 }
