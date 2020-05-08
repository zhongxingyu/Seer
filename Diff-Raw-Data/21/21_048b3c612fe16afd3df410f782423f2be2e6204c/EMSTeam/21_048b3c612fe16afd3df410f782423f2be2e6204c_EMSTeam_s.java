 package io.github.basicmark.ems;
 
 import io.github.basicmark.config.ConfigUtils;
 import io.github.basicmark.util.SpawnMethod;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.Player;
 
 public class EMSTeam implements ConfigurationSerializable {
 	// Static data
 	protected String name;
 	protected String displayName;
 	protected Location lobby;
 	protected List<Location> spawns;
 	protected SpawnMethod spawnMethod;
 	protected Location joinSignLocation;
 	protected int teamCap;
 	protected int forcedCap;
 	
 	// Run time data
 	/*
 	 *  Note:
 	 *  players can be accessed from multiple threads (server thread & client threads
 	 *  for chat message processing) and thus must use a thread safe set implementation
 	 */
 	private Set<Player> players;
 	private EMSArena arena;
 	private EMSTeamState teamState;
 
 	public EMSTeam(String name, String displayName, EMSArena arena) {
 		// Init all the "static" data to their default values
 		this.name = name;
 		this.displayName = displayName.replace('&', ChatColor.COLOR_CHAR);
 		this.lobby = null;
 		this.spawns = new ArrayList<Location>();
 		this.spawnMethod = SpawnMethod.ROUNDROBIN;
 		this.joinSignLocation = null;
 		this.teamCap = 0;
 		this.forcedCap = 0;
 
 		// Create the "run time" data which doesn't need to be saved
 		this.players = new HashSet<Player>();	//new ConcurrentSkipListSet<Player>();
 		this.arena = arena;
 		this.teamState = EMSTeamState.EDITING;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public EMSTeam(Map<String, Object> values, EMSArena arena) {
 		// Load all the "static" data from the data object
 		this.name = (String) values.get("name");
 		this.displayName = (String) values.get("displayname");
 		
 		// Get optional config data for team lobby
 		Map<String, Object> teamLobbyData;
 		try {
 			teamLobbyData = (Map<String, Object>) values.get("lobby");
 		} catch (Exception e) {
 			teamLobbyData = null;
 		}
 		this.lobby = ConfigUtils.DeserializeLocation(teamLobbyData);
 
 		// Get optional config data for team spawn(s)
 		int spawnCount = (int) values.get("spawncount");
 		this.spawns = new ArrayList<Location>();
 
 		for (int i=0;i<spawnCount;i++) {
 			this.spawns.add(ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("teamspawn"+i)));
 		}
 
 		// Get optional config data for join sign
 		Map<String, Object> joinSignData;
 		try {
 			joinSignData = (Map<String, Object>) values.get("joinsign");
 		} catch (Exception e) {
 			joinSignData = null;
 		}
 
 		this.joinSignLocation = ConfigUtils.DeserializeLocation(joinSignData);
 		if (this.joinSignLocation != null) {
 			arena.addProtection(joinSignLocation);
 		}
 		this.spawnMethod = SpawnMethod.fromString((String) values.get("spawnmethod"));
 		
 		try {
 			this.teamCap = (int) values.get("teamcap");
 		} catch (Exception e) {
 			this.teamCap = 0;
 		}
 
 		// Create the "run time" data which doesn't need to be saved
 		this.players = new HashSet<Player>();	//new ConcurrentSkipListSet<Player>();
 		this.arena = arena;
 		this.teamState = EMSTeamState.CLOSED;
 		this.forcedCap = 0;
 	}
 
 	@Override
 	public Map<String, Object> serialize() {
 		Map<String, Object> values = new LinkedHashMap<String, Object>();
 		values.put("name", name);
 		values.put("displayname", displayName);
 		if (lobby != null) {
 			values.put("lobby", ConfigUtils.SerializeLocation(lobby));
 		}
 		values.put("spawncount", spawns.size());
 
 		Iterator<Location> i = spawns.iterator();
 		int j=0;
 		while (i.hasNext()) {
 			Location loc = i.next();
 
 			values.put("teamspawn" + j, ConfigUtils.SerializeLocation(loc));
 			j++;
 		}
 
 		if (joinSignLocation != null) {
 			values.put("joinsign", ConfigUtils.SerializeLocation(joinSignLocation));
 		}
 		
 		values.put("spawnmethod", spawnMethod.toString().toLowerCase());
 		values.put("teamcap", teamCap);
 		
 		return values;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getDisplayName() {
 		return displayName;
 	}
 	
 	public void setLobby(Player player) {
 		lobby = player.getLocation();
 	}
 	
 	public void addSpawn(Player player) {
 		spawns.add(player.getLocation());
 	}
 	
 	public void clearSpawns() {
 		spawns.clear();
 	}
 	
 	public int getSpawnCount() {
 		return spawns.size();
 	}
 	
 	public boolean setSpawnMethod(String method) {
 		spawnMethod = SpawnMethod.fromString(method);
 		return spawnMethod != SpawnMethod.INVALID;
 	}
 	
 	public boolean setCap(int capSize) {
 		teamCap = capSize;
 		return true;
 	}
 	
 	public boolean setForceCap(int capSize) {
 		forcedCap = capSize;
 
 		/*
 		 * FIXME:?
 		 * We really should start kicking players if the cap is dropped below the number
 		 * the number of players already in the team. But drop who 1st ?!
 		 */
 		if ((forcedCap != 0) && (players.size() >= capSize)) {
 			teamState = EMSTeamState.FULL;
 		} else {
 			teamState = EMSTeamState.OPEN;
 		}
 		updateStatusSign();
 		return true;
 	}
 	
 	private int getGetCap() {
 		if (forcedCap != 0) {
 			return forcedCap;
 		}
 		return teamCap;
 	}
 	
 	public boolean setJoinSign(Location location) {
 		if (location != null) {
 			if (joinSignLocation != null) {
 				// Removing protection on the old location
 				arena.removeProtection(joinSignLocation);
 			}
 			// Adding the join sign so add it to the protected block list
 			arena.addProtection(location);
 			joinSignLocation = location;
 			updateStatusSign();
 		} else {
 			// Removing the join sign so remove it from the protected block list
 			if (joinSignLocation != null) {
 				arena.removeProtection(joinSignLocation);
 			}
 			joinSignLocation = null;
 		}
 		return false;
 	}
 	
 	public Location getJoinSign() {
 		return joinSignLocation;
 	}
 	
 	public boolean hasJoinSign() {
 		return (joinSignLocation != null);
 	}
 	
 	public void updateStatus(EMSArenaState arenaState) {
 		teamState = EMSTeamState.fromArenaState(arenaState);
 		updateStatusSign();
 	}
 
 	public void updateStatusSign() {
 		if (getJoinSign() == null) {
 			return;
 		}
 
 		Block block = getJoinSign().getBlock();
 
 		if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST)) {
 			BlockState state = block.getState();
 			Sign sign = (Sign) state;
 
 			sign.setLine(0, teamState.toColourString());
 			sign.setLine(1, arena.getName());
 
 			sign.setLine(2, getDisplayName());
 			if (getGetCap() == 0) {
 				sign.setLine(3, "[" + players.size() +"] players");
 			} else {
 				sign.setLine(3, "[" + players.size() + "/" + getGetCap() + "] players");
 			}
 			sign.update(true);	
 		} else {
 			// The sign has gone! Remove it from the team so we don't try again.
 			setJoinSign(null);
 		}
 		
 		// When an arena is closed its forced cap is cleared
 		if (teamState == EMSTeamState.CLOSED) {
 			forcedCap = 0;
 		}
 	}
 	
 	public void clickOnSign(Player player, Block block) {
 		if (joinSignLocation != null) {
 			if (joinSignLocation.equals(block.getLocation())) {
 				addPlayer(player);
 			}
 		}
 	}
 	
 	public void destroySign(Block block) {
 		if (joinSignLocation != null) {
 			if (joinSignLocation.equals(block.getLocation())) {
 				setJoinSign(null);
 			}
 		}
 	}
 	
 	public boolean hasLobby() {
 		return !(lobby == null);
 	}
 
 	public boolean hasSpawn() {
 		return !spawns.isEmpty();
 	}
 	
 	public boolean addPlayer(Player player) {
 		if ((getGetCap() == 0) || (players.size() < getGetCap())) {
 			players.add(player);
 			if (hasLobby()) {
				player.teleport(lobby);
 			}
 
 			if (players.size() == getGetCap()) {
 				teamState = EMSTeamState.FULL;
 			}
 			updateStatusSign();
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * This is where we do the work required to set the player into the correct
 	 * before and send them on their way
 	 */
 	
 	public void removePlayerDo(Player player) {
 		arena.sendToLobby(player);
 		if ((teamState == EMSTeamState.FULL) && (players.size() < getGetCap())) {
 			teamState = EMSTeamState.OPEN;
 		}
 	}
 	
 	public void removePlayer(Player player) {
 		players.remove(player);
 		removePlayerDo(player);
 		updateStatusSign();
 	}
 	
 	public void playerDeath(Player player) {
 		// The death runner in the arena will send the player to the lobby
 		players.remove(player);
 		if ((teamState == EMSTeamState.FULL) && (players.size() < getGetCap())) {
 			teamState = EMSTeamState.OPEN;
 		}
 		updateStatusSign();
 	}
 	
 	public void removeAllPlayers () {
 		Iterator<Player> i = players.iterator();
 
 		while(i.hasNext()) {
 			Player player = i.next();
 
 			i.remove();
 			removePlayerDo(player);
 		}
 		updateStatusSign();
 	}
 
 	public boolean isPlayerInTeam(Player player) {
 		return players.contains(player);
 	}
 	
 	public List<String> getPlayerList() {
 		List<String> data = new ArrayList<String>();
 
 		Iterator<Player> i = players.iterator();
 		while(i.hasNext()) {
 			Player player = i.next();
 
 			data.add(player.getName());
 		}
 		return data;
 	}
 	
 	public Set<Player> getPlayers() {
 		return players;
 	}
 	
 	public int getPlayerCount() {
 		return players.size();
 	}
 	
 	public void spawnPlayers() {
 		if (hasSpawn()) {
 			SpawnMethod.spawnPlayers(spawnMethod, players, spawns);
 		}
 	}
 	
 	public void broadcast(String message) {
 		Iterator<Player> ip = players.iterator();
 		while (ip.hasNext()) {
 			Player player = ip.next();
 			player.sendMessage(message);
 		}
 	}
 }
