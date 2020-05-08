 package com.legit2.Demigods;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.legit2.Demigods.Libraries.ReflectCommand;
 
 public class DCommandExecutor implements CommandExecutor
 {
 	static Demigods plugin;
 	
 	public DCommandExecutor(Demigods instance)
 	{
 		plugin = instance;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (command.getName().equalsIgnoreCase("dg")) return dg(sender,args);
 		else if (command.getName().equalsIgnoreCase("viewhashmaps")) return viewhashmaps(sender);
 		else if (command.getName().equalsIgnoreCase("check")) return check(sender);
 		else if (command.getName().equalsIgnoreCase("setalliance")) return setAlliance(sender,args);
 		else if (command.getName().equalsIgnoreCase("setfavor")) return setFavor(sender,args);
 		else if (command.getName().equalsIgnoreCase("setascensions")) return setAscensions(sender,args);
 		else if (command.getName().equalsIgnoreCase("setdevotion")) return setDevotion(sender,args);
 		else if (command.getName().equalsIgnoreCase("givedeity")) return giveDeity(sender,args);
 		
 		// BETA TESTING ONLY
 		else if (command.getName().equalsIgnoreCase("claim")) return claim(sender,args);
 		
 		return false;
 	}
 	
 	/*
 	 *  Command: "viewhashmaps"
 	 */
 	public static boolean viewhashmaps(CommandSender sender)
 	{
 		HashMap<String, Object> player_data = DSave.getAllPlayerData(sender.getName());	
 		HashMap<String, HashMap<String, Object>> player_deities = DSave.getAllDeityData(sender.getName());	
 
 		// Loop through player data entry set and add to database
 		for(Map.Entry<String, Object> entry : player_data.entrySet())
 		{
 			String id = entry.getKey();
 			Object data = entry.getValue();
 
 			sender.sendMessage(id + ": " + data.toString());
 		}
 		
 		for(Map.Entry<String, HashMap<String, Object>> deity : player_deities.entrySet())
 		{
 			String deity_name = deity.getKey();
 			HashMap<String, Object> deity_data = deity.getValue();
 			
 			for(Map.Entry<String, Object> entry : deity_data.entrySet())
 			{
 				String id = entry.getKey();
 				Object data = entry.getValue();
 				
 				sender.sendMessage(deity_name + ": " + id + ", " + data);
 			}
 		}
 		
 		return true;
 	}
 
 	/*
 	 *  Command: "dg"
 	 */
 	@ReflectCommand.Command(name = "dg", sender = ReflectCommand.Sender.PLAYER, permission = "demigods.basic")
 	public static boolean dg(CommandSender sender, String[] args)
 	{		
 		if(args.length > 0)
 		{
 			dg_info(sender, args);
 			return true;
 		}
 				
 		// Define Player
 		Player player = DUtil.definePlayer(sender);
 		
 		// Check Permissions
 		if(!DUtil.hasPermissionOrOP(player, "demigods.basic")) return DUtil.noPermission(player);
 		
 		DUtil.taggedMessage(sender, "Information Directory");
 		for(String alliance : DUtil.getLoadedDeityAlliances()) sender.sendMessage(ChatColor.GRAY + "/dg " + alliance.toLowerCase());
 		sender.sendMessage(ChatColor.GRAY + "/dg claim");
 		sender.sendMessage(ChatColor.GRAY + "/dg shrine");
 		sender.sendMessage(ChatColor.GRAY + "/dg tribute");
 		sender.sendMessage(ChatColor.GRAY + "/dg player");
 		sender.sendMessage(ChatColor.GRAY + "/dg pvp");
 		sender.sendMessage(ChatColor.GRAY + "/dg rankings");
 		if(DUtil.hasPermissionOrOP(player, "demigods.admin")) sender.sendMessage(ChatColor.RED + "/dg admin");
 		sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/check" + ChatColor.WHITE + " to see your player information.");
 		return true;
 	}
 
 	/*
 	 *  Command: "dg_info"
 	 */
 	@SuppressWarnings("unchecked")
 	public static boolean dg_info(CommandSender sender, String[] args)
 	{
 		// Define Player
 		Player player = DUtil.definePlayer(sender);
 		
 		// Define args
 		String category = args[0];
 		
 		// Check Permissions
 		if(!DUtil.hasPermissionOrOP(player, "demigods.basic")) return DUtil.noPermission(player);
 		
 		for(String alliance : DUtil.getLoadedDeityAlliances())
 		{
 			if(category.equalsIgnoreCase(alliance))
 			{
 				if(args.length < 2)
 				{
 					DUtil.taggedMessage(sender, alliance + " Directory");
 				
 					for(String deity : DUtil.getAllDeitiesInAlliance(alliance))
 					{
 						sender.sendMessage(ChatColor.GRAY + "/dg " + alliance.toLowerCase() + " " + deity.toLowerCase());
 					}
 				}
 				else
 				{
 					for(String deity : DUtil.getAllDeitiesInAlliance(alliance))
 					{
 						if(args[1].equalsIgnoreCase(deity))
 						{
 							try
 							{
 								for(String toPrint : (ArrayList<String>) DUtil.invokeDeityMethodWithString(DUtil.getDeityClass(deity), "getInfo", player.getName()))
 								{
 									sender.sendMessage(toPrint);
 								}
 								return true;
 							}
 							catch (Exception e)
 							{
 								sender.sendMessage(ChatColor.RED + "Something went wrong with deity loading.");
 								return true;
 							}
 						}
 					}
 					sender.sendMessage(ChatColor.DARK_RED + "No such deity, please try again.");
 					return false;
 				}
 			}
 		}
 		
 		if(category.equalsIgnoreCase("save"))
 		{
 			if(DUtil.hasPermissionOrOP(player, "demigods.admin"))
 			{
 				DUtil.serverMsg(ChatColor.RED + "Manually forcing Demigods save...");
 				if(DDatabase.saveAllData())
 				{
 					DUtil.serverMsg(ChatColor.GREEN + "Save complete!");
 				}
 				else
 				{
 					DUtil.serverMsg(ChatColor.RED + "There was a problem with saving...");
 					DUtil.serverMsg(ChatColor.RED + "An admin should check the log immediately.");
 				}
 			}
 			else DUtil.noPermission(player);
 		}
 		else if(category.equalsIgnoreCase("claim"))
 		{
 			DUtil.taggedMessage(sender, "Claiming");
 			sender.sendMessage(ChatColor.GRAY + " This is some info about Claiming.");
 		}
 		else if(category.equalsIgnoreCase("shrine"))
 		{
 			DUtil.taggedMessage(sender, "Shrines");
 			sender.sendMessage(ChatColor.GRAY + " This is some info about Shrines.");
 		}
 		else if(category.equalsIgnoreCase("tribute"))
 		{
 			DUtil.taggedMessage(sender, "Tributes");
 			sender.sendMessage(ChatColor.GRAY + " This is some info about Tributes.");
 		}
 		else if(category.equalsIgnoreCase("player"))
 		{
 			DUtil.taggedMessage(sender, "Players");
 			sender.sendMessage(ChatColor.GRAY + " This is some info about Players.");
 		}
 		else if(category.equalsIgnoreCase("pvp"))
 		{
 			DUtil.taggedMessage(sender, "PVP");
 			sender.sendMessage(ChatColor.GRAY + " This is some info about PVP.");
 		}
 		else if(category.equalsIgnoreCase("stats"))
 		{
 			DUtil.taggedMessage(sender, "Stats");
 			sender.sendMessage(ChatColor.GRAY + " These are some stats for Demigods.");
 		}
 		else if(category.equalsIgnoreCase("rankings"))
 		{
 			DUtil.taggedMessage(sender, "Rankings");
 			sender.sendMessage(ChatColor.GRAY + " This is some ranking info about Demigods.");
 		}
 		else if(category.equalsIgnoreCase("admin"))
 		{
 			DUtil.taggedMessage(sender, ChatColor.RED + "Admin Commands");
 			sender.sendMessage(ChatColor.GRAY + "/setalliance <player> <alliance>");
 			sender.sendMessage(ChatColor.GRAY + "/givedeity <player> <deity>");
 			sender.sendMessage(ChatColor.GRAY + "/setdevotion <player> <deity> <amount>");
 			sender.sendMessage(ChatColor.GRAY + "/setfavor <player> <amount>");
 			sender.sendMessage(ChatColor.GRAY + "/setascensions <player> <amount>");
 		}
 		
 		return true;
 	}
 	
 	/*
 	 *  Command: "check"
 	 */
 	public static boolean check(CommandSender sender)
 	{
 		// Define Player and Username
 		Player player = DUtil.definePlayer(sender);
 		String username = player.getName();
 		
 		if(!DUtil.isImmortal(username))
 		{
 			player.sendMessage(ChatColor.RED + "You cannot use that command, mortal.");
 			return true;
 		}
 				
 		// Define variables
 		HashMap<String, Object> player_data = DUtil.getAllPlayerData(username);
 		HashMap<String, HashMap<String, Object>> player_deities = DUtil.getAllDeityData(username);
 
 		String favor = null;
 		String ascensions = null;
 		String kills = null;
 		String deaths = null;
 		String alliance = null;
 		ArrayList<String> deity_list = new ArrayList<String>();
 		
 		// Loop through player data entry set and them to variables
 		for(Map.Entry<String, Object> entry : player_data.entrySet())
 		{
 			String id = entry.getKey();
 			Object data = entry.getValue();
 
 			// Don't save if it's temporary data
 			if(id.equalsIgnoreCase("alliance")) alliance = data.toString();
 			if(id.equalsIgnoreCase("favor")) favor = data.toString();
 			if(id.equalsIgnoreCase("ascensions")) ascensions = data.toString();
 			if(id.equalsIgnoreCase("kills")) kills = data.toString();
 			if(id.equalsIgnoreCase("deaths")) deaths = data.toString();
 		}
 		
 		// Loop through deity data entry set and add them to variables
 		for(Map.Entry<String, HashMap<String, Object>> deity : player_deities.entrySet())
 		{
 			String deity_name = deity.getKey();
 			HashMap<String, Object> deity_data = deity.getValue();
 			
 			for(Map.Entry<String, Object> entry : deity_data.entrySet())
 			{
 				// Create variables
 				String id = entry.getKey();
 				Object data = entry.getValue();
 				String devotion = null;
 				
 				// Don't save if it's temporary data
 				if(id.contains("devotion"))
 				{
 					devotion = data.toString();
 					deity_list.add(deity_name + " [" + devotion + "]");
 				}
 			}
 		}
 		
 			
 		// Send the user their info via chat
 		DUtil.taggedMessage(sender, "Player check: " + ChatColor.YELLOW + username);
 		sender.sendMessage("Alliance: " + ChatColor.DARK_GREEN + alliance);
 		sender.sendMessage("Deities: " + ChatColor.DARK_GREEN + deity_list.toString());
 		sender.sendMessage("Favor: " + ChatColor.GREEN + favor);
 		sender.sendMessage("Ascensions: " + ChatColor.GREEN + ascensions);
 		sender.sendMessage(" ");
 		sender.sendMessage("Kills: " + ChatColor.GREEN + kills + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + deaths);
 	
 		return true;
 	}
 	
 	/*
 	 *  Command: "setalliance"
 	 */
 	public static boolean setAlliance(CommandSender sender, String[] args)
 	{	
 		if(args.length != 2) return false;
 		
 		// Define args
 		String username = args[0];
 		String alliance = args[1];
 		
 		DUtil.setAlliance(username, alliance);
 		sender.sendMessage(ChatColor.YELLOW + "You've given " + alliance + " to " + username + "!");
 		
 		return true;
 	}
 	
 	/*
 	 *  Command: "setfavor"
 	 */
 	public static boolean setFavor(CommandSender sender, String[] args)
 	{
 		if(args.length != 2) return false;
 		
 		// Define args
 		String username = args[0];
 		Integer favor = new Integer(args[1]);
 		
 		DUtil.setFavor(username, favor);
 		sender.sendMessage(ChatColor.YELLOW + "You've set " + username + "'s " + ChatColor.GREEN + "favor " + ChatColor.YELLOW + "to " + ChatColor.GREEN + favor +  ChatColor.YELLOW + "!");
 		
 		return true;
 	}
 	
 	/*
 	 *  Command: "setascensions"
 	 */
 	public static boolean setAscensions(CommandSender sender, String[] args)
 	{
 		if(args.length != 2) return false;
 		
 		// Define args
 		String username = args[0];
 		Integer ascensions = new Integer(args[1]);
 		
 		DUtil.setAscensions(username, ascensions);
 		sender.sendMessage(ChatColor.YELLOW + "You've set " + username + "'s " + ChatColor.GREEN + "ascensions " + ChatColor.YELLOW + "to " + ChatColor.GREEN + ascensions +  ChatColor.YELLOW + "!");
 		
 		return true;
 	}
 	
 	/*
 	 *  Command: "setdevotion"
 	 */
 	public static boolean setDevotion(CommandSender sender, String[] args)
 	{
 		if(args.length != 3) return false;
 		
 		// Define args
 		String username = args[0];
 		String deity = args[1];
 		Integer devotion = new Integer(args[2]);
 		
 		DUtil.setDevotion(username, deity, devotion);
 		sender.sendMessage(ChatColor.YELLOW + "You've set " + username + "'s " + ChatColor.GREEN + "devotion " + ChatColor.YELLOW + "for " + ChatColor.GREEN + deity + ChatColor.YELLOW + " to " + ChatColor.GREEN + devotion +  ChatColor.YELLOW + "!");
 		
 		return true;
 	}
 	
 	/*
 	 *  Command: "givedeity"
 	 */
 	public static boolean giveDeity(CommandSender sender, String[] args)
 	{
 		if(args.length != 2) return false;
 		
 		// Define args
 		String username = args[0];
 		String deity = args[1];
 		
 		DUtil.giveDeity(username, deity);
 		DUtil.setImmortal(username, true);
 		DUtil.setFavor(username, 500);
 		DUtil.setAscensions(username, 9);
 		DUtil.setDevotion(username, deity, 900);
 		DUtil.setKills(username, 2);
 		
 		sender.sendMessage(ChatColor.YELLOW + "You've given " + deity + " to " + username + "!");
 		
 		return true;
 	}
 	
 	/*
 	 *  Command: "claim"
 	 */
 	public static boolean claim(CommandSender sender, String[] args)
 	{
 		Player player = DUtil.definePlayer(sender);
 		
 		if(player == null) return DUtil.noConsole(sender);
 		
 		if(args.length != 1) return false;
 		
 		// Define args
 		String username = player.getName();
 		String deity = args[0];
 		
 		ArrayList<String> loadedDeities = DUtil.getLoadedDeityNames();
 		
 		if(!loadedDeities.contains(deity))
 		{
 			player.sendMessage(ChatColor.RED + "That deity does not exist.");
 			return false;
 		}
 		
 		String alliance;
 		Boolean firstTime = false;
 		
 		if(DUtil.getAlliance(username) == (String) null) 
 		{
 			alliance = DUtil.getDeityAlliance(deity);
 			firstTime = true;
 		}
 		else alliance = DUtil.getAlliance(username);
 		
		if(!alliance.equalsIgnoreCase(DUtil.getDeityAlliance(deity)))
 		{
 			player.sendMessage(ChatColor.RED + "You cannot claim a deity from another alliance.");
 			return true;
 		}
 		
 		if(DUtil.hasDeity(username, deity))
 		{
 			player.sendMessage(ChatColor.RED + "You cannot claim a the same deity twice.");
 			return true;
 		}
 		
 		ArrayList<Material> claimItems = DUtil.getDeityClaimItems(args[0]);
 		int needs = claimItems.size();
 		int count = 0;
 			
 		for(Material claimItem : claimItems)
 		{
 			if(player.getInventory().contains(claimItem))
 			{
 				count++;
 			}
 		}
 		
 		if(count != needs)
 		{
 			player.sendMessage(ChatColor.RED + "You do not have the correct claim items.");
 			return true;
 		}
 		
 		DUtil.giveDeity(username, deity);
 		if(firstTime) DUtil.setAlliance(username, alliance);
 		if(firstTime) DUtil.setImmortal(username, true);
 		if(firstTime) DUtil.setFavor(username, 500);
 		if(firstTime) DUtil.setAscensions(username, 9);
 		DUtil.setDevotion(username, deity, 900);
 		if(firstTime) DUtil.setKills(username, 2);
 		
 		player.sendMessage(ChatColor.YELLOW + "You've claimed " + deity + "!");
 		
 		return true;
 	}
 }
