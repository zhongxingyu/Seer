 package bitlegend.bitauth.commands;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import bitlegend.bitauth.BitAuth;
 
 public class Whitelist implements CommandExecutor {
 	@SuppressWarnings("unused")
 	private BitAuth instance;
 	
 	// Database info
 	private String user = "";
 	private String pass = "";
 	private String url = "";
 	private String wltable = "";
 	
 	public Whitelist(BitAuth instance) {
 		this.instance = instance;
 		user = instance.config.readString("DB_User");
 		pass = instance.config.readString("DB_Pass");
 		url = "jdbc:mysql://" + instance.config.readString("DB_Host") + 
 			"/" + instance.config.readString("DB_Name");
 		wltable = instance.config.readString("DB_Table_whitelist");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
 		boolean r = false;
 		Player player = (Player)sender;
 		
		// Need to add permission handling
		if (player.getDisplayName().equals("dechiridas")) {
 			try {
 				if (split.length == 2) {
 					if (split[0].equals("add") || split[0].equals("del")) {
 						// Determine add or remove
 						boolean addremove = false;
 						if (split[0].equals("add"))
 							addremove = true;
 						if (split[0].equals("del"))
 							addremove = false;
 						
 						// Get indicated user name
 						String username = split[1];
 						
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
 							String query = "UPDATE `" + wltable + "` SET enabled='"
 									+ (addremove == true ? 1 : 0)
 									+ "' WHERE username='" + username + "'";
 							Statement update = conn.createStatement();
 														
 							update.executeUpdate(query);
 							update.close();
 						}
 						if (playerFound == false) { // Insert new data
 							String query = "INSERT INTO `" + wltable + "` " +
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
 						player.sendMessage(ChatColor.GREEN + split[1]
 								+ " has been "
								+ ((addremove == true) ? "added" : "removed")
								+ " from the whitelist");
 						r = true;
 					}
 				}
 			} catch (SQLException se) {
 				se.printStackTrace();
 			}
 		}
 		
 		return r;
 	}
 
 }
