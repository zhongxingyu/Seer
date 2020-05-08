 package com.alk.battleShops.listeners;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.alk.battleShops.Defaults;
 import com.alk.battleShops.Exceptions.SignFormatException;
 import com.alk.battleShops.controllers.MessageController;
 import com.alk.battleShops.controllers.PermissionController;
 import com.alk.battleShops.objects.ShopOwner;
 import com.alk.battleShops.objects.ShopSign;
 import com.alk.battleShops.objects.SignValues;
 import com.alk.battleShops.objects.WorldShop;
 
 /**
  * 
  *
  * @author Alkarin
  */
 public class BCSOnSignChangeListener implements Listener {
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onSignChange(SignChangeEvent event) {
 		if (Defaults.DEBUG_TRACE) System.out.println("onSignChange Event");
 
 		//    	if (event.isCancelled()) return;
 		final Block block = event.getBlock();
 		final Material type = block.getType();
 		/// Is this ever false? anyways onsignchange is a very low frequency event. best to be certain
 		if (!(type.equals(Material.SIGN) || type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN))) {
 			return;
 		}
 
 		/// Check to see if we have a shop sign
 		if ( event.getLines().length < 3 || !ShopSign.isShopSign(event.getLines())) {
 			return;
 		}
 
 		try{
 			String[] lines = event.getLines();
 			SignValues sv = ShopSign.parseShopSign(lines);
 
 			if (sv != null){
 				Player p = event.getPlayer();
 				if (!p.hasPermission("shop.create") && !PermissionController.isAdmin(p)){
 					MessageController.sendMessage(p, "&cYou don't have permissions to create a Shop Sign");
 					cancelAndDrop(event,block);
 					return;
 				}
 
 				Sign sign = (Sign)block.getState();
				ShopOwner so = new ShopOwner();
 				if (lines[0].toLowerCase().contains(Defaults.ADMIN_STR.toLowerCase()) &&
 						PermissionController.isAdmin(event.getPlayer())){
 					so.setName(Defaults.ADMIN_NAME);
 				}
 				ShopSign ss = new ShopSign(so, sign,sv);
 				ss.validate();
 				ss.setEventValues(event);
 				lines[3] = sv.coloredText; /// Allow for colored signs
 				WorldShop.addShopSign(ss);
 						//                System.out.println("adding shop sign " + ss);
 				if (!ss.isAdminShop()){
 					WorldShop.updateAffectedSigns(so,ss);
 					lines[1] = ss.getQuantityLine();
 					WorldShop.playerUpdatedShop(so.getName());            		
 				}
 			}
 		} catch (SignFormatException e){
 			event.getPlayer().sendMessage(e.getMessage());
 			cancelAndDrop(event,block);
 		} catch (Exception e){
 			e.printStackTrace();
 			cancelAndDrop(event,block);
 		}
 
 	}
 
 	public void cancelAndDrop(SignChangeEvent event, Block block){
 		event.setCancelled(true);
 		block.setType(Material.AIR);
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));   	
 	}
 
 }
