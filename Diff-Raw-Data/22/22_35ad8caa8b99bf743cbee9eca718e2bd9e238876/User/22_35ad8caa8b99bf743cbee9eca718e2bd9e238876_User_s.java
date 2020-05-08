 package de.hydrox.bukkit.DroxPerms.data.flatfile;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import org.bukkit.util.config.ConfigurationNode;
 
 import de.hydrox.bukkit.DroxPerms.data.Config;
 
 public class User {
 	private static HashMap<String, User> users = new HashMap<String, User>();
 
 	private String name;
 	private String group;
 	private ArrayList<String> subgroups;
 	private ArrayList<String> globalPermissions;
 	private HashMap<String, ArrayList<String>> permissions;
 
 	public User() {
 		this("mydrox");
 	}
 
 	public User(String name) {
 		this.name = name;
 		this.group = "default";
 		this.subgroups = new ArrayList<String>();
 		this.globalPermissions = new ArrayList<String>();
 		this.permissions = new HashMap<String, ArrayList<String>>();
 	}
 
 	public User(String name, ConfigurationNode node) {
 		this.name = name;
 		this.group = node.getString("group");
 //		System.out.println("users" + node.getKeys().toString());
 //		System.out.println("users.subgroups" + node.getStringList("subgroups", new ArrayList<String>()));
 		this.subgroups = (ArrayList<String>) node.getStringList("subgroups", new ArrayList<String>());
 //		System.out.println("subgroups: " + subgroups.size());
 		this.globalPermissions = (ArrayList<String>) node.getStringList("globalpermissions", new ArrayList<String>());
 //		System.out.println("globalpermissions: " + globalPermissions.size());
 		this.permissions = new HashMap<String, ArrayList<String>>();
 		ConfigurationNode tmp = node.getNode("permissions");
 		Iterator<String> iter = tmp.getKeys().iterator();
 		while (iter.hasNext()) {
 			String world = iter.next();
 			permissions.put(world, (ArrayList<String>) tmp.getStringList(world, new ArrayList<String>()));
 //			System.out.println("permissions "+world+": " + permissions.get(world).size());
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public HashMap<String, Object> toConfigurationNode() {
 		LinkedHashMap<String, Object> output = new LinkedHashMap<String, Object>();
 		output.put("group", group);
 		output.put("subgroups", subgroups);
 		output.put("permissions", permissions);
 		output.put("globalpermissions", globalPermissions);
 		return output;
 	}
 	
 	public String[] getPermissions(String world) {
 		ArrayList<String> perms = new ArrayList<String>();
 		//add group permissions
 		perms.add("droxperms.meta.group." + group);
 		//add subgroup permissions
 		for (Iterator<String> iterator = subgroups.iterator(); iterator.hasNext();) {
 			String subgroup = iterator.next();
 			perms.add("droxperms.meta.group." + subgroup);
 		}
 		//add global permissions
 		perms.addAll(globalPermissions);
 		//add world permissions
 		perms.addAll(permissions.get(world));
 		return perms.toArray(new String[0]);
 	}
 
 	public boolean setGroup(String newGroup) {
 		if (Group.existGroup(newGroup)) {
 			group = newGroup;
 			return true;
 		}
 		return false;
 	}
 
 	public boolean addPermission(String world, String permission) {
 		if (world == null) {
 			if (globalPermissions.contains(permission)) {
 				return false;
 			}
 			globalPermissions.add(permission);
 			return true;
 		}
 
 		ArrayList<String> permArray = permissions.get(Config.getRealWorld(world).toLowerCase());
 		if (permArray == null) {
 			permArray = new ArrayList<String>();
 			permissions.put(Config.getRealWorld(world).toLowerCase(), permArray);
 		}
		if (permArray != null) {
			if (permArray.contains(permission)) {
				return false;
			}
			permArray.add(permission);
			return true;
 		}
		return false;
 	}
 
 	public boolean removePermission(String world, String permission) {
 		if (world == null) {
 			if (globalPermissions.contains(permission)) {
 				globalPermissions.remove(permission);
 				return true;
 			}
 			return false;
 		}
 
 		ArrayList<String> permArray = permissions.get(Config.getRealWorld(world).toLowerCase());
 		if (permArray == null) {
 			permArray = new ArrayList<String>();
 			permissions.put(Config.getRealWorld(world).toLowerCase(), permArray);
 		}
		if (permArray != null) {
			if (permArray.contains(permission)) {
				permArray.remove(permission);
				return true;
			}
			return false;
 		}
 		return false;
 	}
 
 	public boolean addSubgroup(String subgroup) {
 		if(Group.existGroup(subgroup.toLowerCase())) {
 			if(!subgroups.contains(subgroup.toLowerCase())) {
 				subgroups.add(subgroup.toLowerCase());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean removeSubgroup(String subgroup) {
 		if(subgroups.contains(subgroup.toLowerCase())) {
 			subgroups.remove(subgroup.toLowerCase());
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean addUser(User user) {
 		if (existUser(user.name.toLowerCase())) {
 			return false;
 		}
 		users.put(user.name.toLowerCase(), user);
 		return true;
 	}
 
 	public static boolean removeUser(String name) {
 		if (existUser(name.toLowerCase())) {
 			users.remove(name.toLowerCase());
 			return true;
 		}
 		return false;
 	}
 
 	public static User getUser(String name) {
 		return users.get(name.toLowerCase());
 	}
 
 	public static boolean existUser(String name) {
 		if (users.containsKey(name.toLowerCase())) {
 			return true;
 		}
 		return false;
 	}
 
 	public static void clearUsers() {
 		users.clear();
 	}
 	
 	public static Iterator<User> iter() {
 		return users.values().iterator();
 	}
 }
