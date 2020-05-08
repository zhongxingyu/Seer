 package nl.rodey.personalchest;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 public class pchestPlayerListener extends PlayerListener {	
     private pchestManager chestManager;
 	private pchestMain plugin;
 	
     public ItemStack[] chestContents=null;
     
 	public pchestPlayerListener(pchestMain plugin, pchestManager chestManager) {
 		this.plugin = plugin;
         this.chestManager = chestManager;
 	}
     
     public void onPlayerInteract(PlayerInteractEvent event)
     {
     	
         Block block = event.getClickedBlock();
         if (block == null) return; 
         
         boolean cancel=false;
         
         
 	        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
 	        {
 	        	if(block.getType().equals(Material.CHEST)) 
 	            {
	        		if(chestManager.checkChestStatus(block))
 		        	{
			        	if (!plugin.checkpermissions(event.getPlayer(),"pchest.open",true))
 			        	{
			        		cancel = onChestInteract(block,event.getPlayer());
 			    		}
			        	else
			        	{
			        		cancel = true;
			        		event.getPlayer().sendMessage(ChatColor.GREEN + "[PersonalChest]" + ChatColor.WHITE + " You can't access this chest");
			        	}
		    		}
		    		else
		    		{
		    			cancel = false;
		    		}
 	            } 
 	            
 	            if(cancel) event.setCancelled(true);
 	        }   
 	        else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK))
 	        {	
 	            if(block.getType().equals(Material.CHEST))
 	            {
 	            	if(chestManager.checkChestStatus(block))
 	            	{
 	            		cancel = true;
 	            		event.getPlayer().sendMessage(ChatColor.GREEN + "[PersonalChest]" + ChatColor.WHITE + " This chest is protected.");
 	            		
 	                    if(cancel) event.setCancelled(true); 
 	            	}
 	            }
 	        } 
         
         if(cancel) event.setCancelled(true);    		
     }
     
     private boolean onChestInteract(Block block, Player player)
     {
     	
         // By default we cancel access to treasure chests
     	boolean cancel = true;
 
 		Chest chest = (Chest)block.getState();	
 		
 		if(chestManager.checkChestOpened(block))
 		{
 			cancel = true;
     		player.sendMessage(ChatColor.GREEN + "[PersonalChest]" + ChatColor.WHITE + " Chest is currently in use.");
 		}
 		else if(chestManager.load(player, chest, block))
 		{
 			chestManager.setChestOpened(block, player);
 			cancel = false;
 		}
 				
         return cancel;
     }    
 }
