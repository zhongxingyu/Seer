 package me.shock.avatarpvp;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class FireListener implements Listener 
 {
 
 	public Main plugin;
 	
 	public FireListener(Main instance)
 	{
 		this.plugin = instance;
 	}
 	
 	public HashMap<String, Long> fireball = new HashMap<String, Long>();
 	public HashMap<String, Long> lightning = new HashMap<String, Long>();
 	String apvp = ChatColor.BLUE + "[" + ChatColor.WHITE + "AvatarPvP" + ChatColor.BLUE + "]" + ChatColor.WHITE + ": ";
 
 	@EventHandler
 	public void onFireball(PlayerInteractEvent event)
 	{
 		
 		//long fireballCool = (long) plugin.getConfig().getInt("FireNation.fireball.cooldown");
 		//long lightningCool = (long) plugin.getConfig().getInt("FireNation.lightning.cooldown");
 		int lightningRadius = plugin.getConfig().getInt("FireNation.lightning.radius");
 		int lightningDamage = plugin.getConfig().getInt("FireNation.lightning.damage");
 		long lightningCool = 30;
 		long fireballCool = 30;
 		
 		Player player = event.getPlayer();
 		Action action = event.getAction();
 		String name = player.getName();
 		
 		if((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.getItemInHand().getAmount() > 0)
 		{
 			ItemStack itemStack = player.getItemInHand();
 			ItemMeta meta = itemStack.getItemMeta();
 			List<String> lore = meta.getLore();
 			if(itemStack.hasItemMeta())
 			{
 				/**
 				 * Fireball ability
 				 */
 				
				if(itemStack.getItemMeta().hasEnchants())
					return;
				
 				if(lore.contains(ChatColor.DARK_RED + "FireBall"))
 				{
 					/**
 					 * Check if player has used it before.
 					 * If not then let them do it then add them to the timer.
 					 */
 					
 					if(fireball.containsKey(name))
 					{
 						long diff = (System.currentTimeMillis() - fireball.get(name)) / 1000;
 						if(diff < fireballCool)
 						{
 							player.sendMessage(apvp + "You must wait " + ChatColor.RED + (fireballCool - diff) + ChatColor.WHITE + " seconds before using this again.");
 							return;
 						}
 						else
 						{
 							player.launchProjectile(Fireball.class);
 							player.launchProjectile(Fireball.class);
 							player.launchProjectile(Fireball.class);
 							fireball.put(name, System.currentTimeMillis());
 							player.sendMessage(apvp + "You launched a " + ChatColor.DARK_RED + "fireball" + ChatColor.WHITE + ".");
 							return;
 						}
 					}
 					
 					else
 					{
 						player.launchProjectile(Fireball.class);
 						player.launchProjectile(Fireball.class);
 						player.launchProjectile(Fireball.class);
 						fireball.put(name, System.currentTimeMillis());
 						player.sendMessage(apvp + "You launched a " + ChatColor.DARK_RED + "fireball" + ChatColor.WHITE + ".");
 						return;
 					}
 				}
 				
 				/**
 				 * Lightning ability
 				 */
 				
 				if(lore.contains(ChatColor.DARK_RED + "Lightning"))
 				{
 					/**
 					 * Check if player has used it before.
 					 * If not then let them do it and add to hashmap.
 					 */
 					
 					if(lightning.containsKey(name))
 					{
 						long diff = (System.currentTimeMillis() - lightning.get(name) / 1000);
 						if(diff <= lightningCool)
 						{
 							List<Entity> nearby = player.getNearbyEntities(lightningRadius, lightningRadius, lightningRadius);
 							
 							/**
 							 * Strike lightning and damage all living entities
 							 * that aren't players of same nation.
 							 */
 							for(Entity ents : nearby)
 							{
 								if(ents instanceof Player)
 								{
 									Player playerlit = (Player) ents;
 									if(playerlit.hasPermission("avatarpvp.fire"))
 									{
 										player.sendMessage(apvp + name + " just used " + ChatColor.DARK_RED + "lightning " + ChatColor.WHITE + "in your area.");
 									}
 									else
 									{
 										playerlit.getLocation().getWorld().strikeLightningEffect(playerlit.getLocation());
 										playerlit.damage(lightningDamage);
 									}
 								}
 								else if(!(ents.isDead()))
 								{
 									LivingEntity liveent = (LivingEntity) ents;
 									liveent.getLocation().getWorld().strikeLightningEffect(ents.getLocation());
 									liveent.damage(lightningDamage);
 								}
 								else
 								{
 									// Do nothing.
 								}
 							}
 						}
 						else
 						{
 							player.sendMessage(apvp + "You must wait " + ChatColor.RED + diff + " seconds to use this again.");
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * FireBow thing.
 	 * start ground on fire.
 	 */
 	
 	@EventHandler
 	public void onFireArrowHit(ProjectileHitEvent event)
 	{
 		Entity entity = event.getEntity();
 		if(entity instanceof Arrow)
 		{
 			Entity shooter = ((Arrow) entity).getShooter();
 			if(shooter instanceof Player)
 			{
 				Player player = (Player) shooter;
 				if(player.hasPermission("avatarpvp.fire.firebow") && player.getItemInHand() != null)
 				{
 					ItemStack inHand = player.getItemInHand();
 					if(inHand.getType() == Material.BOW)
 					{
 						Location loc = event.getEntity().getLocation();
 						loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0, true, false);
 					}
 				}
 			}
 		}
 	}
 	
 }
