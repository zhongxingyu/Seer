 package de.MrX13415.Massband.Listener;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.Listener;
 
 import de.MrX13415.Massband.Massband;
 import de.MrX13415.Massband.PlayerVars;
 import de.MrX13415.Massband.CommandExecuter.MassbandCommandExecuter;
 import de.MrX13415.Massband.Config.Config;
 
 
 public class MassbandPlayerListener implements Listener {
         
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Action action = Action.valueOf(new Config().defaultAction);
 		try {
 			action = Action.valueOf(Massband.configFile.defaultAction);
 		} catch (IllegalArgumentException e) {
 			Massband.log.warning(Massband.getConsoleOutputHeader() + " The value of the key 'Action', in the config is invalid. using default ...");
 		}
 		
     	if (event.getAction() == action) {
     		Player player = event.getPlayer();
     		Block block = event.getClickedBlock();
     	
         	//define tool that is used for Massband from the ID in the config-file.
         	ItemStack item = new ItemStack(Massband.configFile.itemID, player.getItemInHand().getAmount());
         	
         	//Player holds Item ...
         	if (player.getItemInHand().equals(item)) {
         		if (Massband.permissions()) {					
         			//use Permission
         			if (Massband.hasPermission(player, Massband.PERMISSION_NODE_Massband_use)) {
        				playerInteract(player, block, event);
         			}else{
         				player.sendMessage(String.format(Massband.language.PERMISSION_NOT, Massband.PERMISSION_NODE_Massband_use));
         				//event.setCancelled(true);	
         			}
     			}else{
     				//no Permission installed ! (op only)
 //    				if (player.isOp() && ButtonLock.getButtonLockConfig().oPOnly) {
 //    					playerInteract(player, block);
 //    				}else if (! ButtonLock.getButtonLockConfig().oPOnly){
 //    					playerInteract(player, block);
 //    				}else{
 //    				    event.setCancelled(true);	
 //    				}
    				playerInteract(player, block, event);
     			}
         	}
         		
 //    		if (Massband.permissionHandler != null && Massband.configFile.usePermissions) {
 //    			//use Permission
 //    			if (Massband.permissionHandler.permission(player, Massband.PERMISSION_NODE_Massband_use)) {
 //        			playerInteract(player, block);
 //    			}
 //			}else{
 //				//no Permission installed ! (everyone has access)
 //				playerInteract(player, block);
 //			}
     	}
     }
     
    private void playerInteract(Player player, Block block, PlayerInteractEvent event){
 
 		//get Vars for the current Player or create it.
 		PlayerVars tmpVars = Massband.getPlayerVars(player);
 	
 		if (tmpVars.isEnabled()){
			event.setCancelled(true);
 			//mode ?
 			if (tmpVars.getMode() == PlayerVars.MODE_SIMPLE) {
 				onModeSimple(tmpVars, player, block);
 			}else if (tmpVars.getMode() == PlayerVars.MODE_LENGTH) {
 				onModeLength(tmpVars, player, block);
 			}else if(tmpVars.getMode() == PlayerVars.MODE_SURFACE){
 				onModeSurface(tmpVars, player, block);
 			}
 		}
     }
     
     public void onModeSimple(PlayerVars tmpVars, Player player, Block block){
 		tmpVars.addPoint(block.getX(), block.getY(), block.getZ());
 		
 		printPoints(tmpVars, player, block);
 		
 		if (tmpVars.getWayPointListSize() >= 2) {
 			tmpVars.computingVectors();
 			MassbandCommandExecuter.onCommandLength(tmpVars, player); //output
 			tmpVars.removeAllWayPoints();	//clear all Points
 		}
     }
     
     public void onModeLength(PlayerVars tmpVars, Player player, Block block){
 		tmpVars.addPoint(block.getX(), block.getY(), block.getZ());
 		
 		printPoints(tmpVars, player, block);
 		
 		if (tmpVars.getWayPointListSize() >= 2) {
 			tmpVars.computingVectors();
 			MassbandCommandExecuter.onCommandLength(tmpVars, player); //output
 		}
     }
     
     public void onModeSurface(PlayerVars tmpVars, Player player, Block block){
 		tmpVars.addPoint(block.getX(), block.getY(), block.getZ());
 		if (tmpVars.getWayPointListSize() >= 3) {
 			tmpVars.removeAllWayPoints();	//clear all Points
 			tmpVars.addPoint(block.getX(), block.getY(), block.getZ());
 		}
 		
 		printPoints(tmpVars, player, block);
 		
 		if (tmpVars.getWayPointListSize() == 2) {
 			tmpVars.computingVectors();
 			tmpVars.calculateDiminsions();
 			MassbandCommandExecuter.onCommandDimensions(tmpVars, player);	//output
 		}
     }
     
     /** send a Message to the Player, which conntains the current Points
      * 
      * @param tmpVars
      * @param player
      * @param block
      */
     private void printPoints(PlayerVars tmpVars, Player player,  Block block){
     	int size = tmpVars.getWayPointListSize();
     	
     	if (size == 1){ 
     		if (tmpVars.getMode() == PlayerVars.MODE_LENGTH) {
     			player.sendMessage(Massband.language.MODE_LENGTH);
 			}else if(tmpVars.getMode() == PlayerVars.MODE_SURFACE){
 				player.sendMessage(Massband.language.MODE_SURFACE);
 //			}else if(tmpVars.getMode() == PlayerVars.MODE_SIMPLE){
 //				player.sendMessage(Massband.language.MODE_SIMPLE);
 			}
     	}
     	
 			player.sendMessage(String.format(Massband.language.POINT, size, block.getX(), block.getY(), block.getZ(), tmpVars.getIgnoredAxesAsString()));
     }
    
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
     	Massband.removePlayer(event.getPlayer());
 	}
 
 }
