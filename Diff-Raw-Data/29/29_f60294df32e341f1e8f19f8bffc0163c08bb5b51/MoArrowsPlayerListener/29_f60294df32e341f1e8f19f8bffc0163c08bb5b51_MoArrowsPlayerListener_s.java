 package com.gmail.l0g1clvl.MoArrows;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import java.util.List;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Arrow;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.MaterialData;
 import com.gmail.l0g1clvl.MoArrows.arrows.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 
 /**
  * Changes arrow types, fires arrows
  * @author MrAverage with code from ayan4m1
  */
 
 public class MoArrowsPlayerListener implements Listener {
 	
 	public static int[] matsReqd;
 	public static String[] parse1;
 	public static ItemStack[] stack;
 	public static List <String> matsList;
 	
 	MoArrows moArrows = new MoArrows();
 	private MaterialHandler materialHandler;
 
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (moArrows.activeArrowType.containsKey(event.getPlayer())) {
 			moArrows.activeArrowType.remove(event.getPlayer());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		String message = "";
 		Player player = event.getPlayer();
 		if (player.getItemInHand().getType() == Material.BOW) {
 			if (event.getAction() == Action.RIGHT_CLICK_AIR	|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				if (!moArrows.activeArrowType.containsKey(player)) {
 					moArrows.activeArrowType.put(player, ArrowType.Normal);
 				}
 
 				ArrowType arrowType = moArrows.activeArrowType.get(player);
 				
 			} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				if (moArrows.activeArrowType.containsKey(player)) {
 					int arrowTypeIndex = moArrows.activeArrowType.get(player).ordinal();
 					
 					do {
 						if (!player.isSneaking()) {
 							if (arrowTypeIndex == ArrowType.values().length - 1) {
 								arrowTypeIndex = 0;
 							} else {
 								arrowTypeIndex++;
 							}
 						} else {
 							if (arrowTypeIndex == 0) {
 								arrowTypeIndex = ArrowType.values().length - 1;
 							} else {
 								arrowTypeIndex--;
 							}
 						} 
 						
 						moArrows.activeArrowType.put(player, ArrowType.values()[arrowTypeIndex]);
 					
					} while (moArrows.removedArrows.contains(moArrows.activeArrowType.get(player).toString().toLowerCase()) || !player.hasPermission("moarrows.use." + moArrows.activeArrowType.get(player).toString().toLowerCase()));
 					
 
 				} else {
 					moArrows.activeArrowType.put(player, ArrowType.Normal);
 				}
 
 				// Figure out the perission node name
 				String specificPerm = "" + moArrows.activeArrowType.get(player).toString().toLowerCase();
 				
 				if (player.hasPermission("moarrows.use.all")) {
 					ArrowType arrowType = moArrows.activeArrowType.get(player);
 					message = ChatColor.BLUE + "You select " + moArrows.activeArrowType.get(player).toString() + " arrows.";
 					player.sendMessage(message);
 				} else if (player.hasPermission("moarrows.use." + specificPerm) || !player.isPermissionSet("moarrows.use." + specificPerm)) {
 					ArrowType arrowType = moArrows.activeArrowType.get(player);
 					message = ChatColor.BLUE + "You select " + moArrows.activeArrowType.get(player).toString() + " arrows.";
 					player.sendMessage(message);
 				} //else if (!player.hasPermission("moarrows.use." + specificPerm)) {
 					//moArrows.activeArrowType.put(player, ArrowType.Normal);
 				//}
 			}
 		}
 	}
 	
 	
 	@EventHandler
     public void playerBowShoot(EntityShootBowEvent e) {
 		
 		MoArrowsEntityListener mael = new MoArrowsEntityListener();
         Entity entity = e.getEntity();
         Float speed = e.getForce();
         Entity arrow = e.getProjectile();
         
         Player player = (Player) entity;
         int arrowNum = e.getProjectile().getEntityId(); //IMPORTANT!!!
         //      ^--- this is the custom ID for that arrow!
         
 //--------------------------MATERIALS NEEDED CODE----------------------------------
         
         Inventory inventory = player.getInventory();
         ItemStack[] stack = new ItemStack[10];
         
         String l = "" + moArrows.activeArrowType.get(player);
         
         if (l.contains("Explosive"))
         	stack = materialHandler.removedItemStacks.get("explosive");
         else if (l.contains("Poison"))
         	stack = materialHandler.removedItemStacks.get("poison");
         else if (l.contains("Water"))
         	stack = materialHandler.removedItemStacks.get("water");
         else if (l.contains("Drill"))
         	stack = materialHandler.removedItemStacks.get("drill");
         else if (l.contains("Lightning"))
         	stack = materialHandler.removedItemStacks.get("lightning");
         else if (l.contains("Torch"))
         	stack = materialHandler.removedItemStacks.get("torch");
         else if (l.contains("Teleport"))
         	stack = materialHandler.removedItemStacks.get("teleport");
         else if (l.contains("Animal"))
         	stack = materialHandler.removedItemStacks.get("animal");
         else if (l.contains("Razor"))
         	stack = materialHandler.removedItemStacks.get("razor");
         else if (l.contains("Slow"))
         	stack = materialHandler.removedItemStacks.get("slow"); 
         else		//all lower case! if not, it will kick it here!
         	stack = null;
         
         if (stack != null) {
         
 	        //get stack size	
 	        int len = 0;
 	        for (int y = 0; y < stack.length; y++) {
 	        	if (stack[y] != null) {
 	        		len++;
 	        	} 
 	        }
 	        
 	        // Check for all the right materials..
 	        Boolean hasMaterials = true;
 	        for (int u = 0; u < len; u++) {
 	        	int temp1 = stack[u].getTypeId();
 	        	int temp2 = stack[u].getAmount();
 	        	if (!inventory.contains(temp1, temp2)) {
 	        		hasMaterials = false;
 	        		break;
 	        	}
 	        }
 	        
 	        if (!hasMaterials) {
 	        	player.sendMessage(ChatColor.RED + "You dont have enough materials for that type of arrow!");
 	        } else {
 	        	for (int k = 0; k < len; k++) {
 		        	inventory.removeItem(stack[k]);
 		        }
 	        	
 	        	//player is good to fire a special arrow by this point..
 	        	//associate a custom ID to an arrow entity with a specific arrow type
 	            for (int j = 0; j < 100; j ++) {
 	            	if (moArrows.arrowID[j] == "") {
 	            		if (player.isSneaking())
 	            			moArrows.arrowID[j] = "" + arrowNum + "." + moArrows.activeArrowType.get(player) + ".c";
 	            		else
 	            			moArrows.arrowID[j] = "" + arrowNum + "." + moArrows.activeArrowType.get(player) + ".s";
	        	    moArrows.log.info("#debug arrow id:" + moArrows.arrowID[j]);
 	        	    break;
 	            	}
 	            }
 	        }
         }
     }
 }
 
