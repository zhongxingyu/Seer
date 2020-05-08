 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class ToolListener implements Listener{
 	
 	public ToolListener(){
 		
 	}
 	
 	private Log log = new Log();
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		
 		Player p = event.getPlayer();
 		int id = p.getItemInHand().getTypeId();
 		ItemStack item = new ItemStack(p.getItemInHand());
 		//Do this to prevent accidentally using armor or tool damage values
 		if(Tools.tools.containsKey(id) || Armour.armours.containsKey(id)){
 			item.setDurability((short) 0);
 		}
 		
 		log.debug("BlockBreakEvent fired. ".concat(Integer.toString(id)));
 		
 		if(Perms.NOUSE.has(p, item)){
 			event.setCancelled(true);
 			if (Configuration.notifyNoUse()) {
 				StringHelper.notifyPlayer(p, Configuration.noUseMessage(), id);
 			}
 			if (Configuration.notifyAdmins()) {
 				StringHelper.notifyAdmin(p, EventTypes.USE, p.getItemInHand());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onLandFarm(PlayerInteractEvent event){
 		Block block = event.getClickedBlock();
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
 			if(block.getType().equals(Material.DIRT) || block.getType().equals(Material.GRASS)){
 				Player p = event.getPlayer();
				ItemStack stack = p.getItemInHand();
 				int inHand = stack.getTypeId();
 				if(inHand >= 290 && inHand <= 294){
 					stack.setDurability((short) 0);
 					//Use a data value of 0 because it is a tool and is not wanted!
 					if(Perms.NOUSE.has(p, stack)){
 						event.setCancelled(true);
 						if (Configuration.notifyNoUse()) {
 							StringHelper.notifyPlayer(p, Configuration.noUseMessage(), inHand);
 						}
 						if (Configuration.notifyAdmins()) {
 							StringHelper.notifyAdmin(p, EventTypes.USE, stack);
 						}
 					}
 				}
 			}
 		}
 	}
 	//TODO: Add bow support
 }
