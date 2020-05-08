 /**
  * (C) Matt McCouaig 2012.
  */
 
 package uk.co.eclipsion.Eclipsi;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.map.MapView;
 import org.bukkit.map.MapView.Scale;
 import org.bukkit.plugin.Plugin;
 
 public class Commands {
 
 	public static void Ignition(CommandSender sender, String target,
 			String ticks) {
 		Player cmdPlayer = (Player) sender;
 		Player cmdTarget = cmdPlayer.getServer().getPlayer(target);
 		cmdTarget.setFireTicks(ticks.charAt(0));
 		cmdPlayer.getServer().broadcast(
 				"You successfully set " + target + " on fire for: " + ticks
 						+ " fire ticks!",
 				Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	}
 
 	public static void Smite(CommandSender sender, String target) {
 		Player cmdPlayer = (Player) sender;
 		Player cmdTarget = cmdPlayer.getServer().getPlayer(target);
 		cmdTarget.getWorld().strikeLightning(cmdTarget.getLocation());
 		cmdPlayer.getServer().broadcast(
 				ChatColor.GOLD + cmdPlayer.getDisplayName() + ChatColor.WHITE
 						+ " just smited " + ChatColor.GREEN
 						+ cmdTarget.getDisplayName(),
 				Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	}
 
 	public static void Weather(CommandSender sender, String value) {
 		Player player = (Player) sender;
 		World world = player.getWorld();
 		Server server = player.getServer();
 
 		if (value.equalsIgnoreCase("rain")) {
 			if (!world.hasStorm()) {
 				world.setStorm(true);
 				world.setThundering(false);
 			} else {
 				server.broadcastMessage(ChatColor.GOLD
 						+ player.getDisplayName() + ChatColor.WHITE
 						+ " is a plank and uses commands wrong.");
 			}
 		} else if (value.equalsIgnoreCase("sun")) {
 			if (world.hasStorm()) {
 				world.setStorm(false);
 				world.setThundering(false);
 			} else {
 				server.broadcastMessage(ChatColor.GOLD
 						+ player.getDisplayName() + ChatColor.WHITE
 						+ " is a plank and uses commands wrong.");
 			}
 		} else if (value.equalsIgnoreCase("thunder")) {
 			if (!world.isThundering()) {
 				world.setStorm(true);
 				world.setThundering(true);
 			} else {
 				server.broadcastMessage(ChatColor.GOLD
 						+ player.getDisplayName() + ChatColor.WHITE
 						+ " is a plank and uses commands wrong.");
 			}
 		}
 	}
 
 	public static void SpawnEntity(CommandSender sender, String entity) {
 		Player player = (Player) sender;
 		World world = player.getWorld();
 		Block block = player.getTargetBlock(null, 120);
 		EntityType entityMob;
 
 		if (entity == "irongolem") {
 			world.spawnCreature(block.getRelative(0, 1, 0).getLocation(),
 					EntityType.IRON_GOLEM);
 		} else if (entity == "cavespider") {
 			world.spawnCreature(block.getRelative(0, 1, 0).getLocation(),
 					EntityType.CAVE_SPIDER);
 		} else {
 
 			entityMob = EntityType.fromName(entity.toUpperCase());
 
 			if (!(entityMob == null)) {
 				world.spawnCreature(block.getRelative(0, 1, 0).getLocation(),
 						entityMob);
 			} else {
 				player.sendMessage(ChatColor.RED
 						+ "That entity doesn't exist. Plank.");
 			}
 		}
 	}
 
 	public static void SpawnTree(CommandSender sender, String type) {
 		Player player = (Player) sender;
 		World world = player.getWorld();
 		Block block = player.getTargetBlock(null, 120);
 
 		if (type.equalsIgnoreCase("Birch")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.BIRCH);
 		} else if (type.equalsIgnoreCase("bigtree")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.BIG_TREE);
 		} else if (type.equalsIgnoreCase("brownmushroom")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.BROWN_MUSHROOM);
 		} else if (type.equalsIgnoreCase("redmushroom")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.RED_MUSHROOM);
 		} else if (type.equalsIgnoreCase("redwood")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.REDWOOD);
 		} else if (type.equalsIgnoreCase("tallredwood")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.TALL_REDWOOD);
 		} else if (type.equalsIgnoreCase("tree")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.TREE);
 		} else if (type.equalsIgnoreCase("smalljungle")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.SMALL_JUNGLE);
 		} else if (type.equalsIgnoreCase("jungle")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.JUNGLE);
 		} else if (type.equalsIgnoreCase("junglebush")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.JUNGLE_BUSH);
 		} else if (type.equalsIgnoreCase("swamp")) {
 			world.generateTree(block.getRelative(0, 1, 0).getLocation(),
 					TreeType.SWAMP);
 		} else {
 			player.sendMessage(ChatColor.RED
 					+ "That tree doesn't exist. Make sure you're putting it in right.");
 		}
 	}
 
 	public static void Back(CommandSender sender) {
 		Player player = (Player) sender;
 		if (EclipsiMain.backAlready.get(player) == null
 				&& EclipsiMain.deathLocation.get(player) == null) {
 			sender.sendMessage(ChatColor.RED + "You haven't died yet!");
 		} else {
			if (EclipsiMain.backAlready.get(player)) {
 				EclipsiMain.backAlready.put(player, true);
 				player.teleport(EclipsiMain.deathLocation.get(player));
 				player.getServer()
 						.broadcast(
 								ChatColor.GOLD
 										+ player.getDisplayName()
 										+ ChatColor.RED
 										+ " just teleported back to their point of death.",
 								Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 			} else {
 				sender.sendMessage(ChatColor.RED
 						+ "You already went back bro. Don't be greedy now.");
 			}
 		}
 	}
 
 	public static void Fly(CommandSender sender, EclipsiMain instance) {
 		Player player = (Player) sender;
 		Plugin plugin = instance;
 		Boolean flightStatus = plugin.getConfig().getBoolean(
 				player.getDisplayName() + "flystatus", false);
 		if (flightStatus) {
 			player.setAllowFlight(false);
 			player.sendMessage(ChatColor.RED + "Flight turnt off.");
 			plugin.getConfig()
 					.set(player.getDisplayName() + "flystatus", false);
 			plugin.saveConfig();
 		} else {
 			player.setAllowFlight(true);
 			player.sendMessage(ChatColor.RED + "Flight turnt on.");
 			plugin.getConfig().set(player.getDisplayName() + "flystatus", true);
 			plugin.saveConfig();
 		}
 	}
 
 	public static void FlyTarget(CommandSender sender, EclipsiMain instance,
 			String target) {
 		Player player = (Player) sender;
 		Player targetted = sender.getServer().getPlayer(target);
 		Plugin plugin = instance;
 		if (sender.getServer().getPlayer(target) == null) {
 			sender.sendMessage("That player is not online.");
 		} else {
 			Boolean flightStatus = plugin.getConfig().getBoolean(
 					targetted.getDisplayName() + "flystatus", false);
 			if (flightStatus) {
 				targetted.setAllowFlight(false);
 				targetted.sendMessage(ChatColor.RED + "Flight turnt off by "
 						+ ChatColor.GOLD + player.getDisplayName());
 				plugin.getConfig().set(
 						targetted.getDisplayName() + "flystatus", false);
 				plugin.saveConfig();
 			} else {
 				targetted.setAllowFlight(true);
 				targetted.sendMessage(ChatColor.RED + "Flight turnt on by "
 						+ ChatColor.GOLD + player.getDisplayName());
 				plugin.getConfig().set(
 						targetted.getDisplayName() + "flystatus", true);
 				plugin.saveConfig();
 			}
 		}
 	}
 
 	public static void Map(CommandSender sender, String mapId, String scale) {
 		Player player = (Player) sender;
 		short map = Short.parseShort(mapId);
 		MapView mapView = player.getServer().getMap(map);
 		if (scale == "0") {
 			mapView.setScale(Scale.CLOSEST);
 		} else if (scale == "1") {
 			mapView.setScale(Scale.CLOSE);
 		} else if (scale == "2") {
 			mapView.setScale(Scale.NORMAL);
 		} else if (scale == "3") {
 			mapView.setScale(Scale.FAR);
 		} else if (scale == "4") {
 			mapView.setScale(Scale.FARTHEST);
 		}
 	}
 
 	public static void setHome(CommandSender sender) {
 		Player player = (Player) sender;
 		player.setBedSpawnLocation(player.getLocation());
 	}
 
 	public static void goHome(CommandSender sender) {
 		Player player = (Player) sender;
 		Location loc = player.getBedSpawnLocation();
 		player.teleport(loc);
 		player.sendMessage(ChatColor.AQUA + "Teleported to home.");
 	}
 
 	public static void setTimber(CommandSender sender, EclipsiMain instance,
 			String amount) {
 		Player player = (Player) sender;
 		Plugin plugin = instance;
 		int amt = Integer.parseInt(amount);
 		plugin.getConfig().set(player.getDisplayName() + ".timber", amt);
 		plugin.saveConfig();
 	}
 }
