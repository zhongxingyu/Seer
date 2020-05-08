 package edu.unca.twreese.Hardcore;
 
 import java.util.Random;
 
 import org.bukkit.Location;
import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 /*
  * This is a sample event listener
  */
 public class HardcoreListener implements Listener {
     @SuppressWarnings("unused")
 	private final Hardcore plugin;
     private final int worldSize = 21550; //defines how many blocks a side of the square you are restricted you in this mod is
    private World world;
 
     /*
      * This listener needs to know about the plugin which it came from
      */
     public HardcoreListener(Hardcore plugin) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
        world = plugin.getServer().getWorld("world");
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event){
     	if(!event.getPlayer().hasPlayedBefore()){
    		Location loc = new Location(world, 0, 63, 0);
         	Random rnd = new Random();
         	boolean looking = true;
         	while(looking){
         		loc.setX(loc.getX() -(worldSize/2) + (rnd.nextDouble() * worldSize));
         		loc.setZ(loc.getZ() - (worldSize/2)+ (rnd.nextDouble() * worldSize));
         		loc.getChunk().load(true);
        		loc.setY(world.getHighestBlockYAt(loc));
        		if(world.getBiome(loc.getBlockX(), loc.getBlockZ()).compareTo(Biome.OCEAN) != 0)
         			looking = false;
         	}
         	event.getPlayer().teleport(loc.add(0, 2, 0));
     	}
     	event.getPlayer().sendMessage("This is a Hardcore Server.");
     	event.getPlayer().sendMessage("On death you will spawn somewhere random in a great distance.");
     	event.getPlayer().sendMessage("This may cause you to be kicked relog and it will have finished loading your new spawn.");
     }
     
 
     
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent event){
    	Location loc = new Location(world, 0, 63, 0);
     	Random rnd = new Random();
     	boolean looking = true;
     	while(looking){
     		loc.setX(loc.getX() -(worldSize/2) + (rnd.nextDouble() * worldSize));
     		loc.setZ(loc.getZ() - (worldSize/2)+ (rnd.nextDouble() * worldSize));
     		loc.getChunk().load(true);
    		loc.setY(world.getHighestBlockYAt(loc));
    		if(world.getBiome(loc.getBlockX(), loc.getBlockZ()).compareTo(Biome.OCEAN) != 0)
     			looking = false;
     	}
     	event.setRespawnLocation(loc.add(0, 2, 0));
     }
     
 }
