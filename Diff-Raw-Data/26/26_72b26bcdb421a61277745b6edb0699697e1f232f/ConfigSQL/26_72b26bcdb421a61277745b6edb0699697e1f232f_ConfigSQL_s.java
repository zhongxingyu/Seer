 package net.crystalyx.bukkit.simplyperms.io;
 
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.sql.Connection;
 
 import net.crystalyx.bukkit.simplyperms.SimplyPlugin;
 
 public class ConfigSQL implements PermsConfig {
 
 	private SimplyPlugin plugin;
 	private ConfigFile config;
 	private Connection connection;
 	private String table_players;
 	private String table_groups;
 	private String column_playerid;
 	private String column_groupname;
 	private String column_permission;
 	private String column_world;
 	private String column_value;
 	private String column_date;
 
 	public ConfigSQL(SimplyPlugin plugin) {
 		this.plugin = plugin;
 		config = new ConfigFile(plugin);
 		table_players = plugin.getConfig().getString("db/table/players");
 		table_groups = plugin.getConfig().getString("db/table/groups");
 		column_playerid = plugin.getConfig().getString("db/column/playerid");
 		column_groupname = plugin.getConfig().getString("db/column/groupname");
 		column_permission = plugin.getConfig().getString("db/column/permission");
 		column_world = plugin.getConfig().getString("db/column/world");
 		column_value = plugin.getConfig().getString("db/column/value");
 		column_date = plugin.getConfig().getString("db/column/date");
 	}
 
 	private boolean init() {
 		try {
 			if (connection != null) {
 				connection.close();
 			}
 			connection = DriverManager.getConnection("jdbc:"
 					+ plugin.getConfig().getString("db/type") + "://"
 					+ plugin.getConfig().getString("db/host") + ":"
 					+ plugin.getConfig().getString("db/port") + "/"
 					+ plugin.getConfig().getString("db/database"),
 					plugin.getConfig().getString("db/user"),
 					plugin.getConfig().getString("db/pass"));
 			return true;
 		} catch (SQLException e) {
 			plugin.debug(e.getMessage());
 			return false;
 		}
 	}
 
 	public boolean checkDatabase() {
 		if (init()) {
 			String date = (!column_date.isEmpty()) ? column_date + " DATETIME," : "";
 			try {
 				PreparedStatement sql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table_players + "(" + column_playerid + " VARCHAR(16) NOT NULL, " + column_world + " VARCHAR(30) NOT NULL, " + column_permission + " VARCHAR(100) NOT NULL, " + column_value + " SMALLINT NOT NULL, " + date + " PRIMARY KEY (" + column_playerid + ", " + column_world + ", " + column_permission + "))");
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 				return false;
 			}
 			try {
 				PreparedStatement sql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table_groups + "(" + column_playerid + " VARCHAR(16) NOT NULL, " + column_groupname + " VARCHAR(30) NOT NULL, " + date + " PRIMARY KEY (" + column_playerid + ", " + column_groupname + "))");
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void removePlayer(String player) {
 		removePlayerPermissions(player);
 		removePlayerGroups(player);
 	}
 
 	@Override
 	public void removePlayerGroups(String player) {
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("DELETE FROM " + table_groups + " WHERE " + column_playerid + " = ?");
 				sql.setString(1, player);
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void removePlayerGroup(String player, String group) {
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("DELETE FROM " + table_groups + " WHERE " + column_playerid + " = ? AND " + column_groupname + " = ?");
 				sql.setString(1, player);
 				sql.setString(2, group);
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 	}
 
 	private void addPlayerGroup(String player, String group, boolean first) {
 		if (init()) {
 			try {
 				PreparedStatement sql = connection.prepareStatement("INSERT INTO " + table_groups + "(" + column_playerid + ", " + column_groupname + ((!column_date.isEmpty()) ? ", " + column_date : "") + ") VALUES(?, ?" + ((!column_date.isEmpty()) ? ", NOW()" : "") + ")");
 				sql.setString(1, player);
 				sql.setString(2, group);
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				if (first) {
 					removePlayerGroup(player, group);
 					addPlayerGroup(player, group, false);
 				}
 				else {
 					plugin.debug(e.getMessage());
 				}
 			}
 		}
 	}
 
 	@Override
 	public void addPlayerGroup(String player, String group) {
 		addPlayerGroup(player, group, true);
 	}
 
 	private void addPlayerPermission(String player, String world, String permission, boolean value, boolean first) {
 		if (init()) {
 			try {
 				PreparedStatement sql = connection.prepareStatement("INSERT INTO " + table_players + "(" + column_playerid + ", " + column_world + ", " + column_permission + ", " + column_value + ((!column_date.isEmpty()) ? ", " + column_date : "") + ") VALUES(?, ?, ?, ?" + ((!column_date.isEmpty()) ? ", NOW()" : "") + ")");
 				sql.setString(1, player);
 				sql.setString(2, world);
 				sql.setString(3, permission);
 				sql.setBoolean(4, value);
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				if (first) {
 					removePlayerPermission(player, world, permission);
 					addPlayerPermission(player, world, permission, value, false);
 				}
 				else {
 					plugin.debug(e.getMessage());
 				}
 			}
 		}
 	}
 
 	@Override
 	public void addPlayerPermission(String player, String permission, boolean value) {
 		addPlayerPermission(player, "", permission, value);
 	}
 
 	@Override
 	public void addPlayerPermission(String player, String world, String permission, boolean value) {
 		addPlayerPermission(player, world, permission, value, true);
 	}
 
 	@Override
 	public void removePlayerPermissions(String player) {
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("DELETE FROM " + table_players + " WHERE " + column_playerid + " = ?");
 				sql.setString(1, player);
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void removePlayerPermission(String player, String permission) {
 		removePlayerPermission(player, "", permission);
 	}
 
 	@Override
 	public void removePlayerPermission(String player, String world, String permission) {
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("DELETE FROM " + table_players + " WHERE " + column_playerid + " = ? AND " + column_world + " = ? AND " + column_permission + " = ?");
 				sql.setString(1, player);
 				sql.setString(2, world);
 				sql.setString(3, permission);
 				sql.executeUpdate();
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public List<String> getPlayers(String group) {
 		List<String> results = new ArrayList<String>();
 		if (init()) {
 			try {
 				PreparedStatement sql = connection.prepareStatement("SELECT " + column_playerid + " FROM " + table_groups + " WHERE " + column_groupname + " = ?");
 				sql.setString(1, group);
 				ResultSet players = sql.executeQuery();
 				while (players.next()) {
 					results.add(players.getString(column_playerid));
 				}
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 		return results;
 	}
 
 	@Override
 	public List<String> getPlayerGroups(String player) {
 		List<String> results = new ArrayList<String>();
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("SELECT " + column_groupname + " FROM " + table_groups + " WHERE " + column_playerid + " = ?");
 				sql.setString(1, player);
 				ResultSet players = sql.executeQuery();
 				while (players.next()) {
 					results.add(players.getString(column_groupname));
 				}
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 		return results;
 	}
 
 	@Override
 	public Map<String, Boolean> getPlayerPermissions(String player) {
 		return getPlayerPermissions(player, "");
 	}
 
 	@Override
 	public Map<String, Boolean> getPlayerPermissions(String player, String world) {
 		Map<String, Boolean> results = new HashMap<String, Boolean>();
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("SELECT " + column_permission + ", " + column_value + " FROM " + table_players + " WHERE " + column_playerid + " = ? AND " + column_world + " = ?");
 				sql.setString(1, player);
 				sql.setString(2, world);
 				ResultSet permissions = sql.executeQuery();
 				while (permissions.next()) {
 					results.put(permissions.getString(column_permission), permissions.getBoolean(column_value));
 				}
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 		return results;
 	}
 
 	@Override
 	public boolean isPlayerInDB(String player) {
 		if (init()) {
 			try {
 				int count = 0;
				PreparedStatement sql = connection.prepareStatement("SELECT COUNT(" + column_playerid + ") FROM " + table_groups + " WHERE " + column_playerid + " = ?");
 				sql.setString(1, player);
 				ResultSet results = sql.executeQuery();
 				if (results.next()) {
 					count += results.getInt(1);
 				}
				sql = connection.prepareStatement("SELECT COUNT(" + column_playerid + ") FROM " + table_players + " WHERE " + column_playerid + " = ?");
 				sql.setString(1, player);
 				results = sql.executeQuery();
 				if (results.next()) {
 					count += results.getInt(1);
 				}
 				return count > 0;
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 				return false;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public List<String> getPlayerWorlds(String player) {
 		List<String> results = new ArrayList<String>();
 		if (init()) {
 			try {
				PreparedStatement sql = connection.prepareStatement("SELECT " + column_world + " FROM " + table_players + " WHERE " + column_playerid + " = ?");
 				sql.setString(1, player);
 				ResultSet players = sql.executeQuery();
 				while (players.next()) {
 					results.add(players.getString(column_world));
 				}
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 		return results;
 	}
 
 	@Override
 	public List<String> getAllPlayers() {
 		List<String> results = new ArrayList<String>();
 		if (init()) {
 			try {
 				PreparedStatement sql = connection.prepareStatement("SELECT " + column_playerid + " FROM " + table_groups + " GROUP BY " + column_playerid);
 				ResultSet players = sql.executeQuery();
 				while (players.next()) {
 					results.add(players.getString(column_playerid));
 				}
 				sql = connection.prepareStatement("SELECT " + column_playerid + " FROM " + table_players + " GROUP BY " + column_playerid);
 				players = sql.executeQuery();
 				while (players.next()) {
 					if (!results.contains(players.getString(column_playerid))) {
 						results.add(players.getString(column_playerid));
 					}
 				}
 			} catch (SQLException e) {
 				plugin.debug(e.getMessage());
 			}
 		}
 		return results;
 	}
 
 	@Override
 	public List<String> getAllGroups() {
 		return config.getAllGroups();
 	}
 
 	@Override
 	public List<String> getGroupWorlds(String group) {
 		return config.getGroupWorlds(group);
 	}
 
 	@Override
 	public List<String> getGroupInheritance(String group) {
 		return config.getGroupInheritance(group);
 	}
 
 	@Override
 	public void addGroupInheritance(String group, String inherit) {
 		config.addGroupInheritance(group, inherit);
 	}
 
 	@Override
 	public void removeGroupInheritance(String group, String inherit) {
 		config.removeGroupInheritance(group, inherit);
 	}
 
 	@Override
 	public void removeGroupInheritances(String group) {
 		config.removeGroupInheritances(group);
 	}
 
 	@Override
 	public Map<String, Boolean> getGroupPermissions(String group, String world) {
 		return config.getGroupPermissions(group, world);
 	}
 
 	@Override
 	public Map<String, Boolean> getGroupPermissions(String group) {
 		return config.getGroupPermissions(group);
 	}
 
 	@Override
 	public void addGroupPermission(String group, String world, String permission, boolean value) {
 		config.addGroupPermission(group, world, permission, value);
 	}
 
 	@Override
 	public void addGroupPermission(String group, String permission, boolean value) {
 		config.addGroupPermission(group, permission, value);
 	}
 
 	@Override
 	public void removeGroupPermission(String group, String world, String permission) {
 		config.removeGroupPermission(group, world, permission);
 	}
 
 	@Override
 	public void removeGroupPermission(String group, String permission) {
 		config.removeGroupPermission(group, permission);
 	}
 
 	@Override
 	public void removeGroupPermissions(String group) {
 		config.removeGroupPermissions(group);
 	}
 
 	@Override
 	public void removeGroup(String group) {
 		config.removeGroup(group);
 	}
 
 	@Override
 	public Map<String, Object> getMessages() {
 		return config.getMessages();
 	}
 
 	@Override
 	public void addMessage(String key, String message) {
 		config.addMessage(key, message);
 	}
 
 	@Override
 	public void removeMessage(String key) {
 		config.removeMessage(key);
 	}
 
 	@Override
 	public String getDefaultGroup() {
 		return config.getDefaultGroup();
 	}
 
 	@Override
 	public void setDefaultGroup(String group) {
 		config.setDefaultGroup(group);
 	}
 
 }
