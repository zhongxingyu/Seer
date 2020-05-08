 package me.feltlizard.hellotorch;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class HelloTorchBlockListener extends BlockListener {
 	
 	public static HelloTorch plugin; 
 	
 	public HelloTorchBlockListener(HelloTorch instance) {
 		plugin = instance;
 	}
 	
 	public void onBlockPlace(BlockPlaceEvent event) {
 		
 		Player player = event.getPlayer();
 		//gets player
 		//String name = event.getPlayer().getDisplayName();
 		//gets players user name
 		Block block = event.getBlockPlaced();
 		//gets placed block
 		
		if (Global.num == 1) {
			player.sendMessage(HelloTorch.config.getString(Integer.toString(block.getTypeId()), "You placed a " + block.getType().toString()));
		}
 		
 	}
	}
 	

