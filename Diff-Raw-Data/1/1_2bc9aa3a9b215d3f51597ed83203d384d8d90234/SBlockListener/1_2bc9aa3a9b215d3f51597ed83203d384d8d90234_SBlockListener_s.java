 package nl.lolmen.sortal;
 
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SBlockListener implements Listener{
 	public Main plugin;
 	public SBlockListener(Main main) {
 		plugin = main;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onSignChange(SignChangeEvent event){
 		Player p = event.getPlayer();
 		for(int i = 0; i<event.getLines().length; i++){
 			if(event.getLine(i).toLowerCase().contains("[sortal]") || event.getLine(i).toLowerCase().contains(plugin.signContains)){
 				if(event.getPlayer().hasPermission("sortal.placesign")){
 					plugin.log.info("[Sortal] Sign placed by " + p.getDisplayName() + ", AKA " + p.getName() + "!");
 				}else{
 					p.sendMessage("You are not allowed to place a [Sortal] sign!");
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(BlockBreakEvent event){
 		Block block = event.getBlock();
 		Sign s = (Sign)block.getState();
 		if ((block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
 			if(!plugin.loc.containsKey(block.getLocation())){
 				for(int i = 0; i<s.getLines().length; i++){
 					if(s.getLine(i).toLowerCase().contains("[sortal]") || s.getLine(i).toLowerCase().contains(plugin.signContains)){
 						if(!event.getPlayer().hasPermission("sortal.delsign")){
 							event.getPlayer().sendMessage("[Sortal] You do not have permissions to destroy a [Sortal] sign!");
 							event.setCancelled(true);
 						}
 					}
 				}
 				return;
 			}
 			if(!event.getPlayer().hasPermission("sortal.delsign")){
 				event.getPlayer().sendMessage("[Sortal] You do not have permissions to destroy a registered sign!");
 				event.setCancelled(true);
 			}
 			delLoc(block.getLocation());
 			plugin.log.info("Registered sign destroyed by " + event.getPlayer().getDisplayName() + ", AKA " + event.getPlayer().getName() + "!");
 		}
 	}
 
 	private void delLoc(Location location) {
 		Properties prop = new Properties();
 		try {
 			prop.remove(location);
 			prop.store(new FileOutputStream(plugin.locs), "[Location] = [Name]");
 			plugin.loc.remove(location);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
