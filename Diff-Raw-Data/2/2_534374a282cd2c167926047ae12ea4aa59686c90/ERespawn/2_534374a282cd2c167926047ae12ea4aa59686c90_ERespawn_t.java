 package net.year4000.erespawn;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.sk89q.commandbook.CommandBook;
 import com.zachsthings.libcomponents.ComponentInformation;
 import com.zachsthings.libcomponents.bukkit.BukkitComponent;
 import com.zachsthings.libcomponents.config.ConfigurationBase;
 import com.zachsthings.libcomponents.config.Setting;
 
@ComponentInformation(friendlyName = "eRespawn", desc = "Bring back classic death spawning.")
 public class ERespawn extends BukkitComponent implements Listener{
 	
 	private LocalConfiguration config;
 	private String component = "[eRespawn]";
 	
     public void enable() {
     	config = configure(new LocalConfiguration());
     	CommandBook.registerEvents(this);
         Logger.getLogger(component).log(Level.INFO, component+" has been enabled.");
     }
 
     public void reload() {
         super.reload();
         configure(config);
         Logger.getLogger(component).log(Level.INFO, component+" has been reloaded.");
     }
 	
     public static class LocalConfiguration extends ConfigurationBase {
     	@Setting("respawn-default-world") public boolean respawnDefaultWorld = true;
     	@Setting("bed-day") public boolean bedDay = true;
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onRespawn(PlayerRespawnEvent event){
     	Player p = event.getPlayer();
     	Location bl = p.getBedSpawnLocation();
     	Location l = event.getRespawnLocation();
     	
     	event.setRespawnLocation(setRespawn(p, bl, l));
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void setBed(PlayerInteractEvent event){
     	if(config.bedDay){
     		Block b = event.getClickedBlock();
     		Action a = event.getAction();
     		Player p = event.getPlayer();
 
 			if(b != null && b.getType() == Material.BED_BLOCK && a == Action.RIGHT_CLICK_BLOCK){
 				Location l = b.getLocation();
 				Location bl = p.getBedSpawnLocation();
 				Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
 				
 				if(p.getWorld() == spawn.getWorld()){
 					if(bl == null) bl = spawn;
 					if(p.getWorld().getTime() < 12500){
 		    			p.setBedSpawnLocation(setBedSpawn(bl,l,p), true);
 					}
 				}
 			}
     	}
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onSleep(BlockBreakEvent event){
     	if(config.bedDay){
 			Block b = event.getBlock();
 			Player p = event.getPlayer();
 			
 			if(b != null && b.getType() == Material.BED_BLOCK){
 				Location bl = p.getBedSpawnLocation();
 		    	double d = 3;
 		    	try{
 					if(bl != null) d = bl.distance(b.getLocation());
 		    	} catch(IllegalArgumentException e){}
 				if(d < 2.5){
 					p.setBedSpawnLocation(b.getLocation(), true);
 				}
 			}
     	}
     }
     
     public Location setBedSpawn(Location bl, Location l, Player p){
     	Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
     	double d = 3;
     	try{
 			if(bl != null) d = bl.distance(l);
     	} catch(IllegalArgumentException e){}
 		if(d > 2.5){
 			p.sendMessage(ChatColor.YELLOW + "You will respawn at this location, you may destory the bed.");
 			//p.sendMessage(ChatColor.YELLOW + "Enter the bed again to respawn at spawn.");
 			return l;
 		}
 		p.sendMessage(ChatColor.YELLOW + "You will respawn at spawn.");
 		return spawn;
     }
     
     public Location setRespawn(Player p, Location bl, Location l){
     	World w = Bukkit.getWorld(Bukkit.getWorlds().get(0).getName());
     	if(bl != null) return bl;
     	if(l != w && config.respawnDefaultWorld) return w.getSpawnLocation();
 		return l;
     }
 }
