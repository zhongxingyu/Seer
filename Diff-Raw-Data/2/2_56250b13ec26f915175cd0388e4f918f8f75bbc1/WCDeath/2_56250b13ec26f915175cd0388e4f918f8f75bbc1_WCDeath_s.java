 package com.github.lyokofirelyte.WaterCloset;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class WCDeath implements Listener{
 
 	WCMain plugin;
 	public WCDeath(WCMain instance){
 	plugin = instance;
 	}
 	
 	  @EventHandler(priority=EventPriority.NORMAL)
 	  public void EDBEE(EntityDamageByEntityEvent e) {
 		  
 		  if (e.getDamager() instanceof Player){
 			  
 			  Player p = (Player) e.getDamager();
 			  
 			  if (p.getItemInHand().hasItemMeta()){
 					if (p.getItemInHand().getItemMeta().hasLore() && p.getItemInHand().getItemMeta().hasDisplayName()){
 						if (p.getItemInHand().getItemMeta().getDisplayName().toString().contains("HAMDRAX")){
 							short dur = p.getItemInHand().getDurability();
 							if (!p.getItemInHand().getType().equals(Material.DIAMOND_SWORD)){
 								WCMobDrops.swapDrax(Material.DIAMOND_SWORD, p, dur, "Sword");
 							}
 						}
 					}
 			  }
 		  }
 	  
 	  }
 	  
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onKerSmashSplode(EntityDeathEvent e){
 	
 		Entity ent = e.getEntity();
 		EntityDamageEvent ede = ent.getLastDamageCause();
 		DamageCause dc = ede.getCause();
 	
 		if (ent instanceof Player){
 			
 			Player p = (Player) ent;
 			int deaths = plugin.datacore.getInt("Users." + p.getName() + ".DeathCount");
 			deaths++;
 			plugin.datacore.set("Users." + p.getName() + ".DeathCount", deaths);
 			((PlayerDeathEvent) e).setDeathMessage(null);
 			String message = null;
 			
 			if (dc == DamageCause.ENTITY_ATTACK){
 				
				EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) ent;
 				message = tPD(p, dc, edee);
 				
 				
 			} else {
 				
 				message = tPD(p, dc);
 				
 			}
 			
 			Bukkit.broadcastMessage(message);
 			
 		}
 		
 	}
 	
 	public String tPD(Player p, DamageCause dc){
 		
 		Random rand = new Random();
 		List<String> dML = plugin.config.getStringList("Core.DeathMessages." + dc.toString());
 		int dMN = rand.nextInt(dML.size());
 		String message = dML.get(dMN);
 		
 		int deaths = plugin.datacore.getInt("Users." + p.getName() + ".DeathCount");
 		StringBuilder sb = new StringBuilder(message);
 		
 		sb.append(" &7( " + deaths + ")");
 		
 		message = "&o" + sb.toString();
 		message = WCMail.AS(message.replace("%p", p.getDisplayName() + "&r&o"));
 		
 		return message;
 		
 	}
 	
 	public String tPD(Player p, DamageCause dc, EntityDamageByEntityEvent ede){
 		
 		Random rand = new Random();
 		List<String> dML = plugin.config.getStringList("Core.DeathMessages." + dc.toString() + "." + ede.getDamager().getType().toString());
 		
 		if (dML == null){
 			
 			dML = plugin.config.getStringList("Core.DeathMessages." + dc.toString() + ".DEFAULT");
 			
 		}
 		
 		int dMN = rand.nextInt(dML.size());
 		String message = dML.get(dMN);
 		
 		int deaths = plugin.datacore.getInt("Users." + p.getName() + ".DeathCount");
 		StringBuilder sb = new StringBuilder(message);
 		
 		sb.append(" &7( " + deaths + ")");
 		
 		message = "&o" + sb.toString();
 		
 		if (ede.getDamager() instanceof Player){
 			
 			Player player = (Player) ede;
 			String pDN = player.getDisplayName();
 			ItemStack weapon = player.getItemInHand();
 			String wN = weapon.getItemMeta().getDisplayName();
 			
 			if (wN == null){
 				
 				wN = weapon.getType().name();
 				message = WCMail.AS(message.replace("%p", p.getDisplayName() + "&r&o").replace("%a", "&6" + pDN + "&r&o").replace("%i", "&6" + wN + "&r&o"));
 				
 			} else {
 				
 				message = WCMail.AS(message.replace("%p", p.getDisplayName() + "&r&o").replace("%a", "&6" + pDN + "&r&o").replace("%i", "&6" + wN + "&r&o"));
 				
 			}
 			
 		} else {
 			
 			String aTT = ede.getDamager().getType().name();
 			message = WCMail.AS(message.replace("%p", p.getDisplayName() + "&r&o").replace("%a", "&6" + aTT + "&r&o"));
 			
 		}
 		
 		return message;
 		
 	}
 	
 }
