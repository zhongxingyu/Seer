 package com.ekstemicraft.plugin.echorses;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ECHorsesListeners implements Listener {
 	
 	ECHorses pl;
 	public ECHorsesListeners(ECHorses plugin){
 		pl = plugin;
 	}
 	
 		
 @EventHandler
 	public void horseEnter(VehicleEnterEvent event){
 
 	if(event.getVehicle() instanceof Horse){
 
       EntityType entered = (EntityType)event.getEntered().getType();
       if(entered == EntityType.PLAYER){ //Playercheck
     	  
     	  Player p = (Player)event.getEntered();
     	  String playername = p.getName();
     	  @SuppressWarnings("unused")
 		Server server = p.getServer(); //Not used for now
           Horse h = (Horse)event.getVehicle();
           AnimalTamer owner = h.getOwner();
           HashMap<String, ArrayList<Block>> map = ECHorses.hashmap; //Getting hashmap from main class
           if(map.containsKey(playername)){ //If player has done /horseunclaim, then when the player left click the horse, unclaim the horse.
         	  if(owner == null){
         		  p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.GREEN + " Nobody owns this horse!");
 
         		  return;
         	  }
         	  if(!(owner == null)){
         		  if(p.isOp() || p.hasPermission("echorse.override")){
         			  p.sendMessage("You are overriding horse protection!");
         			  h.setOwner(null); //Removing horse claim (untame horse)
         			  map.remove(playername); //Remove the player from hashmap.
         			  return;
         		  }
         		 if(h.getOwner().getName() == p.getName()){ //Owner check
         			Location loc = h.getLocation().clone();
         			Inventory inv = h.getInventory();
         			for (ItemStack item : inv.getContents()){ //Checking if the horse has saddle & armor & or something its inventory (Donkey)
         				if(item!= null){
         					loc.getWorld().dropItemNaturally(loc, item.clone()); //Drops all the inventory contents on the horse location ground.
         				}
         			}
         			inv.clear(); //Clears the inventory of the horse.
         			if(h.isCarryingChest()){ //If the horse is carrying a chest (Donkey)
         				h.setCarryingChest(false); //if true then remove the chest.
         			}
         			 h.setOwner(null); //Remove horse claim (untame horse.)
         			 p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.GOLD + " Successfully removed horse protection. (Horse is not tamed anymore)");
         			 map.remove(playername); //Remove the player from hashmap.
         			 event.setCancelled(true);
         			 return;
         		 }		  
         	  }
           }    
           //Horse stealing protection (Entering the horse)
           if(!(owner == null)){
         	  
         	  if(p.isOp() || p.hasPermission("echorse.override")){ //Overriding the protection if player is OP
         		  return;
         	  }
         	  if(h.getOwner().getName() == p.getName()){ //Checking if entering player is the owner
         	  return;
         	  }
         	  else {
         		  p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.RED + " This horse is owned by " + h.getOwner().getName());
         		  Bukkit.getLogger().info("[ECHorses] " + playername + " tried entering someone elses horse");
         		  event.setCancelled(true);
         		  return;
         	  }
           } 
        p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.GREEN + " This horse is untamed!"); 
       }		
 	}
 }
 @EventHandler
 public void horseTameEvent(EntityTameEvent event){ 
      
 	 if(event.getEntityType() == EntityType.HORSE){
 		 Player p = (Player)event.getOwner();
 		 p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.GOLD + " You have succesfully protected this horse!");
 		 p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.GREEN + " To unclaim your horse, use /horseunclaim");
 		 
 	 }	
 }
 @EventHandler
 public void horseDamageByEntity(EntityDamageByEntityEvent event){
 	if(event.getEntityType() == EntityType.HORSE){
 		Horse h = (Horse)event.getEntity();
 		
 		//Handle arrow shots from players
 		if(event.getDamager().getType() == EntityType.ARROW){ //Arrowcheck
 			Arrow a = (Arrow)event.getDamager();
 			if(a.getShooter() instanceof Player){
 				if(h.getOwner() == null){
 					return;
 				}
 				event.setCancelled(true);
 				return;
 			}
 		}
 		
 		//Handle melee attack direct from player
 		if(event.getDamager().getType() == EntityType.PLAYER){ //Playercheck
 			Player p = (Player)event.getDamager();
              
 			if(p.isOp() || p.hasPermission("echorse.override") || h.getOwner() == null){ //Op & permission check & if horse isnt tamed.
 				return;
 			}
 			 if(!(h.getOwner().getName() == p.getName())){ //If its not the horse owner, cancel the event
 				 event.setCancelled(true);
				 p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.RED + " You dont have permission to hurt " + h.getOwner().getName() + "'s horse!" );
 				 return;
 			 }
 		}		
 	return;	
 	}	
 }
  
 
 //Protect horse inventory stealing
 @EventHandler
 public void protectHorseInventory(InventoryOpenEvent event){
 	if(event.getInventory().getHolder() instanceof Horse){
 		Horse h = (Horse)event.getInventory().getHolder();
 		Player p = (Player) event.getPlayer();
 		String playername = event.getPlayer().getName();
 		if(p.isOp() || p.hasPermission("echorse.override")){ //Op & permission check
 			return;
 		}
 		if(!(h.getOwner().getName() == playername)){ //Not the horse owner, cancel event
 				
 			event.setCancelled(true);
			p.sendMessage(ChatColor.AQUA + "[ECHorses]" + ChatColor.RED + " You dont have permission to open " + h.getOwner().getName() + "'s horse inventory!");
 			return;
 		}
 		return;
 	}
 	return;	
 }
 
 }
