 package de.hydrox.bukkit.DroxPerms.data.flatfile;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.permissions.Permission;
 import org.bukkit.util.config.ConfigurationNode;
 
 import de.hydrox.bukkit.DroxPerms.data.Config;
 
 public class Group {
 	private static HashMap<String, Group> groups = new HashMap<String, Group>();
 	private static HashMap<String, Group> backupGroups = new HashMap<String, Group>();
 	private static boolean testmode = false;
 
 	private String name;
 	private HashMap<String, ArrayList<String>> permissions;
 	private HashMap<String, String> info;
 	private ArrayList<String> globalPermissions;
 	private ArrayList<String> subgroups;
 
 	private HashMap<String, Permission> bukkitPermissions;
 
 	public Group() {
 		this("default");
 	}
 
 	public Group(String name) {
 		this.name = name;
 		this.subgroups = new ArrayList<String>();
 		this.globalPermissions = new ArrayList<String>();
 		this.permissions = new HashMap<String, ArrayList<String>>();
 	}
 
 	public Group(String name, ConfigurationNode node) {
 		this.name = name;
 		this.subgroups = (ArrayList<String>) node.getStringList("subgroups", new ArrayList<String>());
 		this.globalPermissions = (ArrayList<String>) node.getStringList("globalpermissions", new ArrayList<String>());
 		this.permissions = new HashMap<String, ArrayList<String>>();
 		ConfigurationNode tmp = node.getNode("permissions");
 		if (tmp != null) {
 			Iterator<String> iter = tmp.getKeys().iterator();
 			while (iter.hasNext()) {
 				String world = iter.next();
 				permissions.put(world, (ArrayList<String>) tmp.getStringList(world, new ArrayList<String>()));
 			}
 		}
 		tmp = null;
 		tmp = node.getNode("info");
 		if(tmp != null) {
 			this.info = new HashMap<String, String>();
 			Iterator<String> iter = tmp.getKeys().iterator();
 			while (iter.hasNext()) {
 				String infoNode = iter.next();
 				info.put(infoNode, tmp.getString(infoNode));
 			}
 		}
 
 		updatePermissions();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public HashMap<String, Object> toConfigurationNode() {
 		HashMap<String, Object> output = new HashMap<String, Object>();
 		if (subgroups != null && subgroups.size() != 0) {
 			output.put("subgroups", subgroups);
 		}
 		if (permissions != null && permissions.size() != 0) {
 			output.put("permissions", permissions);
 		}
 		if (info != null && info.size() != 0) {
 			output.put("info", info);
 		}
 		if (globalPermissions != null && globalPermissions.size() != 0) {
 			output.put("globalpermissions", globalPermissions);
 		}
 		return output;
 	}
 
 	public HashMap<String, ArrayList<String>> getPermissions(String world) {
 		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
 		ArrayList<String> groupperms = new ArrayList<String>();
 		//add group permissions
 		groupperms.add("droxperms.meta.group." + name);
 		if (world != null) {
 			groupperms.add("droxperms.meta.group." + name + "." + Config.getRealWorld(world));
 		}
 		result.put("group", groupperms);
 		//add subgroup permissions
 		if (subgroups != null) {
 			ArrayList<String> subgroupperms = new ArrayList<String>();
 			for (Iterator<String> iterator = subgroups.iterator(); iterator.hasNext();) {
 				String subgroup = iterator.next();
 				subgroupperms.add("droxperms.meta.group." + subgroup);
 				if (world != null) {
 					subgroupperms.add("droxperms.meta.group." + subgroup + "." + Config.getRealWorld(world));
 				}
 			}
 			result.put("subgroups", subgroupperms);
 		}
 		//add global permissions
 		if (globalPermissions != null) {
 			result.put("global", globalPermissions);
 		}
 		//add world permissions
 		if (world != null && permissions != null) {
 			ArrayList<String> worldperms = new ArrayList<String>();
 			if (permissions.get(Config.getRealWorld(world)) != null) {
 				worldperms.addAll(permissions.get(Config.getRealWorld(world)));
 			}
 			result.put("world", worldperms);
 		}
 		return result;
 	}
 
 	public boolean addPermission(String world, String permission) {
 		if (world == null) {
 			if (globalPermissions == null) {
 				globalPermissions = new ArrayList<String>();
 			}
 			if (globalPermissions.contains(permission)) {
 				return false;
 			}
 			globalPermissions.add(permission);
 			updatePermissions();
 			return true;
 		}
 
 		ArrayList<String> permArray = permissions.get(Config.getRealWorld(world).toLowerCase());
 		if (permArray == null) {
 			permArray = new ArrayList<String>();
 			permissions.put(Config.getRealWorld(world).toLowerCase(), permArray);
 		}
 		if (permArray.contains(permission)) {
 			return false;
 		}
 		permArray.add(permission);
 		updatePermissions();
 		return true;
 	}
 
 	public boolean removePermission(String world, String permission) {
 		if (world == null) {
 			if (globalPermissions.contains(permission)) {
 				globalPermissions.remove(permission);
 				updatePermissions();
 				return true;
 			}
 			return false;
 		}
 
 		ArrayList<String> permArray = permissions.get(Config.getRealWorld(world).toLowerCase());
 		if (permArray == null) {
 			permArray = new ArrayList<String>();
 			permissions.put(Config.getRealWorld(world).toLowerCase(), permArray);
 		}
 		if (permArray.contains(permission)) {
 			permArray.remove(permission);
 			updatePermissions();
 			return true;
 		}
 		return false;
 	}
 
 	public boolean addSubgroup(String subgroup) {
 		if(Group.existGroup(subgroup.toLowerCase())) {
 			if (subgroups == null) {
 				subgroups = new ArrayList<String>();
 			}
 			if(!subgroups.contains(subgroup.toLowerCase())) {
 				subgroups.add(subgroup.toLowerCase());
 				updatePermissions();
 				return true;
 			}
 			
 		} 
 		return false;
 	}
 
 	public boolean removeSubgroup(String subgroup) {
 		if(subgroups != null && subgroups.contains(subgroup.toLowerCase())) {
 			subgroups.remove(subgroup.toLowerCase());
 			updatePermissions();
 			return true;
 		}
 		return false;
 	}
 
 	public boolean hasPermission(String world, String permission) {
 		ArrayList<String> permArray = permissions.get(world.toLowerCase());
 		if (permArray != null) {
 			if (permArray.contains(permission)) {
 				return true;
 			}
 		}
 
 		for (String subgroup : subgroups) {
 			if (Group.getGroup(subgroup) != null) {
 				if (Group.getGroup(subgroup).hasPermission(world.toLowerCase(), permission)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean setInfo(String node, String data) {
 		if (info == null) {
 			info = new HashMap<String, String>();
 		}
 		info.put(node, data);
 		return true;
 	}
 
 	public String getInfo(String node) {
 		if (info == null) {
 			return null;
 		}
 		return info.get(node);
 	}
 
 	public boolean addWorld(String world) {
 		if (permissions.containsKey(world.toLowerCase())) {
 			return false;
 		}
 		permissions.put(world.toLowerCase(), new ArrayList<String>());
 		updatePermissions();
 		return true;
 	}
 
 	public ArrayList<String> getSubgroups() {
 		if (subgroups == null) {
 			subgroups = new ArrayList<String>();
 		}
 		return subgroups;
 	}
 
 	public void updatePermissions() {
 		bukkitPermissions = new HashMap<String, Permission>();
 		//create Permission for default world
 		if (subgroups == null) {
 			subgroups = new ArrayList<String>();
 		}
 		if (!permissions.containsKey(Config.getDefaultWorld())) {
 			HashMap<String, Boolean> children = new HashMap<String, Boolean>();
 			for (String subgroup : subgroups) {
 				children.put("droxperms.meta.group." + subgroup + "." + Config.getDefaultWorld(), true);
 			}
 			children.put("droxperms.meta.group." + name, true);
 
 			Permission permission = new Permission("droxperms.meta.group." + name + "." + Config.getDefaultWorld(), "Group-Permissions for group " + name + " on world " + Config.getDefaultWorld(), children);
 			FlatFilePermissions.plugin.getServer().getPluginManager().removePermission("droxperms.meta.group." + name + "." + Config.getDefaultWorld());
 			FlatFilePermissions.plugin.getServer().getPluginManager().addPermission(permission);
 			bukkitPermissions.put(Config.getDefaultWorld(), permission);
 		}
 
 		//create Permissions for other worlds
 		for (String world : permissions.keySet()) {
 			HashMap<String, Boolean> children = new HashMap<String, Boolean>();
 			for (String subgroup : subgroups) {
 				children.put("droxperms.meta.group." + subgroup + "." + world, true);
 			}
 
 			for (String permission : permissions.get(world)) {
 				if (permission.startsWith("-")) {
 					children.put(permission, false);
 				} else {
 					children.put(permission, true);
 				}
 			}
 
 			children.put("droxperms.meta.group." + name, true);
 
 			Permission permission = new Permission("droxperms.meta.group." + name + "." + world, "Group-Permissions for group " + name + " on world " + world, children);
 			FlatFilePermissions.plugin.getServer().getPluginManager().removePermission("droxperms.meta.group." + name + "." + world);
 			FlatFilePermissions.plugin.getServer().getPluginManager().addPermission(permission);
 			bukkitPermissions.put(world, permission);
 		}
 
 		HashMap<String, Boolean> children = new HashMap<String, Boolean>();
 		for (String subgroup : subgroups) {
 			children.put("droxperms.meta.group." + subgroup, true);
 		}
 
 		for (String permission : globalPermissions) {
 			if (permission.startsWith("-")) {
 				children.put(permission, false);
 			} else {
 				children.put(permission, true);
 			}
 		}
 
 		//create Permission for global grouppermissions
 		Permission permission = new Permission("droxperms.meta.group." + name, "Group-Permissions for group " + name, children);
 		FlatFilePermissions.plugin.getServer().getPluginManager().removePermission("droxperms.meta.group." + name);
 		FlatFilePermissions.plugin.getServer().getPluginManager().addPermission(permission);
 	}
 
 	public static boolean addGroup(Group group) {
 		if (existGroup(group.name.toLowerCase())) {
 			return false;
 		}
 		groups.put(group.name.toLowerCase(), group);
 		return true;
 	}
 
 	public static boolean removeGroup(String name) {
 		if (existGroup(name.toLowerCase())) {
 			groups.remove(name.toLowerCase());
 			return true;
 		}
 		return false;
 	}
 
 	public static Group getGroup(String name) {
 		return groups.get(name.toLowerCase());
 	}
 
 	public static boolean existGroup(String name) {
 		if (groups.containsKey(name.toLowerCase())) {
 			return true;
 		}
 		return false;
 	}
 
 	public static void clearGroups() {
 		groups.clear();
 	}
 	
 	public static Iterator<Group> iter() {
 		return groups.values().iterator();
 	}
 
 	public static void setTestMode() {
 		if (!testmode) {
 			backupGroups = groups;
 			groups = new HashMap<String, Group>();
 			testmode = true;
 		}
 	}
 
 	public static void setNormalMode() {
 		if (testmode) {
 			groups = backupGroups;
 			testmode = false;
 		}
 	}
 }
