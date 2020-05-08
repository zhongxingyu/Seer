 package com.jmeyer.bukkit.jlevel;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.filechooser.FileFilter;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  * Handle calls to SQLite Database
  * @author JMEYER
  * 
  * Special Thanks:
  * - tkelly: suggesting SQLite, offering IRC assistance, providing sample source
  * - thegleek: suggesting SQLite, offering IRC assistance
  */
 public class DatabaseManager {
 	
 	public static final Logger LOG = Logger.getLogger("Minecraft");
 	public static final String ROOT_DIRECTORY = "JLevel-Data";
 	public static final String PLAYER_DIRECTORY = ROOT_DIRECTORY + File.separator + "Players" + File.separator;
 	public static final String SKILL_DIRECTORY = ROOT_DIRECTORY + File.separator + "Skills" + File.separator;
 	public static final String PLAYER_DB_DIRECTORY = "jdbc:sqlite:" + PLAYER_DIRECTORY;
 	public static final String SKILL_DB_DIRECTORY = "jdbc:sqlite:" + SKILL_DIRECTORY;
 	
 	// TODO: getQueryResult(databasePath, get _, where _, equals_);
 	// TODO: getQueryResult(databasePath, query); have 1 return String and others that cast afterwards
 	// TODO: runUpdate(databasePath, update);
 	
 	
 	// TODO: make ignore null fields
 	// TODO: implement clearCurrent, which removes original tables if true
 	// TODO: implement clearCurrent with createSkillDatabaseIfNotExists
 	// Updates, or creates skill if not already created
 	public static void updateSkill(String skill, String[] itemRules, String[] expRules, String[] expTable, boolean clearCurrent) {
 		createSkillDatabaseIfNotExists(skill);
 		
 		// TODO: remove test values and actually parse
 		Connection conn = null;
 		Statement st = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(skillDatabasePath(skill));
 			st = conn.createStatement();
 			
 			// TODO: make addRow() and addItemRulesRow(Skill, _, _);
 			if (itemRules != null) {
 				for (String itemRule : itemRules) {
 					String[] split = itemRule.split(":", 2);
 					String itemRulesUpdate = "INSERT INTO `itemRules` (`itemId`,`level`) VALUES(" + split[0] + "," + split[1] + ");";
 					st.executeUpdate(itemRulesUpdate);
 				}
 			}
 			
 			if (expRules != null) {
 				for (String expRule : expRules) {
 					String[] split = expRule.split(":", 4);
 					String expRulesUpdate = "INSERT INTO `expRules` (`action`,`receiver`,`receiverState`,`exp`) VALUES('" + split[0] + "', '" + split[1] + "', '" + split[2] + "', " + split[3] + ");";
 					st.executeUpdate(expRulesUpdate);
 				}
 			}
 		
 			if (expTable != null) {
 				for (String expRow : expTable) {
 					String[] split = expRow.split(":", 2);
 					String expLevelsUpdate = "INSERT INTO `expLevels` (`level`, `expNeeded`) VALUES(" + split[0] + ", " + split[1] + ");";
 					st.executeUpdate(expLevelsUpdate);
 				}
 			}
 
 		} catch (SQLException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Update Table Exception (Skill: " + skill + ")", e);
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (st != null)
 					st.close();
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Could not update the table (on close) (Skill: " + skill + ")");
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	// ======================================================
 	// Creation methods (Tables, Directories, etc.)
 	// ======================================================
 	
 	public static void createDirectoriesIfNotExists() {
 		File rootDirectory = new File("JLevel-Data");
 		File playerDirectory = new File("JLevel-Data/Players");
 		File skillDirectory = new File("JLevel-Data/Skills");
 		
 		if (!rootDirectory.exists()) {
 			rootDirectory.mkdir();
 		}
 		
 		if (!playerDirectory.exists()) {
 			playerDirectory.mkdir();
 		}
 		
 		if (!skillDirectory.exists()) {
 			skillDirectory.mkdir();
 		}
 	}
 	
 	public static void createPlayerDatabaseIfNotExists(Player player) {
 		if (!playerTableExists(player, false)) {
 			Connection conn = null;
 			Statement st = null;
 			String name = player.getName();
 			try {
 				Class.forName("org.sqlite.JDBC");
 				conn = DriverManager.getConnection(playerDatabasePath(player));
 				st = conn.createStatement();
 				
 				String update = "CREATE TABLE `" + name + "` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`skillName` varchar(32)," +
 					"`skillLevel` INTEGER," +
 					"`levelExp` INTEGER," +
 					"`nextLevelExp` INTEGER," +
 					"`totalExp` INTEGER" + ");";
 				
 				st.executeUpdate(update);
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Create Table Exception (Player: " + name + ")", e);
 			} catch (ClassNotFoundException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 			} finally {
 				try {
 					if (conn != null)
 						conn.close();
 					if (st != null)
 						st.close();
 				} catch (SQLException e) {
 					LOG.log(Level.SEVERE, "[JLEVEL]: Could not create the table (on close) (Player: " + name + ")");
 				}
 			}
 		}
 	}
 	
 	public static void createSkillDatabaseIfNotExists(String skill) { // , String[] itemRules, String[] expRules, String[] expTable) {
 		Connection conn = null;
 		Statement st = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(skillDatabasePath(skill));
 			st = conn.createStatement();
 			
 			if (!itemRulesTableExistsForSkill(skill, false)) {
 				String itemRulesUpdate = "CREATE TABLE `itemRules` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`itemId` INTEGER," +
 					"`level` INTEGER" + ");";
 				st.executeUpdate(itemRulesUpdate);
 			}
 			
 			if (!expRulesTableExistsForSkill(skill, false)) {
 				String expRulesUpdate = "CREATE TABLE `expRules` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`action` varchar(32)," +
 					"`receiver` varchar(32)," + 
 					"`receiverState` varchar(32)," +
 					"`exp` INTEGER" + ");";
 				st.executeUpdate(expRulesUpdate);
 			}
 			
 			if (!expLevelsTableExistsForSkill(skill, false)) {
 				String expLevelsUpdate = "CREATE TABLE `expLevels` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`level` INTEGER," +
 					"`expNeeded` INTEGER" + ");";
 				st.executeUpdate(expLevelsUpdate);
 			}			
 			
 		} catch (SQLException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Create Table Exception (Skill: " + skill + ")", e);
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (st != null)
 					st.close();
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Could not create the table (on close) (Skill: " + skill + ")");
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	
 	// ======================================================
 	// Data check methods (Tables)
 	// ======================================================
 	
 	public static boolean playerCanUseItem(Player player, int itemId) {
 		File root = new File(SKILL_DIRECTORY);
         File[] skillFiles = root.listFiles();
 		boolean canUse = true;
 		
 		for (File file : skillFiles) {
 			String fileName = file.getName();
 			String skill = fileName.substring(0, fileName.indexOf('.'));
 			int reqLevel = requiredLevelForItem(skill, itemId);
 			if (playerSkillLevel(player, skill) < reqLevel) {
 				player.sendMessage("You must be at least level " + reqLevel + " of the " + ChatColor.YELLOW + skill + ChatColor.WHITE + " skill to use this.");
         		canUse = false;
 			}
 		}
 		
 		return canUse;
 	}
 	
 	public static int playerSkillLevel(Player player, String skill) {
 		if (playerTableExists(player, true)) {
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			String name = player.getName();
 			try {
 				Class.forName("org.sqlite.JDBC");
 				conn = DriverManager.getConnection(playerDatabasePath(player));
 				st = conn.createStatement();
 				
 				String query = "SELECT * FROM " + name + " WHERE skillName='" + skill + "' LIMIT 1;";
 				rs = st.executeQuery(query);
 				
 				if (rs.next())
 					return rs.getInt("skillLevel");
 				else
 					return 1;			
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Table Read Exception (Player: " + name + ", Skill: " + skill + ")", e);
 				return 1;
 			} catch (ClassNotFoundException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 				return 1;
 			} finally {
 				try {
 					if (conn != null)
 						conn.close();
 					if (st != null)
 						st.close();
 					if (rs != null)
 						rs.close();
 				} catch (SQLException e) {
 					LOG.log(Level.SEVERE, "[JLEVEL]: Could not read the table (on close) (Player: " + player.getName() + ", Skill: " + skill + ")");
 				}
 			}
 		} else {
 			return 1;
 		}
 	}
 	
 	public static ArrayList<String> relatedSkillsForItem(int itemId) {
 		ArrayList<String> relatedSkills = new ArrayList<String>();
 		File root = new File(SKILL_DIRECTORY);
         File[] skillFiles = root.listFiles();
         
         for (File file : skillFiles) {
 			String fileName = file.getName();
 			String skill = fileName.substring(0, fileName.indexOf('.'));
 			
 			if (itemRelatesToSkill(skill, itemId)) {
         		relatedSkills.add(skill);
         	}
         }
         
 		return relatedSkills;
 	}
 	
 	private static int requiredLevelForItem(String skill, int itemId) {
 		if (itemRulesTableExistsForSkill(skill, true)) {
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				conn = DriverManager.getConnection(skillDatabasePath(skill));
 				st = conn.createStatement();
 				
 				String query = "SELECT * FROM itemRules WHERE itemId=" + itemId + " LIMIT 1;";
 				rs = st.executeQuery(query);
 				
 				if (rs.next())
 					return rs.getInt("level");
 				else
 					return -1;		
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Table Read Exception (Skill: " + skill + ")", e);
 				return -1;
 			} catch (ClassNotFoundException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 				return -1;
 			} finally {
 				try {
 					if (conn != null)
 						conn.close();
 					if (st != null)
 						st.close();
 					if (rs != null)
 						rs.close();
 				} catch (SQLException e) {
 					LOG.log(Level.SEVERE, "[JLEVEL]: Could not read the table (on close) (Skill: " + skill + ")");
 				}
 			}
 		}
 		return -1;
 	}
 	
 	private static boolean itemRelatesToSkill(String skill, int itemId) {
 		if (itemRulesTableExistsForSkill(skill, true)) {
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				conn = DriverManager.getConnection(skillDatabasePath(skill));
 				st = conn.createStatement();
 				
 				String query = "SELECT * FROM itemRules WHERE itemId=" + itemId + ";";
 				rs = st.executeQuery(query);
 				
 				if (!rs.next())
 					return false;
 				else
 					return true;
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Table Read Exception (Skill: " + skill + ")", e);
 				return false;
 			} catch (ClassNotFoundException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 				return false;
 			} finally {
 				try {
 					if (conn != null)
 						conn.close();
 					if (st != null)
 						st.close();
 					if (rs != null)
 						rs.close();
 				} catch (SQLException e) {
 					LOG.log(Level.SEVERE, "[JLEVEL]: Could not read the table (on close) (Skill: " + skill + ")");
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static void addExperience(Player player, String skill, int amount) {
 		String name = player.getName();		
 		String dbPath = playerDatabasePath(player);
 		String condition = "skillName='" + skill + "'";
 		String result = getQueryResult(dbPath, name, skill, condition);
 		
 		// Add skill if not yet learned
 		if (result == null) {
 			// newLines.add("skill:" + skill + ":1:0:" + getSkillExperienceNeededForLevel(skill, 1) + ":0");
 			String update = "INSERT INTO `" + name + "` (`skillName`,`skillLevel`,`levelExp`,`nextLevelExp`,`totalExp`) " + 
 				"VALUES('" + skill + "', 1, 0, " + skillExperienceNeededForLevel(skill, 1) + ", 0);";
 			runUpdate(dbPath, update);
         	player.sendMessage("You learned the " + ChatColor.YELLOW + skill + ChatColor.WHITE + " skill!");
 			
 			return;
 		}
 		
 		// TODO: make more efficient (grab string[] of row values from query)
 		int skillLevel = Integer.parseInt(getQueryResult(dbPath, player.getName(), "skillLevel", condition));
 		int levelExp = Integer.parseInt(getQueryResult(dbPath, player.getName(), "levelExp", condition));
 		int nextLevelExp = Integer.parseInt(getQueryResult(dbPath, player.getName(), "nextLevelExp", condition));
 		int totalExp = Integer.parseInt(getQueryResult(dbPath, player.getName(), "nextLevelExp", condition));
 		
 		// Add exp if not max level
 		if (nextLevelExp > 0) {
 			levelExp += amount;
 			totalExp += amount;
 			player.sendMessage("+(" + amount + ") " + skill);
 		}
 		
 		// Level up if nextLevelExp reached and not max lvl
 		while (levelExp > nextLevelExp && nextLevelExp > 0) {
 			levelExp -= nextLevelExp;
 			skillLevel++;
 			nextLevelExp = skillExperienceNeededForLevel(skill, skillLevel);
 			player.sendMessage("Level up! You are now level " + skillLevel + " of the " + ChatColor.YELLOW + skill + ChatColor.WHITE + " skill.");
 		}
 	}
 	
 	public static int getExperienceGainedFromAction(String skill, String action, String receiver, String receiverState) {
 		String condition = null;
 		String result = null;
 		
 		if (action.equals("blockbreak")) {
 			condition = "action='blockbreak' AND receiver='" + receiver + "'";
 			
 			if (Integer.parseInt(receiverState) >= 0) {
 				condition += " AND (receiverState='" + receiverState + "' OR receiverState='-1')";
 			}
 			
 			System.out.println(condition);
 		} else if (action.equals("monsterkill")) {
 			condition = "action='monsterkill' AND receiver='" + receiver + "'";
 		} else {
 			return 0;
 		}
 		
 		result = getQueryResult(skillDatabasePath(skill), "expRules", "exp", condition);
 		
 		if (result != null)
 			return Integer.parseInt(result);
 		else 
 			return 0;
 	}
 	
 	public static int skillExperienceNeededForLevel(String skill, int level) {
 		String condition = "level=" + level;
		String result = getQueryResult(skillDatabasePath(skill), "expLevels", "exp", condition);
 		
 		if (result != null)
 			return Integer.parseInt(result);
 		else 
 			return -1;
 	}
 	
 	
 	
 	
 	
 	// ======================================================
 	// Data script methods
 	// ======================================================
 	
 	// getQueryResult(databasePath, from _, get _, where _, equals_);
 	// EX Condition: "level=1"
 	public static String getQueryResult(String dbPath, String tableFrom, String itemToGet, String condition) {
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		String query = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(dbPath);
 			st = conn.createStatement();
 			
 			query = "SELECT * FROM " + tableFrom + " WHERE " + condition + " LIMIT 1;";
 			rs = st.executeQuery(query);
 			
 			if (rs.next())
 				return rs.getString(itemToGet);
 			else
 				return null;		
 		} catch (SQLException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Table Read Exception (getResult) \n" + query, e);
 			return null;
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 			return null;
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (st != null)
 					st.close();
 				if (rs != null)
 					rs.close();
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Could not read the table (on close) (getResult) \n" + query);
 			}
 		}
 	}
 	
 	public static void runUpdate(String dbPath, String update) {
 		Connection conn = null;
 		Statement st = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(dbPath);
 			st = conn.createStatement();
 			
 			st.executeUpdate(update);
 		} catch (SQLException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Run Update Exception \n" + update, e);
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (st != null)
 					st.close();
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Could not run update \n" + update);
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	// ======================================================
 	// File check methods (Tables, Directories, etc.)
 	// ======================================================
 	
 	public static boolean playerTableExists(Player player, boolean showError) {
 		if(tableExists(playerDatabasePath(player), player.getName())) {
 			return true;
 		} else {
 			if (showError) {
 				LOG.log(Level.WARNING, "[JLEVEL]: Missing player table for " + player.getName() + ".");
 			}
 			return false;
 		}
 	}
 	
 	public static boolean allTablesExistForSkill(String skill, boolean showError) {
 		return itemRulesTableExistsForSkill(skill, showError) && 
 			expRulesTableExistsForSkill(skill, showError) && 
 			expLevelsTableExistsForSkill(skill, showError);
 	}
 	
 	public static boolean itemRulesTableExistsForSkill(String skill, boolean showError) {
 		if (tableExists(skillDatabasePath(skill), "itemRules")) {
 			return true;
 		} else {
 			if (showError) {
 				LOG.log(Level.WARNING, "[JLEVEL]: Missing itemRules table for " + skill + " skill.");
 			}
 			return false;
 		}
 	}
 	
 	public static boolean expRulesTableExistsForSkill(String skill, boolean showError) {
 		if (tableExists(skillDatabasePath(skill), "expRules")) {
 			return true;
 		} else {
 			if (showError) {
 				LOG.log(Level.WARNING, "[JLEVEL]: Missing expRules table for " + skill + " skill.");
 			}
 			return false;
 		}
 	}
 	
 	public static boolean expLevelsTableExistsForSkill(String skill, boolean showError) {
 		if (tableExists(skillDatabasePath(skill), "expLevels")) {
 			return true;
 		} else {
 			if (showError) {
 				LOG.log(Level.WARNING, "[JLEVEL]: Missing expLevels table for " + skill + " skill.");
 			}
 			return false;
 		}
 	}
 	
 	public static boolean tableExists(String connectionPath, String tableName) {
 		Connection conn = null;
 		ResultSet rs = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(connectionPath);
 			DatabaseMetaData dbm = conn.getMetaData();
 			rs = dbm.getTables(null, null, tableName, null);
 			if (!rs.next())
 				return false;
 			return true;
 		} catch (SQLException ex) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Table Check Exception", ex);
 			return false;
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 			return false;
 		} finally {
 			try {
 				if (rs != null)
 					rs.close();
 				if (conn != null)
 					conn.close();
 			} catch (SQLException ex) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Table Check SQL Exception (on closing)");
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	// ======================================================
 	// Private path methods
 	// ======================================================
 	
 	private static String playerDatabasePath(Player player) {
 		return PLAYER_DB_DIRECTORY + player.getName() + ".db";
 	}
 	
 	private static String skillDatabasePath(String skill) {
 		return SKILL_DB_DIRECTORY + skill + ".db";
 	}
 	
 }
