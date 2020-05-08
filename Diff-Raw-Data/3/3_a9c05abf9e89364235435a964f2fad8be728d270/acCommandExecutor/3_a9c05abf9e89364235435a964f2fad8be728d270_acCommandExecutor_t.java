 package com.amazar.plugin;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.RoundingMode;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Dispenser;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.scoreboard.Team;
 import org.bukkit.util.ChatPaginator;
 import org.bukkit.util.ChatPaginator.ChatPage;
 
 import com.amazar.utils.Arena;
 import com.amazar.utils.ArenaCtf;
 import com.amazar.utils.ArenaPush;
 import com.amazar.utils.ArenaPvp;
 import com.amazar.utils.ArenaShape;
 import com.amazar.utils.ArenaSurvival;
 import com.amazar.utils.ArenaTeams;
 import com.amazar.utils.ArenaTntori;
 import com.amazar.utils.ArenaType;
 import com.amazar.utils.ListStore;
 import com.amazar.utils.Minigame;
 import com.amazar.utils.Profile;
 import com.amazar.utils.StringColors;
 import com.amazar.utils.UCarsArena;
 import com.amazar.utils.getColor;
 import com.amazar.utils.getItemMinigame;
 import com.useful.ucars.ClosestFace;
 
 public class acCommandExecutor implements CommandExecutor {
 private ac plugin;
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
 		else if(cmd.getName().equalsIgnoreCase("games")){
 			if(player == null){
 				return true;
 			}
 			BlockState orig = new Location(player.getWorld(), 1, 1, 1).getBlock().getState();
 			Block toSet = new Location(player.getWorld(), 1, 1, 1).getBlock();
 			toSet.setType(Material.CHEST);
 			Chest holder = (Chest) toSet.getState();
 			holder.getInventory().clear();
 			ItemStack CTF = getItemMinigame.getItem(ChatColor.RED+"CTF", ChatColor.GOLD+"Collect the flag!", Material.WOOL, (short) 14);
 			ItemStack PUSH = getItemMinigame.getItem(ChatColor.RED+"PUSH", ChatColor.GOLD+"Push the others out!", Material.PISTON_BASE, (short) 0);
 			ItemStack PVP = getItemMinigame.getItem(ChatColor.RED+"PVP", ChatColor.GOLD+"Player vs Player!", Material.DIAMOND_SWORD, (short) 0);
 			ItemStack SURVIVAL = getItemMinigame.getItem(ChatColor.RED+"SURVIVAL", ChatColor.GOLD+"Survive the monsters!", Material.SKULL_ITEM, (short) 4);
 			ItemStack TEAMS = getItemMinigame.getItem(ChatColor.RED+"TEAMS", ChatColor.GOLD+"Team vs Team!", Material.SKULL_ITEM, (short) 3);
 			ItemStack TNTORI = getItemMinigame.getItem(ChatColor.RED+"TNTORI", ChatColor.GOLD+"Knock others off the platform with tnt!", Material.TNT, (short) 0);
 			ItemStack UCARS = getItemMinigame.getItem(ChatColor.RED+"UCARS", ChatColor.GOLD+"Race around the track!", Material.MINECART, (short) 0);
 			ItemStack INFO = getItemMinigame.getItem(ChatColor.RED+"MORE INFO", ChatColor.GOLD+"Click for more game info!", Material.APPLE, (short) 0);
 			Inventory hinv = holder.getInventory();
 			hinv.setItem(1, CTF);
 			hinv.setItem(2, PUSH);
 			hinv.setItem(3, PVP);
 			hinv.setItem(4, SURVIVAL);
 			hinv.setItem(5, TEAMS);
 			hinv.setItem(6, TNTORI);
 			hinv.setItem(7, UCARS);
 			hinv.setItem(0, INFO);
 			player.openInventory(hinv);
 			holder.getBlock().getDrops().clear();
 			orig.update(true);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("minigame")){
 			if(player == null){
 				sender.sendMessage(ChatColor.RED+"Players only!");
 				return true;
 			}
 			if(args.length < 1){
 				return false;
 			}
 			String action = args[0];
 			if(action.equalsIgnoreCase("leave")){
 				Minigame game = plugin.mgMethods.inAGame(player.getName());
 				if(game == null){
 					String arenaName = plugin.mgMethods.inGameQue(player.getName());
 					if(arenaName == null){
 						sender.sendMessage(ChatColor.RED+"Not in a game or game que!");
 						return true;
 					}
 					Arena arena = plugin.minigamesArenas.getArena(arenaName);
 					arena.removePlayer(player.getName());
 					plugin.minigamesArenas.setArena(arenaName, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully left game que!");
 					return true;
 				}
 				else{
 					game.leave(player.getName());
 					return true;
 			    }
 			}
 			else if(action.equalsIgnoreCase("list")){
 				if(args.length < 2){
 					return false;
 				}
 				String game = args[1];
 				int page = 1;
 				if(args.length > 2){
 					try {
 						page = Integer.parseInt(args[2]);
 					} catch (NumberFormatException e) {
 sender.sendMessage(ChatColor.RED+"Invalid page number!");
 return true;
 					}
 				}
 				ArenaType type = ArenaType.INAVLID;
 				if(game.equalsIgnoreCase("ctf")){
 					type = ArenaType.CTF;
 				}
 				else if(game.equalsIgnoreCase("push")){
 					type = ArenaType.PUSH;
 				}
 				else if(game.equalsIgnoreCase("pvp")){
 					type = ArenaType.PVP;
 				}
 				else if(game.equalsIgnoreCase("survival")){
 					type = ArenaType.SURVIVAL;
 				}
 				else if(game.equalsIgnoreCase("teams")){
 					type = ArenaType.TEAMS;
 				}
 				else if(game.equalsIgnoreCase("tntori")){
 					type = ArenaType.TNTORI;
 				}
 				else if(game.equalsIgnoreCase("ucars")){
 					type = ArenaType.UCARS;
 				}
 				else{
 					sender.sendMessage(ChatColor.RED+"Invalid minigame. Please do /"+cmd.getLabel()+" games for a list of valid minigames!");
 					return true;
 				}
 				List<String> gameArenas = new ArrayList<String>();
 				for(String aname:plugin.minigamesArenas.getArenas()){
 					Arena arena = plugin.minigamesArenas.getArena(aname);
 					if(arena.getType() == type){
 						gameArenas.add(aname);
 					}
 				}
 				List<String> arenaInfo = new ArrayList<String>();
 				for(String name:gameArenas){
 					Arena arena = plugin.minigamesArenas.getArena(name);
 					String toAdd = ChatColor.BLUE + "["+name+":] "+ChatColor.GOLD+"Type: "+ChatColor.RED+arena.getType().toString().toLowerCase() + ChatColor.GOLD+"  Players: "+ChatColor.RED+"["+arena.getHowManyPlayers()+"/"+arena.getPlayerLimit()+"]";
 					if(!arena.isValid()){
 						toAdd = toAdd + ChatColor.GRAY + " -Invalid Arena (Not setup yet!)";
 					}
 					arenaInfo.add(toAdd);
 				}
 				int displayed = 0;
 				double totalPagesunrounded = arenaInfo.size() / 5d;
 				NumberFormat fmt = NumberFormat.getNumberInstance();
 				fmt.setMaximumFractionDigits(0);
 				fmt.setRoundingMode(RoundingMode.UP);
 				String value = fmt.format(totalPagesunrounded);
 				int totalPages = Integer.parseInt(value);
 				if(totalPages == 0){
 					totalPages = 1;
 				}
 				page -= 1;
 				if(page > totalPages){
 					page = totalPages;
 				}
 				
 				if(page < 0){
 					page = 0;
 				}
 				int startpoint = page * 5;
 				sender.sendMessage(ChatColor.DARK_RED+"Arenas for "+type.toString().toLowerCase()+": Page: ["+(page+1)+"/"+(totalPages)+"]");
 				for(int i=startpoint;i<arenaInfo.size()&&displayed<5;i++){
 					sender.sendMessage(StringColors.colorise(arenaInfo.get(i)));
 					displayed++;
 				}
 				return true;
 			}
 			else if(action.equalsIgnoreCase("games")){
 				sender.sendMessage(ChatColor.DARK_RED+"Available minigames:");
 				sender.sendMessage(ChatColor.RED+"CTF "+ChatColor.GOLD+"- 2 teams must race to collect their flags and do all they can to stop the other team from doing so first!");
 				sender.sendMessage(ChatColor.RED+"PUSH "+ChatColor.GOLD+"- The players must try to push each other out of the arena. Last person standing wins!");
 				sender.sendMessage(ChatColor.RED+"PVP "+ChatColor.GOLD+"- The players must try to kill each other until one remains and they win!");
 				sender.sendMessage(ChatColor.RED+"SURVIVAL "+ChatColor.GOLD+"- The players must work together to survive attacking mobs until the countdown ends. Players alive at the end win!");
 				sender.sendMessage(ChatColor.RED+"TEAMS "+ChatColor.GOLD+"- The blue team must fight the red team. The winning team wins!");
 				sender.sendMessage(ChatColor.RED+"TNTORI "+ChatColor.GOLD+"- The players must push each other out of the arena using the blast force of tnt!");
 				sender.sendMessage(ChatColor.RED+"UCARS "+ChatColor.GOLD+"- Race around the racetrack!");
 				sender.sendMessage(ChatColor.GOLD+"Now do /games to get started!");
 				return true;
 			}
 			else if(action.equalsIgnoreCase("join")){
 				//TODO Joining of game ques
 				if(args.length < 3){
 					return false;
 				}
 				String game = args[1];
 				String arenaName = args[2];
 				if(plugin.mgMethods.inAGame(player.getName()) != null){
 					sender.sendMessage(ChatColor.RED+"Already in a minigame! Please leave it before joining another!");
 					return true;
 				}
 				if(plugin.mgMethods.inGameQue(player.getName()) != null){
 					sender.sendMessage(ChatColor.RED+"Already in a minigame que! Please leave it before joining another!");
 					return true;
 				}
 				ArenaType type = ArenaType.INAVLID;
 				if(game.equalsIgnoreCase("ctf")){
 					type = ArenaType.CTF;
 				}
 				else if(game.equalsIgnoreCase("push")){
 					type = ArenaType.PUSH;
 				}
 				else if(game.equalsIgnoreCase("pvp")){
 					type = ArenaType.PVP;
 				}
 				else if(game.equalsIgnoreCase("survival")){
 					type = ArenaType.SURVIVAL;
 				}
 				else if(game.equalsIgnoreCase("teams")){
 					type = ArenaType.TEAMS;
 				}
 				else if(game.equalsIgnoreCase("tntori")){
 					type = ArenaType.TNTORI;
 				}
 				else if(game.equalsIgnoreCase("ucars")){
 					type = ArenaType.UCARS;
 				}
 				else{
 					sender.sendMessage(ChatColor.RED+"Invalid minigame. Please do /"+cmd.getLabel()+" games for a list of valid minigames!");
 					return true;
 				}
 				if(arenaName.equalsIgnoreCase("auto")){
 					List<String> gameArenas = new ArrayList<String>();
 					List<String> order = new ArrayList<String>();
 					int waitingPlayers = 0;
 					for(String aname:plugin.minigamesArenas.getArenas()){
 						Arena arena = plugin.minigamesArenas.getArena(aname);
 						if(arena.getType() == type && arena.getHowManyPlayers() < arena.getPlayerLimit()){
 							gameArenas.add(aname);
 							if(arena.getHowManyPlayers() > waitingPlayers){
 								waitingPlayers = arena.getHowManyPlayers();
 							}
 						}
 					}
 					int waitNo = 1;
 					List<String> remaining = new ArrayList<String>();
 					remaining.addAll(gameArenas);
 					for(int i=waitNo;i<=waitingPlayers;i++){
 						for(String aname:gameArenas){
 							Arena arena = plugin.minigamesArenas.getArena(aname);
 							if(arena.getHowManyPlayers() == waitNo){
 								order.add(aname);
 								if(remaining.contains(aname)){
 								remaining.remove(aname);
 								}
 							}
 						}
 					}
 					for(String aname:remaining){
 						order.add(aname);
 					}
 					if(order.size() < 1){
 						sender.sendMessage(ChatColor.RED+"No arenas found!");
 						return true;
 					}
 					String name = order.get(0);
 					Arena arena = plugin.minigamesArenas.getArena(name);
 					if(arena.getHowManyPlayers() < 1){
 						int rand = 0 + (int)(Math.random() * ((order.size() - 0) + 0));
 						name = order.get(rand);
 						arena = plugin.minigamesArenas.getArena(name);
 					}
 					plugin.gameScheduler.joinGame(player.getName(), arena, name);
 					return true;
 				}
 				List<String> gameArenas = new ArrayList<String>();
 				for(String aname:plugin.minigamesArenas.getArenas()){
 					Arena arena = plugin.minigamesArenas.getArena(aname);
 					if(arena.getType() == type){
 						gameArenas.add(aname);
 					}
 				}
 				Boolean arenaExists = false;
 				for(String aname:gameArenas){
 					if(aname.equalsIgnoreCase(arenaName)){
 						arenaExists = true;
 						arenaName = aname;
 					}
 				}
 				if(!arenaExists){
 					sender.sendMessage(ChatColor.RED+"Arena doesn't exist! Do /"+cmd.getLabel()+" list "+type.toString().toLowerCase()+" for a valid list of "+type.toString().toLowerCase()+" arenas.");
 					return true;
 				}
 				Arena arena = plugin.minigamesArenas.getArena(arenaName);
 				if(!(arena.getHowManyPlayers() < arena.getPlayerLimit())){
 					sender.sendMessage(ChatColor.RED+"Arena game que full! Please try another arena or again later!");
 				}
 				plugin.gameScheduler.joinGame(player.getName(), arena, arenaName);
 				return true;
 			}
 			return false;
 		}
 		else if(cmd.getName().equalsIgnoreCase("test")){
 			Set<Team> teams = plugin.getServer().getScoreboardManager().getMainScoreboard().getTeams();
 			for(Team team:teams){
 				if(team.getName().startsWith("red") || team.getName().startsWith("blue")){
 					team.unregister();
 				}
 			}
 			sender.sendMessage(ChatColor.GREEN+"Successfully purged minigame teams left hanging!");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("arena")){
 			if(args.length < 1){
 				return false;
 			}
 			String method = args[0];
 			if(method.equalsIgnoreCase("create")){
 				if(player == null){
 					sender.sendMessage(ChatColor.RED + "Only players can create arenas!");
 					return true;
 				}
 				if(args.length < 6){
 					return false;
 				}
 				String name = args[1];
 				String type = args[2];
 				String shapeName = args[3];
 				ArenaShape shape = null;
 				if(shapeName.equalsIgnoreCase("square")){
 					shape = ArenaShape.SQUARE;
 				}
 				else if(shapeName.equalsIgnoreCase("circle")){
 					shape = ArenaShape.CIRCLE;
 				}
 				else{
 					sender.sendMessage(ChatColor.RED + "Invalid shape. Valid: square, circle");
 					return true;
 				}
 				String radiusRaw = args[4];
 				int radius = 1;
 				try {
 					radius = Integer.parseInt(radiusRaw);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(ChatColor.RED+"Invalid radius!");
 					return true;
 				}
 				String playerlimitRaw = args[5];
 				int playerlimit = 1;
 				try {
 					playerlimit = Integer.parseInt(playerlimitRaw);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(ChatColor.RED+"Invalid player limit!");
 					return true;
 				}
 				if(radius < 1 || playerlimit < 1){
 					sender.sendMessage(ChatColor.RED + "Radius and player limit must be at least 1!");
 					return true;
 				}
 				if(type.equalsIgnoreCase("pvp")){
 					ArenaPvp arena = new ArenaPvp(player.getLocation(), radius, shape, ArenaType.PVP, null, null, 0, null, playerlimit);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a pvp arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else if(type.equalsIgnoreCase("tntori")){
 					ArenaTntori arena = new ArenaTntori(player.getLocation(), radius, shape, ArenaType.TNTORI, null, null, 0, null, true, 50, false, playerlimit);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a tntori arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else if(type.equalsIgnoreCase("survival")){
 					ArenaSurvival arena = new ArenaSurvival(player.getLocation(), radius, shape, ArenaType.SURVIVAL, 500, true, null, null, null, playerlimit);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a survival arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else if(type.equalsIgnoreCase("ctf")){
 					ArenaCtf arena = new ArenaCtf(player.getLocation(), radius, shape, ArenaType.CTF, null, null, null, null, playerlimit, null);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a ctf arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else if(type.equalsIgnoreCase("team")||type.equalsIgnoreCase("teams")){
 					ArenaTeams arena = new ArenaTeams(player.getLocation(), radius, shape, ArenaType.TEAMS, null, null, playerlimit, null);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a teams arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else if(type.equalsIgnoreCase("push")){
 					ArenaPush arena = new ArenaPush(player.getLocation(), radius, shape, ArenaType.PUSH, null, null, 2, null, 20, false, playerlimit);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a push arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else if(type.equalsIgnoreCase("ucars")){
 					UCarsArena arena = new UCarsArena(player.getLocation(), radius, shape, ArenaType.UCARS, playerlimit);
 				    if(plugin.minigamesArenas.arenaExists(name)){
 				    	sender.sendMessage(ChatColor.RED+"Arena already exists!");
 				    	return true;
 				    }
 				    plugin.minigamesArenas.setArena(name, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully created a ucars arena! It will need setting up with /arena set");
 				    return true;
 				}
 				else{
 					sender.sendMessage(ChatColor.RED + "Invalid type! Valid ones: Pvp, Tntori, Survival, CTF, Teams, Push, UCars");
 					return true;
 				}
 			}
 			else if(method.equalsIgnoreCase("remove")){
 				if(args.length < 2){
 					return false;
 				}
 				String arenaName = args[1];
 				if(!plugin.minigamesArenas.arenaExists(arenaName)){
 					sender.sendMessage(ChatColor.RED+"Arena doesn't exist!");
 					return true;
 				}
 				plugin.minigamesArenas.removeArena(arenaName);
 				sender.sendMessage(ChatColor.GREEN+"Successfully removed the arena!");
 				return true;
 			}
 			else if(method.equalsIgnoreCase("set")){
 				if(player == null){
 					sender.sendMessage(ChatColor.RED+"Players only.");
 					return true;
 				}
 				if(args.length < 3){
 					return false;
 				}
 				String arenaName = args[1];
 				String setting = args[2];
 				if(!plugin.minigamesArenas.arenaExists(arenaName)){
 					sender.sendMessage(ChatColor.RED+"Arena doesn't exist!");
 					return true;
 				}
 				//TODO code for setup and modification fo arenas
 				Arena arena = plugin.minigamesArenas.getArena(arenaName);
 				if(setting.equalsIgnoreCase("setCenter")){
 					arena.setCenter(player.getLocation());
 					plugin.minigamesArenas.setArena(arenaName, arena);
 					sender.sendMessage(ChatColor.GREEN+"Arena center set to your feet!");
 					return true;
 				}
 				else if(setting.equalsIgnoreCase("setRadius")){
 					if(args.length < 4){
 						sender.sendMessage(ChatColor.GOLD+"Usage: /arena set [Name] setRadius [Radius]");
 						return true;
 					}
 					String radius = args[3];
 					int radi = 0;
 					try {
 						radi = Integer.parseInt(radius);
 					} catch (NumberFormatException e) {
 						sender.sendMessage(ChatColor.RED+"Invalid radius");
 						return true;
 					}
 					arena.setRadius(radi);
 					plugin.minigamesArenas.setArena(arenaName, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully set the radius for this arena.");
 					return true;
 				}
 				else if(setting.equalsIgnoreCase("show")){
 					arena.markArena(Material.SANDSTONE);
 					sender.sendMessage(ChatColor.GREEN+"Arena zone marked out with sandstone.");
 					return true;
 				}
 				else if(setting.equalsIgnoreCase("setShape")){
 					if(args.length < 4){
 						sender.sendMessage(ChatColor.GOLD+"Usage: /arena set [Name] setShape [square/circle]");
 						return true;
 					}
 					String shapeName = args[3];
 					ArenaShape shape = ArenaShape.INVALID;
 					if(shapeName.equalsIgnoreCase("circle")){
 						shape = ArenaShape.CIRCLE;
 					}
 					else if(shapeName.equalsIgnoreCase("square")){
 						shape = ArenaShape.SQUARE;
 					}
 					else{
 						sender.sendMessage(ChatColor.RED+"Invalid shape! Valid: circle, square!");
 						return true;
 					}
 					arena.setShape(shape);
 					plugin.minigamesArenas.setArena(shapeName, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully set the arena shape!");
 					return true;
 				}
 				else if(setting.equalsIgnoreCase("setType")){
 					if(args.length < 4){
 						sender.sendMessage(ChatColor.GOLD+"Usage: /arena set [Name] setType [Type]");
 						return true;
 					}
 					String typeName = args[3];
 					ArenaType type = ArenaType.INAVLID;
 					if(typeName.equalsIgnoreCase("pvp")){
 						type = ArenaType.PVP;
 					}
 					else if(typeName.equalsIgnoreCase("ctf")){
 						type = ArenaType.CTF;
 					}
 					else if(typeName.equalsIgnoreCase("push")){
 						type = ArenaType.PUSH;
 					}
 					else if(typeName.equalsIgnoreCase("survival")){
 						type = ArenaType.SURVIVAL;
 					}
 					else if(typeName.equalsIgnoreCase("teams")){
 						type = ArenaType.TEAMS;
 					}
 					else if(typeName.equalsIgnoreCase("tntori")){
 						type = ArenaType.TNTORI;
 					}
 					else if(typeName.equalsIgnoreCase("ucars")){
 						type = ArenaType.UCARS;
 					}
 					else{
 						sender.sendMessage(ChatColor.RED+"Invalid type! Valid: ctf, pvp, push, survival, teams, tntori, ucars");
 						return true;
 					}
 					arena.setType(type);
 					plugin.minigamesArenas.setArena(arenaName, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully set arena type!");
 					return true;
 				}
 				else if(setting.equalsIgnoreCase("inArena")){
 					Boolean inArena = arena.isLocInArena(player.getLocation());
 					sender.sendMessage(ChatColor.GREEN + "You are in the arena: "+inArena);
 					return true;
 				}
 				else if(setting.equalsIgnoreCase("setPlayerLimit")){
 					if(args.length < 4){
 						sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setPlayerLimit [Limit]");
 						return true;
 					}
 					int limit = 2;
 					try {
 						limit = Integer.parseInt(args[3]);
 					} catch (NumberFormatException e) {
                     sender.sendMessage(ChatColor.RED+"Limit must be a number!");
                     return true;
 					}
 					arena.setPlayerLimit(limit);
 					plugin.minigamesArenas.setArena(arenaName, arena);
 					sender.sendMessage(ChatColor.GREEN+"Successfully set player limit!");
 					return true;
 				}
 				if(arena.getType() == ArenaType.CTF){
 					ArenaCtf ctf = (ArenaCtf) arena;
 					if(setting.equalsIgnoreCase("setItems")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set setItems id:data id:data etc..");
 							return true;
 						}
 						List<String> raws = new ArrayList<String>();
 						for(int i=3;i<args.length;i++){
 							raws.add(args[i]);
 						}
 						Object[] array = (Object[]) raws.toArray();
 						ctf.setItems((String[]) array);
 						plugin.minigamesArenas.setArena(arenaName, ctf);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set items given to players!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setBlueSpawn")){
 						ctf.setBlueSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, ctf);
 						sender.sendMessage(ChatColor.GREEN+"Set blue team spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setRedSpawn")){
 						ctf.setRedSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, ctf);
 						sender.sendMessage(ChatColor.GREEN+"Set the red team spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setBlueFlag")){
 						ctf.setBlueFlag(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, ctf);
 						sender.sendMessage(ChatColor.GREEN+"The the blue team flag has been set to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setRedFlag")){
 						ctf.setRedFlag(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, ctf);
 						sender.sendMessage(ChatColor.GREEN+"The red team flag has been set to where you are standing!");
 						return true;
 					}
 				}
 				else if(arena.getType() == ArenaType.UCARS){
 					UCarsArena gameArena = (UCarsArena) arena;
 					if(setting.equalsIgnoreCase("setGridLocation")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setGridLocation [Number]");
 							return true;
 						}
 						int gridPos = 0;
 						try {
 							gridPos = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setGridLocation [Number]");
 return true;
 						}
 						Location gridLoc = player.getLocation();
 						if(!gameArena.validateGridLocationSetRequest(gridPos)){
 							sender.sendMessage(ChatColor.RED+"Set the earlier grid positions first!");
 							return true;
 						}
 						gameArena.setGridPosition(gridPos, gridLoc);
 						gameArena.check();
 						plugin.minigamesArenas.setArena(arenaName, gameArena);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set grid location: "+args[3]);
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setLineLocation")){
 						if(args.length < 5){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLineLocation [Direction (N/E/S/W)] [Amount]");
 							return true;
 						}
 						String direction = args[3];
 						BlockFace dir = BlockFace.NORTH;
 						int amount = 0;
 						try {
 							amount = Integer.parseInt(args[4]);
 						} catch (Exception e) {
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLineLocation [Direction (N/E/S/W)] [Amount]");
 							return true;
 						}
 						if(amount < 1){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLineLocation [Direction (N/E/S/W)] [Amount]");
 							return true;
 						}
 						if(direction.equalsIgnoreCase("n")){
 							dir = BlockFace.NORTH;
 						}
 						else if(direction.equalsIgnoreCase("e")){
 							dir = BlockFace.EAST;
 						}
 						else if(direction.equalsIgnoreCase("s")){
 							dir = BlockFace.SOUTH;
 						}
 						else if(direction.equalsIgnoreCase("w")){
 							dir = BlockFace.WEST;
 						}
 						else{
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLineLocation [Direction (N/E/S/W)] [Amount]");
 							return true;
 						}
 						List<Location> line = new ArrayList<Location>();
 						Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 						line.add(block.getLocation());
 						for(int i=1;i<=amount;i++){
 							Block toAdd = block.getRelative(dir,i);
 							line.add(toAdd.getLocation());
 						}
 						gameArena.setLineLocation(line);
 						plugin.minigamesArenas.setArena(arenaName, gameArena);
 						player.sendMessage(ChatColor.GREEN+"Successfully set line location!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setLaps")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setlaps [Number]");
 							return true;
 						}
 						int laps = 1;
 						try {
 							laps = Integer.parseInt(args[3]);
 						} catch (Exception e) {
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setlaps [Number]");
 							return true;
 						}
 						gameArena.setLaps(laps);
 						plugin.minigamesArenas.setArena(arenaName, gameArena);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set laps!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setItems")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set setItems id:data id:data etc..");
 							return true;
 						}
 						List<String> raws = new ArrayList<String>();
 						for(int i=3;i<args.length;i++){
 							raws.add(args[i]);
 						}
 						Object[] array = (Object[]) raws.toArray();
 						gameArena.setItems((String[]) array);
 						plugin.minigamesArenas.setArena(arenaName, gameArena);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set items given to players!");
 						return true;
 					}
 				}
 				else if(arena.getType() == ArenaType.PUSH){
 					ArenaPush push = (ArenaPush) arena; 
 					if(setting.equalsIgnoreCase("setBlueSpawn")){
 						push.setPlayerBlueSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, push);
 						sender.sendMessage(ChatColor.GREEN+"The blue player spawnpoint has been set to where you are standing!");
 					    return true;
 					}
 					else if(setting.equalsIgnoreCase("setRedSpawn")){
 						push.setPlayerRedSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, push);
 						sender.sendMessage(ChatColor.GREEN+"The red player spawnpoint has been set to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("doCountdown")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] doCountdown [true/false]");
 						}
 						Boolean doCountdown = false;
 						if(args[3].equalsIgnoreCase("true")){
 							doCountdown = true;
 						}
 						else if(args[3].equalsIgnoreCase("false")){
 							doCountdown = false;
 						}
 						else{
 							sender.sendMessage(ChatColor.RED+"Valid values: true/false");
 							return true;
 						}
 						push.setDoCountdown(doCountdown);
 						plugin.minigamesArenas.setArena(arenaName, push);
 						sender.sendMessage(ChatColor.GREEN+"Countdown set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("Countdown")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] countdown [Number]");
 						}
 						int number = 0;
 						try {
 							number = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED+"Invalid number!");
                         return true;
 						}
 						push.setCountdown(number);
 						plugin.minigamesArenas.setArena(arenaName, push);
 						sender.sendMessage(ChatColor.GREEN+"Countdown set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setLives")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLives [Number]");
 						}
 						int number = 0;
 						try {
 							number = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED+"Invalid number!");
                         return true;
 						}
 						push.setLives(number);
 						plugin.minigamesArenas.setArena(arenaName, push);
 						sender.sendMessage(ChatColor.GREEN+"Lives set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setItems")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set setItems id:data id:data etc..");
 							return true;
 						}
 						List<String> raws = new ArrayList<String>();
 						for(int i=3;i<args.length;i++){
 							raws.add(args[i]);
 						}
 						Object[] array = (Object[]) raws.toArray();
 						push.setItems((String[]) array);
 						plugin.minigamesArenas.setArena(arenaName, push);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set items given to players!");
 						return true;
 					}
 					
 				}
 				else if(arena.getType() == ArenaType.TEAMS){
 					ArenaTeams teams = (ArenaTeams) arena;
 					if(setting.equalsIgnoreCase("setItems")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set setItems id:data id:data etc..");
 							return true;
 						}
 						List<String> raws = new ArrayList<String>();
 						for(int i=3;i<args.length;i++){
 							raws.add(args[i]);
 						}
 						Object[] array = (Object[]) raws.toArray();
 						teams.setItems((String[]) array);
 						plugin.minigamesArenas.setArena(arenaName, teams);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set items given to players!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setBlueSpawnpoint")){
 						teams.setBlueSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, teams);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set blue team spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setRedSpawnpoint")){
 						teams.setRedSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, teams);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set red team spawnpoint to where you are standing!");
 						return true;
 					}
 				}
 				else if(arena.getType() == ArenaType.PVP){
 					ArenaPvp pvp = (ArenaPvp) arena;
 					if(setting.equalsIgnoreCase("setRedSpawnpoint")){
 						pvp.setPlayerRedSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, pvp);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set red player spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setBlueSpawnpoint")){
 						pvp.setPlayerBlueSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, pvp);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set blue player spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setLives")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLives [Number]");
 						}
 						int number = 0;
 						try {
 							number = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED+"Invalid number!");
                         return true;
 						}
 						pvp.setLives(number);
 						plugin.minigamesArenas.setArena(arenaName, pvp);
 						sender.sendMessage(ChatColor.GREEN+"Lives set!");
 						return true;
 					}
 				}
 				else if(arena.getType() == ArenaType.SURVIVAL){
 					ArenaSurvival survival = (ArenaSurvival) arena;
 					if(setting.equalsIgnoreCase("doCountdown")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] doCountdown [true/false]");
 						}
 						Boolean doCountdown = false;
 						if(args[3].equalsIgnoreCase("true")){
 							doCountdown = true;
 						}
 						else if(args[3].equalsIgnoreCase("false")){
 							doCountdown = false;
 						}
 						else{
 							sender.sendMessage(ChatColor.RED+"Valid values: true/false");
 							return true;
 						}
 						survival.setDoCountdown(doCountdown);
 						plugin.minigamesArenas.setArena(arenaName, survival);
 						sender.sendMessage(ChatColor.GREEN+"Countdown set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("Countdown")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] countdown [Number]");
 						}
 						int number = 0;
 						try {
 							number = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED+"Invalid number!");
                         return true;
 						}
 						survival.setCountdown(number);
 						plugin.minigamesArenas.setArena(arenaName, survival);
 						sender.sendMessage(ChatColor.GREEN+"Countdown set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setItems")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set setItems id:data id:data etc..");
 							return true;
 						}
 						List<String> raws = new ArrayList<String>();
 						for(int i=3;i<args.length;i++){
 							raws.add(args[i]);
 						}
 						Object[] array = (Object[]) raws.toArray();
 						survival.setItems((String[]) array);
 						plugin.minigamesArenas.setArena(arenaName, survival);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set items given to players!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setPlayerSpawn")){
 						survival.setPlayerSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, survival);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set the player spawn location to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setEnemySpawn")){
 						survival.setEnemySpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, survival);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set the enemy spawn location to where you are standing!");
 						return true;
 					}
 				}
 				else if(arena.getType() == ArenaType.TNTORI){
 					ArenaTntori tntori = (ArenaTntori) arena;
 					if(setting.equalsIgnoreCase("setProtect")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setProtect [true/false]");
 							return true;
 						}
 						Boolean protect = false;
 						if(args[3].equalsIgnoreCase("true")){
 							protect = true;
 						}
 						else if(args[3].equalsIgnoreCase("false")){
 							protect = false;
 						}
 						else{
 							sender.sendMessage(ChatColor.RED+"Valid values: true, false");
 							return true;
 						}
 						tntori.setProtect(protect);
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN + "Successfully set protect!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("doCountdown")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] doCountdown [true/false]");
 						}
 						Boolean doCountdown = false;
 						if(args[3].equalsIgnoreCase("true")){
 							doCountdown = true;
 						}
 						else if(args[3].equalsIgnoreCase("false")){
 							doCountdown = false;
 						}
 						else{
 							sender.sendMessage(ChatColor.RED+"Valid values: true/false");
 							return true;
 						}
 						tntori.setDoCountdown(doCountdown);
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN+"Countdown set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("Countdown")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] countdown [Number]");
 						}
 						int number = 0;
 						try {
 							number = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED+"Invalid number!");
                         return true;
 						}
 						tntori.setCountdown(number);
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN+"Countdown set!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setItems")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set setItems id:data id:data etc..");
 							return true;
 						}
 						List<String> raws = new ArrayList<String>();
 						for(int i=3;i<args.length;i++){
 							raws.add(args[i]);
 						}
 						Object[] array = (Object[]) raws.toArray();
 						tntori.setItems((String[]) array);
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set items given to players!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setRedSpawnpoint")){
 						tntori.setPlayerRedSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set red player spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setBlueSpawnpoint")){
 						tntori.setPlayerBlueSpawn(player.getLocation());
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN+"Successfully set blue player spawnpoint to where you are standing!");
 						return true;
 					}
 					else if(setting.equalsIgnoreCase("setLives")){
 						if(args.length < 4){
 							sender.sendMessage(ChatColor.RED+"Usage: /arena set [Name] setLives [Number]");
 						}
 						int number = 0;
 						try {
 							number = Integer.parseInt(args[3]);
 						} catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED+"Invalid number!");
                         return true;
 						}
 						tntori.setLives(number);
 						plugin.minigamesArenas.setArena(arenaName, tntori);
 						sender.sendMessage(ChatColor.GREEN+"Lives set!");
 						return true;
 					}
 					
 				}
 				sender.sendMessage(ChatColor.DARK_RED+"Valid settings for selected arena:");
 				sender.sendMessage(ChatColor.GOLD+"setCenter - Sets the center of the arena zone.");
 				sender.sendMessage(ChatColor.GOLD+"setRadius [Radius] - Sets the radius of the arena zone.");
 				sender.sendMessage(ChatColor.GOLD+"show - Shows the arena zone.");
 				sender.sendMessage(ChatColor.GOLD+"setShape [circle/square] - Sets arena shape.");
 				sender.sendMessage(ChatColor.GOLD+"setType [Type] - Sets the type of arena.");
 				sender.sendMessage(ChatColor.GOLD+"inArena - Says if you are in the arena zone.");
 				sender.sendMessage(ChatColor.GOLD+"setPlayerLimit [Limit] - Sets the player limit.");
 				if(arena.getType() == ArenaType.CTF){
 					sender.sendMessage(ChatColor.GOLD+"setItems id:data id:data etc... - Sets the items given to the player.");
 					sender.sendMessage(ChatColor.GOLD+"setBlueSpawn - Sets the spawn area for the blue team.");
 					sender.sendMessage(ChatColor.GOLD+"setRedSpawn - Sets the spawn area for the red team.");
 					sender.sendMessage(ChatColor.GOLD+"setBlueFlag - Sets the flag for the blue team to capture.");
 					sender.sendMessage(ChatColor.GOLD+"setRedFlag - Sets the flag for the red team to capture.");
 				}
 				else if(arena.getType() == ArenaType.PUSH){
 					sender.sendMessage(ChatColor.GOLD+"setBlueSpawn - Sets where the blue player spawns.");
 					sender.sendMessage(ChatColor.GOLD+"setRedSpawn - Sets where the red player spawns.");
 					sender.sendMessage(ChatColor.GOLD+"setLives [Lives] - Sets how many lives per player.");
 					sender.sendMessage(ChatColor.GOLD+"setItems id:data id:data etc... - Sets the items given to the player.");
 				}
 				else if(arena.getType() == ArenaType.PVP){
 					sender.sendMessage(ChatColor.GOLD+"setRedSpawnpoint - Sets the spawnpoint for the blue player.");
 					sender.sendMessage(ChatColor.GOLD+"setBlueSpawnpoint - Sets the spawnpoint for the blue player.");
 					sender.sendMessage(ChatColor.GOLD+"setLives [Lives] - Sets how many lives per player.");
 				}
 				else if(arena.getType() == ArenaType.SURVIVAL){
 					sender.sendMessage(ChatColor.GOLD+"doCountdown [true/false] - Sets the countdown on/off.");
 					sender.sendMessage(ChatColor.GOLD+"countdown [Number] - Sets how long the countdown is.");
 					sender.sendMessage(ChatColor.GOLD+"setItems id:data id:data etc... - Sets the items given to the player.");
 					sender.sendMessage(ChatColor.GOLD+"setPlayerSpawn - Sets where the players spawn.");
 					sender.sendMessage(ChatColor.GOLD+"setEnemySpawn - Sets where the enemies spawn.");
 				}
 				else if(arena.getType() == ArenaType.TEAMS){
 					sender.sendMessage(ChatColor.GOLD+"setBlueSpawnpoint - Sets the spawnpoint for the blue team.");
 					sender.sendMessage(ChatColor.GOLD+"setRedSpawnpoint - Sets the spawnpoint for the red team.");
 				}
 				else if(arena.getType() == ArenaType.TNTORI){
 					sender.sendMessage(ChatColor.GOLD+"setProtect [true/false] - Sets if the arena is protected(true) or generated(false).");
 					//sender.sendMessage(ChatColor.GOLD+"doCountdown [true/false] - Sets the countdown on/off.");
 					//sender.sendMessage(ChatColor.GOLD+"countdown [Number] - Sets how long the countdown is.");
 					sender.sendMessage(ChatColor.GOLD+"setLives [Lives] - Sets how many lives per player.");
 					sender.sendMessage(ChatColor.GOLD+"setItems id:data id:data etc... - Sets the items given to the player.");
 					sender.sendMessage(ChatColor.GOLD+"setRedSpawnpoint - Sets the spawnpoint for the blue player.");
 					sender.sendMessage(ChatColor.GOLD+"setBlueSpawnpoint - Sets the spawnpoint for the blue player.");
 				}
 				else if(arena.getType() == ArenaType.UCARS){
 					sender.sendMessage(ChatColor.GOLD+"setGridLocation [Position] - Sets the starting grid location [position].");
 					sender.sendMessage(ChatColor.GOLD+"setLineLocation [Direction (N/E/S/W)] [Distance] - Sets the start/finish line to your location to the block to the [dir] in [dist].");
 					sender.sendMessage(ChatColor.GOLD+"setLaps [Laps] - Sets the number of laps in a race.");
 					sender.sendMessage(ChatColor.GOLD+"setItems id:data id:data etc... - Sets the items given to the player.");
 				}
 					return true;
 				
 			}
 			else if(method.equalsIgnoreCase("view")){
 				if(args.length < 2){
 					return false;
 				}
 				String arenaName = args[1];
 				if(!plugin.minigamesArenas.arenaExists(arenaName)){
 					sender.sendMessage(ChatColor.RED+"Arena doesn't exist!");
 					return true;
 				}
 				Arena arena = plugin.minigamesArenas.getArena(arenaName);
 				sender.sendMessage(ChatColor.RED + "[Type:] "+ChatColor.GOLD+arena.getType().toString().toLowerCase());
 				sender.sendMessage(ChatColor.RED + "[Max players:] "+ChatColor.GOLD+arena.getPlayerLimit());
 				sender.sendMessage(ChatColor.RED + "[Shape:] "+ChatColor.GOLD+arena.getShape().toString().toLowerCase());
 				sender.sendMessage(ChatColor.RED + "[Valid (setup):] "+ChatColor.GOLD+arena.isValid());
 			}
 			else if(method.equalsIgnoreCase("setLobby")){
 				if(args.length < 2){
 					return false;
 				}
 				String typeName = args[1];
 				ArenaType type = ArenaType.INAVLID;
 				if(typeName.equalsIgnoreCase("pvp")){
 					type = ArenaType.PVP;
 				}
 				else if(typeName.equalsIgnoreCase("ctf")){
 					type = ArenaType.CTF;
 				}
 				else if(typeName.equalsIgnoreCase("push")){
 					type = ArenaType.PUSH;
 				}
 				else if(typeName.equalsIgnoreCase("survival")){
 					type = ArenaType.SURVIVAL;
 				}
 				else if(typeName.equalsIgnoreCase("teams")){
 					type = ArenaType.TEAMS;
 				}
 				else if(typeName.equalsIgnoreCase("tntori")){
 					type = ArenaType.TNTORI;
 				}
 				else if(typeName.equalsIgnoreCase("ucars")){
 					type = ArenaType.UCARS;
 				}
 				else{
 					sender.sendMessage(ChatColor.RED+"Invalid type! Valid: ctf, pvp, push, survival, teams, tntori");
 					return true;
 				}
 				plugin.mgLobbies.setLobby(type, player.getLocation());
 				plugin.mgLobbyManager.save();
 				sender.sendMessage(ChatColor.GREEN+"Successfully set lobby location to where you are!");
 			}
 			else{
 				return false;
 			}
 			//TODO
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("listarenas")){
 			if(args.length <1){
 				return false;
 			}
 			int page = 1;
 			try {
 				page = Integer.parseInt(args[0]);
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			if(page < 1){
 				return false;
 			}
 			Set<String> arenas = plugin.minigamesArenas.getArenas();
 			List<String> arenaInfo = new ArrayList<String>();
 			for(String name:arenas){
 				Arena arena = plugin.minigamesArenas.getArena(name);
 				String toAdd = ChatColor.BLUE + "["+name+":] "+ChatColor.GOLD+"Type: "+ChatColor.RED+arena.getType().toString().toLowerCase() + ChatColor.GOLD+"  Players: "+ChatColor.RED+"["+arena.getHowManyPlayers()+"/"+arena.getPlayerLimit()+"]";
 				if(!arena.isValid()){
 					toAdd = toAdd + ChatColor.GRAY + " -Invalid Arena (Not setup yet!)";
 				}
 				arenaInfo.add(toAdd);
 			}
 			int displayed = 0;
 			double totalPagesunrounded = arenaInfo.size() / 5d;
 			NumberFormat fmt = NumberFormat.getNumberInstance();
 			fmt.setMaximumFractionDigits(0);
 			fmt.setRoundingMode(RoundingMode.UP);
 			String value = fmt.format(totalPagesunrounded);
 			int totalPages = Integer.parseInt(value);
 			if(totalPages == 0){
 				totalPages = 1;
 			}
 			page -= 1;
 			if(page > totalPages){
 				page = totalPages;
 			}
 			
 			if(page < 0){
 				page = 0;
 			}
 			int startpoint = page * 5;
 			sender.sendMessage(ChatColor.DARK_RED+"Arenas: Page: ["+(page+1)+"/"+(totalPages)+"]");
 			for(int i=startpoint;i<arenaInfo.size()&&displayed<5;i++){
 				sender.sendMessage(StringColors.colorise(arenaInfo.get(i)));
 				displayed++;
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("trainme")){
 			if(args.length < 1){
 				//no args
 				return false;
 			}
 			String skill = args[0];
 			if(skill.equalsIgnoreCase("worldguard")){
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
 				
 				String listString = "";
 				
 				//String newLine = System.getProperty("line.separator");
 
 				for (String s : info)
 				{
 				    listString += s + " %n";
 				}
 				//sender.sendMessage(playerName + " " + listString);
 				
 				String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 				String toPage = message[0];
 						for(int x=1 ; x<message.length ; x++) {
 						toPage = ChatColor.GOLD + toPage + ", " + StringColors.colorise(message[x]); // Send each argument in the message
 						}
 						int page = 1;
 						if(args.length > 1){
 							try {
 								page = Integer.parseInt(args[1]);
 							} catch (NumberFormatException e) {
 								sender.sendMessage(ChatColor.RED + "Invalid page number");
 								return true;
 							}
 						}
 						ChatPage tPage = ChatPaginator.paginate(toPage, page);
 						sender.sendMessage(ChatColor.RED + "Clans: "+ChatColor.GOLD + "[" + tPage.getPageNumber() + "/" + tPage.getTotalPages() + "]");
 						String[] lines = tPage.getLines();
 						for(int i=0;i<lines.length;i++){
 							sender.sendMessage(ChatColor.GOLD + lines[i]);
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
 				if(ac.clanMembers.containsKey(sender.getName())){
 					ac.clanMembers.remove(sender.getName());
 				}
 				ac.clanMembers.put(sender.getName(), newClan);
 				ac.saveHashMap(ac.clanMembers, plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 				ac.clanMembers = ac.loadHashMapString(plugin.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
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
 			String msg = "";//lol
 			for(int i=1;i<args.length;i++){
 				msg = msg + args[i] + " ";
 			}
 			msg = StringColors.colorise(msg);
 			String[] lines = msg.split("%n");
 			for(int i=0;i<lines.length;i++){
 				p.sendMessage(lines[i]);
 			}
 			sender.sendMessage(ChatColor.RED + "Sent to " + ChatColor.GOLD + p.getName());
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("profile")){
 			if(args.length<1){
 				return false;
 			}
 			String name = args[0];
 			OfflinePlayer[] players = plugin.getServer().getOfflinePlayers();
 			for(int i=0;i<players.length;i++){
 				if(players[i].getName().equalsIgnoreCase(name)){
 					name = players[i].getName();
 				}
 			}
 			String filename = name+".yml";
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "profiles" + File.separator + filename);
 			if(file.length() < 1 || !file.exists()){
 				sender.sendMessage(ChatColor.RED + "No profile exists for "+name);
 				return true;
 			}
 			Profile pProfile = new Profile(name);
 	        int warns = pProfile.getWarns();
 	        boolean isOnline = pProfile.getOnline();
 	        String online = "";
 	        if(isOnline){
 	        	online = "Online now!";
 	        }
 	        else{
 	        	online = pProfile.getOnlineTime();
 	        }
 	        String clan = pProfile.getClan();
 	        int points = pProfile.getRewardPoints();
 	        int kills = pProfile.getKills();
 	        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Profile for: " + ChatColor.GOLD + name);
 	        sender.sendMessage(ChatColor.RED + "Warns: " + ChatColor.GOLD + warns);
 	        sender.sendMessage(ChatColor.RED + "Last online: " + ChatColor.GOLD + online);
 	        sender.sendMessage(ChatColor.RED + "Clan: " + ChatColor.GOLD + clan);
 	        sender.sendMessage(ChatColor.RED + "Gamer points: " + ChatColor.GOLD + points);
 	        sender.sendMessage(ChatColor.RED + "Kills: " + ChatColor.GOLD + kills);
 	        pProfile.save();
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("spend")){
 			if(!(sender instanceof Player)){
 				sender.sendMessage(ChatColor.RED + "Must be a player to spend gamer points!");
 				return true;
 			}
 			if(args.length < 2){
 				return false;
 			}
 			String type = args[0];
 			int amount = 0;
 			try {
 				amount = Integer.parseInt(args[1]);
 			} catch (NumberFormatException e) {
 				sender.sendMessage(ChatColor.RED + "Invalid amount");
 				return true;
 			}
 			ArrayList<String> spendables = ac.spends.values;
 			Boolean contains = false;
 			int iterator = 0;
 			for(int i=0;i<spendables.size();i++){
 				String[] parts = spendables.get(i).split(":");
 				String name = parts[0];
 				if(name.equalsIgnoreCase(type)){
 					iterator = i;
 					contains = true;
 				}
 			}
 			if(type.equalsIgnoreCase("money")){
 				/*
 				if(!(ac.econ.hasAccount(player.getName()))){
 					sender.sendMessage(ChatColor.RED + "You don't have a bank account!");
 					return true;
 				}
 				*/
 				Profile pProfile = new Profile(player.getName());
 				int balance = pProfile.getRewardPoints();
 				if(balance - amount < 0){
 					sender.sendMessage(ChatColor.RED + "Not enough gamer points. You have " + ChatColor.GOLD + balance + ChatColor.RED + " gamer points");
 				    return true;
 				}
 				pProfile.addRewardPoint(-amount);
 				pProfile.save();
 				ac.econ.bankDeposit(player.getName(), amount);
 				EconomyResponse newBalance = ac.econ.bankBalance(player.getName());
 				double bal = newBalance.balance;
 				sender.sendMessage(ChatColor.RED + "Successfully transferred " + ChatColor.GOLD + amount + ChatColor.RED + " gamer points into " + ChatColor.GOLD + amount + " " + ac.econ.currencyNamePlural() + ChatColor.RED + ". You now have " + ChatColor.GOLD + bal + " " + ac.econ.currencyNamePlural() + ChatColor.RED + " in your account!");
 			}
 			else if(contains){
 				//TODO buy them
 				String data = spendables.get(iterator);
 				String[] parts = data.split(":");
 				String name = parts[0];
 				String perm = parts[1];
 				Profile pProfile = new Profile(player.getName());
 				int balance = pProfile.getRewardPoints();
 				int cost = Integer.parseInt(parts[2]);
 				if(amount < cost){
 					sender.sendMessage(ChatColor.RED + "The cost of "+name+" is "+cost+"pts");
 					return true;
 				}
 				if(cost < amount){
 					sender.sendMessage(ChatColor.RED + "The cost of "+name+" is "+cost+"pts");
 					return true;
 				}
 				if(balance < 30){
 					sender.sendMessage(ChatColor.RED + "The cost of "+name+" is "+cost+"pts. You only have " + balance + "pts");
 					return true;
 				}
 				pProfile.addRewardPoint(-cost);
 				pProfile.unlockPerm(perm);
 				sender.sendMessage(ChatColor.RED + "Successfully bought "+name+"!");
 				YamlConfiguration editor = pProfile.getEditor();
 				List<String> perms = editor.getStringList("perms.has");
 				for(int i=0;i<perms.size();i++){
 					String tperm = perms.get(i);
 					player.addAttachment(plugin, tperm, true);
 				}
 				player.recalculatePermissions();
 			}
 			else {
 				String toSend = ChatColor.RED + "Invalid type: Valid ones are: " + ChatColor.GOLD + "money, ";
 				for(int i=0;i<spendables.size();i++){
 					String v = spendables.get(i);
 					String[] parts = v.split(":");
 					String name = parts[0];
 					String cost = parts[2];
 					toSend = toSend+ChatColor.stripColor(name + " (" + cost + "pts), ");
 				}
 				sender.sendMessage(toSend);
 				return true;
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("stats")){
 			SortedMap<String, Integer> stats = Profile.getStats();
 			Map<String, Integer> vals = new HashMap<String, Integer>();
 			Object[] gamers = stats.keySet().toArray();
 			vals.putAll(stats);
 			int displayed = 0;
 			sender.sendMessage(ChatColor.RED+"Top ten gamers: (Gamer points)");
 			for(int i=0;i<gamers.length && displayed < 10;i++){
				sender.sendMessage(ChatColor.GOLD+"["+(i+1)+"] "+ChatColor.RED+gamers[i].toString()+": "+ChatColor.GOLD+vals.get(gamers[i].toString())+"pts");
				displayed++;
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("reward")){
 			if(args.length < 2){
 				return false;
 			}
 			String name = args[0];
 			OfflinePlayer[] players = plugin.getServer().getOfflinePlayers();
 			for(int i=0;i<players.length;i++){
 				if(name.equalsIgnoreCase(players[i].getName())){
 					name = players[i].getName();
 				}
 			}
 			String amount = args[1];
 			int num = 0;
 			try {
 				num = Integer.parseInt(amount);
 			} catch (NumberFormatException e) {
 				sender.sendMessage(ChatColor.RED + "Invalid amount");
 				return false;
 			}
 			Profile pProfile = new Profile(name);
 			pProfile.addRewardPoint(num);
 			pProfile.save();
 			Profile.calculateLeaderboard();
 			sender.sendMessage(ChatColor.RED + "Allocated " + ChatColor.GOLD + num + ChatColor.RED + " gamer points to " + ChatColor.GOLD + name);
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
                 Profile pProfile = new Profile(target.getName());
 		        pProfile.addWarn();
 		        pProfile.save();
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
 					Profile pProfile = new Profile(playerName);
 			        pProfile.clearWarns();
 			        pProfile.save();
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
 	else if(cmd.getName().equalsIgnoreCase("maintenance")){
 		if(args.length < 1){
 			return false;
 		}
 		String action = args[0];
 		if(action.equalsIgnoreCase("off")){
 			ac.config.set("general.maintenance.enable", false);
 			plugin.saveConfig();
 			sender.sendMessage(ChatColor.GOLD+"Disabled maintenance!");
 			return true;
 		}
 		else if(action.equalsIgnoreCase("on")){
 			if(args.length < 2){
 				return false;
 			}
 			ac.config.set("general.maintenance.enable", true);
 			String msg = "&6"+args[1];
 			for(int i=2;i<args.length;i++){
 				msg = msg + " "+args[i];
 			}
 			ac.config.set("general.maintenance.msg", msg);
 			plugin.saveConfig();
 			sender.sendMessage(ChatColor.GOLD+"Enabled maintenance with the msg: "+StringColors.colorise(msg));
 		    return true;
 		}
 		else{
 			return false;
 		}
 	}
 		return false;
 	}
 }
 	
 		 
