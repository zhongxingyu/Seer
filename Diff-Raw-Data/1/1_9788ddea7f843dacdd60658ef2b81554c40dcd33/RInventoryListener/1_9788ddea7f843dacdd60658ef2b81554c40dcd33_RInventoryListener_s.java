 package net.croxis.plugins.research;
 
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import org.getspout.spoutapi.event.inventory.InventoryListener;
 
 public class RInventoryListener extends InventoryListener{
 	public void onInventoryCraft(InventoryCraftEvent event){
 		if (event.getPlayer().hasPermission("research"))
 			if(TechManager.players.get(event.getPlayer()).cantCraft.contains(event.getResult().getTypeId()) && event.getPlayer().hasPermission("research"))
 				event.setCancelled(true);
 	}
 
 }
