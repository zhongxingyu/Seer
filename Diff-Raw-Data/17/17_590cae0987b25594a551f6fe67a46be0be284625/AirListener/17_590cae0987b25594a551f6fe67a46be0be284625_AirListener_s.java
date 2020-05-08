 package me.shock.avatarpvp;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class AirListener implements Listener 
 {
 
 	public Main plugin;
 	
 	public AirListener(Main instance)
 	{
 		this.plugin = instance;
 	}
 	
 	/**
 	 * Hashmaps and configuration stuff.
 	 */
 	String apvp = ChatColor.BLUE + "[" + ChatColor.WHITE + "AvatarPvP" + ChatColor.BLUE + "]" + ChatColor.WHITE + ": ";
 	String noperm = apvp + "You don't have permission to use this.";
 	
 	public HashMap<String, Long> fly = new HashMap<String, Long>();
 	public HashMap<String, Long> inFlight = new HashMap<String, Long>();
 	
 	//long flyCool = plugin.getConfig().getInt("AirNation.fly.cooldown");
 	long flyCool = 30;
 
 	
 	
 	/**
 	 * Flight listener
 	 */
 	
 	@EventHandler
 	public void onAirFlight(PlayerInteractEvent event)
 	{
 		Player player = event.getPlayer();
 		Action action = event.getAction();
 		if((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && player.getItemInHand().getAmount() > 0)
 		{
 			ItemStack inHand = player.getItemInHand();
 			ItemMeta meta = inHand.getItemMeta();
 			List<String> lore = meta.getLore();
 			if(inHand.hasItemMeta())
 			{
 				/**
 				 * Flight ability.
 				 * Allow player to fly for 5 seconds.
 				 * then run a task to stop it.
 				 */
				
 				if(lore.contains(ChatColor.AQUA + "Fly"))
 				{
 					if(player.hasPermission("avatarpvp.air.fly"))
 					{
 						/**
 						 * Check if a player has used it before
 						 * If not then let them do it and add to hashmap
 						 * If yes then check cooldown and let them know
 						 * how much time they have left.
 						 */
 						
 						if(fly.containsKey(player.getName()))
 						{
 							long diff = (System.currentTimeMillis() - fly.get(player.getName())) / 1000;
 							if(flyCool < diff)
 							{
 								player.setVelocity(player.getVelocity().setY(2D));
 								fly.put(player.getName(), System.currentTimeMillis());
 								inFlight.put(player.getName(), System.currentTimeMillis());
 								return;
 							}
 							else
 							{
 								player.sendMessage(apvp + "You must wait " + ChatColor.RED + (flyCool - diff) + ChatColor.WHITE + " seconds before using this again.");
 								return;
 							}
 						}
 						else
 						{
 							player.setVelocity(player.getVelocity().setY(2D));
 							fly.put(player.getName(), System.currentTimeMillis());
 							inFlight.put(player.getName(), System.currentTimeMillis());
 							return;
 						}
 						
 					}
 					else
 					{
 						player.sendMessage(noperm);
 						return;
 					}
 				}
 				else
 					return;
 			}
 		}
 	}
 	
 	/**
 	 * Reduce fall damage if
 	 * using the flying ability.
 	 */
 	
 	@EventHandler
 	public void onAirFall(EntityDamageEvent event)
 	{
 		if ((event.getCause() == EntityDamageEvent.DamageCause.FALL) && ((event.getEntity() instanceof Player))) 
 		{
 			Player player = (Player) event.getEntity();
 			if(inFlight.containsKey(player.getName()))
 			{
 				long diff = (System.currentTimeMillis() - inFlight.get(player.getName())) / 1000;
 
 				if (diff < 10)
 				{
 					event.setDamage(event.getDamage() / 5);
 					inFlight.remove(player.getName());
 				}
 			}
 			
 		      return;
 		}
 	}
 }
