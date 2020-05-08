 package pgDev.bukkit.DisguiseCraft;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.minecraft.server.Packet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
 import pgDev.bukkit.DisguiseCraft.debug.DebugPacketOutput;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class DisguiseCraft extends JavaPlugin {
 	// File Locations
     static String pluginMainDir = "./plugins/DisguiseCraft";
     static String pluginConfigLocation = pluginMainDir + "/DisguiseCraft.cfg";
 	
     // Permissions support
     static PermissionHandler Permissions;
     
     boolean debug = false;
     
     // Listener
     DCMainListener mainListener = new DCMainListener(this);
     
     // Disguise database
     public ConcurrentHashMap<String, Disguise> disguiseDB = new ConcurrentHashMap<String, Disguise>();
     public HashMap<String, String> disguisedentID = new HashMap<String, String>();
     public LinkedList<String> disguiseQuitters = new LinkedList<String>();
     
     // Custom display nick saving
     public HashMap<String, String> customNick = new HashMap<String, String>();
     
 	public void onEnable() {
 		// Check for the plugin directory (create if it does not exist)
     	File pluginDir = new File(pluginMainDir);
 		if(!pluginDir.exists()) {
 			boolean dirCreation = pluginDir.mkdirs();
 			if (dirCreation) {
 				System.out.println("New DisguiseCraft directory created!");
 			}
 		}
 		
 		// If we are debugging show packet output for disguised players using spout.
 		if (debug) {
 			if (spoutEnabled()) {
 				new DebugPacketOutput(this);
 			} else {
 				System.out.println("DisguiseCraft's debug mode requires Spout.");
 			}
 		}
 		
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(mainListener, this);
 		
 		// Toss over the command events
 		DCCommandListener commandListener = new DCCommandListener(this);
 		String[] commandList = {"disguise", "d", "dis", "undisguise", "u", "undis"};
         for (String command : commandList) {
         	try {
         		this.getCommand(command).setExecutor(commandListener);
         	} catch (NullPointerException e) {
         		System.out.println("Another plugin is using the /" + command + " command. You will need to use one of DisguiseCraft's alternate commands.");
         	}
         }
 		
 		// Get permissions in the game!
         setupPermissions();
         
         // Heyo!
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 	
 	public boolean spoutEnabled() {
 		return (this.getServer().getPluginManager().getPlugin("Spout") != null);
 	}
 	
 	public void onDisable() {
 		System.out.println("DisguiseCraft disabled!");
 	}
 	
 	// Permissions Methods
     private void setupPermissions() {
         Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (Permissions == null) {
             if (permissions != null) {
                 Permissions = ((Permissions)permissions).getHandler();
             } else {
             }
         }
     }
     
     public boolean hasPermissions(Player player, String node) {
         if (Permissions != null) {
         	return Permissions.has(player, node);
         } else {
             return player.hasPermission(node);
         }
     }
     
     // Obtaining the API
     public DisguiseCraftAPI api = new DisguiseCraftAPI(this);
     /**
      * Get the DisguiseCraft API
      * @return The API
      */
     public static DisguiseCraftAPI getAPI() {
     	return ((DisguiseCraft) Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft")).api;
     }
     
     // Important Disguise Methods
     protected int nextID = Integer.MIN_VALUE;
     public int getNextAvailableID() {
     	return nextID++;
     }
     
     public void disguisePlayer(Player player, Disguise disguise) {
     	if (disguise.isPlayer()) {
     		if (!customNick.containsKey(player.getName()) && !player.getName().equals(player.getDisplayName())) {
         		customNick.put(player.getName(), player.getDisplayName());
         	}
     		player.setDisplayName(disguise.data);
     	}
     	disguiseDB.put(player.getName(), disguise);
     	disguisedentID.put(Integer.toString(disguise.entityID), "true");
     	sendDisguise(player, null);
     }
     
     public void changeDisguise(Player player, Disguise newDisguise) {
     	unDisguisePlayer(player);
     	disguisePlayer(player, newDisguise);
     	//(new Timer()).schedule(new DisguiseChangeTask(this, player, newDisguise), 500);
     }
     
     public void unDisguisePlayer(Player player) {
     	String name = player.getName();
     	if (disguiseDB.containsKey(name)) {
     		if (customNick.containsKey(name)) {
 	    		player.setDisplayName(customNick.get(name));
 	    		customNick.remove(name);
 	    	} else {
 	    		player.setDisplayName(name);
 	    	}
     		sendUnDisguise(player, null);
     		Disguise disguise = disguiseDB.get(name);
     		disguiseDB.remove(name);
     		disguisedentID.remove(Integer.toString(disguise.entityID));
     	}
     }
     
     public void halfUndisguiseAllToPlayer(Player observer) {
     	World world = observer.getWorld();
     	for (String name : disguiseDB.keySet()) {
     		Player disguised = getServer().getPlayer(name);
     		if (disguised != null) {
     			if (world == disguised.getWorld()) {
     				observer.showPlayer(disguised);
     			}
     		}
     	}
     }
     
     public static byte degreeToByte(float degree) {
     	return (byte) ((int) degree * 256.0F / 360.0F);
     }
     
     public void sendDisguise(Player disguised, Player observer) {
     	if (disguiseDB.containsKey(disguised.getName())) {
     		Disguise disguise = disguiseDB.get(disguised.getName());
     		if (disguise.mob == null) { // Non-mob disguise
     			if (disguise.data.equals("$")) { // Invisible
     				if (observer == null) {
     					disguiseToWorld(disguised.getWorld(), disguised, (Packet[]) null);
     				} else {
     					observer.hidePlayer(disguised);
     				}
     			} else { // Player disguise
     				Packet packet = disguise.getPlayerSpawnPacket(disguised.getLocation(), (short) disguised.getItemInHand().getTypeId());
     				Packet packet2 = disguise.getPlayerInfoPacket(disguised, true);
     				if (observer == null) {
     					if (packet2 == null) {
     						disguiseToWorld(disguised.getWorld(), disguised, packet);
     					} else {
     						disguiseToWorld(disguised.getWorld(), disguised, packet, packet2);
     					}
     				} else {
     					if (!hasPermissions(observer, "disguisecraft.seer")) {
     						observer.hidePlayer(disguised);
     					}
     					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
     					if (packet2 != null) {
     						((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
     					}
     				}
     			}
     		} else { // Mob Disguise
     			Packet packet = disguise.getMobSpawnPacket(disguised.getLocation());
     			if (observer == null) {
     				disguiseToWorld(disguised.getWorld(), disguised, packet);
     			} else {
     				if (!hasPermissions(observer, "disguisecraft.seer")) {
 						observer.hidePlayer(disguised);
 					}
     				((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
     			}
     		}
     	}
     }
     
     public void sendUnDisguise(Player disguised, Player observer) {
     	if (disguiseDB.containsKey(disguised.getName())) {
     		Disguise disguise = disguiseDB.get(disguised.getName());
     		Packet packet = disguise.getEntityDestroyPacket();
     		Packet packet2 = disguise.getPlayerInfoPacket(disguised, false);
     		if (observer == null) {
 				if (packet2 == null) {
 					undisguiseToWorld(disguised.getWorld(), disguised, packet);
 				} else {
 					undisguiseToWorld(disguised.getWorld(), disguised, packet, packet2);
 				}
 			} else {
 				if (packet2 != null) {
 					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
 				}
 				((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
 				observer.showPlayer(disguised);
 			}
     	}
     }
     
     public void sendMovement(Player disguised, Player observer, Vector vector, Location to) {
     	if (disguiseDB.containsKey(disguised.getName())) {
     		Disguise disguise = disguiseDB.get(disguised.getName());
     		MovementValues movement = disguise.getMovement(to);
     		if (movement.x < -128 || movement.x > 128 || movement.y < -128 || movement.y > 128 || movement.z < -128 || movement.z > 128) { // That's like a teleport right there!
     			Packet packet = disguise.getEntityTeleportPacket(to);
     			if (observer == null) {
 					sendPacketToWorld(disguised.getWorld(), packet);
 				} else {
 					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
 				}
     		} else { // Relative movement
     			if (movement.x == 0 && movement.y == 0 && movement.z == 0) { // Just looked around
     				//Client doesn't seem to want to register this
     				Packet packet = disguise.getEntityLookPacket(to);
     				if (observer == null) {
    					sendPacketToWorld(disguised.getWorld(), packet);
     				} else {
     					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
     				}
     			} else { // Moved legs
     				Packet packet = disguise.getEntityMoveLookPacket(to);
     				if (observer == null) {
    					sendPacketToWorld(disguised.getWorld(), packet);
     				} else {
     					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
     				}
     			}
     		}
     	}
     }
     
     public void sendPacketToWorld(World world, Packet... packet) {
     	for (Player observer : world.getPlayers()) {
     		for (Packet p : packet) {
     			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
     		}
     	}
     }
     
     public void disguiseToWorld(World world, Player player, Packet... packet) {
     	for (Player observer : world.getPlayers()) {
 	    	if (observer != player) {
 	    		if (!hasPermissions(observer, "disguisecraft.seer")) {
 					observer.hidePlayer(player);
 				}
 	    		for (Packet p : packet) {
 	    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
 	    		}
     		}
     	}
     }
     
     public void undisguiseToWorld(World world, Player player, Packet... packet) {
     	for (Player observer : world.getPlayers()) {
     		if (observer != player) {
 	    		for (Packet p : packet) {
 	    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
 	    		}
 				observer.showPlayer(player);
     		}
     	}
     }
     
     public void showWorldDisguises(Player observer) {
     	for (String disguisedName : disguiseDB.keySet()) {
 			Player disguised = getServer().getPlayer(disguisedName);
 			if (disguised != null && disguised != observer) {
 				if (disguised.getWorld() == observer.getWorld()) {
 					sendDisguise(disguised, observer);
 				}
 			}
 		}
     }
 }
