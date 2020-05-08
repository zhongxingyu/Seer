 package net.amoebaman.gamemaster;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 
 import net.amoebaman.gamemaster.api.AutoGame;
 import net.amoebaman.gamemaster.api.GameMap;
 import net.amoebaman.gamemaster.api.TeamAutoGame;
 import net.amoebaman.gamemaster.enums.PlayerStatus;
 import net.amoebaman.gamemaster.enums.Team;
 import net.amoebaman.gamemaster.modules.TimerModule;
 import net.amoebaman.gamemaster.utils.PropertySet;
 import net.amoebaman.kitmaster.Actions;
 import net.amoebaman.kitmaster.controllers.ItemController;
 import net.amoebaman.kitmaster.enums.Attribute;
 import net.amoebaman.kitmaster.handlers.HistoryHandler;
 import net.amoebaman.kitmaster.handlers.KitHandler;
 import net.amoebaman.kitmaster.objects.Kit;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.utils.CommandController.CommandHandler;
 import net.amoebaman.utils.GenUtil;
 import net.amoebaman.utils.chat.Align;
 import net.amoebaman.utils.chat.Chat;
 import net.amoebaman.utils.chat.Message;
 import net.amoebaman.utils.chat.Scheme;
 
 import net.minecraft.util.com.google.common.collect.Lists;
 
 public class CommandListener {
 	
 	@CommandHandler(cmd = "game")
 	public Object gameCmd(CommandSender sender, String[] args){
 		if(GameMaster.activeGame == null || !GameMaster.activeGame.isActive())
 			return new Message(Scheme.ERROR).then("There isn't a game running");
 		
 		Player context = sender instanceof Player? (Player) sender : null;
 		
 		List<Object> status = Lists.newArrayList(
 				(Object) new Message(Scheme.HIGHLIGHT).then("Currently playing ").then(GameMaster.activeGame).strong().then(" on ").then(GameMaster.activeMap).strong()
 		);
 		
 		if(GameMaster.activeGame instanceof TeamAutoGame && context != null && GameMaster.getStatus(context) == PlayerStatus.PLAYING){
 			Team team = ((TeamAutoGame) GameMaster.activeGame).getTeam(context);
 			status.add(new Message(Scheme.HIGHLIGHT).then("You are on the ").then(team).color(team.chat).then(" team").toString());
 		}
 		
 		status.addAll(GameMaster.activeGame.getStatus(context));
 
 		if(GameMaster.activeGame instanceof TimerModule){
 			long millis = ((TimerModule) GameMaster.activeGame).getGameLength() * 60 * 1000 - (System.currentTimeMillis() - GameMaster.gameStart);
 			int seconds = Math.round(millis / 1000F);
 			int mins = seconds / 60;
 			status.add(new Message(Scheme.NORMAL).then(mins).strong().then(" minutes and ").then(seconds%60).strong().then(" seconds remain").toString());
 		}
 		
 		return Align.addSpacers("", Align.center(status));
 	}
 	
 	@CommandHandler(cmd = "vote")
 	public Object voteCmd(CommandSender sender, String[] args){
 		switch(GameMaster.status){
 			case INTERMISSION:
 				if(GameMaster.games.size() < 2)
 					return new Message(Scheme.ERROR).then("There aren't any games to vote for");
 				AutoGame game = args.length > 0 ? GameMaster.getRegisteredGame(args[0]) : null;
 				if(game == null){
 					List<Message> list = Lists.newArrayList(new Message(Scheme.NORMAL).then("Click to vote for a game:"));
 					for(AutoGame each : GameMaster.games)
 						list.add(new Message(Scheme.NORMAL).then(" > ").then(each).strong().tooltip(Chat.format("&xClick to vote for &z" + each, Scheme.NORMAL)).command("/vote " + each));
 					return list;
 				}
 				if(game.equals(GameMaster.lastGame))
 					return new Message(Scheme.ERROR).then(game).strong().then(" has played recently, choose a different game");
 				GameMaster.votes.put(sender, game.getGameName());
 				return new Message(Scheme.NORMAL).then("You voted for ").then(game).strong().then(" for the next game");
 			case PREP:
 				GameMap map = args.length > 0 ? GameMaster.getRegisteredMap(args[0]) : null;
 				if(map == null){
 					List<Message> list = Lists.newArrayList(new Message(Scheme.NORMAL).then("Click to vote for a map:"));
 					for(GameMap each : GameMaster.getCompatibleMaps(GameMaster.activeGame))
 						list.add(new Message(Scheme.NORMAL).then(" > ").then(each).strong().tooltip(Chat.format("&xClick to vote for &z" + each, Scheme.NORMAL)).command("/vote " + each));
 					return list;
 				}
 				if(!GameMaster.activeGame.isCompatible(map))
 					return new Message(Scheme.ERROR).then(GameMaster.activeGame).strong().then(" can't be played on ").then(map).strong();
 				if(GameMaster.mapHistory.contains(map))
 					return new Message(Scheme.ERROR).then(map).strong().then(" has played recently, choose a different game");
 				GameMaster.votes.put(sender, map.name);
 				return new Message(Scheme.NORMAL).then("You voted for ").then(map).strong().then(" for the next map");
 			case RUNNING:
 			case SUSPENDED:
 				return new Message(Scheme.ERROR).then("You can only vote on games and maps during the intermission");
 		}
 		return null;
 	}
 	
 	private Kit getChargedKit(Kit normal){
 		return KitHandler.getKitByIdentifier("C-" + normal.name);
 	}
 	
 	@CommandHandler(cmd = "charges")
 	public Object chargesCmd(CommandSender sender, String[] args){
 		
 		if(args == null || args.length < 1){
 			return Align.addSpacers("", Lists.newArrayList(
 				new Message(Scheme.HIGHLIGHT).then("You have ").then(StatMaster.getHandler().getStat((Player) sender, "charges")).strong().then(" charges"),
 				new Message(Scheme.HIGHLIGHT).then("Earn free charges by voting for us on server lists daily"),
 				new Message(Scheme.HIGHLIGHT).then("  Click here to vote on PlanetMinecraft")
 					.link("http://bit.ly/landwarvotepmc"),
 				new Message(Scheme.HIGHLIGHT).then("  Click here to vote on MineStatus")
 					.link("http://bit.ly/landwarvotems"),
 				new Message(Scheme.HIGHLIGHT).then("  Check here to vote on MinecraftServerList")
 					.link("http://bit.ly/landwarvotemcsl"),
 				new Message(Scheme.HIGHLIGHT).then("Use charges to power up your kits with ")
 					.then("/charges use").strong()
 						.tooltip(Scheme.NORMAL.normal + "Click here to use a charge").command("/charges use"),
 				new Message(Scheme.HIGHLIGHT).then("Get info about a charged kit with ")
 					.then("/charges info <kit>").strong()
 			));
 		}
 		
 		if((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) && sender.hasPermission("gamemaster.admin")){
 			OfflinePlayer target = Bukkit.getPlayer(args[1]);
 			if(target == null)
 				target = Bukkit.getOfflinePlayer(args[1]);
 			if(!target.hasPlayedBefore())
 				return new Message(Scheme.ERROR).then("Could not find player");
 			int amount = Integer.parseInt(args[2]);
 			if(args[0].equalsIgnoreCase("set"))
 				StatMaster.getHandler().updateStat(target, "charges", amount);
 			else
 				StatMaster.getHandler().adjustStat(target, "charges", amount);
 			return new Message(Scheme.NORMAL)
 				.then(target.getName()).strong()
 				.then(" now has ")
 				.then(StatMaster.getHandler().getStat(target, "charges")).strong()
 				.then(" charges");
 		}
 		
 		OfflinePlayer other = Bukkit.getOfflinePlayer(args[0]);
 		if(other.hasPlayedBefore())
 			return new Message(Scheme.NORMAL)
 				.then(other.getName()).strong()
 				.then(" has ")
 				.then(StatMaster.getHandler().getStat(other, "charges")).strong()
 				.then(" charges");
 		
 		return null;
 	}
 	
 	@CommandHandler(cmd = "charges use")
 	public Object chargesUseCmd(Player player, String[] args){
 		if(StatMaster.getHandler().getStat(player, "charges") < 1)
 			return new Message(Scheme.ERROR).then("You don't have any charges");
 		List<Kit> last = HistoryHandler.getHistory(player);
 		if(last == null || last.isEmpty())
 			return new Message(Scheme.ERROR).then("You haven't taken a kit to charge");
 		Kit charged = null;
 		for(Kit kit : last)
 			if(!kit.stringAttribute(Attribute.IDENTIFIER).contains("parent") && !kit.stringAttribute(Attribute.IDENTIFIER).contains("supplydrop"))
 				charged = getChargedKit(kit);
 		if(charged == null)
 			return new Message(Scheme.ERROR).then("Your kit doesn't have a charged state available");
 		StatMaster.getHandler().adjustStat(player, "charges", -1);
 		Actions.giveKit(player, charged, true);
 		return new Message(Scheme.NORMAL).then("You used a charge to power up your kit");
 	}
 	
 	@CommandHandler(cmd = "charges info")
 	public Object chargesInfoCmd(Player sender, String[] args){
		if(args.length < 1)
 			return new Message(Scheme.ERROR).then("Name a kit to get info about its charged state");
		Kit charged = getChargedKit(KitHandler.getKit(args[0]));
 		if(charged == null)
 			return new Message(Scheme.ERROR).then("That kit doesn't have an upgraded state available");
 		new Message(Scheme.NORMAL)
 			.then("Kit info for ")
 			.then(charged.name).strong()
 			.send(sender);
 		new Message(Scheme.NORMAL).then("Items:").send(sender);
 		for(ItemStack item : charged.items)
 			new Message(Scheme.NORMAL)
 				.then(" - " + ItemController.friendlyItemString(item))
 					.itemTooltip(item)
 				.send(sender);
 		new Message(Scheme.NORMAL).then("Effects:").send(sender);
 		for(PotionEffect effect : charged.effects)
 			new Message(Scheme.NORMAL).then(" - " + ItemController.friendlyEffectString(effect)).send(sender);
 		return null;
 	}
 	
 	@CommandHandler(cmd = "teamchat")
 	public Object teamchatCmd(Player player, String[] args){
 		if(GameMaster.teamChatters.contains(player))
 			GameMaster.teamChatters.remove(player);
 		else
 			GameMaster.teamChatters.add(player);
 		return new Message(Scheme.NORMAL)
 			.then("Team-exclusive chatting is ")
 			.then((GameMaster.teamChatters.contains(player) ? "enabled" : "disabled")).strong();
 	}
 	
 	@CommandHandler(cmd = "fixme")
 	public Object fixmeCmd(Player player, String[] args){
 		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp " + player.getName() + " " + player.getName());
 		return new Message(Scheme.NORMAL).then("No problem");
 	}
 	
 	@CommandHandler(cmd = "changeteam")
 	public Object changeteamCmd(CommandSender sender, String[] args){
 		if(!GameMaster.status.active)
 			return new Message(Scheme.ERROR).t("There isn't a game running");
 		if(!(GameMaster.activeGame instanceof TeamAutoGame))
 			return new Message(Scheme.ERROR).t("This isn't a team game");
 		if(args.length == 0 && !(sender instanceof Player))
 			return new Message(Scheme.ERROR).t("Include a player to swap");
 		
 		Player target = args.length == 0 ? (Player) sender : Bukkit.getPlayer(args[0]);
 		if(target == null)
 			return new Message(Scheme.ERROR).t("Couldn't find specified player");
 		if(sender.hasPermission("gamemaster.admin")){
 			((TeamAutoGame) GameMaster.activeGame).changeTeam(target);
 			return new Message(Scheme.NORMAL)
 				.t("Changing ")
 				.t(target.equals(sender) ? "your" : target.getName() + "'s").s()
 				.t(" team");
 		}
 		return null;
 	}
 	
 	@CommandHandler(cmd = "balanceteams")
 	public Object balanceteamsCmd(CommandSender sender, String[] args){
 		TeamAutoGame.balancing = !TeamAutoGame.balancing;
 		return new Message(Scheme.NORMAL)
 			.t("Automatic team balancing is ")
 			.t(TeamAutoGame.balancing ? "enabled" : "disabled").s();
 	}
 	
 	@CommandHandler(cmd = "enter")
 	public Object enterCmd(Player player, String[] args){
 		if(GameMaster.getStatus(player) != PlayerStatus.PLAYING){
 			GameMaster.changeStatus(player, PlayerStatus.PLAYING);
 			return new Message(Scheme.NORMAL).t("You have entered the game");
 		}
 		return new Message(Scheme.NORMAL).t("You are already in the game");
 	}
 	
 	@CommandHandler(cmd = "exit")
 	public Object exitCmd(Player player, String[] args){
 		if(GameMaster.getStatus(player) != PlayerStatus.ADMIN){
 			GameMaster.changeStatus(player, PlayerStatus.ADMIN);
 			return new Message(Scheme.NORMAL).t("You have exited the game");
 		}
 		return new Message(Scheme.NORMAL).t("You were not in the game");
 	}
 	
 	@CommandHandler(cmd = "spectate")
 	public Object spectateCmd(Player player, String[] args){
 		if(GameMaster.getStatus(player) != PlayerStatus.EXTERIOR){
 			GameMaster.changeStatus(player, PlayerStatus.EXTERIOR);
 			return new Message(Scheme.HIGHLIGHT).t("You are spectating the game");
 		}
 		return new Message(Scheme.NORMAL).t("You are already spectating the game");
 	}
 	
 	@CommandHandler(cmd = "setlobby")
 	public Object setWaitCmd(Player player, String[] args){
 		GameMaster.mainLobby = player.getLocation();
 		return new Message(Scheme.NORMAL).t("Set the waiting room to your location");
 	}
 	
 	@CommandHandler(cmd = "setfireworks")
 	public Object setFireworksCmd(Player player, String[] args){
 		GameMaster.fireworksLaunch = player.getLocation();
 		return new Message(Scheme.NORMAL).t("Set the fireworks launch site to your location");
 	}
 	
 	@CommandHandler(cmd = "endgame")
 	public Object endGameCmd(CommandSender sender, String[] args){
 		if(GameMaster.activeGame != null && GameMaster.activeGame.isActive()){
 			GameMaster.activeGame.abort();
 			GameFlow.startIntermission();
 		}
 		return null;
 	}
 	
 	@CommandHandler(cmd = "nextgame")
 	public Object nextGameCmd(CommandSender sender, String[] args){
 		AutoGame game = GameMaster.getRegisteredGame(args[0]);
 		if(game == null)
 			return new Message(Scheme.ERROR).t("That game doesn't exist");
 		GameMaster.nextGame = game;
 		GameMaster.nextMap = null;
 		return new Message(Scheme.NORMAL).t("Set the next game to ").t(game).s();
 	}
 	
 	@CommandHandler(cmd = "nextmap")
 	public Object nextMapCmd(CommandSender sender, String[] args){
 		AutoGame game = GameMaster.nextGame == null ? GameMaster.activeGame : GameMaster.nextGame;
 		if(game == null)
 			return new Message(Scheme.ERROR).t("There isn't a game scheduled yet");
 		GameMap map = GameMaster.getRegisteredMap(args[0]);
 		if(map == null)
 			return new Message(Scheme.ERROR).t("That map doesn't exist");
 		if(!game.isCompatible(map))
 			return new Message(Scheme.ERROR).t(game).s().t(" can't be played on ").t(map).s();
 		GameMaster.nextMap = map;
 		return new Message(Scheme.NORMAL).t("Set the next map to ").t(map).s();
 	}
 	
 	@CommandHandler(cmd = "patch")
 	public Object patchCmd(CommandSender sender, String[] args){
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
 		return null;
 	}
 	
 	@CommandHandler(cmd = "gm-debug-cycle")
 	public Object gmDebugCycleCmd(CommandSender sender, String[] args){	
 		GameMaster.debugCycle = true;
 		return new Message(Scheme.NORMAL).then("Priting debug info for one cycle of recucring ops");
 	}
 
 	@CommandHandler(cmd = "game-map create")
 	public Object mapCreateCmd(CommandSender sender, String[] args){
 		if(args.length < 1)
 			return new Message(Scheme.ERROR).t("Include the name of the new map");
 
 		if(GameMaster.editMap != null)
 			Bukkit.dispatchCommand(sender, "game-map save");
 		GameMap map = new GameMap(args[0]);
 		if(GameMaster.maps.contains(map))
 			return new Message(Scheme.ERROR).t("A map with that name already exists");
 		GameMaster.editMap = map;
 		return new Message(Scheme.NORMAL).t("Created a new map named ").t(args[0]).s();
 	}
 	
 	@CommandHandler(cmd = "game-map edit")
 	public Object mapEditCmd(CommandSender sender, String[] args){
 		if(args.length < 1)
 			return new Message(Scheme.ERROR).t("Include the name of the map to edit");
 		
 		if(GameMaster.editMap != null)
 			Bukkit.dispatchCommand(sender, "game-map save");
 		GameMap map = GameMaster.getRegisteredMap(args[0]);
 		if(map == null)
 			return new Message(Scheme.ERROR).t("That map doesn't exist");
 		GameMaster.editMap = map;
 		return new Message(Scheme.NORMAL).t("Editing the map ").t(GameMaster.editMap).s();
 	}
 	
 	@CommandHandler(cmd = "game-map delete")
 	public Object mapDeleteCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		
 		GameMaster.maps.remove(GameMaster.editMap);
 		GameMap t = GameMaster.editMap;
 		GameMaster.editMap = null;
 		
 		return new Message(Scheme.NORMAL).t("Deleted the map named ").t(t).s();
 	}
 	
 	@CommandHandler(cmd = "game-map save")
 	public Object mapSaveCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		
 		GameMaster.maps.remove(GameMaster.editMap);
 		GameMaster.maps.add(GameMaster.editMap);
 		GameMap t = GameMaster.editMap;
 		GameMaster.editMap = null;
 
 		return new Message(Scheme.NORMAL).t("Saved the map named ").t(t).s();
 	}
 	
 	@CommandHandler(cmd = "game-map info")
 	public Object mapInfoCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		
 		List<Object> list = new ArrayList<Object>();
 		
 		list.add(new Message(Scheme.NORMAL).t("Name: ").s().t(GameMaster.editMap));
 		PropertySet prop = GameMaster.editMap.properties;
 		for(String key : prop.getKeys(false))
 			if(prop.isConfigurationSection(key)){
 				list.add(new Message(Scheme.NORMAL).t(key).s());
 				ConfigurationSection sec = prop.getConfigurationSection(key);
 				for(String subKey : sec.getKeys(true))
 					if(!sec.isConfigurationSection(subKey))
 						list.add(new Message(Scheme.NORMAL).t(subKey).s().t(sec.get(subKey)));
 			}
 			else
 				list.add(new Message(Scheme.NORMAL).t(key).s().t(prop.get(key)));
 		
 		return list;
 	}
 	
 	@CommandHandler(cmd = "game-map list")
 	public Object mapListCmd(CommandSender sender, String[] args){
 		new Message(Scheme.NORMAL).t("Maps: ").s().t(Chat.format(GenUtil.concat(GenUtil.objectsToStrings(GameMaster.maps), "&z", "&x, &z", ""), Scheme.NORMAL));
 		if(GameMaster.editMap != null)
 			new Message(Scheme.NORMAL).t("Editing: ").s().t(GameMaster.editMap);
 		if(args.length > 0){
 			AutoGame game = GameMaster.getRegisteredGame(args[0]);
 			if(game != null)
 				new Message(Scheme.NORMAL).t("Compatible with " + game + ": ").s().t(Chat.format(GenUtil.concat(GenUtil.objectsToStrings(GameMaster.getCompatibleMaps(game)), "&z", "&x, &z", ""), Scheme.NORMAL));
 		}
 		return null;
 	}
 
 	@CommandHandler(cmd = "game-map world")
 	public Object mapWorldCmd(Player player, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		GameMaster.editMap.properties.set("world", player.getWorld());
 		return new Message(Scheme.NORMAL).t("Map world set to ").t(player.getWorld().getName()).s();
 	}
 	
 	@CommandHandler(cmd = "game-map addteam")
 	public Object mapAddTeamCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		if(args.length < 1)
 			return new Message(Scheme.ERROR).t("Include a team to add");
 		Team newTeam = Team.getByString(args[0]);
 		if(newTeam == null)
 			return new Message(Scheme.ERROR).t("Invalid team...choose from: ").t(Chat.format(GenUtil.concat(Lists.newArrayList(Team.values()), "&z", "&x, &z", ""), Scheme.WARNING));
 		List<String> teams = GameMaster.editMap.properties.getStringList("active-teams");
 		for(String team : teams)
 			if(Team.getByString(team) == newTeam)
 				return new Message(Scheme.NORMAL).t("The ").t(newTeam).s().t(" team is already on this map");
 		teams.add(newTeam.name());
 		GameMaster.editMap.properties.set("active-teams", teams);
 		return new Message(Scheme.NORMAL).t("The ").t(newTeam).s().t(" now plays on this map");
 	}
 	
 	@CommandHandler(cmd = "game-map removeteam")
 	public Object mapRemoveTeamCmd(CommandSender sender, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		if(args.length < 1)
 			return new Message(Scheme.ERROR).t("Include a team to remove");
 		Team oldTeam = Team.getByString(args[0]);
 		if(oldTeam == null)
 			return new Message(Scheme.ERROR).t("Invalid team...choose from: ").t(Chat.format(GenUtil.concat(Lists.newArrayList(Team.values()), "&z", "&x, &z", ""), Scheme.WARNING));
 		
 		List<String> teams = GameMaster.editMap.properties.getStringList("active-teams");
 		boolean success = teams.remove(oldTeam.name()) || teams.remove(oldTeam.name().toLowerCase());
 		GameMaster.editMap.properties.set("active-teams", teams);
 		
 		if(success)
 			return new Message(Scheme.NORMAL).t("The ").t(oldTeam).s().t(" team has been removed from this map");
 		else
 			return new Message(Scheme.NORMAL).t("The ").t(oldTeam).s().t(" team was not part of this map");
 	}
 	
 	@CommandHandler(cmd = "game-map setspawn")
 	public Object mapSetSpawnCmd(Player player, String[] args){
 		if(GameMaster.editMap == null)
 			return new Message(Scheme.ERROR).t("No map is being edited");
 		if(args.length < 1)
 			return new Message(Scheme.ERROR).t("Include the team to set the spawn of");
 		Team team = Team.getByString(args[0]);
 		if(team == null)
 			return new Message(Scheme.ERROR).t("Invalid team...choose from: ").t(Chat.format(GenUtil.concat(Lists.newArrayList(Team.values()), "&z", "&x, &z", ""), Scheme.WARNING));
 		Location loc = player.getLocation();
 		loc.setX(loc.getBlockX() + 0.5);
 		loc.setY(loc.getBlockY() + 0.5);
 		loc.setZ(loc.getBlockZ() + 0.5);
 		GameMaster.editMap.properties.set("team-respawn/" + team.name(), loc);
 		return new Message(Scheme.NORMAL).t("Set the ").t(team).s().t(" team's spawn location to your position");
 	}
 
 }
