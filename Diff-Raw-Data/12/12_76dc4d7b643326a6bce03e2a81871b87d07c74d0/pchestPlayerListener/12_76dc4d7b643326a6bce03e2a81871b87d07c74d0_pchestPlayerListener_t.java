 package nl.rodey.personalchest;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
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
 	        			if(!plugin.SpoutLoaded)
 	        			{
 			        		cancel = true;
 	        				event.getPlayer().sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " You can't access this chest at this moment");
 	        			}
	        			else if (event.getPlayer().hasPermission("pchest.open") || event.getPlayer().isOp())
 			        	{
 			        		cancel = onChestInteract(block,event.getPlayer());
			        	}
 			        	else
 			        	{
	        				cancel = true;
	        				event.getPlayer().sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " You can't access this chest");
			    		}
 		    		}
 		    		else
 		    		{
 		    			cancel = false;
 		    		}
 	            } 
 	            
 	            if(cancel) event.setCancelled(true);
 	        }
 	        /*
 	        else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK))
 	        {	
 	            if(block.getType().equals(Material.CHEST))
 	            {
 	            	if(chestManager.checkChestStatus(block))
 	            	{
 	            		event.getPlayer().sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " This chest is registerd.");
 	            	}
 	            }
 	        } 
 	        */
         
         if(cancel) event.setCancelled(true);    		
     }
     
     private boolean onChestInteract(Block block, Player player)
     {
     	
         // By default we cancel access to treasure chests
     	boolean cancel = true;
 	
 		if(chestManager.checkChestOpened(block, player))
 		{
 			cancel = true;
     		player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " Chest is currently in use.");
 		}
 		else if(chestManager.load(player, block))
 		{
 			cancel = false;
 		}
 				
         return cancel;
     }    
 }
