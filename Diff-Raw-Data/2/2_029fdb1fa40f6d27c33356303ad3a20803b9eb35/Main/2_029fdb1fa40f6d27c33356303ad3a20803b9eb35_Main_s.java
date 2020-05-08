 package net.stuffyserv.simplechatrooms;
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public final class Main extends JavaPlugin implements Listener {
 	
 	ArrayList<Object[]> rooms = new ArrayList<Object[]>();
 	Connection conn;
 	
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(this, this);
 		createSqLiteDb();
 		loadRooms();
 	}
 	
 	public void onDisable() {
 		try {
 			if (conn != null) {
 				conn.close();
 			}
 		} catch (SQLException e) {
 			// TODO: Add error handling
 		}
 	}
 	
 	public void createSqLiteDb() {
 	    try {
 	    	
 	    	File theDir = new File("plugins/SimpleChatrooms");
 	    	
 	    	if (!theDir.exists()) {
 	    		theDir.mkdir();
 	    	}
 	    	
 			File f = new File("plugins/SimpleChatrooms/simplechatrooms.db");
 			
 			if (!f.exists()) {
 				Class.forName("org.sqlite.JDBC");
 			    conn = DriverManager.getConnection("jdbc:sqlite:plugins/SimpleChatrooms/simplechatrooms.db");
 			    Statement stat = conn.createStatement();
 			    stat.executeUpdate("create table rooms (roomname, password, maxmembers);");
 			}
 			else {
 				conn = DriverManager.getConnection("jdbc:sqlite:plugins/SimpleChatrooms/simplechatrooms.db");
 			}
 	    }
 	    catch (SQLException e) {
 	    	getLogger().info(e.getMessage());
 			// TODO Auto-generated catch block
 	    } catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean saveRooms() {
 		try {
 
 		    Statement stat = conn.createStatement();
 		    stat.executeUpdate("DELETE FROM rooms;");
 			
 			PreparedStatement prep = conn.prepareStatement("insert into rooms values (?, ?, ?);");
 			
 			for (int i = 0; i < rooms.size(); i++) {
 				Object[] subarray = rooms.get(i);
 				String roomname = (String) subarray[0];
 				String password = (String) subarray[1];
 				int maxmembers = (int) subarray[3];
 				
 				prep.setString(1, roomname);
 				prep.setString(2, password);
 				prep.setInt(3, maxmembers);
 				
 				prep.addBatch();
 			}
 			
 		    conn.setAutoCommit(false);
 		    prep.executeBatch();
 		    conn.setAutoCommit(true);
 		    return true;
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			return false;
 		}
 	}
 	
 	public boolean loadRooms() {
 		try {
 			Statement stat = conn.createStatement();
 			ResultSet rs = stat.executeQuery("select * from rooms;");
 				while (rs.next())
 				{
 					String roomname = rs.getString("roomname");
 					String password = rs.getString("password");
 					int maxmembers = rs.getInt("maxmembers");
 					
 					createRoom(roomname, password, maxmembers);
 					
 				}
 			rs.close();
 			return true;
 		}
 		catch (SQLException e) {
 			return false;
 		}
 	}
 	
 	public void loadConfig() {
 	}
 	
 	public void createRoom(String name, String password, int maxmembers) {
 		Object[] roomprops = new Object[4];
 		
 		ArrayList<String> members = new ArrayList<String>();
 		
 		roomprops[0] = name;
 		roomprops[1] = password;
 		roomprops[2] = members;
 		roomprops[3] = maxmembers;
 		
 		rooms.add(roomprops);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if (cmd.getName().equalsIgnoreCase("createroom")) {
 			if (sender instanceof Player) {
 				if (sender.hasPermission("simplechatrooms.create")) {
 					
 					if (args.length > 3) {
 						sender.sendMessage("Zu viele Argumente angegeben.");
 						return false;
 					}
 					else if (args.length < 3) {
 						return false;
 					}
 					
 					try {
 						createRoom(args[0], args[1], Integer.parseInt(args[2]));
 					}
 					catch (NumberFormatException nfx) {
 						sender.sendMessage("Fehlerhafte Eingabe.");
 						return false;
 					}
 					
 					sender.sendMessage("Raum mit dem Namen: " + ChatColor.GREEN + args[0] + ChatColor.WHITE + " und dem Passwort: " + ChatColor.GREEN + args[1] + ChatColor.WHITE + " hinzugefgt");
 					
 					return true;
 					
 				}
 				else {
 					return false;
 				}
 			}
 			else {
 				sender.sendMessage("Es knnen keine Chatrooms von der Konsole aus erstellt werden.");
 				return true;
 			}
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("saverooms")) {
 			if (rooms.size() == 0) {
 				return false;
 			}
 			saveRooms();
 			sender.sendMessage("Rume gespeichert!");
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("deleteroom")) {
 			
 			if (rooms.size() == 0) {
 				return false;
 			}
 			for (int i = 0; i < rooms.size(); i++) {
 				Object[] subarray = rooms.get(i);
 				
 				@SuppressWarnings("unchecked")
 				ArrayList<Player> roommembers = (ArrayList<Player>) subarray[2];
 				
 				if (subarray[0].equals(args[0])) {
 					rooms.remove(i);
 					sender.sendMessage("Raum " + ChatColor.RED + ((String) subarray[0]) + ChatColor.WHITE + " gelscht.");
 					for (Player p : roommembers) {
 						p.sendMessage("Dein Raum wurde gelscht!");
 					}
 					return true;
 				}
 			}
 			sender.sendMessage("Raum:" + ChatColor.RED + args[0] + ChatColor.WHITE + " wurde nicht gefunden.");
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("joinroom")) {
 			if (sender instanceof Player) {
 				if (rooms.size() > 0) {
 					if (args.length == 2) {
 						if (getRoom((Player) sender) == "NONE") {
 							for (int i = 0; i < rooms.size(); i++) {
 								Object[] subarray = rooms.get(i);
 								String roomname = (String) subarray[0];
 								String password = (String) subarray[1];
 								int maxmembers = (int) subarray[3];
 								if (args[0].equals(roomname) && args[1].equals(password)) {
 									@SuppressWarnings("unchecked")
 									ArrayList<Player> playerlist = (ArrayList<Player>) subarray[2];
 									
 									if (playerlist.size() == maxmembers && !sender.isOp()) {
 										sender.sendMessage("Raum ist voll");
 										return true;
 									}
 									
 									for (Player p : playerlist) {
 										p.sendMessage("Player " + ChatColor.GREEN + sender.getName() + ChatColor.WHITE + " ist dem Raum beigetreten.");
 									}
 									playerlist.add((Player) sender);
 									
 									subarray[2] = (Object) playerlist;
 									rooms.set(i, subarray);
 									
 									sender.sendMessage("Du bist dem Raum " + ChatColor.RED + roomname + ChatColor.WHITE + " beigetreten.");
 									
 									return true;
 								}
 							}
 							sender.sendMessage("Falsches Passwort oder Raum nicht gefunden.");
 							return true;
 						}
 						else {
 							sender.sendMessage("Du bist bereits im Raum " + ChatColor.RED + getRoom((Player) sender) + ChatColor.WHITE + ".");
 							return true;
 						}
 					}
 					else {
 						sender.sendMessage("Not all args");
 						return false;
 					}
 				}
 			}
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("showrooms")) {
 			if (rooms.size() > 0) {
 				for (Object[] subarray : rooms) {
 					@SuppressWarnings("unchecked")
 					int membercount = ((ArrayList<Player>) subarray[2]).size();
 					int maxmembers = (int) subarray[3];
 					sender.sendMessage("Raumname: " + ChatColor.RED + subarray[0].toString() + ChatColor.WHITE + " (" + ChatColor.GREEN + membercount + ChatColor.WHITE + "/" + ChatColor.GREEN + maxmembers + ChatColor.WHITE + ")");
 				}
 				return true;
 			}
 			else {
 				sender.sendMessage("Keine Rume gefunden");
 				return true;
 			}
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("moveplayer")) {
 			
 			if (args.length < 1) {
 				return false;
 			}
 			
 			Player target = (Bukkit.getServer().getPlayer(args[0]));
 			boolean check = false;
 			boolean check2 = false;
 			
 			if (target != null) {
 				if (rooms.size() > 0) {
 					if (args.length == 2) {
 						String roomname = "";
 						if (args[1].equals("#") && getRoom(target) != "NONE") {
 							roomname = getRoom(target);
 						}
 						else {
 							roomname = args[1];
 						}
 						for (int i = 0; i < rooms.size(); i++) {
 							Object[] subarray = rooms.get(i);
 							
 							@SuppressWarnings("unchecked")
 							ArrayList<Player> playerlist = (ArrayList<Player>) subarray[2];
 							
 							if (getRoom(target) != "NONE") {
 								if (playerlist.contains(target)) {
 									playerlist.remove(target);
 									
 									subarray[2] = (Object) playerlist;
 									rooms.set(i, subarray);
 									
 									for (Player p : playerlist) {
 										p.sendMessage("Player " + ChatColor.GREEN + target.getName() + ChatColor.WHITE + " wurde aus dem Raum bewegt.");
 									}
 									
 									check = true;
 								}
 							}
 							else {
 								check = true;
 							}
 							
 							if (((String) subarray[0]).equals(roomname)) {
 								
 								for (Player p : playerlist) {
 									p.sendMessage("Player " + ChatColor.GREEN + target.getName() + ChatColor.WHITE + " wurde in den Raum bewegt.");
 								}
 								playerlist.add(target);
 								
 								subarray[2] = (Object) playerlist;
 								rooms.set(i, subarray);
 								check2 = true;
 							}
 						}
 						
 						if (check && check2 || getRoom(target) == "NONE" && check2) {
 							target.sendMessage("Du wurdest in den Raum " + ChatColor.RED + roomname + ChatColor.WHITE + " bewegt.");
 							sender.sendMessage("Player " + ChatColor.GREEN + target.getName() + ChatColor.WHITE + " wurde in den Raum " + ChatColor.RED + roomname + ChatColor.WHITE + " bewegt.");
 							return true;
 						}
 						else {
 							sender.sendMessage("Bewegen fehlgeschlagen.");
 							return false;
 						}
 					}
 					else {
 						return false;
 					}
 		        }
 			}
 		    else {
 	        	sender.sendMessage("Spieler " + ChatColor.RED + args[0] + ChatColor.WHITE + " wurde nicht gefunden.");
 	        	return true;
 		    }
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("showmembers")) {
 			if (args.length > 0) {
 				String roomname = "";
 				for (Object[] subarray : rooms) {
 					if (args[0].equals("#") && getRoom((Player) sender) != "NONE") {
 						roomname = getRoom((Player) sender);
 					}
 					else if (getRoom((Player) sender) != "NONE") {
 						roomname = args[0];
 					}
 					else {
 						sender.sendMessage("Du bist in keinem Raum.");
 						return true;
 					}
 					
 					if (((String) subarray[0]).equals(roomname)) {
 						@SuppressWarnings("unchecked")
 						int membercount = ((ArrayList<Player>) subarray[2]).size();
 						int maxmembers = (int) subarray[3];
 						@SuppressWarnings("unchecked")
 						ArrayList<Player> playerlist = (ArrayList<Player>) subarray[2];
 						sender.sendMessage(ChatColor.YELLOW + "---------------------------------------");
 						sender.sendMessage("Member im Raum " + ChatColor.RED + roomname + ChatColor.WHITE + " (" + ChatColor.GREEN + membercount + ChatColor.WHITE + "/" + ChatColor.GREEN + maxmembers + ChatColor.WHITE + ")");
 						sender.sendMessage(ChatColor.YELLOW + "---------------------------------------");
 						for (Player p : playerlist) {
 							sender.sendMessage(ChatColor.GREEN + p.getName());
 						}
 						return true;
 					}
 				}
 				sender.sendMessage("Raum " + ChatColor.RED + args[0] + ChatColor.WHITE + " wurde nicht gefunden.");
 				return true;
 			}
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("leaveroom")) {
 			if (sender instanceof Player) {
 				if (rooms.size() > 0) {
 					for (int i = 0; i < rooms.size(); i++) {
 						Object[] subarray = rooms.get(i);
 						
 						@SuppressWarnings("unchecked")
 						ArrayList<Player> playerlist = (ArrayList<Player>) subarray[2];
 						
 						if (playerlist.contains((Player) sender)) {
 							playerlist.remove((Player) sender);
 							
 							subarray[2] = (Object) playerlist;
 							rooms.set(i, subarray);
 							
 							sender.sendMessage("Du hast den Raum " + ChatColor.RED + ((String) subarray[0]) + ChatColor.WHITE + " verlassen.");
 							
 							return true;
 						}
 					}
 				}
 			}
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("kickplayer")) {
 			if (rooms.size() > 0) {
 				if (args.length > 0) {
 					for (int i = 0; i < rooms.size(); i++) {
 						Object[] subarray = rooms.get(i);
 						
 						@SuppressWarnings("unchecked")
 						ArrayList<Player> playerlist = (ArrayList<Player>) subarray[2];
 						
 						Player target = (Bukkit.getServer().getPlayer(args[0]));
 				        if (target != null) {
 							if (playerlist.contains(target)) {
 								playerlist.remove(target);
 								
 								subarray[2] = (Object) playerlist;
 								rooms.set(i, subarray);
 								
 								sender.sendMessage("Player " + ChatColor.GREEN + target.getName() + ChatColor.WHITE + " aus Raum " + ChatColor.RED + subarray[0].toString() + ChatColor.WHITE + " gekickt.");
 								target.sendMessage("Du wurdest aus dem Raum " + ChatColor.RED + subarray[0].toString() + ChatColor.WHITE + " gekickt");
 								
 								return true;
 							}
 				        }
 				        else {
 				        	sender.sendMessage("Spieler " + ChatColor.RED + args[0] + ChatColor.WHITE + " wurde nicht gefunden.");
 				        	return true;
 				        }
 					}
 				}
 				else {
 					return false;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public String getRoom(Player pl) {
 		if (rooms.size() >= 0) {
 			for (Object obj : rooms) {
 				Object[] subarray = (Object[]) obj;
 				if (((ArrayList<Player>) subarray[2]).contains(pl)) {
 					return (String) subarray[0];
 				}
 			}
 		}
 		return "NONE";
 	}
 	
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent e) {
     	
     	Player sender = e.getPlayer();
     	
     	for (Player p : Bukkit.getOnlinePlayers()) {
     		String playersroom = getRoom(sender);
     		if (!getRoom(p).equals(playersroom)) {
     			e.getRecipients().remove(p);
     		}
     		
     		if (p.isOp()) {
     			if (e.getMessage().contains(p.getName())) {
     				if (!getRoom(p).equals(getRoom(sender))) {
    					p.sendMessage(ChatColor.BLUE + "[" + getRoom(p.getPlayer()) + "]" + ChatColor.WHITE + e.getMessage());
     				}
     			}
     		}
     	}
     	
     	if (!getRoom(sender).equals("NONE")) {
     		e.setMessage(ChatColor.RED + "[" + getRoom(sender) + "] " + ChatColor.WHITE + e.getMessage());
     	}
     }
 	
 }
