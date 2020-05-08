 package de.hydrox.bukkit.DroxPerms.data.sql;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import de.hydrox.bukkit.DroxPerms.data.AGroup;
 import de.hydrox.bukkit.DroxPerms.data.Config;
 
 public class SQLGroup extends AGroup {
 
 	private static String global = "globalpermissions";
 	private SQLPermissions provider= null;
 	
 	private int ID;
 	
 	public SQLGroup(int ID, String name, SQLPermissions provider) {
 		this.ID = ID;
 		this.name = name;
 		this.provider = provider;
 //		provider.logger.info("Groupname: " + name + " ID: " + ID);
 		updatePermissions();
 	}
 
 	@Override
 	public Map<String, Map<String, Boolean>> getPermissions(String world) {
 		Map<String, Map<String, Boolean>> result = new HashMap<String, Map<String, Boolean>>();
 		Map<String, Boolean> groupperms = new LinkedHashMap<String, Boolean>();
 		//add group permissions
 		String group = getName();
 		groupperms.put("droxperms.meta.group." + group, true);
 		if (world != null) {
 			groupperms.put("droxperms.meta.group." + group + "." + Config.getRealWorld(world), true);
 		}
 		result.put("group", groupperms);
 		//add subgroup permissions
 		List<String> subgroups = getSubgroups();
 		if (subgroups.size() > 0) {
 			Map<String, Boolean> subgroupperms = new LinkedHashMap<String, Boolean>();
 			for (String subgroup : subgroups) {
 				subgroupperms.put("droxperms.meta.group." + subgroup, true);
 				if (world != null) {
 					subgroupperms.put("droxperms.meta.group." + subgroup + "." + Config.getRealWorld(world), true);
 				}
 			}
 			result.put("subgroups", subgroupperms);
 		}
 		//add global permissions
 		Map<String, Boolean> globalPermissions = getWorldPermissions(global);
 		result.put("global", globalPermissions);
 
 		//add world permissions
 		if (world != null) {
 			Map<String, Boolean> worldperms = getWorldPermissions(Config.getRealWorld(world));
 			result.put("world", worldperms);
 		}
 		return result;
 	}
 
 	@Override
 	public boolean addPermission(String world, String permission) {
 		if (world == null) {
 			world = global;
 		} else {
 			world = Config.getRealWorld(world).toLowerCase();
 		}
 		permission = permission.toLowerCase();
 
 		boolean value = true;
 		if (permission.startsWith("-")) {
 			permission = permission.substring(1);
 			value = false;
 		}
 		PreparedStatement prep = provider.prepAddGroupPermission;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, world);
 			prep.setString(3, permission);
 			prep.setBoolean(4, value);
 			prep.setBoolean(5, value);
 			prep.executeUpdate();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		}
 		updatePermissions();
 		return true;
 	}
 
 	@Override
 	public boolean removePermission(String world, String permission) {
 		if (world == null) {
 			world = global;
 		} else {
 			world = Config.getRealWorld(world).toLowerCase();
 		}
 		
 		PreparedStatement prep = provider.prepRemoveGroupPermission;
 		int num=0;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, world);
 			prep.setString(3, permission);
 			num = prep.executeUpdate();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		} 
 		if (num==1) {
 			updatePermissions();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean addSubgroup(String subgroup) {
 		PreparedStatement prep = provider.prepAddGroupSubgroup;
 		int num=0;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, subgroup);
 			num = prep.executeUpdate();
 		} catch (SQLException e) {
 			if (e.getErrorCode()!=1062) {
 				SQLPermissions.mysqlError(e);
 			}
 		} 
 		if (num==1) {
 			updatePermissions();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean removeSubgroup(String subgroup) {
 		PreparedStatement prep = provider.prepRemoveGroupSubgroup;
 		int num=0;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, subgroup);
 			num = prep.executeUpdate();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		} 
 		if (num==1) {
 			updatePermissions();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasPermission(String world, String permission) {
 		if (world == null) {
 			world = global;
 		} else {
 			world = Config.getRealWorld(world).toLowerCase();
 		}
 		permission = permission.toLowerCase();
 		Map<String, Boolean> perms = getWorldPermissions(world);
 		if (perms.containsKey(permission)) {
 			return perms.get(permission);
 		}
 
 		for (String subgroup : getSubgroups()) {
 			if (SQLGroup.getGroup(subgroup) != null &&
 					SQLGroup.getGroup(subgroup).hasPermission(world.toLowerCase(), permission)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean setInfo(String node, String data) {
 		PreparedStatement prep = provider.prepAddGroupInfoNode;
 		int num = 0;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, node);
 			prep.setString(3, data);
 			prep.setString(4, data);
 			num = prep.executeUpdate();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
			return false;
 		} 
		return true;
 	}
 
 	@Override
 	public String getInfo(String node) {
 		String result = null;
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetGroupInfoNode;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, node);
 			ResultSet rs = prep.executeQuery();
 			rs.last(); int numrows = rs.getRow(); rs.beforeFirst();
 			if(numrows == 1) {
 				rs.next();
 //				provider.logger.info("NODE: " + node + " DATA: " + rs.getString(1));
 				result = rs.getString(1);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		}
 		return result;
 	}
 
 	@Override
 	public Map<String, String> getInfoComplete() {
 		HashMap<String, String> result = new HashMap<String, String>();
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetGroupInfoComplete;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			ResultSet rs = prep.executeQuery();
 //			rs.last(); int numrows = rs.getRow(); rs.beforeFirst();
 //			provider.logger.info("ROWS: " + numrows);
 			while(rs.next()) {
 
 //				provider.logger.info("NODE: " + rs.getString(1) + " DATA: " + rs.getString(2));
 				result.put(rs.getString(1), rs.getString(2));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		}
 		return result;
 	}
 
 	@Override
 	public List<String> getSubgroups() {
 		List<String> list = new ArrayList<String>();
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetGroupSubgroups;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			ResultSet rs = prep.executeQuery();
 //			rs.last(); int numrows = rs.getRow(); rs.beforeFirst();
 //			provider.logger.info("ROWS: " + numrows);
 			while(rs.next()){
 				String adding = rs.getString(1);
 				list.add(adding);
 //				provider.logger.info("SUBGROUP FOR GROUP " + name + ": " + adding);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			provider.checkConnection();
 			SQLPermissions.mysqlError(e);
 		}
 		return list;
 	}
 
 	private Map<String, Boolean> getWorldPermissions(String world) {
 		HashMap<String, Boolean> result = new HashMap<String, Boolean>();
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetGroupPermissions;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			prep.setString(2, world);
 			ResultSet rs = prep.executeQuery();
 //			rs.last(); int numrows = rs.getRow(); rs.beforeFirst();
 //			provider.logger.info("ROWS: " + numrows);
 			while(rs.next()) {
 //				provider.logger.info("PERMISSION: " + rs.getString(1) + " VALUE: " + rs.getBoolean(2));
 				result.put(rs.getString(1), rs.getBoolean(2));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		}
 		return result;
 	}
 
 	public int getID() {
 		return ID;
 	}
 
 	private void updatePermissions() {
 		bukkitPermissions = new LinkedHashMap<String, Permission>();
 		List<String> subgroups = getSubgroups();
 		//create Permission for default world
 		{
 			Map<String, Boolean> children = new LinkedHashMap<String, Boolean>();
 			for (String subgroup : subgroups) {
 				children.put("droxperms.meta.group." + subgroup + "." + Config.getDefaultWorld(), true);
 			}
 			children.put("droxperms.meta.group." + name, true);
 
 			Permission permission = new Permission("droxperms.meta.group." + name + "." + Config.getDefaultWorld(), "Group-Permissions for group " + name + " on world " + Config.getDefaultWorld(), PermissionDefault.FALSE, children);
 			Bukkit.getPluginManager().removePermission(permission);
 			Bukkit.getPluginManager().addPermission(permission);
 			bukkitPermissions.put(Config.getDefaultWorld(), permission);
 		}
 
 		//create Permissions for other worlds
 		for (String world : Config.getWorlds()) {
 			Map<String, Boolean> children = new LinkedHashMap<String, Boolean>();
 			for (String subgroup : subgroups) {
 				children.put("droxperms.meta.group." + subgroup + "." + world, true);
 			}
 
 			children.put("droxperms.meta.group." + name, true);
 
 			children.putAll(getWorldPermissions(world));
 
 			Permission permission = new Permission("droxperms.meta.group." + name + "." + world, "Group-Permissions for group " + name + " on world " + world, PermissionDefault.FALSE, children);
 			Bukkit.getPluginManager().removePermission(permission);
 			Bukkit.getPluginManager().addPermission(permission);
 			bukkitPermissions.put(world, permission);
 		}
 
 		Map<String, Boolean> children = new LinkedHashMap<String, Boolean>();
 		for (String subgroup : subgroups) {
 			children.put("droxperms.meta.group." + subgroup, true);
 		}
 
 		children.putAll(getWorldPermissions(global));
 
 		//create Permission for global grouppermissions
 		Permission permission = new Permission("droxperms.meta.group." + name, "Group-Permissions for group " + name, PermissionDefault.FALSE, children);
 		Bukkit.getPluginManager().removePermission(permission);
 		Bukkit.getPluginManager().addPermission(permission);
 	}
 }
