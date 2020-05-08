 package me.asofold.bpl.archer.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.asofold.bpl.archer.Archer;
 import me.asofold.bpl.archer.config.compatlayer.CompatConfig;
 import me.asofold.bpl.archer.core.contest.HitResult;
 import me.asofold.bpl.archer.utils.Utils;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 /**
  * Central access point for all contests.<br>
  * Active contests are stored in PlayerData.
  * @author mc_dev
  *
  */
 public class ContestManager {
 	/** All contests by contest-name. */
 	protected final Map<String, Contest> contests = new LinkedHashMap<String, Contest>();
 	
 	/** TEMP: World to contests. */
 	protected final Map<String, Set<Contest>> worldMap = new HashMap<String, Set<Contest>>();
 	
 	/**
 	 * Get a contest by name, case-insensitive.
 	 * @param name
 	 * @return
 	 */
 	public Contest getContest(String name){
 		// TODO: Lower case ?
 		return contests.get(name.toLowerCase());
 	}
 	
 	/**
 	 * Get available contests for the current location the player is at (convenience method).
 	 * @param data
 	 * @return
 	 */
 	public Collection<Contest> getAvailableContests(PlayerData data){
 		return getAvailableContests(data, data.player == null ? null : data.player.getLocation());
 	}
 	
 	/**
 	 * Get all available contests for the player at the given location.<br>
 	 * TODO: Might do without the player ?
 	 * 
 	 * @param data
 	 * @param loc
 	 * @return
 	 */
 	public Collection<Contest> getAvailableContests(final PlayerData data, final Location loc){
 		final List<Contest> found = new LinkedList<Contest>();
 		if (loc == null){
 			// TODO: Error / policy ?
 			return found;
 		}
 		addAvailableContexts(data, loc, loc.getWorld().getName(), found);
 		addAvailableContexts(data, loc, "*", found);
 		return found;
 	}
 	
 	/**
 	 * Add available contexts for the given world name.
 	 * @param data
 	 * @param loc
 	 * @param worldName
 	 * @param collection
 	 * @return
 	 */
 	private <C extends Collection<Contest>> C addAvailableContexts(PlayerData data, Location loc, String worldName, C collection)
 	{
 		final Set<Contest> perWorld = worldMap.get(worldName.toLowerCase());
 		if (perWorld != null){
 			for (final Contest contest : perWorld){
 				if (contest.isAvailable(data, loc)){
 					collection.add(contest);
 				}
 			}
 		}
 		return collection;
 	}
 
 	public void fromConfig(CompatConfig cfg, String prefix){
 		// The keys are ignored, due to special characters.
 		for (String key : cfg.getStringKeys(prefix)){
 			Contest contest = new Contest(null, null);
 			contest.fromConfig(cfg, prefix + key + ".");
 			if (contest.name != null){
 				addContest(contest);
 			}
 			// TODO: else warn.
 		}
 	}
 
 	public void addContest(Contest contest) {
 		if (contest.world == null) contest.world = "*";
 		if (contest.owner == null) contest.owner = "";
 		contests.put(contest.name.toLowerCase(), contest);
 		String wKey = contest.world.toLowerCase();
 		Set<Contest> wContests = worldMap.get(wKey);
 		if (wContests == null){
 			wContests = new LinkedHashSet<Contest>();
 			worldMap.put(wKey, wContests);
 		}
 		wContests.remove(contest); // To remove old one with the same name.
 		wContests.add(contest);
 	}
 	
 	public void onPlayerChangedWorld(final PlayerData data, final Location to){
 		if (data.activeContests.isEmpty()) return;
 		final Iterator<Entry<String, ContestData>> it = data.activeContests.entrySet().iterator();
 		while (it.hasNext()){
 			final Entry<String, ContestData> entry = it.next();
 			if (entry.getValue().contest.onPlayerChangedWorld(data, to)){
 				it.remove();
 			}
 		}
 	}
 	
 	public void onPlayerLeaveServer(final PlayerData data){
 		if (data.activeContests.isEmpty()) return;
 		final Iterator<Entry<String, ContestData>> it = data.activeContests.entrySet().iterator();
 		while (it.hasNext()){
 			final Entry<String, ContestData> entry = it.next();
 			if (entry.getValue().contest.onPlayerLeaveServer(data)){
 				it.remove();
 			}
 		}
 	}
 	
 	public void onPlayerJoinServer(final PlayerData data){
 		if (data.activeContests.isEmpty()) return;
 		final Iterator<Entry<String, ContestData>> it = data.activeContests.entrySet().iterator();
 		while (it.hasNext()){
 			final Entry<String, ContestData> entry = it.next();
 			final ContestData cd = entry.getValue();
 			if (cd.contest.onPlayerJoinServer(data)){
 				it.remove();
 				Archer.send(data.player, ChatColor.YELLOW + "Contest ended: " + cd.contest.name);
 			}
 		}
 	}
 	
 	public void onPlayerDataExpire(final PlayerData data){
 		if (data.activeContests.isEmpty()) return;
 		for (final ContestData cd : data.activeContests.values()){
 			cd.contest.onPlayerDataExpire(data);
 		}
 	}
 
 	/**
 	 * Just remove data and contests, calls Contest.clear, no other side effects like unregistering ContestData.
 	 */
 	public void clear() {
 		for (final Contest contest : contests.values()){
 			contest.clear();
 		}
 		contests.clear();
 		worldMap.clear();
 	}
 	
 	/**
 	 * Re-checks availability. This adds a new ContestData to the player data becasue the contest-specific settings [might change and add a factory method to the contest].
 	 * @param data
 	 * @param contest
 	 * @return
 	 */
 	public boolean joinContest(final PlayerData data, final Location loc, final Contest contest){
 		// Use the stored one to be sure.
 		final Contest ref = getContest(contest.name);
 		if (ref == null || loc == null || !ref.isAvailable(data, loc)) return false;
 		else{
 			ref.addPlayer(data);
 			return true;
 		}
 	}
 	
 	/**
 	 * Removes the player. Does not alter the given PlayerData instance.
 	 * @param data
 	 * @param contest
 	 * @return If previously contained.
 	 */
 	public boolean leaveContest(final PlayerData data, final Contest contest){
 		return contest.removePlayer(data);
 	}
 
 	/**
 	 *  Remove the player (usually called after checking). Accesses activeContests but does not change given PlayerData.
 	 * @param data
 	 */
 	public void removePlayer(final PlayerData data) {
 		for (final ContestData cd : data.activeContests.values()){
 			cd.contest.removePlayer(data);
 		}
 	}
 
 	/**
 	 * Return values (no copy).
 	 * @return
 	 */
 	public Collection<Contest> getAllContests() {
 		return contests.values();
 	}
 
 	public void checkState(boolean notifyTime) {
 		for (final Contest contest : contests.values()){
 			contest.checkState();
 			if (notifyTime){
 				if (!contest.started && contest.startDelay.value > 0.0 && contest.lastTimeValid > 0){
 					final long time = System.currentTimeMillis();
 					final long timeDiff = time - contest.lastTimeValid;
 					if (timeDiff > 0 && timeDiff < contest.startDelay.value){
 						contest.notifyActive(Archer.msgStart + ChatColor.YELLOW + "Contest " + ChatColor.RED + contest.name + ChatColor.YELLOW + " starting in " + ((int) (contest.startDelay.value - timeDiff) / 1000) + " seconds (" + contest.activePlayers.size() + " players)...");
 					}
 				}
 			}
 		}
 	}
 
 	public void onProjectileHit(final PlayerData data, final LaunchSpec launchSpec, final Location hitLoc, final PlayerData damagedData)
 	{
 		if (!launchSpec.world.equals(hitLoc.getWorld().getName())){
 			return;
 		}
 		if (data.activeContests.isEmpty() || damagedData.activeContests.isEmpty()){
 			return;
 		}
 		final double distance = launchSpec.distance(hitLoc);
 		HitResult result = HitResult.NOT_HIT_FINISHED; // Only to see if there was any hit (!).
 		for (final ContestData cd : new ArrayList<ContestData>(data.activeContests.values())){
 			// (Use copy because of endContest calls.)
 			// TODO: Might remove if shots used up... 
 			final String key = cd.contest.name.toLowerCase();
 			if (!damagedData.activeContests.containsKey(key)) continue;
 			final HitResult thisResult = cd.contest.onHit(data, cd, distance, launchSpec.force, damagedData, damagedData.activeContests.get(key));
 			if (thisResult.hit && !result.hit){
 				// TODO: Make methods in Archer ?
 				Utils.sendMessage(data, Archer.msgStart + "Hit: " + ChatColor.GREEN + damagedData.playerName);
 				Utils.sendMessage(damagedData, Archer.msgStart + "Hit by: " + ChatColor.RED + data.playerName);
 			}
 			result = result.max(thisResult);
 			if (thisResult.finished){
 				data.activeContests.remove(key);
 				Archer.send(data.player, ChatColor.YELLOW + "Contest ended: " + cd.contest.name);
 			}
 		}
 	}
 	
 	public void endAllContests(final String message) {
 		for (final Contest contest : contests.values()){
 			contest.endContest(message);
 		}
 	}
 
 	/**
 	 * End and delete.
 	 * @param contest
 	 */
 	public void deleteContest(Contest contest) {
 		contest.endContest("Contest deleted.");
 		final String lcName = contest.name.toLowerCase();
 		contests.remove(lcName);
		worldMap.get(contest.world).remove(lcName);
 	}
 	
 	/**
 	 * End and delete.
 	 */
 	public void deleteAllContests() {
 		endAllContests("Contest deleted.");
 		clear();
 	}
 
 	/**
 	 * Removes and re-adds the contest.
 	 * @param contest
 	 * @param newWorld
 	 */
 	public void changeWorld(Contest contest, String newWorld) {
 		deleteContest(contest);
 		contest.world = newWorld;
 		addContest(contest);
 	}
 	
 }
