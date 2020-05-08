 package net.amoebaman.gamemaster;
 
 import java.util.List;
 
 import net.amoebaman.gamemaster.api.AutoGame;
 import net.amoebaman.gamemaster.api.GameMap;
 import net.amoebaman.gamemaster.api.TeamAutoGame;
 import net.amoebaman.gamemaster.enums.PlayerStatus;
 import net.amoebaman.gamemaster.enums.Team;
 import net.amoebaman.gamemaster.modules.TimerModule;
 import net.amoebaman.utils.ChatUtils;
 import net.amoebaman.utils.ChatUtils.ColorScheme;
 import net.amoebaman.utils.CommandController.CommandHandler;
 import net.amoebaman.gamemaster.utils.PropertySet;
 import net.amoebaman.kitmaster.Actions;
 import net.amoebaman.kitmaster.controllers.ItemController;
 import net.amoebaman.kitmaster.enums.Attribute;
 import net.amoebaman.kitmaster.handlers.HistoryHandler;
 import net.amoebaman.kitmaster.handlers.KitHandler;
 import net.amoebaman.kitmaster.objects.Kit;
 import net.amoebaman.statmaster.StatMaster;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 
 public class CommandListener {
 	
 	@CommandHandler(cmd = "game")
 	public void gameCmd(CommandSender sender, String[] args){
 		if(GameMaster.activeGame == null || !GameMaster.activeGame.isActive()){
 			sender.sendMessage(ChatUtils.format("There isn't a game running", ColorScheme.ERROR));
 			return;
 		}
 		Player context = sender instanceof Player? (Player) sender : null;
 		sender.sendMessage(ChatUtils.spacerLine());
 		sender.sendMessage(ChatUtils.centerAlign(ChatUtils.format("Currently playing [[" + GameMaster.activeGame + "]] on [[" + GameMaster.activeMap + "]]", ColorScheme.HIGHLIGHT)));
 		if(GameMaster.activeGame instanceof TeamAutoGame && context != null && GameMaster.getStatus(context) == PlayerStatus.PLAYING){
 			Team team = ((TeamAutoGame) GameMaster.activeGame).getTeam(context);
 			sender.sendMessage(ChatUtils.centerAlign(ChatUtils.format("You are on the " + team.chat + team + "]] team", ColorScheme.HIGHLIGHT)));
 		}
 		for(String line : GameMaster.activeGame.getStatus(context))
 			sender.sendMessage(ChatUtils.centerAlign(ChatUtils.format(line, ColorScheme.NORMAL)));
 		if(GameMaster.activeGame instanceof TimerModule){
			long millis = ((TimerModule) GameMaster.activeGame).getGameLengthMinutes() * 60 * 1000 - (System.currentTimeMillis() - GameMaster.gameStart);
			int seconds = Math.round(millis / 1000F);
 			int mins = seconds / 60;
 			sender.sendMessage(ChatUtils.centerAlign(ChatUtils.format("[[" + mins + "]] minutes and [[" + seconds % 60 + "]] seconds remain", ColorScheme.NORMAL)));
 		}
 		sender.sendMessage(ChatUtils.spacerLine());
 	}
 	
 	@CommandHandler(cmd = "vote")
 	public void voteCmd(CommandSender sender, String[] args){
 		switch(GameMaster.status){
 			case INTERMISSION:
 				if(GameMaster.games.size() < 2){
 					sender.sendMessage(ChatUtils.format("There aren't any games to vote for", ColorScheme.ERROR));
 					return;
 				}
 				AutoGame game = args.length > 0 ? GameMaster.getRegisteredGame(args[0]) : null;
 				if(game == null){
 					sender.sendMessage(ChatUtils.format("Choose a valid game to vote for", ColorScheme.ERROR));
 					sender.sendMessage(ChatUtils.format("Available games: [[" + GameMaster.games + "]]", ColorScheme.NORMAL));
 					return;
 				}
 				if(game.equals(GameMaster.lastGame)){
 					sender.sendMessage(ChatUtils.format("You can't vote for the game that just ran", ColorScheme.ERROR));
 					return;
 				}
 				GameMaster.votes.put(sender, game.getGameName());
 				sender.sendMessage(ChatUtils.format("You voted for [[" + game + "]] for the next event", ColorScheme.NORMAL));
 				break;
 			case PREP:
 				GameMap map = args.length > 0 ? GameMaster.getRegisteredMap(args[0]) : null;
 				if(map == null){
 					sender.sendMessage(ChatUtils.format("Choose a valid map to vote for", ColorScheme.ERROR));
 					sender.sendMessage(ChatUtils.format("Available maps: [[" + GameMaster.getCompatibleMaps(GameMaster.activeGame) + "]]", ColorScheme.NORMAL));
 					return;
 				}
 				if(!GameMaster.activeGame.isCompatible(map)){
 					sender.sendMessage(ChatUtils.format("That map isn't compatible with the scheduled game", ColorScheme.ERROR));
 					return;
 				}
 				if(GameMaster.mapHistory.contains(map)){
 					sender.sendMessage(ChatUtils.format("That map has already played recently, choose another", ColorScheme.ERROR));
 					return;
 				}
 				GameMaster.votes.put(sender, map.name);
 				sender.sendMessage(ChatUtils.format("You voted for [["  + map + "]] for the next map", ColorScheme.NORMAL));
 				break;
 			case RUNNING:
 			case SUSPENDED:
 				sender.sendMessage(ChatUtils.format("You can only vote on games or maps during the intermission", ColorScheme.ERROR));
 				return;	
 		}
 		return;
 	}
 	
 	private Kit getChargedKit(Kit normal){
 		return KitHandler.getKitByIdentifier("C-" + normal.name);
 	}
 	
 	@CommandHandler(cmd = "charges")
 	public void chargesCmd(CommandSender sender, String[] args){
 		
 		if(args == null || args.length < 1){
 			sender.sendMessage(ChatUtils.spacerLine());
 			if(sender instanceof Player)
 				sender.sendMessage(ChatUtils.format(" You have [[" + StatMaster.getHandler().getStat((Player) sender, "charges") + "]] charges", ColorScheme.HIGHLIGHT));
 			sender.sendMessage(ChatUtils.format(" Earn charges by voting for us once per day", ColorScheme.HIGHLIGHT));
 			sender.sendMessage(ChatUtils.format("   http://bit.ly/landwarvotepmc", ColorScheme.HIGHLIGHT));
 			sender.sendMessage(ChatUtils.format("   http://bit.ly/landwarvotems", ColorScheme.HIGHLIGHT));
 			sender.sendMessage(ChatUtils.format("   http://bit.ly/landwarvotemcsl", ColorScheme.HIGHLIGHT));
 			sender.sendMessage(ChatUtils.format(" Use charges with [[/charges use]] to upgrade kits", ColorScheme.NORMAL));
 			sender.sendMessage(ChatUtils.format(" Get info about a charged kit with [[/charges info <kit>]]", ColorScheme.NORMAL));
 			sender.sendMessage(ChatUtils.spacerLine());
 			return;
 		}
 		
 		if((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) && sender.hasPermission("gamemaster.admin")){
 			OfflinePlayer target = Bukkit.getPlayer(args[1]);
 			if(target == null)
 				target = Bukkit.getOfflinePlayer(args[1]);
 			if(!target.hasPlayedBefore()){
 				sender.sendMessage(ChatUtils.format("Could not find player", ColorScheme.ERROR));
 				return;
 			}
 			int amount = Integer.parseInt(args[2]);
 			if(args[0].equalsIgnoreCase("set"))
 				StatMaster.getHandler().updateStat(target, "charges", amount);
 			else
 				StatMaster.getHandler().adjustStat(target, "charges", amount);
 			sender.sendMessage(ChatUtils.format("[[" + target.getName() + "]] now has [[" + StatMaster.getHandler().getStat(target, "charges") + "]] charges", ColorScheme.NORMAL));
 			return;
 		}
 		
 		OfflinePlayer other = Bukkit.getOfflinePlayer(args[0]);
 		if(other.hasPlayedBefore()){
 			sender.sendMessage(ChatUtils.format("[[" + other.getName() + "]] has [[" + StatMaster.getHandler().getStat(other, "charges") + "]] charges", ColorScheme.NORMAL));
 			return;
 		}
 	}
 	
 	@CommandHandler(cmd = "charges use")
 	public void chargesUseCmd(Player player, String[] args){
 		if(StatMaster.getHandler().getStat(player, "charges") < 1){
 			player.sendMessage(ChatUtils.format("You don't have any charges", ColorScheme.ERROR));
 			return;
 		}
 		List<Kit> last = HistoryHandler.getHistory(player);
 		if(last == null || last.isEmpty()){
 			player.sendMessage(ChatUtils.format("You haven't taken a kit to use the charge on", ColorScheme.ERROR));
 			return;
 		}
 		Kit charged = null;
 		for(Kit kit : last)
 			if(!kit.stringAttribute(Attribute.IDENTIFIER).contains("parent") && !kit.stringAttribute(Attribute.IDENTIFIER).contains("supplydrop"))
 				charged = getChargedKit(kit);
 		if(charged == null){
 			player.sendMessage(ChatUtils.format("Your current kit doesn't have an upgraded state available", ColorScheme.ERROR));
 			return;
 		}
 		StatMaster.getHandler().adjustStat(player, "charges", -1);
 		Actions.giveKit(player, charged, true);
 		player.sendMessage(ChatUtils.format("You used a charge to upgrade your kit", ColorScheme.NORMAL));
 		return;
 	}
 	
 	@CommandHandler(cmd = "charges info")
 	public void chargesInfoCmd(CommandSender sender, String[] args){
 		if(args.length < 2){
 			sender.sendMessage(ChatUtils.format("Name a kit to get info about its charged state", ColorScheme.ERROR));
 			return;
 		}
 		Kit charged = getChargedKit(KitHandler.getKit(args[1]));
 		if(charged == null){
 			sender.sendMessage(ChatUtils.format("That kit doesn't have an upgraded state available", ColorScheme.ERROR));
 			return;
 		}
 		sender.sendMessage(ChatColor.ITALIC + "Kit info for " + charged.name);
 		sender.sendMessage(ChatColor.ITALIC + "Items:");
 		for(ItemStack item : charged.items)
 			sender.sendMessage(ChatColor.ITALIC + "  - " + ItemController.friendlyItemString(item));
 		sender.sendMessage(ChatColor.ITALIC + "Effects:");
 		for(PotionEffect effect : charged.effects)
 			sender.sendMessage(ChatColor.ITALIC + "  - " + ItemController.friendlyEffectString(effect));
 		sender.sendMessage(ChatColor.ITALIC + "Permissions:");
 		for(String perm : charged.permissions)
 			sender.sendMessage(ChatColor.ITALIC + "  - " + perm);
 		for(Attribute attribute : charged.attributes.keySet())
 			sender.sendMessage(ChatColor.ITALIC + attribute.toString() + ": " + charged.getAttribute(attribute));
 	}
 	
 	@CommandHandler(cmd = "teamchat")
 	public void teamchatCmd(Player player, String[] args){
 		if(GameMaster.teamChatters.contains(player))
 			GameMaster.teamChatters.remove(player);
 		else
 			GameMaster.teamChatters.add(player);
 		player.sendMessage(ChatUtils.format("Team-exclusive chatting is [[" + (GameMaster.teamChatters.contains(player) ? "enabled" : "disabled") + "]]", ColorScheme.NORMAL));	
 	}
 	
 	@CommandHandler(cmd = "fixme")
 	public void fixmeCmd(Player player, String[] args){
 		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp " + player.getName() + " " + player.getName());
 		player.sendMessage(ChatUtils.format("No problem", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "changeteam")
 	public void changeteamCmd(CommandSender sender, String[] args){
 		if(!GameMaster.status.active){
 			sender.sendMessage(ChatUtils.format("There isn't a game running", ColorScheme.ERROR));
 			return;
 		}
 		if(!(GameMaster.activeGame instanceof TeamAutoGame)){
 			sender.sendMessage(ChatUtils.format("The current game doesn't utilize teams", ColorScheme.ERROR));
 			return;
 		}
 		if(args.length == 0 && !(sender instanceof Player)){
 			sender.sendMessage(ChatUtils.format("Specify a player to change", ColorScheme.ERROR));
 			return;
 		}
 		Player target = args.length == 0 ? (Player) sender : Bukkit.getPlayer(args[0]);
 		if(target == null){
 			sender.sendMessage(ChatUtils.format("Could not find target player", ColorScheme.ERROR));
 			return;
 		}
 		if(sender.hasPermission("gamemaster.admin")){
 			if(!target.equals(sender))
 				sender.sendMessage(ChatUtils.format("Changing [[" + target.getName() + "]]'s team", ColorScheme.NORMAL));
 			else
 				target.sendMessage(ChatUtils.format("Your team has been changed", ColorScheme.NORMAL));
 		}
 		((TeamAutoGame) GameMaster.activeGame).changeTeam(target);
 	}
 	
 	@CommandHandler(cmd = "balanceteams")
 	public void balanceteamsCmd(CommandSender sender, String[] args){
 		TeamAutoGame.balancing = !TeamAutoGame.balancing;
 		sender.sendMessage(ChatUtils.format("Automatic team balancing is [[" + (TeamAutoGame.balancing ? "enabled" : "disabled") + "]]", ColorScheme.ERROR));
 	}
 	
 	@CommandHandler(cmd = "enter")
 	public void enterCmd(Player player, String[] args){
 		if(GameMaster.getStatus(player) != PlayerStatus.PLAYING){
 			GameMaster.changeStatus(player, PlayerStatus.PLAYING);
 			player.sendMessage(ChatUtils.format("You have entered the game", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 	@CommandHandler(cmd = "exit")
 	public void exitCmd(Player player, String[] args){
 		if(GameMaster.getStatus(player) != PlayerStatus.ADMIN){
 			GameMaster.changeStatus(player, PlayerStatus.ADMIN);
 			player.sendMessage(ChatUtils.format("You have exited the game", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 	@CommandHandler(cmd = "spectate")
 	public void spectateCmd(Player player, String[] args){
 		if(GameMaster.getStatus(player) != PlayerStatus.EXTERIOR){
 			GameMaster.changeStatus(player, PlayerStatus.EXTERIOR);
 			player.sendMessage(ChatUtils.format("You are now spectating the game", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 	@CommandHandler(cmd = "setlobby")
 	public void setWaitCmd(Player player, String[] args){
 		GameMaster.mainLobby = player.getLocation();
 		player.sendMessage(ChatUtils.format("The waiting room was set to your location", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "setfireworks")
 	public void setFireworksCmd(Player player, String[] args){
 		GameMaster.fireworksLaunch = player.getLocation();
 		player.sendMessage(ChatUtils.format("The fireworks launch position was set to your location", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "endgame")
 	public void endGameCmd(CommandSender sender, String[] args){
 		if(GameMaster.activeGame != null && GameMaster.activeGame.isActive()){
 			GameMaster.activeGame.abort();
 			GameFlow.startIntermission();
 		}
 	}
 	
 	@CommandHandler(cmd = "nextgame")
 	public void nextGameCmd(CommandSender sender, String[] args){
 		AutoGame game = GameMaster.getRegisteredGame(args[0]);
 		if(game == null){
 			sender.sendMessage(ChatUtils.format("Sorry, we couldn't find that game", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.nextGame = game;
 		GameMaster.nextMap = null;
 		sender.sendMessage(ChatUtils.format("Set the next game to [[" + game.getGameName() + "]]", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "nextmap")
 	public void nextMapCmd(CommandSender sender, String[] args){
 		AutoGame game = GameMaster.nextGame == null ? GameMaster.activeGame : GameMaster.nextGame;
 		if(game == null){
 			sender.sendMessage(ChatUtils.format("There is no game scheduled yet", ColorScheme.ERROR));
 			return;
 		}
 		GameMap map = GameMaster.getRegisteredMap(args[0]);
 		if(map == null){
 			sender.sendMessage(ChatUtils.format("Sorry, we couldn't find that map", ColorScheme.ERROR));
 			return;
 		}
 		if(!game.isCompatible(map)){
 			sender.sendMessage(ChatUtils.format("That map isn't compatible with the scheduled game", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.nextMap = map;
 		sender.sendMessage(ChatUtils.format("Set the next map to [[" + GameMaster.nextMap + "]]", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "patch")
 	public void patchCmd(CommandSender sender, String[] args){
 		String reason = "";
 		for(String str : args)
 			reason += str + " ";
 		reason = reason.trim();
 		for (Player all : Bukkit.getOnlinePlayers()){
 			if(reason.equals(""))
 				all.kickPlayer("The server is restarting to put in a patch");	
 			else
 				all.kickPlayer("The server is restarting to patch " + reason);
 		}
 		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
 		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
 	}
 	
 	@CommandHandler(cmd = "gm-debug-cycle")
 	public void gmDebugCycleCmd(CommandSender sender, String[] args){	
 		GameMaster.debugCycle = true;
 		sender.sendMessage(ChatUtils.format("Printing debug info for one cycle of recurring ops", ColorScheme.NORMAL));
 	}
 
 	@CommandHandler(cmd = "game-map create")
 	public void mapCreateCmd(CommandSender sender, String[] args){
 		if(args.length < 1){
 			sender.sendMessage(ChatUtils.format("Include the name of the new map", ColorScheme.ERROR));
 			return;
 		}
 		if(GameMaster.editMap != null)
 			Bukkit.dispatchCommand(sender, "game-map save");
 		GameMap map = new GameMap(args[0]);
 		if(GameMaster.maps.contains(map)){
 			sender.sendMessage(ChatUtils.format("A map with that name already exists", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.editMap = map;
 		sender.sendMessage(ChatUtils.format("Created a new map named [[" + args[0] + "]]", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "game-map edit")
 	public void mapEditCmd(CommandSender sender, String[] args){
 		if(args.length < 1){
 			sender.sendMessage(ChatUtils.format("Include the name of the map to edit", ColorScheme.ERROR));
 			return;
 		}
 		if(GameMaster.editMap != null)
 			Bukkit.dispatchCommand(sender, "game-map save");
 		GameMap map = GameMaster.getRegisteredMap(args[0]);
 		if(map == null){
 			sender.sendMessage(ChatUtils.format("That map does not exist", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.editMap = map;
 		sender.sendMessage(ChatUtils.format("Now editing the map named [[" + GameMaster.editMap + "]]", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "game-map delete")
 	public void mapDeleteCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null){
 			sender.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.maps.remove(GameMaster.editMap);
 		sender.sendMessage(ChatUtils.format("Deleted the map named [[" + GameMaster.editMap + "]]", ColorScheme.NORMAL));
 		GameMaster.editMap = null;
 	}
 	
 	@CommandHandler(cmd = "game-map save")
 	public void mapSaveCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null){
 			sender.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.maps.remove(GameMaster.editMap);
 		GameMaster.maps.add(GameMaster.editMap);
 		sender.sendMessage(ChatUtils.format("Saved the map named [[" + GameMaster.editMap + "]]", ColorScheme.NORMAL));
 		GameMaster.editMap = null;
 	}
 	
 	@CommandHandler(cmd = "game-map info")
 	public void mapInfoCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null){
 			sender.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		sender.sendMessage(ChatUtils.format("[[Name:]] " + GameMaster.editMap.name, ColorScheme.NORMAL));
 		PropertySet prop = GameMaster.editMap.properties;
 		for(String key : prop.getKeys(false))
 			if(prop.isConfigurationSection(key)){
 				sender.sendMessage(ChatUtils.format("[[" + key + ":]]", ColorScheme.NORMAL));
 				ConfigurationSection sec = prop.getConfigurationSection(key);
 				for(String subKey : sec.getKeys(true))
 					if(!sec.isConfigurationSection(subKey))
 						sender.sendMessage(ChatUtils.format("  [[" + subKey + ":]] " + sec.get(subKey), ColorScheme.NORMAL));
 			}
 			else
 				sender.sendMessage(ChatUtils.format("[[" + key + ":]] " + prop.get(key), ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "game-map list")
 	public void mapListCmd(CommandSender sender, String[] args){
 		sender.sendMessage(ChatUtils.format("[[Maps:]] " + GameMaster.maps, ColorScheme.NORMAL));
 		if(GameMaster.editMap != null)
 			sender.sendMessage(ChatUtils.format("[[Editing:]] " + GameMaster.editMap, ColorScheme.NORMAL));
 		if(args.length > 0){
 			AutoGame game = GameMaster.getRegisteredGame(args[0]);
 			if(game != null)
 				sender.sendMessage(ChatUtils.format("[[Compatible with " + game + ":]] " + GameMaster.getCompatibleMaps(game), ColorScheme.NORMAL));
 		}
 	}
 
 	@CommandHandler(cmd = "game-map world")
 	public void mapWorldCmd(Player player, String[] args){
 		if(GameMaster.editMap == null){
 			player.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		GameMaster.editMap.properties.set("world", player.getWorld());
 		player.sendMessage(ChatUtils.format("Map world set to your current world", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "game-map addteam")
 	public void mapAddTeamCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null){
 			sender.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		if(args.length < 1){
 			sender.sendMessage(ChatUtils.format("Include a team to add", ColorScheme.ERROR));
 			return;
 		}
 		Team newTeam = Team.getByString(args[0]);
 		if(newTeam == null){
 			sender.sendMessage(ChatUtils.format("Invalid team", ColorScheme.ERROR));
 			sender.sendMessage(ChatUtils.format("Valid teams: [[" + Team.values() + "]]", ColorScheme.ERROR));
 			return;
 		}
 		List<String> teams = GameMaster.editMap.properties.getStringList("active-teams");
 		for(String team : teams)
 			if(Team.getByString(team) == newTeam){
 				sender.sendMessage(ChatUtils.format("The " + newTeam + " team is already included on this map", ColorScheme.NORMAL));
 				return;
 			}
 		teams.add(newTeam.name());
 		GameMaster.editMap.properties.set("active-teams", teams);
 		sender.sendMessage(ChatUtils.format("The " + newTeam + " has been added to this map", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "game-map removeteam")
 	public void mapRemoveTeamCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null){
 			sender.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		if(args.length < 1){
 			sender.sendMessage(ChatUtils.format("Include the team to remove", ColorScheme.ERROR));
 			return;
 		}
 		Team oldTeam = Team.getByString(args[0]);
 		if(oldTeam == null){
 			sender.sendMessage(ChatUtils.format("Invalid team", ColorScheme.ERROR));
 			sender.sendMessage(ChatUtils.format("Valid teams: [[" + Team.values() + "]]", ColorScheme.ERROR));
 			return;
 		}
 		List<String> teams = GameMaster.editMap.properties.getStringList("active-teams");
 		if(teams.remove(oldTeam.name()) || teams.remove(oldTeam.name().toLowerCase()))
 			sender.sendMessage(ChatUtils.format("The " + oldTeam + " team has been removed from the map", ColorScheme.NORMAL));
 		else
 			sender.sendMessage(ChatUtils.format("The " + oldTeam + " team was not a part of the map", ColorScheme.NORMAL));
 		GameMaster.editMap.properties.set("active-teams", teams);
 		sender.sendMessage(ChatUtils.format("The " + oldTeam + " has been added to this map", ColorScheme.NORMAL));
 	}
 	
 	@CommandHandler(cmd = "game-map setspawn")
 	public void mapSetSpawnCmd(Player player, String[] args){
 		if(GameMaster.editMap == null){
 			player.sendMessage(ChatUtils.format("No map is being edited", ColorScheme.ERROR));
 			return;
 		}
 		if(args.length < 1){
 			player.sendMessage(ChatUtils.format("Include the team to set the spawn of", ColorScheme.ERROR));
 			return;
 		}
 		Team team = Team.getByString(args[0]);
 		if(team == null){
 			player.sendMessage(ChatUtils.format("Invalid team", ColorScheme.ERROR));
 			player.sendMessage(ChatUtils.format("Valid teams: [[" + Team.values() + "]]", ColorScheme.ERROR));
 			return;
 		}
 		Location loc = player.getLocation();
 		loc.setX(loc.getBlockX() + 0.5);
 		loc.setY(loc.getBlockY() + 0.5);
 		loc.setZ(loc.getBlockZ() + 0.5);
 		GameMaster.editMap.properties.set("team-respawn/" + team.name(), loc);
 		player.sendMessage(ChatUtils.format("Set the " + team + " team's spawn location to your position", ColorScheme.NORMAL));
 	}
 
 }
