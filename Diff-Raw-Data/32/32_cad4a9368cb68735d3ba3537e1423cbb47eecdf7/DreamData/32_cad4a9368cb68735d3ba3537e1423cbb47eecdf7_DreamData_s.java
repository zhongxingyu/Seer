 package de.blablubbabc.dreamworld.objects;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 
 public class DreamData implements ConfigurationSerializable {
 	private final long startTime;
 	private int remainingSeconds;
 	
	// can be null (!)
 	private PlayerDataStore playerData;
 	
 	public DreamData(PlayerDataStore playerData, long startTime, int remainingSeconds) {
 		this.playerData = playerData;
 		this.startTime = startTime;
 		this.remainingSeconds = remainingSeconds;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public DreamData(String playerName, Map<String, Object> map) {
		this(map.containsKey("player data") ? new PlayerDataStore(playerName, (Map<String, Object>) map.get("player data")) : null, (Long) map.get("start time"), (Integer) map.get("remaining seconds"));
 	}
 	
	// can be null (!)// an be 
 	public PlayerDataStore getPlayerData() {
 		return playerData;
 	}
 	
 	public void setPlayerData(PlayerDataStore newPlayerData) {
 		this.playerData = newPlayerData;
 	}
 	
 	public long getStartTime() {
 		return startTime;
 	}
 	
 	public int getRemainingSeconds() {
 		return remainingSeconds;
 	}
 	
 	public void decreaseRemainingSeconds() {
 		remainingSeconds -= 1;
 	}
 
 	@Override
 	public Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		
 		map.put("start time", new Long(startTime));
 		map.put("remaining seconds", new Integer(remainingSeconds));
		if (playerData != null) map.put("player data", playerData.serialize());
 		
 		return map;
 	}
 }
