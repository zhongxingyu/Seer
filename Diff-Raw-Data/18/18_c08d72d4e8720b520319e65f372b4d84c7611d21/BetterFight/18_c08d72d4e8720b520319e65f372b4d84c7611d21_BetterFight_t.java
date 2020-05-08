 package net.slipcor.pvparena.modules.betterfight;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.commands.AbstractArenaCommand;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 public class BetterFight extends ArenaModule {
 	
 	Map<String, Integer> killMap = null;
 	
 	public BetterFight() {
 		super("BetterFight");
 	}
 	
 	@Override
 	public String version() {
		return "v1.0.3.164 - various fixes";
 	}
 	
 	@Override
 	public boolean checkCommand(String s) {
 		return s.equals("!bf") || s.equals("betterfight");
 	}
 	
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		// !bf messages # 
 		// !bf items [items]
 		// !bf reset
 		// !bf explode
 		
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(
 					sender,
 					Language.parse(MSG.ERROR_NOPERM,
 							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 		
 		if (args[0].equals("!bf") || args[0].equals("betterfight")) {
 			if (args.length == 2) {
 				if (args[1].equals("reset")) {
 					boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH);
 					
 					arena.getArenaConfig().set(CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH, !b);
 					arena.getArenaConfig().save();
 					arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH.getNode(), String.valueOf(!b)));
 					return;
 				} else if (args[1].equals("explode")) {
 					boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_EXPLODEONDEATH);
 					
 					arena.getArenaConfig().set(CFG.MODULES_BETTERFIGHT_EXPLODEONDEATH, !b);
 					arena.getArenaConfig().save();
 					arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_BETTERFIGHT_EXPLODEONDEATH.getNode(), String.valueOf(!b)));
 					return;
 				}
 				arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "reset | explode"));
 				return;
 			}
 			if (args[1].equals("items")) {
 
 				if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 3 })) {
 					return;
 				}
 				
 				arena.getArenaConfig().set(CFG.MODULES_BETTERFIGHT_ONEHITITEMS, args[2]);
 				arena.getArenaConfig().save();
 				arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_BETTERFIGHT_ONEHITITEMS.getNode(), args[2]));
 				return;
 				
 			} else if (args[1].equals("messages")) {
 				int i = 0;
 				try {
 					i = Integer.parseInt(args[2]);
 				} catch (Exception e) {
 					arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
 					return;
 				}
 				String value = StringParser.joinArray(StringParser.shiftArrayBy(args, 2), " ");
 				arena.getArenaConfig().setManually("betterfight.messages.m" + i,
 						value);
 				arena.getArenaConfig().save();
 				arena.msg(sender, Language.parse(MSG.SET_DONE, "betterfight.messages.m" + i, value));
 				return;
 			}
 			
 			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "reset | items | messages | explode"));
 			return;
 		}
 	}
 	
 	@Override
 	public void configParse(YamlConfiguration config) {
 
 		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_MESSAGES)) {
 			config.addDefault("betterfight.messages.m1", "First Kill!");
 			config.addDefault("betterfight.messages.m2", "Double Kill!");
 			config.addDefault("betterfight.messages.m3", "Triple Kill!");
 			config.addDefault("betterfight.messages.m4", "Quadra Kill!");
 			config.addDefault("betterfight.messages.m5", "Super Kill!");
 			config.addDefault("betterfight.messages.m6", "Ultra Kill!");
 			config.addDefault("betterfight.messages.m7", "Godlike!");
 			config.addDefault("betterfight.messages.m8", "Monster!");
 		}
 
 		config.addDefault("betterfight.sounds.arrow", "none");
 		config.addDefault("betterfight.sounds.egg", "none");
 		config.addDefault("betterfight.sounds.snow", "none");
 		config.addDefault("betterfight.sounds.fireball", "none");
 		
 		config.options().copyDefaults(true);
 	}
 	
 	@Override
 	public void displayInfo(CommandSender sender) {
 		
 		sender.sendMessage("one-hit items: " + arena.getArenaConfig().getString(
 				CFG.MODULES_BETTERFIGHT_ONEHITITEMS));
 		sender.sendMessage(StringParser.colorVar("explode",
 				arena.getArenaConfig().getBoolean(
 						CFG.MODULES_BETTERFIGHT_EXPLODEONDEATH)) + " | " +
 				StringParser.colorVar("messages",
 						arena.getArenaConfig().getBoolean(
 								CFG.MODULES_BETTERFIGHT_MESSAGES)) + " | " +
 				StringParser.colorVar("reset on death",
 						arena.getArenaConfig().getBoolean(
 								CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH)));
 		
 	}
 	
 	private Map<String, Integer> getKills() {
 		if (killMap == null) {
 			killMap = new HashMap<String, Integer>();
 		}
 		return killMap;
 	}
 	
 	@Override
 	public void onEntityDamageByEntity(Player attacker,
 			Player defender, EntityDamageByEntityEvent event) {
 		String s = arena.getArenaConfig().getString(CFG.MODULES_BETTERFIGHT_ONEHITITEMS);
 		if (s.equalsIgnoreCase("none")) {
 			return;
 		}
 		
 		if (event.getDamager() instanceof Projectile) {
 			if (event.getDamager() instanceof Snowball) {
 				handle(event, "snow");
 				if (s.toLowerCase().contains("snow")) {
 					event.setDamage(1000);
 				}
 			}
 			if (event.getDamager() instanceof Arrow) {
 				handle(event, "arrow");
 				if (s.toLowerCase().contains("arrow")) {
 					event.setDamage(1000);
 				}
 			}
 			if (event.getDamager() instanceof Fireball) {
 				handle(event, "fireball");
 				if (s.toLowerCase().contains("fireball")) {
 					event.setDamage(1000);
 				}
 			}
 			if (event.getDamager() instanceof Egg) {
 				handle(event, "egg");
 				if (s.toLowerCase().contains("egg")) {
 					event.setDamage(1000);
 				}
 			}
 		}
 	}
 	
 	private void handle(EntityDamageByEntityEvent event, String string) {
 		if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
 			Player shooter = (Player) ((Projectile) event.getDamager()).getShooter();
 			
 			float volume = 1.0f;
 			float pitch = 1.0f;
 			
 			String node = "betterfight.sounds." + string;
 			
 			String value = (String) arena.getArenaConfig().getUnsafe(node);
 			
 			if (value.equals("none")) {
 				return;
 			}
 			
 			try {
 				
 				Sound sound = Sound.valueOf(value.toUpperCase());
 				
 				shooter.playSound(shooter.getLocation(), sound, volume, pitch);
 				if (event.getEntity() instanceof Player) {
 					Player damagee = (Player) event.getEntity();
 					
 					damagee.playSound(shooter.getLocation(), sound, volume, pitch);
 				}
 			} catch (Exception e) {
 				PVPArena.instance.getLogger().warning("Node " + node + " is not a valid sound in arena " + arena.getName());
 			}
 		}
 	}
 
 /*
 	@Override
 	public void parseRespawn(Player player, ArenaTeam team,
 			DamageCause cause, Entity damager) {
 		
 		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_MESSAGES)) {
 			return;
 		}
 		
 		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH)) {
 			kills.put(player.getName(), 0);
 		}
 		
 		if (! (damager instanceof Player)) {
 			return;
 		}
 		Player p = (Player) damager;
 		if (p == null || kills.get(p.getName()) == null) {
 			return;
 		}
 		int killcount = kills.get(p.getName());
 
 		kills.put(player.getName(), ++killcount);
 		
 		String msg = (String) arena.getArenaConfig().getUnsafe("betterfight.messages.m"+killcount);
 		
 		if (msg == null || msg.equals("")) {
 			return;
 		}
 		
 		arena.broadcast(msg);
 	}*/
 	
 	@Override
 	public void parsePlayerDeath(Player player,
 			EntityDamageEvent cause) {
		Player p = ArenaPlayer.getLastDamagingPlayer(cause, player);
 		
 		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH)) {
 			getKills().put(player.getName(), 0);
 		}
 		
 		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_EXPLODEONDEATH)) {
 			
 			class RunLater implements Runnable {
 				final Location l;
 				public RunLater(Location loc) {
 					l = loc;
 				}
 				@Override
 				public void run() {
 					l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 2f);
 				}
 				
 			}
 			Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new RunLater(player.getLocation().clone()), 2L);
 		}
 		
 		if (p == null || getKills().get(p.getName()) == null) {
 			return;
 		}
 		int killcount = getKills().get(p.getName());
 
 		getKills().put(p.getName(), ++killcount);
 		
 		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_MESSAGES)) {
 			return;
 		}
 		
 		String msg = (String) arena.getArenaConfig().getUnsafe("betterfight.messages.m"+killcount);
 		
 		if (msg == null || msg.equals("")) {
 			return;
 		}
 		
 		arena.broadcast(msg);
 	}
 	
 	@Override
 	public void parseStart() {
 		for (ArenaTeam team : arena.getTeams()) {
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 				getKills().put(ap.getName(), 0);
 			}
 		}
 	}
 	
 	@Override
 	public void reset(boolean force) {
 		getKills().clear();
 	}
 }
