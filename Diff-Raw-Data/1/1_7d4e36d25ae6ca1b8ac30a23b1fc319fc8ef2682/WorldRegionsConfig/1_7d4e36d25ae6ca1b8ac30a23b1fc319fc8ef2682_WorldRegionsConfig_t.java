 package com.wolflink289.bukkit.worldregions;
 
 import java.io.File;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class WorldRegionsConfig {
 	
 	private YamlConfiguration cfg;
 	
 	public WorldRegionsConfig(File file) {
 		// Load
 		cfg = new YamlConfiguration();
 		
 		try {
 			cfg.load(file);
 		} catch (Exception ex) {}
 		
 		// Defaults
 		defaults();
 		
 		// Save
 		try {
 			cfg.save(file);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		// Read
 		ENABLE_FLY = cfg.getBoolean("flag.fly.enabled");
 		ENABLE_APPLY_POTION = cfg.getBoolean("flag.apply-potion.enabled");
 		ENABLE_HEALING = cfg.getBoolean("flag.healing.enabled");
 		ENABLE_HUNGER = cfg.getBoolean("flag.hunger.enabled");
 		ENABLE_ITEM_SPAWN = cfg.getBoolean("flag.item-spawn.enabled");
 		ENABLE_MOB_TARGETING = cfg.getBoolean("flag.mob-targeting.enabled");
 		ENABLE_PVE = cfg.getBoolean("flag.pve.enabled");
 		ENABLE_REGEN = cfg.getBoolean("flag.regen.enabled");
 		ENABLE_ZOMBIE_DOOR_BREAK = cfg.getBoolean("flag.zombie-door-break.enabled");
 		ENABLE_BLOCKED_BREAK = cfg.getBoolean("flag.blocked-break.enabled");
 		ENABLE_BLOCKED_PLACE = cfg.getBoolean("flag.blocked-place.enabled");
 		ENABLE_ALLOWED_BREAK = cfg.getBoolean("flag.allowed-break.enabled");
 		ENABLE_ALLOWED_PLACE = cfg.getBoolean("flag.allowed-place.enabled");
 		ENABLE_ITEM_PICKUP = cfg.getBoolean("flag.item-pickup.enabled");
 		ENABLE_INSTABREAK = cfg.getBoolean("flag.instabreak.enabled");
 		ENABLE_ALLOWED_DAMAGE = cfg.getBoolean("flag.allowed-damage.enabled");
 		ENABLE_BLOCKED_DAMAGE = cfg.getBoolean("flag.blocked-damage.enabled");
 		MSG_FLY_SET_ALLOW = cfg.getString("message.fly.set.allow").trim().replace('&', '\247');
 		MSG_FLY_SET_BLOCK = cfg.getString("message.fly.set.block").trim().replace('&', '\247');
 		MSG_FLY_RESET_ALLOW = cfg.getString("message.fly.reset.allow").trim().replace('&', '\247');
 		MSG_FLY_RESET_BLOCK = cfg.getString("message.fly.reset.block").trim().replace('&', '\247');
 		MSG_NO_BREAK = cfg.getString("message.misc.no-break").trim().replace('&', '\247');
 		MSG_NO_PLACE = cfg.getString("message.misc.no-place").trim().replace('&', '\247');
 	}
 	
 	private void defaults() {
 		setDefault("flag.apply-potion.enabled", true);
 		setDefault("flag.blocked-break.enabled", true);
 		setDefault("flag.blocked-place.enabled", true);
 		setDefault("flag.blocked-damage.enabled", true);
 		setDefault("flag.allowed-break.enabled", true);
 		setDefault("flag.allowed-place.enabled", true);
 		setDefault("flag.allowed-damage.enabled", true);
 		setDefault("flag.fly.enabled", true);
 		setDefault("flag.healing.enabled", true);
 		setDefault("flag.hunger.enabled", true);
 		setDefault("flag.instabreak.enabled", true);
 		setDefault("flag.item-spawn.enabled", true);
 		setDefault("flag.item-pickup.enabled", true);
 		setDefault("flag.mob-targeting.enabled", true);
 		setDefault("flag.pve.enabled", true);
 		setDefault("flag.regen.enabled", true);
 		setDefault("flag.zombie-door-break.enabled", true);
 		setDefault("message.fly.set.allow", "&9You are allowed to fly here.");
 		setDefault("message.fly.set.block", "&9You are not allowed to fly here.");
 		setDefault("message.fly.reset.allow", "&9You are no longer allowed to fly.");
 		setDefault("message.fly.reset.block", "&9You are now allowed to fly again.");
 		setDefault("message.misc.no-place", "&cYou are not allowed to place that block!");
		setDefault("message.misc.no-break", "&cYou are not allowed to break that block!");
 	}
 	
 	private void setDefault(String name, Object value) {
 		if (!cfg.contains(name)) {
 			cfg.set(name, value);
 		}
 	}
 	
 	public final boolean ENABLE_FLY;
 	public final boolean ENABLE_APPLY_POTION;
 	public final boolean ENABLE_MOB_TARGETING;
 	public final boolean ENABLE_PVE;
 	public final boolean ENABLE_ITEM_SPAWN;
 	public final boolean ENABLE_HUNGER;
 	public final boolean ENABLE_REGEN;
 	public final boolean ENABLE_HEALING;
 	public final boolean ENABLE_ZOMBIE_DOOR_BREAK;
 	public final boolean ENABLE_BLOCKED_BREAK;
 	public final boolean ENABLE_BLOCKED_PLACE;
 	public final boolean ENABLE_ALLOWED_BREAK;
 	public final boolean ENABLE_ALLOWED_PLACE;
 	public final boolean ENABLE_ITEM_PICKUP;
 	public final boolean ENABLE_INSTABREAK;
 	public final boolean ENABLE_BLOCKED_DAMAGE;
 	public final boolean ENABLE_ALLOWED_DAMAGE;
 	public final String MSG_FLY_SET_ALLOW;
 	public final String MSG_FLY_SET_BLOCK;
 	public final String MSG_FLY_RESET_ALLOW;
 	public final String MSG_FLY_RESET_BLOCK;
 	public final String MSG_NO_BREAK;
 	public final String MSG_NO_PLACE;
 }
