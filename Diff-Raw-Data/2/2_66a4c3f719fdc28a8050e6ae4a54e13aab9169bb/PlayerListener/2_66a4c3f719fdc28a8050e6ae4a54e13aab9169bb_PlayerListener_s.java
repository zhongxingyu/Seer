 package nl.giantit.minecraft.GiantShop.Locationer.Listeners;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Locationer.Locationer;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Location;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author Giant
  */
 public class PlayerListener implements Listener {
 	GiantShop plugin;
 	Locationer lH;
 	ArrayList<Player> inShop = new ArrayList<Player>();
 	
 	public PlayerListener(GiantShop plugin) {
 		this.plugin = plugin;
 		lH = plugin.getLocHandler();
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Heraut.savePlayer(player);
 		
 		if(!inShop.contains(player) && plugin.getLocHandler().inShop(player.getLocation())) {
 			inShop.add(player);
 			Heraut.say("&3You have just entered a shop &e(&f" + plugin.getLocHandler().getShopName(player.getLocation()).toString() + "&e)&3!");
 			return;
 		}else if(inShop.contains(player) && !plugin.getLocHandler().inShop(player.getLocation())) {
 			inShop.remove(player);
 			Heraut.say("&3You have just left a shop!");
 			return;
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(perm.Obtain().has(event.getPlayer(), "giantshop.location.add")) {
 			config conf = config.Obtain();
 			ItemStack i = event.getItem();
 			if(i.getTypeId() == conf.getInt("GiantShop.Location.tool.id") && i.getData().getData() == (byte)((int) conf.getInt("GiantShop.Location.tool.type"))) {
 				if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
 					HashMap<String, Location> point = lH.getPlayerPoints(event.getPlayer());
 					point.put("min", event.getClickedBlock().getLocation());
 
 					lH.setPlayerPoint(event.getPlayer(), point);
 					Heraut.say(event.getPlayer(), "Successfully set first point to: " 
 														+ event.getClickedBlock().getLocation().getBlockX() 
 														+ ", " + event.getClickedBlock().getLocation().getBlockY()
 														+ ", " + event.getClickedBlock().getLocation().getBlockZ());
 				}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 					HashMap<String, Location> point = lH.getPlayerPoints(event.getPlayer());
					point.put("min", event.getClickedBlock().getLocation());
 
 					lH.setPlayerPoint(event.getPlayer(), point);
 					Heraut.say(event.getPlayer(), "Successfully set second point to: " 
 													+ event.getClickedBlock().getLocation().getBlockX() 
 													+ ", " + event.getClickedBlock().getLocation().getBlockY()
 													+ ", " + event.getClickedBlock().getLocation().getBlockZ());
 				}
 			}
 		}
 	}
 }
