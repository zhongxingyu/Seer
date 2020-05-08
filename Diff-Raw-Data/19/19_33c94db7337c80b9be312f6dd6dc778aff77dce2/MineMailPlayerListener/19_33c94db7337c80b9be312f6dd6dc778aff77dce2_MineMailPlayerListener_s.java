 package minemail;
 
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.Configuration;
 
 public class MineMailPlayerListener extends PlayerListener{
 	MineMail plugin;
	Configuration config;
 	
 	public MineMailPlayerListener(MineMail mineMail){
 		plugin = mineMail;
		config = plugin.getConfiguration();
 	}
 	
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 			if(event.getClickedBlock().getType().equals(Material.CHEST)){
 				MailChest chest = new MailChest(event.getClickedBlock(),config);
 				try {
 					if(event.getItem().getType().equals(Material.ARROW))
 					{
 						String playerowner = chest.getOwnerName();
 						if(playerowner == null) {
 							chest.setOwner(event.getPlayer());
 							event.getPlayer().sendMessage("This chest is now registered as your mailbox.");
 							event.setCancelled(true);
 						} else {
 							event.getPlayer().sendMessage("This chest is already registered.");
 							event.setCancelled(true);
 						}
 					}
 					
 				} catch(Exception e) {
 					//Do nothing
 				}
 				
 				if(!chest.getOwnerName().equals(event.getPlayer().getDisplayName().toLowerCase())) {
 					event.getPlayer().sendMessage("This ain't your mailbox, bro.");
 					event.setCancelled(true);
 				}
 		}
 			
 			if(event.getClickedBlock().getType().equals(Material.SIGN)){
 				Sign sign = (Sign) event.getClickedBlock().getState(); //Added proper casting
 				String[] textLines = sign.getLines();
 				if(textLines[0].equalsIgnoreCase("[MineMail]")){
 					//Send the item to the mailbox of player textLines[1]
 	    				if(event.getItem().getAmount() == 1){
 	    					event.getPlayer().getInventory().remove(event.getItem());
 	    				}
 	    				else {
 	    						event.getItem().setAmount(event.getItem().getAmount()-1);
 	    				}
 				}
 			}
 		}
 	}
 }
