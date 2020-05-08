 package com.etriacraft.probending;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Color;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.sk89q.worldguard.bukkit.WGBukkit;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 import tools.BendingType;
 import tools.Tools;
 
 public class Methods {
 
 	Probending plugin;
 
 	public Methods(Probending plugin) {
 		this.plugin = plugin;
 	}
 
 	// Probending Match Stuff
 	public static boolean matchStarted = false;
 	public static boolean matchPaused = false;
 	public static Set<String> playingTeams = new HashSet<String>();
 	public static String TeamOne = null;
 	public static String TeamTwo = null;
 	public static HashMap<String, String> allowedZone = new HashMap<String, String>();
 
 	// WorldGuard Stuffs
 	public static boolean WGSupportEnabled = Probending.plugin.getConfig().getBoolean("WorldGuard.EnableSupport");
 	public static boolean buildDisabled = Probending.plugin.getConfig().getBoolean("WorldGuard.DisableBuildOnField");
 	public static String ProbendingField = Probending.plugin.getConfig().getString("WorldGuard.ProbendingField");
 	public static boolean AutomateMatches = Probending.plugin.getConfig().getBoolean("WorldGuard.AutomateMatches");
 	public static String t1z1 = Probending.plugin.getConfig().getString("WorldGuard.TeamOneZoneOne").toLowerCase();
 	public static String t1z2 = Probending.plugin.getConfig().getString("WorldGuard.TeamOneZoneTwo").toLowerCase();
 	public static String t1z3 = Probending.plugin.getConfig().getString("WorldGuard.TeamOneZoneThree").toLowerCase();
 	public static String t2z1 = Probending.plugin.getConfig().getString("WorldGuard.TeamTwoZoneOne").toLowerCase();
 	public static String t2z2 = Probending.plugin.getConfig().getString("WorldGuard.TeamTwoZoneTwo").toLowerCase();
 	public static String t2z3 = Probending.plugin.getConfig().getString("WorldGuard.TeamTwoZoneThree").toLowerCase();
 
 	// Ends a Match
 	public static void restoreArmor() {
 		for (Player player: Bukkit.getOnlinePlayers()) {
 			if (Commands.tmpArmor.containsKey(player)) {
 				if (player.getInventory().getArmorContents() != null) {
 					player.getInventory().setArmorContents(null);
 				}
 				player.getInventory().setArmorContents(Commands.tmpArmor.get(player));
 				Commands.tmpArmor.remove(player);
 			}
 		}
 		
 	}
 	
 	public static Set<String> colors = new HashSet<String>();
 	// Moves players up
 	public static void MovePlayersUp(String team, String Side) {
 		for (Player player: Bukkit.getOnlinePlayers()) {
 			if (getPlayerTeam(player.getName()) != null) {
 				if (getPlayerTeam(player.getName()).equalsIgnoreCase(team)) {
 					String playerZone = allowedZone.get(player.getName());
 						if (playerZone != null) {
 							if (Side.equalsIgnoreCase("One")) {
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t1z1)) {
 									allowedZone.put(player.getName(), t2z1); // Moves them up to Team Two Zone One
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t2z1)) {
 									allowedZone.put(player.getName(), t2z2);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t1z2)) {
 									allowedZone.put(player.getName(), t1z1);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t1z3)) {
 									allowedZone.put(player.getName(), t1z2);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 							}
 							if (Side.equalsIgnoreCase("Two")) {
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t2z1)) {
 									allowedZone.put(player.getName(), t1z1);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t2z2)) {
 									allowedZone.put(player.getName(), t2z1);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t2z3)) {
 									allowedZone.put(player.getName(), t2z2);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 								if (allowedZone.get(player.getName()).equalsIgnoreCase(t1z1)) {
 									allowedZone.put(player.getName(), t1z2);
 									player.sendMessage(Strings.Prefix + Strings.MoveUpOneZone);
 									return;
 								}
 							}
 							
 					}
 				}
 			}
 		}
 	}
 	//Returns a set of players allowed in a zone.
 	public static Set<String> playersInZone(String zone) {
 		Set<String> playersInZone = new HashSet<String>();
 		for (Player p: Bukkit.getOnlinePlayers()) {
 			if (allowedZone.containsKey(p.getName())) {
 				if (allowedZone.get(p.getName()).equalsIgnoreCase(zone)) {
 					playersInZone.add(p.getName());
 				}
 			}
 		}
 		return playersInZone;
 	}
 	// Storage Data
 	public static Set<String> teams = new HashSet<String>();
 	public static HashMap<String, String> players = new HashMap<String, String>();
 	public static String storage = Probending.plugin.getConfig().getString("General.Storage");
 
 	// Gives the player Leather Armor (With Color)
 	public static ItemStack createColorArmor(ItemStack i, Color c)
 	{
 		LeatherArmorMeta meta = (LeatherArmorMeta)i.getItemMeta();
 		meta.setColor(c);
 		i.setItemMeta(meta);
 		return i;
 	}
 
 	// Populates list of colors.
 	public static void populateColors() {
 		colors.add("Cyan");
 		colors.add("Black");
 		colors.add("Blue");
 		colors.add("Magenta");
 		colors.add("Gray");
 		colors.add("Green");
 		colors.add("LightGreen");
 		colors.add("DarkRed");
 		colors.add("DarkBlue");
 		colors.add("Olive");
 		colors.add("Orange");
 		colors.add("Purple");
 		colors.add("Red");
 		colors.add("Gray");
 		colors.add("Teal"); 
 		colors.add("White");
 		colors.add("Yellow");
 
 	}
 
 	// Checks if WorldGuard is enabled.
 	public static WorldGuardPlugin getWorldGuard() {
 		Plugin plugin = Probending.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
 
 		// WorldGuard may not be loaded
 		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
 			return null; // Maybe you want throw an exception instead
 		}
 
 		return (WorldGuardPlugin) plugin;
 	}
 	// Gets region player is in
 	public static Set<String> RegionsAtLocation(Location loc) {
 		
 		ApplicableRegionSet set = WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
 		Set<String> regions = new HashSet<String>();
 		for (ProtectedRegion region: set) {
 			regions.add(region.getId());
 		}
 		
 		return regions;
 		
 	}
 
 	// Sends a message in Probending Chat
 	public static void sendPBChat(String message) {
 		for (Player player: Bukkit.getOnlinePlayers()) {
 			if (Commands.pbChat.contains(player)) {
 				player.sendMessage(Strings.Prefix + message);
 			}
 		}
 	}
 	// Gets a color from a string.
 	public static Color getColorFromString (String pretendColor) {
 		if (pretendColor.equalsIgnoreCase("Cyan")) {
 			return Color.AQUA;
 		}
 		if (pretendColor.equalsIgnoreCase("Black")) {
 			return Color.BLACK;
 		}
 		if (pretendColor.equalsIgnoreCase("Blue")) {
 			return Color.BLUE;
 		}
 		if (pretendColor.equalsIgnoreCase("Magenta")) {
 			return Color.FUCHSIA;
 		}
 		if (pretendColor.equalsIgnoreCase("Gray")) {
 			return Color.GRAY;
 		}
 		if (pretendColor.equalsIgnoreCase("Green")) {
 			return Color.GREEN;
 		}
 		if (pretendColor.equalsIgnoreCase("LightGreen")) {
 			return Color.LIME;
 		}
 		if (pretendColor.equalsIgnoreCase("DarkRed")) {
 			return Color.MAROON;
 		}
 		if (pretendColor.equalsIgnoreCase("Navy")) {
 			return Color.NAVY;
 		}
 		if (pretendColor.equalsIgnoreCase("Olive")) {
 			return Color.OLIVE;
 		}
 		if (pretendColor.equalsIgnoreCase("Orange")) {
 			return Color.ORANGE;
 		}
 		if (pretendColor.equalsIgnoreCase("Purple")) {
 			return Color.PURPLE;
 		}
 		if (pretendColor.equalsIgnoreCase("Red")) {
 			return Color.RED;
 		}
 		if (pretendColor.equalsIgnoreCase("Silver")) {
 			return Color.SILVER;
 		}
 		if (pretendColor.equalsIgnoreCase("Teal")) {
 			return Color.TEAL;
 		}
 		if (pretendColor.equalsIgnoreCase("White")) {
 			return Color.WHITE;
 		}
 		if (pretendColor.equalsIgnoreCase("Yellow")) {
 			return Color.YELLOW;
 		}
 		return null;
 	}
 
 	// Checks if the team exists, returns true if the team does exist, returns false if not.
 	public static boolean teamExists(String teamName) {
 		for (String team: teams) {
 			if (team.equalsIgnoreCase(teamName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// Loads teams on startup
 	public static void loadTeams() {
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT team FROM probending_teams");
 			try {
 				while (rs2.next()) {
 					teams.add(rs2.getString("team"));
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			teams = Probending.plugin.getConfig().getConfigurationSection("TeamInfo").getKeys(false);
 		}
 	}
 
 	// Load Players on Starup
 	public static void loadPlayers() {
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM probending_players");
 			try {
 				while (rs2.next()) {
 					players.put(rs2.getString("player"), rs2.getString("team"));
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Set<String> tmpPlayers = new HashSet<String>();
 			tmpPlayers.addAll(Probending.plugin.getConfig().getConfigurationSection("players").getKeys(false));
 			for (String player: tmpPlayers) {
 				if (Probending.plugin.getConfig().getString("players." + player) != null) {
 					String teamName = Probending.plugin.getConfig().getString("players." + player);
 					players.put(player, teamName);
 				}
 			}
 		}
 	}
 	// Creates a team.
 	public static void createTeam(String teamName, String owner) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("INSERT INTO probending_teams (team, owner) VALUES ('" + teamName + "', '" + owner + "')");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + ".Owner", owner);
 			Probending.plugin.saveConfig();
 		}
 		teams.add(teamName);
 	}
 	// Deletes a team.
 	public static void deleteTeam(String teamName) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("DELETE FROM probending_teams WHERE team = '" + teamName + "'");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName, null);
 			Probending.plugin.saveConfig();
 		}
 		teams.remove(teamName);
 	}
 	// Adds a player to a team.
 	public static void addPlayerToTeam(String teamName, String player, String element) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET " + element + " = '" + player + "' WHERE team = '" + teamName + "'");
 			DBConnection.sql.modifyQuery("INSERT INTO probending_players (player, team) VALUES ('" + player + "', '" + teamName + "')");
 			players.put(player, teamName);
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + "." + element, player);
 			Probending.plugin.getConfig().set("players." + player, teamName);
 			Probending.plugin.saveConfig();
 			players.put(player, teamName);
 		}
 	}
 
 	// Removes a player from a team.
 	public static void removePlayerFromTeam(String teamName, String player, String element) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("DELETE FROM probending_players WHERE player = '" + player + "'");
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET " + element + " = NULL WHERE team = '" + teamName + "'");
 			players.remove(player);
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + "." + element, null);
 			Probending.plugin.getConfig().set("players." + player, null);
 			Probending.plugin.saveConfig();
 			players.remove(player);
 		}
 	}
 
 	// Checks if the player is in a team. Returns true if the player is in a team.
 	public static boolean playerInTeam(String playerName) {
 		if (players.containsKey(playerName)) {
 			return true;
 		}
 		return false;
 		//		if (storage.equalsIgnoreCase("mysql")) {
 		//			ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM probending_players WHERE player = '" + playerName + "'");
 		//			try {
 		//				if (rs2.next()) {
 		//					return true;
 		//				} else {
 		//					return false;
 		//				}
 		//			} catch (SQLException ex) {
 		//				ex.printStackTrace();
 		//			}
 		//		}
 		//		if (storage.equalsIgnoreCase("flatfile")) {
 		//			if (Probending.plugin.getConfig().get("players." + playerName) == (null)) {
 		//				return false;
 		//			} else {
 		//				return true;
 		//			}
 		//		}
 		//		return false;
 	}
 
 	// Checks the player's element in the team. (Regardless of if they've changed)
 	public static String getPlayerElementInTeam(String playerName, String teamName) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM probending_teams WHERE team = '" + teamName + "'");
 			try {
 				rs2.next();
 				if (rs2.getString("Air") != null && rs2.getString("Air").equals(playerName)) {
 					return "Air";
 				}
 				if (rs2.getString("Water") != null && rs2.getString("Water").equals(playerName)) {
 					return "Water";
 				}
 				if (rs2.getString("Earth") != null && rs2.getString("Earth").equals(playerName)) {
 					return "Earth";
 				}
 				if (rs2.getString("Fire") != null && rs2.getString("Fire").equals(playerName)) {
 					return "Fire";
 				}
 				if (rs2.getString("Chi") != null && rs2.getString("Chi").equals(playerName)) {
 					return "Chi";
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			if (Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Air") != null && Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Air").equals(playerName)) {
 				return "Air";
 			}
 			if (Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Water") != null && Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Water").equals(playerName)) {
 				return "Water";
 			}
 			if (Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Earth") != null && Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Earth").equals(playerName)) {
 				return "Earth";
 			}
 			if (Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Fire") != null && Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Fire").equals(playerName)) {
 				return "Fire";
 			}
 			if (Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Chi") != null && Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Chi").equals(playerName)) {
 				return "Chi";
 			}
 		}
 		return null;
 	}
 
 	// Returns the name of the player's team.
 	public static String getPlayerTeam(String player) {
 		return players.get(player);
 		//		if (storage.equalsIgnoreCase("mysql")) {
 		//			ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM probending_players WHERE player = '" + player + "'");
 		//			try {
 		//				if (rs2.next()) {
 		//					return rs2.getString("team");
 		//				}
 		//			} catch (SQLException ex) {
 		//				ex.printStackTrace();
 		//			}
 		//		}
 		//		if (storage.equalsIgnoreCase("flatfile")) {
 		//			return Probending.plugin.getConfig().getString("players." + player);
 		//		}
 		//		return null;
 	}
 
 	// Returns the amount of players in a team.
 	public static int getTeamSize(String teamName) {
 		int size = 0;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM probending_teams WHERE team = '" + teamName + "'");
 			try {
 				rs2.next();
 				if (rs2.getString("Air") != null) {
 					size++;
 				}
 				if (rs2.getString("Water") != null) {
 					size++;
 				}
 				if (rs2.getString("Earth") != null) {
 					size++;
 				}
 				if (rs2.getString("Fire") != null) {
 					size++;
 				}
 				if (rs2.getString("Chi") != null) {
 					size++;
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			if (Probending.plugin.getConfig().get("TeamInfo." + teamName + ".Air") != null) {
 				size++;
 			}
 			if (Probending.plugin.getConfig().get("TeamInfo." + teamName + ".Water") != null) {
 				size++;
 			}
 			if (Probending.plugin.getConfig().get("TeamInfo." + teamName + ".Earth") != null) {
 				size++;
 			}
 			if (Probending.plugin.getConfig().get("TeamInfo." + teamName + ".Fire") != null) {
 				size++;
 			}
 			if (Probending.plugin.getConfig().get("TeamInfo." + teamName + ".Chi") != null) {
 				size++;
 			}
 		}
 		return size;
 	}
 
 	// Gets the owner of a team.
 	public static String getOwner(String teamName) {
 		String owner = null;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT owner FROM probending_teams WHERE team = '" + teamName + "'");
 			try {
 				rs2.next();
 				owner = rs2.getString("owner");
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			owner = Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Owner");
 		}
 		return owner;
 	}
 	// Sets the owner of a team.
 	public static void setOwner(String player, String teamName) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET owner = '" + player + "' WHERE team = '" + teamName + "'");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + ".Owner", player);
 			Probending.plugin.saveConfig();
 		}
 	}
 	// Returns true if the player is the owner of the team.
 	public static boolean isPlayerOwner(String player, String teamName) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT owner FROM probending_teams WHERE team = '" + teamName + "' AND owner = '" + player + "'");
 			try {
 				if (rs2.next()) {
 					return true;
 				} else {
 					return false;
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			if (Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Owner").equals(player)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// Returns a set (List) of all of the teams elements.
 	public static Set<String> getTeamElements(String teamName) {
 		Set<String> teamelements = new HashSet<String>();
 
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM probending_teams WHERE team = '" + teamName + "'");
 			try {
 				rs2.next();
 				if (rs2.getString("Air") != null) {
 					teamelements.add("Air");
 				}
 				if (rs2.getString("Water") != null) {
 					teamelements.add("Water");
 				}
 				if (rs2.getString("Earth") != null) {
 					teamelements.add("Earth");
 				}
 				if (rs2.getString("Fire") != null) {
 					teamelements.add("Fire");
 				}
 				if (rs2.getString("Chi") != null) {
 					teamelements.add("Chi");
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			teamelements.addAll(Probending.plugin.getConfig().getConfigurationSection("TeamInfo." + teamName).getKeys(true));
 			return teamelements;
 		}
 		return teamelements;
 	}
 
 	// Returns the name of the team's airbender.
 	public static String getTeamAirbender(String teamName) {
 		String airbender = null;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT Air FROM probending_teams WHERE team = '"+ teamName + "'");
 			try {
 				if (rs2.next()) {
 					airbender = rs2.getString("Air");
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			airbender = Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Air");
 		}
 		return airbender;
 	}
 
 	// Returns the name of the team's waterbender.
 	public static String getTeamWaterbender(String teamName) {
 		String waterbender = null;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT Water FROM probending_teams WHERE team = '"+ teamName + "'");
 			try {
 				if (rs2.next()) {
 					waterbender = rs2.getString("Water");
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			waterbender = Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Water");
 		}
 		return waterbender;
 	}
 
 	// Returns the name of the team's earthbender.
 	public static String getTeamEarthbender(String teamName) {
 		String earthbender = null;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT Earth FROM probending_teams WHERE team = '"+ teamName + "'");
 			try {
 				if (rs2.next()) {
 					earthbender = rs2.getString("Earth");
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			earthbender = Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Earth");
 		}
 		return earthbender;
 	}
 
 	// Returns the name of the team's firebender.
 	public static String getTeamFirebender(String teamName) {
 		String fire = null;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT Fire FROM probending_teams WHERE team = '"+ teamName + "'");
 			try {
 				if (rs2.next()) {
 					fire = rs2.getString("Fire");
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			fire = Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Fire");
 		}
 		return fire;
 	}
 
 	// Returns the name of the team's chiblocker.
 	public static String getTeamChiblocker(String teamName) {
 		String chi = null;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT Chi FROM probending_teams WHERE team = '"+ teamName + "'");
 			try {
 				if (rs2.next()) {
 					chi = rs2.getString("Chi");
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			chi = Probending.plugin.getConfig().getString("TeamInfo." + teamName + ".Chi");
 		}
 		return chi;
 	}
 
 	// Checks if Airbenders are allowed to be in Probending teams.
 	public static boolean getAirAllowed() {
 		return Probending.plugin.getConfig().getBoolean("TeamSettings.AllowAir");
 	}
 
 	// Checks if Waterbenders are allowed to be in Probending teams.
 	public static boolean getWaterAllowed() {
 		return Probending.plugin.getConfig().getBoolean("TeamSettings.AllowWater");
 	}
 
 	// Checks if Earthbenders are allowed to be in Probending teams.
 	public static boolean getEarthAllowed() {
 		return Probending.plugin.getConfig().getBoolean("TeamSettings.AllowEarth");
 	}
 
 	// Checks if Firebenders are allowed to be in Probending teams.
 	public static boolean getFireAllowed() {
 		return Probending.plugin.getConfig().getBoolean("TeamSettings.AllowFire");
 	}
 
 	// Checks if Chiblockers are allowed to be in Probending teams.
 	public static boolean getChiAllowed() {
 		return Probending.plugin.getConfig().getBoolean("TeamSettings.AllowChi");
 	}
 
 	//Returns the player's element as a string.
 	public static String getPlayerElementAsString(String player) {
 		if (!Tools.isBender(player)) {
 			return null;
 		}
 		if (Tools.isBender(player, BendingType.Air)) {
 			return "Air";
 		}
 		if (Tools.isBender(player, BendingType.Water)) {
 			return "Water";
 		}
 		if (Tools.isBender(player, BendingType.Earth)) {
 			return "Earth";
 		}
 		if (Tools.isBender(player, BendingType.Fire)){
 			return "Fire";
 		}
 		if (Tools.isBender(player, BendingType.ChiBlocker)) {
 			return "Chi";
 		}
 		return null;
 	}
 
 	// Adds color to messages.
 	public static String colorize(String message) {
 		return message.replaceAll("(?i)&([a-fk-or0-9])", "\u00A7$1");
 	}
 
 	
 	// Returns a Set (List) of Strings.
 	public static Set<String> getTeams() {
 		return teams;
 	}
 
 	public static boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = Probending.plugin.getServer().getServicesManager().getRegistration(Economy.class);
 		Probending.econ = economyProvider.getProvider();
 		return (Probending.econ != null);
 	}
 
 	public static void setWins(int wins, String teamName) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET wins = " + wins + " WHERE team = '" + teamName + "'");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + ".Wins", wins);
 			Probending.plugin.saveConfig();
 		}
 	}
 
 	public static void setLosses(int losses, String teamName) {
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET losses = " + losses + " WHERE team = '" + teamName + "'");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + ".Losses", losses);
 			Probending.plugin.saveConfig();
 		}
 	}
 
 	public static int getWins(String teamName) {
 		int wins = 0;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT wins FROM probending_teams WHERE team = '" + teamName + "'");
 			try {
 				if (rs2.next()) {
 					wins = rs2.getInt("wins");
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			wins = Probending.plugin.getConfig().getInt("TeamInfo." + teamName + ".Wins");
 		}
 		return wins;
 	}
 
 	public static int getLosses(String teamName) {
 		int losses = 0;
 		if (storage.equalsIgnoreCase("mysql")) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT losses FROM probending_teams WHERE team = '" + teamName + "'");
 			try {
 				if (rs2.next()) {
 					losses = rs2.getInt("losses");
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			losses = Probending.plugin.getConfig().getInt("TeamInfo." + teamName + ".Losses");
 		}
 		return losses;
 	}
 
 	public static void addWin(String teamName) {
 		int currentWins = getWins(teamName);
 		int newWins = currentWins + 1;
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET wins = " + newWins + " WHERE team = '" + teamName + "'");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + ".Wins", newWins);
 			Probending.plugin.saveConfig();
 		}
 	}
 
 	public static void addLoss(String teamName) {
 		int currentLosses = getLosses(teamName);
 		int newLosses = currentLosses + 1;
 		if (storage.equalsIgnoreCase("mysql")) {
 			DBConnection.sql.modifyQuery("UPDATE probending_teams SET losses = " + newLosses + " WHERE team = '" + teamName + "'");
 		}
 		if (storage.equalsIgnoreCase("flatfile")) {
 			Probending.plugin.getConfig().set("TeamInfo." + teamName + ".Losses", newLosses);
 			Probending.plugin.saveConfig();
 		}
 	}
 
 	public static int getOnlineTeamSize(String teamName) {
 		int o = 0;
 		for (Player player: Bukkit.getOnlinePlayers()) {
 			String playerName = player.getName();
 			if (player != null) {
 				if (getPlayerTeam(playerName) != null) {
 					if (getPlayerTeam(playerName).equalsIgnoreCase(teamName)) {
 						o++;
 					}
 				}
 			}
 		}
 		return o;
 
 	}
 	public static void importTeams() {
 		Set<String> yamlTeams = Probending.plugin.getConfig().getConfigurationSection("TeamInfo").getKeys(false);
 		for (String team: yamlTeams) {
 			String owner = Probending.plugin.getConfig().getString("TeamInfo." + team + ".Owner");
 			String airbender = Probending.plugin.getConfig().getString("TeamInfo." + team + ".Air");
 			String waterbender = Probending.plugin.getConfig().getString("TeamInfo." + team + ".Water");
 			String earthbender = Probending.plugin.getConfig().getString("TeamInfo." + team + ".Earth");
 			String firebender = Probending.plugin.getConfig().getString("TeamInfo." + team + ".Fire");
 			String chiblocker = Probending.plugin.getConfig().getString("TeamInfo." + team + ".Chi");
 			Integer wins = Probending.plugin.getConfig().getInt("TeamInfo." + team + ".Wins");
 			Integer losses = Probending.plugin.getConfig().getInt("TeamInfo." + team + ".Losses");
 
 			Methods.createTeam(team, owner);
 			if (airbender != null) {
 				Methods.addPlayerToTeam(team, airbender, "Air");
 			}
 			if (waterbender != null) {
 				Methods.addPlayerToTeam(team, waterbender, "Water");
 			}
 			if (earthbender != null) {
 				Methods.addPlayerToTeam(team, earthbender, "Earth");
 			}
 			if (firebender != null) {
 				Methods.addPlayerToTeam(team, firebender, "Fire");
 			}
 			if (chiblocker != null) {
 				Methods.addPlayerToTeam(team, chiblocker, "Chi");
 			}
 
 			if (wins == null) {
 				Methods.setWins(0, team);
 			} else {
 				Methods.setWins(wins, team);
 			}
 
 			if (losses == null) {
 				Methods.setLosses(0, team);
 			} else {
 				Methods.setLosses(losses, team);
 			}
			
			Probending.plugin.getConfig().set("TeamInfo." + team, null);
 
 		}
 	}
 
 }
