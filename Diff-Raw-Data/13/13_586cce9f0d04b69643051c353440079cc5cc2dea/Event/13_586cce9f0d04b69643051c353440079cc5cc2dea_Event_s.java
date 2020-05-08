 package tsuttsu305.Main;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffectType;
 
 public class Event implements Listener {
 	
 	
 
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPlayeruse(PlayerInteractEvent event) {
		Main Main = new Main();
 		if ((event instanceof PlayerInteractEvent))
 		{
 			PlayerInteractEvent aaaa = event;
 			if (aaaa.useItemInHand() != null) {
 				Player player = aaaa.getPlayer();
 				ItemStack it = player.getItemInHand();
 				Material abc = it.getType();
 
 				if (abc == Material.POTION)
 				{
 					Potion po = Potion.fromItemStack(it);
 
 					PotionEffectType poet = po.getType().getEffectType();
 
 					if (poet == PotionEffectType.INVISIBILITY){
 						
 						//Alllow op true
 						if (Main.AllowOP){
 							if ((player.hasPermission("invisibility.on")) || (player.isOp())) {
 								return;
 							}else{
 							event.setCancelled(true);
 							
 							
 							player.sendMessage(ChatColor.RED + Main.Msg);
 							
 							return;
 							}
 														
 						}
 						else //Allow OP false
 						{
 							if ((player.hasPermission("invisibility.on"))) {
 								return;
 							}else{
 							event.setCancelled(true);
 							
 							
 							player.sendMessage(ChatColor.RED + Main.Msg);
 							
 							return;
 							}
 							
 						}
 						
 						
 					}
 				}
 				return;
 			}
 		}
 	}
 
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onDispense(BlockDispenseEvent event){
 		if (event instanceof BlockDispenseEvent){
 			BlockDispenseEvent BDE = event;
 			ItemStack item = BDE.getItem();
 			Material mat = item.getType();
 
 			if (mat == Material.POTION)
 			{
 				Potion po = Potion.fromItemStack(item);
 
 				PotionEffectType poet = po.getType().getEffectType();
 
 				if (poet == PotionEffectType.INVISIBILITY){
 					BDE.setCancelled(true);
 					return;
 
 
 
 				}
 			}
 		}
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
