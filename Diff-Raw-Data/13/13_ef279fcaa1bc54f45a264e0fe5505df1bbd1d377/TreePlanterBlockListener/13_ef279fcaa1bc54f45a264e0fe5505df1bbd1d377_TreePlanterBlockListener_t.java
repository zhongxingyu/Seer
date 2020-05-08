 package hawox.treeplanter;
 
 
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockListener;
 
 public class TreePlanterBlockListener extends BlockListener{
 	private final Hawox_TreePlanter plugin; 
 
     public TreePlanterBlockListener(Hawox_TreePlanter instance) {
         plugin = instance;
     } 
     
  
     @Override
 	public void onBlockBreak(BlockBreakEvent event){
     	if(plugin.isEnabled()){
     		Block block = event.getBlock();
     		Player player = event.getPlayer();
     		//Okay so lets check if the player has broken a log block!
     		if(block.getType() == Material.LOG){
 				//block is broken! Now lets see if this block was on a piece of dirt...
     			Block block_under_me = block.getFace(BlockFace.DOWN);
     			if(   (block_under_me.getType() ==  Material.DIRT)  ||  (block_under_me.getType() ==  Material.GRASS) ) {
     				//woot! on grass here so lets plant the sapling! wooooo!
     				PlantMe(block);
     				if(plugin.isTelluser())
     					player.sendMessage(ChatColor.GREEN + plugin.getTellUserPlanted());
     			}
     		}//Not a log, maybe they are trying to break a protected sapling?
     		else if(block.getType() == Material.SAPLING){
     			if(plugin.getProtect().contains(block)){
     				event.setCancelled(true);
     				if(plugin.isTelluserProtected())
     					player.sendMessage(ChatColor.GREEN + plugin.getTellUserProtected());
     			}
     		}//How about the dirt under the sapling?
    		else if( block.getFace(BlockFace.UP).getType() == Material.SAPLING ){
     			//The sapling is one above the dirt, so get the block there instead
     			Block sap = block.getFace(BlockFace.UP);
    			if(plugin.getProtect().contains(sap)){
    				event.setCancelled(true);
    				if(plugin.isTelluserProtected())
    					player.sendMessage(ChatColor.GREEN + plugin.getTellUserProtected());
     			}
     		}
     	}
 	}
     
     @Override
     public void onBlockBurn(BlockBurnEvent event){
     	Block block = event.getBlock();
 		//Log?
 		if(block.getType() == Material.LOG){ 
 			if(plugin.isEnabled() == true){
 				//block is broken! Now lets see if this block was on a piece of dirt...
 				Block block_under_me = block.getFace(BlockFace.DOWN);
 				if(   (block_under_me.getType() ==  Material.DIRT)  ||  (block_under_me.getType() ==  Material.GRASS) ) {
 					//broadcast("Dirt is below me!");
 					//woot! on grass here so lets plant the sapling! wooooo!
 				
 					//It will not replant the block if it is not air, so lets help that burning log out
 					block.setType(Material.AIR);
 					PlantMe(block);				
 				}
 			}
 		}
     }
     
     
     public void PlantMe(Block block){
 		//store the sapling into a list to replant, replant sapling after set time
     	plugin.getReplant().add(block);
 		//run the timer to load the block
     	
     	if(plugin.getTreeplant_Timer().getPoolSize() < plugin.getTreeplant_Timer().getMaximumPoolSize()){
     		plugin.getTreeplant_Timer().schedule(new Runnable() {
 				public void run() {
 					if(!(plugin.getReplant().isEmpty())){
 						Block loadedBlock = plugin.getReplant().get(0);
 						//remove it from the list
 						plugin.getReplant().remove(0);
 						//protect it
 						ProtectMe(loadedBlock);
 						//made into a sap
 						if( (loadedBlock.getType().equals(Material.AIR)) || (loadedBlock.getType().equals(Material.FIRE)) ) //just incase it was on fire
 							loadedBlock.setType(Material.SAPLING);
 						
 						/* No longer needed! Didn't realized leaves light blocking changed way back when.
 						 * for(int i=loadedBlock.getY(); i < 128; i++){
 							//delete all the leaves in a line to the sky limit
 							if(loadedBlock.getType()==Material.LEAVES){
 								//it's a leaf delete it
 								loadedBlock.setType(Material.AIR);
 								//update on server | Not needed?
 								//etc.getServer().setBlock(leafToDelete);
 							}
 							//go up one
 							loadedBlock = loadedBlock.getFace(BlockFace.UP);
 						}*/
 					}
 				}
 			}, plugin.getdelayTime(), TimeUnit.MILLISECONDS);
     	}
 	}
     
     public void ProtectMe(Block block){
 		//store the sapling into a list to protect the sapling
     	plugin.getProtect().add(block);
 		//run the timer to load the block
     	
     	if(plugin.getTreeprotect_Timer().getPoolSize() < plugin.getTreeprotect_Timer().getMaximumPoolSize()){
     		plugin.getTreeprotect_Timer().schedule(new Runnable() {
 				public void run() {
 					if(!(plugin.getProtect().isEmpty())){
 						//remove it from the list
 						plugin.getProtect().remove(0);
 					}
 				}
 			}, plugin.getProtectTime(), TimeUnit.MILLISECONDS);
     	}
 	}
 }
