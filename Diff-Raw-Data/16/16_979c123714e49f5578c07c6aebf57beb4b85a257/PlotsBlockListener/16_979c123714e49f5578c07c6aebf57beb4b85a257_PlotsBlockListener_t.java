 package me.benphelps.Plots;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class PlotsBlockListener implements Listener {
 
 	public Plots plots;
 
 	public PlotsBlockListener(Plots plots) {
 		this.plots = plots;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		Material mat = block.getType(); 
 		player.sendMessage("You placed a block with ID : " + mat);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public boolean onBockDestroy(BlockBreakEvent event){
 		Player player = event.getPlayer();
		//Block block = event.getBlock();
 		if(player.getItemInHand().getTypeId() == Material.STICK.getId()){
 			
			//long point = PlotsPoint.point(block.getX(), block.getY());
 			//if ((max(x0,x1,x2) != x0 && min(x0,x1,x2) != x0) &&(max(y0,y1,y2) != y0 && min(y0,y1,y2) != x0)) {
 			//}
 			
 			return false;
 		}
 		return true;
 	}
 }
