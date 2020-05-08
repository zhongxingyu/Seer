 package org.maski.overlord;
 
 import java.util.logging.Logger;
 
 import net.minecraft.server.*;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.*;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.entity.*;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Overlord extends JavaPlugin {
 	private final OverlordEntityListener entityListener = new OverlordEntityListener(this);
 	
     private static Logger l = Logger.getLogger("Minecraft.PogicPlugin");
 
     public void onDisable() {
     }
 
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         
        getCommand("time").setExecutor(new SpawnCommand(this));
         pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
     }
     
     private class SpawnCommand implements CommandExecutor {
         private final Overlord plugin;
 
         public SpawnCommand(Overlord plugin) {
             this.plugin = plugin;
         }
 
         public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("must be done by a player");
         	}
         	Player player = (Player)sender;
     		try {
 	    		Block block = player.getTargetBlock(null, 20);
 	    		Location loc = block.getLocation();
 	    		int y = loc.getWorld().getHighestBlockYAt(loc);
 	    		loc.setY(y);
 	    		CraftWorld cw = (CraftWorld)player.getWorld();
 	    		Entity newThing = EntityTypes.a(args[0], cw.getHandle());
 	    		if (newThing == null) {
 	    			sender.sendMessage("could not find " + args[0]);
 	    			return true;
 	    		}
 	    		newThing.b(loc.getX() + 0.5D, loc.getY(), loc.getZ() + 0.5D, loc.getYaw(), loc.getPitch());
 	    		cw.getHandle().a(newThing);
     		} catch(Throwable e) {
     			e.printStackTrace();
     		}
     		return true;
         	
         }
     }
 
     private class OverlordEntityListener extends EntityListener {
         private final Overlord plugin;
 
         public OverlordEntityListener(Overlord instance) {
             plugin = instance;
         }
 
 		@Override
 		public void onCreatureSpawn(CreatureSpawnEvent event) {
 			//System.out.println("preventing the spawn of " + event.getEntity());
 			event.setCancelled(true);
 		}
     }
 
 }
