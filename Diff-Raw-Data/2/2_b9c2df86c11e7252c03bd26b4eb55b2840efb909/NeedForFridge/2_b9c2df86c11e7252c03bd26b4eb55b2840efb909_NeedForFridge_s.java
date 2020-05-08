 package plugin;
 
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NeedForFridge extends JavaPlugin implements Listener {
 
 	ChestTick chesttick = null;
 	SilverfishTick silverfishtick = null;
 	HashMap<Player,Integer> lastMsg1 = null;
 	HashMap<Player,Integer> lastMsg2 = null;
 	HashMap<Player,Integer> lastMsg3 = null;
 	
 	public void onEnable(){
 		chesttick = new ChestTick(getServer(), this);
 		silverfishtick = new SilverfishTick(getServer(), this);
 		lastMsg1 = new HashMap<Player,Integer>();
 		lastMsg2 = new HashMap<Player,Integer>();
 		lastMsg3 = new HashMap<Player,Integer>();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void InventoryOpen(InventoryOpenEvent event) {
 		if(event.getInventory().getType() == InventoryType.CHEST){
			InventoryHolder chest = (Chest)event.getInventory().getHolder();
 			if(!chesttick.chests.containsKey(chest)){
 				chesttick.chests.put(chest, 3600);
 			}
 			Player player = (Player)event.getPlayer();
 			if(!lastMsg1.containsKey(player)){
 				lastMsg1.put(player, 0);
 			}
 			if(lastMsg1.get(player) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
 				player.sendMessage("Don't forget to store flesh, fruits and vegetables in a snow or ice cooled chest.");
 				lastMsg1.put(player, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
 			}
 		}
 	}
 	
 	@EventHandler
 	public void InventoryClose(InventoryCloseEvent event) {
 		if(event.getInventory().getType() == InventoryType.CHEST){
 			Chest chest = (Chest)event.getInventory().getHolder();
 			Location location = chest.getLocation();
 			if(chest.getInventory().contains(Material.ROTTEN_FLESH)){
 				if(!silverfishtick.chests.contains(location)){
 					silverfishtick.chests.add(location);
 				}
 				Player player = (Player)event.getPlayer();
 				if(!lastMsg2.containsKey(player)){
 					lastMsg2.put(player, 0);
 				}
 				if(lastMsg2.get(player) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
 					player.sendMessage("Be carefull. Storing rotten flesh may attract insects.");
 					lastMsg2.put(player, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
 				}
 			}else{
 				if(silverfishtick.chests.contains(location)){
 					silverfishtick.chests.remove(location);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void PlayerPickupItem(PlayerPickupItemEvent event) {
 		if(event.getItem().getItemStack().getType() == Material.ROTTEN_FLESH){
 			Player player = event.getPlayer();
 			if(!lastMsg3.containsKey(player)){
 				lastMsg3.put(player, 0);
 			}
 			if(lastMsg3.get(player) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
 				player.sendMessage("Be carefull. Carrying rotten flesh may attract insects.");
 				lastMsg3.put(player, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
 			}
 			if(silverfishtick.items.contains(event.getItem())){
 				silverfishtick.items.remove(event.getItem());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void BlockBreak(BlockBreakEvent event) {
 		if(event.getBlock().getType() == Material.CHEST){
 			Chest chest = (Chest)event.getBlock().getState();
 			if(chesttick.chests.containsKey(chest)){
 				chesttick.chests.remove(chest);
 			}
 			if(silverfishtick.chests.contains(event.getBlock().getLocation())){
 				silverfishtick.chests.remove(event.getBlock().getLocation());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void ItemSpawn(ItemSpawnEvent event) {
 		if(event.getEntity().getItemStack().getType() == Material.ROTTEN_FLESH){
 			silverfishtick.items.add(event.getEntity());
 		}
 	}
 	
 }
