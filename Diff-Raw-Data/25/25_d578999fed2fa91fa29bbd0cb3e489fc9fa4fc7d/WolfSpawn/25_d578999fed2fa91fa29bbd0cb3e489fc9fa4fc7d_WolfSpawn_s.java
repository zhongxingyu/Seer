 package com.jynxdaddy.wolfspawn;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 
 import net.minecraft.server.EntityWolf;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.entity.CraftWolf;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * WolfSpawn
  * 
  * @author ashtonw
  */
 public class WolfSpawn extends JavaPlugin {
 	
 	private final WolfListener wolfListener = new WolfListener(this);
 	private final WPlayerListener playerListener = new WPlayerListener(this);
 
 	public static Logger log = Logger.getLogger("Minecraft");
 	public Configuration cfg;
 	public static PermissionHandler permissions;
 	
 	private HashSet<String> putDownUsers = new HashSet<String>(10);
 
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " version " + pdfFile.getVersion()
 				+ " disabled!");
 	}
 
 	public void onEnable() {
 		//Config
 		readyConfig();
 		cfg = this.getConfiguration();
 		setupPermissions();
 		
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.ENTITY_DEATH, wolfListener,
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
 				Priority.Normal, this);
 		
 		//register commands
 		getCommand("putdown").setExecutor(new WolfCommand(this));
 		getCommand("spawnwolf").setExecutor(new WolfCommand(this));
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " version " + pdfFile.getVersion()
 				+ " is enabled!");
 	}
 
 	private void setupPermissions() {
 		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
 	      if (WolfSpawn.permissions == null) {
 	          if (test != null) {
 	              WolfSpawn.permissions = ((Permissions)test).getHandler();
 	          } else {
 	              log.info("Permission system not detected");
 	          }
 	      }
 	}
 	
 	public boolean permsOn() {
 		return WolfSpawn.permissions != null;
 	}
 	
 	public boolean getPermission(Player player, String permission) {
 		if (permissions == null) return true;
 		return permissions.has(player, permission);
 	}
 
 	public boolean addPutDownPlayer(String name) {
 		return putDownUsers.add(name);
 	}
 	
 	public boolean isPutDownPlayer(String name) {
 		return putDownUsers.contains(name);
 	}
 	
 	public boolean removePutDownPlayer(String name) {
 		return putDownUsers.remove(name);
 	}
 	
 	/*
 	 * Checks that config exists, if it doesn't, extract default from jar
 	 * Based on zydeco / PickBoat 
 	 */
 	private void readyConfig() {
 		File configFile = new File(this.getDataFolder(), "config.yml");
 
 		if (!configFile.exists()) {
 			try {
 				configFile.getParentFile().mkdirs();
 				JarFile jar = new JarFile(this.getFile());
 				JarEntry entry = jar.getJarEntry("config.yml");
 				InputStream is = jar.getInputStream(entry);
 				FileOutputStream os = new FileOutputStream(configFile);
 				byte[] buf = new byte[(int)entry.getSize()];
 				is.read(buf, 0, (int)entry.getSize());
 				os.write(buf);
 				os.close();
 				this.getConfiguration().load();
 			} catch (Exception e) {
 				log.info("WolfSpawn: could not create configuration file");
 				// unload plugin??
 			}
 		}
 	}
 	
 	public enum Message {
 		PUT_DOWN_TOGGLE_ON,
 		PUT_DOWN_TOGGLE_OFF,
 		WOLF_PUT_DOWN,
 		WOLF_DEATH
 	}
 	
 	public boolean sendMessage(Player player, Message msg) {
 		if (player == null) return false;
 		if (!cfg.getBoolean("msg-send", true)) return true;
 		
 		switch (msg) {
 		case PUT_DOWN_TOGGLE_ON:
 			if (!cfg.getBoolean("msg-putdown-toggle", true)) break;
 			player.sendMessage(cfg.getString("msg-putdown-toggle-on-text", ""));
 			break;
 		case PUT_DOWN_TOGGLE_OFF:
 			if (!cfg.getBoolean("msg-putdown-toggle", true)) break;
 			player.sendMessage(cfg.getString("msg-putdown-toggle-off-text", ""));
 			break;
 		case WOLF_PUT_DOWN:
 			if (!cfg.getBoolean("msg-wolf-putdown", true)) break;
 			player.sendMessage(cfg.getString("msg-wolf-putdown-text", ""));
 			break;
 		case WOLF_DEATH:
 			if (!cfg.getBoolean("msg-death", true)) break;
 			player.sendMessage(cfg.getString("msg-death-text", ""));
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 	
 	public void spawnWolf(Location spawn, World world, String owner) {
 		LivingEntity newWolf = world.spawnCreature(spawn, CreatureType.WOLF);
 		
 		int health = cfg.getInt("wolf-respawn-health", 5);
 		health = health > 0 && health <= 20 ? health : 5;
 		if (health <= 0 || health > 20) health = 5;
		
 		EntityWolf newMcwolf = ((CraftWolf)  newWolf).getHandle();
		newMcwolf.a(owner == null ? "" : owner); //setOwner
		newMcwolf.d(true); // owned?
		newMcwolf.b(true); // sitting
 		newMcwolf.health = health;
 	}
 
 }
