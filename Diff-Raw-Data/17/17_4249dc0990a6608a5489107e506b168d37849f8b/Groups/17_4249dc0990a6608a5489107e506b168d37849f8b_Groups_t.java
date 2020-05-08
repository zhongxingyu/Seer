 package me.Staartvin.Groups;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import me.Staartvin.Staff_Info.Staff_Info;
 
 public class Groups {
 
 	private Staff_Info plugin;
 	public initialiseGroups iGroups;
 	List<String> groups;
 	private String[] examplegroup = {"ExamplePlayer1", "ExamplePlayer2", "ExamplePlayer3"};
 	
 	public Groups(Staff_Info instance) {
 		plugin = instance;
 	}
 	
 	public void getSubClasses(Staff_Info plugin) {
 		iGroups = new initialiseGroups(plugin);
 		iGroups.initialiseAllGroups();
 	}
 	
 	public List<String> getGroups() {
 		return groups;
 	}
 	
 	public void showShortGroupInfo(String group, CommandSender sender) {
 		
		if (searchInGroups(group) == null) {
			sender.sendMessage(ChatColor.RED + "Group '" + group + "' is not defined!"); 
 			return;
 		}
		String groupname = searchInGroups(group);
 		 sender.sendMessage(ChatColor.BLUE + "------------------[" + ChatColor.GOLD + groupname + " Info" + ChatColor.BLUE + "]");
 		 sender.sendMessage(ChatColor.GOLD + "Description: " + ChatColor.BLUE + plugin.config.replaceColours(plugin.getConfig().getString("Groups." + groupname + ".shortdescription")));
 		 sender.sendMessage(ChatColor.BLUE + "If you want to know more about " + groupname + ", type " + ChatColor.GOLD + "/staff info " + groupname);
 	}
 	
 	public void showLongGroupInfo(String group, CommandSender sender) {
 		
		if (searchInGroups(group) == null) {
			sender.sendMessage(ChatColor.RED + "Group '" + group + "' is not defined!"); 
 			return;
 		}
		String groupname = searchInGroups(group);
 		if (!plugin.getConfig().getBoolean("Groups." + groupname + ".visible")) {
 			sender.sendMessage(ChatColor.RED + groupname + " is not visible!");
 			return;
 		}
 		 sender.sendMessage(ChatColor.BLUE + "------------------[" + ChatColor.GOLD + groupname + " Info" + ChatColor.BLUE + "]");
 		 sender.sendMessage(ChatColor.GOLD + "Name: " + ChatColor.BLUE + plugin.config.replaceColours(plugin.getConfig().getString("Groups." + groupname + ".displayname")));
 		 sender.sendMessage(ChatColor.GOLD + "Description: " + ChatColor.BLUE + plugin.config.replaceColours(plugin.getConfig().getString("Groups." + groupname + ".longdescription")));
 		 sender.sendMessage(ChatColor.GOLD + "Staff members in " + groupname + ": " + ChatColor.BLUE + plugin.config.replaceColours(plugin.getConfig().getStringList("Groups." + groupname + ".members").toString()));
 	}
 	
 	public String searchInGroups(String group) {
 		
 		String groupname = null;
 		
 		for (int i=0;i<groups.size();i++) {
 			if (groups.get(i).equalsIgnoreCase(group)) {
 				groupname = groups.get(i);
 				break;
 			}
 		}
 		return groupname;
 	}
 	
 	public boolean createGroup(String groupname, CommandSender sender) {
 		
 		if (searchInGroups(groupname) != null) {
 			sender.sendMessage(ChatColor.RED + "Cannot create '" + groupname + "', this group already exists!");
 			return false;
 		}
 		plugin.getConfig().set("Groups." + groupname + ".displayname", "Example displayname");
 		plugin.getConfig().set("Groups." + groupname + ".visible", false);
 		plugin.getConfig().set("Groups." + groupname + ".shortdescription", "Example of a short description");
 		plugin.getConfig().set("Groups." + groupname + ".longdescription", "This is an example of a longer description");
 		plugin.getConfig().set("Groups." + groupname + ".members", Arrays.asList(examplegroup));
 		
 		List<String> groups = plugin.getConfig().getStringList("Staff Groups");
 		groups.add(groupname);
 		plugin.getConfig().set("Staff Groups", groups);
 		
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.GREEN + "Group '" + groupname + "' has been created!");
 		return true;
 	}
 	
 	public boolean editVariable(String[] args, CommandSender sender) {
 		String groupName = plugin.groups.searchInGroups(args[1]);
 		List<String> variableResult = new ArrayList<String>();
 		int counter = 3;
 		String resultString = "";
 		String variable = args[2].toLowerCase();
 		
 		if (groupName == null) {
 			sender.sendMessage(ChatColor.RED + "Group '" + args[1] + "' is not defined!");
 			return false;
 		}
 		if (!plugin.CommandExecutor.hasPermission("staffinfo.groups.edit." + groupName, sender)) return true;
 		
 		if (plugin.getConfig().get("Groups." + groupName + "." + variable) == null) {
 			sender.sendMessage(ChatColor.RED + "'" + variable + "' is not a valid variable!");
 			return false;
 		}
 		
 		if (variable.equalsIgnoreCase("members")) {
 			sender.sendMessage(ChatColor.RED + "The member variable cannot be changed this way!");
 			sender.sendMessage(ChatColor.BLUE + "Use /staff add <playername> <groupname> to add someone to members!");
 			return false;
 		}
 		
 		for (int i=0;i<(args.length - 3);i++) {
 			variableResult.add(args[counter]);
 			++counter;
 		}
 		for (int i=0;i<variableResult.size();i++) {
 			if (i == 0) {
 				resultString = resultString.concat(variableResult.get(i));
 			}
 			else {
 				resultString = resultString.concat(" " + variableResult.get(i));
 			}
 		}
 		if (variable.equalsIgnoreCase("visible")) {
 			if (resultString.equalsIgnoreCase("true")) {
 				plugin.getConfig().set("Groups." + groupName + "." + variable, true);
 				resultString = "true";
 			}
 			else {
 				plugin.getConfig().set("Groups." + groupName + "." + variable, false);
 				resultString = "false";
 			}
 		}
 		else {
 			plugin.getConfig().set("Groups." + groupName + "." + variable, resultString);
 		}
 		plugin.saveConfig();
 		plugin.reloadConfig();
 		
 		sender.sendMessage(ChatColor.GREEN + variable + " of " + groupName + " has been set to: '" + resultString + "'.");
 		return true;
 	}
 	
 	public boolean deleteGroup(String groupname, CommandSender sender) {
 		if (searchInGroups(groupname) == null) {
 			sender.sendMessage(ChatColor.RED + "Cannot delete '" + groupname + "', this group doesn't exist!");
 			return false;
 		}
 		plugin.getConfig().set("Groups." + groupname + ".displayname", null);
 		plugin.getConfig().set("Groups." + groupname + ".visible", null);
 		plugin.getConfig().set("Groups." + groupname + ".shortdescription", null);
 		plugin.getConfig().set("Groups." + groupname + ".longdescription", null);
 		plugin.getConfig().set("Groups." + groupname + ".members", null);
 		plugin.getConfig().set("Groups." + groupname, null);
 		
 		List<String> groups = plugin.getConfig().getStringList("Staff Groups");
 		groups.remove(groupname);
 		plugin.getConfig().set("Staff Groups", groups);
 		
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.GREEN + "Group '" + groupname + "' has been deleted!");
 		return true;
 	}
 	
 	public boolean addPlayerToGroup(String groupName, String playerName, CommandSender sender) {
 		
 		if (searchInGroups(groupName) == null) {
 			sender.sendMessage(ChatColor.RED + "Group '" + groupName + "' is not defined!");
 			return false;
 		}
 		
 		groupName = searchInGroups(groupName);
 		
 		if (!plugin.CommandExecutor.hasPermission("staffinfo.groups.addplayer." + groupName, sender)) return false;
 		
 		List<String> members = plugin.getConfig().getStringList("Groups." + groupName + ".members");
 		if (members.contains(playerName)) {
 			sender.sendMessage(ChatColor.RED + "Player '" +  playerName + "' cannot be added to the members list of " + groupName + " because (s)he is already on the list!");
 			return false;
 		}
 		
 		members.add(playerName);
 		plugin.getConfig().set("Groups." + groupName + ".members", members);
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.GREEN + "Player '" +  playerName + "' has been added to the members list of " + groupName);
 		return true;
 	}
 	
 	public boolean removePlayerFromGroup(String groupName, String playerName, CommandSender sender) {
 		
 		if (searchInGroups(groupName) == null) {
 			sender.sendMessage(ChatColor.RED + "Group '" + groupName + "' is not defined!");
 			return false;
 		}
 		groupName = searchInGroups(groupName);
 		if (!plugin.CommandExecutor.hasPermission("staffinfo.groups.removeplayer." + groupName, sender)) return true;
 		
 		List<String> members = plugin.getConfig().getStringList("Groups." + groupName + ".members");
 		if (!members.contains(playerName)) {
 			sender.sendMessage(ChatColor.RED + "Player '" +  playerName + "' cannot be removed from the members list of " + groupName + " because (s)he is not on the list!");
 			return false;
 		}
 		members.remove(playerName);
 		plugin.getConfig().set("Groups." + groupName + ".members", members);
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.GREEN + "Player '" +  playerName + "' has been removed from the members list of " + groupName);
 		return true;
 	}
 }
