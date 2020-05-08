 package net.preoccupied.bukkit.warp;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 
 import net.preoccupied.bukkit.LocationMap;
 import net.preoccupied.bukkit.permissions.PermissionCommand;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 
 public class WarpPlugin extends JavaPlugin {
     
     
     private LocationMap<Warp> warpTriggers = null;
     
     private Map<String,Warp> warpNames = null;
     
     private Map<Chunk,List<Runnable>> teleportQueue = null;
     
 
     
     public void onEnable() {
	System.out.println("Warp.onEnable()");
 
 	warpTriggers = new LocationMap<Warp>();
 	warpNames = new HashMap<String,Warp>();
 
 	teleportQueue = new HashMap<Chunk,List<Runnable>>();
 
 	loadWarps();
 	
 	PluginManager pm = getServer().getPluginManager();
 	EventExecutor ee = null;
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onPlayerPortal((PlayerPortalEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.PLAYER_PORTAL, null, ee, Priority.Low, this);
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onChunkLoad((ChunkLoadEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.CHUNK_LOAD, null, ee, Priority.Low, this);
 
 	setupCommands();
     }
     
     
     
     public void onDisable() {
 	warpTriggers.clear();
 	warpNames.clear();
     }
     
     
     
     private void loadWarps() {
 	try {
 	    for(Warp w : getDatabase().find(Warp.class).findList()) {
 		Location l = null;
 		
 		l = w.getInputA();
 		if(l != null) warpTriggers.put(l, w);
 		
 		l = w.getInputB();
 		if(l != null) warpTriggers.put(l, w);
 		
 		warpNames.put(w.getName(), w);
 	    }
 	    
 	} catch(PersistenceException pe) {
 	    installDDL();
 	}
     }
     
     
     
     public Warp createWarp(String name, Location input_a, Location input_b, Location output) {
 	Warp warp = new Warp();
 	warp.setName(name);
 	warp.setInputA(input_a);
 	warp.setInputB(input_b);
 	warp.setOutput(output);
 
 	if(input_a != null) warpTriggers.put(input_a, warp);
 	if(input_b != null) warpTriggers.put(input_b, warp);
 	warpNames.put(name, warp);
 
 	getDatabase().save(warp);
 	return warp;
     }
 
 
 
     public void deleteWarp(Warp warp) {
 	Location l;
 
 	l = warp.getInputA();
 	if(l != null) warpTriggers.remove(l);
 	l = warp.getInputB();
 	if(l != null) warpTriggers.remove(l);
 
 	warpNames.remove(warp.getName());
 	
 	getDatabase().delete(warp);
     }
     
 
 
     public Warp getWarp(String name) {
 	return warpNames.get(name);
     }
 
 
 
     public Warp getWarp(Location trigger) {
 	return warpTriggers.get(trigger);
     }
 
 
 
     public void updateWarp(Warp warp) {
 	getDatabase().update(warp);
     }
 
 
     
     private static final String safeStr(Object o) {
 	if(o == null) {
 	    return "[null]";
 	} else {
 	    return o.toString();
 	}
     }
 
 
 
     private static final String globconvert(String pattern) {
 	pattern = pattern.replace("\\","\\\\");
 	pattern = pattern.replace(".", "\\.");
 	pattern = pattern.replace("?", ".");
 	pattern = pattern.replace("*", ".*");
 	return pattern;
     }
 
 
 
     private void setupCommands() {
 
 	new PermissionCommand(this, "warp-list") {
 	    public boolean run(Player player) {
 		msg(player, "Warp names:");
 		for(Warp w : warpNames.values()) {
 		    msg(player, " " + w.getName());
 		}
 		return true;
 	    }
 	    public boolean run(Player player, String pattern) {
 		pattern = globconvert(pattern);
 		msg(player, "Warp names:");
 		for(Warp w : warpNames.values()) {
 		    if(w.getName().matches(pattern)) {
 			msg(player, " " + w.getName());
 		    }
 		}
 		return true;
 	    }
 	};
 	
 
 	new PermissionCommand(this, "warp-info") {
 	    public boolean run(Player player, String name) {
 		Warp warp = getWarp(name);
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 		    
 		} else {
 		    msg(player, "Information for Warp ID: " + safeStr(warp.getId()));
 		    msg(player, " Warp Name: " + name);
 		    msg(player, " Dest Name: " + safeStr(warp.getDestination()));
 		    msg(player, " InputA: " + safeStr(warp.getInputA()));
 		    msg(player, " InputB: " + safeStr(warp.getInputB()));
 		    msg(player, " Output: " + safeStr(warp.getOutput()));
 		}
 		
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "add-warp") {
 	    public boolean run(Player player, String name) {
 		return run(player, name, null);
 	    }
 	    public boolean run(Player player, String name, String dest) {
 		Warp w;
 		
 		w = getWarp(name);
 		if(w != null) {
 		    msg(player, "A warp with that name already exists");
 		    return true;
 		}
 		
 		Location l = player.getLocation();
 		w = getWarp(l);
 		if(w != null) {
 		    msg(player, "A warp already exists in that location");
 		    return true;
 		}
 		
 		w = createWarp(name, l, null, null);
 
 		if(dest != null) {
 		    w.setDestination(dest);
 		    updateWarp(w);
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "set-warp-input") {
 	    public boolean run(Player player, String name) {
 		Warp warp = warpNames.get(name);
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 		    return true;
 		}
 
 		Location l = null;
 		l = warp.getInputA();
 		warpTriggers.remove(l);
 
 		l = player.getLocation();
 		warp.setInputA(l);
 
 		warpTriggers.put(l, warp);
 		updateWarp(warp);
 
 		return true;
     	    }
 	};
 
 
 	new PermissionCommand(this, "set-warp-input-b") {
 	    public boolean run(Player player, String name) {
 		Warp warp = warpNames.get(name);
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 		    return false;
 		}
 
 		Location l = null;
 		l = warp.getInputB();
 		warpTriggers.remove(l);
 
 		l = player.getLocation();
 		warp.setInputB(l);
 
 		warpTriggers.put(l, warp);
 		updateWarp(warp);
 		
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "set-warp-output") {
 	    public boolean run(Player player, String name) {
 		Warp warp = getWarp(name);
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 		    return true;
 		}
 
 		warp.setOutput(player.getLocation());
 		updateWarp(warp);
 
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "set-warp-destination") {
 	    public boolean run(Player player, String name, String dest) {
 		Warp warp = getWarp(name);
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 		    return false;
 		}
 
 		warp.setDestination(dest);
 		updateWarp(warp);
 
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "link-warps") {
 	    public boolean run(Player player, String name1, String name2) {
 		Warp warp1 = getWarp(name1);
 		Warp warp2 = getWarp(name2);
 		if(warp1 == null) {
 		    msg(player, "No such warp: " + name1);
 		    return false;
 		}
 		if(warp2 == null) {
 		    msg(player, "No such warp: " + name2);
 		    return false;
 		}
 
 		warp1.setDestination(name2);
 		updateWarp(warp1);
 
 		warp2.setDestination(name1);
 		updateWarp(warp2);
 
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "remove-warp") {
 	    public boolean run(Player player, String name) {
 		Warp warp = getWarp(name);
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 
 		} else {
 		    deleteWarp(warp);
 		    msg(player, "Deleted warp: " + name);
 		}
 		
 		return true;
 	    }
 	};	
 
 
 	new PermissionCommand(this, "warp-to") {
 	    public boolean run(Player player, String name) {
 		Warp warp = getWarp(name);
 
 		if(warp == null) {
 		    msg(player, "No such warp: " + name);
 		    return true;
 		}
 
 		Location l = warp.getOutput();
 		if(l == null) {
 		    msg(player, "Warp has no output: " + name);
 		    return true;
 		}
 
 		safeTeleport(player, l);
 		return true;
 	    }
 	};
     }
 
 
 
     private void onPlayerPortal(PlayerPortalEvent ppe) {
 	if (ppe.isCancelled()) {
 	    return;
 	}
 
 	Player player = ppe.getPlayer();
 
 	ppe.useTravelAgent(false);
 	ppe.setCancelled(true);
 
 	if(player.isSneaking()) {
 	    return;
 	}
 	
 	Location trigger = ppe.getFrom();
 
 	Warp w = warpTriggers.get(trigger);
 	if (w == null) {
 	    return;
 
 	} else {
 	    String destname = w.getDestination();
 	    if(destname == null)
 		return;
 
 	    Warp dest = getWarp(destname);
 	    if(dest == null)
 		return;
 
 	    Location output = dest.getOutput();
 	    if(output == null)
 		return;
 
 	    safeTeleport(player, output);
 	}
     }
 
 
 
     private void onChunkLoad(ChunkLoadEvent cle) {
 	Chunk chunk = cle.getChunk();
 	List<Runnable> queue = teleportQueue.get(chunk);
 
 	if(queue != null) {
 	    for(Runnable task : queue) {
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, task);
 	    }
 	    teleportQueue.remove(chunk);
 	}
     }
 
 
 
     public void safeTeleport(final Player player, final Location destination) {
 
 	Runnable task = new Runnable() {
 		public void run() {
 		    player.teleport(destination);
 		}
 	    };
 	
 	World world = destination.getWorld();
 	Chunk chunk = world.getChunkAt(destination);
 
 	if(world.isChunkLoaded(chunk)) {
 	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, task);
 
 	} else {
 	    List<Runnable> queue = teleportQueue.get(chunk);
 	    if(queue == null) {
 		queue = new LinkedList<Runnable>();
 		teleportQueue.put(chunk, queue);
 	    }
 	    queue.add(task);
 	    world.loadChunk(chunk);
 	}
     }
 
 
     public List<Class<?>> getDatabaseClasses() {
 	List<Class<?>> list = new ArrayList<Class<?>>();
 	list.add(Warp.class);
 	return list;
     }
 
     
 }
 
 
 /* The end. */
