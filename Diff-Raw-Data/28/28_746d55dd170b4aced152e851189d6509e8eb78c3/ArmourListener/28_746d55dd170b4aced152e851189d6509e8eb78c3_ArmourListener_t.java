 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 
 public class ArmourListener implements Listener {
 	
 	private Log log = new Log();
 
 	@EventHandler
 	public void onArmourEquip(InventoryClickEvent event){
 		if(event.getInventory().getType().equals(InventoryType.CRAFTING)){
 			
 			Player p = Bukkit.getPlayer(event.getWhoClicked().getName());
 			
 			if(event.isShiftClick() ){
				if (event.getCurrentItem() != null) {
					int iid = event.getCurrentItem().getTypeId();

					if (VaultPerms.Permissions.NOWEAR.has(p, iid) && !p.isOp()
							&& !VaultPerms.Permissions.ALLITEMS.has(p)) {
						if (Configuration.notifyNoHold()) {
							notifyPlayer(p, iid);
						}
						if (Configuration.notifyAdmins()) {
							notifyAdmin(p, iid);
						}
						event.setCancelled(true);
 					}
 				}
 				//Shift click checking
 			} else {
 				if (event.getSlotType().equals(SlotType.ARMOR)) {
 					if (event.getCursor() != null) {
						int iid = event.getCursor().getTypeId();
 						
 						if (VaultPerms.Permissions.NOWEAR.has(p, iid)
 								&& !p.isOp()
 								&& !VaultPerms.Permissions.ALLITEMS.has(p)) {
 							if (Configuration.notifyNoHold()) {
 								notifyPlayer(p, iid);
 							}
 							if (Configuration.notifyAdmins()) {
 								notifyAdmin(p, iid);
 							}
 							event.setCancelled(true);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void notifyPlayer(Player p, int iid) {
 		String dn = p.getDisplayName();
 		String w = p.getWorld().getName();
 
 		int x = p.getLocation().getBlockX();
 		int y = p.getLocation().getBlockY();
 		int z = p.getLocation().getBlockZ();
 
 		p.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 				+ StringHelper.replaceVars(Configuration.noWearMessage(), dn, w, x, y, z, iid));
 	}
 
 	private void notifyAdmin(Player p, int iid) {
 		String dn = p.getDisplayName();
 		String w = p.getWorld().getName();
 
 		int x = p.getLocation().getBlockX();
 		int y = p.getLocation().getBlockY();
 		int z = p.getLocation().getBlockZ();
 		String formatedMessage = StringHelper.replaceVars(Configuration.adminMessage(), dn, w, x, y,
 				z, iid);
 
 		log.log(formatedMessage);
 		Player[] players = Bukkit.getOnlinePlayers();
 		for (Player player : players)
 			if ((player.isOp()) || (VaultPerms.Permissions.ADMIN.has(player)))
 				player.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 						+ formatedMessage);
 	}
 }
