 package com.smartaleq.bukkit.dwarfcraft;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 import org.bukkit.entity.Vehicle;
 
 import redecouverte.npcspawner.NpcSpawner;
 
 class DataManager {
 
 	private List<Dwarf> dwarves = new ArrayList<Dwarf>();
 	private List<DwarfVehicle> vehicleList = new ArrayList<DwarfVehicle>();
 	public HashMap<String, DwarfTrainer> trainerList = new HashMap<String, DwarfTrainer>();
 	private HashMap<String, GreeterMessage> greeterMessageList = new HashMap<String, GreeterMessage>();
 	private final ConfigManager configManager;
 	private final DwarfCraft plugin;
 
 	protected DataManager(DwarfCraft plugin, ConfigManager cm) {
 		this.plugin = plugin;
 		this.configManager = cm;
 		dbInitialize();
 		for (Iterator<World> i = plugin.getServer().getWorlds().iterator(); i
 				.hasNext();) {
 			World w = i.next();
 			populateTrainers(w);
 		}
		// readGreeterMessagesfile() depends on datamanager existing, so this
		// has to go here rather than in the constructor for configmanager,
		// which gets instantiated first
		if (!plugin.getConfigManager().readGreeterMessagesfile()) {
			System.out
					.println("[SEVERE] Failed to Enable DwarfCraft Skills and Effects)");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			// TODO failed to init skills
		}
 	}
 
 	protected void addVehicle(DwarfVehicle v) {
 		vehicleList.add(v);
 	}
 
 	/**
 	 * this is untested and quite a lot of new code, it will probably fail
 	 * several times. no way to bugfix currently. Just praying it works - joey
 	 * 
 	 * @param oldVersion
 	 */
 	private void buildDB(int oldVersion) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			ResultSet rs1;
 			ResultSet rs2;
 			ResultSet rs3;
 			// if no trainer table, create it
 			rs1 = statement
 					.executeQuery("select * from sqlite_master WHERE name = 'trainers';");
 			rs1.next();
 			// SCHEMA(world,uniqueId,name,skill,maxSkill,material,isGreeter,messageId,x,y,z,yaw,pitch)
 			if (rs1.isClosed())
 				statement
 						.executeUpdate("create table trainers (world,uniqueId,name,skill,maxSkill,material,isGreeter,messageId,x,y,z,yaw,pitch);");
 			rs1.close();
 			// Create the new table based on current version of skills file
 			String skillTableCreater = "";
 			for (Skill s : configManager.getAllSkills())
 				skillTableCreater = skillTableCreater
 						.concat("," + s.toString());
 			String tableCreateSQL = "create table dwarfs"
 					+ configManager.getConfigSkillsVersion()
 					+ " (playername,iself" + skillTableCreater + ");";
 			statement.executeUpdate(tableCreateSQL);
 			// Update this new table with old data if old data exists
 			if (oldVersion == 0) {
 				conn.close();
 				return;
 			}
 			rs2 = statement
 					.executeQuery("select sql from sqlite_master WHERE name='dwarfs"
 							+ oldVersion + "';");
 			rs2.next();
 			String schema = rs2.getString(1);
 			rs2.close();
 
 			rs3 = statement.executeQuery("select * from dwarfs" + oldVersion);
 
 			String sqlLine1 = "insert into dwarfs"
 					+ configManager.getConfigSkillsVersion()
 					+ " (playername, iself";
 			String sqlLine2 = ") values (?,?";
 
 			List<Skill> tempSkills = configManager.getAllSkills();
 			for (Skill s : tempSkills) {
 				if (schema.contains(s.toString())) {
 					s.setLevel(1);
 				}
 				sqlLine1 = sqlLine1.concat("," + s.toString());
 				sqlLine2 = sqlLine2.concat(",?");
 			}
 			sqlLine2 = sqlLine2.concat(");");
 			PreparedStatement prep = conn.prepareStatement(sqlLine1 + sqlLine2);
 			while (rs3.next()) {
 				prep.setString(1, rs3.getString("playername"));
 				prep.setString(2, rs3.getString("iself"));
 				int i = 3;
 				for (Skill s : tempSkills) {
 					if (s.getLevel() == 1) {
 						prep.setInt(i,
 								Integer.parseInt(rs3.getString(s.toString())));
 					} else
 						prep.setInt(i, 0);
 					i++;
 				}
 				prep.addBatch();
 			}
 			rs3.close();
 			prep.executeBatch();
 			conn.close();
 		} catch (SQLException e) {
 			System.out.println("[SEVERE]DB not built successfully");
 			e.printStackTrace();
 			plugin.getServer().getPluginManager().disablePlugin(plugin);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected boolean checkTrainersInChunk(Chunk chunk) {
 		for (Iterator<Map.Entry<String, DwarfTrainer>> i = trainerList
 				.entrySet().iterator(); i.hasNext();) {
 			Map.Entry<String, DwarfTrainer> pairs = i.next();
 			DwarfTrainer d = (pairs.getValue());
 			if (Math.abs(chunk.getX()
 					- d.getLocation().getBlock().getChunk().getX()) > 1)
 				continue;
 			if (Math.abs(chunk.getZ()
 					- d.getLocation().getBlock().getChunk().getZ()) > 1)
 				continue;
 			return true;
 		}
 		return false;
 	}
 
 	protected Dwarf createDwarf(Player player) {
 		Dwarf newDwarf = new Dwarf(plugin, player);
 		newDwarf.setSkills(plugin.getConfigManager().getAllSkills());
 		for (Skill skill : newDwarf.getSkills())
 			if (skill != null)
 				skill.setLevel(0);
 		if (player != null)
 			dwarves.add(newDwarf);
 		return newDwarf;
 	}
 
 	protected void createDwarfData(Dwarf dwarf) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			String sql = "insert into dwarfs"
 					+ configManager.getConfigSkillsVersion()
 					+ " (playername, iself";
 			List<Skill> allSkills = configManager.getAllSkills();
 			for (Skill skill : allSkills) {
 				if (skill != null)
 					sql = sql.concat("," + skill.toString());
 			}
 			sql = sql.concat(") values (");
 			sql = sql.concat("'" + dwarf.getPlayer().getName() + "'," + "'"
 					+ dwarf.isElf() + "'");
 			for (Skill skill : allSkills) {
 				if (skill != null)
 					sql = sql.concat("," + skill.getLevel());
 			}
 			sql = sql.concat(");");
 			statement.executeUpdate(sql);
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void dbInitialize() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			ResultSet rs;
 			rs = statement
 					.executeQuery("select * from sqlite_master WHERE name = 'dwarfs"
 							+ configManager.getConfigSkillsVersion() + "';");
 			rs.next();
 			if (rs.isClosed()) { // if current version table doesn't exist
 				for (int versionNumber = configManager.getConfigSkillsVersion() - 1; versionNumber >= 100; versionNumber--) { // a
 																																// is
 																																// number
 																																// of
 																																// past
 																																// versions
 																																// to
 																																// look
 																																// for
 					rs = statement
 							.executeQuery("select * from sqlite_master WHERE name = 'dwarfs"
 									+ versionNumber + "';");
 					rs.next();
 					if (!rs.isClosed()) { // if there is a recent past table,
 											// use it to build the new table
 						conn.close();
 						buildDB(versionNumber);
 						return;
 					}
 				}
 				conn.close();
 				buildDB(0); // if there are no recent past tables, build a new
 							// db from scratch
 			}
 			conn.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 			// total failure
 		}
 	}
 
 	/**
 	 * Finds a dwarf from the server's static list based on player's name
 	 * 
 	 * @param player
 	 * @return dwarf or null
 	 */
 	protected Dwarf find(Player player) {
 		for (Dwarf d : plugin.getDataManager().getDwarves()) {
 			if (d != null)
 				if (d.getPlayer() != null)
 					if (d.getPlayer().getName()
 							.equalsIgnoreCase(player.getName()))
 						return d;
 		}
 		return null;
 	}
 
 	protected Dwarf findOffline(String name) {
 		Dwarf dwarf = createDwarf(null);
 		if (getDwarfData(dwarf, name))
 			return dwarf;
 		else {
 			// No dwarf or data found
 			return null;
 		}
 	}
 
 	protected boolean getDwarfData(Dwarf dwarf) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			// Unsanitized because no one has the player name Robert' Drop Table
 			// dwarfs;
 			String query = "select * from dwarfs"
 					+ configManager.getConfigSkillsVersion()
 					+ " WHERE playername = '" + dwarf.getPlayer().getName()
 					+ "';";
 			ResultSet rs = statement.executeQuery(query);
 			rs.next();
 			if (rs.isClosed())
 				return false;
 			System.out.println("DC: PlayerJoin success for "
 					+ dwarf.getPlayer().getName());
 			dwarf.setElf(rs.getBoolean("iself"));
 			for (Skill skill : dwarf.getSkills()) {
 				if (skill != null)
 					skill.setLevel(rs.getInt(skill.toString()));
 			}
 			rs.close();
 			conn.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Used for creating and populating a dwarf with a null(offline) player
 	 * 
 	 * @param dwarf
 	 * @param name
 	 */
 	private boolean getDwarfData(Dwarf dwarf, String name) {
 		try {
 			String sanitizedName;
 			sanitizedName = Util.sanitize(name);
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			ResultSet rs = statement.executeQuery("select * from dwarfs"
 					+ configManager.getConfigSkillsVersion()
 					+ " where playername='" + sanitizedName + "';");
 			if (rs == null) {
 				conn.close();
 				return false;
 			}
 			rs.next();
 			if (rs.isClosed()) {
 				conn.close();
 				return false;
 			}
 			dwarf.setElf(rs.getBoolean("iself"));
 			for (Skill skill : dwarf.getSkills()) {
 				if (skill != null)
 					skill.setLevel(rs.getInt(skill.toString()));
 			}
 			rs.close();
 			conn.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Deprecated
 	// TODO: remove this and replace by other stuff
 	protected List<Dwarf> getDwarves() {
 		return dwarves;
 	}
 
 	protected GreeterMessage getGreeterMessage(String messageId) {
 		System.out.println(messageId);
 		return greeterMessageList.get(messageId);
 	}
 
 	protected DwarfTrainer getTrainer(Entity entity) {
 		// kind of ugly, could replace this with a hashmap, but i dont think the
 		// perf. gains will be very significant
 		for (Iterator<Map.Entry<String, DwarfTrainer>> i = trainerList
 				.entrySet().iterator(); i.hasNext();) {
 			Map.Entry<String, DwarfTrainer> pairs = i.next();
 			DwarfTrainer d = (pairs.getValue());
 			if (d.getBasicHumanNpc().getBukkitEntity().getEntityId() == entity
 					.getEntityId())
 				return d;
 		}
 		return null;
 	}
 
 	protected DwarfTrainer getTrainer(String str) {
 		return (trainerList.get(str)); // can return null
 	}
 
 	protected DwarfVehicle getVehicle(Vehicle v) {
 		for (DwarfVehicle i : vehicleList) {
 			if (i.equals(v)) {
 				return i;
 			}
 		}
 		return null;
 	}
 
 	protected void insertGreeterMessage(String messageId,
 			GreeterMessage greeterMessage) {
 		try {
 			greeterMessageList.put(messageId, greeterMessage);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void insertTrainer(DwarfTrainer d) {
 		assert (d != null);
 		trainerList.put(d.getUniqueId(), d);
 		// SCHEMA(world,uniqueId,name,skill,maxSkill,material,isGreeter,messageId,x,y,z,yaw,pitch)
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			PreparedStatement prep = conn
 					.prepareStatement("insert into trainers values (?,?,?,?,?,?,?,?,?,?,?,?,?);");
 			prep.setString(1, d.getWorld().getName());
 			prep.setString(2, d.getUniqueId());
 			prep.setString(3, d.getName());
 			if (!d.isGreeter()) {
 				prep.setInt(4, d.getSkillTrained());
 				prep.setInt(5, d.getMaxSkill());
 			} else {
 				prep.setInt(4, 0);
 				prep.setInt(5, 0);
 			}
 			prep.setInt(6, d.getMaterial());
 			prep.setBoolean(7, d.isGreeter());
 			prep.setString(8, d.getMessage());
 			prep.setDouble(9, d.getLocation().getX());
 			prep.setDouble(10, d.getLocation().getY());
 			prep.setDouble(11, d.getLocation().getZ());
 			prep.setFloat(12, d.getLocation().getYaw());
 			prep.setFloat(13, d.getLocation().getPitch());
 			if (DwarfCraft.debugMessagesThreshold < 7)
 				System.out.println("Debug Message Added trainer"
 						+ d.getUniqueId() + " in world: "
 						+ d.getWorld().getName());
 			prep.addBatch();
 			conn.setAutoCommit(false);
 			prep.executeBatch();
 			conn.setAutoCommit(true);
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 
 	private HashMap<String, DwarfTrainer> populateTrainers(World world) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			String query = "select * from trainers Where world='"
 					+ world.getName() + "';";
 			ResultSet rs = statement.executeQuery(query);
 			while (rs.next()) {
 				// DB SCHEMA
 				// (world,uniqueId,name,skill,maxSkill,material,isGreeter,messageId,x,y,z,yaw,pitch)
 				if (world.getName().equals(rs.getString("world"))) {
 					// create trainer in this world
 					// if (DwarfCraft.debugMessagesThreshold < 7)
 					if (DwarfCraft.debugMessagesThreshold < 5)
 						System.out.println("DC5: trainer:"
 								+ rs.getString("name") + " in world: "
 								+ world.getName());
 					DwarfTrainer trainer = new DwarfTrainer(plugin, world,
 							rs.getString("uniqueId"), rs.getString("name"),
 							rs.getInt("skill"), rs.getInt("maxSkill"),
 							Material.getMaterial(rs.getInt("material")),
 							rs.getBoolean("isGreeter"),
 							rs.getString("messageId"), rs.getDouble("x"),
 							rs.getDouble("y"), rs.getDouble("z"),
 							rs.getFloat("yaw"), rs.getFloat("pitch"));
 					trainerList.put(rs.getString("uniqueId"), trainer);
 				}
 			}
 			rs.close();
 			statement.close();
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return trainerList;
 	}
 
 	protected void removeDwarf(Dwarf dwarf) {
 		// TODO removedwarf
 
 	}
 
 	protected void removeTrainer(String str) {
 		DwarfTrainer d;
 		d = trainerList.remove(str);
 		NpcSpawner.RemoveBasicHumanNpc(d.getBasicHumanNpc());
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			statement.execute("delete from trainers where uniqueId='"
 					+ d.getUniqueId() + "';");
 			statement.close();
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void removeVehicle(Vehicle v) {
 		for (DwarfVehicle i : vehicleList) {
 			if (i.equals(v)) {
 				plugin.getDataManager().vehicleList.remove(i);
 				if (DwarfCraft.debugMessagesThreshold < 5)
 					System.out
 							.println("DC5:Removed DwarfVehicle from vehicleList");
 			}
 		}
 	}
 
 	protected boolean saveDwarfData(Dwarf dwarf) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ configManager.getDbPath());
 			Statement statement = conn.createStatement();
 			String sqlsend = "UPDATE dwarfs"
 					+ configManager.getConfigSkillsVersion() + " SET iself='"
 					+ dwarf.isElf() + "', ";
 			for (Skill skill : dwarf.getSkills())
 				if (skill != null)
 					sqlsend = sqlsend.concat(skill.toString() + "="
 							+ skill.getLevel() + ", ");
 			sqlsend = sqlsend.substring(0, sqlsend.length() - 2)
 					+ " WHERE playername = '" + dwarf.getPlayer().getName()
 					+ "';";
 			statement.execute(sqlsend);
 			conn.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private void updateDwarfsTable() {
 		// TODO when table is not right size, resize it
 		// add columns or:
 
 		// remove columns
 	}
 
 }
