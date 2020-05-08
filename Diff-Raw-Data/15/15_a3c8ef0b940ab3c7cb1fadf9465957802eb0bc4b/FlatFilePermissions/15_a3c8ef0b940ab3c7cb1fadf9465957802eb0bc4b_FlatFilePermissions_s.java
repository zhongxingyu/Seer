 package de.hydrox.bukkit.DroxPerms.data.flatfile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import de.hydrox.bukkit.DroxPerms.data.IDataProvider;
 
 /**
  * 
  * @author Matthias Söhnholz
  *
  */
 public class FlatFilePermissions implements IDataProvider {
 
 	public static final String NODE = "FlatFile";
 	protected static Plugin plugin = null;
 
 	private YamlConfiguration groupsConfig;
 	private YamlConfiguration usersConfig;
 	private YamlConfiguration tracksConfig;
 
 
 	public FlatFilePermissions() {
 		groupsConfig = YamlConfiguration.loadConfiguration(new File("groups.yml"));
 		usersConfig = YamlConfiguration.loadConfiguration(new File("users.yml"));
 		tracksConfig = YamlConfiguration.loadConfiguration(new File("tracks.yml"));
 	}
 
 	public FlatFilePermissions(Plugin plugin) {
 		FlatFilePermissions.plugin = plugin;
 		// Write some default configuration
 
 		groupsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "groups.yml"));
 		YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(plugin.getResource("groups.yml"));
 		groupsConfig.setDefaults(defConfig);
 		usersConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "users.yml"));
 		defConfig = YamlConfiguration.loadConfiguration(plugin.getResource("users.yml"));
 		usersConfig.setDefaults(defConfig);
 		tracksConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "tracks.yml"));
 
 		Set<String> groups = groupsConfig.getConfigurationSection("groups.").getKeys(false);
 		for (String group : groups) {
 			plugin.getServer().getLogger().fine("load group: " + group);
 			ConfigurationSection conf = groupsConfig.getConfigurationSection("groups." + group);
 			Group newGroup = new Group(group, conf);
 			Group.addGroup(newGroup);
 		}
 
		Set<String> tracks = tracksConfig.getConfigurationSection("tracks.").getKeys(false);
		for (String track : tracks) {
			plugin.getServer().getLogger().fine("load track: " + track);
			ConfigurationSection conf = tracksConfig.getConfigurationSection("tracks." + track);
			Track newTrack = new Track(track, conf);
			Track.addTrack(newTrack);
 		}
 	}
 
 	public void save() {
 		HashMap<String,Object> tmp = new HashMap<String,Object>();
 		Iterator<Group> iter = Group.iter();
 		while (iter.hasNext()) {
 			Group group = iter.next();
 			tmp.put(group.getName().toLowerCase(), group.toConfigurationNode());
 			groupsConfig.createSection("groups." + group.getName(), group.toConfigurationNode());
 		}
 
 		try {
 			groupsConfig.save(new File(plugin.getDataFolder(),"groups.yml"));
 		} catch (IOException e) {
 			plugin.getLogger().log(Level.SEVERE, "Could not save config to groups.yml", e);
 		}
 
 		tmp = new HashMap<String,Object>();
 		Iterator<User> iter2 = User.iter(); 
 		while (iter2.hasNext()) {
 			User user = iter2.next();
 			if (user.isDirty()) {
 				usersConfig.createSection("users." + user.getName(), user.toConfigurationNode());
 				user.clean();
 			}
 		}
 
 		try {
 			usersConfig.save(new File(plugin.getDataFolder(), "users.yml"));
 		} catch (IOException e) {
 			plugin.getLogger().log(Level.SEVERE, "Could not save config to users.yml", e);
 		}
 	}
 
 	public boolean createPlayer(String name) {
 		if (User.existUser(name)) {
 			return false;
 		} else {
 			User.addUser(new User(name));
 			return true;
 		}
 	}
 
 	public boolean deletePlayer(CommandSender sender, String name) {
 		Player player = plugin.getServer().getPlayerExact(name);
 		if (player != null) {
 			sender.sendMessage(ChatColor.RED + "Can't delete online Player.");
 			return false;
 		}
 		User user = getExactUser(name);
 		if (user != null) {
 			User.removeUser(name);
 			usersConfig.set("users." + name, null);
 			sender.sendMessage(ChatColor.GREEN + "Deleted Player " + name + ".");
 			return true;
 		}
 		sender.sendMessage(ChatColor.RED + "No Player with this exact name found.");
 
 		return false;
 	}
 
 	public boolean createGroup(CommandSender sender, String name) {
 		if (Group.existGroup(name)) {
 			return false;
 		} else {
 			Group.addGroup(new Group(name));
 			return true;
 		}
 	}
 
 	public String getPlayerGroup(String player) {
 		User user = getUser(player, true);
 		if (user != null) {
 			return user.getGroup();
 		} else {
 			return "";
 		}
 	}
 
 	public boolean setPlayerGroup(CommandSender sender, String player, String group) {
 		User user = getUser(player, true);
 		if (user != null) {
 			boolean result = user.setGroup(group);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "Set group of player " + user.getName() + " to " + group);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't set group of player " + user.getName());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public List<String> getPlayerSubgroups(String player) {
 		User user = getUser(player, true);
 		if (user != null) {
 			List<String> result = new ArrayList<String>(user.getSubgroups());
 			result.add(user.getGroup());
 			result = calculateSubgroups(result);
 			result.remove(user.getGroup());
 			return result;
 		} else {
 			return null;
 		}
 	}
 
 	public List<String> getPlayerSubgroupsSimple(String player) {
 		User user = getUser(player, true);
 		if (user != null) {
 			return new ArrayList<String>(user.getSubgroups());
 		} else {
 			return null;
 		}
 	}
 
 	private List<String> calculateSubgroups(List<String> input) {
 		List<String> result = new ArrayList<String>(input);
 		List<String> toTest = new ArrayList<String>(input);
 
 		while (toTest.size()!=0) {
 			String string = toTest.get(0);
 			Group group = Group.getGroup(string);
 			if (group != null) {
 				List<String> subgroups = group.getSubgroups();
 				for (String string2 : subgroups) {
 					if (!result.contains(string2)) {
 						result.add(string2);
 						toTest.add(string2);
 					}
 				}
 			}
 			toTest.remove(string);
 		}
 		return result;
 	}
 
 	public boolean addPlayerSubgroup(CommandSender sender, String player, String subgroup) {
 		User user = getUser(player, true);
 		if (user != null) {
 			boolean result = user.addSubgroup(subgroup);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "Added " + subgroup + " to subgrouplist of player " + user.getName());
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't add subgroup to player " + user.getName());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public boolean removePlayerSubgroup(CommandSender sender, String player, String subgroup) {
 		User user = getUser(player, true);
 		if (user != null) {
 			boolean result = user.removeSubgroup(subgroup);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "removed " + subgroup + " from subgrouplist of player " + user.getName());
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't remove subgroup from player " + user.getName());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public boolean addPlayerPermission(CommandSender sender, String player, String world, String node) {
 		User user = getUser(player, true);
 		if (user != null) {
 			boolean result = user.addPermission(world, node);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "Added " + node + " to permissionslist of player " + user.getName());
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't add permission to player " + user.getName());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public boolean removePlayerPermission(CommandSender sender, String player, String world, String node) {
 		User user = getUser(player, true);
 		if (user != null) {
 			boolean result = user.removePermission(world, node);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "removed " + node + " from permissionslist of player " + user.getName());
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't remove permission from player " + user.getName());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public Map<String, List<String>> getPlayerPermissions(String player, String world, boolean partialMatch) {
 		User user = getUser(player, partialMatch);
 		if (user == null) {
 			if (partialMatch) {
 				return null;
 			}
 			plugin.getServer().getLogger().info("[DroxPerms] User " + player + " doesn't exist yet. Creating ...");
 			user = new User(player);
 			User.addUser(user);
 			return user.getPermissions(world);
 		}
 		return user.getPermissions(world);
 	}
 
 	public boolean setPlayerInfo(CommandSender sender, String player, String node, String data) {
 		User user = getUser(player, true);
 		if (user != null) {
 			boolean result = user.setInfo(node, data);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "set info-node " + node + " of player " + user.getName());
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't set info-node of player " + user.getName());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public String getPlayerInfo(CommandSender sender, String player, String node) {
 		User user = getUser(player, true);
 		if (user != null) {
 			return user.getInfo(node);
 		} else {
 			return null;
 		}
 	}
 
 	public boolean addGroupPermission(CommandSender sender, String group, String world, String node) {
 		if (Group.existGroup(group)) {
 			boolean result = Group.getGroup(group).addPermission(world, node);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "Added " + node + " to permissionslist of group " + group);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't add permission to group " + group);
 				return false;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Group " + group + " doesn't exist.");
 			return false;
 		}
 	}
 
 	public boolean removeGroupPermission(CommandSender sender, String group, String world, String node) {
 		if (Group.existGroup(group)) {
 			boolean result = Group.getGroup(group).removePermission(world, node);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "removed " + node + " from permissionslist of group " + group);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't remove permission from group " + group);
 				return false;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Group " + group + " doesn't exist.");
 			return false;
 		}
 	}
 
 	public List<String> getGroupSubgroups(String groupName) {
 		Group group = Group.getGroup(groupName);
 		if (group != null) {
 			return calculateSubgroups(group.getSubgroups());
 		} else {
 			return null;
 		}
 	}
 
 	public boolean addGroupSubgroup(CommandSender sender, String group, String subgroup) {
 		if (Group.existGroup(group)) {
 			boolean result = Group.getGroup(group).addSubgroup(subgroup);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "Added " + subgroup + " to subgrouplist of group " + group);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't add subgroup to group " + group);
 				return false;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Group " + group + " doesn't exist.");
 			return false;
 		}
 	}
 
 	public boolean removeGroupSubgroup(CommandSender sender, String group, String subgroup) {
 		if (Group.existGroup(group)) {
 			boolean result = Group.getGroup(group).removeSubgroup(subgroup);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "removed " + subgroup + " from subgrouplist of group " + group);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't remove subgroup from group " + group);
 				return false;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Group " + group + " doesn't exist.");
 			return false;
 		}
 	}
 
 	public Map<String, List<String>> getGroupPermissions(String groupName, String world) {
 		Group group = Group.getGroup(groupName);
 		if (group == null) {
 			return null;
 		}
 		return group.getPermissions(world);
 	}
 
 	public boolean setGroupInfo(CommandSender sender, String group, String node, String data) {
 		if (Group.existGroup(group)) {
 			boolean result = Group.getGroup(group).setInfo(node, data);
 			if (result) {
 				sender.sendMessage(ChatColor.GREEN + "set info-node " + node + " for group " + group);
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Couldn't set info-node for group " + group);
 				return false;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Group " + group + " doesn't exist.");
 			return false;
 		}
 	}
 
 	public String getGroupInfo(CommandSender sender, String group, String node) {
 		if (Group.existGroup(group)) {
 			return Group.getGroup(group).getInfo(node);
 		} else {
 			sender.sendMessage(ChatColor.RED + "Group " + group + " doesn't exist.");
 			return null;
 		}
 	}
 
 	private User getUser(String name, boolean checkPartial) {
 		if (checkPartial) {
 			return getPartialUser(name);
 		}
 		return getExactUser(name);
 	}
 	private User getExactUser(String name) {
 		User user = null;
 		if (User.existUser(name)) {
 			user = User.getUser(name);
 			return user;
 		} else {
 			ConfigurationSection node = usersConfig.getConfigurationSection("users." + name);
 			if (node != null) {
 				user = new User(name, node);
 				User.addUser(user);
 				return user;
 			}
 		}
 		return null;
 	}
 
 	private User getPartialUser(String name) {
 		boolean unsureOnline = false;
 		User user = null;
 		String onlineplayer = null;
 		Player player = plugin.getServer().getPlayer(name);
 		if (player != null) {
 			onlineplayer = player.getName();
 		}
 		if (onlineplayer != null && User.existUser(onlineplayer)) {
 			user = User.getUser(onlineplayer);
 		} else if (User.existUser(name)) {
 			user = User.getUser(name);
 		} else {
 			Iterator<User> iter = User.iter();
 			while (iter.hasNext()) {
 				User user2 = iter.next();
 				if (user2.getName().toLowerCase().contains(name.toLowerCase())) {
 					if (user == null) {
 						user = user2;
 					} else {
 						unsureOnline = true;
 					}
 				}
 			}
 			if (user == null || unsureOnline) {
 				ConfigurationSection node = usersConfig.getConfigurationSection("users." + name);
 				if (node != null) {
 					user = new User(name, node);
 					User.addUser(user);
 				} else {
 					Set<String> users = usersConfig.getConfigurationSection("users.").getKeys(false);
 					for (String potentialUser : users) {
 						if(potentialUser.toLowerCase().contains(name.toLowerCase())) {
 							if (user == null) {
 								node = usersConfig.getConfigurationSection("users." + potentialUser);
 								user = new User(potentialUser, node);
 								User.addUser(user);
 							} else {
 								return null;
 							}
 						}
 					}
 				}
 			}
 		}
 		if (user == null) {
 			return null;
 		}
 		return user;
 	}
 
 	@Override
 	public boolean promotePlayer(CommandSender sender, String player,
 			String track) {
 		Track selectedTrack = Track.getTrack(track);
 		if (selectedTrack == null) {
 			sender.sendMessage(ChatColor.RED + "Could not find Track " + track + ".");
 			return false;
 		}
 		User user = getUser(player, true);
 		if (user != null) {
 			String newGroup = selectedTrack.getPromoteGroup(user.getGroup());
 			if (newGroup == null) {
 				sender.sendMessage(ChatColor.RED + "Could not promote on Track " + track + ".");
 				return false;
 			}
 			return setPlayerGroup(sender, player, newGroup);
 		} else {
 			sender.sendMessage(ChatColor.RED + "Could not find User " + player + ".");
 			return false;
 		}
 	}
 
 	@Override
 	public boolean demotePlayer(CommandSender sender, String player,
 			String track) {
 		Track selectedTrack = Track.getTrack(track);
 		if (selectedTrack == null) {
 			sender.sendMessage(ChatColor.RED + "Could not find Track " + track + ".");
 			return false;
 		}
 		User user = getUser(player, true);
 		if (user != null) {
 			String newGroup = selectedTrack.getDemoteGroup(user.getGroup());
 			if (newGroup == null) {
 				sender.sendMessage(ChatColor.RED + "Could not demote on Track " + track + ".");
 				return false;
 			}
 			return setPlayerGroup(sender, player, newGroup);
 		} else {
 			sender.sendMessage(ChatColor.RED + "Could not find User " + player + ".");
 			return false;
 		}
 	}
 
 	@Override
 	public Map<String, List<String>> getGroupMembers() {
 		Map<String, List<String>> result = new HashMap<String, List<String>>();
 		Iterator<Group> groups = Group.iter();
 		while (groups.hasNext()) {
 			Group group = (Group) groups.next();
 			result.put(group.getName(), new ArrayList<String>());
 		}
 
 		Set<String> users = usersConfig.getConfigurationSection("users.").getKeys(false);
 		for (String key : users) {
 			ConfigurationSection conf = usersConfig.getConfigurationSection("users." + key);
 			User user = new User(key, conf);
 			result.get(user.getGroup()).add(user.getName());
 		}
 		return result;
 	}
 
 	@Override
 	public Map<String, List<String>> getSubgroupMembers() {
 		Map<String, List<String>> result = new HashMap<String, List<String>>();
 		Iterator<Group> groups = Group.iter();
 		while (groups.hasNext()) {
 			Group group = (Group) groups.next();
 			result.put(group.getName(), new ArrayList<String>());
 		}
 
 		Set<String> users = usersConfig.getConfigurationSection("users.").getKeys(false);
 		for (String key : users) {
 			ConfigurationSection conf = usersConfig.getConfigurationSection("users." + key);
 			User user = new User(key, conf);
 			List<String> subgroups = user.getSubgroups();
 			for (String subgroup : subgroups) {
 				result.get(subgroup).add(user.getName());
 			}
 		}
 		return result;
 	}
 
 	public String getUserNameFromPart(String partialName) {
 		User user = getUser(partialName, true);
 		if (user != null) {
 			return user.getName();
 		}
 		return null;
 	}
 }
