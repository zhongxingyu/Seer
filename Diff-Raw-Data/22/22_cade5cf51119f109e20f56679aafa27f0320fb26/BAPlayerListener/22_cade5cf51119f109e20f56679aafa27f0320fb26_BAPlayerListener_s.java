 package bitlegend.bitauth.listeners;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 
 import bitlegend.bitauth.BitAuth;
 import bitlegend.bitauth.timers.Login;
 
 public class BAPlayerListener implements Listener {
 	private BitAuth instance;
 	
 	// Database info
 	private String user = "";
 	private String pass = "";
 	private String url = "";
 	private String logintable = "";
 	private String wltable = "";
 	private int logintimelimit = 0;
 	
 	public BAPlayerListener(BitAuth instance) {
 		this.instance = instance;
 		user = instance.config.readString("DB_User");
 		pass = instance.config.readString("DB_Pass");
 		url = "jdbc:mysql://" + instance.config.readString("DB_Host") + 
 				"/" + instance.config.readString("DB_Name");
 		logintable = instance.config.readString("DB_Table_login");
 		wltable = instance.config.readString("DB_Table_whitelist");
 		logintimelimit = Integer.parseInt(instance.config.readString("Login_Time_Limit"));
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		boolean whitelisted = false; // Will be false if player doesn't exist in database
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery(
 					"SELECT * FROM `" + wltable + "`");
 			while (result.next()) {
 				String user = result.getString(1);
 				boolean allowed = (result.getInt(2) == 1 ? true : false);
 				if (user.equalsIgnoreCase(event.getPlayer().getName())) // Player found
 					if (allowed == true)
 						whitelisted = true;
 			}
 			result.close();
 			select.close();
 			conn.close();
 		} catch (SQLException sq) {
 			sq.printStackTrace();
 		}
 
 		if (whitelisted == false && instance.config.readBoolean("Use_Whitelist") == true)
 			event.disallow(null, "You are not whitelisted.");
 		else {
 			try {
 				Player player = event.getPlayer();
 				Connection conn = DriverManager.getConnection(url, user, pass);
 				Statement select = conn.createStatement();
 				ResultSet result = select.executeQuery(
 						"SELECT * FROM `" + logintable + "`");
 				if (result.next()) { // Results found
 					boolean playerFound = false;
 					do {
 						String username = result.getString(1);
 						boolean pwreset = result.getInt(8) == 1 ? true : false;
 						if (username.equalsIgnoreCase(player.getName())) { // Player
 																		// data
 																		// found
 							playerFound = true;
 							long lastlogintest = (System.currentTimeMillis() / 1000L) - 1800;
 							long lastlogintime = result.getLong(5);
 							boolean playerLoggedIn = false;
 	
 							if (lastlogintime < lastlogintest) {
 								instance.requireLogin.add(player);
 								playerLoggedIn = false;
 							} else {
								playerLoggedIn = true;
 							}
 							
 							if (pwreset == true) // Password reset
 								instance.pwreset.add(player);
							
							if (playerLoggedIn == true) {
 								boolean alreadyInList = false;
 								boolean alreadyInList2 = false;
 								int index = 0;
 								for (Player p : instance.loggedIn) {
 									if (p.getName().equals(player.getName()))
 										alreadyInList = true;
 								}
 								for (Player p : instance.requireLogin) {
 									if (p.getName().equals(player.getName())) {
 										alreadyInList2 = true;
 										index = instance.requireLogin.indexOf(p);
 									}
 								}
 								if (alreadyInList == false)
 									instance.loggedIn.add(player);
 								if (alreadyInList2 == true)
 									instance.requireLogin.remove(index);
 								player.sendMessage(ChatColor.GREEN +
 										"Welcome back, " + player.getName());
 							}
 						}
 					} while (result.next());
 					if (playerFound == false) { // Player data not found
 						instance.unregistered.add(player);
 					}
 				}
 				else { // No records found, create new for this player
 					instance.unregistered.add(player);
 				}
 				result.close();
 				select.close();
 				conn.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				p.sendMessage(new String[] { 
 						ChatColor.GREEN + "Oh no! It appears you haven't registered for our server.", 
 						ChatColor.GREEN + "Not to fret though, you can register by simply typing:",
 						ChatColor.GREEN + "/register <password>" 
 				});
 				Login kick = new Login(p, logintimelimit, instance);
 			}
 		}
 		boolean pwreset = false;
 		for (Player p : instance.pwreset) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				p.sendMessage(new String[] { 
 						ChatColor.RED + "You need to reset your password! You can do this by typing:",
 						ChatColor.RED + "/login <temp password> <new password>"
 				});
 				pwreset = true;
 				Login kick = new Login(p, logintimelimit, instance);
 			}
 		}
 		if (pwreset == false) {
 			for (Player p : instance.requireLogin) {
 				if (p.getName().equals(event.getPlayer().getName())) {
 					p.sendMessage(new String[] { 
 							ChatColor.GREEN + "Please log in by typing:",
 							ChatColor.GREEN + "/login <password>" });
 					Login kick = new Login(p, logintimelimit, instance);
 				}
 			}
 		}
 		for (Player p : instance.loggedIn) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				p.sendMessage(ChatColor.GREEN + 
 						"Welcome back, " + event.getPlayer().getName());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		int[] index = new int[instance.unregistered.size()], 
 				index2 = new int[instance.requireLogin.size()], 
 				index3 = new int[instance.loggedIn.size()];
 		Player player = event.getPlayer();
 		
 		for (int ti = 0; ti < index.length; ti++)
 			index[ti] = -1;
 		for (int ti = 0; ti < index2.length; ti++)
 			index2[ti] = -1;
 		for (int ti = 0; ti < index3.length; ti++)
 			index3[ti] = -1;
 		
 		int i = 0, j = 0, k = 0;
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(player.getName())) {
 				index[i] = instance.unregistered.indexOf(p);
 				i++;
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(player.getName())) {
 				index2[j] = instance.requireLogin.indexOf(p);
 				j++;
 			}
 		}
 		for (Player p : instance.loggedIn) {
 			if (p.getName().equals(player.getName())) {
 				index3[k] = instance.loggedIn.indexOf(p);
 				k++;
 			}
 		}
 		
 		boolean remove = false, remove2 = false, remove3 = false;
 		int iii = 0, jjj = 0, kkk = 0;
 		int rindex = 0, rindex2 = 0, rindex3 = 0;
 		
 		while (iii < index.length) {
 			if (index[iii] > -1) {
 				remove = true;
 				rindex = iii;
 			}
 			iii++;
 		}
 		while (jjj < index2.length) {
 			if (index2[jjj] > -1) {
 				remove2 = true;
 				rindex2 = jjj;
 			}
 			jjj++;
 		}
 		while (kkk < index3.length) {
 			if (index3[kkk] > -1) {
 				remove3 = true;
 				rindex3 = kkk;
 			}
 			kkk++;
 		}
 		
 		if (remove == true)
 			instance.unregistered.remove(rindex);
 		if (remove2 == true)
 			instance.requireLogin.remove(rindex2);
 		if (remove3 == true)
 			instance.loggedIn.remove(rindex3);
 	}
 	
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		String[] split = event.getMessage().split(" ");
 		boolean allow = false;
 		
 		if (split[0].equals("/register"))
 			allow = true;
 		if (split[0].equals("/login"))
 			allow = true;
 		
 		if (allow == false) {
 			for (Player p : instance.unregistered) {
 				if (p.getName().equals(event.getPlayer().getName())) {
 					event.setCancelled(true);
 				}
 			}
 			for (Player p : instance.requireLogin) {
 				if (p.getName().equals(event.getPlayer().getName())) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerExpChangeEvent(PlayerExpChangeEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setAmount(0);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setAmount(0);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
 		for (Player p : instance.unregistered) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 		for (Player p : instance.requireLogin) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	
 }
