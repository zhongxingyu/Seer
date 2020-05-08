 package com.amazar.plugin;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import com.amazar.utils.*;
 
 public class acCommandExecutor implements CommandExecutor {
 private Plugin plugin;
 public acCommandExecutor(ac plugin) {
 	this.plugin = plugin;
 } //test
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
 			String[] args) {
 		String cmdname = commandLabel;
 		Player player = null;
 		if(sender instanceof Player){
 			player = (Player) sender;
 		}
 		if(cmd.getName().equalsIgnoreCase("who")){
 			//TODO list players online
 			String msg = "Online players:" + ChatColor.DARK_RED;
 			Player[] players = plugin.getServer().getOnlinePlayers();
 			for(int i=0;i<players.length;i++){
 				Player p = players[i];
 				String name = p.getName();
 				msg = msg + " " + name + ",";
 			}
 			sender.sendMessage(ChatColor.GOLD + msg);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("trainme")){
 			if(args.length < 1){
 				//no args
 				return false;
 			}
 			String skill = args[0];
 			if(skill.equalsIgnoreCase("worldguard")){
 				//TODO worldguard help
                 sender.sendMessage(ChatColor.GRAY + "Type //wand to get the wooden axe cuboid selection tool. You select the corners of the cuboid area" +
                 		" you wish to protect. You now do /region define [Region name] [Player],[Player],...    For more info do /help worldguard");//Line 1
 				return true;
 			}
 			else if(skill.equalsIgnoreCase("warns")){
 				sender.sendMessage(ChatColor.GRAY + "To warn somebody do /warn [Player] [Reason]. To view all recent server warns do " +
 						"/warnslog or /warnslog clear to reset it. To view and individuals warns do /view-warns [Player]. Then to delete their warns do /delete-warns [Name]");
 				return true;
 			}
 			else{
 				sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.GRAY + "invalid skill! " +ChatColor.GOLD +"Valid skills are: worldguard, warns");
 			}
 			return true;
 		}
 		
 		else if(cmd.getName().equalsIgnoreCase("vote")){
 			ArrayList<String> info =  ac.vote.getValues();
 			//sender.sendMessage(ChatColor.RED + "Voting info:");
 			
 			String listString = "";
 			
 			//String newLine = System.getProperty("line.separator");
 
 			for (String s : info)
 			{
 			    listString += s + " %n";
 			}
 			//sender.sendMessage(playerName + " " + listString);
 			String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 			
 					for(int x=0 ; x<message.length ; x++) {
 					sender.sendMessage(ChatColor.GOLD +  StringColors.colorise(message[x])); // Send each argument in the message
 					}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("c")){
 			if(player == null){
 				return true;
 			}
 			//TODO
 			if(args.length < 1){
 				return false;
 			}
 			if(args[0].equalsIgnoreCase("list")){
 				ArrayList<String> info =  ac.clans.getValues();
 				sender.sendMessage(ChatColor.RED + "Clans:");
 				
 				String listString = "";
 				
 				//String newLine = System.getProperty("line.separator");
 
 				for (String s : info)
 				{
 				    listString += s + " %n";
 				}
 				//sender.sendMessage(playerName + " " + listString);
 				String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 				
 						for(int x=0 ; x<message.length ; x++) {
 						sender.sendMessage(ChatColor.GOLD +  StringColors.colorise(message[x])); // Send each argument in the message
 						}
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("join")){
 				if(!(sender.hasPermission("ac.clan.join"))){
 					sender.sendMessage(ChatColor.RED + "You don't have the permission ac.clan.join");
 					return true;
 				}
 				if(args.length < 2){
 					sender.sendMessage(ChatColor.RED + "Usage: /c join [Name]");
 				return true;
 				}
 				String newClan = args[1];
 				boolean exists = false;
 				//TODO
 				Object[] clans =  ac.clans.getValues().toArray();
 				for(int i=0; i<clans.length;i++){
 					String clan = (String) clans[i];
 					if(ChatColor.stripColor(newClan).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(StringColors.colorise(clan)))){
 						exists = true;
 						newClan = clan;
 					}
 				}
 				if(!exists){
 					sender.sendMessage(ChatColor.RED + "Clan doesn't exist! Do /c list for a list of them!");
 					return true;
 				}
 				if(ac.clanMembers.containsKey(sender.getName())){
 					ac.clanMembers.remove(sender.getName());
 				}
 				ac.clanMembers.put(sender.getName(), newClan);
 				ac.saveHashMap(ac.clanMembers, plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 				ac.clanMembers = ac.loadHashMapString(plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are now in the " + StringColors.colorise(newClan) + ChatColor.GOLD + " clan!");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("view")){
 				String name = sender.getName();
 				try {
 					if(!(ac.clanMembers.containsKey(name))){
 						sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently not in a clan!");
 						return true;
 					}
 				} catch (Exception e) {
 					sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently not in a clan!");
 					return true;
 				}
 				String clanName = ac.clanMembers.get(name);
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently in the " + StringColors.colorise(clanName) + ChatColor.GOLD + " clan!");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("invite")){
 				if(args.length < 2){
 					sender.sendMessage("Usage: /" + cmdname + " invite [Name]");
 					return true;
 				}
 				String name = sender.getName();
 				try {
 					if(!(ac.clanMembers.containsKey(name))){
 						sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently not in a clan!");
 						return true;
 					}
 				} catch (Exception e) {
 					sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently not in a clan!");
 					return true;
 				}
 				String clanName = ac.clanMembers.get(name);
 				String nameToJoin = args[1];
 				//sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently in the " + StringColors.colorise(clanName) + ChatColor.GOLD + " clan!");
 				Object[] players = plugin.getServer().getOfflinePlayers();
 				OfflinePlayer invitee = null;
 				boolean found = false;
 				for(int i=0; i<players.length; i++){
 					if(((String)((OfflinePlayer) players[i]).getName()).equalsIgnoreCase(nameToJoin)){
 						found = true;
 						nameToJoin = ((String)((OfflinePlayer) players[i]).getName());
 						invitee = ((OfflinePlayer) players[i]);
 					}
 				}
 				if(!found){
 					sender.sendMessage(ChatColor.RED + "Player has not been on this server!");
 					return true;
 				}
 				if(invitee.isOnline()){
 					Player toJoin = plugin.getServer().getPlayer(nameToJoin);
 					toJoin.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You have been invited to join the " + StringColors.colorise(clanName) + ChatColor.RESET + "" + ChatColor.GOLD + " clan. Do /c accept to join it!");
 				}
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "Sent an invite request to " + nameToJoin + " to join " + StringColors.colorise(clanName));
 				ac.clanInvites.put(nameToJoin, ChatColor.stripColor(clanName));
 				ac.saveHashMap(ac.clanInvites, plugin.getDataFolder().getAbsolutePath() + File.separator + "cinvites.bin");
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("accept")){
 				String name = sender.getName();
 				if(!ac.clanInvites.containsKey(name)){
 					sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You don't have any clan invites to accept!");
 				    return true;
 				}
 				boolean inaClan = false;
 				try {
 					if((ac.clanMembers.containsKey(name))){
                      inaClan = true;
 					}
 				} catch (Exception e) {
 				}
 				if(inaClan){
 					sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are already in a clan! Do /c leave first before joining another one!");
 				    return true;
 				}
 				String newClan = ChatColor.stripColor(StringColors.colorise(ac.clanInvites.get(name)));
 				boolean exists = false;
 				Object[] clans =  ac.clans.getValues().toArray();
 				for(int i=0; i<clans.length;i++){
 					String clan = (String) clans[i];
 					if(ChatColor.stripColor(newClan).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(StringColors.colorise(clan)))){
 						exists = true;
 						newClan = clan;
 					}
 				}
 				if(!exists){
 					sender.sendMessage(ChatColor.RED + "Clan "+newClan+" doesn't exist! Do /c list for a list of them!");
 					return true;
 				}
 				if(ac.clanMembers.containsKey(sender.getName())){
 					ac.clanMembers.remove(sender.getName());
 				}
 				ac.clanMembers.put(sender.getName(), newClan);
 				ac.saveHashMap(ac.clanMembers, plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 				ac.clanMembers = ac.loadHashMapString(plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are now in the " + StringColors.colorise(newClan) + ChatColor.GOLD + " clan!");
 				ac.clanInvites.remove(name);
 				ac.saveHashMap(ac.clanInvites, plugin.getDataFolder().getAbsolutePath() + File.separator + "cinvites.bin");
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("leave")){
 				
 				if(ac.clanMembers.containsKey(sender.getName())){
 					ac.clanMembers.remove(sender.getName());
 				}
 				else{
 					sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are currently not in a clan!");
 				}
 				ac.saveHashMap(ac.clanMembers, plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You are no longer in a clan!");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("create")){
 				//TODO
 				if(args.length < 2){
 					sender.sendMessage(ChatColor.RED + "Usage: /c create [Name]");
 				return true;
 				}
 				String newClan = args[1];
 				boolean exists = false;
 				Object[] clans =  ac.clans.getValues().toArray();
 				for(int i=0; i<clans.length;i++){
 					String clan = (String) clans[i];
 					if(clan.toLowerCase() == ChatColor.stripColor(newClan).toLowerCase()){
 						exists = true;
 					}
 				}
 				if(exists){
 					sender.sendMessage(ChatColor.RED + "Clan already exists Do /c delete to delete it!");
 					return true;
 				}
 				String toPut = ChatColor.stripColor(newClan);
 				ac.clans.add(toPut);
 				ac.clans.save();
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "Clan created!");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("delete")){
 				//TODO
 				if(args.length < 2){
 					sender.sendMessage(ChatColor.RED + "Usage: /c delete [Name]");
 				return true;
 				}
 				String newClan = args[1];
 				boolean exists = false;
 				Object[] clans =  ac.clans.getValues().toArray();
 				for(int i=0; i<clans.length;i++){
 					String clan = (String) clans[i];
 					if(ChatColor.stripColor(StringColors.colorise(clan)).equalsIgnoreCase(args[1])){
 						exists = true;
 						newClan = clan;
 					}
 				}
 				if(!exists){
 					sender.sendMessage(ChatColor.RED + "Clan doesn't exist!");
 					return true;
 				}
 				String toPut = ChatColor.stripColor(newClan);
 				ac.clans.remove(toPut);
 				ac.clans.save();
 				sender.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "Clan deleted!");
 				return true;
 			}
 			return false;
 		}
 		else if (cmd.getName().equalsIgnoreCase("package")){
 			Object[] packages = ac.packages.values.toArray();
 			if(args.length < 1){
 				sender.sendMessage("Usage: /" + cmdname + " [Name]");
 				sender.sendMessage(ChatColor.RED + "Valid packages are:");
 				for(int i = 0;i<packages.length;i++){
 					String info = (String) packages[i];
 					String[] parts = info.split(":");
 					if(parts.length < 1){
 						return true;
 					}
 					String thePackageName = parts[0];
 					sender.sendMessage(ChatColor.GOLD + thePackageName);
 				}
 				return true;
 			}
 			String packageName = args[0];
 			boolean found = false;
 			for(int i = 0; i<packages.length; i++){
 				String line = (String) packages[i];
 				//String[] parts = line.split(":", 1);
 				if(line.toLowerCase().startsWith(packageName.toLowerCase())){
 					found = true;
 					sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Package info for the " + packageName +" package:");
 					line = line.replaceFirst("(?i)"+packageName + ":", "");
 					line = StringColors.colorise(line);
 					String[] lines = line.split("%n");
 					for(int z = 0; z<lines.length;z++){
 						sender.sendMessage(lines[z]);
 					}
 				}
 			}
 			if(!found){
 				sender.sendMessage(ChatColor.RED + "Valid packages are:");
 				for(int i = 0;i<packages.length;i++){
 					String info = (String) packages[i];
 					String[] parts = info.split(":");
 					if(parts.length < 1){
 						return true;
 					}
 					String thePackageName = parts[0];
 					sender.sendMessage(ChatColor.GOLD + thePackageName);
 				}
 				return true;
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("logchat")){
 			if(args.length<1){
 				return false;
 			}
 			String msg = "";
 			for(int i=0;i<args.length;i++){
 				msg = msg + args[i] + " ";
 			}
 			msg = StringColors.colorise(msg);
 			String[] lines = msg.split("%n");
 			for(int i=0;i<lines.length;i++){
 				Bukkit.broadcastMessage(lines[i]);
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("logchatp")){
 			if(args.length<2){
 				return false;
 			}
 			String playername = args[0];
 			Player p = plugin.getServer().getPlayer(playername);
 			if(p == null){
 				sender.sendMessage(ChatColor.RED + "Unable to find player " + playername);
 				return true;
 			}
 			String msg = "";
 			for(int i=1;i<args.length;i++){
 				msg = msg + args[i] + " ";
 			}
 			msg = StringColors.colorise(msg);
 			String[] lines = msg.split("%n");
 			for(int i=0;i<lines.length;i++){
 				p.sendMessage(lines[i]);
 			}
			sender.sendMessage(ChatColor.RED + "Send to " + ChatColor.GOLD + p.getName());
 			return true;
 		}
 	else if (cmd.getName().equalsIgnoreCase("accommands")){ // If the player typed /setlevel then do the following...
 			  PluginDescriptionFile desc = plugin.getDescription();
 			  Map<String, Map<String, Object>> cmds = desc.getCommands();
 			  Set<String> keys = cmds.keySet();
 			  Object[] commandsavailable = keys.toArray();
 			  int displayed = 0;
 			  int page = 1;
 			  if (args.length < 1){
 				  page = 1;
 			  }
 			  else {
 				  try {
 					page = Integer.parseInt(args[0]);
 				} catch (Exception e) {
 					sender.sendMessage(ChatColor.RED + "Given page number is not a number!");
 					return true;
 				}
 			  }
 			  int startpoint = (page - 1) * 3;
 			  double tot = keys.size() / 3;
 			  double total = (double)Math.round(tot * 1) / 1;
 			  total += 1;
 			  if (page > total || page < 1){
 				  sender.sendMessage(ChatColor.RED + "Invalid page number!");
 				  return true;
 			  }
 			  int totalpages = (int) total;
 			  sender.sendMessage(ChatColor.DARK_GREEN + "Page: [" + page + "/" + totalpages + "]");
 			  for(int i = startpoint; displayed < 3 && i<commandsavailable.length; i++) {
 				  String v = commandsavailable[i].toString();
 				  /*
 				  try {
 					  v = commandsavailable[i].toString();
 				} catch (Exception e) {
 					return true;
 				}
 				*/
 				  boolean doit = true;
 				  if(v == null){
 					  doit = false;
 				  }
 				  Map<String, Object> vmap = cmds.get(v);
 				    @SuppressWarnings("unused")
 					Set<String> commandInfo = null;
 				    String usage = null;
 				    String description = null;
 				    String perm = null;
 				    
 				    try{
 				    	commandInfo = vmap.keySet();
 					    usage = vmap.get("usage").toString();
 					    description = vmap.get("description").toString();
 					    perm = vmap.get("permission").toString();
 				    }
 				    catch(Exception e){
 				    	Bukkit.broadcastMessage("unable to retrieve command data (jam2400 edited plugin.yml incorrectly)"); //SHOULDNT happen
 				    	doit = false;
 				    }
 				    if(doit){
 				    
 				    	@SuppressWarnings("unchecked")
 						List<String> aliases = (List<String>) vmap.get("aliases");
 					    usage = usage.replaceAll("<command>", v);
 			        	sender.sendMessage(ChatColor.GOLD + usage + ChatColor.RED + " Description: " + ChatColor.DARK_PURPLE + description);
 			        	sender.sendMessage(ChatColor.RED + " Aliases: " + ChatColor.DARK_PURPLE + aliases);
 			        	sender.sendMessage(ChatColor.RED + " Permission: " + ChatColor.DARK_PURPLE + perm);	
 				    
 				    }
 				    else{
 				    	//nothing
 				    }
 		        	displayed++;
 					}
 			  int next = page + 1;
 			  if (next < total + 1){
 			  sender.sendMessage(ChatColor.DARK_GREEN+ "Do /accommands " + next + " for the next page");
 			  }
 			  return true;
 	}
 		
 	else if(cmd.getName().equalsIgnoreCase("firework")){
 		if(!(sender instanceof Player)){
 			sender.sendMessage(ChatColor.RED + "This command is for players");
 			return true;
 		}
 		if(args.length < 5){
 			return false;
 		}
 		String height = args[0];
 		String col1 = args[1];
 		String col2 = args[2];
 		String fade = args[3];
 		String fade2 = args[4];
 		String type = args[5];
 		String flicker = args[6];
 		String trail = args[7];
 		boolean doFlicker = false;
 		boolean doTrail = false;
 		if(flicker.equalsIgnoreCase("true")){
 			doFlicker = true;
 		}
 		else if(flicker.equalsIgnoreCase("false")){
 			doFlicker = false;
 		}
 		else{
 			return false;
 		}
 		if(trail.equalsIgnoreCase("true")){
 			doTrail = true;
 		}
 		else if(trail.equalsIgnoreCase("false")){
 			doTrail = false;
 		}
 		else{
 			return false;
 		}
 		int amount = 0;
 		try {
 			amount = Integer.parseInt(args[8]);
 		} catch (NumberFormatException e) {
 			sender.sendMessage(ChatColor.RED + "The amount specified is not an integer.");
 			return true;
 		}
 		ItemStack item = new ItemStack(Material.FIREWORK, 1);
 	    FireworkMeta fmeta = (FireworkMeta) item.getItemMeta();
 		if(height.equalsIgnoreCase("1")){
 			fmeta.setPower(1);
 		}
 		else if(height.equalsIgnoreCase("2")){
 			fmeta.setPower(2);
 		}
 		else if(height.equalsIgnoreCase("3")){
 			fmeta.setPower(3);
 		}
 		else{
 			sender.sendMessage(ChatColor.RED + "The firework height must be either 1, 2 or 3");
 			return true;
 		}
 		//Set the height
 		org.bukkit.FireworkEffect.Builder effect = FireworkEffect.builder();
 		if(type.equalsIgnoreCase("small")){
 			Type effectType = FireworkEffect.Type.BALL;
 			effect.with(effectType);
 		}
 		else if(type.equalsIgnoreCase("big")){
 			effect.with(FireworkEffect.Type.BALL_LARGE);
 		}
 		else if(type.equalsIgnoreCase("burst")){
 			effect.with(FireworkEffect.Type.BURST);
 		}
 		else if(type.equalsIgnoreCase("creeper")){
 			effect.with(FireworkEffect.Type.CREEPER);
 		}
 		else if(type.equalsIgnoreCase("star")){
 			effect.with(FireworkEffect.Type.STAR);
 		}
 		else if(type.equalsIgnoreCase("epic_creeper")){
 			org.bukkit.FireworkEffect.Builder Epiceffect = FireworkEffect.builder();
 			Epiceffect.flicker(true);
 			Epiceffect.trail(true);
 			Epiceffect.withColor(Color.RED, Color.YELLOW, Color.ORANGE);
 			Epiceffect.with(Type.STAR);
 			FireworkEffect explosion = Epiceffect.build();
 			fmeta.addEffect(explosion);
 			effect.with(FireworkEffect.Type.CREEPER);
 		}
 		else{
 			sender.sendMessage(ChatColor.RED + "Invalid type - Valid types are: small, big, burst, creeper, epic_creeper, star");
 			return true;
 		}
 		if(doFlicker){
 			effect.withFlicker();
 		}
 		if(doTrail){
 			effect.withTrail();
 		}
 		Color color1 = getColor.getColorFromString(col1);
 		if(color1 == null){
 			sender.sendMessage(ChatColor.RED + "Error: invalid first color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 			return true;
 		}
 		Color color2 = getColor.getColorFromString(col2);
 		if(color2 == null){
 			sender.sendMessage(ChatColor.RED + "Error: invalid second color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 			return true;
 		}
 		Color fader = getColor.getColorFromString(fade);
 		if(fader == null){
 			sender.sendMessage(ChatColor.RED + "Error: invalid first fade color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 			return true;
 		}
 		Color fader2 = getColor.getColorFromString(fade2);
 		if(fader2 == null){
 			sender.sendMessage(ChatColor.RED + "Error: invalid second fade color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 			return true;
 		}
 		effect.withFade(fader, fader2);
 		effect.withColor(color1, color2);
 		FireworkEffect teffect = effect.build();
 		fmeta.addEffect(teffect);
 		item.setItemMeta(fmeta);
 		item.setAmount(amount);
 		sender.sendMessage(ChatColor.GOLD + "Created firework");
 		player.getInventory().addItem(item);
 		return true;
 	}
 	else if(cmd.getName().equalsIgnoreCase("news")){
 		ArrayList<String> vals = ac.news.values;
 		Object[] news = vals.toArray();
 		if(args.length < 1){
 			sender.sendMessage(ChatColor.RED + "Current news:");
 			boolean isNews = false;
 			for(int i=0;i<news.length;i++){
 				isNews = true;
 				String line = (String) news[i];
 				line = StringColors.colorise(line);
 				sender.sendMessage(ChatColor.GOLD + line);
 				
 			}
 			if(!isNews){
 				sender.sendMessage(ChatColor.GOLD + "-none-");	
 			}
 			return true;
 		}
 		else{
 			String article = args[0];
 			boolean found = false;
 			sender.sendMessage(ChatColor.RED + "News matching '"+article+"':");
 			for(int i=0;i<news.length;i++){
 				String line = (String) news[i];
 				line = StringColors.colorise(line);
 				if(ChatColor.stripColor(line).toLowerCase().startsWith("["+article.toLowerCase()+"]")){
 					found = true;
 					sender.sendMessage(ChatColor.GOLD + line);
 				}
 			}
 			if(found == false){
 				sender.sendMessage(ChatColor.GOLD + "-none-");
 			}
 		}
 		return true;
 	}
 	else if(cmd.getName().equalsIgnoreCase("createnews")){
 		if(args.length < 2){
 			return false;
 		}
 		String article = "[" + args[0] + "]";
 		String story = "";
 		for(int i=1;i<args.length;i++){
 			story = story + args[i] + " ";
 		}
 		ac.news.add(article + " " + story);
 		ac.news.save();
 		sender.sendMessage(ChatColor.GOLD + "News story created!");
 		return true;
 	}
 	else if(cmd.getName().equalsIgnoreCase("deletenews")){
 		if(args.length < 1){
 			return false;
 		}
 		String article = ChatColor.stripColor(StringColors.colorise(args[0]));
 		ArrayList<String> vals = ac.news.values;
 		Object[] news = vals.toArray();
 		boolean found = false;
 		for(int i=0;i<news.length;i++){
 			String line = (String) news[i];
 			if(ChatColor.stripColor(StringColors.colorise(line)).toLowerCase().startsWith("["+article.toLowerCase()+"]")){
 				found = true;
 				ac.news.remove(line);
 				ac.news.save();
 			}
 		}
 		if(!found){
 			sender.sendMessage(ChatColor.RED + "Article not found!");
 			return true;
 		}
 		sender.sendMessage(ChatColor.GOLD + "Deleted article!");
 		return true;
 	}
 	else if(cmd.getName().equalsIgnoreCase("acupdate")){
 		//TODO
 		try {
 			String PathP = "https://dl.dropbox.com/u/50672767/amazarplugin/amazar.jar";
 			sender.sendMessage(ChatColor.GOLD + "Downloading update from " + PathP);
 				 URL update = new URL(PathP);
 				 InputStream inUp = new BufferedInputStream(update.openStream());
 				 ByteArrayOutputStream outUp = new ByteArrayOutputStream();
 				 byte[] buf = new byte[1024];
 				 int n = 0;
 				 while (-1!=(n=inUp.read(buf)))
 				 {
 				    outUp.write(buf, 0, n);
 				 }
 				 outUp.close();
 				 inUp.close();
 				 byte[] responseUp = outUp.toByteArray();
 				 (new File(plugin.getDataFolder().getParent() + File.separator + plugin.getServer().getUpdateFolder())).mkdirs();
 				 FileOutputStream fos = new FileOutputStream(new File(plugin.getDataFolder().getParent() + File.separator + plugin.getServer().getUpdateFolder() + File.separator + "amazar.jar"));
 				     fos.write(responseUp);
 				     fos.close();
 				     sender.sendMessage("Successfully updated attempting to reload server...");
 				     plugin.getServer().reload();
 		
 		} catch (Exception e) {
 			sender.sendMessage(ChatColor.RED + "Failed to update");
 		}
 		return true;
 	}
 	else if (cmd.getName().equalsIgnoreCase("warn")){ // If the player typed /burn then do the following...
 		boolean isenabled = true;
 	       if(isenabled == true){
 	    	   
 		        	if(args.length < 1)
 		        	{
 		        	    //No arguments given!
 		        		sender.sendMessage("Usage: " + cmdname + " [Player] [Reason]");
 		        	}
 		        	else{
 					StringBuilder warnmsg = new StringBuilder();
 					for (int i = 1; i < args.length; i++) {
 					    if (i != 0)
 					         warnmsg.append(" ");
 					    warnmsg.append(args[i]);
 					}
 				
 				Player check = Bukkit.getPlayer(args[0]);
 		        Player target = plugin.getServer().getPlayer(args[0]); // Gets the player who was typed in the command.
 		        // For instance, if the command was "/ignite notch", then the player would be just "notch".
 		        // Note: The first argument starts with [0], not [1]. So arg[0] will get the player typed.
 		        if(check != null){
 
 		        	
 		        target.sendMessage(ChatColor.RED + "You have been warned " + ChatColor.GOLD + "for" + warnmsg);
 		        plugin.getLogger().info(target.getName() + " has been warned "+"for" + warnmsg);
 		        sender.sendMessage("Warning sent!");
 		        boolean sendtoall = true;
 		        if (sendtoall == true) {
 		        	plugin.getServer().broadcastMessage(ChatColor.RED + target.getName() + " has been warned " + " " + ChatColor.GOLD + "for" + warnmsg);
 		        	ac.warns.add("" + target.getName() + " has been warned by " + sender.getName() + " for" + warnmsg);
 		        	ac.warns.save();
 		        	String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 		        	File playerFile = new File(pluginFolder + File.separator + "warns" + File.separator + target.getName() + ".txt");
 		        	if(!(playerFile.exists()) || playerFile.length() < 1){
 		        		try {
 							playerFile.createNewFile();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 		        	}
 		        	ac.warnsplayer = new com.amazar.utils.ListStore(playerFile);
 		        	ac.warnsplayer.load();
 		        	ac.warnsplayer.add("* Warned"+" for" + warnmsg);
 		        	ac.warnsplayer.save();
 		        }
 		        else {
 		        
 		        }
 		        }
 		        	
 		        	
 		        
 		        else {
 		        	sender.sendMessage(ChatColor.RED + "Player not found!");
 		        }
 		        	}
 				
 				}
 		        else {
 		        	return true;	
 		        }
 		
 		return true;
 	}
 	else if (cmd.getName().equalsIgnoreCase("delete-warns")){ // If the player typed /view-warns then do the following...
 		boolean isenabled = true;
 	       if(isenabled == true){
 	    	   if (player == null) {
 					sender.sendMessage("This command can only be used by a player");
 				} else {
 		        	if(args.length < 1)
 		        	{
 		        	    //No arguments given!
 		        		sender.sendMessage("Usage /" + cmdname + " [name]");
 		        	}
 		        	else{
 					String playerName = args[0];
 					String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 					File playerFile = new File(pluginFolder + File.separator + "warns" + File.separator + playerName + ".txt");
 					if(playerFile.exists() && playerFile.length() > 1){
 					playerFile.delete();
 					}
 					sender.sendMessage(ChatColor.RED + playerName + "'s warning's have been deleted.");
 		        	}
 				}
 				}
 		        else {
 		        	return true;	
 		        }
 		
 		return true;
 	}
 	else if (cmd.getName().equalsIgnoreCase("view-warns")){ // If the player typed /view-warns then do the following...
 		boolean isenabled = true;
 	       if(isenabled == true){
 		        	if(args.length < 1)
 		        	{
 		        	    //No arguments given!
 		        		sender.sendMessage("Usage /" + cmdname + " [name]");
 		        	}
 		        	else{
 					String playerName = args[0];
 					String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 					File playerFile = new File(pluginFolder + File.separator + "warns" + File.separator + playerName + ".txt");
 					if(!(playerFile.exists()) || playerFile.length() < 1){
 						sender.sendMessage(ChatColor.RED + playerName + " has no warnings!");
 						return true;
 					}
 					ac.warnsplayer = new ListStore(playerFile);
 					ac.warnsplayer.load();
 					ArrayList<String> warnlist =  ac.warnsplayer.getValues();
 					sender.sendMessage(ChatColor.RED +  playerName + "'s warnings:");
 					
 					String listString = "";
 					
 					//String newLine = System.getProperty("line.separator");
 
 					for (String s : warnlist)
 					{
 					    listString += s + " %n";
 					}
 					//sender.sendMessage(playerName + " " + listString);
 					String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 							for(int x=0 ; x<message.length ; x++) {
 							sender.sendMessage(ChatColor.GOLD + message[x]); // Send each argument in the message
 							}
 							
 							ac.warnsplayer.save();
 		        	}
 				
 				}
 		        else {
 		        	
 		        	return true;	
 		        }
 		
 		return true;
 	}
 	else if (cmd.getName().equalsIgnoreCase("warnslog")){ // If the player typed /view-warns then do the following...
 		boolean isenabled = true;
 	       if(isenabled == true){
 	    	   if(args.length < 1)
 	        	{
 	        	    //No arguments given!
 					ArrayList<String> warnlist =  ac.warns.getValues();
 					sender.sendMessage(ChatColor.RED + "Log of warnings:");
 					
 					String listString = "";
 					
 					//String newLine = System.getProperty("line.separator");
 
 					for (String s : warnlist)
 					{
 					    listString += s + " %n";
 					}
 					//sender.sendMessage(playerName + " " + listString);
 					String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 							for(int x=0 ; x<message.length ; x++) {
 							sender.sendMessage(ChatColor.GOLD + message[x]); // Send each argument in the message
 							}
 							
 	        	}
 	        	else{
 	        		String action = args[0];
 	        		if (action.equalsIgnoreCase("clear")){
 	        			String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 						File log = new File(pluginFolder + File.separator + "warns.log");
 						if(log.exists() && log.length() > 1){
 						log.delete();
 						}
 						File newLog = new File(pluginFolder + File.separator + "warns.log");
 						if(!(newLog.exists()) || newLog.length() < 1){
 							try {
 								newLog.createNewFile();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 						ac.warns = new ListStore(newLog);
 						ac.warns.load();
 						sender.sendMessage(ChatColor.RED + "The warning's log has been cleared.");
 						ac.warns.save();
 	        		}
 	        		else {
 	        			sender.sendMessage("Usage /warnslog ([Nothing, clear])");
 	        		}
 	        	}
 	       }
 		        else {
 		        return true;	
 		        }
 		
 		return true;
 	}
 		return false;
 	}
 }
 	
 		 
