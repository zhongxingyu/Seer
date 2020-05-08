 package net.slipcor.pvparena.arenas.domination;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.managers.Spawns;
 import net.slipcor.pvparena.managers.Teams;
 import net.slipcor.pvparena.neworder.ArenaType;
 import net.slipcor.pvparena.runnables.EndRunnable;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 
 public class Domination extends ArenaType {
 	private Debug db = new Debug(39);
 	/**
 	 * TeamName => PlayerName
 	 */
 	public HashMap<String, String> paTeamFlags = null;
 	public HashMap<Location, String> paFlags = null;
 	public HashMap<String, ItemStack> paHeadGears = null;
 
 	public HashMap<Location, DominationRunnable> paRuns = new HashMap<Location, DominationRunnable>();
 
 	public Domination() {
 		super("dom");
 	}
 	
 	@Override
 	public String version() {
		return "v0.8.10.0";
 	}
 	
 	@Override
 	public void addDefaultTeams(YamlConfiguration config) {
 		config.addDefault("game.woolHead", Boolean.valueOf(false));
 		if (arena.cfg.get("teams") == null) {
 			db.i("no teams defined, adding custom red and blue!");
 			arena.cfg.getYamlConfiguration().addDefault("teams.red",
 					ChatColor.RED.name());
 			arena.cfg.getYamlConfiguration().addDefault("teams.blue",
 					ChatColor.BLUE.name());
 		}
 	}
 
 	@Override
 	public boolean allowsJoinInBattle() {
 		return arena.cfg.getBoolean("join.inbattle");
 	}
 
 	@Override
 	public boolean checkAndCommit() {
 		db.i("[FLAG]");
 
 		ArenaPlayer activePlayer = null;
 		for (ArenaPlayer p : arena.getPlayers()) {
 			if (p.getStatus().equals(Status.FIGHT)) {
 				if (activePlayer != null) {
 					db.i("more than one player active => no end :p");
 					return false;
 				}
 				activePlayer = p;
 			}
 		}
 
 		if (activePlayer == null) {
 			commit("$%&/", true);
 			return false;
 		}
 
 		ArenaTeam team = Teams.getTeam(arena, activePlayer);
 
 		if (team == null) {
 			commit("$%&/", true);
 			return false;
 		}
 
 		commit(team.getName(), true);
 		return true;
 	}
 
 	/**
 	 * return a hashset of players names being near a specified location, except
 	 * one player
 	 * 
 	 * @param loc
 	 *            the location to check
 	 * @param distance
 	 *            the distance in blocks
 	 * @return a set of player names
 	 */
 	private HashSet<String> checkLocationPresentTeams(Location loc, int distance) {
 		HashSet<String> result = new HashSet<String>();
 
 		for (ArenaPlayer p : arena.getPlayers()) {
 
 			if (p.get().getLocation().distance(loc) > distance) {
 				continue;
 			}
 
 			result.add(Teams.getTeam(arena, p).getName());
 		}
 
 		return result;
 	}
 
 	protected void checkMove() {
 
 		/**
 		 * possible Situations
 		 * 
 		 * >>- flag is unclaimed and no one is there - flag is unclaimed and
 		 * team a is there - flag is unclaimed and multiple teams are there
 		 * 
 		 * >>- flag is being claimed by team a, no one is present - flag is
 		 * being claimed by team a, team a is present - flag is being claimed by
 		 * team a, multiple teams are present - flag is being claimed by team a,
 		 * team b is present
 		 * 
 		 * >>- flag is claimed by team a, no one is present >>- flag is claimed
 		 * by team a, team a is present >>- flag is claimed by team a, multiple
 		 * teams are present >>- flag is claimed by team a, team b is present
 		 * 
 		 * >>- flag is claimed by team a and being unclaimed, no one is present
 		 * >>- flag is claimed by team a and being unclaimed, team a is present
 		 * >>- flag is claimed by team a and being unclaimed, multiple teams are
 		 * present >>- flag is claimed by team a and being unclaimed, team b is
 		 * present
 		 * 
 		 */
 
 		db.i("------------------");
 		db.i("   checkMove();");
 		db.i("------------------");
 		
 		int checkDistance = 2;
 
 		for (Location loc : Spawns.getSpawns(arena, "flags")) {
 			//db.i("checking location: " + loc.toString());
 			
 			HashSet<String> teams = checkLocationPresentTeams(loc,
 					checkDistance);
 			
 			String sTeams = "teams: ";
 			
 			for (String team : teams) {
 				sTeams += ", " + team;
 			}
 
 			db.i(sTeams);
 
 			// teams now contains all teams near the flag
 
 			if (teams.size() < 1) {
 				//db.i("=> noone there!");
 				// no one there
 				if (paRuns.containsKey(loc)) {
 					db.i("flag is being (un)claimed! Cancelling!");
 					// cancel unclaiming/claiming if noone's near
 					Bukkit.getScheduler().cancelTask(paRuns.get(loc).ID);
 					paRuns.remove(loc);
 				}
 				continue;
 			}
 
 			// there are actually teams at the flag
 			db.i("=> at least one team is at the flag!");
 
 			if (paFlags.containsKey(loc)) {
 				db.i("- flag is taken");
 
 				// flag is taken. by whom?
 				if (teams.contains(paFlags.get(loc))) {
 					// owning team is there
 					db.i("  - owning team is there");
 					if (teams.size() > 1) {
 						// another team is there
 						db.i("    - and another one");
 						if (paRuns.containsKey(loc)) {
 							// it is being unclaimed
 							db.i("      - being unclaimed. continue!");
 						} else {
 							// unclaim
 							db.i("      - not being unclaimed. do it!");
 							DominationRunnable dr = new DominationRunnable(
 									arena, false, loc, "another team", this);
 							dr.ID = Bukkit.getScheduler()
 									.scheduleSyncRepeatingTask(PVPArena.instance,
 											dr, 10 * 20L, 10 * 20L);
 							paRuns.put(loc, dr);
 						}
 					} else {
 						// just the owning team is there
 						db.i("    - noone else");
 						if (paRuns.containsKey(loc)) {
 							db.i("      - being unclaimed. cancel!");
 							// it is being unclaimed
 							// cancel task!
 							Bukkit.getScheduler()
 									.cancelTask(paRuns.get(loc).ID);
 							paRuns.remove(loc);
 						}
 					}
 					continue;
 				}
 
 				db.i("  - owning team is not there!");
 				// owning team is NOT there ==> unclaim!
 
 				if (paRuns.containsKey(loc)) {
 					if (paRuns.get(loc).take) {
 						db.i("    - runnable is trying to score, abort");
 
 						Bukkit.getScheduler().cancelTask(paRuns.get(loc).ID);
 						paRuns.remove(loc);
 					} else {
 						db.i("    - being unclaimed. continue.");
 					}
 					continue;
 				}
 				db.i("    - not yet being unclaimed, do it!");
 				// create an unclaim runnable
 				DominationRunnable running = new DominationRunnable(arena,
 						false, loc, "", this);
 				long interval = 20L * 10;
 
 				Bukkit.getScheduler().scheduleSyncRepeatingTask(
 						PVPArena.instance, running, interval, interval);
 				paRuns.put(loc, running);
 			} else {
 				// flag not taken
 				db.i("- flag not taken");
 
 				/*
 				 * check if a runnable yes check if only that team yes =>
 				 * continue; no => cancel no check if only that team yes =>
 				 * create runnable; no => continue
 				 */
 				if (paRuns.containsKey(loc)) {
 					db.i("  - being claimed");
 					if (teams.size() < 2) {
 						db.i("  - only one team present");
 						if (teams.contains(paRuns.get(loc).team)) {
 							// just THE team that is claiming => NEXT
 							db.i("  - claiming team present. next!");
 							continue;
 						}
 					}
 					db.i("  - more than one team. cancel claim!");
 					// more than THE team that is claiming => cancel!
 					Bukkit.getScheduler().cancelTask(paRuns.get(loc).ID);
 				} else {
 					db.i("  - not being claimed");
 					// not being claimed
 					if (teams.size() < 2) {
 						db.i("  - just one team present");
 						for (String sName : teams) {
 							db.i("TEAM " + sName + " IS CLAIMING " + loc.toString());
 							ArenaTeam team = Teams.getTeam(arena, sName);
 							arena.tellEveryone(Language.parse("domclaiming",
 									team.colorize()));
 
 							DominationRunnable running = new DominationRunnable(
 									arena, true, loc, sName, this);
 							long interval = 20L * 10;
 							Bukkit.getScheduler().scheduleSyncRepeatingTask(
 									PVPArena.instance, running, interval, interval);
 							paRuns.put(loc, running);
 						}
 					} else {
 						db.i("  - more than one team present. continue!");
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean checkSetFlag(Player player, Block block) {
 		if (!block.getType().equals(Material.WOOL)) {
 			return false;
 		}
 		if ((PVPArena.hasAdminPerms(player) || (PVPArena.hasCreatePerms(player,
 				arena)))
 				&& (player.getItemInHand() != null)
 				&& (player.getItemInHand().getTypeId() == arena.cfg.getInt(
 						"setup.wand", 280))) {
 			HashSet<Location> flags = Spawns.getSpawns(arena, "flags");
 			if (flags.contains(block.getLocation())) {
 				return false;
 			}
 			Spawns.setCoords(arena, block.getLocation(), "flag" + flags.size());
 			Arenas.tellPlayer(player,
 					Language.parse("setflag", String.valueOf(flags.size())));
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String checkSpawns(Set<String> list) {
 		boolean contains = false;
 		for (String s : list) {
 			if (s.startsWith("flag")) {
 				contains = true;
 				break;
 			}
 		}
 		if (!contains) {
 			return "flags not set";
 		}
 		return super.checkSpawns(list);
 	}
 
 	private void commit(String sTeam, boolean win) {
 		win = !win;
 
 		db.i("[DOM] committing end: " + sTeam);
 		db.i("win: " + String.valueOf(win));
 
 		String winteam = sTeam;
 
 		for (ArenaTeam team : arena.getTeams()) {
 			if (team.getName().equals(sTeam) == win) {
 				continue;
 			}
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 
 				ap.losses++;
 				arena.tpPlayerToCoordName(ap.get(), "spectator");
 			}
 		}
 		for (ArenaTeam team : arena.getTeams()) {
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 				if (!ap.getStatus().equals(Status.FIGHT)) {
 					continue;
 				}
 				winteam = team.getName();
 				break;
 			}
 		}
 
 		if (Teams.getTeam(arena, winteam) != null) {
 			PVPArena.instance.getAmm().announceWinner(arena,
 					Language.parse("teamhaswon", "Team " + winteam));
 			arena.tellEveryone(Language.parse("teamhaswon",
 					Teams.getTeam(arena, winteam).getColor() + "Team "
 							+ winteam));
 		}
 
 		arena.lives.clear();
 		EndRunnable er = new EndRunnable(arena, arena.cfg.getInt("goal.endtimer"),0);
 		arena.REALEND_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
 				er, 20L, 20L);
 		er.setId(arena.REALEND_ID);
 	}
 
 	@Override
 	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) {
 			Language.parse("onlyplayers");
 			return;
 		}
 		
 		Player player = (Player) sender;
 		
 		if (!PVPArena.hasAdminPerms(player)
 				&& !(PVPArena.hasCreatePerms(player, arena))) {
 			Arenas.tellPlayer(player,
 					Language.parse("nopermto", Language.parse("admin")), arena);
 			return;
 		}
 		
 
 		if (args[0].startsWith("spawn") || args[0].equals("spawn")) {
 			Arenas.tellPlayer(sender, Language.parse("errorspawnfree", args[0]),
 					arena);
 			return;
 		}
 
 		if (args[0].contains("spawn")) {
 			String[] split = args[0].split("spawn");
 			String sName = split[0];
 			if (Teams.getTeam(arena, sName) == null) {
 				Arenas.tellPlayer(sender, Language.parse("arenateamunknown", sName), arena);
 				return;
 			}
 
 			Spawns.setCoords(arena, player, args[0]);
 			Arenas.tellPlayer(player, Language.parse("setspawn", sName), arena);
 			return;
 		}
 		
 		if (args[0].equals("lounge")) {
 			Arenas.tellPlayer(sender, Language.parse("errorloungefree", args[0]),
 					arena);
 			return;
 		}
 
 		if (args[0].contains("lounge")) {
 			String[] split = args[0].split("lounge");
 			String sName = split[0];
 			if (Teams.getTeam(arena, sName) == null) {
 				Arenas.tellPlayer(sender, Language.parse("arenateamunknown", sName), arena);
 				return;
 			}
 
 			Spawns.setCoords(arena, player, args[0]);
 			Arenas.tellPlayer(player, Language.parse("loungeset", sName), arena);
 			return;
 		}
 		
		String sName = args[0].replace("flag", "");

		boolean found = false;

		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (sName.startsWith(sTeam)) {
				found = true;
			}
		}

		if (!found) {
			return;
		}
 		if (Arena.regionmodify.startsWith(arena.name)) {
 			Arena.regionmodify = "";
 			Arenas.tellPlayer(player, Language.parse("tosetflagdone"));
 			return;
 		}
		Arena.regionmodify = arena.name + ":" + sName;
		Arenas.tellPlayer(player, Language.parse("tosetflag", sName));
 	}
 
 	protected short getFlagOverrideTeamShort(String team) {
 		if (arena.cfg.get("flagColors." + team) == null) {
 
 			return StringParser.getColorDataFromENUM(Teams.getTeam(arena, team)
 					.getColor().name());
 		}
 		return StringParser.getColorDataFromENUM(arena.cfg
 				.getString("flagColors." + team));
 	}
 
 	@Override
 	public int getLives(Player defender) {
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(defender);
 		ArenaTeam team = Teams.getTeam(arena, ap);
 		return arena.lives.get(team.getName());
 	}
 
 	@Override
 	public String guessSpawn(String place) {
 		// no exact match: assume we have multiple spawnpoints
 		HashMap<Integer, String> locs = new HashMap<Integer, String>();
 		int i = 0;
 
 		db.i("searching for team spawns");
 
 		HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
 				.getYamlConfiguration().getConfigurationSection("spawns")
 				.getValues(false);
 		for (String name : coords.keySet()) {
 			if (name.startsWith(place)) {
 				locs.put(i++, name);
 				db.i("found match: " + name);
 			}/*
 			if (name.endsWith("flag")) {
 				for (ArenaTeam team : arena.getTeams()) {
 					String sTeam = team.getName();
 					if (name.startsWith(sTeam)) {
 						locs.put(i++, name);
 						db.i("found match: " + name);
 					}
 				}
 			}*/
 		}
 
 		if (locs.size() < 1) {
 			return null;
 		}
 		Random r = new Random();
 
 		place = locs.get(r.nextInt(locs.size()));
 
 		return place;
 	}
 
 	@Override
 	public void initiate() {
		// TODO for all spawns
 		takeFlag(Spawns.getCoords(arena, "flag"));
 		paFlags = new HashMap<Location, String>();
 
 		arena.lives.clear();
 		for (ArenaTeam team : arena.getTeams()) {
 			if (team.getTeamMembers().size() > 0) {
 				db.i("adding team " + team.getName());
 				// team is active
 				arena.lives.put(team.getName(),
 						arena.cfg.getInt("game.lives", 3));
 			}
 		}
 		
 		DominationMainRunnable dmr = new DominationMainRunnable(arena, this);
 		dmr.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
 				PVPArena.instance, dmr, 3*20L, 3*20L);
 	}
 
 	@Override
 	public void initLanguage(YamlConfiguration config) {
 		config.addDefault("lang.killedby", "%1% has been killed by %2%!");
 		config.addDefault("lang.setflag", "Flag set: %1%");
 		config.addDefault("lang.tosetflagdone", "Exited flag set mode!");
 
 		config.addDefault("lang.domscore",
 				"Team %1% scored a point by holding a flag!");
 		config.addDefault("lang.domclaiming", "Team %1% is claiming a flag!");
 		config.addDefault("lang.domunclaiming",
 				"A flag claimed by Team %1% is being unclaimed!");
 		config.addDefault("lang.domunclaimingby",
 				"A flag claimed by Team %1% is being unclaimed by %2%!");
 		config.options().copyDefaults(true);
 	}
 
 	@Override
 	public boolean parseCommand(String cmd) {
 		return cmd.contains("flag");
 	}
 
 	@Override
 	public void parseRespawn(Player respawnPlayer, ArenaTeam respawnTeam,
 			int lives, DamageCause cause, Entity damager) {
 		arena.tellEveryone(Language.parse("killedby",
 				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
 				arena.parseDeathCause(respawnPlayer, cause, damager)));
 		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
 				+ "spawn");
 
 		checkEntityDeath(respawnPlayer);
 	}
 
 	@Override
 	public int reduceLives(Player player, int lives) {
 		return lives;
 	}
 
 	/**
 	 * [FLAG] take away one life of a team
 	 * 
 	 * @param team
 	 *            the team name to take away
 	 * @return
 	 */
 	public boolean reduceLivesCheckEndAndCommit(String team) {
 
 		db.i("reducing lives of team " + team);
 		if (arena.lives.get(team) != null) {
 			int i = arena.lives.get(team) - 1;
 			if (i > 0) {
 				arena.lives.put(team, i);
 			} else {
 				arena.lives.remove(team);
 				commit(team, false);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void reset(boolean force) {
 		if (paTeamFlags != null) {
 			paTeamFlags.clear();
 		}
 		if (paHeadGears != null) {
 			paHeadGears.clear();
 		}
 
		if (paRuns == null || paRuns.size() < 1) {
			return;
		}

 		for (DominationRunnable run : paRuns.values()) {
 			Bukkit.getScheduler().cancelTask(run.ID);
 		}
 		paRuns.clear();
 	}
 
 	private void takeFlag(Location lBlock) {
 		lBlock.getBlock().setData(StringParser.getColorDataFromENUM("WHITE"));
 	}
 
 	/**
 	 * hook into the timed end
 	 */
 	@Override
 	public void timed() {
 		int i;
 
 		int max = -1;
 		HashSet<String> result = new HashSet<String>();
 		db.i("timed end!");
 
 		for (String sTeam : arena.lives.keySet()) {
 			i = arena.lives.get(sTeam);
 
 			if (i > max) {
 				result = new HashSet<String>();
 				result.add(sTeam);
 				max = i;
 			} else if (i == max) {
 				result.add(sTeam);
 			}
 
 		}
 
 		for (ArenaTeam team : arena.getTeams()) {
 			if (result.contains(team.getName())) {
 				PVPArena.instance.getAmm().announceWinner(arena,
 						Language.parse("teamhaswon", "Team " + team.getName()));
 				arena.tellEveryone(Language.parse("teamhaswon", team.getColor()
 						+ "Team " + team.getName()));
 			}
 			for (ArenaPlayer p : arena.getPlayers()) {
 				if (!p.getStatus().equals(Status.FIGHT)) {
 					continue;
 				}
 				if (!result.contains(team.getName())) {
 					p.losses++;
 					arena.tpPlayerToCoordName(p.get(), "spectator");
 				}
 			}
 		}
 
 		PVPArena.instance.getAmm().timedEnd(arena, result);
 		EndRunnable er = new EndRunnable(arena, arena.cfg.getInt("goal.endtimer"),0);
 		arena.REALEND_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
 				er, 20L, 20L);
 		er.setId(arena.REALEND_ID);
 	}
 
 	@Override
 	public boolean usesFlags() {
 		return true;
 	}
 }
