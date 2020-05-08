 /**
  * Copyright (C) 2012 t7seven7t
  */
 package net.t7seven7t.swornguard.types;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import lombok.AccessLevel;
 import lombok.Data;
 import lombok.Setter;
 import net.t7seven7t.swornguard.detectors.SpamDetector;
 
 import org.bukkit.Location;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 /**
  * @author t7seven7t
  */
 @Data
 public class PlayerData implements ConfigurationSerializable {
 	private long firstLogin;
 	private long lastOnline;
 	
 	private List<String> ipAddressList = new ArrayList<String>();
 	private List<String> profilerList = new ArrayList<String>();
 	private List<String> factionLog = new ArrayList<String>();
 	
 	private transient int consecutivePings;
 	private transient int patrolInterval;
 
 	private int logins;
 	private int blocksDeleted;
 	private int blocksBuilt;
 	private int messages;
 	private int animalKills;
 	private int monsterKills;
 	private int playerKills;
 	private int deaths;
 	private int kicks;
 	private int jails;
 	private int bans;
 	private int playersKicked;
 	private int playersBanned;
 	private int playersJailed;
 	private int reportsRespondedTo;
 	private int patrols;
 	private int stoneMined;
 	private int diamondMined;
 	private int ironMined;
 	private int factions;
 	
 	private transient long lastActivity;
 	private transient long lastAttacked;
 	private transient long lastMonsterKill;
 	private transient long lastPlayerKill;
 	private transient long lastAnimalKill;
 	private transient long lastXrayWarn;
 	private transient long lastFlyWarn;
 	private transient long lastSpamWarn;
 	private transient long lastTeleport;
 	private transient long lastUpdateTimeSpent;
 	private long lastKick;
 	private long lastJail;
 	private long lastBan;
 	private long lastUnban;
 	private long onlineTime;
 	private long jailTime;
 	
 	private transient boolean patrolling;
 	private transient boolean cooldownPatrolling;
 	private transient boolean inspecting;
 	private transient boolean vanished;
 	private transient boolean fireworking; // hehe
 	private boolean jailed;
 	private boolean jailMuted;
 	private boolean unjailNextLogin;
 	private boolean trollHell;
 	
 	private transient Location previousLocation;
 	
 	private transient SpamDetector spamManager;
 	
 	private String lastJailer;
 	private String lastJailReason;
 	private String lastBanner;
 	private String lastBanReason;
 	private String lastUnbanner;
 	private String lastUnbanReason;
 	private String lastKicker;
 	private String lastKickReason;
 	private String lastFaction;
 	
	@Setter(AccessLevel.NONE) private Map<String, Object> data = new HashMap<String, Object>();
 	
 	public void updateSpentTime() {
 		long now = System.currentTimeMillis();
 		onlineTime = onlineTime + (now - ((lastUpdateTimeSpent > lastOnline) ? lastUpdateTimeSpent : lastOnline));
 		lastUpdateTimeSpent = now;
 	}
 	
 	public PlayerData() {
 		
 	}
 	
 	public PlayerData(Map<String, Object> args) {
 		for (Entry<String, Object> entry : args.entrySet()) {
 			try {
 				for (Field field : getClass().getDeclaredFields()) {
 					if (field.getName().equals(entry.getKey())) {
 						boolean accessible = field.isAccessible();
						if (!accessible)
							field.setAccessible(true);
 												
 						field.set(this, entry.getValue());
												
						if (!accessible)
							field.setAccessible(false);
 					}
 				}
 			} catch (Throwable ex) {
 			}
 		}
 	}
 	
 	/**
 	 * Any data put into this map needs to be inherently serializable, 
 	 * either using ConfigurationSerializable or being a java primitive.
 	 * @param key Key to store the object under
 	 * @param object Object to store.
 	 */
 	public void putData(String key, Object object) {
 		data.put(key, object);
 	}
 
 	public boolean removeData(String key) {
 		return data.remove(key) != null;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Map<String, Object> serialize() {
 		
 		
 		Map<String, Object> data = new HashMap<String, Object>();
 		
 		for (Field field : getClass().getDeclaredFields()) {
 			if (Modifier.isTransient(field.getModifiers()))
 				continue;
 			
 			try {
 				boolean accessible = field.isAccessible();
 				
 				if (!accessible)
 					field.setAccessible(true);
 				
 				if (field.getType().equals(Integer.TYPE)) {
 					if (field.getInt(this) != 0)
 						data.put(field.getName(), field.getInt(this));
 				} else if (field.getType().equals(Long.TYPE)) {
 					if (field.getLong(this) != 0)
 						data.put(field.getName(), field.getLong(this));
 				} else if (field.getType().equals(Boolean.TYPE)) {
 					if (field.getBoolean(this))
 						data.put(field.getName(), field.getBoolean(this));
 				} else if (field.getType().isAssignableFrom(Collection.class)) {
 					if (!((Collection) field.get(this)).isEmpty())
 						data.put(field.getName(), field.get(this));
 				} else if (field.getType().isAssignableFrom(String.class)) {
 					if (((String) field.get(this)) != null)
 						data.put(field.getName(), field.get(this));
 				} else if (field.getType().isAssignableFrom(Map.class)) {
 					if (!((Map) field.get(this)).isEmpty())
 						data.put(field.getName(), field.get(this));
 				} else {
 					if (field.get(this) != null)
 						data.put(field.getName(), field.get(this));
 				}
 								
 				if (!accessible)
 					field.setAccessible(false);
 				
 			} catch (Throwable ex) {
 			}
 		}
 		
 		return data;
 	}
 }
