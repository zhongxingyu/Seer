 package me.xir.moovr;
 
import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MoovrInit extends JavaPlugin {
 	
 	public Player sender;
 	@EventHandler
 	public void onPlayerMoveEvent(PlayerMoveEvent event) {
 		Player player = (Player) sender;
		Location loc = player.getLocation();
		Block block = loc.getBlock().getRelative(BlockFace.DOWN);
 		if(block.getType() == Material.POWERED_RAIL) {
 			Block blockUnder = block.getRelative(BlockFace.DOWN);
 			if(blockUnder.getType() == Material.GOLD_BLOCK){
				player.sendMessage("YOU ARE STANDING ON THE CORRECT COMBINATION OF BLOCKS!");
 			}
 		}
 	}
 }
