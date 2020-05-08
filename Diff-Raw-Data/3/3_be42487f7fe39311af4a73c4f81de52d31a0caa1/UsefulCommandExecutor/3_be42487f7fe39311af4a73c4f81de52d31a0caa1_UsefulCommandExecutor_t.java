 package com.useful.useful;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.conversations.Prompt;
 import org.bukkit.conversations.StringPrompt;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionType;
 import org.bukkit.util.ChatPaginator;
 import org.bukkit.util.ChatPaginator.ChatPage;
 
 import com.useful.useful.utils.Builder;
 import com.useful.useful.utils.ClosestFace;
 import com.useful.useful.utils.ColoredLogger;
 import com.useful.useful.utils.Copier;
 import com.useful.useful.utils.Entities;
 import com.useful.useful.utils.ItemRename;
 import com.useful.useful.utils.JailInfo;
 import com.useful.useful.utils.ListStore;
 import com.useful.useful.utils.Performance;
 import com.useful.useful.utils.Potions;
 import com.useful.useful.utils.TeleportRequest;
 import com.useful.useful.utils.TpaReq;
 import com.useful.useful.utils.UConnectDataRequest;
 import com.useful.useful.utils.UConnectProfile;
 import com.useful.useful.utils.UConnectRank;
 import com.useful.useful.utils.getColor;
 import com.useful.useful.utils.getEnchant;
 
 public class UsefulCommandExecutor implements CommandExecutor {
 	static useful plugin;
 	public static String pluginFolder;
 	public int number;
 	public int numberorig;
 	FileConfiguration config = useful.config;
 	public UsefulCommandExecutor(useful instance){
 		plugin = useful.plugin;
 	}
 	@Override
 	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args){
 		String disabledmessage = useful.config.getString("general.disabledmessage");
 		Player player = null;
 		String cmdname = commandLabel;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if(cmd.getName().equalsIgnoreCase("test")){ // If the player typed /basic then do the following...
 			sender.sendMessage("The useful plugin is working! - coded by storm345");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("worlds")){ // If the player typed /basic then do the following...
 			@SuppressWarnings("rawtypes")
 			List world = plugin.getServer().getWorlds();
 			Object[] worlds = world.toArray();
 			String message = "Worlds on the server:";
 			for (Object s  : worlds)
 			{
 				String wname = ((World) s).getName();
 				message = message + " " + wname + ",";
 			}
 			sender.sendMessage(plugin.colors.getInfo() + message);
 			
 			
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("craft")){
 			if(player == null){
 				sender.sendMessage(plugin.colors.getError() + "This command is for players!");
 				return true;
 			}
 			//TODO drop a craft bench in front of them
 			BlockFace face = ClosestFace.getClosestFace(player.getLocation().getYaw());
 			Block toPlaceAt = player.getLocation().getBlock().getRelative(face);
 			boolean changed = false;
 			Location spawnAt = toPlaceAt.getLocation();
 			Boolean canBuild = Builder.canBuild(player, spawnAt);
 			if(!canBuild){
 				sender.sendMessage(plugin.colors.getError() + "You cannot build here");
 				return true;
 			}
 			if(toPlaceAt.getType() != Material.AIR){
 				sender.sendMessage(plugin.colors.getError() + "Nowhere to place the crafting table!");
 				return true;
 			}
 			Block above = toPlaceAt.getRelative(BlockFace.UP);
 			if(above.getType() != Material.AIR && !changed){
 				spawnAt = toPlaceAt.getLocation();
 				changed = true;
 			}
 			Block above2 = above.getRelative(BlockFace.UP);
 			if(above2.getType() != Material.AIR && !changed){
 				spawnAt = above.getLocation();
 				changed = true;
 			}
 			Block above3 = above2.getRelative(BlockFace.UP);
 			if(above3.getType() != Material.AIR && !changed){
 				spawnAt = above2.getLocation();
 				changed = true;
 			}
 			Block above4 = above3.getRelative(BlockFace.UP);
 			if(above4.getType() != Material.AIR && !changed){
 				spawnAt = above3.getLocation();
 				changed = true;
 			}
 			else{
 				if(!changed){
 				spawnAt = above4.getLocation();
 				}
 			}
 			spawnAt.getWorld().spawnFallingBlock(spawnAt, Material.WORKBENCH, Byte.parseByte("0"));
 			sender.sendMessage(plugin.colors.getSuccess() + "Successfully given you a crafting table.");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("world")){ // If the player typed /basic then do the following...
 			if (sender instanceof Player == false){
 				sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 				return true;
 			}
 			if (args.length < 1){
 				sender.sendMessage("Usage /world ([Player]) [World-Name]");
 				return true;
 			}
 			String wname = "world";
             if (args.length > 1){
             	wname = args[1];
 		    }
             else {
 			wname = args[0];
             }
 		    World world = plugin.getServer().getWorld(wname);
 		    if (world == null){
 		    	sender.sendMessage(plugin.colors.getError() + "World not found! Do /worlds for a list.");
 		    	return true;
 		    }
 		    Location loc = world.getSpawnLocation();
 		    Player target = ((Player) sender);
 		    if (args.length > 1){
 		    	target = plugin.getServer().getPlayer(args[0]);
 		    	target.sendMessage(plugin.colors.getSuccess() + "Teleporting you to world " + world.getName() + " courtesy of " + sender.getName());
 		    }
 		    else {
 		    	sender.sendMessage(plugin.colors.getSuccess() + "Teleporting you to world " + world.getName());
 		    }
 		    try{
 		    	target.teleport(loc);
 		    }
 		    catch(Exception e){
 		    	sender.sendMessage(plugin.colors.getError() + "World not found! Do /worlds for a list.");
 		    }
              
 			
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("firework")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "This command is for players");
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
 				sender.sendMessage(plugin.colors.getError() + "The amount specified is not an integer.");
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
 				sender.sendMessage(plugin.colors.getError() + "The firework height must be either 1, 2 or 3");
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
 				sender.sendMessage(plugin.colors.getError() + "Invalid type - Valid types are: small, big, burst, creeper, epic_creeper, star");
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
 				sender.sendMessage(plugin.colors.getError() + "Error: invalid first color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 				return true;
 			}
 			Color color2 = getColor.getColorFromString(col2);
 			if(color2 == null){
 				sender.sendMessage(plugin.colors.getError() + "Error: invalid second color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 				return true;
 			}
 			Color fader = getColor.getColorFromString(fade);
 			if(fader == null){
 				sender.sendMessage(plugin.colors.getError() + "Error: invalid first fade color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 				return true;
 			}
 			Color fader2 = getColor.getColorFromString(fade2);
 			if(fader2 == null){
 				sender.sendMessage(plugin.colors.getError() + "Error: invalid second fade color." + ChatColor.RESET + " Valid colors are: aqua, black, blue, fuchsia, grey, green, lime, maroon, navy, olive, orange, pink, purple, red, silver, teal, white and yellow");
 				return true;
 			}
 			effect.withFade(fader, fader2);
 			effect.withColor(color1, color2);
 			FireworkEffect teffect = effect.build();
 			fmeta.addEffect(teffect);
 			item.setItemMeta(fmeta);
 			item.setAmount(amount);
 			sender.sendMessage(plugin.colors.getSuccess() + "Created firework");
 			player.getInventory().addItem(item);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("uconnect")){
 			//TODO the uconnect inta-server system!
 			String[] usage = {ChatColor.GREEN + "" + ChatColor.BOLD + "UConnect help:",ChatColor.DARK_AQUA + "Sections:", ChatColor.DARK_RED + "/"+cmdname+ChatColor.YELLOW+" msg", ChatColor.DARK_RED + "/"+cmdname+ChatColor.YELLOW+" profile", ChatColor.DARK_RED+"/"+cmdname+ChatColor.YELLOW+" setprofile", ChatColor.DARK_RED+"/"+cmdname+ChatColor.YELLOW+" news", ChatColor.DARK_RED+"/"+cmdname+ChatColor.YELLOW+" createnews", ChatColor.DARK_RED+"/"+cmdname+ChatColor.YELLOW+" deletenews", ChatColor.DARK_RED+"/"+cmdname+ChatColor.YELLOW+" servers"};
 			if(args.length <1){
 				for(String line:usage){
 					sender.sendMessage(line);
 				}
 				return true;
 			}
 			String program = args[0];
 			if(program.equalsIgnoreCase("message") || program.equalsIgnoreCase("msg")){
 				String[] msgUsage = {ChatColor.GREEN + "" + ChatColor.BOLD + "UConnect help:",ChatColor.DARK_AQUA + "Sections:", ChatColor.DARK_RED + "/"+cmdname+" msg "+ChatColor.YELLOW+"send <Player> <Msg>", ChatColor.DARK_RED + "/"+cmdname+" msg "+ChatColor.YELLOW+"clear", ChatColor.DARK_RED + "/"+cmdname+" msg "+ChatColor.YELLOW+"read (Page)", ChatColor.DARK_RED + "/"+cmdname+" msg "+ChatColor.YELLOW+"block <Player>", ChatColor.DARK_RED + "/"+cmdname+" msg "+ChatColor.YELLOW+"unblock <Player>", ChatColor.DARK_RED + "/"+cmdname+" msg "+ChatColor.YELLOW+"blocked (Page)"};
 				if(player == null){
 					sender.sendMessage(plugin.colors.getError() + "Only players can use the messaging part of uconnect.");
 					return true;
 				}
 				if(args.length <2){
 					for(String line:msgUsage){
 						sender.sendMessage(line);
 					}
 					return true;
 				}
 				String action = args[1];
 				if(action.equalsIgnoreCase("read")){
 					UConnectProfile profile = new UConnectProfile(player.getName());
 					String page = "1";
 					if(args.length > 2){
 						page = args[2];
 					}
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					plugin.uconnect.getMessages(profile,page,sender);
 					return true;
 				}
 				else if(action.equalsIgnoreCase("clear")){
 					UConnectProfile profile = new UConnectProfile(player.getName());
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					plugin.uconnect.clearMessages(profile, sender);
 					return true;
 				}
 				else if(action.equalsIgnoreCase("send")){
 					if(args.length < 4){
 						sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" msg"+ChatColor.YELLOW + " send <Player> <Msg>");
 						return true;
 					}
 					String playerName = args[2];
 					sender.sendMessage(plugin.colors.getError() + "WARNING: Player names are CaSe SenSitIvE");
 					String message = args[3];
 				    for(int i = 4; i<args.length;i++){
 						message = message + " " + args[i];
 					}
 				    sender.sendMessage(ChatColor.GRAY + "Loading...");
 				    plugin.uconnect.message(new UConnectProfile(playerName), new UConnectProfile(player.getName()), message, sender);
 				    return true;
 				}
 				else if(action.equalsIgnoreCase("block")){
 					if(args.length < 3){
 						sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" msg"+ChatColor.YELLOW + " block <Player>");
 						return true;
 					}
 					String name = args[2];
 					sender.sendMessage(plugin.colors.getError() + "WARNING: Player names are CaSe SenSitIvE");
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(name);
 					UConnectDataRequest request = new UConnectDataRequest("block", uargs.toArray(), player);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					plugin.uconnect.load(request);
 					return true;
 				}
 				else if(action.equalsIgnoreCase("unblock")){
 					if(args.length < 3){
 						sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" msg"+ChatColor.YELLOW + " unblock <Player>");
 						return true;
 					}
 					String name = args[2];
 					sender.sendMessage(plugin.colors.getError() + "WARNING: Player names are CaSe SenSitIvE");
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(name);
 					UConnectDataRequest request = new UConnectDataRequest("unblock", uargs.toArray(), player);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					plugin.uconnect.load(request);
 					return true;
 				}
 				else if(action.equalsIgnoreCase("blocked")){
 					//TODO
 					String page = "1";
 					if(args.length > 2){
 						page = args[2];
 					}
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(page);
 					UConnectDataRequest request = new UConnectDataRequest("blocked", uargs.toArray(), player);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					plugin.uconnect.load(request);
 					return true;
 				}
 				else{
 					for(String line:msgUsage){
 						sender.sendMessage(line);
 					}
 					return true;
 				}
 			}
 			else if(program.equalsIgnoreCase("profile")){
 				if(args.length < 2){
 					sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" profile"+ChatColor.YELLOW + " <Player>");
 					return true;
 				}
 				String pName = args[1];
 				List<Object> uargs = new ArrayList<Object>();
 				uargs.add(pName);
 				UConnectDataRequest request = new UConnectDataRequest("loadProfile", uargs.toArray(), sender);
 				sender.sendMessage(ChatColor.GRAY + "Loading...");
 				plugin.uconnect.loadProfile(pName, request, sender);
 				return true;
 			}
 			else if(program.equalsIgnoreCase("setprofile")){
 				String[] msgUsage = {ChatColor.GREEN + "" + ChatColor.BOLD + "UConnect help:",ChatColor.DARK_AQUA + "Sections:", ChatColor.DARK_RED + "/"+cmdname+" setprofile "+ChatColor.YELLOW+"About <Value>", ChatColor.DARK_RED + "/"+cmdname+" setprofile "+ChatColor.YELLOW+"Contact <Value>", ChatColor.DARK_RED + "/"+cmdname+" setprofile "+ChatColor.YELLOW+"Favserer <Value>"};
 				if(player == null){
 					sender.sendMessage(plugin.colors.getError() + "This part of uConnect is for players");
 					return true;
 				}
 				if(args.length < 3){
 					for(String line:msgUsage){
 						sender.sendMessage(line);
 					}
 					return true;
 				}
 				String action = args[1];
 				if(action.equalsIgnoreCase("contact")){
 					String contactInfo = args[2];
 					for(int i=3;i<args.length;i++){
 						contactInfo = contactInfo + " " + args[i];
 					}
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(player.getName());
 					uargs.add(contactInfo);
 					UConnectDataRequest request = new UConnectDataRequest("setProfileContact", uargs.toArray(), sender);
 					plugin.uconnect.loadProfile(player.getName(), request, sender);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					return true;
 				}
 				else if(action.equalsIgnoreCase("about")){
 					String contactInfo = args[2];
 					for(int i=3;i<args.length;i++){
 						contactInfo = contactInfo + " " + args[i];
 					}
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(player.getName());
 					uargs.add(contactInfo);
 					UConnectDataRequest request = new UConnectDataRequest("setProfileAbout", uargs.toArray(), sender);
 					plugin.uconnect.loadProfile(player.getName(), request, sender);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					return true;
 				}
 				else if(action.equalsIgnoreCase("favserver")){
 					String contactInfo = args[2];
 					for(int i=3;i<args.length;i++){
 						contactInfo = contactInfo + " " + args[i];
 					}
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(player.getName());
 					uargs.add(contactInfo);
 					UConnectDataRequest request = new UConnectDataRequest("setProfileFavServer", uargs.toArray(), sender);
 					plugin.uconnect.loadProfile(player.getName(), request, sender);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					return true;
 				}
 				else{
 					for(String line:msgUsage){
 						sender.sendMessage(line);
 					}
 					return true;
 				}
 			}
 			
 				else if(program.equalsIgnoreCase("news")){
 					UConnectDataRequest request = new UConnectDataRequest("showNews", null, sender);
 					plugin.uconnect.load(request);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					return true;
 				}
 				else if(program.equalsIgnoreCase("createnews")){
 					UConnectProfile profile = new UConnectProfile(sender.getName());
 					if(profile.getRank() != UConnectRank.CREATOR && profile.getRank() != UConnectRank.DEVELOPER){
 						sender.sendMessage(plugin.colors.getError() + "You don't have permission to do this");
 						return true;
 					}
 					if(args.length < 3){
 						sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" createnews"+ChatColor.YELLOW + " <Article> <Story>");
 						return true;
 					}
 					String article = args[1];
 					String story = args[2];
 					for(int i=4;i<args.length;i++){
 						story = story + " " + args[i];
 					}
 					String toPut = "&2&l"+article+"abcd&r&e"+story;
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(toPut);
 					UConnectDataRequest request = new UConnectDataRequest("createNews", uargs.toArray(), sender);
 					plugin.uconnect.load(request);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					return true;
 				}
 				else if(program.equalsIgnoreCase("deleteNews")){
 					UConnectProfile profile = new UConnectProfile(sender.getName());
 					if(profile.getRank() != UConnectRank.CREATOR && profile.getRank() != UConnectRank.DEVELOPER){
 						sender.sendMessage(plugin.colors.getError() + "You don't have permission to do this");
 						return true;
 					}
 					if(args.length < 2){
 						sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" deleteNews"+ChatColor.YELLOW + " <Article>");
 						return true;
 					}
 					String article = args[1];
 					List<Object> uargs = new ArrayList<Object>();
 					uargs.add(article);
 					UConnectDataRequest request = new UConnectDataRequest("deleteNews", uargs.toArray(), sender);
 					plugin.uconnect.load(request);
 					sender.sendMessage(ChatColor.GRAY + "Loading...");
 					return true;
 				
 			}
 				else if(program.equalsIgnoreCase("servers")){
 					String[] msgUsage = {ChatColor.GREEN + "" + ChatColor.BOLD + "UConnect help:",ChatColor.DARK_AQUA + "Sections:", ChatColor.DARK_RED + "/"+cmdname+" servers "+ChatColor.YELLOW+"list (Page)", ChatColor.DARK_RED + "/"+cmdname+" servers "+ChatColor.YELLOW+"add <Ip> <About>", ChatColor.DARK_RED + "/"+cmdname+" servers "+ChatColor.YELLOW+"delete <Ip>"};
 				   if(args.length < 2){
 					   for(String line:msgUsage){
 						   sender.sendMessage(line);
 					   }
 					   return true;
 				   }
 				   String action = args[1];
 				   if(action.equalsIgnoreCase("add")){
 					   if(args.length < 4){
 						   sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" servers"+ChatColor.YELLOW + " add <Ip> <About>");
 							return true;
 					   }
 					   String ip = args[2];
 					   String about = args[3];
 					   for(int i=4;i<args.length;i++){
 						   about = about + " " + args[i];
 					   }
 					   List<Object> uargs = new ArrayList<Object>();
 					   uargs.add(ip);
 					   uargs.add(about);
 					   UConnectDataRequest request = new UConnectDataRequest("addServer", uargs.toArray(), sender);
 					   plugin.uconnect.load(request);
 						sender.sendMessage(ChatColor.GRAY + "Loading...");
 						return true;
 				   }
 				   else if(action.equalsIgnoreCase("delete")){
 					   if(args.length < 3){
 						   sender.sendMessage(ChatColor.GREEN+"Usage: "+ChatColor.DARK_RED+"/"+cmdname+" servers"+ChatColor.YELLOW + " delete <Ip>");
 							return true;
 					   }
 					   String ip = args[2];
 					   List<Object> uargs = new ArrayList<Object>();
 					   uargs.add(ip);
 					   UConnectDataRequest request = new UConnectDataRequest("deleteServer", uargs.toArray(), sender);
 					   plugin.uconnect.load(request);
 						sender.sendMessage(ChatColor.GRAY + "Loading...");
 						return true;
 				   }
 				   else if(action.equalsIgnoreCase("list")){
 					   int page = 1;
 					   if(args.length > 2){
 						   String pag = args[2];
 						   try {
 							page = Integer.parseInt(pag);
 						} catch (NumberFormatException e) {
 							sender.sendMessage(plugin.colors.getError()+"Invalid page number");
 							return true;
 						}
 					   }
 					   if(page < 1){
 						   page = 1;
 					   }
 					   List<Object> uargs = new ArrayList<Object>();
 					   uargs.add(page);
 					   UConnectDataRequest request = new UConnectDataRequest("listServer", uargs.toArray(), sender);
 					   plugin.uconnect.load(request);
 						sender.sendMessage(ChatColor.GRAY + "Loading...");
 						return true;
 				   }
 				   else{
 					   if(args.length <1){
 							for(String line:usage){
 								sender.sendMessage(line);
 							}
 							return true;
 						}
 				   }
 				}
 				else if(program.equalsIgnoreCase("friends")){
 					   //TODO A FRIENDLY THINGY!!
 					   return true;
 				   }
 				for(String line:usage){
 					sender.sendMessage(line);
 				}
 				return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("needauth")){
 			//TODO
 			if(args.length < 2){
 				return false;
 			}
 			useful.authed.put(args[0], false);
 			if(plugin.auths.contains(args[0] + " " + args[1].toLowerCase())){
 				sender.sendMessage(plugin.colors.getError() + args[0] + " was already on the authentication list!");
 				return true;
 			}
 			plugin.auths.add(args[0] + " " + args[1].toLowerCase());
 			plugin.auths.save();
 			sender.sendMessage(plugin.colors.getSuccess() + args[0] + " is now on the authentication list with the password: " + args[1].toLowerCase());
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("notneedauth")){
 			//TODO
 			if(args.length < 1){
 				return false;
 			}
 			plugin.auths.load();
 			Object[] authVal = plugin.auths.values.toArray();
 			String tval = "&&unknown**name&&";
      	   for(int i=0;i<authVal.length;i++){
      		   String val = (String) authVal[i];
      		   if(val.startsWith(args[0])){
      			   tval = val;
      			   plugin.auths.remove(val);
      			   plugin.auths.remove((String)authVal[i]);
      		   }
      	   }
      	   if(tval == "&&unknown**name&&"){
      		   sender.sendMessage(plugin.colors.getError() + "Player is not on the authentication list (Player names are case sensitive)!)");
      		   return true;
      	   }
      	   plugin.auths.remove(tval);
      	   plugin.auths.save();
      	   plugin.auths.load();
      	   useful.authed.remove(args[0]);
      	  sender.sendMessage(plugin.colors.getSuccess() + args[0] + " is now not on the authentication list.");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("login")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "Non players don't need authentication!");
 				return true;
 			}
 			if(args.length < 1){
 				return false;
 			}
 			if(!useful.authed.containsKey(sender.getName())){
 				sender.sendMessage(plugin.colors.getError() + "You do not need to be logged in!");
 				return true;
 			}
 			String tval = player.getName() + " " + args[0].toLowerCase();
 			if(plugin.auths.contains(tval)){
 				//They are logged in!
 				if(useful.authed.get(player.getName())){
 					sender.sendMessage(plugin.colors.getError() + "Already logged in!!");
 					return true;
 				}
 				useful.authed.put(player.getName(), true);
 				sender.sendMessage(plugin.colors.getSuccess() + "Password accepted!");
 				return true;
 			}
 			else{
 				sender.sendMessage(plugin.colors.getError() + "Password incorrect!");
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("shelter")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "You need to be a player to use this command.");
 				return true;
 			}
 			if(args.length < 1){
 				return false;
 			}
 			String rank = "";
 			if(args[0].equalsIgnoreCase("1")){
 				rank = "1";
 			}
 			Block start = player.getLocation().add(0, -1, 0).getBlock();
 			Block floorMiddle = player.getLocation().add(0, -1, 0).getBlock();
 			//BEGIN FLOOR
 			Builder.build(player, floorMiddle, Material.WOOD); // middle of floor
 			Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH), Material.WOOD); //north floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH), Material.WOOD); //south floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.EAST), Material.WOOD); //east floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.WEST), Material.WOOD); //west floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_EAST), Material.WOOD); //north east floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_EAST), Material.WOOD); //south east floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_WEST), Material.WOOD); //south west floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_WEST), Material.WOOD); //north west floor block
 			//END FLOOR
 			//BEGIN NORTH WALL
 			Block wallCenter = start.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD); //wall block under window
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.GLASS);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.STEP);
 			//ENG NORTH WALL
 			//BEGIN EAST WALL
 			wallCenter = start.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.GLASS);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.STEP);
 			//ENG EAST WALL
 			//BEGIN WEST WALL
 			wallCenter = start.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.GLASS);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.STEP);
 			//ENG WEST WALL
 			//BEGIN SOUTH WALL
 			wallCenter = start.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.UP), Material.AIR);
 			Builder.build(player, wallCenter.getRelative(BlockFace.UP, 2), Material.AIR);
 			Builder.build(player, wallCenter.getRelative(BlockFace.DOWN), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH), Material.WOOD);
 			Builder.buildById(player, wallCenter.getRelative(BlockFace.UP), "64:8");
 			Builder.buildById(player, wallCenter, "64:3");
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getRelative(BlockFace.EAST), Material.TORCH);
 			Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getRelative(BlockFace.WEST), Material.TORCH);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 			wallCenter = wallCenter.getRelative(BlockFace.UP);
 			Builder.build(player, wallCenter, Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.STEP);
 			Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.STEP);
 			//ENG SOUTH WALL
 			//BEGIN INTERIOR AIR & torches
 			Block levelMid = floorMiddle.getRelative(BlockFace.UP);
 			Builder.build(player, levelMid, Material.AIR);
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH), Material.AIR); //north block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH), Material.AIR); //south block
 			Builder.build(player, levelMid.getRelative(BlockFace.EAST), Material.AIR); //east block
 			Builder.build(player, levelMid.getRelative(BlockFace.WEST), Material.AIR); //west block
 			Builder.buildById(player, levelMid.getRelative(BlockFace.WEST), "26:2");
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_EAST), Material.AIR); //south east block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_WEST), Material.AIR); //south west block
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH_WEST), Material.AIR); //north west block
 			Builder.buildById(player, levelMid.getRelative(BlockFace.NORTH_WEST), "26:10");
 			levelMid = levelMid.getRelative(BlockFace.UP);
 			Builder.build(player, levelMid, Material.AIR);
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH), Material.AIR); //north block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH), Material.AIR); //south block
 			Builder.build(player, levelMid.getRelative(BlockFace.EAST), Material.AIR); //east block
 			Builder.build(player, levelMid.getRelative(BlockFace.WEST), Material.AIR); //west block
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_EAST), Material.AIR); //south east block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_WEST), Material.AIR); //south west block
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH_WEST), Material.AIR); //north west block
 			levelMid = levelMid.getRelative(BlockFace.UP);
 			Builder.build(player, levelMid, Material.AIR);
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH), Material.TORCH); //north block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH), Material.TORCH); //south block
 			Builder.build(player, levelMid.getRelative(BlockFace.EAST), Material.TORCH); //east block
 			Builder.build(player, levelMid.getRelative(BlockFace.WEST), Material.TORCH); //west block
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_EAST), Material.AIR); //south east block
 			Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_WEST), Material.AIR); //south west block
 			Builder.build(player, levelMid.getRelative(BlockFace.NORTH_WEST), Material.AIR); //north west block
 			//END INTERIOR AIR
 			//BEGIN ROOF
 			floorMiddle = levelMid.getRelative(BlockFace.UP);
 			//BEGIN FLOOR
 			Builder.build(player, floorMiddle, Material.WOOD); // middle of floor
 			Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH), Material.WOOD); //north floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH), Material.WOOD); //south floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.EAST), Material.WOOD); //east floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.WEST), Material.WOOD); //west floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_EAST), Material.WOOD); //north east floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_EAST), Material.WOOD); //south east floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_WEST), Material.WOOD); //south west floor block
 			Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_WEST), Material.WOOD); //north west floor block
 			//END ROOF
 			if(args[0].equalsIgnoreCase("2")){
 				rank = "2";
 				start = player.getLocation().add(0, 3, 0).getBlock();
 				floorMiddle = player.getLocation().add(0, 3, 0).getBlock();
 				//BEGIN FLOOR
 				Builder.build(player, floorMiddle, Material.WOOD); // middle of floor
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH), Material.AIR); //north floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH), Material.WOOD); //south floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.EAST), Material.AIR); //east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.WEST), Material.WOOD); //west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_EAST), Material.WOOD); //south east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_WEST), Material.WOOD); //south west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_WEST), Material.WOOD); //north west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH, 2), Material.DOUBLE_STEP); //north floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH,2 ), Material.DOUBLE_STEP); //south floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.EAST, 2), Material.DOUBLE_STEP); //east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.WEST, 2), Material.DOUBLE_STEP); //west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_EAST, 2), Material.DOUBLE_STEP); //north east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_EAST, 2), Material.DOUBLE_STEP); //south east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_WEST, 2), Material.DOUBLE_STEP); //south west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_WEST, 2), Material.DOUBLE_STEP); //north west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_NORTH_EAST), Material.DOUBLE_STEP); //north east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_SOUTH_EAST), Material.DOUBLE_STEP); //south east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_SOUTH_WEST), Material.DOUBLE_STEP); //south west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_NORTH_WEST), Material.DOUBLE_STEP); //north west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.EAST_NORTH_EAST), Material.DOUBLE_STEP); //north east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.EAST_SOUTH_EAST), Material.DOUBLE_STEP); //south east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.WEST_SOUTH_WEST), Material.DOUBLE_STEP); //south west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.WEST_NORTH_WEST), Material.DOUBLE_STEP); //north west floor block
 				//END FLOOR
 				//BEGIN NORTH WALL
 				wallCenter = start.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD); //wall block under window
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.DOWN, 4), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.STEP);
 				//ENG NORTH WALL
 				//BEGIN EAST WALL
 				wallCenter = start.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.GLASS);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.STEP);
 				//ENG EAST WALL
 				//BEGIN WEST WALL
 				wallCenter = start.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.GLASS);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.SOUTH, 2), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.NORTH, 2), Material.STEP);
 				//ENG WEST WALL
 				//BEGIN SOUTH WALL
 				wallCenter = start.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD); //wall block under window
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.GLASS);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.WOOD);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.LOG);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.LOG);
 				wallCenter = wallCenter.getRelative(BlockFace.UP);
 				Builder.build(player, wallCenter, Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.EAST, 2), Material.STEP);
 				Builder.build(player, wallCenter.getRelative(BlockFace.WEST, 2), Material.STEP);
 				//ENG SOUTH WALL
 				//BEGIN INTERIOR AIR & torches
 				levelMid = floorMiddle.getRelative(BlockFace.UP);
 				Builder.build(player, levelMid, Material.AIR);
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH), Material.AIR); //north block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH), Material.AIR); //south block
 				Builder.build(player, levelMid.getRelative(BlockFace.EAST), Material.AIR); //east block
 				Builder.build(player, levelMid.getRelative(BlockFace.WEST), Material.AIR); //west block
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_EAST), Material.AIR); //south east block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_WEST), Material.AIR); //south west block
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH_WEST), Material.AIR); //north west block
 				levelMid = levelMid.getRelative(BlockFace.UP);
 				Builder.build(player, levelMid, Material.AIR);
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH), Material.AIR); //north block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH), Material.AIR); //south block
 				Builder.build(player, levelMid.getRelative(BlockFace.EAST), Material.AIR); //east block
 				Builder.build(player, levelMid.getRelative(BlockFace.WEST), Material.AIR); //west block
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_EAST), Material.AIR); //south east block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_WEST), Material.AIR); //south west block
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH_WEST), Material.AIR); //north west block
 				levelMid = levelMid.getRelative(BlockFace.UP);
 				Builder.build(player, levelMid, Material.AIR);
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH), Material.AIR); //north block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH), Material.TORCH); //south block
 				Builder.build(player, levelMid.getRelative(BlockFace.EAST), Material.TORCH); //east block
 				Builder.build(player, levelMid.getRelative(BlockFace.WEST), Material.TORCH); //west block
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH_EAST), Material.AIR); //north east block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_EAST), Material.AIR); //south east block
 				Builder.build(player, levelMid.getRelative(BlockFace.SOUTH_WEST), Material.AIR); //south west block
 				Builder.build(player, levelMid.getRelative(BlockFace.NORTH_WEST), Material.AIR); //north west block
 				//END INTERIOR AIR
 				//START STAIRS
 				levelMid = floorMiddle.getRelative(BlockFace.DOWN, 3);
 				Builder.buildById(player, levelMid.getRelative(BlockFace.EAST), "53:3"); //west block
 				levelMid = levelMid.getRelative(BlockFace.UP);
 				Builder.buildById(player, levelMid.getRelative(BlockFace.NORTH_EAST), "53:3"); //north west block
 				levelMid = levelMid.getRelative(BlockFace.UP);
 				Builder.buildById(player, levelMid.getRelative(BlockFace.NORTH), "53:1"); //north block
 				Builder.buildById(player, levelMid.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST), "53:1"); //north east block
 				
 				//END STAIRS
 				//BEGIN ROOF
 				floorMiddle = levelMid.getRelative(BlockFace.UP, 5);
 				//BEGIN FLOOR
 				Builder.build(player, floorMiddle, Material.WOOD); // middle of floor
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH), Material.WOOD); //north floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH), Material.WOOD); //south floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.EAST), Material.WOOD); //east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.WEST), Material.WOOD); //west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_EAST), Material.WOOD); //north east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_EAST), Material.WOOD); //south east floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.SOUTH_WEST), Material.WOOD); //south west floor block
 				Builder.build(player, floorMiddle.getRelative(BlockFace.NORTH_WEST), Material.WOOD); //north west floor block
 				//END ROOF
 			}
 			player.sendMessage(plugin.colors.getSuccess() + "Built an instant shelter size " + rank + "!");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("mobtypes")){ // If the player typed /basic then do the following...
 			sender.sendMessage(plugin.colors.getInfo() + "Available mob types: cow, chicken, blaze, cave-spider, creeper, enderdragon, " +
 					"enderman, xp, ghast, giant, irongolem, magmacube, mushroomcow, ocelot, pig, pigzombie, silverfish, " +
 					"sheep, skeleton, slime, snowman, spider, squid, villager, wolf, zombie, bat, witch, wither, wither_skull, firework");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("mobset")){ // If the player typed /basic then do the following...
 			if (sender instanceof Player == false){
 				sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 				return true;
 			}
 			boolean isenabled = useful.config.getBoolean("general.mobset.enable");
 			if (isenabled == false){
 				sender.sendMessage(disabledmessage);
 				return true;
 			}
 			if (args.length < 1){
 				sender.sendMessage("Usage /mobset [Type] - do /mobtypes for a list");
 				return true;
 			}
 			Block block = plugin.getServer().getPlayer(sender.getName()).getTargetBlock(null, 10);
 			//Block block = loc.getBlock();
 			if (block.getTypeId() == 52){
 				//change type of spawner
 				BlockState isSpawner = block.getState();
 				CreatureSpawner spawner = (CreatureSpawner) isSpawner;
 				EntityType type = Entities.getEntityTypeByName(args[0], sender);
 				if (type == null){
 					return true;
 				}
 				spawner.setSpawnedType(type);
 				sender.sendMessage(plugin.colors.getSuccess() + "Spawner type set!");
 			}
 			else {
 				sender.sendMessage(plugin.colors.getError() + "You must be looking at a mob spawner to use this command.");
 				return true;
 			}
 			
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("spawnmob")){ // If the player typed /basic then do the following...
 			if (sender instanceof Player == false){
 				sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 				return true;
 			}
 			boolean isenabled = useful.config.getBoolean("general.spawnmob.enable");
 			if (isenabled == false){
 				sender.sendMessage(disabledmessage);
 				return true;
 			}
 			if (args.length < 2){
 				sender.sendMessage("Usage /" + cmdname + " [Type] [Amount] - do /mobtypes for a list");
 				return true;
 			}
 			//Block block = plugin.getServer().getPlayer(sender.getName()).getTargetBlock(null, 10);
 			//Block block = loc.getBlock();
 			
 				EntityType type = Entities.getEntityTypeByName(args[0], sender);
 				if (type == null){
 					return true;
 				}
 				Location loc = ((Player )sender).getLocation();
 				int number = 0;
 				try {
 					number = Integer.parseInt(args[1]);
 				} catch (Exception e) {
 					sender.sendMessage("Usage /" + cmdname + " [Type] [Amount] - do /mobtypes for a list");
 					return true;
 				}
 				while (number > 0){
 					((Player )sender).getWorld().spawnEntity(loc, type);
 					number--;
 				}
 				if (number <= 0){
 					sender.sendMessage(plugin.colors.getSuccess() + "Successfully spawned " + args[1] + " " + args[0] + "'s!");
 					return true;
 				}
 				//((Player )sender).getWorld().spawnEntity(loc, type);
 				
 			
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("hat")){ // If the player typed /basic then do the following...
 			
 			boolean isenabled = useful.config.getBoolean("general.hat.enable");
 		       if(isenabled == true){
 		    	   
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 		    	   if (args.length < 1){
 		    		   //no arguments given
 		    		   Player target = (Player) sender;
 		    		   PlayerInventory inv = target.getInventory();
 		    		   
 		    		   if (inv.getHelmet() != null && inv.getHelmet() != new ItemStack(0)){
 		    		   ItemStack helmet = inv.getHelmet();
 		    		   inv.setHelmet(new ItemStack(0));
 		    		   if (helmet.getTypeId() != 0){
 		    		   inv.addItem(helmet);
 		    		   }
 		    		   }
 		    		   int item = target.getItemInHand().getTypeId();
 		    		   inv.setHelmet(new ItemStack(item));
 		    		   target.setItemInHand(new ItemStack(0));
 			sender.sendMessage(plugin.colors.getSuccess() + "Hat set!");
 						} 
 		    	   else {
 		    		   Player target = (Player) sender;
 		    		   PlayerInventory inv = target.getInventory();
 		    		   int item = 0;
 		    		   if (inv.getHelmet() != null && inv.getHelmet() != new ItemStack(0)){
 		    		   ItemStack helmet = inv.getHelmet();
 		    		   inv.setHelmet(new ItemStack(item));
 		    		   if (helmet.getTypeId() != 0){
 		    		   inv.addItem(helmet);
 		    		   }
 		    		   }
 		    		   sender.sendMessage(plugin.colors.getError() + "Hat taken off!");
 		    	   }
 		    	   }
 		       }
 		       else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("kick")){ // If the player typed /basic then do the following...
 			if (args.length < 1){
 				sender.sendMessage("Usage /kick [Name] [Reason]");
 			}
 			else if (args.length == 1){
 				// kick [name]
 				Player bad = plugin.getServer().getPlayer(args[0]);
 				if (bad == null){
 					sender.sendMessage(plugin.colors.getError() + "Player not found!");
 					return true;
 				}
 				bad.kickPlayer("You were kicked from the server :(");
 				Bukkit.broadcastMessage(plugin.colors.getInfo() + bad.getName() + " was kicked from the server by " + sender.getName());
 			}
 			else {
 				//kick [name] [reason]
 				Player bad = plugin.getServer().getPlayer(args[0]);
 				if (bad == null){
 					sender.sendMessage(plugin.colors.getError() + "Player not found!");
 					return true;
 				}
 				StringBuilder msg = new StringBuilder();
 				for (int i = 1; i < args.length; i++) {
 				    if (i != 0)
 				         msg.append(" ");
 				    msg.append(args[i]);
 				}
 				bad.kickPlayer("Kicked: " + msg);
 				Bukkit.broadcastMessage(plugin.colors.getInfo() + bad.getName() + " was kicked from the server by " + sender.getName() + " for " + msg);
 			}
 			
 	
 			return true;
 		}
 else if(cmd.getName().equalsIgnoreCase("ban")){ // If the player typed /basic then do the following...
 	if (args.length < 1){
 		sender.sendMessage("Usage /ban [Name] [Reason]");
 	}
 	else if (args.length == 1){
 		// kick [name]
 		OfflinePlayer bad = getServer().getOfflinePlayer(args[0]);
 		Player online = getServer().getPlayer(args[0]);
 		if (bad == null){
 			sender.sendMessage(plugin.colors.getError() + "Player not found!");
 			return true;
 		}
 		if (online != null){
 			online.kickPlayer("You were banned from the server :(");
 		}
 		bad.setBanned(true);
 		
 		Bukkit.broadcastMessage(plugin.colors.getInfo() + bad.getName() + " was banned from the server by " + sender.getName());
 	}
 	else {
 		//kick [name] [reason]
 		OfflinePlayer bad = getServer().getOfflinePlayer(args[0]);
 		Player online = getServer().getPlayer(args[0]);
 		if (bad == null){
 			sender.sendMessage(plugin.colors.getError() + "Player not found!");
 			return true;
 		}
 		StringBuilder msg = new StringBuilder();
 		for (int i = 1; i < args.length; i++) {
 		    if (i != 0)
 		         msg.append(" ");
 		    msg.append(args[i]);
 		}
         if (online != null){
         	online.kickPlayer("Banned: " + msg);
 		}
 		bad.setBanned(true);
 		Bukkit.broadcastMessage(plugin.colors.getInfo() + bad.getName() + " was banned from the server by " + sender.getName() + " for " + msg);
 	}
 	
 
 	return true;
 }
 else if(cmd.getName().equalsIgnoreCase("unban")){ // If the player typed /basic then do the following...
 	if (args.length < 1){
 		sender.sendMessage("Usage /" + cmdname + " [Name]");
 	}
 	else {
 		
 			//kick [name] [reason]
 			Set<OfflinePlayer> banned = getServer().getBannedPlayers();
 			Object[] ban = banned.toArray();
 			String name = "Unknown";
 			OfflinePlayer theplayer = null;
 			boolean found = false;
 			for (int i = 0; i < ban.length; i++) {
 			         if(((OfflinePlayer) ban[i]).getName().equalsIgnoreCase(args[0])){
 			        	 theplayer = (OfflinePlayer) ban[i];
 			        	 found = true;
 			        	 name = ((OfflinePlayer) ban[i]).getName();
 			         }
 			}
 		if(found == false){
 			sender.sendMessage(plugin.colors.getError() + "Player not found on ban list!");
 		return true;
 		}
 		theplayer.setBanned(false);
 		Bukkit.broadcastMessage(plugin.colors.getInfo() + name + " was unbanned from the server by " + sender.getName());
 	}
 	
 
 	return true;
 }
 else if(cmd.getName().equalsIgnoreCase("backup")){
 	plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
 		public void run(){
 			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 			   //get current date time with Date()
 			java.util.Date dateTime = new java.util.Date();
 			String date = dateFormat.format(dateTime);
 			sender.sendMessage(plugin.colors.getSuccess() + "Starting backup procedure...");
 			getLogger().info(sender.getName() + " Ran a backup");
 			List<World> worlds = plugin.getServer().getWorlds();
 			Object[] theWorlds = worlds.toArray();
 			String path = new File(".").getAbsolutePath();
 			for(int i=0;i<theWorlds.length; i++){
 				World w = (World) theWorlds[i];
 				try {
 					w.save();
 				} catch (Exception e1) {
 					getLogger().info("Failed to save world. Proceeding on assumption it is already done automatically...");
 				}
 				String wNam = w.getName();
 			File srcFolder = new File(path + File.separator + wNam);
 			File destFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "World Backups" + File.separator + date + File.separator + wNam);
 		    destFolder.mkdirs();
 			//make sure source exists
 			if(!srcFolder.exists()){
 		       sender.sendMessage(plugin.colors.getError() + "Failed to find world " + wNam);
 
 		    }else{
 
 		       try{
 		    	Copier.copyFolder(srcFolder,destFolder);
 		       }catch(IOException e){
 		    	sender.sendMessage(plugin.colors.getError() + "Error copying world " + wNam);
 		       }
 		    }
 
 			}
 			sender.sendMessage(plugin.colors.getSuccess() + "Backup procedure complete!");
 		}
 	});
 	
 	return true;
 }
 else if(cmd.getName().equalsIgnoreCase("canfly")){
 	if(!(sender instanceof Player)){
 		sender.sendMessage(plugin.colors.getError() + "This command is for players!");
 		return true;
 	}
 	if(player.getAllowFlight()){
 		player.setAllowFlight(false);
 		player.sendMessage(plugin.colors.getSuccess() + "Fly mode disabled!");
 	}
 	else if(!player.getAllowFlight()){
 		player.setAllowFlight(true);
 		player.sendMessage(plugin.colors.getSuccess() + "Fly mode enabled!");
 	}
 	return true;
 }
 else if(cmd.getName().equalsIgnoreCase("rename")){
 	if(!(sender instanceof Player)){
 		sender.sendMessage(plugin.colors.getError() + "This command is for players!");
 		return true;
 	}
 	if(args.length<1){
 		return false;
 	}
 	String raw = args[0];
 	for(int i=1;i<args.length;i++){
 		raw = raw + " " + args[i];
 	}
 	String newName = ChatColor.RESET + raw;
 	newName = useful.colorise(newName);
 	ItemStack toName = player.getItemInHand();
 	if(toName.getTypeId() == 0){
 		sender.sendMessage(plugin.colors.getError() + "Cannot rename air!");
 		return true;
 	}
 	ItemRename manager = new ItemRename(plugin);
 	manager.rename(toName, newName);
 	sender.sendMessage(plugin.colors.getSuccess() + "Successfully renamed the item in hand to " + newName);
 	return true;
 }
 		else if(cmd.getName().equalsIgnoreCase("useful")){ // If the player typed /basic then do the following...
 			if (args.length < 1){
 			sender.sendMessage("The useful plugin version " + config.getDouble("version.current") + " is working! - coded by storm345 - do /useful reload to reload the config.");
 			return true;
 			}
 			else if(args[0].equalsIgnoreCase("changelog")){
 				sender.sendMessage(plugin.colors.getTitle() + "Changelog:");
 				Object[] changes = plugin.changelog.values.toArray();
 				for(int i=0;i<changes.length;i++){
 					String change = (String) changes[i];
 					sender.sendMessage(ChatColor.BOLD + plugin.colors.getInfo() + change);
 				}
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("reload")){
 				try{
 					plugin.getServer().getPluginManager().getPlugin("useful").reloadConfig();
 					plugin.getServer().getPluginManager().getPlugin("useful").reloadConfig();
 				sender.sendMessage(plugin.colors.getSuccess() + "Useful config successfully reloaded!");
 				plugin.colLogger.info("Useful config reloaded by " + sender.getName());
 				}
 				catch(Exception e){
 					sender.sendMessage(plugin.colors.getError() + "Useful config reload was unsuccessful");
 					plugin.colLogger.log(Level.SEVERE, "Useful config was attempting to reload and caused an error:");
 					e.printStackTrace();
 					plugin.colLogger.log(Level.SEVERE, "Useful is attempting to disable itself to avoid errors:..");
 					Plugin me = plugin.getServer().getPluginManager().getPlugin("useful");
 					plugin.getServer().getPluginManager().disablePlugin(me);
 				}
 				
 				
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("head")){
 			//TODO bartys /head command
 		    if(args.length < 1){ //If they didnt mention a name
 		    	return false; //Show them the usage
 		    }
 		    if(!(sender instanceof Player)){ //If not a player
 		    	sender.sendMessage(plugin.colors.getError() + "Not a player!"); //Tell them
 		    	return true; //return
 		    }
 		    String playerName = args[0]; //Make a string of the inputted name!
 			//Put ur code here
 		    ItemStack itemStack = new ItemStack(Material.SKULL_ITEM);
 		    itemStack.setDurability((short) 3);
 		    SkullMeta data = (SkullMeta) itemStack.getItemMeta();
 		    data.setOwner(playerName);
 		    itemStack.setItemMeta(data);
 		    player.sendMessage(plugin.colors.getSuccess() + "Made a skull of " + playerName + "'s face (Place to see it!)!");
 		    player.getInventory().addItem(itemStack);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("potion")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "This command is for players!");
 				return true;
 			}
 			if(args.length < 4){
 				return false;
 			}
 			Potions manager = new Potions(useful.plugin);
 			PotionType type = manager.potionTypeFromString(args[0]);
 			if(type == null){
 				sender.sendMessage(plugin.colors.getTitle() + "Valid potion types:");
 				sender.sendMessage(plugin.colors.getInfo() + "fire_resistance, instant_damage, instant_heal, invisibility, night_vision, poison, regen, slowness, speed, strength, water, weakness");
 				return true;
 			}
 			int level = 0;
 			try {
 				level = Integer.parseInt(args[1]);
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			if(level < 1){
 				sender.sendMessage(plugin.colors.getError() + "Level must be bigger than 0");
 				return true;
 			}
 			//type and level set
 			boolean splash = false;
 			if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("yes")){
 				splash = true;
 			}
 			//nearly...
 			int amount = 0;
 			try {
 				amount = Integer.parseInt(args[3]);
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			if(amount < 1){
 				sender.sendMessage(plugin.colors.getError() + "Amount must be bigger than 0");
 				return true;
 			}
 			//All set!
 			Potion potion;
 			try {
 				potion = new Potion(type, level);
 			} catch (Exception e) {
 				sender.sendMessage(plugin.colors.getError() + "Potion is invalid (Level too high?)");
 				return true;
 			}
 			if(splash){
 				potion.splash();
 			}
 			if(args.length == 5){
 				boolean extended = false;
 				if(args[4].equalsIgnoreCase("yes") || args[4].equalsIgnoreCase("true")){
 					extended = true;
 				}
 				potion.setHasExtendedDuration(extended);
 			}
 			ItemStack potions = potion.toItemStack(amount);
 			player.getInventory().addItem(potions);
 			player.sendMessage(plugin.colors.getSuccess() + "Successfully created potion!");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("timeget")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.timeget.enable");
 		       if(isenabled == false){
 		    	   sender.sendMessage(disabledmessage);
 		    	   return true;
 		       }
 			if(sender instanceof Player == false){
 				sender.sendMessage(plugin.colors.getError() + "This command can only be used by players.");
 				return true;
 			}
 			Location loc = ((Entity) sender).getLocation();
 			long current = loc.getWorld().getTime();
 		       long time = current;
 		       double realtime = time + 6000;
  		   realtime = realtime / 1000;
  		   realtime = (double)Math.round(realtime * 100) / 100;
  		   if (realtime > 24){
  			   realtime = realtime - 24;
  		   }
  		   realtime = (double)Math.round(realtime * 100) / 100;
  		   String r = realtime + "";
  		   r = r.replace(".", ":");
  		   String[] re = r.split(":");
  		   String re1 = re[0];
  		   String re2 = re[1];
  		   if (re1.length() < 2){
  			   
  			   re1 = "0" + re1;
  		   }
  		   Double re2dbl = Double.parseDouble(re2);
 			   re2dbl = re2dbl / 100;
 			   re2dbl = re2dbl * 60;
 			   re2dbl = (double)Math.round(re2dbl * 1) / 1;
 			   if (number > 59){
 				   number = 59;
 			   }
 			   int number=(int)Math.floor(re2dbl);
 			   if (number > 59){
 				   number = 59;
 			   }
 			   re2 = number + "";
  		   if (re2.length() < 2){
  			   if (number < 10){
  			   re2 = "0" + re2; 
  			   }
  			   else {
  			   re2 = re2 + "0";
  			   }
  		   }
  		   r = re1 + ":" + re2;
  		  sender.sendMessage(plugin.colors.getInfo() + "The time is " + time + " or " + r + " in world " + loc.getWorld().getName() + ".");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("gm")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.gamemode.enable");
 		       if(isenabled == false){
 		    	   sender.sendMessage(disabledmessage);
 		    	   return true;
 		       }
 			if (args.length < 1){
 			if(sender instanceof Player == false){
 				sender.sendMessage(plugin.colors.getError() + "This command can only be used by players. Or setting another players gamemode");
 				return true;
 			}
 			Player check = (Player) sender;
 			
 				GameMode gm = check.getGameMode();
 				GameMode CREATIVE = GameMode.CREATIVE;
 				GameMode SURVIVAL = GameMode.SURVIVAL;
 				//GameMode ADVENTURE = GameMode.ADVENTURE;
 				if (gm == SURVIVAL){
 					check.setGameMode(CREATIVE);
 					check.sendMessage(plugin.colors.getInfo() + "Gamemode set to CREATIVE");
 					plugin.colLogger.info(check.getName() + "'s gamemode was changed to creative by themself");
 				}
 				/*
 				 //for next update
 				if (gm == CREATIVE){
 					check.setGameMode(ADVENTURE);
 					check.sendMessage(plugin.colors.getInfo() + "Gamemode set to ADVENTURE");
 					getLogger().info(check.getName() + "'s gamemode was changed to adventure by themself");
 				}
 				*/
 				//in next update change creative to adventure below.
 				if (gm == CREATIVE){
 					check.setGameMode(SURVIVAL);
 					check.sendMessage(plugin.colors.getInfo() + "Gamemode set to SURVIVAL");
 					plugin.colLogger.info(check.getName() + "'s gamemode was changed to survival by themself");
 				}
 			
 			return true;
 			}
 			String pname = args[0];
 			Player check = Bukkit.getPlayer(pname);
 			if (check != null){
 				//do command for player check
 				GameMode gm = check.getGameMode();
 				GameMode CREATIVE = GameMode.CREATIVE;
 				GameMode SURVIVAL = GameMode.SURVIVAL;
 				//GameMode ADVENTURE = GameMode.ADVENTURE;
 				if (gm == SURVIVAL){
 					check.setGameMode(CREATIVE);
 					if (sender instanceof Player && sender.hasPermission("useful.gamemode.others")){
 						check.sendMessage(plugin.colors.getInfo() + "Gamemode set to CREATIVE by " + sender.getName());
 						sender.sendMessage(plugin.colors.getSuccess() + check.getName() + " is now in creative mode.");
 						plugin.colLogger.info(check.getName() + " is now in creative mode and changed by " + sender.getName());
 						return true;
 					}
 					check.sendMessage(plugin.colors.getInfo() + "Gamemode set to CREATIVE");
 					plugin.colLogger.info(check.getName() + "'s gamemode was changed to creative");
 					sender.sendMessage(plugin.colors.getSuccess() + check.getName() + " is now in creative mode.");
 				}
 				/*
 				 //for next update
 				if (gm == CREATIVE){
 					check.setGameMode(ADVENTURE);
 					if (sender instanceof Player){
 						check.sendMessage(plugin.colors.getInfo() + "Gamemode set to ADVENTURE by " + sender.getName());
 						sender.sendMessage(plugin.colors.getSuccess() + check.getName() + " is now in adventure mode.");
 						getLogger().info(check.getName() + " is now in adventure mode and changed by " + sender.getName());
 						return true;
 					}
 					check.sendMessage(plugin.colors.getInfo() + "Gamemode set to ADVENTURE");
 					getLogger().info(check.getName() + "'s gamemode was changed to adventure");
 					sender.sendMessage(plugin.colors.getSuccess() + check.getName() + " is now in adventure mode.");
 				}
 				*/
 				//in next update change creative to adventure below.
 				if (gm == CREATIVE){
 					check.setGameMode(SURVIVAL);
 					if (sender instanceof Player && sender.hasPermission("useful.gamemode.others")){
 						check.sendMessage(plugin.colors.getInfo() + "Gamemode set to SURVIVAL by " + sender.getName());
 						sender.sendMessage(plugin.colors.getSuccess() + check.getName() + " is now in survival mode.");
 						plugin.colLogger.info(check.getName() + " is now in survival mode and changed by " + sender.getName());
 						return true;
 					}
 					check.sendMessage(plugin.colors.getInfo() + "Gamemode set to SURVIVAL");
 					plugin.colLogger.info(check.getName() + "'s gamemode was changed to survival");
 					sender.sendMessage(plugin.colors.getSuccess() + check.getName() + " is now in survival mode.");
 				}
 			}
 			else {
 				sender.sendMessage(plugin.colors.getError() + "Player not found.");
 			}
 			
 			
  		   
  		   
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("time")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.time.enable");
 		       if (isenabled == true){
 		    	   if (sender instanceof Player == false){
 		    		   sender.sendMessage(plugin.colors.getError() + "Only a player can use this command.");
 		    		   return true;
 		    	   }
 		    	   
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage: /" + cmdname + " [day/night/early/late/set/add] (if set/add[Number])");
 		    		   return true;
 		    	   }
 		    	   
 		    	   long time = 0;
 		    	   Location loc = ((Entity) sender).getLocation();
 		    	   if (args[0].equalsIgnoreCase("day")){
 		    		   time = 1500;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Time set to day or 7:30 AM in world " + loc.getWorld().getName() + ".");
 		    	   }
 		    	   else if (args[0].equalsIgnoreCase("midday")){
 		    		   time = 6000;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Time set to midday or 12:00 PM in world " + loc.getWorld().getName() + ".");
 		    	   }
 		    	   else if (args[0].equalsIgnoreCase("night")){
 		    		   time = 15000;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Time set to night or 9:00 PM in world " + loc.getWorld().getName() + ".");
 		    	   }
 		    	   else if (args[0].equalsIgnoreCase("early")){
 		    		   time = -1000;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "The time is early or 5:00 AM in world " + loc.getWorld().getName() + ".");
 		    	   }
 		    	   else if (args[0].equalsIgnoreCase("late")){
 		    		   time = 12500;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "The time is late or 6:30 PM in world " + loc.getWorld().getName() + ".");
 		    	   }
 		    	   else if (args[0].equalsIgnoreCase("set")){
 		    		   if (args.length < 2){
 		    			   sender.sendMessage("Usage: /" + cmdname + " [day/night/early/late/set/add] (if set/add[Number])");
 			    		   return true; 
 		    		   }
 		    		   try {
 		    		   time = Long.parseLong(args[1]);
 		    		   double realtime = time + 6000;
 		    		   realtime = realtime / 1000;
 		    		   realtime = (double)Math.round(realtime * 100) / 100;
 		    		   if (realtime > 24){
 		    			   realtime = realtime - 24;
 		    		   }
 		    		   realtime = (double)Math.round(realtime * 100) / 100;
 		    		   String r = realtime + "";
 		    		   r = r.replace(".", ":");
 		    		   String[] re = r.split(":");
 		    		   String re1 = re[0];
 		    		   String re2 = re[1];
 		    		   if (re1.length() < 2){
 		    			   
 		    			   re1 = "0" + re1;
 		    		   }
 		    		   Double re2dbl = Double.parseDouble(re2);
 	    			   re2dbl = re2dbl / 100;
 	    			   re2dbl = re2dbl * 60;
 	    			   re2dbl = (double)Math.round(re2dbl * 1) / 1;
 	    			   int number=(int)Math.floor(re2dbl);
 	    			   if (number > 59){
 	    				   number = 59;
 	    			   }
 	    			   re2 = number + "";
 	    			   if (re2.length() < 2){
 	    	 			   if (number < 10){
 	    	 			   re2 = "0" + re2; 
 	    	 			   }
 	    	 			   else {
 	    	 			   re2 = re2 + "0";
 	    	 			   }
 	    	 		   }
 		    		   r = re1 + ":" + re2;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Time set to " + time + " or " + r + " in world " + loc.getWorld().getName() + ".");
 		    		   }
 		    		   catch (Exception e){
 		    			   sender.sendMessage(plugin.colors.getError() + args[1] + " is not a number.");
 		    			   return true;
 		    		   }
 		    	   }
 		    	   else if (args[0].equalsIgnoreCase("add")){
 		    		   if (args.length < 2){
 		    			   sender.sendMessage("Usage: /" + cmdname + " [day/night/early/late/set/add] (if set/add[Number])");
 			    		   return true; 
 		    		   }
 		    		   try {
 		    		   Long add = Long.parseLong(args[1]);
 				       long current = loc.getWorld().getTime();
 				       time = current + add;
 				       double realtime = time + 6000;
 		    		   realtime = realtime / 1000;
 		    		   realtime = (double)Math.round(realtime * 100) / 100;
 		    		   if (realtime > 24){
 		    			   realtime = realtime - 24;
 		    		   }
 		    		   realtime = (double)Math.round(realtime * 100) / 100;
 		    		   String r = realtime + "";
 		    		   r = r.replace(".", ":");
 		    		   String[] re = r.split(":");
 		    		   String re1 = re[0];
 		    		   String re2 = re[1];
 		    		   if (re1.length() < 2){
 		    			   
 		    			   re1 = "0" + re1;
 		    		   }
 		    		   Double re2dbl = Double.parseDouble(re2);
 	    			   re2dbl = re2dbl / 100;
 	    			   re2dbl = re2dbl * 60;
 	    			   re2dbl = (double)Math.round(re2dbl * 1) / 1;
 	    			   int number=(int)Math.floor(re2dbl);
 	    			   if (number > 59){
 	    				   number = 59;
 	    			   }
 	    			   re2 = number + "";
 		    		   if (re2.length() < 2){
 		    			   re2 = re2 + "0";
 		    		   }
 		    		   r = re1 + ":" + re2;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Time increased by " + add + " and now is " + r + " in world " + loc.getWorld().getName() + ".");
 		    		   }
 		    		   catch (Exception e){
 		    			   sender.sendMessage(plugin.colors.getError() + args[2] + " is not a number.");
 		    			   return true;
 		    		   }
 		    	   }
 		    	   else {
 		    		   sender.sendMessage(plugin.colors.getError() + "Invalid time argument. Setting time to default..."); 
 		    		   time = 1500;
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Time set to day.");
 		    	   }
 		       loc.getWorld().setTime(time);
 		       }
 		       else {
    				sender.sendMessage(disabledmessage);
    			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("jail")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	   
 					
 			if(args.length < 3)
         	{
         	    //Not enough arguments given!
         		sender.sendMessage("Usage: /jail [Jailname] [Name] [Time(minutes)] [Reason]");
         	}
         	else{
         		String jailname = args[0].toLowerCase();
         		
         		Location jail = null;
         		String locWorld = null;
         		double locX;
         		double locY;
         		double locZ;
         		float locPitch;
         		float locYaw;
 				try {
 					String query = "SELECT DISTINCT * FROM jails WHERE jailname='"+jailname+"' ORDER BY jailname";
 					ResultSet rs = plugin.sqlite.query(query);
 					try {
 						locWorld = rs.getString("locWorld");
 						locX = Double.parseDouble(rs.getString("locX"));
 						locY = Double.parseDouble(rs.getString("locY"));
 						locZ = Double.parseDouble(rs.getString("locZ"));
 						locPitch = Float.parseFloat((rs.getString("locPitch")));
 						locYaw = Float.parseFloat((rs.getString("locYaw")));
 						jail = new Location(getServer().getWorld(locWorld), locX, locY, locZ, locPitch, locYaw);
 						rs.close();
 					} catch (Exception e) {
 						e.printStackTrace();
 						return true;
 					}
 				} catch (Exception e) {
 					sender.sendMessage(plugin.colors.getError() + "Jail not found! Do /jails for a list of them");
 					return true;
 				}
         		
 				/*
         		int timeset = Integer.parseInt(args[2]);
         		
         		if(timeset < 1){
         			sender.sendMessage(plugin.colors.getError() + "Player must be jailed for at least a minute.");
         			return true;
         		}
         		*/
 				double timeold = 0;
         		try {
 					timeold = Double.parseDouble(args[2]);
 				} catch (Exception e) {
 					sender.sendMessage("Usage: /jail [Jailname] [Name] [Time(minutes] [Reason]");
 					return true;
 				}
 				double time = System.currentTimeMillis() + (timeold * 60000);
         		String jailee = args[1];
         		
         		Boolean contains = useful.jailed.containsKey(jailee);
         		
         		if (contains == false){
         			
         			if (jail != null){
         				
         				ArrayList<String> jailinfo = new ArrayList<String>();
         				
         				String sentence = "" + time;
         				
         				
         				StringBuilder messagetosend = new StringBuilder();
 						for (int i = 3; i < args.length; i++) {
 						    if (i != 0)
 						         messagetosend.append(" ");
 						    messagetosend.append(args[i]);
 						}
 						String cause = "" + messagetosend + "";
 						jailinfo.add(sentence);
         				jailinfo.add(jailname);
         				jailinfo.add(cause);
         				
         				useful.jailed.put(jailee, jailinfo);
             			
             			
         				plugin.saveHashMap(useful.jailed, plugin.getDataFolder() + File.separator + "jailed.bin");
 						
             			
             			
             			//sender.sendMessage(plugin.colors.getSuccess() + "Player jailed!");
             			if (plugin.getServer().getPlayer(jailee) != null && plugin.getServer().getPlayer(jailee).isOnline()){
             				
             				plugin.getServer().getPlayer(jailee).sendMessage(plugin.colors.getError() + "You have been jailed for " + timeold + " minutes because" +  cause);
             				PlayerInventory inv = plugin.getServer().getPlayer(jailee).getInventory();
             				inv.clear();
             				plugin.getServer().getPlayer(jailee).setGameMode(GameMode.SURVIVAL);
             				plugin.getServer().getPlayer(jailee).teleport(jail);
             				Bukkit.broadcastMessage(jailee + " has been jailed for " + timeold + " minutes because " +  cause);
             				plugin.colLogger.info(plugin.colors.getInfo() + jailee + " has been jailed for " + timeold + " minutes because" +  cause);
             				
             			}
             			else{
             				sender.sendMessage(plugin.colors.getError() + "Player is offline!");
             			}
             			}
         			
         			
         		}
         		else {
         			
         			sender.sendMessage(plugin.colors.getError() + "Player already jailed!");
         			return true;
         		
         			
         		} //close player not already jailed
         	
         		
         		//sender.sendMessage("" + jailed + "");
         	}
 		       }
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("setjail")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage /setjail [Name]");
 		    	   }
 		    	   else {
 		    	   if (sender instanceof Player){
 		    		   String query = "SELECT jailname FROM jails";
 						boolean shouldReturn = false;
 						try {
 							ResultSet rs = plugin.sqlite.query(query);
 							if(rs == null){
 								sender.sendMessage(plugin.colors.getError() + "Error saving jail.");
 								plugin.colLogger.log(Level.SEVERE, "[Useful] - error saving a new jail.");
 								return true;
 							}
 							while(rs.next() && shouldReturn == false){
 								if(rs.getString("jailname").equalsIgnoreCase(args[0].toLowerCase())){
 									sender.sendMessage(plugin.colors.getError() + "Jail already exists! Use /deljail to remove it.");
 									shouldReturn = true;
 								}
 							}
 							rs.close();
 						} catch (SQLException e) {
 							e.printStackTrace();
 							return true;
 						}
 						if(shouldReturn){
 							return true;
 						}
 						Location loc = ((Entity) sender).getLocation();
 						String world;
 						double x;
 						double y;
 						double z;
 						double yaw;
 						double pitch;
 					world = loc.getWorld().getName();
 					x = loc.getX();
 					y = loc.getY();
 					z = loc.getZ();
 					yaw = loc.getYaw();
 					pitch = loc.getPitch();
 					//We now have all the location details set!
 					String theData = "INSERT INTO jails VALUES('"+args[0].toLowerCase()+"', '"+world+"', "+x+", "+y+", "+z+", "+yaw+", "+pitch+");";
 					
 					try {
 						ResultSet rsi = plugin.sqlite.query(theData);
 						rsi.close();
 					} catch (SQLException e) {
 						sender.sendMessage(plugin.colors.getError() + "Error saving jail.");
 						plugin.colLogger.log(Level.SEVERE, "[Useful] - error saving a new jail.");
 						e.printStackTrace();
 						return true;
 					}
 	        			sender.sendMessage(plugin.colors.getSuccess() + "Jail saved as " + args[0]);
 	        			plugin.colLogger.info("Jail created called " + args[0].toLowerCase() + " by " + sender.getName());
 		    		  
 		    	   }
 		    	   else {
 		    		   sender.sendMessage("Command only available to players.");
 		    	   }
 		    	   }
 		       }
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("mail")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.mail.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage /mail [send/read/clear/clearall] (name) (msg)");
 		    		   return true;
 		    	   }
 		    	   else if (args.length > 1 && args[0].equalsIgnoreCase("send")){
 		    		   String name = args[1].toLowerCase();
 		    		   StringBuilder msg = new StringBuilder();
 						for (int i = 2; i < args.length; i++) {
 						    if (i != 0)
 						         msg.append(" ");
 						    msg.append(args[i]);
 						}
 						ArrayList<String> array = new ArrayList<String>();
 						if (useful.mail.containsKey(name)){
 							array = useful.mail.get(name);
 						}
 						array.add("From " + sender.getName() + ": " + msg);
 						useful.mail.put(name, array);
 						plugin.saveHashMap(useful.mail, plugin.getDataFolder() + File.separator + "mail.bin");
 						sender.sendMessage(ChatColor.GOLD + "Mail sent!");
 						if (plugin.getServer().getPlayer(args[1]) != null){
 							plugin.getServer().getPlayer(args[1]).sendMessage(ChatColor.GOLD + "You have recieved a message from " + sender.getName() + " type /mail read to view it!");
 						}
 		    	   }
 		    	   else if (args.length == 1 && args[0].equalsIgnoreCase("read")){
 		    		   String name = sender.getName().toLowerCase();
 		    		   ArrayList<String> array = new ArrayList<String>();
 						if (useful.mail.containsKey(name)){
 							array = useful.mail.get(name);
 						}
 						else {
 							sender.sendMessage(ChatColor.GOLD + "Your mail:");
 							return true;
 						}
 						sender.sendMessage(ChatColor.GOLD + "Your mail: (type /mail clear to clear your inbox)");
                         String listString = "";
 						
 						//String newLine = System.getProperty("line.separator");
 
 						for (String s : array)
 						{
 						    listString += s + " %n";
 						}
 						//sender.sendMessage(playerName + " " + listString);
 						String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 								for(int x=0 ; x<message.length ; x++) {
 								sender.sendMessage(plugin.colors.getSuccess() + "- " + message[x]); // Send each argument in the message
 								}
 		    	   }
 		    	   else if (args.length == 1 && args[0].equalsIgnoreCase("clear")){
 		    		   ArrayList<String> array = new ArrayList<String>();
 		    		   useful.mail.put(sender.getName(), array);
 		    		   useful.mail.remove(sender.getName());
 		    		   plugin.saveHashMap(useful.mail, plugin.getDataFolder() + File.separator + "mail.bin");
 						sender.sendMessage(ChatColor.GOLD + "Mail deleted!");
 		    	   }
 		    	   else if (args.length == 1 && args[0].equalsIgnoreCase("clearall")){
 		    		   if (sender.hasPermission("useful.mail.clearall")){
 		    			   useful.mail = new HashMap<String, ArrayList<String>>();
 		    			   plugin.saveHashMap(useful.mail, plugin.getDataFolder() + File.separator + "mail.bin");
 							sender.sendMessage(ChatColor.GOLD + "All server mail deleted!"); 
 		    		   }
 		    	   }
 		    	   else {
 		    		   sender.sendMessage("Usage /mail [send/read/clear/clearall] (name) (msg)");
 		    	   }
 		       }
 			  else {
 		        	sender.sendMessage(disabledmessage);
 				  return true;
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("deljail")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage /deljail [Name]");
 		    	   }
 		    	   else {
 		    	   if (sender instanceof Player){
 		    		   boolean found = false;
 		    		   boolean del = false;
 		    		   String query = "SELECT * FROM jails";
 	    			   
 	    			   try {
 	    				   ResultSet rs = plugin.sqlite.query(query);
 	    				   while(rs.next()){
 	    					   if(rs.getString("jailname").equalsIgnoreCase(args[0].toLowerCase())){
 	    						   found = true;
 	    					   }
 	    					   if(found == true){
 	    						   del = true;
 	    					   }
 	    				   }
 	    				if(!found){
 	    					sender.sendMessage(plugin.colors.getError() + "Jail not found!");
 	    				}
 						rs.close();
 					} catch (SQLException e1) {
 						e1.printStackTrace();
 						return true;
 					}
 	    			   if(!del){
 	    		  return true;
 	    			   }
 	    			   //Delete the warp
 	    			   String delquery = "DELETE FROM jails WHERE jailname='"+args[0].toLowerCase()+"'";
 	    			  
 	    			   try {
 	    				   ResultSet delete = plugin.sqlite.query(delquery);
 						delete.close();
 					} catch (SQLException e) {
 						e.printStackTrace();
 						sender.sendMessage(plugin.colors.getError() + "Error deleting jail.");
 						plugin.colLogger.log(Level.SEVERE, "[Useful] - error deleting a jail.");
 						return true;
 					}
 		    		   if (found == true){
 	        			sender.sendMessage(plugin.colors.getSuccess() + "Jail deleted");
 	        			plugin.colLogger.info("Jail deleted called " + args[0] + " by " + sender.getName());
 		    		   }
 		    		   else {
 		    			   sender.sendMessage(plugin.colors.getError() + "Jail not found.");
 		    		   }
 		    		  
 		    	   }
 		    	   else {
 		    		   sender.sendMessage("Command only available to players.");
 		    	   }
 		    	   }
 		       }
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("delwarp")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.warps.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage /delwarp [Name]");
 		    	   }
 		    	   else {
 		    	   if (sender instanceof Player){
 		    			   boolean found = false;
 		    			   boolean isOwner = true;
 		    			   boolean del = false;
 		    			   String query = "SELECT * FROM warps";
 		    			   
 		    			   try {
 		    				   ResultSet rs = plugin.sqlite.query(query);
 		    				   while(rs.next()){
 		    					   if(rs.getString("warpname").equalsIgnoreCase(args[0].toLowerCase())){
 		    						   found = true;
 		    						   if(rs.getString("playername") != sender.getName()){
 		    							   if(sender.hasPermission("useful.delwarp.others") == false){
 		    							   isOwner = false;
 		    							   }
 		    						   }
 		    					   }
 		    					   if(found == true && isOwner == true){
 		    						   del = true;
 		    					   }
 		    				   }
 		    				if(!found){
 		    					sender.sendMessage(plugin.colors.getError() + "Warp not found!");
 		    				}
 		    				if(!isOwner){
 		    					sender.sendMessage(plugin.colors.getError() + "You do not have permission to delete the warps of others (useful.delwarp.others)");
 		    				}
 							rs.close();
 						} catch (SQLException e1) {
 							e1.printStackTrace();
 							return true;
 						}
 		    			   if(!del){
 		    		  return true;
 		    			   }
 		    			   //Delete the warp
 		    			   String delquery = "DELETE FROM warps WHERE warpname='"+args[0].toLowerCase()+"'";
 		    			   
 		    			   try {
 		    				   ResultSet delete = plugin.sqlite.query(delquery);
 							delete.close();
 						} catch (SQLException e) {
 							e.printStackTrace();
 							sender.sendMessage(plugin.colors.getError() + "Error");
 							return true;
 						}
 		    			   sender.sendMessage(plugin.colors.getSuccess() + "Warp deleted!");
 		    			   return true;
 		    	   }
 		    	   else {
 		    		   sender.sendMessage("Command only available to players.");
 		    	   }
 		    	   
 		    	   }
 		       }
 		
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("compass")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "Error: not a player");
 				return true;
 			}
 			Location loc = player.getLocation();
 			float yaw = loc.getYaw();
 			BlockFace face = ClosestFace.getClosestFace(yaw);
 			if(face == BlockFace.DOWN){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking down.");
 			}
 			else if(face == BlockFace.UP){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking up.");
 			}
 			else if(face == BlockFace.NORTH){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking north.");
 			}
 			else if(face == BlockFace.NORTH_EAST){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking north east.");
 			}
 			else if(face == BlockFace.EAST){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking east.");
 			}
 			else if(face == BlockFace.SOUTH_EAST){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking south east.");
 			}
 			else if(face == BlockFace.SOUTH){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking south.");
 			}
 			else if(face == BlockFace.SOUTH_WEST){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking south west.");
 			}
 			else if(face == BlockFace.WEST){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking west.");
 			}
 			else if(face == BlockFace.NORTH_WEST){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking north west.");
 			}
 			else if(face == BlockFace.SELF){
 				sender.sendMessage(plugin.colors.getInfo() + "You are looking at the block your feet are on.");
 			}
 			else{
 				sender.sendMessage(plugin.colors.getError() + "Error");
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("worldgm")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "You are not a player");
 				return true;
 			}
 			if(args.length < 1){
 				return false;
 			}
 			String mode = "survival";
 			if(args[0].equalsIgnoreCase("creative")){
 				mode = "creative";
 			}
 			else if(args[0].equalsIgnoreCase("survival")){
 				mode = "survival";
 			}
 			else if(args[0].equalsIgnoreCase("adventure")){
 				mode = "adventure";
 			}
 			else if(args[0].equalsIgnoreCase("none")){
 				//remove it from list
 				mode = "none";
 				String wname = player.getLocation().getWorld().getName();
 				String delquery = "DELETE FROM worldgm WHERE world='"+wname+"'";
  			   
  			   try {
  				  ResultSet delete = plugin.sqlite.query(delquery);
 					delete.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 					sender.sendMessage(plugin.colors.getError() + "Error");
 					return true;
 				}
  			  sender.sendMessage(plugin.colors.getSuccess() + "The default gamemode for this world set to " + mode + " override with the permission: 'useful.worldgm.bypass'");
 				return true;
 			}
 			else{
 				return false;
 			}
 			//store it in worldgm sqlite table
 			String query = "SELECT world FROM worldgm";
 			String wname = player.getLocation().getWorld().getName();
 			boolean shouldReturn = false;
 			
 			try {
 				ResultSet rs = plugin.sqlite.query(query);
 				while(rs.next()){
 					if(rs.getString("world") == wname){
 						shouldReturn = true;
 					}
 				}
 				rs.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return true;
 			}
 			if(shouldReturn){
 				//remove it from list
 				String delquery = "DELETE FROM worldgm WHERE world='"+wname+"'";
  			   
  			   try {
  				  ResultSet delete = plugin.sqlite.query(delquery);
 					delete.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 					sender.sendMessage(plugin.colors.getError() + "Error");
 					return true;
 				}
 			}
 			//add it to list
 			String theData = "INSERT INTO worldgm VALUES('"+wname+"', '"+mode+"');";
 			
 			try {
 				ResultSet rsi = plugin.sqlite.query(theData);
 				rsi.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return true;
 			}
 			sender.sendMessage(plugin.colors.getSuccess() + "The default gamemode for this world set to " + mode + " override with the permission: 'useful.worldgm.bypass'");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("jailtime")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	   
 		    	   if (sender instanceof Player){
 		    		   if (args.length < 1){
 		    			   String name = sender.getName();
 				    		  if (useful.jailed.containsKey(name)){
 				    			  long currentTime = System.currentTimeMillis();
 				    			  ArrayList<String> array = JailInfo.getPlayerJailInfo(name);
 				    			  //ArrayList<String> array = useful.jailed.get(name);
 				    				String time = array.get(0);
 				    				double time2 = Double.parseDouble(time);
 				    			  double sentence = currentTime - time2;
 				    			  sentence = sentence / 60000;
 				    			  sentence = sentence - sentence - sentence;
 				    			  sentence = (double)Math.round(sentence * 10) / 10;
 				    			  String reason = array.get(2);
 				    			  sender.sendMessage(plugin.colors.getError() + "You were jailed for:" + reason + " and have " + sentence + " minutes left in jail.");
 				    			  
 				    		  }
 				    		  else {
 				    			  sender.sendMessage(plugin.colors.getSuccess() + "You are not in jail!");
 				    		  }
 		    			   return true;
 		    		   }
 		    		   String name = args[0];
 			    		  if (useful.jailed.containsKey(name)){
 			    			  long currentTime = System.currentTimeMillis();
 			    			  ArrayList<String> array = useful.jailed.get(name);
 			    				String time = array.get(0);
 			    				double time2 = Double.parseDouble(time);
 			    			  double sentence = currentTime - time2;
 			    			  sentence = sentence / 60000;
 			    			  sentence = sentence - sentence - sentence;
 			    			  sentence = (double)Math.round(sentence * 10) / 10;
 			    			  String reason = array.get(2);
 			    			  sender.sendMessage(plugin.colors.getError() + name + " has " + sentence + " minutes left in jail and was jailed for " + reason);
 			    			  
 			    		  }
 			    		  else {
 			    			  sender.sendMessage(plugin.colors.getSuccess() + name + " is not in jail!");
 			    		  }
 		    		 
 		    		  
 		    	   }
 		    	   else {
 		    		   sender.sendMessage("Command only available to players.");
 		    	   }
 		    	   }
 		       
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("unjail")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	  
 		    	   
 		    	   
 		    		   if (args.length < 1){
 		    			   sender.sendMessage(plugin.colors.getError() + "Usage: /unjail [Name]");
 		    			   return true;
 		    		   }
 		    		   String name = args[0];
 		    		  if (useful.jailed.containsKey(name)){
 		    			  try {
 		    				  Player p = plugin.getServer().getPlayer(args[0]);
 		    				  
 		    				  useful.jailed.remove(name);
 		    				  plugin.saveHashMap(useful.jailed, plugin.getDataFolder() + File.separator + "jailed.bin");
 	                            String path = plugin.getDataFolder() + File.separator + "jailed.bin";
 	                			File file = new File(path);
 	                			if(file.exists()){ // check if file exists before loading to avoid errors!
 	                				useful.jailed  = plugin.load(path);
 	                			}
 	                			if(!p.isOnline()){
 	                				sender.sendMessage(plugin.colors.getError() + "Player is not online!");
 	                				return true;
 	                			}
 	                			Location spawn = p.getWorld().getSpawnLocation();
 	                			p.teleport(spawn);
 	                			plugin.colLogger.info(name + " has been unjailed.");
 	                            p.sendMessage(plugin.colors.getSuccess() + "You have been unjailed.");
 	                            sender.sendMessage(plugin.colors.getSuccess() + name + " has been unjailed!");
 	                        } catch (Exception ex) {
 	                            // Should never happen
 	                            ex.printStackTrace();
 	                        }
 		    			  
 		    		  }
 		    		  else {
 		    			  sender.sendMessage(plugin.colors.getSuccess() + name + " is not in jail!");
 		    		  }
 		    		  
 		    	   
 		    	   }
 		       
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("jails")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	   ArrayList<String> jailnames = new ArrayList<String>();
 		    	   
 		    	   try {
 		    		   ResultSet jails = plugin.sqlite.query("SELECT DISTINCT jailname FROM jails ORDER BY jailname");
 		    		   while(jails.next()){
 		    			   String name = jails.getString("jailname");
 		    			   jailnames.add(name);
 		    		   }
 		    		   jails.close();
 				} catch (SQLException e) {
 					sender.sendMessage(plugin.colors.getError() + "Errow listing jails from stored data!");
 					getLogger().log(Level.SEVERE, "Errow listing jails from stored data!", e);
 				}
 		    	   
 		    	   Object[] ver = jailnames.toArray();
 		    	   StringBuilder messagetosend = new StringBuilder();
 		           for (int i=0;i<ver.length;i++)
 		           {
 		        	   //output.add(v);
 		        	   String v = (String) ver[i];
 						messagetosend.append(" ");
 						messagetosend.append(v);
 						messagetosend.append(",");
 						
 		           }
 		           int page = 1;
 		           if(args.length > 0){
 		        	   try {
 						page = Integer.parseInt(args[0]);
 					} catch (NumberFormatException e) {
 						sender.sendMessage(plugin.colors.getError() + "Invalid page number");
 						return true;
 					}
 		           }
 		           ChatPage tPage = ChatPaginator.paginate("" + messagetosend, page);
 		           sender.sendMessage(plugin.colors.getTitle() + "Jails: [" + tPage.getPageNumber() + "/" + tPage.getTotalPages() + "]");
 		           String[] lines = tPage.getLines();
 		           for(int i=0;i<lines.length;i++){
 		        	 sender.sendMessage(plugin.colors.getInfo() + lines[i]);  
 		           }
 		    	   }
 		       
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("warps")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.warps.enable");
 		       if(isenabled == true){
 		    	   ArrayList<String> warpnames = new ArrayList<String>();
 		    	   
 		    	   try {
 		    		   ResultSet warps = plugin.sqlite.query("SELECT DISTINCT warpname FROM warps ORDER BY warpname");
 		    		   while(warps.next()){
 		    			   String warpname = warps.getString("warpname");
 		    			   warpnames.add(warpname);
 		    		   }
 		    		   warps.close();
 				} catch (SQLException e) {
 					sender.sendMessage(plugin.colors.getError() + "Errow listing warps from stored data!");
 					getLogger().log(Level.SEVERE, "Errow listing warps from stored data!", e);
 				}
 		    	   
 		    	   Object[] ver = warpnames.toArray();
 		    	   StringBuilder messagetosend = new StringBuilder();
 		           for (int i=0;i<ver.length;i++)
 		           {
 		        	   //output.add(v);
 		        	   String v = (String) ver[i];
 						messagetosend.append(" ");
 						messagetosend.append(v);
 						messagetosend.append(",");
 						
 		           }
 		           int pageNumber = 1;
 		           if(args.length > 0){
 		        	   try {
 						pageNumber = Integer.parseInt(args[0]);
 					} catch (NumberFormatException e) {
 						sender.sendMessage(plugin.colors.getError() + "Is an invalid page number");
 						return true;
 					}
 		           }
 		           ChatPage page = ChatPaginator.paginate("" + messagetosend, pageNumber);
 		           String[] lines = page.getLines();
 		           sender.sendMessage(plugin.colors.getInfo() + "Warps: Page[" + page.getPageNumber() + "/" + page.getTotalPages() + "]");
 		           for(int i=0;i<lines.length;i++){
 		        	   sender.sendMessage(plugin.colors.getInfo() + lines[i]);
 		           }
 		    	   }
 		       
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("warp")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.warps.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 						return true;
 					}
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage /" + cmdname + " [Warp-Name]");
 		    		   return true;
 		    	   }
 		    	   String warpname = args[0].toLowerCase();
 	        		
 	        		Location warp = null;
 	        		String locWorld = null;
 	        		double locX;
 	        		double locY;
 	        		double locZ;
 	        		float locPitch;
 	        		float locYaw;
 					try {
 						String query = "SELECT DISTINCT * FROM warps WHERE warpname='"+warpname+"' ORDER BY warpname";
 						ResultSet wrs = plugin.sqlite.query(query);
 						try {
 							
 							try {
 								locWorld = wrs.getString("locWorld");
 								locX = Double.parseDouble(wrs.getString("locX"));
 								locY = Double.parseDouble(wrs.getString("locY"));
 								locZ = Double.parseDouble(wrs.getString("locZ"));
 								locPitch = Float.parseFloat((wrs.getString("locPitch")));
 								locYaw = Float.parseFloat((wrs.getString("locYaw")));
 								warp = new Location(getServer().getWorld(locWorld), locX, locY, locZ, locPitch, locYaw);
 							} catch (Exception e) {
 							}
 							wrs.close();
 						} catch (Exception e) {
 							e.printStackTrace();
 							return true;
 						}
 						((LivingEntity) sender).teleport(warp);
 						player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
 						sender.sendMessage(plugin.colors.getInfo() + "Warping...");
 					} catch (Exception e) {
 						
 						
 						sender.sendMessage(plugin.colors.getError() + "Warp not found! Do /warps for a list of them");
 					}
 					
 		    	 
 		       }
 		       
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("jailed")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.jail.enable");
 		       if(isenabled == true){
 		    	   Set<String> ver = useful.jailed.keySet();
 		    	   StringBuilder messagetosend = new StringBuilder();
 		           for (String v : ver)
 		           {
 		        	   //output.add(v);
 		        	   
 						messagetosend.append(" ");
 						messagetosend.append(v);
 						messagetosend.append(",");
 						
 		           }
 		           int page = 1;
 		           if(args.length > 0){
 		        	   try {
 						page = Integer.parseInt(args[0]);
 					} catch (NumberFormatException e) {
 						sender.sendMessage(plugin.colors.getError() + "Invalid page number");
 						return true;
 					}
 		           }
 		           ChatPage tPage = ChatPaginator.paginate("" + messagetosend, page);
 		           sender.sendMessage(plugin.colors.getTitle() + "Jailed players: ["+tPage.getPageNumber() + "/" + tPage.getTotalPages() + "]");
 		           String[] lines = tPage.getLines();
 		           for(int i=0;i<lines.length;i++){
 		        	sender.sendMessage(plugin.colors.getInfo() + lines[i]);   
 		           }
 		    	   }
 		       
 			  else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("count")){ // If the player typed /basic then do the following...
 			boolean isenabled = useful.config.getBoolean("general.count.enable");
 		       if(isenabled == true){
 		    	   
 					
 			if(args.length < 1)
         	{
         	    //No arguments given!
         		sender.sendMessage("Usage: /" + cmdname + " [up/down] [number]");
         	}
         	else{
         		String direction = args[0];
 			try {
 				number = Integer.parseInt(args[1]);
 				numberorig = Integer.parseInt(args[1]);
 			} catch (Exception e) {
 				sender.sendMessage("Usage: /" + cmdname + " [up/down] [number]");
 				return true;
 			}
 			if (direction.equalsIgnoreCase("down")){
 				Bukkit.broadcastMessage(plugin.colors.getSuccess() + "Countdown started by " + sender.getName());
 				this.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
 
 					public void run(){
 						while (number > -1){
 							Bukkit.broadcastMessage(ChatColor.AQUA + "" + number);
 						number = number - 1;
 						try {
 							Thread.sleep(1000);
 						} catch (Exception e) {
 							sender.sendMessage("An error occured.");
 						}
 						if (number < 0){
 							Bukkit.broadcastMessage(plugin.colors.getSuccess() + sender.getName() + "'s countdown has ended!");
 							return;
 						}
 						}
 					}
 					
 				});
 			}
 			else if (direction.equalsIgnoreCase("up")){
 				Bukkit.broadcastMessage(plugin.colors.getSuccess() + "Countup started by " + sender.getName());
 				this.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
 
 					public void run(){
 						int number2 = 1;
 						while (number2 <= numberorig){
 							Bukkit.broadcastMessage(ChatColor.AQUA + "" + number2);
 							number2 = number2 + 1;
 						try {
 							Thread.sleep(1000);
 						} catch (Exception e) {
 							sender.sendMessage("An error occured.");
 						}
 						if (number2 > numberorig){
 							Bukkit.broadcastMessage(plugin.colors.getSuccess() + sender.getName() + "'s countup has ended!");
 							return;
 						}
 						}
 					}
 					
 				});
 			}
 				/*
 			while (number > -1){
 				sender.sendMessage("" + number);
 			number = number - 1;
 			try {
 				Thread.sleep(1000);
 			} catch (Exception e) {
 				sender.sendMessage("An error occured.");
 			}
 			}
 			}
 			else if (direction.equalsIgnoreCase("up")){
 				int number2 = 0;
 			while (number2 <= numberorig){
 				sender.sendMessage("" + number2);
 				number2 = number2 + 1;
 				try {
 					Thread.sleep(1000);
 				} catch (Exception e) {
 					sender.sendMessage("An error occured.");
 				}
 			}
 			}
 			*/
 			else {
 				sender.sendMessage("Usage: /count [up/down] [number]");
 				return true;
 			}
         	}
 		       }
 		        else {
 		        	
 		        	sender.sendMessage(disabledmessage);	
 		        }
 			return true;
 		}
 		
 		/*
 		  boolean isenabled = config.getBoolean("general.burn.enable");
 		       if(isenabled == true){
 		    	   
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 		 
 		 */
 	
 		else if (cmd.getName().equalsIgnoreCase("eat")){ // If the player typed /eat then do the following...
 			boolean isenabled = useful.config.getBoolean("general.eat.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if(args.length < 1){
 						((Player) sender).setFoodLevel(21);
 						player.playSound(player.getLocation(), Sound.EAT, 15, 1);
 						sender.sendMessage(plugin.colors.getSuccess() + "You have been fed!");
 						return true;
 						}
 						else {
 							if(sender.hasPermission("useful.eat.others") == false){
 								sender.sendMessage(plugin.colors.getError() + "You don't have the permission useful.eat.others.");
 								return true;
 							}
 							Player p = getServer().getPlayer(args[0]);
 							if(p != null){
 								p.setFoodLevel(20);
 								p.getWorld().playSound(p.getLocation(), Sound.EAT, 15, 1);
 								p.sendMessage(plugin.colors.getSuccess() + sender.getName() + " fed you!");
 								sender.sendMessage(plugin.colors.getSuccess() + "Successfully fed " + p.getName());
 							}
 							else {
 								sender.sendMessage(plugin.colors.getError() + "Player not found!");
 							}
 						}
 					}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("feast")){ // If the player typed /eat then do the following...
 			boolean isenabled = useful.config.getBoolean("general.feast.enable");
 		       if(isenabled == true){
 						Player[] players = getServer().getOnlinePlayers();
 						sender.sendMessage(plugin.colors.getSuccess() + "Let the feast begin!");
 						for(int i = 0; i<players.length; i++){
 							Player p = players[i];
 							p.setFoodLevel(21);
 							p.playSound(player.getLocation(), Sound.EAT, 15, 1);
 							p.sendMessage(plugin.colors.getSuccess() + "You were fed in a feast courtesy of " + sender.getName());
 						}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("murder")){ // If the player typed /eat then do the following...
 			boolean isenabled = useful.config.getBoolean("general.murder.enable");
 		       if(isenabled == true){
 						if(args.length < 1){
 							return false;
 						}
 						Player p = getServer().getPlayer(args[0]);
 						if(p != null){
 							p.setHealth(0);
 						    p.sendMessage(plugin.colors.getError() + "Murdered by " + sender.getName());
 							sender.sendMessage(plugin.colors.getSuccess() + p.getName() + " was successfully murdered!");
 						}
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("genocide")){ // If the player typed /eat then do the following...
 			boolean isenabled = useful.config.getBoolean("general.genocide.enable");
 		       if(isenabled == true){
 						Player[] players = getServer().getOnlinePlayers();
 						sender.sendMessage(plugin.colors.getSuccess() + "Murdered everyone on the server!");
 						for(int i = 0; i<players.length; i++){
 							players[i].sendMessage(plugin.colors.getError() + "Killed in a genocide created by " + sender.getName());
 							players[i].setHealth(0);
 						}
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("smite")){ // If the player typed /eat then do the following...
 			boolean isenabled = useful.config.getBoolean("general.smite.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   sender.sendMessage("Usage /smite [Name]");
 		    		   return true;
 		    	   }
 		    	   
 			        String pName = args[0];
 			        if (getServer().getPlayer(pName) == null){
 			    		   sender.sendMessage(plugin.colors.getError() + "Player not found.");
 			    		   return true;
 			    	   }
 			        Location loc = getServer().getPlayer(pName).getLocation();
 			        boolean damage = config.getBoolean("general.smite.damage");
 			        if (damage){
 			        getServer().getPlayer(pName).getWorld().strikeLightning(loc);
 			        }
 			        else {
 			        	getServer().getPlayer(pName).getWorld().strikeLightningEffect(loc);
 			        }
 			        sender.sendMessage(plugin.colors.getSuccess() + "Player has been smited");
 			        getServer().getPlayer(pName).sendMessage(plugin.colors.getSuccess() + "You have been smited");
 			        plugin.colLogger.info(sender.getName() + " has smited " + pName);
 			        }
 		       
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("setwarp")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.warps.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if (args.length < 1){
 							sender.sendMessage("Usage /setwarp [Name]");
 							return true;
 						}
 						String query = "SELECT warpname FROM warps";
 						boolean shouldReturn = false;
 						
 						try {
 							ResultSet rs = plugin.sqlite.query(query);
 							while(rs.next() && shouldReturn == false){
 								if(rs.getString("warpname").equalsIgnoreCase(args[0].toLowerCase())){
 									sender.sendMessage(plugin.colors.getError() + "Warp already exists! Use /delwarp to remove it.");
 									shouldReturn = true;
 								}
 							}
 							rs.close();
 						} catch (SQLException e) {
 							e.printStackTrace();
 							return true;
 						}
 						if(shouldReturn){
 							return true;
 						}
 						Location loc = ((Entity) sender).getLocation();
 						String owner = sender.getName();
 						String world;
 						double x;
 						double y;
 						double z;
 						double yaw;
 						double pitch;
 					world = loc.getWorld().getName();
 					x = loc.getX();
 					y = loc.getY();
 					z = loc.getZ();
 					yaw = loc.getYaw();
 					pitch = loc.getPitch();
 					//We now have all the location details set!
 					String theData = "INSERT INTO warps VALUES('"+owner+"', '"+args[0].toLowerCase()+"', '"+world+"', "+x+", "+y+", "+z+", "+yaw+", "+pitch+");";
 					
 					try {
 						ResultSet rsi = plugin.sqlite.query(theData);
 						rsi.close();
 					} catch (SQLException e) {
 						e.printStackTrace();
 						return true;
 					}
 					sender.sendMessage(plugin.colors.getSuccess() + "Successfully set warp!");
 					}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("setspawn")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.setspawn.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						Location loc = ((Entity) sender).getLocation();
 						double xd = loc.getX();
 						double yd = loc.getY();
 						double zd = loc.getZ();
 						int x = (int)Math.floor(xd);
 						int y = (int)Math.floor(yd);
 						int z = (int)Math.floor(zd);
 						((Entity) sender).getWorld().setSpawnLocation(x, y, z);
 						sender.sendMessage(plugin.colors.getSuccess() + "World spawn point set!");
 					}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("spawn")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.spawn.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						Location sp = ((Entity) sender).getWorld().getSpawnLocation();
 						sender.sendMessage(plugin.colors.getSuccess() + "Teleported to world's spawn location!");
 						((Entity) sender).teleport(sp);
 					}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("tp")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.tp.enable");
 		       if(isenabled == true){
 		    	   if (!isenabled) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if (args.length < 1){
 						sender.sendMessage("Usage: /" + cmdname + " [name] ([Name]) or... ([Name]) [x] [y] [z]");	
 						}
 						else if (args.length == 2){
 							if (player == null) {
 								sender.sendMessage("This command can only be used by a player");
 							return true;
 							} 
 							Player target = this.getServer().getPlayer(args[1]);
 							Player telepoertee = this.getServer().getPlayer(args[0]);
 							if (target != null && telepoertee != null){
 								telepoertee.sendMessage(plugin.colors.getTp() + "Teleporting you to " + target.getName() + " courtesy of " + sender.getName() + "...");
 								sender.sendMessage(plugin.colors.getTp() + "Teleporting...");
 								((LivingEntity) telepoertee).teleport(target);
 								target.sendMessage(plugin.colors.getTp() + telepoertee.getName() + " was teleported to your location by " + sender.getName() + "!");
 							}
 							else {
 								sender.sendMessage(plugin.colors.getError() + "Player not found.");
 							}
 						}
 						else if (args.length == 3){
 							if (player == null) {
 								sender.sendMessage("This command can only be used by a player");
 							return true;
 							}
 							Player p = getServer().getPlayer(sender.getName());
 							Location sendloc = p.getLocation();
 							World world = sendloc.getWorld();
 							 double x = 0;
 							 double y = 0;
 							 double z = 0;
 							 try{
 								 x = Double.parseDouble(args[0]);
 								 y = Double.parseDouble(args[1]);
 								 z = Double.parseDouble(args[2]);
 							 }
 							 catch(Exception e){
 								 sender.sendMessage(plugin.colors.getError() + "Your coordinates were not recognised as numbers!");
 								 return true;
 							 }
 							 Location loc = new Location(world, x, y, z);
 							 sender.sendMessage(plugin.colors.getTp() + "Teleporting...");
 							 ((LivingEntity) sender).teleport(loc);
 						}
 						else if (args.length == 4){
 							Player p = getServer().getPlayer(args[0]);
 							if (p == null){
 								sender.sendMessage(plugin.colors.getError() + "Player not found.");
 								return true;
 							}
 							Location sendloc = p.getLocation();
 							World world = sendloc.getWorld();
 							 double x = 0;
 							 double y = 0;
 							 double z = 0;
 							 try{
 								 x = Double.parseDouble(args[1]);
 								 y = Double.parseDouble(args[2]);
 								 z = Double.parseDouble(args[3]);
 							 }
 							 catch(Exception e){
 								 sender.sendMessage(plugin.colors.getError() + "Your coordinates were not recognised as numbers!");
 								 return true;
 							 }
 							 Location loc = new Location(world, x, y, z);
 							 sender.sendMessage(plugin.colors.getTp() + "Teleporting...");
 							 p.sendMessage(plugin.colors.getTp() + "Teleporting courtesy of " + sender.getName() + "...");
 							 ((LivingEntity) p).teleport(loc);
 						}
 						else {
 							
 							if (player == null) {
 								sender.sendMessage("This command can only be used by a player");
 							return true;
 							}
 							Player target = this.getServer().getPlayer(args[0]);
 							if (target != null){
 								sender.sendMessage(plugin.colors.getTp() + "Teleporting...");
 								((LivingEntity) sender).teleport(target);
 								target.sendMessage(plugin.colors.getTp() + sender.getName() + " teleported to your location!");
 							}
 							else {
 								sender.sendMessage(plugin.colors.getError() + "Player not found.");
 							}
 						
 						}
 					}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("tpa")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.tpa.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if (args.length < 1){
 						sender.sendMessage("Usage: /" + cmdname + " [Name]");
 						return true;
 						}
 							Location loc = getServer().getPlayer(args[0]).getLocation();
 							Player target = getServer().getPlayer(args[0]);
 							if(loc != null && target != null){
 								//initiate request
 								long reqTime = System.currentTimeMillis() + 60000;
 								TpaReq req = new TpaReq(player.getName(), target.getName(), reqTime, true);
 								TeleportRequest.addTeleportRequest(player, req);
 								sender.sendMessage(plugin.colors.getSuccess() + "Request sent!");
 							}
 							else{
 								sender.sendMessage(plugin.colors.getError() + "Player not found!");
 							}
 					}
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("tpahere")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.tpa.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if (args.length < 1){
 						sender.sendMessage("Usage: /" + cmdname + " [Name]");
 						return true;
 						}
 							Location loc = getServer().getPlayer(args[0]).getLocation();
 							Player target = getServer().getPlayer(args[0]);
 							if(loc != null && target != null){
 								//initiate request
 								long reqTime = System.currentTimeMillis() + 60000;
 								TpaReq req = new TpaReq(player.getName(), target.getName(), reqTime, false);
 								TeleportRequest.addTeleportRequest(player, req);
 								sender.sendMessage(plugin.colors.getSuccess() + "Request sent!");
 							}
 							else{
 								sender.sendMessage(plugin.colors.getError() + "Player not found!");
 							}
 					}
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("tpaccept")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.tpa.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 							TpaReq req = TeleportRequest.getTeleportRequest(player);
 							if(req == null){
 								sender.sendMessage(plugin.colors.getError() + "You don't have any teleport requests!");
 								return true;
 							}
 							String requester;
 								requester = req.getRequester();
 							Player target = null;
 							try {
 								target = plugin.getServer().getPlayer(requester);
 							} catch (Exception e) {
 								sender.sendMessage("An error occured");
 								return true;
 							}
 							if(target == null){
 								sender.sendMessage(plugin.colors.getError() + "You don't have any teleport requests!");
 								return true;
 							}
 							Player[] players = getServer().getOnlinePlayers();
 							boolean online = false;
 							for(int i = 0; i < players.length; i++){
 								if(players[i] == target){
 									online = true;
 								}
 							}
 							if(online == false){
 								TeleportRequest.requests.remove(player);
 								sender.sendMessage(plugin.colors.getError() + "You don't have any teleport requests!");
 								return true;
 							}
 							boolean timeout = req.getTimedOut(System.currentTimeMillis());
 							if(timeout){
 								TeleportRequest.requests.remove(player);
 								sender.sendMessage(plugin.colors.getError() + "Request timeout: Answer sooner.");
 								return true;	
 							}
 							boolean tpa = req.getTpa();
 			                if(tpa){
 							player.sendMessage(plugin.colors.getSuccess() + "Teleporting " + target.getName() + " to you!");
 							target.sendMessage(plugin.colors.getSuccess() + player.getName() + " Accepted your request!");
 							target.teleport(player);
 			                }
 			                else {
 			                	player.sendMessage(plugin.colors.getSuccess() + "Teleporting you to " + target.getName() + "!");
 								target.sendMessage(plugin.colors.getSuccess() + player.getName() + " Accepted your request!");
 								player.teleport(target);
 			                }
 							
 					}
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("tphere")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.tphere.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if (args.length < 1){
 						sender.sendMessage("Usage: /" + cmdname + " [name]");	
 						}
 						else {
 							
 							
 							Player target = this.getServer().getPlayer(args[0]);
 							String sendername = sender.getName();
 							Player sender2 = this.getServer().getPlayer(sendername);
 							if (target != null){
 								sender.sendMessage(plugin.colors.getTp() + "Teleporting...");
 								((LivingEntity) target).teleport(sender2);
 								target.sendMessage(plugin.colors.getTp() + sender.getName() + " teleported you to their location!");
 							}
 							else {
 								sender.sendMessage(plugin.colors.getError() + "Player not found.");
 							}
 						
 						}
 					}
 			        
 			        }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("levelup")){ // If the player typed /eat then do the following...
 			boolean isenabled = config.getBoolean("general.levelup.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						((Player) sender).setLevel(((Player) sender).getLevel() + 1);
 						sender.sendMessage(plugin.colors.getSuccess() + "You have been levelled up!");
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("killmobs")){ // If the player typed /eat then do the following...
 			//Fixed perm glitch!!!!! In plugin.yml!!!
 			boolean isenabled = config.getBoolean("general.killmobs.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if (args.length < 1){
 							Entity senderent = ((Entity) sender);
 							Location loc = senderent.getLocation();
 							 
 							
 							
 							List<Entity> entities =  ((Location) loc).getWorld().getEntities();
 							
 							
 							int killed = 0;
 							
 							Object[] entarray = entities.toArray();
 							
 								Entity listent;
 								
 								
 
 								for (Object s  : entarray)
 								{
 									boolean playerfound = false;
 									
 								    listent = (Entity) s;
 								    EntityType type = listent.getType();
 								    if (listent instanceof Player){
 								    	playerfound = true;
 								    }
 								    
 								    else if (listent instanceof LivingEntity && playerfound == false && type.toString() != "WOLF" && type.toString() != "OCELOT" && type.toString() != "SHEEP" && type.toString() != "COW" && type.toString() != "CHICKEN" && type.toString() != "SQUID" && type.toString() != "VILLAGER" && type.toString() != "MOOSHROOM" && type.toString() != "PIG" && type.toString() != "WITCH" && type.toString() != "BAT" && type.toString() != "ITEM_FRAME"){
 								    	
 								    listent.remove();
 								    killed++; 
 								    }
 								    
 								}
 								
 								
 								
 							
 							sender.sendMessage(plugin.colors.getInfo() + "" + killed + " Monsters's killed in the whole world.");
 							
 							
 						}
 						else {
 							int radius = 0;
 							try {
 								radius = Integer.parseInt(args[0]);
 							} catch (Exception e) {
 								sender.sendMessage("Your radius was not recognised as a number!");
 								return true;
 							}
 						Entity senderent = ((Entity) sender);
 					
 						 
 						Double x = (double) radius;
 						Double y = (double) radius;
 						Double z = (double) radius;
 						List<Entity> near = senderent.getNearbyEntities(x, y, z);
 						
 						
 						int killed = 0;
 						
 						Object[] entarray = near.toArray();
 						
 							Entity listent;
 							
 							
 
 							for (Object s  : entarray)
 							{
 								boolean playerfound = false;
 								
 							    listent = (Entity) s;
 							    EntityType type = listent.getType();
 							    if (listent instanceof Player){
 							    	playerfound = true;
 							    }
 							    
 							    else if (listent instanceof LivingEntity && playerfound == false && type.toString() != "WOLF" && type.toString() != "OCELOT" && type.toString() != "SHEEP" && type.toString() != "COW" && type.toString() != "CHICKEN" && type.toString() != "SQUID" && type.toString() != "VILLAGER" && type.toString() != "MOOSHROOM" && type.toString() != "PIG" && type.toString() != "WITCH" && type.toString() != "BAT" && type.toString() != "ITEM_FRAME"){
 							    	
 							    listent.remove();
 							    killed++; 
 							    }
 							    
 							}
 							
 							
 							
 						
 						sender.sendMessage(plugin.colors.getInfo() + "" + killed + " Monsters's killed in the radius of " + radius + ".");
 						
 						}
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("setlevel")){ // If the player typed /setlevel then do the following...
 			boolean isenabled = config.getBoolean("general.setlevel.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						if(args.length < 1)
 			        	{
 			        	    //No arguments given!
 			        		sender.sendMessage("Usage: /" + cmdname + " [Level]");
 			        		return true;
 			        	}
 						else{
 						int level = 0;
 						try {
 							level = Integer.parseInt(args[0]);
 						} catch (Exception e) {
 							sender.sendMessage("Usage: /" + cmdname + " [Level]");
 							return true;
 						}
 						((Player) sender).setLevel(level);
 						sender.sendMessage(plugin.colors.getSuccess() + "You have been levelled up!");
 						}
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("getid")){ // If the player typed /getid then do the following...
 			 boolean isenabled = config.getBoolean("general.getid.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						ItemStack itemslot = ((HumanEntity) sender).getInventory().getItemInHand();
 						int itemid = itemslot.getType().getId();
 						MaterialData itemname = itemslot.getData();
 						String name = itemname.getItemType().name();
 						short newitemdata = itemslot.getDurability();
 						String theitemname = name;//itemname.toString();
 						//Scanner in = new Scanner(theitemdata).useDelimiter("[^0-9]+");
 						//int itemdataint = in.nextInt();
 						String newitemname = theitemname.replaceAll("[0-9]", "");
 						String neweritemname = newitemname.replace(")", "");
 						String newestitemname = neweritemname.replace("(", "");
 						newestitemname = newestitemname.toLowerCase();
 						newestitemname = newestitemname.replaceAll("facing", "");
 						newestitemname = newestitemname.replaceAll("null", "");
 						//int itemdataint = Integer.parseInt(itemdataext);
 						String id = Integer.toString(itemid);
 						//String data = Integer.toString(itemdataint);
 						if (newitemdata < 1){
 							String message = id;
 							sender.sendMessage(plugin.colors.getInfo() + "The item in hand is " + newestitemname + " id:" + plugin.colors.getSuccess() + message);
 						}
 						else {
 							String message = id + ":"+ newitemdata;
 							sender.sendMessage(plugin.colors.getInfo() + "The item in hand is " + newestitemname + " id:" + plugin.colors.getSuccess() + message);
 						}
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("look")){ // If the player typed /getid then do the following...
 			 boolean isenabled = config.getBoolean("general.getid.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 						Block itemslot = player.getTargetBlock(null, 5);
 						int itemid = itemslot.getType().getId();
 						MaterialData itemname = itemslot.getState().getData();
 						String name = itemname.getItemType().name();
 						int newitemdata = itemslot.getState().getRawData();
 						String theitemname = name;//itemname.toString();
 						//Scanner in = new Scanner(theitemdata).useDelimiter("[^0-9]+");
 						//int itemdataint = in.nextInt();
 						String newitemname = theitemname.replaceAll("[0-9]", "");
 						String neweritemname = newitemname.replace(")", "");
 						String newestitemname = neweritemname.replace("(", "");
 						newestitemname = newestitemname.toLowerCase();
 						newestitemname = newestitemname.replaceAll("facing", "");
 						newestitemname = newestitemname.replaceAll("null", "");
 						//int itemdataint = Integer.parseInt(itemdataext);
 						String id = Integer.toString(itemid);
 						//String data = Integer.toString(itemdataint);
 						if (newitemdata < 1){
 							String message = id;
 							sender.sendMessage(plugin.colors.getInfo() + "The block you are looking at is " + newestitemname + " id:" + plugin.colors.getSuccess() + message);
 						}
 						else {
 							String message = id + ":"+ newitemdata;
 							sender.sendMessage(plugin.colors.getInfo() + "The block you are looking at is " + newestitemname + " id:" + plugin.colors.getSuccess() + message);
 						}
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("burn")){ // If the player typed /burn then do the following...
 			
 			if (player == null) {
 				sender.sendMessage("This command can only be used by a player");
 			} else {
 				if(args.length < 1)
 	        	{
 	        	    //No arguments given!
 	        		sender.sendMessage("You forgot to mention who to burn!");
 	        	}
 				else{
 			Player s = (Player) sender;
 			Player check = Bukkit.getPlayer(args[0]);
 	        Player target = s.getServer().getPlayer(args[0]); // Gets the player who was typed in the command.
 	        // For instance, if the command was "/ignite notch", then the player would be just "notch".
 	        // Note: The first argument starts with [0], not [1]. So arg[0] will get the player typed.
 	        if(check != null){
 	        	boolean isenabled = config.getBoolean("general.burn.enable");
 	        if(isenabled == true){
 	        	target.setFireTicks(10000);
 	        sender.sendMessage("That player is on fire! (If they are in survival)");
 	        }
 	        else {
 	        	
 	        	sender.sendMessage(disabledmessage);	
 	        }
 	        
 	        }
 	        else {
 	        	s.sendMessage(plugin.colors.getError() + "Player not found!");
 	        }
 			}
 			}
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("message")){ // If the player typed /burn then do the following...
 			boolean isenabled = config.getBoolean("general.message.enable");
 		       if(isenabled == true){
 		    	   
 			        	if(args.length < 1)
 			        	{
 			        	    //No arguments given!
 			        		sender.sendMessage("Usage: /" + cmdname + " [Name] [Message]");
 			        	}
 			        	else{
 						StringBuilder messagetosend = new StringBuilder();
 						for (int i = 1; i < args.length; i++) {
 						    if (i != 0)
 						         messagetosend.append(" ");
 						    messagetosend.append(args[i]);
 						}
 					Player check = Bukkit.getPlayer(args[0]);
 			        Player target = getServer().getPlayer(args[0]); // Gets the player who was typed in the command.
 			        // For instance, if the command was "/ignite notch", then the player would be just "notch".
 			        // Note: The first argument starts with [0], not [1]. So arg[0] will get the player typed.
 			        if(check != null){
 
 			        	
 			        target.sendMessage(plugin.colors.getTitle() + "from " + plugin.colors.getSuccess() + sender.getName() + ": " + plugin.colors.getInfo() + messagetosend);
 			        getLogger().info("from " + sender.getName() + " to " + args[0] + " -message:-" + messagetosend);
 			        sender.sendMessage(plugin.colors.getTitle() + "to " + plugin.colors.getSuccess() +  target.getName() + ": " + plugin.colors.getInfo() + messagetosend);
 			        }
 			        	
 			        	
 			        
 			        else {
 			        	sender.sendMessage(plugin.colors.getError() + "Player not found!");
 			        }
 			        	}
 					
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("warn")){ // If the player typed /burn then do the following...
 			boolean isenabled = config.getBoolean("general.warning.enable");
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
 			        Player target = getServer().getPlayer(args[0]); // Gets the player who was typed in the command.
 			        // For instance, if the command was "/ignite notch", then the player would be just "notch".
 			        // Note: The first argument starts with [0], not [1]. So arg[0] will get the player typed.
 			        if(check != null){
 
 			        	
 			        target.sendMessage(plugin.colors.getError() + "You have been warned by " + sender.getName() + " " + plugin.colors.getInfo() + "for" + warnmsg);
 			        getLogger().info(plugin.colors.getError() + target.getName() + " has been warned by " + sender.getName() + " " + plugin.colors.getInfo() + "for" + warnmsg);
 			        sender.sendMessage("Warning sent!");
 			        boolean sendtoall = config.getBoolean("general.warning.sendtoall");
 			        if (sendtoall == true) {
 			        	getServer().broadcastMessage(plugin.colors.getError() + target.getName() + " has been warned by " + sender.getName() + " " + plugin.colors.getInfo() + "for" + warnmsg);
 			        	plugin.warns.add("" + target.getName() + " has been warned by " + sender.getName() + " for" + warnmsg);
 			        	plugin.warns.save();
 			        	String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 			        	plugin.warnsplayer = new ListStore(new File(pluginFolder + File.separator + "warns" + File.separator + target.getName() + ".txt"));
 			        	plugin.warnsplayer.load();
 			        	plugin.warnsplayer.add("* Warned by " + sender.getName() + " for" + warnmsg);
 			        	plugin.warnsplayer.save();
 			        }
 			        else {
 			        
 			        }
 			        }
 			        	
 			        	
 			        
 			        else {
 			        	sender.sendMessage(plugin.colors.getError() + "Player not found!");
 			        }
 			        	}
 					
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("hero")){ // If the player typed /burn then do the following...
 			boolean isenabled = config.getBoolean("general.hero.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   //no arguments given
 		    		   if (player == null) {
 							sender.sendMessage("This command can only be used by a player");
 						} else {
 							String heroName = sender.getName();
 						       if (plugin.heros.contains(heroName) == false){
 						    	   plugin.heros.add(heroName);
 						    	   plugin.heros.save();
 						       Player hero = (Player) sender;
 						       PlayerInventory heroinv = hero.getInventory();
 						       if (heroinv.getHelmet() != null && heroinv.getHelmet() != new ItemStack(0)){
 					    		   ItemStack helmet = heroinv.getHelmet();
 					    		   heroinv.setHelmet(new ItemStack(0));
 					    		   if (helmet.getTypeId() != 0){
 					    		   heroinv.addItem(helmet);
 					    		   }
 					    		   }
 						       if (heroinv.getBoots() != null && heroinv.getBoots() != new ItemStack(0)){
 					    		   ItemStack boots = heroinv.getBoots();
 					    		   heroinv.setBoots(new ItemStack(0));
 					    		   if (boots.getTypeId() != 0){
 					    		   heroinv.addItem(boots);
 					    		   }
 					    		   }
 						       if (heroinv.getChestplate() != null && heroinv.getChestplate() != new ItemStack(0)){
 					    		   ItemStack chest = heroinv.getChestplate();
 					    		   heroinv.setChestplate(new ItemStack(0));
 					    		   if (chest.getTypeId() != 0){
 					    		   heroinv.addItem(chest);
 					    		   }
 					    		   }
 						       if (heroinv.getLeggings() != null && heroinv.getLeggings() != new ItemStack(0)){
 					    		   ItemStack legs = heroinv.getLeggings();
 					    		   heroinv.setLeggings(new ItemStack(0));
 					    		   if (legs.getTypeId() != 0){
 					    		   heroinv.addItem(legs);
 					    		   }
 					    		   }
 						       heroinv.addItem(new ItemStack(276));
 						       heroinv.setBoots(new ItemStack(313));
 						       heroinv.setChestplate(new ItemStack(311));
 						       heroinv.setHelmet(new ItemStack(20));
 						       heroinv.setLeggings(new ItemStack(312));
 						       ((LivingEntity) sender).setHealth(20);
 						       ((Player) sender).setFoodLevel(21);
 						       sender.sendMessage(plugin.colors.getTitle() + "[HERO MODE]" + plugin.colors.getSuccess() + "Hero mode enabled.");
 						       this.getLogger().info("Hero mode enabled for " + sender.getName());
 						       }
 						       else {
 						    	   plugin.heros.remove(heroName);
 						    	   plugin.heros.save();  
 							       Player hero = (Player) sender;
 							       PlayerInventory heroinv = hero.getInventory();
 							       heroinv.removeItem(new ItemStack(276));
 							       if (heroinv.getHelmet() != null && heroinv.getHelmet() != new ItemStack(0)){
 						    		   ItemStack helmet = heroinv.getHelmet();
 						    		   heroinv.setHelmet(new ItemStack(0));
 						    		   if (helmet.getTypeId() != 0){
 						    		   heroinv.addItem(helmet);
 						    		   }
 						    		   }
 							       if (heroinv.getBoots() != null && heroinv.getBoots() != new ItemStack(0)){
 						    		   ItemStack boots = heroinv.getBoots();
 						    		   heroinv.setBoots(new ItemStack(0));
 						    		   if (boots.getTypeId() != 0){
 						    		   heroinv.addItem(boots);
 						    		   }
 						    		   }
 							       if (heroinv.getChestplate() != null && heroinv.getChestplate() != new ItemStack(0)){
 						    		   ItemStack chest = heroinv.getChestplate();
 						    		   heroinv.setChestplate(new ItemStack(0));
 						    		   if (chest.getTypeId() != 0){
 						    		   heroinv.addItem(chest);
 						    		   }
 						    		   }
 							       if (heroinv.getLeggings() != null && heroinv.getLeggings() != new ItemStack(0)){
 						    		   ItemStack legs = heroinv.getLeggings();
 						    		   heroinv.setLeggings(new ItemStack(0));
 						    		   if (legs.getTypeId() != 0){
 						    		   heroinv.addItem(legs);
 						    		   }
 						    		   }
 							       heroinv.removeItem(new ItemStack(313));
 							       heroinv.removeItem(new ItemStack(20));
 							       heroinv.removeItem(new ItemStack(311));
 							       heroinv.removeItem(new ItemStack(312));
 							       //heroinv.setBoots(new ItemStack(0));
 							       //heroinv.setChestplate(new ItemStack(0));
 							       //heroinv.setHelmet(new ItemStack(0));
 							       //heroinv.setLeggings(new ItemStack(0));
 						       sender.sendMessage(plugin.colors.getTitle() + "[HERO MODE]" + plugin.colors.getError() + "Hero mode disabled.");
 						       this.getLogger().info("Hero mode disabled for " + sender.getName());
 						       }
 						}
 		    		   
 		    	   }
 		    	   else{
 		    	   
 						
 			        	
 					
 					 // Gets the player who was typed in the command.
 			        // For instance, if the command was "/ignite notch", then the player would be just "notch".
 			        // Note: The first argument starts with [0], not [1]. So arg[0] will get the player typed.
 			       sender.sendMessage("Usage /hero");
 		    	   }
 			       
 			        	
 					
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("delete-warns")){ // If the player typed /view-warns then do the following...
 			boolean isenabled = config.getBoolean("general.warning.enable");
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
 						new File(pluginFolder + File.separator + "warns" + File.separator + playerName + ".txt").delete();
 						sender.sendMessage(plugin.colors.getSuccess() + playerName + "'s warning's have been deleted.");
 			        	}
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("view-warns")){ // If the player typed /view-warns then do the following...
 			boolean isenabled = config.getBoolean("general.warning.enable");
 		       if(isenabled == true){
 			        	if(args.length < 1)
 			        	{
 			        	    //No arguments given!
 			        		sender.sendMessage("Usage /" + cmdname + " [name]");
 			        	}
 			        	else{
 						String playerName = args[0];
 						String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 						plugin.warnsplayer = new ListStore(new File(pluginFolder + File.separator + "warns" + File.separator + playerName + ".txt"));
 						plugin.warnsplayer.load();
 						ArrayList<String> warnlist =  plugin.warnsplayer.getValues();
 						
 						String listString = "";
 						
 						//String newLine = System.getProperty("line.separator");
 
 						for (String s : warnlist)
 						{
 						    listString += s + " %n";
 						}
 						//sender.sendMessage(playerName + " " + listString);
 						int page = 1;
 				           if(args.length > 1){
 				        	   try {
 								page = Integer.parseInt(args[1]);
 							} catch (NumberFormatException e) {
 								sender.sendMessage(plugin.colors.getError() + "Invalid page number");
 								return true;
 							}
 				           }
 				           ChatPage tPage = ChatPaginator.paginate("" + listString, page);
 				           sender.sendMessage(plugin.colors.getTitle() +  playerName + "'s warnings: ["+tPage.getPageNumber() + "/" + tPage.getTotalPages() + "]");
 				           String[] lines = tPage.getLines();
 				           String list = plugin.colors.getInfo() + "";
 				           for(int i=0;i<lines.length;i++){
 				        	list = plugin.colors.getInfo() + list + plugin.colors.getInfo() + ChatColor.stripColor(lines[i]) + " ";   
 				           }
 						String[] message = list.split("%n"); // Split everytime the "\n" into a new array value
 								for(int x=0 ; x<message.length ; x++) {
 								sender.sendMessage(plugin.colors.getInfo() + ChatColor.stripColor(message[x])); // Send each argument in the message
 								}
 								
 								plugin.warnsplayer.save();
 			        	}
 					
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("warnslog")){ // If the player typed /view-warns then do the following...
 			boolean isenabled = config.getBoolean("general.warning.enable");
 		       if(isenabled == true){
 		    	   if(args.length < 1)
 		        	{
 		        	    //No arguments given!
 						ArrayList<String> warnlist =  plugin.warns.getValues();
 						sender.sendMessage(plugin.colors.getTitle() + "Log of warnings:");
 						
 						String listString = "";
 						
 						//String newLine = System.getProperty("line.separator");
 
 						for (String s : warnlist)
 						{
 						    listString += s + " %n";
 						}
 						//sender.sendMessage(playerName + " " + listString);
 						String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 								for(int x=0 ; x<message.length ; x++) {
 								sender.sendMessage(plugin.colors.getError() + message[x]); // Send each argument in the message
 								}
 								
 		        	}
 		        	else{
 		        		String action = args[0];
 		        		if (action.equalsIgnoreCase("clear")){
 		        			String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 							new File(pluginFolder + File.separator + "warns.log").delete();
 							plugin.warns = new ListStore(new File(pluginFolder + File.separator +"warns.log"));
 							plugin.warns.load();
 							sender.sendMessage(plugin.colors.getSuccess() + "The warning's log has been cleared.");
 							plugin.warns.save();
 		        		}
 		        		else {
 		        			sender.sendMessage("Usage /warnslog ([Nothing, clear])");
 		        		}
 		        	}
 		       }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("rules")){
 			ArrayList<String> rules =  plugin.rules.getValues();
 			sender.sendMessage(plugin.colors.getTitle() + "Server rules:");
 			
 			String listString = "";
 			
 			//String newLine = System.getProperty("line.separator");
 
 			for (String s : rules)
 			{
 			    listString += s + " %n";
 			}
 			//sender.sendMessage(playerName + " " + listString);
 			String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 			
 					for(int x=0 ; x<message.length ; x++) {
 					sender.sendMessage(plugin.colors.getInfo() + "[" + (x+1) + "]" + plugin.colors.getInfo() + useful.colorise(message[x])); // Send each argument in the message
 					}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("information")){
 			ArrayList<String> info =  plugin.info.getValues();
 			
 			String listString = "";
 			
 			//String newLine = System.getProperty("line.separator");
 
 			for (String s : info)
 			{
 			    listString += s + " %n";
 			}
 			//sender.sendMessage(playerName + " " + listString);
 			int page = 1;
 	           if(args.length > 0){
 	        	   try {
 					page = Integer.parseInt(args[0]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(plugin.colors.getError() + "Invalid page number");
 					return true;
 				}
 	           }
 	           ChatPage tPage = ChatPaginator.paginate("" + listString, page);
 	           sender.sendMessage(plugin.colors.getTitle() + "Server info: ["+tPage.getPageNumber() + "/" + tPage.getTotalPages() + "]");
 	           String[] lines = tPage.getLines();
 	           String list = plugin.colors.getInfo() + "";
 	           for(int i=0;i<lines.length;i++){
 	        	list = plugin.colors.getInfo() + list + plugin.colors.getInfo() + lines[i] + " ";   
 	           }
 			String[] message = list.split("%n"); // Split everytime the "\n" into a new array value
 			
 					for(int x=0 ; x<message.length ; x++) {
 					sender.sendMessage(plugin.colors.getInfo() +  useful.colorise(message[x])); // Send each argument in the message
 					}
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("magicmessage")){ // If the player typed /burn then do the following...
 			boolean isenabled = config.getBoolean("general.magicmessage.enable");
 		       if(isenabled == true){
 		    	   if (player == null) {
 						sender.sendMessage("This command can only be used by a player");
 					} else {
 			        	if(args.length < 1)
 			        	{
 			        	    //No arguments given!
 			        		sender.sendMessage("Usage: /" + cmdname + " [Message]");
 			        	}
 			        	else{
 						StringBuilder messagetosender = new StringBuilder();
 						for (int i = 0; i < args.length; i++) {
 						    if (i != 0)
 						         messagetosender.append(" ");
 						    messagetosender.append(args[i]);
 						}
 					// Gets the player who was typed in the command.
 			        // For instance, if the command was "/ignite notch", then the player would be just "notch".
 			        // Note: The first argument starts with [0], not [1]. So arg[0] will get the player typed.
 
 			        	getServer().broadcastMessage(plugin.colors.getInfo() + "<" + sender.getName() + ">" + ChatColor.MAGIC + "" + messagetosender);
 			        	
 			        
 			       
 			        	}
 					}
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("listplayers")){ // If the player typed /setlevel then do the following...
 			  boolean isenabled = config.getBoolean("general.listplayers.enable");
 		       if(isenabled == true){
 		    	   OfflinePlayer[] allplayers = getServer().getOfflinePlayers();
 					StringBuilder playerslist = new StringBuilder();
 					for (int i = 0; i < allplayers.length; i++) {
 					    if (i != 0)
 					         playerslist.append(" ");
 					    playerslist.append(allplayers[i].getName());
 					    playerslist.append(", ");
 					}
 					sender.sendMessage("All the players that have been on this server are: " + playerslist);
 				
 					}
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("ci")){ // If the player typed /setlevel then do the following...
 			  boolean isenabled = config.getBoolean("general.ci.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   //No args given - clear sender inv
 		    		   if (sender instanceof Player == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command. Or do /ci [Name]");
 		    			   return true;
 		    		   }
 		    		   PlayerInventory inv = ((Player )sender).getInventory();
 		    		   inv.clear();
 		    		   inv.setChestplate(new ItemStack(0));
 		    		   inv.setHelmet(new ItemStack(0));
 		    		   inv.setLeggings(new ItemStack(0));
 		    		   inv.setBoots(new ItemStack(0));
 		    		   sender.sendMessage(plugin.colors.getSuccess() + "Inventory has been cleared!");
 		    	   }
 		    	   else {
 		    		   //player name given
 		    		   if (sender instanceof Player && sender.hasPermission("useful.ci.others") == false){
 		    			   if (sender.getName().equalsIgnoreCase(args[0])){
 		    				   
 		    			   }
 		    			   else {
 		    			   sender.sendMessage(plugin.colors.getError() + "You are not allowed to clear other's inventories.");
 		    			   return true;
 		    			   }
 		    		   }
 					   Player target = getServer().getPlayer(args[0]);
 					   if (target == null){
 						   sender.sendMessage(plugin.colors.getError() + "Player not found!");
 						   return true;
 					   }
 		    		   PlayerInventory inv = target.getInventory();
 		    		   inv.clear();
 		    		   inv.setChestplate(new ItemStack(0));
 		    		   inv.setHelmet(new ItemStack(0));
 		    		   inv.setLeggings(new ItemStack(0));
 		    		   inv.setBoots(new ItemStack(0));
 		    		   sender.sendMessage(plugin.colors.getSuccess() + "Inventory has been cleared!");
 		    		   target.sendMessage(plugin.colors.getInfo() + "Your inventory has been cleared by " + sender.getName());
 		    	   }
 		       }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 	}
 		else if (cmd.getName().equalsIgnoreCase("invsee")){ // If the player typed /setlevel then do the following...
 			  boolean isenabled = config.getBoolean("general.invsee.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   //No args given - clear sender inv
 		    		   
 		    		   sender.sendMessage(plugin.colors.getInfo() + "Usage /" + cmdname + " [Name]");
 		    	   }
 		    	   else {
 		    		   //player name given
 		    		   if (sender instanceof Player == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 		    			   return true;
 		    		   }
 					   Player target = getServer().getPlayer(args[0]);
 					   if (target == null){
 						   sender.sendMessage(plugin.colors.getError() + "Player not found!");
 						   return true;
 					   }
 		    		   Inventory inv = target.getInventory();
 		    		   HumanEntity ent = player;
 		    		   useful.invsee.add(sender.getName());
 		    		   player.getWorld().playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
 		    		   ent.openInventory(inv);
 		    	   }
 		       }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 	}
 		else if (cmd.getName().equalsIgnoreCase("creative")){ // If the player typed /setlevel then do the following...
 			  boolean isenabled = config.getBoolean("general.creativecommand.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   if (sender instanceof Player == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 		    			   return true;
 		    		   }
 		    		   //No args given - clear sender inv
 		    		   player.setGameMode(GameMode.CREATIVE);
 		    		   player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1, 10);
 		    		   sender.sendMessage(plugin.colors.getSuccess() + "Your gamemode was set to creative");
 		    		   getLogger().info(sender.getName() + "'s gamemode was set to creative by themselves");
 		    	   }
 		    	   else {
 		    		   //player name given
 		    		   if (sender.hasPermission("useful.gamemode.others") == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You don't have permission to change others gamemodes (useful.gamemode.others)");
 		    		   return true;
 		    		   }
 					   Player target = getServer().getPlayer(args[0]);
 					   if (target == null){
 						   sender.sendMessage(plugin.colors.getError() + "Player not found!");
 						   return true;
 					   }
 		    		   target.setGameMode(GameMode.CREATIVE);
 		    		   target.getWorld().playSound(target.getLocation(), Sound.FIZZ, 1, 10);
 		    		   sender.sendMessage(plugin.colors.getSuccess() + target.getName() + "'s gamemode was set to creative!");
 		    		   target.sendMessage(plugin.colors.getSuccess() + sender.getName() + " set your gamemode to creative!");
 		    		   getLogger().info(target.getName() + "'s gamemode was set to creative by " + sender.getName());
 		    	   }
 		       }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 	}
 		else if (cmd.getName().equalsIgnoreCase("survival")){ // If the player typed /setlevel then do the following...
 			  boolean isenabled = config.getBoolean("general.survivalcommand.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   if (sender instanceof Player == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 		    			   return true;
 		    		   }
 		    		   //No args given - clear sender inv
 		    		   player.setGameMode(GameMode.SURVIVAL);
 		    		   player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1, 10);
 		    		   sender.sendMessage(plugin.colors.getSuccess() + "Your gamemode was set to survival");
 		    		   getLogger().info(sender.getName() + "'s gamemode was set to survival by themselves");
 		    	   }
 		    	   else {
 		    		   //player name given
 		    		   if (sender.hasPermission("useful.gamemode.others") == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You don't have permission to change others gamemodes (useful.gamemode.others)");
 		    		   return true;
 		    		   }
 					   Player target = getServer().getPlayer(args[0]);
 					   if (target == null){
 						   sender.sendMessage(plugin.colors.getError() + "Player not found!");
 						   return true;
 					   }
 					   target.getWorld().playSound(target.getLocation(), Sound.FIZZ, 1, 10);
 		    		   target.setGameMode(GameMode.SURVIVAL);
 		    		   sender.sendMessage(plugin.colors.getSuccess() + target.getName() + "'s gamemode was set to survival!");
 		    		   target.sendMessage(plugin.colors.getSuccess() + sender.getName() + " set your gamemode to survival!");
 		    		   getLogger().info(target.getName() + "'s gamemode was set to survival by " + sender.getName());
 		    	   }
 		       }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 	}
 		else if (cmd.getName().equalsIgnoreCase("adventure")){ // If the player typed /setlevel then do the following...
 			  boolean isenabled = config.getBoolean("general.adventurecommand.enable");
 		       if(isenabled == true){
 		    	   if (args.length < 1){
 		    		   if (sender instanceof Player == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You must be a player to use this command.");
 		    			   return true;
 		    		   }
 		    		   //No args given - clear sender inv
 		    		   player.setGameMode(GameMode.ADVENTURE);
 		    		   player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1, 10);
 		    		   sender.sendMessage(plugin.colors.getSuccess() + "Your gamemode was set to adventure");
 		    		   getLogger().info(sender.getName() + "'s gamemode was set to adventure by themselves");
 		    	   }
 		    	   else {
 		    		   //player name given
 		    		   if (sender.hasPermission("useful.gamemode.others") == false){
 		    			   sender.sendMessage(plugin.colors.getError() + "You don't have permission to change others gamemodes (useful.gamemode.others)");
 		    		   return true;
 		    		   }
 					   Player target = getServer().getPlayer(args[0]);
 					   if (target == null){
 						   sender.sendMessage(plugin.colors.getError() + "Player not found!");
 						   return true;
 					   }
 		    		   target.setGameMode(GameMode.ADVENTURE);
 		    		   target.getWorld().playSound(target.getLocation(), Sound.FIZZ, 1, 10);
 		    		   sender.sendMessage(plugin.colors.getSuccess() + target.getName() + "'s gamemode was set to adventure!");
 		    		   target.sendMessage(plugin.colors.getSuccess() + sender.getName() + " set your gamemode to adventure!");
 		    		   getLogger().info(target.getName() + "'s gamemode was set to adventure by " + sender.getName());
 		    	   }
 		       }
 			        else {
 			        	
 			        	sender.sendMessage(disabledmessage);	
 			        }
 			
 			return true;
 	}
 		else if (cmd.getName().equalsIgnoreCase("ucommands")){ // If the player typed /setlevel then do the following...
 			  PluginDescriptionFile desc = getServer().getPluginManager().getPlugin("useful").getDescription();
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
 					sender.sendMessage(plugin.colors.getError() + "Given page number is not a number!");
 					return true;
 				}
 			  }
 			  int startpoint = (page - 1) * 3;
 			  double tot = keys.size() / 3;
 			  double total = (double)Math.round(tot * 1) / 1;
 			  total += 1;
 			  if (page > total || page < 1){
 				  sender.sendMessage(plugin.colors.getError() + "Invalid page number!");
 				  return true;
 			  }
 			  int totalpages = (int) total;
 			  sender.sendMessage(ChatColor.DARK_GREEN + "Page: [" + page + "/" + totalpages + "]");
 			  for(int i = startpoint; displayed < 3; i++) {
 				  String v;
 				  try {
 					v = commandsavailable[i].toString();
 				} catch (Exception e) {
 					return true;
 				}
 				  Map<String, Object> vmap = cmds.get(v);
 				    @SuppressWarnings("unused")
 					Set<String> commandInfo = vmap.keySet();
 				    String usage = vmap.get("usage").toString();
 				    String description = vmap.get("description").toString();
 				    String perm = vmap.get("permission").toString();
 				    @SuppressWarnings("unchecked")
 					List<String> aliases = (List<String>) vmap.get("aliases");
 				    usage = usage.replaceAll("<command>", v);
 		        	sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + usage + plugin.colors.getTitle() + " Description: " + plugin.colors.getInfo() + description);
 		        	sender.sendMessage(plugin.colors.getTitle() + " Aliases: " + plugin.colors.getInfo() + aliases);
 		        	sender.sendMessage(plugin.colors.getTitle() + " Permission: " + plugin.colors.getInfo() + perm);
 		        	displayed++;
 					}
 			  int next = page + 1;
 			  if (next < total + 1){
 			  sender.sendMessage(ChatColor.DARK_GREEN+ "Do /ucommands " + next + " for the next page of commands!");
 			  }
 			return true;
 	}
 		else if (cmd.getName().equalsIgnoreCase("ucommand")){ // If the player typed /setlevel then do the following...
 			if (args.length < 1){
 				sender.sendMessage("Usage /ucommand [Command]");
 				return true;
 			}
 			String thecmd = args[0];
 			  PluginDescriptionFile desc = getServer().getPluginManager().getPlugin("useful").getDescription();
 			  Map<String, Map<String, Object>> cmds = desc.getCommands();
 			  String v = thecmd;
 				  try {
 					Map<String, Object> vmap = cmds.get(v);
 					if (vmap == null) {
 						sender.sendMessage(plugin.colors.getError() + "Command not found!");
 						return true;
 					}
 					@SuppressWarnings("unused")
 					Set<String> commandInfo = vmap.keySet();
 					String usage = vmap.get("usage").toString();
 					String description = vmap.get("description").toString();
 					String perm = vmap.get("permission").toString();
 					@SuppressWarnings("unchecked")
 					List<String> aliases = (List<String>) vmap.get("aliases");
 					usage = usage.replaceAll("<command>", v);
 					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + usage
 							+ plugin.colors.getTitle() + " Description: "
 							+ plugin.colors.getInfo() + description);
 					sender.sendMessage(plugin.colors.getTitle() + " Aliases: "
 							+ plugin.colors.getInfo() + aliases);
 					sender.sendMessage(plugin.colors.getTitle() + " Permission: "
 							+ plugin.colors.getInfo() + perm);
 				} catch (Exception e) {
 					sender.sendMessage(plugin.colors.getError() + "Command not found!");
 					return true;
 				}
 			return true;
 	}
 		else if(cmd.getName().equalsIgnoreCase("back")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "You are not a player");
 				return true;
 			}
 			String pname = player.getName();
 			Location prev;
 			String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 			File pFile = new File(pluginFolder + File.separator + "player-data" + File.separator + pname + ".yml"); //Should create is doesn't exist?
 			FileConfiguration pData = new YamlConfiguration();
 			try {
 				pData.load(pFile);
 			} catch (FileNotFoundException e) {
 				try {
 					pFile.createNewFile();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InvalidConfigurationException e) {
 				pFile.delete();
 				sender.sendMessage(plugin.colors.getError() + "Error");
 				return true;
 			}
 			if(!(pData.contains("data.previous-location.world"))){
 				sender.sendMessage(plugin.colors.getError() + "Error: previous location wasn't saved!");
 				return true;
 			}
 			World world = null;
 			try {
 				world = getServer().getWorld(pData.getString("data.previous-location.world"));
 			} catch (Exception e) {
 				sender.sendMessage(plugin.colors.getError() + "Error: Invalid world");
 			}
 			try {
 				double x = pData.getDouble("data.previous-location.x");
 				double y = pData.getDouble("data.previous-location.y");
 				double z = pData.getDouble("data.previous-location.z");
 				float yaw = Float.parseFloat(pData.getString("data.previous-location.yaw"));
 				float pitch = Float.parseFloat(pData.getString("data.previous-location.pitch"));
 				prev = new Location(world, x, y, z, yaw, pitch);
 			} catch (NumberFormatException e) {
 				sender.sendMessage(plugin.colors.getError() + "Error - pitch and yaw incorrectly formatted");
 				return true;
 			}
 			player.teleport(prev);
 			sender.sendMessage(plugin.colors.getSuccess() + "Returned to previous location!");
 			player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("enchant")){
 			//TODO /enchant
 			if(!(sender instanceof Player)){
 				sender.sendMessage(plugin.colors.getError() + "Not a player");
 				return true;
 			}
 			if(args.length < 2){
 				return false;
 			}
 			String enchantmentStr = args[0];
 			int enchantmentLvl = 0;
 			try {
 				enchantmentLvl = Integer.parseInt(args[1]);
 			} catch (NumberFormatException e) {
 				sender.sendMessage(plugin.colors.getError() + "Expected a number but got " + args[1]);
 				return true;
 			}
 			if(enchantmentLvl < 1){
 				sender.sendMessage(plugin.colors.getError() + "Level must be higher than 1");
 				return true;
 			}
 			Enchantment toEnchant = getEnchant.getEnchantFromString(enchantmentStr);
 			if(toEnchant == null){
 				sender.sendMessage(plugin.colors.getError() + "Invalid enchantment!" + ChatColor.RESET + plugin.colors.getTitle() + " Valid enchantments are:" + ChatColor.RESET + plugin.colors.getInfo() + "arrow_damage, " +
 						"arrow_fire, arrow_infinite, arrow_knockback, damage_all, damage_spiders, damage_zombies, haste, durability, fire_aspect, knockback, loot_bonus_blocks, loot_bonus_mobs, oxygen, environmental_protection, " +
 						"explosion_protection, fall_protection, fire_protection, arrow_protection, silk_touch, thorns, water_worker");
 				return true;
 			}
 			ItemStack item = player.getItemInHand();
 			if(item.getTypeId() == 0){
 				sender.sendMessage(plugin.colors.getError() + "Cannot enchant air!");
 			}
 			try {
 				item.addUnsafeEnchantment(toEnchant, enchantmentLvl);
 			} catch (Exception e) {
 				sender.sendMessage(plugin.colors.getError() + "Enchantment invalid! (Level is too high/item is invalid for that enchantment)");
 				return true;
 			}
 			sender.sendMessage(plugin.colors.getSuccess() + "Successfully enchanted to level " + enchantmentLvl);
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("uhost")){ // If the player typed /setlevel then do the following...
 			//TODO uhost
 			if (args.length < 1){
 				//do summin
 				return false;
 			}
 			if(player != null){
 				//is a player
 				player.getWorld().playSound(player.getLocation(), Sound.CLICK, 1, 10);
 			}
 			String version = getServer().getBukkitVersion();
 			int maxplayers = getServer().getMaxPlayers();
 			String motd = getServer().getMotd();
 			String name = getServer().getServerName();
 			int port = getServer().getPort();
 			@SuppressWarnings("rawtypes")
 			List world = getServer().getWorlds();
 			Object[] worlds = world.toArray();
 			String worldnames = "";
 			for (Object s  : worlds)
 			{
 				String wname = ((World) s).getName();
 				worldnames = worldnames + " " + wname + ",";
 			}
 			String flight = "false";
 			boolean doflight = getServer().getAllowFlight();
 			if (doflight){
 				flight = "true";
 			}
 			OfflinePlayer[] players = getServer().getOfflinePlayers();
 			int playerCount = players.length;
 			OfflinePlayer[] playerso = getServer().getOnlinePlayers();
 			int online = playerso.length;
 			boolean nether = getServer().getAllowNether();
 			boolean end = getServer().getAllowEnd();
 			int animalSpawnLimit = getServer().getAnimalSpawnLimit();
 			int monsterSpawnLimit = getServer().getMonsterSpawnLimit();
 			int spawnRadius = getServer().getSpawnRadius();
 			int WaterSpawnLimit = getServer().getWaterAnimalSpawnLimit();
 			long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
 			long runningTime = ((startTime - System.currentTimeMillis()) / 1000) / 60;
 			String runTime = "" + runningTime;
 			runTime = runTime.replaceAll("-", "");
 			double runningTimeHr = Math.round((Double.parseDouble(runTime) / 60) * 10) / 10;
 			String runTimeHr = "" + runningTimeHr;
 			runTimeHr = runTimeHr.replaceAll("-", "");
 			double time = Double.parseDouble(runTime);
 			String DD = Math.round((time/24/60)*1)/1 + "";
 			String HH = Math.round((time/60%24)*1)/1 + "";
 			String MM = Math.round((time%60)*1)/1 + "";
 			if (DD.length() < 2){
 				DD = "0" + DD;
 			}
 			if (HH.length() < 2){
 				HH = "0" + HH;
 			}
 			if (MM.length() < 2){
 				MM = "0" + MM;
 			}
 			String theTime = DD + ":" + HH + ':' + MM;
 			long totMem = Runtime.getRuntime().totalMemory() / 1024 / 1024;// Allocated memory MB
 			long freMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;//Free memory MB
 			long usedMem = totMem - freMem;//Used memory MB
 			if (args[0].equalsIgnoreCase("stats")){
 				//show stats
 				sender.sendMessage(plugin.colors.getSuccess() + "" + ChatColor.BOLD + "Server stats:");
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server name: " + ChatColor.RESET + "" + plugin.colors.getInfo() + name);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server run time: " + ChatColor.RESET + "" + plugin.colors.getInfo() + "(Days:Hours:Minutes) " + theTime);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server version: " + ChatColor.RESET + "" + plugin.colors.getInfo() + version);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Player count: " + ChatColor.RESET + "" + plugin.colors.getInfo() + playerCount);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Online players: " + ChatColor.RESET + "" + plugin.colors.getInfo() + online);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server worlds: " + ChatColor.RESET + "" + plugin.colors.getInfo() + worldnames);
 				
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("ram")|| args[0].equalsIgnoreCase("memory")){
 				sender.sendMessage(plugin.colors.getSuccess() + "" + ChatColor.BOLD + "Server memory/RAM:");
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server allocated memory: " + ChatColor.RESET + "" + plugin.colors.getInfo() + totMem + "MB");
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server free memory: " + ChatColor.RESET + "" + plugin.colors.getInfo() + freMem + "MB");
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server memory usage: " + ChatColor.RESET + "" + plugin.colors.getInfo() + usedMem + "MB");
 			    return true;
 			}
 			else if(args[0].equalsIgnoreCase("clean")|| args[0].equalsIgnoreCase("optimise")||args[0].equals("gc")){
 				sender.sendMessage(plugin.colors.getSuccess() + "The server memory has been purged of all unused objects to free up memory/ram");
 				System.gc();
 			    return true;
 			}
 			else if(args[0].equalsIgnoreCase("system")){
 				Properties props = System.getProperties();
 				//user.language  user.timezome java.runtime.name user.country java.runtime.version
 				String server_lang;
 				String server_timezone;
 				String server_script;
 				String server_country;
 				String java_version;
 				try {
 					server_lang = props.getProperty("user.language");
 					server_timezone = props.getProperty("user.timezone");
 					server_script = props.getProperty("java.runtime.name");
 					server_country = props.getProperty("user.country");
 					java_version = props.getProperty("java.runtime.version");
 				} catch (Exception e) {
 					sender.sendMessage("Error has occurred while obtaining system information");
 					return true;
 				}
 				sender.sendMessage(plugin.colors.getSuccess() + "" + ChatColor.BOLD + "Server system information:");
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server default language: " + ChatColor.RESET + "" + plugin.colors.getInfo() + server_lang);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server default timezone: " + ChatColor.RESET + "" + plugin.colors.getInfo() + server_timezone);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server script language: " + ChatColor.RESET + "" + plugin.colors.getInfo() + server_script);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server country: " + ChatColor.RESET + "" + plugin.colors.getInfo() + server_country);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server reccomended java version to use: " + ChatColor.RESET + "" + plugin.colors.getInfo() + java_version);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Useful plugin version: " + config.getDouble("version.current"));
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("properties")){
 				sender.sendMessage(plugin.colors.getSuccess() + "" + ChatColor.BOLD + "Server properties:");
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server name: " + ChatColor.RESET + "" + plugin.colors.getInfo() + name);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server MOTD: " + ChatColor.RESET + "" + plugin.colors.getInfo() + motd);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Maximum players: " + ChatColor.RESET + "" + plugin.colors.getInfo() + maxplayers);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server port: " + ChatColor.RESET + "" + plugin.colors.getInfo() + port);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server allow flight: " + ChatColor.RESET + "" + plugin.colors.getInfo() + flight);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server allow nether: " + ChatColor.RESET + "" + plugin.colors.getInfo() + nether);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Server allow end: " + ChatColor.RESET + "" + plugin.colors.getInfo() + end);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Animal spawn limit: " + ChatColor.RESET + "" + plugin.colors.getInfo() + animalSpawnLimit);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Monster spawn limit: " + ChatColor.RESET + "" + plugin.colors.getInfo() + monsterSpawnLimit);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Water Creature spawn limit: " + ChatColor.RESET + "" + plugin.colors.getInfo() + WaterSpawnLimit);
 				sender.sendMessage(plugin.colors.getTitle() + "" + ChatColor.BOLD + "Spawn protection radius: " + ChatColor.RESET + "" + plugin.colors.getInfo() + spawnRadius);
 			    return true;
 			}
 			else if(args[0].equalsIgnoreCase("performance")){
 				//performance stuff
 				if(useful.uhost_settings.get("performance") != null && useful.uhost_settings.get("performance") == true){
 					sender.sendMessage(plugin.colors.getSuccess() + "Performance mode disabled!");
 					useful.uhost_settings.put("performance", false);
 					Performance.performanceMode(false);
 				}
 				else {
 					//enable performance
 					sender.sendMessage(plugin.colors.getSuccess() + "Performance mode enabled!");
 					useful.uhost_settings.put("performance", true);
 					Performance.performanceMode(true);
 				}
 				plugin.saveHashMapBoolean(useful.uhost_settings, plugin.getDataFolder() + File.separator + "uhost_settings.bin");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("logger")){
 				if(sender instanceof Player == false){
 					sender.sendMessage(plugin.colors.getError() + "Only players can use this subcommand");
 					return true;
 				}
 				if(plugin.commandViewers.contains(sender.getName()) == false){
 				plugin.commandViewers.add(sender.getName());
 				sender.sendMessage(plugin.colors.getSuccess() + "Logger mode enabled!");
 				plugin.commandViewers.save();
 				}
 				else if(plugin.commandViewers.contains(sender.getName())){
 					plugin.commandViewers.remove(sender.getName());
 					plugin.commandViewers.save();
 					sender.sendMessage(plugin.colors.getSuccess() + "Logger mode disabled!");
 				}
 				plugin.commandViewers.save();
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("dplugin")){
 				//disable plugin args[1]
 				if(args.length < 2){
 					return false;
 				}
 				Plugin[] plugins = getServer().getPluginManager().getPlugins();
 				boolean exists = false;
 				Plugin thepl = null;
 				for (int i = 0; i < plugins.length; i++) {
 			         if(plugins[i].getName().equalsIgnoreCase(args[1])){
 			        	 exists = true;
 			        	 thepl = plugins[i];
 			         }
 				}
 				if(exists == false){
 					sender.sendMessage(plugin.colors.getError() + "Plugin not found!");
 					return true;
 				}
 				sender.sendMessage(plugin.colors.getSuccess() + "Plugin disabled!");
 				getServer().getPluginManager().disablePlugin(thepl);
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("config")){
 				/*
 				if(args.length < 5){
 					// 1 = this, 2 = plugin, 3 = node, 4 = setting
 					sender.sendMessage("Usage: /" + cmdname + " config [Set [Plugin] [Node] [Setting]] [Unset [Plugin] [Node]] [List [Plugin] [Page number]]");
 				return true;
 				}
 				*/
 				if(args.length < 2){
 					sender.sendMessage("Usage: /" + cmdname + " config [Set [Plugin] [Node] [Setting]] [Unset [Plugin] [Node]] [List [Plugin] [Page number]] [View [Plugin] [Node]]");
 					return true;
 				}
 				String action = args[1];
 				if(action.equalsIgnoreCase("set")){
 					if(args.length < 5){
 						sender.sendMessage("Usage: /" + cmdname + " config [Set [Plugin] [Node] [Setting]] [Unset [Plugin] [Node]] [List [Plugin] [Page number]] [View [Plugin] [Node]]");
 						return true;
 					}
 					String pname = args[2];
 					Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
 					boolean found = false;
 					Plugin tPlugin = null;
 					for(int i = 0;i<plugins.length;i++){
 						Plugin test = plugins[i];
 					    if(test.getName().equalsIgnoreCase(pname)){
 					    	tPlugin = test;
 					    	found = true;
 					    }
 					}
 					if(plugin == null || found == false){
 						sender.sendMessage(plugin.colors.getError() + "Plugin not found! Do /plugins for a list!");
 						return true;
 					}
 					String type = "unknown";
 					FileConfiguration config = tPlugin.getConfig();
 					String node = args[3];
 					String setting = args[4];
 					float num = 0;
 					try {
 						num = Float.parseFloat(setting);
 						type = "number";
 					} catch (NumberFormatException e) {
 						type = "unknown";
 					}
 					boolean bool = false;
 					if(setting.equalsIgnoreCase("true")){
 						bool = true;
 						type = "boolean";
 					}
 					else if(setting.equalsIgnoreCase("false")){
 						bool = false;
 						type = "boolean";
 					}
 					if(type == "unknown"){
 						//It is a string
 						for(int i = 5; i< args.length;i++){
 							setting = setting + " " + args[i];
 						}
 						config.set(node, setting);
 					}
 					else if(type == "number"){
 						//It is a number
 						config.set(node, num);
 					}
 					else if(type == "boolean"){
 						//It is a boolean
 						config.set(node, bool);
 					}
 					try {
 						tPlugin.saveConfig();
 					} catch (Exception e) {
 						sender.sendMessage(plugin.colors.getError() + "Unable to find config file for the plugin " + tPlugin.getName());
 						return true;
 					}
 				sender.sendMessage(plugin.colors.getSuccess() + "Successfully created/set the node: " + node + " to " + setting + " in " + tPlugin.getName() + " reload for it to take effect!");
 					
 				return true;
 				}
 				else if(action.equalsIgnoreCase("unset")){
 					if(args.length < 4){
 						sender.sendMessage("Usage: /" + cmdname + " config [Set [Plugin] [Node] [Setting]] [Unset [Plugin] [Node]] [List [Plugin] [Page number]] [View [Plugin] [Node]]");
 						return true;
 					}
 				String pname = args[2];
 				String node = args[3];
 				Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
 				boolean found = false;
 				Plugin tPlugin = null;
 				for(int i = 0;i<plugins.length;i++){
 					Plugin test = plugins[i];
 				    if(test.getName().equalsIgnoreCase(pname)){
 				    	tPlugin = test;
 				    	found = true;
 				    }
 				}
 				if(plugin == null || found == false){
 					sender.sendMessage(plugin.colors.getError() + "Plugin not found! Do /plugins for a list!");
 					return true;
 				}
 				//String type = "unknown";
 				FileConfiguration config = tPlugin.getConfig();
 				config.set(node, null);
 				try {
 					tPlugin.saveConfig();
 				} catch (Exception e) {
 					sender.sendMessage(plugin.colors.getError() + "Unable to find config file for the plugin " + tPlugin.getName());
 					return true;
 				}
 			sender.sendMessage(plugin.colors.getSuccess() + "Successfully removed the node: " + node + " in " + tPlugin.getName() + " reload for it to take effect!");
 				
 			return true;
 				}
 				else if(action.equalsIgnoreCase("list")){
 					if(args.length < 4){
 						sender.sendMessage("Usage: /" + cmdname + " config [Set [Plugin] [Node] [Setting]] [Unset [Plugin] [Node]] [List [Plugin] [Page number]] [View [Plugin] [Node]]");
 						return true;
 					}
 					String pname = args[2];
 					String page = args[3];
 					int pnum = 0;
 					try {
 						pnum = Integer.parseInt(page);
 					} catch (NumberFormatException e) {
 						sender.sendMessage(plugin.colors.getError() + "Page number is incorrect!");
 						return true;
 					}
 					Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
 					boolean found = false;
 					Plugin tPlugin = null;
 					for(int i = 0;i<plugins.length;i++){
 						Plugin test = plugins[i];
 					    if(test.getName().equalsIgnoreCase(pname)){
 					    	tPlugin = test;
 					    	found = true;
 					    }
 					}
 					if(plugin == null || found == false){
 						sender.sendMessage(plugin.colors.getError() + "Plugin not found! Do /plugins for a list!");
 						return true;
 					}
 					//String type = "unknown";
 					FileConfiguration config = tPlugin.getConfig();
 					Set<String> keys = config.getKeys(true);
 					Object[] nodes = keys.toArray();
 					List<String> listNodes = new ArrayList<String>();
 					if(tPlugin.getName().equalsIgnoreCase("useful")){
 					for(int i=0;i<nodes.length;i++){
 						listNodes.add((String) nodes[i]);
 					}
 					for(int i=0;i<listNodes.size();i++){
 						String node = listNodes.get(i);
 						if((node).contains("description") && tPlugin.getName().equalsIgnoreCase("useful")){
 							listNodes.remove(node);
 						}
 					}
 					nodes = listNodes.toArray();
 					}
 					int totalPages = nodes.length / 15;
 					totalPages += 1;
 					sender.sendMessage(plugin.colors.getTitle() + "Valid nodes for " + tPlugin.getName() + ":" + plugin.colors.getInfo() + "(Page: " + pnum + "/"+totalPages+")");
 					int displayed = 0;
 					int start = pnum - 1;
 					start = start * 15;
 					for(int i = start;i<nodes.length && displayed < 15;i++){
 						String v = (String) nodes[i];
 						if(!(v.contains("."))){
 							sender.sendMessage(plugin.colors.getTitle() + v);
 						}
 						else{
 							String[] nodeParts = v.split("\\.");
 							if(nodeParts.length < 3 && nodeParts.length > 1){
 								nodeParts[0] = ChatColor.RED + nodeParts[0];
 								nodeParts[1] = ChatColor.YELLOW + nodeParts[1];
 								v = nodeParts[0] + "." + nodeParts[1];
 							}
 							if(nodeParts.length > 2){
 								nodeParts[0] = ChatColor.RED + nodeParts[0];
 								nodeParts[1] = ChatColor.YELLOW + nodeParts[1];
 								v = nodeParts[0] + "." + nodeParts[1];
 								for(int o=2;o<nodeParts.length;o++){
 									v = v + "." + ChatColor.BLUE + nodeParts[o];
 								}
 							}
 						sender.sendMessage(plugin.colors.getInfo() + v);
 						}
 						displayed++;
 					}
 				}
 				else if(action.equalsIgnoreCase("view")){
 					//TODO
 					String pname = args[2];
 					Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
 					boolean found = false;
 					Plugin tPlugin = null;
 					for(int i = 0;i<plugins.length;i++){
 						Plugin test = plugins[i];
 					    if(test.getName().equalsIgnoreCase(pname)){
 					    	tPlugin = test;
 					    	found = true;
 					    }
 					}
 					if(plugin == null || found == false){
 						sender.sendMessage(plugin.colors.getError() + "Plugin not found! Do /plugins for a list!");
 						return true;
 					}
 					FileConfiguration config = tPlugin.getConfig();
 					String node = args[3];
 					Object result = config.get(node);
 					sender.sendMessage(plugin.colors.getSuccess() + "Value of " + node + " in " + tPlugin.getName() + " is: " + result);
 					return true;
 				}
 				else{
 					sender.sendMessage("Usage: /" + cmdname + " config [Set [Plugin] [Node] [Setting]] [Unset [Plugin] [Node]] [List [Plugin] [Page number]] [View [Plugin] [Node]]");
 					return true;
 				}
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("perms")){
 				if(!(useful.config.getBoolean("uperms.enable"))){
 					sender.sendMessage(plugin.colors.getError() + "Uperms is not enabled in the config!");
 					return true;
 				}
 				//TODO ingame perms
 				if(args.length < 2){
 					sender.sendMessage("Usage: /" + cmdname + " perms [Group/Player/Reload]");
 				    return true;	
 				}
 				if(args[1].equalsIgnoreCase("reload")){
 					useful.uperms = new YamlConfiguration();
 					useful.uperms.options().pathSeparator('/');
 					plugin.loadYamls();
 					Player[] onlineP = plugin.getServer().getOnlinePlayers();
 					for(int i=0;i<onlineP.length;i++){
 						plugin.permManager.unLoadPerms(onlineP[i].getName());
 						plugin.permManager.refreshPerms(onlineP[i]);
 					}
 					sender.sendMessage(plugin.colors.getSuccess() + "Successfully refreshed server permissions to match currently loaded file!");
 					return true;
 				}
 				if(args[1].equalsIgnoreCase("group")){
 					if(args.length < 3){
 						sender.sendMessage("Usage: /" + cmdname + " <Perms> <Group> [<SetPerm> <Name> <Perm> <Value>], [<UnsetPerm> <Name> <Perm>], [<Create> <Name> <Inheritance>], [<Delete> <Name>], [<List>], [<View> <Group> <Page>]");
 					    return true;
 					}
 					String action = args[2];
 					boolean valid = true;
 					if(action.equalsIgnoreCase("setperm")){
 						String gname = "";
 						if(args.length < 6){
 							valid = false;
 						}
 						if(valid){
 						gname = args[3];
 						String gperm = args[4];
 						String gval = args[5];
 						Boolean Val;
 						try {
 							Val = Boolean.parseBoolean(gval);
 						} catch (Exception e) {
 							sender.sendMessage(plugin.colors.getError() + "Value must be true or false!");
 							return true;
 						}
 						gname = plugin.permManager.groupExists(gname);
 						if(gname == "^^error^^"){
 							sender.sendMessage(plugin.colors.getError() + "Group doesn't exist!");
 							return true;
 						}
 						String path = "groups/"+gname+"/permissions";
 						gperm = gperm.replaceAll(":", "");
 						plugin.permManager.setPerm(path, gperm, Val);
 						}
 						if(valid){
 						sender.sendMessage(plugin.colors.getSuccess() + "Successfully set perm for "+gname+"!");
 						}
 					}
 					else if(action.equalsIgnoreCase("unsetperm")){
 						String gname = "";
 						if(args.length < 5){
 							valid = false;
 						}
 						if(valid){
 						gname = args[3];
 						String gperm = args[4];
 						gname = plugin.permManager.groupExists(gname);
 						if(gname == "^^error^^"){
 							sender.sendMessage(plugin.colors.getError() + "Group doesn't exist!");
 							return true;
 						}
 						String path = "groups/"+gname+"/permissions";
 						plugin.permManager.setPerm(path, gperm, null);
 						}
 						if(valid){
 						sender.sendMessage(plugin.colors.getSuccess() + "Successfully unset perm for "+gname+"!");
 						}
 					}
 					else if(action.equalsIgnoreCase("create")){
 						if(args.length < 5){
 							valid = false;
 						}
 						if(valid){
 						String gname = args[3];
 						List<String> inherited = new ArrayList<String>();
 						for(int i=4;i<args.length;i++){
 							inherited.add(args[i]);
 						}
 						plugin.permManager.createGroup(gname, inherited);
 						sender.sendMessage(plugin.colors.getSuccess() + "Successfully created the group " + gname);
 						}
 					}
 					else if(action.equalsIgnoreCase("delete")){
 						if(args.length < 4){
 							valid = false;
 						}
 						String gname = args[3];
 						gname = plugin.permManager.groupExists(gname);
 						if(gname == "^^error^^"){
 							sender.sendMessage(plugin.colors.getError() + "Group doesn't exist!");
 							return true;
 						}
 						plugin.permManager.removeGroup(gname);
 						sender.sendMessage(plugin.colors.getSuccess() + "Successfully deleted the group " + gname);
 					}
 					else if(action.equalsIgnoreCase("list")){
 						List<String> groups = plugin.permManager.listGroups();
 						Object[] array = groups.toArray();
 						String result = "**start**";
 						for(int i=0;i<array.length;i++){
 							if(result == "**start**"){
 								result = (String) array[i];
 							}
 							else{
 							result = result + "," + array[i];
 							}
 						}
 						sender.sendMessage(plugin.colors.getTitle() + "Groups: " + plugin.colors.getInfo() + result);
 					}
 					else if(action.equalsIgnoreCase("view")){
 					      if(args.length < 5){
 					    	  valid = false;
 					      }
 					      if(valid){
 						String gname = args[3];
 						gname = plugin.permManager.groupExists(gname);
 						if(gname == "^^error^^"){
 							sender.sendMessage(plugin.colors.getError() + "Group doesn't exist!");
 							return true;
 						}
 						int page = 1;
 						try {
 							page = Integer.parseInt(args[4]);
 						} catch (NumberFormatException e) {
 							sender.sendMessage(plugin.colors.getError() + "Page number incorrect");
 							return true;
 						}
 						ConfigurationSection validGroups = plugin.permManager.getConfig().getConfigurationSection("groups");
 						Map<String, Object> perms = new HashMap<String, Object>();
 						if(validGroups.contains(gname)){
 							perms = plugin.permManager.viewPerms("groups/"+gname);
 						}
 						Set<String> keys = perms.keySet();
 						String result = "";
 						for(String v:keys){
 							result = result + v + ":" + perms.get(v) + ",  ";
 						}
 						String msg = result;
 						ChatPaginator.ChatPage tpage = ChatPaginator.paginate(msg, page);
 						int total = tpage.getTotalPages();
 						int current = tpage.getPageNumber();
 						String[] lines = tpage.getLines();
 						sender.sendMessage(plugin.colors.getTp()+"Page number: [" +current+"/"+total+"]");
 						sender.sendMessage(plugin.colors.getTitle() + "Permissions for " + gname + " = ");
 						for(int i=0;i<lines.length;i++){
 							sender.sendMessage(plugin.colors.getInfo() + ChatColor.stripColor(lines[i]));
 						}
 					      }
 					}
 					else{
 						valid = false;
 					}
 					if(!valid){
 						sender.sendMessage("Usage: /" + cmdname + " <Perms> <Group> [<SetPerm> <Name> <Perm> <Value>], [<UnsetPerm> <Name> <Perm>], [<Create> <Name> <Inheritance>], [<Delete> <Name>], [<List>], [<View> <Group> <Page>]");
 						return true;
 					}
 				}
 				else if(args[1].equalsIgnoreCase("player")){
 					boolean valid = true;
 					//TODO player perms management
 					String usage = "Usage: /" + cmdname + " <Perms> <Player> [<Setgroups> <Player> <Groups>], [<Setperm> <Player> <Node> <value>], [<Unsetperm> <Player> <Node>], [<View> <Player>], [<viewPersonalPerms> <Player> (Page)], [<ViewAllPerms> <Player> (Page)]";
 					if(args.length < 3){
 						sender.sendMessage(usage);
 						return true;
 					}
 					String action = args[2];
 					if(action.equalsIgnoreCase("setgroups")){
 						if(args.length < 5){
 							sender.sendMessage(usage);
 							return true;
 						}
 						List<String> groups = new ArrayList<String>();
 						for(int i=4;i<args.length;i++){
 							groups.add(args[i]);
 						}
 						String playerName = args[3];
 						if(plugin.getServer().getOfflinePlayer(playerName)!= null){
 							playerName = plugin.getServer().getOfflinePlayer(playerName).getName();
 						}
 						for(int i=0;i<groups.size();i++){
 							String Group = groups.get(i);
 							Group = plugin.permManager.groupExists(Group);
 							if(Group == "^^error^^"){
 								sender.sendMessage(plugin.colors.getError() + "Group "+groups.get(i)+" doesn't exist!");
 								return true;
 							}
 						}
 						plugin.permManager.setGroups(playerName, groups);
 						sender.sendMessage(plugin.colors.getSuccess() + playerName + " is now in groups "+ groups);
 					}
 					else if(action.equalsIgnoreCase("setperm")){
 						String gname = "";
 						if(args.length < 6){
 							valid = false;
 						}
 						if(valid){
 						gname = args[3];
 						String gperm = args[4];
 						String gval = args[5];
 						Boolean Val;
 						try {
 							Val = Boolean.parseBoolean(gval);
 						} catch (Exception e) {
 							sender.sendMessage(plugin.colors.getError() + "Value must be true or false!");
 							return true;
 						}
 						OfflinePlayer[] allPlay = plugin.getServer().getOfflinePlayers();
 						for(int i=0;i<allPlay.length;i++){
 							OfflinePlayer play = allPlay[i];
 							if(play.getName().equalsIgnoreCase(gname)){
 								gname = play.getName();
 							}
 						}
 						String path = "users/"+gname+"/permissions";
 						gperm = gperm.replaceAll(":", "");
 						plugin.permManager.setPerm(path, gperm, Val);
 						}
 						if(valid){
 						sender.sendMessage(plugin.colors.getSuccess() + "Successfully set perm for "+gname+"!");
 						}
 					}
 					else if(action.equalsIgnoreCase("unsetperm")){
 						String gname = "";
 						if(args.length < 5){
 							valid = false;
 						}
 						if(valid){
 						gname = args[3];
 						String gperm = args[4];
 						OfflinePlayer[] allPlay = plugin.getServer().getOfflinePlayers();
 						for(int i=0;i<allPlay.length;i++){
 							OfflinePlayer play = allPlay[i];
 							if(play.getName().equalsIgnoreCase(gname)){
 								gname = play.getName();
 							}
 						}
 						String path = "users/"+gname+"/permissions";
 						plugin.permManager.setPerm(path, gperm, null);
 						}
 						if(valid){
 						sender.sendMessage(plugin.colors.getSuccess() + "Successfully unset perm for "+gname+"!");
 						}
 					}
 					else if(action.equalsIgnoreCase("view")){
 						if(args.length < 4){
 							valid = false;
 						}
 						if(valid){
 						String pname = args[3];
 						OfflinePlayer[] allPlay = plugin.getServer().getOfflinePlayers();
 						for(int i=0;i<allPlay.length;i++){
 							OfflinePlayer play = allPlay[i];
 							if(play.getName().equalsIgnoreCase(pname)){
 								pname = play.getName();
 							}
 						}
 						//TODO
 						if(plugin.permManager.getConfig().contains("users/"+pname+"/groups")){
 						List<String> list = plugin.permManager.getConfig().getStringList("users/"+pname+"/groups");
 						if(list.size() < 1){
 							sender.sendMessage(plugin.colors.getError() + pname + " is not in a group");
 							return true;
 						}
 						String toSend = list.get(0);
 						for(int i=1;i<list.size();i++){
 							String groupName = list.get(i);
 							toSend = toSend + ", " + groupName;
 						}
 						sender.sendMessage(plugin.colors.getTitle() + pname+" is in the groups: " + plugin.colors.getInfo() + toSend);
 						return true;
 						}
 						else{
 							sender.sendMessage(plugin.colors.getError() + pname + " is not in a group");
 							return true;
 						}
 						}
 					}
 					else if(action.equalsIgnoreCase("viewPersonalPerms")){
 						if(args.length < 4){
 							valid = false;
 						}
 						int page = 0;
 						if(args.length < 5){
 							page = 1;
 						}
 						else {
 						String pagRaw = args[4];
 						try {
 							page = Integer.parseInt(pagRaw);
 						} catch (NumberFormatException e) {
 							sender.sendMessage(plugin.colors.getError() + "Page number invalid");
 							return true;
 						}
 						}
 						String pname = args[3];
 						OfflinePlayer[] allPlay = plugin.getServer().getOfflinePlayers();
 						for(int i=0;i<allPlay.length;i++){
 							OfflinePlayer play = allPlay[i];
 							if(play.getName().equalsIgnoreCase(pname)){
 								pname = play.getName();
 							}
 						}
 						String personalPath = "users/"+pname+"/permissions";
 						if(!plugin.permManager.getConfig().getConfigurationSection("users").getKeys(false).contains(pname)){
 							sender.sendMessage(plugin.colors.getInfo() + pname + " does not have any personal permissions set");
 							return true;
 						}
 						else if(!plugin.permManager.getConfig().getConfigurationSection("users/" + pname).getKeys(false).contains("permissions")){
 							sender.sendMessage(plugin.colors.getInfo() + pname + " does not have any personal permissions set");
 							return true;
 						}
 						ConfigurationSection perms = plugin.permManager.getConfig().getConfigurationSection(personalPath);
 					    Set<String> permsKeys = perms.getKeys(false);
 					    String permsToSend = "";
 					    for(String key:permsKeys){
 					    	sender.sendMessage(".");
 					    	boolean value = false;
 					    	try {
 								value = perms.getBoolean(key);
 							} catch (Exception e) {
 								value = true;
 							}
 					    	permsToSend = permsToSend + key + " : " + value + ", ";
 					    }
 					    //TODO
 					    ChatPage toSend = ChatPaginator.paginate(permsToSend, page);
 					    int total = toSend.getTotalPages();
 					    sender.sendMessage(plugin.colors.getTp() + "Page: ["+toSend.getPageNumber()+"/"+total+"]");
 					    String[] lines = toSend.getLines();
 					    sender.sendMessage(plugin.colors.getTitle() + "Permissions for " + pname + ":");
 					    for(int i=0;i<lines.length;i++){
 					    	sender.sendMessage(plugin.colors.getInfo() + lines[i]);
 					    }
 					    return true;
 					}
 					else if(action.equalsIgnoreCase("viewAllPerms")){
 						if(args.length < 4){
 							valid = false;
 						}
 						int page = 0;
 						if(args.length < 5){
 							page = 1;
 						}
 						else {
 						String pagRaw = args[4];
 						try {
 							page = Integer.parseInt(pagRaw);
 						} catch (NumberFormatException e) {
 							sender.sendMessage(plugin.colors.getError() + "Page number invalid");
 							return true;
 						}
 						}
 						String pname = args[3];
 						OfflinePlayer[] allPlay = plugin.getServer().getOfflinePlayers();
 						for(int i=0;i<allPlay.length;i++){
 							OfflinePlayer play = allPlay[i];
 							if(play.getName().equalsIgnoreCase(pname)){
 								pname = play.getName();
 							}
 						}
 						String personalPath = "users/"+pname;
 						if(!plugin.permManager.getConfig().getConfigurationSection("users").getKeys(false).contains(pname)){
 							sender.sendMessage(plugin.colors.getInfo() + pname + " does not have any permissions set");
 							return true;
 						}
 						Map<String, Object> perms = plugin.permManager.viewPerms(personalPath);
 						if(plugin.permManager.getConfig().getConfigurationSection("users/"+pname).getKeys(false).contains("groups")){
 							List<String> allGroups = plugin.permManager.getConfig().getStringList("users/"+pname+"/groups");
 							for(String gname:allGroups){
 								gname = plugin.permManager.groupExists(gname);
 								if(gname != "^^error^^"){
 									Map<String, Object> permsToAdd = plugin.permManager.viewPerms("groups/"+gname);
 									Set<String> keysPerms = permsToAdd.keySet();
 									for(String keyPerm:keysPerms){
 										perms.put(keyPerm, permsToAdd.get(keyPerm));
 									}
 								}
 							}
 						}
 					    Set<String> permsKeys = perms.keySet();
 					    String permsToSend = "";
 					    for(String key:permsKeys){
 					    	sender.sendMessage(".");
 					    	Object value = false;
 					    	try {
 								value = perms.get(key);
 							} catch (Exception e) {
 								value = true;
 							}
 					    	permsToSend = plugin.colors.getInfo() + permsToSend + plugin.colors.getInfo() + key + " : " + value + ", ";
 					    }
 					    //TODO
 					    ChatPage toSend = ChatPaginator.paginate(permsToSend, page);
 					    int total = toSend.getTotalPages();
 					    sender.sendMessage(plugin.colors.getTp() + "Page: ["+toSend.getPageNumber()+"/"+total+"]");
 					    String[] lines = toSend.getLines();
 					    sender.sendMessage(plugin.colors.getTitle() + "All permissions for " + pname + ":");
 					    for(int i=0;i<lines.length;i++){
 					    	sender.sendMessage(plugin.colors.getInfo() + lines[i]);
 					    }
 					    return true;
 					}
 					else{
 						sender.sendMessage(usage);
 						return true;
 					}
 					if(!valid){
 						sender.sendMessage(usage);
 						return true;
 					}
 				}
 				else{
 					sender.sendMessage("Usage: /" + cmdname + " perms [Group/Player]");
 					return true;
 				}
 				return true;
 			}
 			else{
 				return false;
 			}
 	}	
 		//If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
 		return false; 
 		}
 	Server getServer() {
 		return plugin.getServer();
 	}
 	ColoredLogger getLogger() {
 		return plugin.colLogger;
 	}
 }
