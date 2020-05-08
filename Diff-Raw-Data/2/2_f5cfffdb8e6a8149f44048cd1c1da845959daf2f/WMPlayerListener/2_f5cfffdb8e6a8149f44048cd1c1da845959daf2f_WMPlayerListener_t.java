 import java.io.IOException;
 
 public class WMPlayerListener extends PluginListener {
 
 	public void onLogin(Player player) {
 		
 		try {
 			if(Updater.isUpdateAvailable()) {
 				player.sendMessage(Colors.Navy + "WorldManager" + Colors.White + " is out of date, please download the newest Version (" + Colors.Green + "v" + Updater.getNewestVersion() + Colors.White + ")");
 			} else {
 				player.sendMessage(Colors.Navy + "WorldManager" + Colors.White + " is up to date!");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		if (!PlayerLocationsFile.getInstance().hasPlayer(player)) {
 			PlayerLocationsFile.getInstance().addPlayer(player);
 		} else {
 			PlayerLocation pl = PlayerLocationsFile.getInstance().getLocationOf(player);
 			World[] world = etc.getServer().getWorld(pl.worldName);
 			if (world != null && world[0] != null) {
				WMEventManager.processWorldTeleport(player, pl.worldName);
 			} else {
 				WorldManager.mclogger.info("[WorldManager] Could not change the world of " + player.getName() + ", maybe " + pl.worldName + "is not existing anymore?");
 			}
 			player.setX(pl.x);
 			player.setY(pl.y);
 			player.setZ(pl.z);
 		}
 		
 		InventoryManager.loadInventory(player);
 	}
 
 	public void onDisconnect(Player player) {
 		PlayerLocationsFile.getInstance().updateLocation(player);
 		InventoryManager.saveInventory(player);
 		}
 
 	public void onBan(Player mod, Player player, String reason) {
 		PlayerLocationsFile.getInstance().updateLocation(player);
 		InventoryManager.saveInventory(player);
 	}
 
 	public void onKick(Player mod, Player player, String reason) {
 		PlayerLocationsFile.getInstance().updateLocation(player);
 		InventoryManager.saveInventory(player);
 	}
 
 }
