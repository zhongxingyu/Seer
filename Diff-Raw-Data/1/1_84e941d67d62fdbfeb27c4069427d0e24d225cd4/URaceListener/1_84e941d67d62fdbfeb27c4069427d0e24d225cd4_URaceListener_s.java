 package net.stormdev.mario.mariokart;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.stormdev.mario.utils.CheckpointCheck;
 import net.stormdev.mario.utils.DoubleValueComparator;
 import net.stormdev.mario.utils.HotBarItem;
 import net.stormdev.mario.utils.HotBarSlot;
 import net.stormdev.mario.utils.HotBarUpgrade;
 import net.stormdev.mario.utils.MarioHotBar;
 import net.stormdev.mario.utils.MarioKartHotBarClickEvent;
 import net.stormdev.mario.utils.MarioKartRaceFinishEvent;
 import net.stormdev.mario.utils.PlayerQuitException;
 import net.stormdev.mario.utils.RaceEndEvent;
 import net.stormdev.mario.utils.RaceFinishEvent;
 import net.stormdev.mario.utils.RaceQueue;
 import net.stormdev.mario.utils.RaceStartEvent;
 import net.stormdev.mario.utils.RaceType;
 import net.stormdev.mario.utils.RaceUpdateEvent;
 import net.stormdev.mario.utils.TrackCreator;
 import net.stormdev.mario.utils.shellUpdateEvent;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.EnderCrystal;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.event.vehicle.VehicleUpdateEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 import com.useful.ucars.ItemStackFromId;
 import com.useful.ucars.ucarUpdateEvent;
 import com.useful.ucars.ucars;
 import com.useful.ucarsCommon.StatValue;
 
 public class URaceListener implements Listener {
 	main plugin = null;
 
 	public URaceListener(main plugin) {
 		this.plugin = plugin;
 	}
 
 	public void penalty(final Minecart car, long time) {
 		if (car == null) {
 			return;
 		}
 		if (car.hasMetadata("kart.immune")) {
 			return;
 		}
 		double power = (time / 2);
 		if (power < 1) {
 			power = 1;
 		}
 		car.setMetadata("car.frozen", new StatValue(time, plugin));
 		car.setVelocity(new Vector(0, power, 0));
 		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 
 			public void run() {
 				car.getLocation().getWorld()
 				.playSound(car.getLocation(), Sound.WOOD_CLICK, 1f, 1f);
 				car.removeMetadata("car.frozen", plugin);
 			}
 		}, (time * 20));
 		return;
 	}
 
 	@EventHandler
 	void bananas(PlayerPickupItemEvent event) {
 		Item item = event.getItem();
 		ItemStack stack = item.getItemStack();
 		Player player = event.getPlayer();
 		if (!ucars.listener.inACar(player)) {
 			return;
 		}
 		if (plugin.raceMethods.inAGame(player, false) == null) {
 			return;
 		}
 		if (ItemStackFromId.equals(main.config.getString("mariokart.banana"),
 				stack.getTypeId(), stack.getDurability())) {
 			player.getWorld().playSound(player.getLocation(), Sound.SPLASH2,
 					1f, 0.5f);
 			item.remove();
 			this.penalty(((Minecart) player.getVehicle()), 1);
 			event.setCancelled(true);
 			return;
 		}
 		return;
 	}
 
 	@EventHandler
 	void playerDeath(PlayerDeathEvent event) {
 		Race r = plugin.raceMethods.inAGame(event.getEntity(), false);
 		if (r == null) {
 			return;
 		}
 		//r.broadcast(ChatColor.GREEN + event.getEntity().getName() + " respawned");
 		event.setDeathMessage("");
 		event.getDrops().clear();
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	void vehDestroy(VehicleDamageEvent event) { // Stops player's cars being
 		// broken in a race.
 		Vehicle veh = event.getVehicle();
 		if (veh.getPassenger() == null) {
 			return;
 		}
 		Entity e = veh.getPassenger();
 		if (!(e instanceof Player)) {
 			return;
 		}
 		Player player = (Player) e;
 		if (plugin.raceMethods.inAGame(player, false) == null) {
 			return;
 		}
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler
 	void invClick(InventoryClickEvent event) {
 		HumanEntity player = event.getWhoClicked();
 		if (!(player instanceof Player)) {
 			return;
 		}
 		if (!ucars.listener.inACar(player.getName())) {
 			return;
 		}
 		if (plugin.raceMethods.inAGame((Player) player, false) == null) {
 			return;
 		}
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler
 	public void onWandClickEvent(PlayerInteractEvent event) {
 		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)
 				&& !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			return;
 		}
 		Player player = event.getPlayer();
 		if (!main.trackCreators.containsKey(player.getName())) {
 			return;
 		}
 		TrackCreator creator = main.trackCreators.get(player.getName());
 		Boolean wand = false;
 		int handid = player.getItemInHand().getTypeId();
 		if (handid == main.config.getInt("setup.create.wand")) {
 			wand = true;
 		}
 		creator.set(wand);
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	void powerups(ucarUpdateEvent event) {
 		Player player = (Player) event.getVehicle().getPassenger();
 		try {
 			if (plugin.raceMethods.inAGame(player, false) == null) {
 				return;
 			}
 		} catch (Exception e) {
 			return;
 		}
 		main.marioKart.calculate(player, event);
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	void trackingShells(shellUpdateEvent event) {
 		// if target is null then green shell
 		final Entity shell = event.getShell();
 		Location shellLoc = shell.getLocation();
 		int sound = 0;
 		if (shell.hasMetadata("shell.sound")) {
 			sound = (Integer) ((StatValue) shell.getMetadata("shell.sound")
 					.get(0)).getValue();
 		}
 		if (sound < 1) {
 			shellLoc.getWorld().playSound(shellLoc, Sound.NOTE_PLING, 1.25f,
 					1.8f);
 			sound = 3;
 			shell.removeMetadata("shell.sound", plugin);
 			shell.setMetadata("shell.sound", new StatValue(sound, plugin));
 		} else {
 			sound--;
 			shell.removeMetadata("shell.sound", plugin);
 			shell.setMetadata("shell.sound", new StatValue(sound, plugin));
 		}
 		double speed = 1.2;
 		String targetName = event.getTarget();
 		if (targetName != null) {
 			final Player target = plugin.getServer().getPlayer(targetName);
 			Location targetLoc = target.getLocation();
 			double x = targetLoc.getX() - shellLoc.getX();
 			double z = targetLoc.getZ() - shellLoc.getZ();
 			Boolean ux = true;
 			double px = Math.abs(x);
 			double pz = Math.abs(z);
 			if (px > pz) {
 				ux = false;
 			}
 
 			if (ux) {
 				// x is smaller
 				// long mult = (long) (pz/speed);
 				x = (x / pz) * speed;
 				z = (z / pz) * speed;
 			} else {
 				// z is smaller
 				// long mult = (long) (px/speed);
 				x = (x / px) * speed;
 				z = (z / px) * speed;
 			}
 			Vector vel = new Vector(x, 0, z);
 			shell.setVelocity(vel);
 			if (pz < 1.1 && px < 1.1) {
 				String msg = main.msgs.get("mario.hit");
 				msg = msg.replaceAll(Pattern.quote("%name%"), "tracking shell");
 				target.getLocation()
 				.getWorld()
 				.playSound(target.getLocation(), Sound.ENDERDRAGON_HIT,
 						1, 0.8f);
 				target.sendMessage(ChatColor.RED + msg);
 				penalty(((Minecart) target.getVehicle()), 4);
 				shell.setMetadata("shell.destroy", new StatValue(0, plugin));
 				return;
 			}
 			return;
 		} else {
 			speed = 1.5;
 			Vector direction = event.direction;
 			if (!event.getCooldown()) {
 				if (shellLoc.getBlock().getType() != Material.AIR
 						&& shellLoc.getBlock().getType() != Material.CARPET) {
 					// Bounce
 					direction = direction.multiply(-1);
 				}
 			}
 			shell.setVelocity(direction);
 			if (!event.getCooldown()) {
 				if (shell.getNearbyEntities(2, 2, 2).size() > 0) {
 					List<Entity> nearby = shell.getNearbyEntities(2, 2, 2);
 					for (Entity entity : nearby) {
 						if (entity instanceof Player) {
 							Player pl = (Player) entity;
 							if (ucars.listener.inACar(pl)) {
 								String msg = main.msgs.get("mario.hit");
 								msg = msg.replaceAll(Pattern.quote("%name%"),
 										"green shell");
 								pl.getLocation()
 								.getWorld()
 								.playSound(pl.getLocation(),
 										Sound.ENDERDRAGON_HIT, 1, 0.8f);
 								pl.sendMessage(ChatColor.RED + msg);
 								penalty(((Minecart) pl.getVehicle()), 4);
 								shell.setMetadata("shell.destroy",
 										new StatValue(0, plugin));
 							}
 							return;
 						}
 					}
 				}
 			}
 		}
 
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	void RaceEnd(RaceEndEvent event) {
 		Race game = event.getRace();
 		if(game == null){
 			return;
 		}
 		game.running = false;
 		try {
 			game.users.clear();
 		} catch (Exception e2) {
 			//Users already cleared
 		}
 		try {
 			if (plugin.raceScheduler.isTrackInUse(game.getTrack(), game.getType())) {
 				plugin.raceScheduler.removeRace(game);
 			}
 		} catch (Exception e1) {
 			try {
 				plugin.raceScheduler.removeRace(game);
 			} catch (Exception e) {
 				// Nothing
 			}
 		}
 		plugin.raceScheduler.recalculateQueues();
 		if(!game.isEmpty()){
 			main.logger.info("MEMORY LEAK ALERT");
 		}
 	}
 
 	// Much extranious PORTED code from here on (Races)
 	@EventHandler(priority = EventPriority.HIGHEST)
 	void RaceEnd(RaceFinishEvent event) {
 		try {
 			Race game = event.getRace();
 			Boolean timed = game.getType() == RaceType.TIME_TRIAL;
 			List<User> usersIn = game.getUsersIn();
 			String in = "";
 			for (User user : usersIn) {
 				in = in + ", " + user.getPlayerName();
 			}
 			Map<String, Double> scores = new HashMap<String, Double>();
 			Boolean finished = false;
 			User user = event.getUser();
 			Player player = null;
 			try {
 				player = user.getPlayer();
 			} catch (PlayerQuitException e1) {
 				//Player has left
 			}
 			if(player == null){
 				//Player has been removed from race prematurely
 				player = main.plugin.getServer().getPlayer(user.getPlayerName());
 				if(player == null || !player.isOnline()){
 					return; //Player is no longer around...
 				}
 			}
 			if(player != null){
 				player.removeMetadata("car.stayIn", plugin);
 				player.setCustomName(ChatColor.stripColor(player.getCustomName()));
 				player.setCustomNameVisible(false);
 				if (player.getVehicle() != null) {
 					Vehicle veh = (Vehicle) player.getVehicle();
 
 					veh.eject();
 
 					veh.remove();
 				}
 				Location loc = game.getTrack().getExit(plugin.getServer());
 				if (loc == null) {
 					player.teleport(player.getLocation().getWorld().getSpawnLocation());
 				} else {
 					player.teleport(loc);
 				}
 				if (player.isOnline()) {
 					player.getInventory().clear();
 
 					player.getInventory().setContents(user.getOldInventory());
 				}
 			}
 			if (game.finished.contains(user.getPlayerName())) {
 				finished = true;
 			} else {
 				HashMap<User, Double> checkpointDists = new HashMap<User, Double>();
 				for (User u : game.getUsers()) {
 					try {
 						Player pp = u.getPlayer();
 						if (pp != null){
 							if (pp.hasMetadata("checkpoint.distance")) {
 								List<MetadataValue> metas = pp
 										.getMetadata("checkpoint.distance");
 								checkpointDists.put(u,  (Double) ((StatValue) metas.get(0)).getValue());
 							}
 						}
 					} catch (PlayerQuitException e) {
 						//Player has left
 					}
 				}
 
 				for (User u : game.getUsers()) {
 					try {
 						int laps = game.totalLaps - u.getLapsLeft() + 1;
 
 						int checkpoints = u.getCheckpoint();
 
 						double distance = 1 / (checkpointDists.get(u));
 
 						double score = (laps * game.getMaxCheckpoints()) + checkpoints + distance;
 
 						try {
 							if (game.getWinner().equals(u)) {
 								score = score + 1;
 							}
 						} catch (Exception e) {
 						}
 						scores.put(u.getPlayerName(), score);
 					} catch (Exception e) {
 						//User has left
 					}
 				}
 			}
 			if(player != null){
 				player.getInventory().clear();
 
 				player.getInventory().setContents(user.getOldInventory());
 			}
 			if (!finished) {
 				DoubleValueComparator com = new DoubleValueComparator(scores);
 				SortedMap<String, Double> sorted = new TreeMap<String, Double>(com);
 				sorted.putAll(scores);
 				Set<String> keys = sorted.keySet();
 				Object[] pls = (Object[]) keys.toArray();
 				for (int i = 0; i < pls.length; i++) {
 					Player p = plugin.getServer().getPlayer((String) pls[i]); //Evidence the dodgy PR was not tested as it was still reading string with Player in the map
 					if (p.equals(player)) {
 						if (p != null) {
 							String msg = "";
 							if (!timed) {
 								msg = main.msgs.get("race.end.position");
 								if ((i + 1) <= 4
 										&& (i + 1) != game.getUsers().size()) {player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1, 1);
 								} else {
 									player.getWorld().playSound(
 											player.getLocation(), Sound.NOTE_BASS,
 											1, 1);
 								}
 								String pos = "" + (i + 1);
 								if (pos.endsWith("1")) {
 									pos = pos + "st";
 								} else if (pos.endsWith("2")) {
 									pos = pos + "nd";
 								} else if (pos.endsWith("3")) {
 									pos = pos + "rd";
 								} else {
 									pos = pos + "th";
 								}
 								msg = msg.replaceAll("%position%", "" + pos);
 								MarioKartRaceFinishEvent evt = new MarioKartRaceFinishEvent(player, (i + 1), pos);
 								plugin.getServer().getPluginManager().callEvent(evt);
 							} else {
 								double tim = (game.endTimeMS - game.startTimeMS) / 10;
 								double ti = (int) tim;
 								double t = ti / 100;
 								msg = main.msgs.get("race.end.time");
 								msg = msg.replaceAll(Pattern.quote("%time%"), t
 										+ "");
 								plugin.raceTimes.addRaceTime(game.getTrack()
 										.getTrackName(), player.getName(), t);
 							}
 							p.sendMessage(main.colors.getSuccess() + msg);
 						}
 					}
 				}
 			} else {
 				if (player != null) {
 					int position = 1;
 
 					for (int i = 0; i < game.getUsersFinished().size(); i++) {
 						if (game.getUsersFinished().get(i).equals(user.getPlayerName())) {
 							position = i + 1;
 						}
 					}
 					String msg = "";
 					if (!timed) {
 						msg = main.msgs.get("race.end.position");
 						if (position <= 4 && position != game.getUsers().size()) {
 							player.getWorld().playSound(player.getLocation(),
 									Sound.NOTE_BASS_GUITAR, 1, 1);
 						} else {
 							player.getWorld().playSound(player.getLocation(),
 									Sound.NOTE_BASS, 1, 1);
 						}
 						String pos = "" + position;
 						if (pos.endsWith("1")) {
 							pos = pos + "st";
 						} else if (pos.endsWith("2")) {
 							pos = pos + "nd";
 						} else if (pos.endsWith("3")) {
 							pos = pos + "rd";
 						} else {
 							pos = pos + "th";
 						}
 						try {
 							msg = msg.replaceAll("%position%", "" + pos);
 						} catch (Exception e) {
 						}
 						MarioKartRaceFinishEvent evt = new MarioKartRaceFinishEvent(player, position, pos);
 						plugin.getServer().getPluginManager().callEvent(evt);
 					} else {
 						// Time trial
 						double tim = (game.endTimeMS - game.startTimeMS) / 10;
 						double ti = (int) tim;
 						double t = ti / 100;
 						msg = main.msgs.get("race.end.time");
 						msg = msg.replaceAll(Pattern.quote("%time%"), t + "");
 						plugin.raceTimes
 						.addRaceTime(game.getTrack().getTrackName(),
 								player.getName(), t);
 					}
 					player.sendMessage(main.colors.getSuccess() + msg);
 				}
 			}
 			game.leave(user, false);
 			plugin.raceScheduler.updateRace(game);
 			if (game.getUsersIn().size() < 1) {
 				game.ended = true;
 				game.end();
 			}
 			final Player pl = player;
 			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 
 				public void run() {
 					String rl = main.config
 							.getString("mariokart.resourceNonMarioPack");
 					Boolean valid = true;
 					try {
 						new URL(rl);
 					} catch (MalformedURLException e2) {
 						valid = false;
 					}
 					if (valid) {
 						pl.sendMessage(main.colors.getInfo()
 								+ main.msgs.get("resource.clear"));
 						pl.setTexturePack(rl);
 					}
 					return;
 				}
 			}, 150l);
 			return;
 		} catch (IllegalArgumentException e) {
 			//Player has left (Silly User system breaking everything...)
 		}
 	}
 
 	@EventHandler
 	void gameQuitting(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		Race game = plugin.raceMethods.inAGame(player, false);
 		if (game == null) {
 			RaceQueue queue = plugin.raceMethods.inGameQue(player);
 			if (queue == null) {
 				return;
 			}
 			queue.removePlayer(player);
 			return;
 		} else {
 			game.leave(game.getUser(player.getName()), true);
 			return;
 		}
 	}
 
 	@EventHandler
 	void gameQuitting(PlayerKickEvent event) {
 		Player player = event.getPlayer();
 		Race game = plugin.raceMethods.inAGame(player, false);
 		if (game == null) {
 			RaceQueue queue = plugin.raceMethods.inGameQue(player);
 			if (queue == null) {
 				return;
 			}
 			queue.removePlayer(player);
 			return;
 		} else {
 			game.leave(game.getUser(player.getName()), true);
 			return;
 		}
 	}
 
 	@EventHandler
 	void stayInCar(VehicleExitEvent event) {
 		if (!(event.getVehicle() instanceof Minecart)) {
 			return;
 		}
 		Minecart car = (Minecart) event.getVehicle();
 		if (!(event.getExited() instanceof Player)) {
 			return;
 		}
 		Player player = (Player) event.getExited();
 		if (!(player.hasMetadata("car.stayIn"))) {
 			return;
 		}
 		if (!ucars.listener.isACar(car)) {
 			return;
 		}
 		event.setCancelled(true);
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGHEST)
 	void RaceStart(RaceStartEvent event) {
 		Race game = event.getRace();
 		List<User> users = game.getUsers();
 		for (User user : users) {
 			try {
 				Player player = user.getPlayer();
 				player.setGameMode(GameMode.SURVIVAL);
 				player.getInventory().clear();
 				updateHotBar(player);
 				player.updateInventory();
 			} catch (PlayerQuitException e) {
 				//Player has left
 				game.leave(user, true);
 			}
 		}
 		plugin.raceScheduler.updateRace(game);
 		users = game.getUsers();
 		for (User user : users) {
 			user.setLapsLeft(game.totalLaps);
 			user.setCheckpoint(0);
 			String msg = main.msgs.get("race.mid.lap");
 			msg = msg.replaceAll(Pattern.quote("%lap%"), "" + 1);
 			msg = msg.replaceAll(Pattern.quote("%total%"), "" + game.totalLaps);
 			try {
 				user.getPlayer().sendMessage(main.colors.getInfo() + msg);
 			} catch (PlayerQuitException e) {
 				//Player has left
 			}
 		}
 		game.setUsers(users);
 		plugin.raceScheduler.recalculateQueues();
 		return;
 	}
 
 	@EventHandler
 	void RaceHandler(RaceUpdateEvent event) {
 		final Race game = event.getRace();
 		if (!game.getRunning()) {
 			try {
 				plugin.raceScheduler.stopRace(game);
 			} catch (Exception e) {
 			}
 			plugin.raceScheduler.recalculateQueues();
 			return;
 		}
 		if(!game.ending && !game.ending && main.config.getBoolean("general.race.enableTimeLimit") 
 				&& ((System.currentTimeMillis()-game.startTimeMS)*0.001)>game.timeLimitS){
 			game.broadcast(main.msgs.get("race.end.timeLimit"));
 			game.ending = true;
 			game.end();
 			return;
 		}
 		for (User user : game.getUsersIn()) {
 			String pname = user.getPlayerName();
 			Player player = plugin.getServer().getPlayer(pname);
 			if (player == null) {
 				game.leave(user, true);
 			} else {
 				Location playerLoc = player.getLocation();
 				Boolean checkNewLap = false;
 				int old = user.getCheckpoint();
 				if (old == game.getMaxCheckpoints()) {
 					checkNewLap = true;
 				}
 				Integer[] toCheck = new Integer[] {};
 				if (checkNewLap) {
 					toCheck = new Integer[] { 0 };
 				} else {
 					toCheck = new Integer[] { (old + 1) };
 				}
 				CheckpointCheck check = game.playerAtCheckpoint(toCheck,
 						player, plugin.getServer());
 
 				if (check.at) { // At a checkpoint
 					int ch = check.checkpoint;
 					if (ch >= game.getMaxCheckpoints()) {
 						checkNewLap = true;
 					}
 					if (!(ch == old)) {
 						/*
 						 * Removed to reduce server load - Requires all
 						 * checkpoints to be checked if(ch-2 > old){ //They
 						 * missed a checkpoint
 						 * player.sendMessage(main.colors.getError
 						 * ()+main.msgs.get("race.mid.miss")); return; }
 						 */
 						if (!(old >= ch)) {
 							user.setCheckpoint(check.checkpoint);
 						}
 					}
 				}
 				int lapsLeft = user.getLapsLeft();
 
 				if (lapsLeft < 1 || checkNewLap) {
 					if (game.atLine(plugin.getServer(), playerLoc)) {
 						if (checkNewLap) {
 							int left = lapsLeft - 1;
 							if (left < 0) {
 								left = 0;
 							}
 							user.setCheckpoint(0);
 							user.setLapsLeft(left);
 							lapsLeft = left;
 							if (left != 0) {
 								String msg = main.msgs.get("race.mid.lap");
 								int lap = game.totalLaps - lapsLeft + 1;
 								msg = msg.replaceAll(Pattern.quote("%lap%"), ""
 										+ lap);
 								msg = msg.replaceAll(Pattern.quote("%total%"),
 										"" + game.totalLaps);
 								if (lap == game.totalLaps) {
 									player.getWorld().playSound(
 											player.getLocation(),
 											Sound.NOTE_STICKS, 2, 1);
 								}
 								player.sendMessage(main.colors.getInfo() + msg);
 							}
 						}
 						if (lapsLeft < 1) {
 							Boolean won = game.getWinner() == null;
 							if (won) {
 								game.setWinner(user);
 							}
 							game.finish(user);
 							if (won && game.getType() != RaceType.TIME_TRIAL) {
 								for (User u : game.getUsers()) {
 									Player p;
 									try {
 										p = u.getPlayer();
 										String msg = main.msgs.get("race.end.soon");
 										msg = msg.replaceAll("%name%", p.getName());
 										p.sendMessage(main.colors.getSuccess() + game.getWinner() + main.msgs.get("race.end.won"));
 										p.sendMessage(main.colors.getInfo() + msg);
 									} catch (PlayerQuitException e) {
 										//Player has left
 									}
 									
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		plugin.raceScheduler.updateRace(game);
 		return;
 	}
 
 	@EventHandler
 	void damage(EntityDamageEvent event) {
 		if (!(event.getEntityType() == EntityType.MINECART)) {
 			return;
 		}
 		if (!(event.getCause() == DamageCause.ENTITY_EXPLOSION || event
 				.getCause() == DamageCause.BLOCK_EXPLOSION)) {
 			return;
 		}
 		if (!ucars.listener.isACar((Minecart) event.getEntity())) {
 			return;
 		}
 		try {
 			if (plugin.raceMethods.inAGame(((Player) event.getEntity()
 					.getPassenger()), false) == null
 					&& !(event.getEntity().hasMetadata("kart.immune"))) {
 				return;
 			}
 		} catch (Exception e) {
 			return;
 		}
 		event.setDamage(0);
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	void exploder(EntityExplodeEvent event) {
 		if (!main.config.getBoolean("mariokart.enable")) {
 			return;
 		}
 		if (event.getEntity() == null) {
 			return;
 		}
 		if (event.getEntity().hasMetadata("explosion.none")) {
 			Location loc = event.getEntity().getLocation();
 			event.setCancelled(true);
 			event.getEntity().remove();
 			double radius = 6;
 			loc.getWorld().createExplosion(loc, 0);
 			Double x = (double) radius;
 			Double y = (double) radius;
 			Double z = (double) radius;
 			List<Entity> near = event.getEntity().getNearbyEntities(x, y, z);
 
 			Object[] entarray = near.toArray();
 
 			Entity listent;
 
 			for (Object s : entarray) {
 				listent = (Entity) s;
 				EntityType type = listent.getType();
 				if (type == EntityType.MINECART) {
 					if (ucars.listener.isACar((Minecart) listent)) {
 						try {
 							((Minecart) listent).setDamage(0);
 							penalty((Minecart) listent, 4);
 						} catch (Exception e) {
 						}
 					}
 				}
 			}
 
 		}
 	}
 
 	@EventHandler
 	void signClicker(PlayerInteractEvent event) {
 		main.marioKart.calculate(event.getPlayer(), event);
 		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
 			return;
 		}
 		if (!(event.getClickedBlock().getState() instanceof Sign)) {
 			return;
 		}
 		Sign sign = (Sign) event.getClickedBlock().getState();
 		String[] lines = sign.getLines();
 		if (!ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")) {
 			return;
 		}
 		String cmd = ChatColor.stripColor(lines[1]);
 		if (cmd.equalsIgnoreCase("list")) {
 			int page = 1;
 			try {
 				page = Integer.parseInt(ChatColor.stripColor(lines[2]));
 			} catch (NumberFormatException e) {
 			}
 			main.cmdExecutor.urace(event.getPlayer(), new String[] { "list",
 				"" + page }, event.getPlayer());
 		} else if (cmd.equalsIgnoreCase("leave")
 				|| cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) {
 			main.cmdExecutor.urace(event.getPlayer(), new String[] { "leave" },
 					event.getPlayer());
 		} else if (cmd.equalsIgnoreCase("join")) {
 			String mode = ChatColor.stripColor(lines[3]);
 			if (mode.length() > 0) {
 				main.cmdExecutor.urace(event.getPlayer(), new String[] {
 					"join", ChatColor.stripColor(lines[2]).toLowerCase(),
 					mode }, event.getPlayer());
 			} else {
 				main.cmdExecutor.urace(event.getPlayer(), new String[] {
 					"join", ChatColor.stripColor(lines[2]).toLowerCase() },
 					event.getPlayer());
 			}
 		}
 		return;
 	}
 
 	@EventHandler
 	void signWriter(SignChangeEvent event) {
 		String[] lines = event.getLines();
 		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")) {
 			lines[0] = main.colors.getTitle() + "[MarioKart]";
 			Boolean text = true;
 			String cmd = ChatColor.stripColor(lines[1]);
 			if (cmd.equalsIgnoreCase("list")) {
 				lines[1] = main.colors.getInfo() + "List";
 				if (!(lines[2].length() < 1)) {
 					text = false;
 				}
 				lines[2] = main.colors.getSuccess()
 						+ ChatColor.stripColor(lines[2]);
 			} else if (cmd.equalsIgnoreCase("join")) {
 				lines[1] = main.colors.getInfo() + "Join";
 				lines[2] = main.colors.getSuccess()
 						+ ChatColor.stripColor(lines[2]);
 				if (lines[2].equalsIgnoreCase("auto")) {
 					lines[2] = main.colors.getTp() + "Auto";
 				}
 				lines[3] = main.colors.getInfo() + lines[3];
 				text = false;
 			} else if (cmd.equalsIgnoreCase("leave")
 					|| cmd.equalsIgnoreCase("exit")
 					|| cmd.equalsIgnoreCase("quit")) {
 				char[] raw = cmd.toCharArray();
 				if (raw.length > 1) {
 					String start = "" + raw[0];
 					start = start.toUpperCase();
 					String body = "";
 					for (int i = 1; i < raw.length; i++) {
 						body = body + raw[i];
 					}
 					body = body.toLowerCase();
 					cmd = start + body;
 				}
 				lines[1] = main.colors.getInfo() + cmd;
 			} else if (cmd.equalsIgnoreCase("items")) {
 				Location above = event.getBlock().getLocation().add(0, 1.4, 0);
 				EnderCrystal crystal = (EnderCrystal) above.getWorld()
 						.spawnEntity(above, EntityType.ENDER_CRYSTAL);
 				above.getBlock().setType(Material.COAL_BLOCK);
 				above.getBlock().getRelative(BlockFace.WEST)
 				.setType(Material.COAL_BLOCK);
 				above.getBlock().getRelative(BlockFace.NORTH)
 				.setType(Material.COAL_BLOCK);
 				above.getBlock().getRelative(BlockFace.NORTH_WEST)
 				.setType(Material.COAL_BLOCK);
 				crystal.setFireTicks(0);
 				crystal.setMetadata("race.pickup", new StatValue(true, plugin));
 				text = false;
 			} else {
 				text = false;
 			}
 			if (text) {
 				lines[2] = ChatColor.ITALIC + "Right click";
 				lines[3] = ChatColor.ITALIC + "to use";
 			}
 		}
 	}
 
 	@EventHandler
 	void crystalExplode(EntityExplodeEvent event) {
 		if (!(event.getEntity() instanceof EnderCrystal)) {
 			return;
 		}
 		Entity crystal = event.getEntity();
 		// if(crystal.hasMetadata("race.pickup")){
 		event.setCancelled(true);
 		event.setYield(0);
 		Location newL = crystal.getLocation();
 		Location signLoc = null;
 		if ((newL.add(0, -2.4, 0).getBlock().getState() instanceof Sign)) {
 			signLoc = newL.add(0, -2.4, 0);
 		} else {
 			return; // Let them destroy it
 		}
 		Location above = signLoc.add(0, 3.8, 0);
 		EnderCrystal newC = (EnderCrystal) above.getWorld().spawnEntity(above,
 				EntityType.ENDER_CRYSTAL);
 		above.getBlock().setType(Material.COAL_BLOCK);
 		above.getBlock().getRelative(BlockFace.WEST)
 		.setType(Material.COAL_BLOCK);
 		above.getBlock().getRelative(BlockFace.NORTH)
 		.setType(Material.COAL_BLOCK);
 		above.getBlock().getRelative(BlockFace.NORTH_WEST)
 		.setType(Material.COAL_BLOCK);
 		newC.setFireTicks(0);
 		newC.setMetadata("race.pickup", new StatValue(true, plugin));
 		// }
 
 		return;
 	}
 
 	public void spawnItemPickupBox(Location previous, Boolean force) {
 		Location newL = previous;
 		newL.getChunk(); //Load chunk
 		Location signLoc = null;
 		if ((newL.add(0, -2.4, 0).getBlock().getState() instanceof Sign) || force) {
 			signLoc = newL.add(0, -2.4, 0);
 		} else {
 			if(force){
 				double ll = newL.getY();
 				Boolean foundSign = false;
 				Boolean cancel = false;
 				while(!foundSign && !cancel){
 					if(ll < newL.getY() - 4){
 						cancel = true;
 					}
 					Location i = new Location(newL.getWorld(), newL.getX(), ll, newL.getZ());
 					if(i.getBlock().getState() instanceof Sign){
 						foundSign = true;
 						signLoc = i;
 					}
 				}
 				if(!foundSign){
 					return; // Let is be destroyed
 				}
 			}
 			else{
 			return; // Let them destroy it
 			}
 		}
 		Location above = signLoc.add(0, 3.8, 0);
 		EnderCrystal newC = (EnderCrystal) above.getWorld().spawnEntity(above,
 				EntityType.ENDER_CRYSTAL);
 		above.getBlock().setType(Material.COAL_BLOCK);
 		above.getBlock().getRelative(BlockFace.WEST)
 		.setType(Material.COAL_BLOCK);
 		above.getBlock().getRelative(BlockFace.NORTH)
 		.setType(Material.COAL_BLOCK);
 		above.getBlock().getRelative(BlockFace.NORTH_WEST)
 		.setType(Material.COAL_BLOCK);
 		newC.setFireTicks(0);
 		newC.setMetadata("race.pickup", new StatValue(true, plugin));
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	void stopCrystalFire(BlockIgniteEvent event) {
 		if (event.getCause() != IgniteCause.ENDER_CRYSTAL) {
 			return;
 		}
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	void playerFireProtection(EntityDamageEvent event) {
 		try {
 			if (event.getCause() != DamageCause.FIRE
 					&& event.getCause() != DamageCause.FIRE_TICK
 					&& event.getCause() != DamageCause.ENTITY_ATTACK) {
 				return;
 			}
 			if (!(event.getEntity() instanceof Player)) {
 				return;
 			}
 			if (!ucars.listener.inACar((Player) event.getEntity())) {
 				return;
 			}
 			if (plugin.raceMethods.inAGame(((Player) event.getEntity()), false) == null) {
 				return;
 			}
 			Player player = ((Player) event.getEntity());
 			player.addPotionEffect(new PotionEffect(
 					PotionEffectType.FIRE_RESISTANCE, 2, 100));
 			double health = 5;
 			try {
 				health = player.getHealth();
 			} catch (Exception e) {
 				health = Double.MAX_VALUE;
 			}
 			health = health + event.getDamage();
 			if (health > 20) {
 				health = 20;
 			}
 			player.setHealth(health);
 			player.setFireTicks(0);
 			event.setCancelled(true);
 			return;
 		} catch (Exception e) {
 			// Fire event error - Yes it happens
 			return;
 		}
 	}
 
 	@EventHandler
 	void carDeath(VehicleDamageEvent event) {
 		if (!(event.getVehicle() instanceof Minecart)) {
 			return;
 		}
 		if (!ucars.listener.isACar((Minecart) event.getVehicle())) {
 			return;
 		}
 		try {
 			if (plugin.raceMethods.inAGame(((Player) event.getVehicle()
 					.getPassenger()), false) == null) {
 				return;
 			}
 		} catch (Exception e) {
 			return;
 		}
 		if (!main.config.getBoolean("mariokart.enable")) {
 			return;
 		}
 		event.setDamage(0);
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler
 	void playerDeathEvent(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		Race r = plugin.raceMethods.inAGame(player, false);
 		if (r == null) {
 			return;
 		}
 		if (!(player.getVehicle() == null)) {
 			player.getVehicle().eject();
 			player.getVehicle().remove();
 		}
 		List<MetadataValue> metas = null;
 		if (player.hasMetadata("car.stayIn")) {
 			metas = player.getMetadata("car.stayIn");
 			for (MetadataValue val : metas) {
 				player.removeMetadata("car.stayIn", val.getOwningPlugin());
 			}
 		}
 		return;
 	}
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void playerRespawnEvent(PlayerRespawnEvent event) {
 		final Player player = event.getPlayer();
 		if (plugin.raceMethods.inAGame(player, false) == null) {
 			return;
 		}
 		Race race = plugin.raceMethods.inAGame(player, false);
 		int checkpoint = 0;
 		try {
 			User user = race.getUser(player);
 			
 			checkpoint = user.getCheckpoint();
 		} catch (Exception e) {
 		}
 		final Location loc = race.getTrack().getCheckpoints().get(checkpoint)
 				.getLocation(plugin.getServer()).add(0, 2, 0);
 		Chunk chunk = loc.getChunk();
         if(!chunk.isLoaded()){
             chunk.load(true);
         }
 		event.setRespawnLocation(loc);
 		return;
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void queueRespawns(PlayerRespawnEvent event){
 		Player player = event.getPlayer();
 		RaceQueue r = main.plugin.raceMethods.inGameQue(player);
 		if(r == null){
 			return;
 		}
 		event.setRespawnLocation(r.getTrack().getLobby(main.plugin.getServer()));
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.MONITOR)
 	void postRespawn(PlayerRespawnEvent event){
 		final Player player = event.getPlayer();
 		if (plugin.raceMethods.inAGame(player, true) == null) {
 			return;
 		}
 		Race race = plugin.raceMethods.inAGame(player, false);
 		race.updateUser(player);
 		int checkpoint = 0;
 		try {
 			User user = race.getUser(player);
 			
 			checkpoint = user.getCheckpoint();
 		} catch (Exception e) {
 		}
 		final Location loc = race.getTrack().getCheckpoints().get(checkpoint)
 				.getLocation(plugin.getServer()).add(0, 2, 0);
 		Chunk chunk = loc.getChunk();
         if(!chunk.isLoaded()){
             chunk.load(true);
         }
         if(player.getLocation().getChunk()!=chunk){
         	Location l = new Location(chunk.getWorld(), chunk.getX(), 90, chunk.getZ());
         	l.getChunk(); //Load the chunk
         	player.teleport(l);
         }
 		Minecart cart = (Minecart) loc.getWorld().spawnEntity(loc,
 				EntityType.MINECART);
 		cart.setMetadata("kart.racing", new StatValue(null, main.plugin));
 		cart.setPassenger(player);
 		player.setMetadata("car.stayIn", new StatValue(null, plugin));
 		updateHotBar(player);
 		player.updateInventory();
 		player.setScoreboard(race.board);
 		main.plugin.raceScheduler.updateRace(race);
 		return;
 	}
 
 	@EventHandler
 	void blockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.raceMethods.inAGame(player, false) == null) {
 			return;
 		}
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler
 	void blockPlace(BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.raceMethods.inAGame(player, false) == null) {
 			return;
 		}
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	void speedo(VehicleUpdateEvent event) {
 		Entity veh = event.getVehicle();
 		if (!(veh instanceof Minecart)) {
 			return;
 		}
 		if (!ucars.listener.isACar((Minecart) veh)) {
 			return;
 		}
 		Minecart car = (Minecart) veh;
 		Entity pass = car.getPassenger();
 		if (!(pass instanceof Player)) {
 			return;
 		}
 		Player player = (Player) pass;
 		if (plugin.raceMethods.inAGame(player, false) == null) {
 			return;
 		}
 		Vector Velocity = car.getVelocity();
 		double speed = (Math.abs(Velocity.getX()) + Math.abs(Velocity.getZ())) * 40;
 		if (speed < 1) {
 			speed = Velocity.getY();
 		}
 		if (speed > 100) {
 			speed = 100;
 		}
 		player.setLevel((int) speed);
 		float xpBar = (float) (speed / 100);
 		if (xpBar >= 1) {
 			xpBar = 0.999f;
 		}
 		player.setExp(xpBar);
 		return;
 	}
 	
 	@EventHandler
 	void raceFinish(MarioKartRaceFinishEvent event){
 		Player player = event.getPlayer();
 		main.plugin.hotBarManager.clearHotBar(player.getName());
 		if(!main.config.getBoolean("general.race.rewards.enable")){
 			return;
 		}
 		int pos = event.getFinishPosition();
 		double reward = 0;
 		switch(pos){
 		case 1:{
 			reward = main.config.getDouble("general.race.rewards.win");
 			break;
 		}
 		case 2:{
 			reward = main.config.getDouble("general.race.rewards.second");
 			break;
 		}
 		case 3:{
 			reward = main.config.getDouble("general.race.rewards.third");
 			break;
 		}
 		default: return;
 		}
 		if(reward <= 0){
 			return;
 		}
 		if(!main.vault || main.economy == null){
 			plugin.setupEconomy(); //Economy plugin loaded after MarioKart
 			if(!main.vault || main.economy == null){ //No Economy plugin installed
 			return;
 			}
 		}
 		EconomyResponse r = main.economy.depositPlayer(player.getName(), reward);
 		double b = r.balance;
 		String currency = main.config.getString("general.race.rewards.currency");
 		String msg = main.msgs.get("race.end.rewards");
 		msg = msg.replaceAll(Pattern.quote("%amount%"), Matcher.quoteReplacement(""+reward));
 		msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(""+b));
 		msg = msg.replaceAll(Pattern.quote("%currency%"), Matcher.quoteReplacement(""+currency));
 		msg = msg.replaceAll(Pattern.quote("%position%"), Matcher.quoteReplacement(""+event.getPlayerFriendlyPosition()));
 		player.sendMessage(main.colors.getInfo()+msg);
 		return;
 	}
 	@EventHandler(priority = EventPriority.LOWEST)
 	void pvp(EntityDamageEvent event){
 		if(event.getEntity() instanceof Player 
 				&& main.plugin.raceMethods.inAGame(((Player)event.getEntity()), false) != null
 				&& event.getCause() == DamageCause.ENTITY_ATTACK){
 			event.setDamage(0);
 			event.setCancelled(true);
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.LOWEST)
 	void pvp(EntityDamageByEntityEvent event){
 		if(event.getEntity() instanceof Player 
 				&& main.plugin.raceMethods.inAGame(((Player)event.getEntity()), false) != null){
 			event.setDamage(0);
 			event.setCancelled(true);
 		}
 		return;
 	}
 	@SuppressWarnings("deprecation")
 	public void updateHotBar(Player player){
 		MarioHotBar hotBar = main.plugin.hotBarManager.getHotBar(player.getName());
 		HotBarItem util = hotBar.getDisplayedItem(HotBarSlot.UTIL);
 		HotBarItem scroller = hotBar.getDisplayedItem(HotBarSlot.SCROLLER);
 		if(util != null){
 			player.getInventory().setItem(7, util.getDisplayItem());
 		}
 		else{
 			player.getInventory().setItem(7, new ItemStack(Material.AIR));
 		}
 		if(scroller != null){
 			player.getInventory().setItem(6, scroller.getDisplayItem());
 		}
 		else{
 			player.getInventory().setItem(6, new ItemStack(Material.AIR));
 		}
 		player.getInventory().setItem(8, main.marioKart.respawn);
 		player.updateInventory();
 		return;
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void hotBarClickEvent(MarioKartHotBarClickEvent event){
 		Player player = event.getPlayer();
 		MarioHotBar hotBar = event.getHotBar();
 		HotBarSlot slot = event.getHotBarSlot();
 		HotBarItem hotBarItem = hotBar.getDisplayedItem(slot);
 		Map<String, Object> data = hotBarItem.getData();
 		HotBarUpgrade type = hotBarItem.getType();
 		String upgradeName = "Unknown";
 		if(data.containsKey("upgrade.name")){
 			upgradeName = data.get("upgrade.name").toString();
 		}
 		if(type == HotBarUpgrade.LEAVE){
 			//Make the player leave the race
 			main.cmdExecutor.urace(player, new String[]{"leave"}, player);
 			return;
 		}
 		else if(type == HotBarUpgrade.SPEED_BOOST){
 			long lengthMS = 5000;
 			double power = 5;
 			Boolean useItem = true;
 			Boolean useUpgrade = false;
 			if(data.containsKey("upgrade.length")){
 				lengthMS = (long) data.get("upgrade.length");
 			}
 			if(data.containsKey("upgrade.power")){
 				power = (double) data.get("upgrade.power");
 			}
 			if(data.containsKey("upgrade.useItem")){
 				useItem = (Boolean) data.get("upgrade.useItem");
 			}
 			if(data.containsKey("upgrade.useUpgrade")){
 				useUpgrade = (Boolean) data.get("upgrade.useUpgrade");
 			}
 			Boolean execute = true;
 			if(useItem){
 				if(!hotBar.useItem(slot)){
 					execute = false;
 				}
 			}
 			if(execute){
 				ucars.listener.carBoost(player.getName(), power, lengthMS, ucars.config.getDouble("general.cars.defSpeed"));
 				//TODO Manage useUpgrade
 			}
 		}
 		player.updateInventory();
 	}
 	@EventHandler
 	public void hotBarScrolling(VehicleUpdateEvent event){
 		Vehicle car = event.getVehicle();
 		if(car.getPassenger() == null
 				|| !(car.getPassenger() instanceof Player)){
 			return;
 		}
 		final Player player = (Player) car.getPassenger();
 		if(car.hasMetadata("car.braking")
 				&& !player.hasMetadata("mariokart.slotChanging")
 				&& (player.getInventory().getHeldItemSlot() == 6
 				|| player.getInventory().getHeldItemSlot() == 7)){
 			MarioHotBar hotBar = main.plugin.hotBarManager.getHotBar(player.getName());
 			if(player.getInventory().getHeldItemSlot() == 6){
 				hotBar.scroll(HotBarSlot.SCROLLER);
 			}
 			else{
 				hotBar.scroll(HotBarSlot.UTIL);
 			}
 			player.setMetadata("mariokart.slotChanging", new StatValue(true, main.plugin));
 			main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){
 
 				@Override
 				public void run() {
 					player.removeMetadata("mariokart.slotChanging", main.plugin);
 				}}, 15);
 		}
 		updateHotBar(player);
 	}
 }
