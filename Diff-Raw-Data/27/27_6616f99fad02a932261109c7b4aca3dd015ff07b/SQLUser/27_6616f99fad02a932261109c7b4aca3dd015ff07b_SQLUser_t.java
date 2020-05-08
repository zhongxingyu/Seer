 package de.hydrox.bukkit.DroxPerms.data.sql;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.hydrox.bukkit.DroxPerms.data.AUser;
 import de.hydrox.bukkit.DroxPerms.data.Config;
 
 public class SQLUser extends AUser {
 	
 	private static String global = "globalpermissions";
 	private SQLPermissions provider= null;
 	
 	private int ID;
 	private String name;
 	
 	public SQLUser(int ID, String name, SQLPermissions provider) {
 		this.ID = ID;
 		this.name = name;
 		this.provider = provider;
 //		provider.logger.info("Username: " + name + " ID: " + ID);
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
 		PreparedStatement prep = provider.prepAddUserPermission;
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
 		return true;
 	}
 
 	@Override
 	public boolean addSubgroup(String subgroup) {
 		PreparedStatement prep = provider.prepAddUserSubgroup;
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
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String getGroup() {
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetUserGroup;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			ResultSet rs = prep.executeQuery();
 			rs.last(); int numrows = rs.getRow(); rs.beforeFirst();
 //			provider.logger.info("ROWS: " + numrows);
 			if(numrows == 1) {
 				rs.next();
 				
 				String adding = rs.getString(1); 
 //				provider.logger.info("GROUPNAME: " + adding);
 				rs.close();
 				return adding;
 			}
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
 		} 
 		return null;
 	}
 
 	@Override
 	public String getInfo(String node) {
 		String result = null;
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetUserInfoNode;
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
 		PreparedStatement prep = provider.prepGetUserInfoComplete;
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
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public Map<String, Map<String, Boolean>> getPermissions(String world) {
 		Map<String, Map<String, Boolean>> result = new HashMap<String, Map<String, Boolean>>();
 		Map<String, Boolean> groupperms = new LinkedHashMap<String, Boolean>();
 		//add group permissions
 		String group = getGroup();
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
 	public List<String> getSubgroups() {
 		List<String> list = new ArrayList<String>();
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetUserSubgroups;
 		try {
 			prep.clearParameters();
 			prep.setInt(1, ID);
 			ResultSet rs = prep.executeQuery();
 //			rs.last(); int numrows = rs.getRow(); rs.beforeFirst();
 //			provider.logger.info("ROWS: " + numrows);
 			while(rs.next()){
 				String adding = rs.getString(1); 
 				list.add(adding);
 //				provider.logger.info("SUBGROUP FOR PLAYER " + name + ": " + adding);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			provider.checkConnection();
 			SQLPermissions.mysqlError(e);
 		} 
 		return list;
 	}
 
 	@Override
 	public boolean removePermission(String world, String permission) {
 		if (world == null) {
 			world = global;
 		} else {
 			world = Config.getRealWorld(world).toLowerCase();
 		}
 		
 		PreparedStatement prep = provider.prepRemoveUserPermission;
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
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean removeSubgroup(String subgroup) {
 		PreparedStatement prep = provider.prepRemoveUserSubgroup;
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
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean setGroup(String newGroup) {
 		PreparedStatement prep = provider.prepSetUserGroup;
 		int num = 0;
 		try {
 			prep.clearParameters();
 			prep.setString(1, newGroup);
 			prep.setInt(2, ID);
 			num = prep.executeUpdate();
 		} catch (SQLException e) {
 			SQLPermissions.mysqlError(e);
			return false;
 		}
		return true;
 	}
 
 	@Override
 	public boolean setInfo(String node, String data) {
 		PreparedStatement prep = provider.prepAddUserInfoNode;
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
 
 	private Map<String, Boolean> getWorldPermissions(String world) {
 		HashMap<String, Boolean> result = new HashMap<String, Boolean>();
 		provider.checkConnection();
 		PreparedStatement prep = provider.prepGetUserPermissions;
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
 }
