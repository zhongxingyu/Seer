 package com.github.bobacadodl.Masks;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 import pgDev.bukkit.DisguiseCraft.Disguise;
 
 public class MasksListener implements Listener{
 
     private Masks masks;
 	public MasksListener(Masks instance){masks = instance;}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerKillEvent(PlayerDeathEvent event){
 		Player killed = event.getEntity();
 		if(killed.getLastDamageCause().equals(org.bukkit.entity.EntityType.PLAYER)==false) return;
 		Player killer = event.getEntity().getKiller();
 		if(killer.getItemInHand().equals(Material.getMaterial(masks.getConfig().getInt("Item-to-kill")))){
 			Short mapid = masks.maskshash.get(killed.getName());
 			ItemStack playermask = new ItemStack(Material.MAP, 1 , mapid);
 			Location killedloc = killed.getLocation();
 			killed.getWorld().dropItemNaturally(killedloc, playermask);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerRightClickEvent(PlayerInteractEvent event){
 		Player p = event.getPlayer();
 		if(p.getItemInHand().getType().equals(Material.MAP) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
 			if(masks.maskshash.inverse().containsKey(p.getItemInHand().getDurability())){
 				
 				if(masks.confirmed.contains(p.getName())){
 					Short maskid = p.getItemInHand().getDurability();
 					String maskname = masks.maskshash.inverse().get(maskid);
 					Disguise maskdisguise = new Disguise(masks.dcAPI.newEntityID(), maskname, null);
 					if(masks.dcAPI.isDisguised(p)){
 						p.sendMessage(ChatColor.RED+"You are already disguised! Please undisguise first!");
 						masks.confirmed.remove(p.getName());
 						return;
 					}
 					p.getInventory().clear(p.getInventory().getHeldItemSlot());
 					masks.dcAPI.disguisePlayer(p, maskdisguise);
 					p.sendMessage(ChatColor.GREEN+"You have been disguised as "+maskname+"!");
 					masks.confirmed.remove(p.getName());
 					return;
 				}
 				masks.confirmed.add(p.getName());
 				p.sendMessage(ChatColor.GREEN+"Right click again to confirm! If not, say /masks cancel!");
 			}
 		}
 	}
	public void onPlayerGetHurtEvent(EntityDamageByEntityEvent event){
		if(event.getDamager().getType().equals(EntityType.PLAYER) && event.getEntityType().equals(EntityType.PLAYER)){
			Player p = (Player) event.getEntity();
			if(masks.dcAPI.isDisguised(p)){
				masks.dcAPI.undisguisePlayer(p);
				p.sendMessage("Your disguise has worn off!");
			}
		}
	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoinEvent(PlayerJoinEvent event) {
 	  if(!masks.maskshash.containsKey(event.getPlayer().getName())) {
 	      //if the player is loggin on for first time
 		  //create a mask for them!
 		  Player p = event.getPlayer();
 		  masks.maskshash.put(p.getName(), masks.createMap(p));
 	  }
 	}
 
 }
