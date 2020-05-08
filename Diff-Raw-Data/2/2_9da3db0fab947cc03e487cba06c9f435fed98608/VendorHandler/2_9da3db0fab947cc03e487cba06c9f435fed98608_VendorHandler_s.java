 package net.mayateck.VillagerVendor;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.block.Chest;
 // Giant import list. :l
 
 @SuppressWarnings("unused")
 public class VendorHandler implements Listener {
 	private VillagerVendor plugin;
 	public VendorHandler(VillagerVendor plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		this.plugin = plugin;
 	}
 	
 	public void interactWithVendor(String vsrc, Player plyr){
 		// Get vendor data.
 		String name = plugin.getVendorsList().getString(vsrc+".name");
 		int chestX = plugin.getVendorsList().getInt(vsrc+".chest.x");
 		int chestY = plugin.getVendorsList().getInt(vsrc+".chest.y");
 		int chestZ = plugin.getVendorsList().getInt(vsrc+".chest.z");
 		World world = plyr.getLocation().getWorld();
 		Block block = new Location(world, chestX, chestY, chestZ).getBlock();
 		if (block.getState() instanceof Chest){
 			// Make sure it's a chest. If not, we have a problem.
 			Chest chest = (Chest) block.getState();
 			ItemStack[] inv = chest.getInventory().getContents();
 			Inventory vendorInv = plugin.getServer().createInventory(null, 9*3, name); // Too lazy to do math. :3
 			// Null holder deletes inventory when closed, I think.
 			vendorInv.setContents(inv);
 			if (plyr.hasPermission("villagervendor.use.buy")){
 				String msg = "";
 				List<String> messages = plugin.getConfig().getStringList("settings.vendormessages");
 				int randMsg = (int) Math.floor(Math.random() * messages.size());
 				msg = messages.get(randMsg);
 				plyr.sendMessage(VillagerVendor.head+name+": "+msg);
 				// TODO: Charge for inventory modification.
 			} else {
 				plyr.sendMessage(VillagerVendor.head+"You don't have permission to buy anything here!");
 			}
 		} else {
 			plyr.sendMessage(VillagerVendor.head+"This vendor's chest is missing or invalid. Interact failed.");
 		}
 	}
 	
 	
 	@EventHandler
 	public void onVendorInteract(EntityDamageByEntityEvent evt){
 		Entity damager = evt.getDamager();
 		Entity vendor = evt.getEntity();
 		if (damager instanceof Player && vendor.getType()==EntityType.VILLAGER){
 			// Making sure it's not some random damage event. We want when a player punches a villager.
			plugin.getLogger().info("[DEBUD] Fired EntityDamageByEntityEvent from Player->Villager");
 			if (plugin.getVendorsList().contains("vendors.vendor_"+vendor.getEntityId())){
 				// Check if it's actually a vendor. If so, cancel the damage and call a method. Otherwise, let it happen.
 				Player plyr = (Player)damager;
 				evt.setCancelled(true);
 				plugin.getLogger().info("[DEBUG] Player holding "+plyr.getItemInHand().getTypeId());
 				if (plyr.hasPermission("villagervendor.use.view")){
 					if ((plyr.getItemInHand().getTypeId()!=plugin.getConfig().getInt("settings.debugID")) || !(plyr.hasPermission("villagervendor.general.debug"))){
 						String vsrc = "vendors.vendor_"+vendor.getEntityId();
 						this.interactWithVendor(vsrc, plyr);
 					} else {
 						plyr.sendMessage(VillagerVendor.head+"VENDOR_"+vendor.getEntityId()+".");
 						plyr.sendMessage(VillagerVendor.head+"For the time being, please edit vendors manually.");
 						plyr.sendMessage(VillagerVendor.head+"To remove, type "+ChatColor.RED+"/vendor delete [id]"+ChatColor.RESET+". (CANNOT BE UNDONE!)");
 					}
 				} else {
 					plyr.sendMessage(VillagerVendor.head+"Sorry! You don't have permission to use this vendor.");
 				}
 			}
 		}
 	}
 
 }
