 package tk.blackwolf12333.grieflog.listeners;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
import org.bukkit.entity.minecart.StorageMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.InventoryHolder;
 
 import tk.blackwolf12333.grieflog.GriefLog;
 import tk.blackwolf12333.grieflog.data.player.ChestAccessData;
 import tk.blackwolf12333.grieflog.utils.InventoryStringDeSerializer;
 import tk.blackwolf12333.grieflog.utils.logging.GriefLogger;
 
 public class InventoryListener implements Listener {
 
 	GriefLog plugin;
 	private static final int INDEX_NOT_FOUND = -1;
 	HashMap<String, String> inventories = new HashMap<String, String>();
 	
 	public InventoryListener(GriefLog plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onInventoryOpen(InventoryOpenEvent event) {
 		inventories.put(event.getPlayer().getName(), InventoryStringDeSerializer.InventoryToString(event.getView().getTopInventory()));
 	}
 	
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent event) {
 		if(event.getInventory().getType() == InventoryType.CHEST) {
 			InventoryHolder holder = event.getInventory().getHolder();
 			Integer chestX;
 			Integer chestY;
 			Integer chestZ;
 			String chestWorld;
 			
 			if(holder instanceof DoubleChest) {
 				DoubleChest chest = (DoubleChest) holder;
 				chestX = chest.getLocation().getBlockX();
 				chestY = chest.getLocation().getBlockY();
 				chestZ = chest.getLocation().getBlockZ();
 				chestWorld = chest.getWorld().getName();
			} else if(holder instanceof StorageMinecart) {
				StorageMinecart chest = (StorageMinecart) holder;
 				Location loc = chest.getLocation();
 				chestX = loc.getBlockX();
 				chestY = loc.getBlockY();
 				chestZ = loc.getBlockZ();
 				chestWorld = loc.getWorld().getName();
 			} else {
				Chest chest = (Chest) holder;
				chestX = chest.getX();
				chestY = chest.getY();
				chestZ = chest.getZ();
				chestWorld = chest.getWorld().getName();
 			}
 			
 			String player = event.getPlayer().getName();
 			String[] diff = new String[2];
 			
 			String before = inventories.get(player);
 			String after = InventoryStringDeSerializer.InventoryToString(event.getView().getTopInventory());
 			diff = difference(before, after);
 			if(diff == null) {
 				inventories.remove(event.getPlayer().getName());
 				return;
 			}
 			GriefLog.debug("Transaction by: " + player + " with taken: " + diff[0] + " put: " + diff[1]);
 			new GriefLogger(new ChestAccessData(player, chestX, chestY, chestZ, chestWorld, diff[0], diff[1]));
 		}
 		inventories.remove(event.getPlayer().getName());
 	}
 
 	public String[] difference(String str1, String str2) {
 		String[] ret = new String[2];
 		if (str1 == null) {
 	        ret[0] = str2;
 			return ret;
 	    }
 	    if (str2 == null) {
 	    	ret[0] = str1;
 	        return ret;
 	    }
 	    int at = indexOfDifference(str1, str2);
 	    if (at == INDEX_NOT_FOUND) {
 	        return null;
 	    }
 	    ret[0] = str1.substring(at);
 	    ret[1] = str2.substring(at);
 	    return ret;
 	}
 
 	public int indexOfDifference(CharSequence cs1, CharSequence cs2) {
 	    if (cs1 == cs2) {
 	        return INDEX_NOT_FOUND;
 	    }
 	    if (cs1 == null || cs2 == null) {
 	        return 0;
 	    }
 	    int i;
 	    for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
 	        if (cs1.charAt(i) != cs2.charAt(i)) {
 	            break;
 	        }
 	    }
 	    if (i < cs2.length() || i < cs1.length()) {
 	        return i;
 	    }
 	    return INDEX_NOT_FOUND;
 	}
 }
