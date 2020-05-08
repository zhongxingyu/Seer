 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class DropListener implements Listener{
 	@EventHandler
 	public void onItemDrop(PlayerDropItemEvent event){
 		Player p = event.getPlayer();
 		ItemStack stack = new ItemStack(event.getItemDrop().getItemStack());
 		int iid = stack.getTypeId();
		if(Tools.isTool(iid) /*TODO: or armor*/){
 			stack.setDurability((short) 0);
 		}
 		if(Perms.NODROP.has(p, stack)){
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.DROP, iid);
 			StringHelper.notifyAdmin(p, EventTypes.DROP, stack);
 		}
 	}
 }
