 package com.dechiridas.bitauth.util;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ConcurrentModificationException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 import com.dechiridas.bitauth.BitAuth;
 import com.dechiridas.bitauth.player.BAPlayer;
 import com.dechiridas.bitauth.player.BAState;
 
 public class Database {
 	private BitAuth plugin;
 	
 	// Database info
 	private String user = ""; // db username
 	private String pass = ""; // db password
 	private String url = ""; // db url
 	private String login = ""; // db login table
 	private String wlist = ""; // db whitelist table
 	private boolean whitelist = false;
 	
 	public Database(BitAuth instance) {
 		this.plugin = instance;
 		
 		user = plugin.config.readString("DB_User");
 		pass = plugin.config.readString("DB_Pass");
 		login = plugin.config.readString("DB_Table_login");
 		wlist = plugin.config.readString("DB_Table_whitelist");
 		url = "jdbc:mysql://" + plugin.config.readString("DB_Host") + 
 				"/" + plugin.config.readString("DB_Name");
 		
 		whitelist = plugin.config.readBoolean("Use_Whitelist");
 	}
 	
 	public boolean whitelistEnabled() {
 		return whitelist;
 	}
 	
 	public boolean isWhitelisted(Player player) {
 		boolean r = false;
 		
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery(
 					"SELECT * FROM `" + wlist + "`");
 			while (result.next()) {
 				String user = result.getString(1);
 				boolean allowed = (result.getInt(2) == 1 ? true : false);
 				if (user.equalsIgnoreCase(player.getName())) // Player found
 					if (allowed == true)
 						r = true;
 			}
 			result.close();
 			select.close();
 			conn.close();
 		} catch (SQLException sq) {
 			sq.printStackTrace();
 		}
 		
 		return r;
 	}
 	
 	public void tryLogin(Player player, PlayerLoginEvent event) {
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery(
 					"SELECT * FROM `" + login + "`");
 			
 			BAPlayer ba = new BAPlayer(plugin, player);
 			
 			if (result.next()) { // Results found
 				boolean playerFound = false;
 				do {
 					String username = result.getString(1);
 					boolean pwreset = result.getInt(8) == 1 ? true : false;
 					if (username.equalsIgnoreCase(player.getName())) { // Player found
 						playerFound = true;
 						long lastlogintest = (System.currentTimeMillis() / 1000L) - 1800;
 						long lastlogintime = result.getLong(5);
 						long playerIP = result.getLong(6);
						long currentIP = Utils.ipToLong(event.getAddress().getHostAddress());
 
 						if (lastlogintime < lastlogintest) {
 							//instance.requireLogin.add(player);
 							//playerLoggedIn = false;
 							
 							ba.setState(BAState.LOGGEDOUT);
 							
 						} else {
 							// Fix to prevent name spoofing from taking advantage
 							// of the 30 minute grace period
 							//System.out.println(currentIP + " ?= " + playerIP);
 							if (currentIP != playerIP) {
 								plugin.log.println("WARNING: Possible name-spoofer detected using name: "
 										+ player.getName());
 								//playerLoggedIn = false;
 								//instance.requireLogin.add(player);
 
 								ba.setState(BAState.LOGGEDOUT);
 							}
 							else {
 								//playerLoggedIn = true;
 								ba.setState(BAState.LOGGEDIN);
 							}
 						}
 						
 						if (pwreset == true) // Password reset
 							ba.setState(BAState.PWRESET);
 
 						if (ba.getState() == BAState.LOGGEDIN) {								
 							player.sendMessage(ChatColor.GREEN +
 									"Welcome back, " + player.getName());
 						}
 					}
 				} while (result.next());
 				
 				if (playerFound == false) { // Player data not found
 					ba.setState(BAState.UNREGISTERED);
 				}
 			}
 			else { // No records found, create new for this player
 				ba.setState(BAState.UNREGISTERED);
 			}
 			
 			plugin.pman.addPlayer(ba);
 			
 			result.close();
 			select.close();
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean tablesExist() {
 		boolean r = false, blogin = false, bwhitelist = false;
 		
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery("SHOW TABLES");
 			
 			if (result.next()) { // Results found
 				do {
 					String table = result.getString(1);
 					if (table.equals(login))
 						blogin = true;
 					if (table.equals(wlist))
 						bwhitelist = true;
 				} while (result.next());
 			}
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 		
 		if (blogin == true && bwhitelist == true)
 			r = true;
 		
 		return r;
 	}
 	
 	public void generateTables() {
 		String queryDropLogin = "DROP TABLE IF EXISTS `" + login + "`";
 		String queryCreateLogin = "CREATE TABLE `" + login + "` (" +
 				"`username` text NOT NULL," +
 				"`salt` blob NOT NULL," +
 				"`password` blob NOT NULL," +
 				"`whitelist` tinyint(1) NOT NULL," +
 				"`lastlogintime` bigint(20) NOT NULL," +
 				"`ipaddress` bigint(20) NOT NULL," +
 				"`enableipcheck` tinyint(1) NOT NULL," +
 				"`pwreset` tinyint(4) NOT NULL," +
 				"`temppwd` blob," +
 				"`tmpsalt` blob" +
 				") ENGINE=InnoDB DEFAULT CHARSET=utf8";
 		String queryDropWhitelist = "DROP TABLE IF EXISTS `" + wlist +"`";
 		String queryCreateWhitelist = "CREATE TABLE `" + wlist + "` (" +
 				"`username` text NOT NULL," +
 				"`enabled` tinyint(4) NOT NULL" +
 				") ENGINE=InnoDB DEFAULT CHARSET=utf8";
 		
 		try {
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			Statement query = conn.createStatement();
 			
 			query.executeUpdate(queryDropLogin);
 			query.executeUpdate(queryCreateLogin);
 			query.executeUpdate(queryDropWhitelist);
 			query.executeUpdate(queryCreateWhitelist);
 			
 			query.close();
 			conn.close();
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 	}
 
 	public void tryRegister(Player player, String password) {
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		try {
 			// Create connection
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			
 			// Check for pre-existing user by this name
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery(
 					"SELECT * FROM `" + login + "`");
 			
 			boolean playerFound = false;
 			
 			if (result.next()) { // Results found
 				do {
 					String username = result.getString(1);
 					if (username.equals(player.getName())) { // Player data found
 						player.sendMessage(ChatColor.GREEN
 								+ "Error: user by the name of "
 								+ player.getName()
 								+ " already found");
 						playerFound = true;
 					}
 				} while (result.next());
 			}
 			
 			if (playerFound == false) { // Player not found
 				// Generate new salt and password from salt and input
 				byte[] salt = HashManager.GenerateSalt();
 				byte[] hash = HashManager.GenerateHash(password, salt);
 
 				// Get unix time stamp
 				long unixtime = Utils.unixTimestamp();
 
 				// Get player IP as long
 				InetAddress iAddress = player.getAddress().getAddress();
 				long playerIP = Utils.ipToLong(iAddress.getHostAddress());
 
 				// Set up the query
 				String query = "INSERT INTO `" + login + "` "
 						+ "(`username`, `salt`, `password`, `whitelist`, "
 						+ "`lastlogintime`, `ipaddress`, `enableipcheck`, "
 						+ "`pwreset`, `temppwd`, `tmpsalt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
 				PreparedStatement statement = conn.prepareStatement(query);
 
 				// Set query values; 1 = user, 2 = salt, 3 = password hash,
 				// 4 = white list T/F, 5 = last login time, 6 = player ip,
 				// 7 = enable ip checking, 8 = pwreset T/F, 9 = temp pw,
 				// 10 = temp salt
 				statement.setString(1, player.getName());
 				statement.setBytes(2, salt);
 				statement.setBytes(3, hash);
 				statement.setBoolean(4, true);
 				statement.setLong(5, unixtime);
 				statement.setLong(6, playerIP);
 				statement.setBoolean(7, false);
 				statement.setBoolean(8, false);
 				statement.setBytes(9, null);
 				statement.setBytes(10, null);
 
 				// Execute the query
 				statement.executeUpdate();
 
 				// Close statement
 				statement.close();
 
 				// Remove player from unregistered list
 				//int index = 0;
 				//for (Player p : instance.unregistered) {
 				//	if (p.getName().equals(player.getName()))
 				//		index = instance.unregistered.indexOf(p);
 				//}
 				//instance.unregistered.remove(index);
 				ba.setState(BAState.REGISTERED);
 				
 				// Add player to the loggedIn list
 				//instance.loggedIn.add(player);
 				ba.setState(BAState.LOGGEDIN);
 
 				player.sendMessage(ChatColor.GREEN
 						+ "Thank you for registering with our server!");
 			}
 			// Close result and connection
 			result.close();
 			conn.close();
 			
 		} catch (ConcurrentModificationException cmx) {
 			cmx.printStackTrace();
 		} catch (UnsupportedEncodingException uex) {
 			uex.printStackTrace();
 		} catch (NoSuchAlgorithmException nsax) {
 			nsax.printStackTrace();
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 	}
 	
 	public void tryLoginManual(Player player, String[] input) {
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		try {
 			if (ba.getState() == BAState.LOGGEDOUT) {
 				// Create connection
 				Connection conn = DriverManager.getConnection(url, user, pass);
 				
 				Statement select = conn.createStatement();
 				ResultSet result = select.executeQuery(
 						"SELECT * FROM `" + login + "` WHERE username='" + player.getName() + "'");
 				if (result.next()) { // Results found
 					byte[] salt = result.getBytes(2);
 					byte[] hash = result.getBytes(3);
 					byte[] passwordCheck = HashManager.GenerateHash(input[0], salt);
 					boolean pwreset = result.getInt(8) == 1 ? true : false;
 					long playerIP = result.getLong(6);
 					boolean checkIP = result.getBoolean(7);
 					
 					// Get player IP
 					InetAddress iAddress = player.getAddress().getAddress();
 					long currentIP = Utils.ipToLong(iAddress.getHostAddress());
 					
 					// Check player IP if necessary
 					if ((!checkIP) || (checkIP && playerIP == currentIP)) { // IP addresses match
 						if (pwreset == true) {
 							hash = result.getBytes(9);
 							salt = result.getBytes(10);
 							passwordCheck = HashManager.GenerateHash(input[0], salt);
 							
 							if (input.length == 1) { // They didn't include the new password or don't know their password has been reset
 								player.sendMessage(ChatColor.YELLOW + "Reset your password: /login <temp passwd> <new passwd>");
 								player.sendMessage(ChatColor.YELLOW + "The temp password is provided by a mod or admin");
 							}
 							else {
 								if (Utils.byteToString(passwordCheck).equals(
 										Utils.byteToString(hash))) { // Password matches temp password
 									if (input.length == 2) { // Check to ensure newpass was entered too
 										player.sendMessage(ChatColor.GREEN + 
 												"Password reset properly, welcome back " + player.getName());
 										
 										// Get unix time stamp
 										long unixtime = Utils.unixTimestamp();
 										
 										// Create new salt and password hash
 										byte[] newsalt = HashManager.GenerateSalt();
 										byte[] newhash = HashManager.GenerateHash(input[1], newsalt);
 										
 										// Create query
 										String query = "UPDATE `"
 												+ login
 												+ "` SET `salt`= ?, `password`= ?, `pwreset`= ?, `temppwd`= ?, "
 												+ "`tmpsalt`= ?, `lastlogintime`= ? WHERE `username`= ?";
 										
 										PreparedStatement update = conn.prepareStatement(query);
 										
 										// Set query values
 										update.setBytes(1, newsalt);
 										update.setBytes(2, newhash);
 										update.setInt(3, 0);
 										update.setBytes(4, null);
 										update.setBytes(5, null);
 										update.setLong(6, unixtime);
 										update.setString(7, player.getName());
 										
 										// Execute query
 										update.execute();
 										
 										// Clean up
 										update.close();
 										
 										// Add player to the logged in list
 										ba.setState(BAState.LOGGEDIN);
 
 										// Remove player from requireLogin list
 										//int index = 0;
 										//for (Player p : instance.requireLogin) {
 										//	if (p.getName().equals(player.getName()))
 										//		index = instance.requireLogin
 										//				.indexOf(p);
 										//}
 										//instance.requireLogin.remove(index);
 										//if (instance.requireLogin.indexOf(player) > 0)
 										//	instance.requireLogin.remove(player);
 									}
 								}
 								else { // Password doesn't match
 									player.sendMessage(ChatColor.YELLOW + "Temp password does not match");
 								}
 							}
 						}
 						else if (Utils.byteToString(passwordCheck).equals(
 								Utils.byteToString(hash))) { // Passwords match
 							player.sendMessage(ChatColor.GREEN
 									+ "Password correct, welcome back "
 									+ player.getName());
 
 							// Get unix time stamp
 							long unixtime = Utils.unixTimestamp();
 
 							// Set up the query
 							String query = "UPDATE `" + login
 									+ "` SET lastlogintime='" + unixtime
 									+ "' WHERE username='"
 									+ player.getName() + "'";
 							PreparedStatement statement = conn
 									.prepareStatement(query);
 							statement.executeUpdate();
 							statement.close();
 
 							// Add player to the logged in list
 							ba.setState(BAState.LOGGEDIN);
 
 							// Remove player from requireLogin list
 							//int index = 0;
 							//for (Player p : instance.requireLogin) {
 							//	if (p.getName().equals(player.getName()))
 							//		index = instance.requireLogin
 							//				.indexOf(p);
 							//}
 							//instance.requireLogin.remove(index);
 							//if (instance.requireLogin.indexOf(player) > 0)
 							//	instance.requireLogin.remove(player);
 						} else { // Passwords do not match
 							player.sendMessage(ChatColor.YELLOW
 									+ "Password incorrect, try again.");
 						}
 					}
 					else { // IP addresses do not match
 						player.sendMessage(new String[] {
 								ChatColor.YELLOW
 										+ "Looks like you are under lockdown and your IP doesn't match.",
 								ChatColor.YELLOW
 										+ "If this is an error, contact an admin in IRC." });
 					}
 				}
 			}
 			else {
 				player.sendMessage(ChatColor.YELLOW + "You are already logged in, silly!");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void tryLogout(String[] input) {
 		Player player = null;
 		
 		for (Player p : plugin.getServer().getOnlinePlayers())
 			if (p.getName().equals(input[0]))
 				player = p;
 		
 		tryLogout(player, input);
 	}
 	
 	public void tryLogout(Player player, String[] input) {
 		// Get target players name
 		String name = input[0];
 		boolean playerFound = false;
 		
 		// Look for player
 		BAPlayer ba = plugin.pman.getBAPlayerByName(name);
 		if (ba != null)
 			playerFound = true;
 		
 		if (playerFound == true) {
 			// Remove the player from the loggedIn list
 			ba.setState(BAState.LOGGEDOUT);
 			
 			try {
 				// Create connection
 				Connection conn = DriverManager.getConnection(url, user, pass);
 				// Query to reset last login time to 1
 				String query = "UPDATE `"
 						+ login
 						+ "` SET lastlogintime='1' WHERE username='"
 						+ name + "'";
 				// Create statement object
 				Statement update = conn.createStatement();
 				// Execute update
 				update.executeUpdate(query);
 				
 				// Message sender
 				player.sendMessage(ChatColor.YELLOW + 
 						"Player " + name + " has been logged out");
 				
 				// Message recipient
 				ba.getPlayer().sendMessage(ChatColor.YELLOW + "You have been logged out");
 				
 				// Clean up
 				update.close();
 				conn.close();
 			} catch (SQLException se) {
 				se.printStackTrace();
 			}
 		}
 	}
 	
 	public void tryLogoutFromConsole(String[] input) {
 		// Get target players name
 		String name = input[0];
 		boolean playerFound = false;
 
 		// Look for player
 		BAPlayer ba = plugin.pman.getBAPlayerByName(name);
 		if (ba != null)
 			playerFound = true;
 
 		if (playerFound == true) {
 			// Remove the player from the loggedIn list
 			ba.setState(BAState.LOGGEDOUT);
 
 			try {
 				// Create connection
 				Connection conn = DriverManager.getConnection(url, user, pass);
 				// Query to reset last login time to 1
 				String query = "UPDATE `" + login
 						+ "` SET lastlogintime='1' WHERE username='"
 						+ name + "'";
 				// Create statement object
 				Statement update = conn.createStatement();
 				// Execute update
 				update.executeUpdate(query);
 
 				// Message sender
 				plugin.log.println("Player " + name + " has been logged out");
 
 				// Message recipient
 				ba.getPlayer().sendMessage(
 						ChatColor.YELLOW + "You have been logged out");
 
 				// Clean up
 				update.close();
 				conn.close();
 			} catch (SQLException se) {
 				se.printStackTrace();
 			}
 		}
 	}
 
 	public void tryUnregister(Player player) {
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		
 		try {
 			// Create connection
 			Connection conn = DriverManager.getConnection(url, user, pass);
 			String query = "DELETE FROM `" + login + "` WHERE username='" + player.getName() + "'";
 			Statement delete = conn.createStatement();
 			delete.executeUpdate(query);
 			
 			// Remove player from loggedIn list
 			//int index = 0;
 			//for (Player p : instance.loggedIn) {
 			//	if (p.getName().equals(player.getName()))
 			//		index = instance.loggedIn.indexOf(p);
 			//}
 			//instance.loggedIn.remove(index);
 			ba.setState(BAState.LOGGEDOUT);
 			
 			// Add player to unregistered list
 			//instance.unregistered.add(player);
 			ba.setState(BAState.UNREGISTERED);
 			
 			// Inform the player that they have been unregistered from the server
 			player.sendMessage(ChatColor.GREEN + "Successfully unregistered, goodbye!");
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 	}
 	
 	public void modifyWhitelist(Object sender, String[] input) {
 		try {
 			if (input.length == 2) {
 				if (input[0].equals("add") || input[0].equals("del")) {
 					// Determine add or remove
 					boolean addremove = false;
 					if (input[0].equals("add"))
 						addremove = true;
 					if (input[0].equals("del"))
 						addremove = false;
 					
 					// Get indicated user name
 					String username = input[1];
 					
 					// Create connection and statement
 					Connection conn = DriverManager.getConnection(url, user, pass);
 					
 					// Check for preexisting entry
 					Statement select = conn.createStatement();
 					ResultSet result = select.executeQuery(
 							"SELECT * FROM `bit_whitelist`");
 					
 					boolean playerFound = false;
 					if (result.next()) { // Results found
 						do {
 							String user = result.getString(1);
 							if (user.equals(username)) { // Player data found
 								playerFound = true;
 							}
 						} while (result.next());
 					}
 					
 					if (playerFound == true) { // Update existing information
 						String query = "UPDATE `" + wlist + "` SET enabled='"
 								+ (addremove == true ? 1 : 0)
 								+ "' WHERE username='" + username + "'";
 						Statement update = conn.createStatement();
 													
 						update.executeUpdate(query);
 						update.close();
 					}
 					if (playerFound == false) { // Insert new data
 						String query = "INSERT INTO `" + wlist + "` " +
 								"(`username`, `enabled`) VALUES (?,?)";
 						PreparedStatement insert = conn.prepareCall(query);
 						
 						insert.setString(1, username);
 						insert.setInt(2, (addremove == true ? 1 : 0));
 						
 						insert.executeUpdate();
 						insert.close();
 					}
 					
 					// Clean up
 					result.close();
 					conn.close();
 					
 					// Report back to player
 					if (sender instanceof Player) {
 						Player player = (Player)sender;
 						player.sendMessage(ChatColor.YELLOW + input[1]
 								+ " has been "
 								+ ((addremove == true) ? "added to" : "removed from")
 								+ " the whitelist");
 					} else {
 						plugin.log.println(input[1]
 								+ " has been "
 								+ ((addremove == true) ? "added to" : "removed from")
 								+ " the whitelist");
 					}
 				}
 			}
 			if (input.length == 1) {
 				if (input[0].equals("enable")) { // Enable whitelist
 					// Check current whitelist state
 					boolean wl = plugin.config.readBoolean("Use_Whitelist");
 					if (wl == false) {
 						plugin.config.write("Use_Whitelist", true);
 						if (sender instanceof Player)
 							((Player)sender).sendMessage(ChatColor.GREEN + "Whitelist enabled");
 						else
 							plugin.log.println("Whitelist enabled");
 					}
 					else
 						if (sender instanceof Player)
 							((Player)sender).sendMessage(ChatColor.YELLOW + "Whitelist is already enabled");
 						else
 							plugin.log.println("Whitelist is already enabled");
 				}
 				if (input[0].equals("disable")) { // Disable whitelist
 					// Check current whitelist state
 					boolean wl = plugin.config.readBoolean("Use_Whitelist");
 					if (wl == true) {
 						plugin.config.write("Use_Whitelist", false);
 						
 						if (sender instanceof Player)
 							((Player)sender).sendMessage(ChatColor.GREEN + "Whitelist disabled");
 						else
 							plugin.log.println("Whitelist disabled");
 					}
 					else
 						if (sender instanceof Player)
 							((Player)sender).sendMessage(ChatColor.YELLOW + "Whitelist is already disabled");
 						else
 							plugin.log.println("Whitelist is already disabled");
 				}
 			}
 		} catch (SQLException se) {
 			se.printStackTrace();
 		}
 	}
 
 	public void tryIpcheck(Player player, String[] input) {
 		if (input.length == 1) {
 			if (input[0].equals("enable") || input[0].equals("disable")) {
 				boolean ipcheckFlag = false;
 				int ipcheckFlagInt = 0;
 				
 				// Check the input
 				if (input[0].equals("enable"))
 					ipcheckFlag = true;
 				if (input[0].equals("disable"))
 					ipcheckFlag = false;
 				
 				if (ipcheckFlag == true)
 					ipcheckFlagInt = 1;
 				if (ipcheckFlag == false)
 					ipcheckFlagInt = 0;
 				
 				try {
 					// Create connection
 					Connection conn = DriverManager.getConnection(url, user, pass);
 					String query = "UPDATE `" + login + "` SET enableipcheck='"
 							+ ipcheckFlagInt + "' WHERE username='"
 							+ player.getName() + "'";
 					Statement update = conn.createStatement();
 					update.executeUpdate(query);
 					
 					update.close();
 					
 					player.sendMessage(ChatColor.GREEN
 							+ "IP checking for your account has been "
 							+ ((ipcheckFlag == true) ? "enabled" : "disabled"));
 				} catch (SQLException se) {
 					se.printStackTrace();
 				}
 			}
 			else {
 				player.sendMessage(ChatColor.YELLOW + "Error: Argument " + input[0] + " not recognized");
 			}
 		}
 	}
 
 	public void tryChangePassword(Player player, String[] input) {
 		if (input.length == 2) {
 			String oldpasswd = input[0];
 			String newpasswd = input[1];
 			
 			try {
 				Connection conn = DriverManager.getConnection(url, user, pass);
 				Statement select = conn.createStatement();
 				ResultSet result = select.executeQuery(
 						"SELECT * FROM `" + login + "` WHERE username='" + player.getName() + "'");
 				
 				if (result.next()) { // Results found
 					byte[] salt = result.getBytes(2);
 					byte[] pwdhash = result.getBytes(3);
 					
 					byte[] pwdtest = HashManager.GenerateHash(oldpasswd, salt);
 					if (Utils.byteToString(pwdtest).equals(Utils.byteToString(pwdhash))) { // oldpasswd matches current password
 						// Generate new salt for password and new password hash from the input + salt
 						byte[] newsalt = HashManager.GenerateSalt();
 						byte[] newpass = HashManager.GenerateHash(newpasswd, newsalt);
 
 						// Create query and statement
 						String query = "UPDATE `" + login
 								+ "` SET `salt`= ?, `password`= ? " + "WHERE `username`= ?";
 						PreparedStatement update = conn.prepareStatement(query);
 						
 						// Set the values in the statement
 						update.setBytes(1, newsalt);
 						update.setBytes(2, newpass);
 						update.setString(3, player.getName());
 						
 						// Execute the statement
 						update.execute();
 						
 						// Clean up
 						update.close();
 						
 						player.sendMessage(ChatColor.GREEN + "Password updated successfully");
 					} else { // oldpasswd does not match current password
 						player.sendMessage(ChatColor.YELLOW + "Incorrect password");
 					}
 				}
 				
 				result.close();
 				conn.close();
 			} catch (SQLException se) {
 				se.printStackTrace();
 			} catch (UnsupportedEncodingException uee) {
 				uee.printStackTrace();
 			} catch (NoSuchAlgorithmException nsae) {
 				nsae.printStackTrace();
 			}
 		}
 	}
 
 	public void tryResetPassword(Object sender, String[] input) {
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			if ((plugin.pex.has(player, "bitauth.pwreset") || player.isOp()) 
 					&& input.length == 2) { // Proper syntax usage and permission
 				String username = input[0];
 				String passwd = input[1];
 				
 				try {
 					// Create connection and exect SELECT query
 					Connection conn = DriverManager.getConnection(url, user, pass);
 					String query = "SELECT * FROM `" + login + "` WHERE `username`='" + username + "'";
 					Statement statement = conn.createStatement();
 					ResultSet result = statement.executeQuery(query);
 					
 					if (result.next()) { // Data found
 						// Generate new password
 						byte[] salt = HashManager.GenerateSalt();
 						byte[] pass = HashManager.GenerateHash(passwd, salt);
 						
 						// Create update query
 						String queryUpdate = "UPDATE `"
 								+ login
 								+ "` SET `pwreset`= ?, `temppwd`= ?, `tmpsalt`= ? WHERE `username`= ?";
 						
 						PreparedStatement update = conn.prepareStatement(queryUpdate);
 						
 						// Set values
 						update.setInt(1, 1);
 						update.setBytes(2, pass);
 						update.setBytes(3, salt);
 						update.setString(4, username);
 						
 						// Execute the query
 						update.execute();
 						
 						// Clean up
 						update.close();
 						
 						// Send feedback to player
 						player.sendMessage(ChatColor.YELLOW
 								+ "Password for "
 								+ username
 								+ " has been reset; Inform them of their new temp password");
 					}
 					
 					result.close();
 					statement.close();
 					conn.close();
 				} catch (SQLException se) {
 					se.printStackTrace();
 				} catch (NoSuchAlgorithmException nsae) {
 					nsae.printStackTrace();
 				} catch (UnsupportedEncodingException uee) {
 					uee.printStackTrace();
 				}
 			}
 			else if (!player.hasPermission("bitauth.pwreset")) {
 				player.sendMessage(ChatColor.YELLOW + "You do not have access to this feature");
 			}
 		}
 		if (sender instanceof ConsoleCommandSender) {
 			if (input.length == 2) {
 				String username = input[0];
 				String passwd = input[1];
 				
 				try {
 					// Create connection and exect SELECT query
 					Connection conn = DriverManager.getConnection(url, user, pass);
 					String query = "SELECT * FROM `" + login + "` WHERE `username`='" + username + "'";
 					Statement statement = conn.createStatement();
 					ResultSet result = statement.executeQuery(query);
 					
 					if (result.next()) { // Data found
 						// Generate new password
 						byte[] salt = HashManager.GenerateSalt();
 						byte[] pass = HashManager.GenerateHash(passwd, salt);
 						
 						// Create update query
 						String queryUpdate = "UPDATE `"
 								+ login
 								+ "` SET `pwreset`= ?, `temppwd`= ?, `tmpsalt`= ?, `lastlogintime`= ? WHERE `username`= ?";
 						
 						PreparedStatement update = conn.prepareStatement(queryUpdate);
 						
 						// Set values
 						update.setInt(1, 1);
 						update.setBytes(2, pass);
 						update.setBytes(3, salt);
 						update.setLong(4, 0);
 						update.setString(5, username);
 						
 						// Execute the query
 						update.execute();
 						
 						// Clean up
 						update.close();
 						
 						// Send feedback to player
 						plugin.log.println("Password for "
 								+ username
 								+ " has been reset; Inform them of their new temp password");
 					}
 					
 					result.close();
 					statement.close();
 					conn.close();
 				} catch (SQLException se) {
 					se.printStackTrace();
 				} catch (NoSuchAlgorithmException nsae) {
 					nsae.printStackTrace();
 				} catch (UnsupportedEncodingException uee) {
 					uee.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void tryJumblePassword(CommandSender sender, String[] split) {
 		if (split.length == 1) {
 			if (sender instanceof Player) {
 				Player player = (Player)sender;
 				if (plugin.pex.has(player, "bitauth.jumble") || player.isOp()) {
 					String username = split[0];
 					
 					try {
 						Connection conn = DriverManager.getConnection(url, user, pass);
 						Statement select = conn.createStatement();
 						ResultSet result = select.executeQuery(
 								"SELECT * FROM `" + login + "` WHERE `username`='" + username + "'");
 						
 						boolean playerFound = false;
 						if (result.next()) { // Results found
 							do {
 								String name = result.getString(1);
 								if (name.equals(username)) { // Player data found
 									playerFound = true;
 								}
 							} while (result.next());
 						}
 						
 						if (playerFound == true) {
 							byte[] rpasswd = HashManager.GenerateHash(
 									HashManager.RandomString(), HashManager.GenerateSalt());
 							Statement update = conn.createStatement();
 							String query = "UPDATE `" + login
 									+ "` SET `password`='" + rpasswd
 									+ "' WHERE `username`='" + username + "'";
 							update.executeUpdate(query);
 							update.close();
 							
 							player.sendMessage(ChatColor.YELLOW +
 									"Password for " + username + " has been replaced");
 						}
 						else
 							player.sendMessage(ChatColor.YELLOW + 
 									"Player " + username + " not found");
 						result.close();
 						select.close();
 						conn.close();
 					} catch (SQLException se) {
 						se.printStackTrace();
 					} catch (NoSuchAlgorithmException nsae) {
 						nsae.printStackTrace();
 					} catch (UnsupportedEncodingException uee) {
 						uee.printStackTrace();
 					}
 				}
 				else {
 					player.sendMessage(ChatColor.YELLOW +
 							"You do not have access to this feature.");
 				}
 			}
 
 			if (sender instanceof ConsoleCommandSender) {
 				String username = split[0];
 				
 				try {
 					Connection conn = DriverManager.getConnection(url, user, pass);
 					Statement select = conn.createStatement();
 					ResultSet result = select.executeQuery(
 							"SELECT * FROM `" + login + "` WHERE `username`='" + username + "'");
 					
 					boolean playerFound = false;
 					if (result.next()) { // Results found
 						do {
 							String name = result.getString(1);
 							if (name.equals(username)) { // Player data found
 								playerFound = true;
 							}
 						} while (result.next());
 					}
 					
 					if (playerFound == true) {
 						byte[] rpasswd = HashManager.GenerateHash(
 								HashManager.RandomString(), HashManager.GenerateSalt());
 						Statement update = conn.createStatement();
 						String query = "UPDATE `" + login
 								+ "` SET `password`='" + rpasswd
 								+ "' WHERE `username`='" + username + "'";
 						update.executeUpdate(query);
 						update.close();
 						
 						plugin.log.println(
 								"Password for " + username + " has been replaced");
 						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 							if (p.getName().equals(username)) {
 								Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "logout " + username);
 								p.kickPlayer("Goodbye.");
 								plugin.log.println("Player " + username + " has been kicked from the server");
 							}
 						}
 					}
 					else
 						plugin.log.println("Player " + username + " not found");
 					result.close();
 					select.close();
 					conn.close();
 				} catch (SQLException se) {
 					se.printStackTrace();
 				} catch (NoSuchAlgorithmException nsae) {
 					nsae.printStackTrace();
 				} catch (UnsupportedEncodingException uee) {
 					uee.printStackTrace();
 				}
 			}
 		}
 	}
 }
