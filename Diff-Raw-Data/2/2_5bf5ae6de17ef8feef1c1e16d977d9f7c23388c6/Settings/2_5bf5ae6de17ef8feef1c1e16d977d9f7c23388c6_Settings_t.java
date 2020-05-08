 package me.asofold.bukkit.fattnt.config;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.fattnt.stats.Stats;
 
 import org.bukkit.entity.EntityType;
 
 /**
  * Settings for FatTnt.
  * 
  * See class Defaults for default settings and application of those.
  * @author mc_dev
  *
  */
 public class Settings {
 	
 	private final Map<String, Map<EntityType, ExplosionSettings>> cache = new HashMap<String, Map<EntityType,ExplosionSettings>>();
 	
 	public final Stats stats;
 	
 	/**
 	 * This is an override.
 	 */
 	private boolean handleExplosions = true;
 	
 	/**
 	 * This is an override.
 	 */
 	private boolean preventExplosions = false;
 	
 	// World dependent settings:
 	
 	/**
 	 * Default settings for all worlds.
 	 */
 	private WorldSettings defaultWorldSettings = new WorldSettings();
 	
 	/**
 	 * World name (lower-case) to WorldSettings.
 	 */
 	private final Map<String, WorldSettings> worldSettings = new HashMap<String, WorldSettings>();
 	
 	/**
 	 * Absolute maximum.
 	 */
 	private float maxRadius = 0;
 
 	/**
 	 * NOTES:<br>
 	 * - Constructor does not initialize arrays !<br>
 	 * - Before using applyConfig you need to add defaults to ensure all paths are there. 
 	 * @param stats Are passed with settings, currently, to use the same stats object.
 	 */
 	public Settings(Stats stats){
 		this.stats = stats;
 	}
 	
 	public void applyConfig(CompatConfig cfg){
 		cache.clear();
 		// world settings:
 		defaultWorldSettings = new WorldSettings();
 		defaultWorldSettings.fromConfig(cfg, "");
 		worldSettings.clear();
 		List<String> worlds = cfg.getStringKeys(Path.worldSettings);
 		for (String world : worlds){
 			WorldSettings ws = new WorldSettings();
 			ws.fromConfig(cfg, Path.worldSettings + Path.sep + world + Path.sep);
 			if (ws.hasValues()) worldSettings.put(world.trim().toLowerCase(), ws);
 		}
 		
 		setMaxRadius();
 	}
 
 	/**
 	 * Set handleExplosions override.
 	 * @param handle
 	 */
 	public void setHandleExplosions(boolean handle){
 		handleExplosions = handle;
 	}
 	
 	/**
 	 * Usually not needed to call after applyConfig was called.
 	 */
 	public void setMaxRadius() {
 		maxRadius = defaultWorldSettings.getMaxRadius();
 		for (WorldSettings ws : worldSettings.values()){
 			maxRadius = Math.max(maxRadius, ws.getMaxRadius());
 		}
 	}
 	
 	/**
 	 * Get maximal maxRadius of all possible settings. 
 	 * @return
 	 */
 	public float getMaxRadius(){
 		return maxRadius;
 	}
 	
 	public ExplosionSettings getApplicableExplosionSettings(String worldName, EntityType type){
 		ExplosionSettings out = getCacheEntry(worldName, type);
 		if (out != null) return out;
 		out = new ExplosionSettings(Integer.MIN_VALUE);
 		out.applySettings(Defaults.defaultExplosionSettings);
 		defaultWorldSettings.applyExplosionSettings(out, type);
 		WorldSettings ref = worldSettings.get(worldName.trim().toLowerCase());
 		if (ref != null){
 			ref.applyExplosionSettings(out, type);
 		}
 		setCacheEntry(worldName, type, out);
 		return out;
 	}
 
 	/**
 	 * Quicker check just for if is handled.
 	 * @param name
 	 * @param type
 	 * @return
 	 */
 	public final boolean handlesExplosions(final String worldName, final EntityType type) {
 		if (!handleExplosions) return false;
 		// adds to the cache, could be abused to query chicken.
 		final ExplosionSettings settings = getApplicableExplosionSettings(worldName, type);
 		return settings.handleExplosions;
 	}
 	
 	private ExplosionSettings getCacheEntry(String worldName, EntityType type){
 		Map<EntityType, ExplosionSettings> map = cache.get(worldName.toLowerCase());
 		if (map == null) return null;
 		return map.get(type);
 	}
 	
 	private void setCacheEntry(String worldName, EntityType type, ExplosionSettings settings){
 		String lcwn = worldName.toLowerCase();
 		Map<EntityType, ExplosionSettings> map = cache.get(lcwn);
 		if (map == null){
 			map = new HashMap<EntityType, ExplosionSettings>();
 			cache.put(lcwn, map);
 		}
 		map.put(type, settings);
 	}
 
 	public void setPreventExplosions(boolean prevent) {
		preventExplosions = prevent;
 	}
 	
 	public boolean preventsExplosions(String worldName, EntityType type){
 		// TODO: query settings / cache etc.
 		return preventExplosions; // global override.
 	}
 
 }
