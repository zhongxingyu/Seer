 package net.slipcor.pvparena.modules.aftermatch;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.managers.Spawns;
 import net.slipcor.pvparena.neworder.ArenaModule;
 
 public class AfterMatch extends ArenaModule {
 	protected HashMap<Arena, Integer> runnables = new HashMap<Arena, Integer>();
 	private HashMap<Arena, Integer> playerCount = new HashMap<Arena, Integer>();
 
 	public AfterMatch() {
 		super("AfterMatch");
 	}
 
 	@Override
 	public String version() {
		return "v0.8.8.8";
 	}
 
 	public void afterMatch(Arena a) {
 		for (ArenaTeam t : a.getTeams()) {
 			for (ArenaPlayer p : t.getTeamMembers()) {
 				if (!p.getStatus().equals(Status.FIGHT)) {
 					continue;
 				}
 				Player player = p.get();
 				a.tpPlayerToCoordName(player, "after");
 			}
 		}
 		a.tellEveryone(Language.parse("aftermatch"));
 	}
 
 	public String checkSpawns(Set<String> list) {
 		for (String s : list) {
 			if (s.startsWith("after")) {
 				return null;
 			}
 		}
 		return "after not set";
 	}
 
 	@Override
 	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) {
 			Language.parse("onlyplayers");
 			return;
 		}
 		if (!args[0].startsWith("after")) {
 			return;
 		}
 
 		Player player = (Player) sender;
 
 		if (!PVPArena.hasAdminPerms(player)
 				&& !(PVPArena.hasCreatePerms(player, arena))) {
 			Arenas.tellPlayer(player,
 					Language.parse("nopermto", Language.parse("admin")), arena);
 			return;
 		}
 		Spawns.setCoords(arena, player, args[0]);
 		Arenas.tellPlayer(player, Language.parse("setspawn", args[0]));
 		return;
 	}
 
 	@Override
 	public void configParse(Arena arena, YamlConfiguration config, String type) {
 		String pu = config.getString("aftermatch.aftermatch", "off");
 
 		String[] ss = pu.split(":");
 		if (pu.startsWith("death")) {
 			playerCount.put(arena, Integer.parseInt(ss[1]));
 		} else if (pu.startsWith("time")) {
			int i = Integer.parseInt(ss[1]);
			playerCount.put(
					arena,
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							PVPArena.instance, new AfterRunnable(arena, this),
							i * 20L));
 		} else {
 			db.w("error activating aftermatch module");
 		}
 
 		config.addDefault("aftermatch.aftermatch", "off");
 		config.options().copyDefaults(true);
 	}
 
 	@Override
 	public HashSet<String> getAddedSpawns() {
 		HashSet<String> result = new HashSet<String>();
 
 		result.add("after");
 
 		return result;
 	}
 
 	public void initLanguage(YamlConfiguration config) {
 		config.addDefault("lang.aftermatch", "The aftermatch has begun!");
 	}
 
 	@Override
 	public boolean parseCommand(String cmd) {
 		return cmd.startsWith("after");
 	}
 
 	@Override
 	public void parseInfo(Arena arena, CommandSender player) {
 		player.sendMessage("");
		player.sendMessage("AfterMatch:f "
 				+ StringParser.colorVar(playerCount.containsKey(arena)
 						|| runnables.containsKey(arena))
 				+ "("
 				+ StringParser.colorVar(arena.cfg
 						.getString("aftermatch.aftermatch")) + ")");
 	}
 
 	@Override
 	public void reset(Arena arena, boolean force) {
 		if (runnables.containsKey(arena)) {
 			Bukkit.getScheduler().cancelTask(runnables.get(arena));
 			runnables.remove(arena);
 		}
 		if (playerCount.containsKey(arena)) {
 			Bukkit.getScheduler().cancelTask(playerCount.get(arena));
 			playerCount.remove(arena);
 		}
 	}
 
 	@Override
 	public void teleportAllToSpawn(Arena arena) {
 		if (runnables.containsKey(arena)) {
 			int i = 0;
			String pu = arena.cfg.getString("game.powerups", "off");
 			String[] ss = pu.split(":");
 			if (pu.startsWith("time")) {
 				// arena.powerupTrigger = "time";
 				i = Integer.parseInt(ss[1]);
 			} else {
 				return;
 			}
 
			db.i("using powerups : "
					+ arena.cfg.getString("game.powerups", "off") + " : " + i);
 			if (i > 0) {
				db.i("powerup time trigger!");
 				Bukkit.getScheduler().cancelTask(runnables.get(arena));
 				playerCount.put(
 						arena,
 						Bukkit.getScheduler().scheduleSyncDelayedTask(
 								PVPArena.instance,
 								new AfterRunnable(arena, this), i * 20L));
 			}
 		}
 	}
 }
