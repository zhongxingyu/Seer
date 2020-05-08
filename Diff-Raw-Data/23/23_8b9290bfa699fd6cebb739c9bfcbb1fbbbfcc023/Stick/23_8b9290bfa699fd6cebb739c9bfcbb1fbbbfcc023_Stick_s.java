 package friskstick.cops.stick;
 
 import java.util.Iterator;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import friskstick.cops.plugin.FriskStick;
 import friskstick.cops.plugin.JailPlayer;
 
 /**
  * 
  * The class that is responsible for handling a right-click with a stick (in other words, frisking someone with a stick). 
  *
  */
 
 public class Stick implements Listener {
 	
 	private FriskStick plugin;
 	
 	/**
 	 * The constructor for {@link Stick this} class.
	 * @param plugin The {@link FriskStick} object required for the jailing feature to function.
 	 */
 
 	public Stick(FriskStick plugin) {
 		
 		this.plugin = plugin;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 
 	}
 
 	
 	private int index = 0;
 	private JailPlayer jailed = new JailPlayer(plugin);
 	
 	/**
 	 * The method that detects if a player frisks someone with a stick.
 	 * @param event The event for Bukkit to handle.
 	 */
 	
 	@EventHandler
 	public void friskStickPlayer(PlayerInteractEntityEvent event) {
 
 		if(event.getRightClicked() instanceof Player && event.getPlayer().getItemInHand().getType() == Material.STICK) {
 
 			Player frisked = (Player)event.getRightClicked();
 			Player cop = event.getPlayer();
 
 			if(cop.hasPermission("friskstick.use")) {
 
 				if(!frisked.hasPermission("friskstick.bypass")) {
 
 					PlayerInventory inventory = frisked.getInventory();
 					boolean found = false;
 
 					for(String drug: plugin.getConfig().getStringList("drug-ids")) {
 
 						if(drug.contains(":")) {
 
 							String firsthalf = drug.split(":")[0];
 							String lasthalf = drug.split(":")[1];
 
 							for(int i = 1; i <= plugin.getConfig().getInt("amount-to-search-for"); i++) {
 
 								if(inventory.contains(new ItemStack(Integer.parseInt(firsthalf), i, Short.parseShort(lasthalf)))) {
 									Iterator<Integer> iter = inventory.all(new ItemStack(Integer.parseInt(firsthalf), i, Short.parseShort(lasthalf))).keySet().iterator();
 									ItemStack[] contents = inventory.getContents();
 									while(iter.hasNext()){
 										cop.getInventory().addItem(new ItemStack(contents[iter.next()]));
 									}
 									inventory.removeItem(new ItemStack(Integer.parseInt(firsthalf), 2305, Short.parseShort(lasthalf)));
 									cop.sendMessage(plugin.getConfig().getString("cop-found-msg").replaceAll("&", "").replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()).replaceAll("%player%", frisked.getName()));
 									frisked.sendMessage(plugin.getConfig().getString("player-found-msg").replaceAll("&", "").replaceAll("%cop%", cop.getName()).replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()));
 
 									if(cop.hasPermission("friskstick.jail")) {
 
 										jailed.jail(frisked.getName());
 
 									}
 
 									found = true;
 
 								}
 
 							}
 
 						} else {
 
 							if(inventory.contains(Integer.parseInt(drug))) {
 								
 								int drugid = Integer.parseInt(drug);
 								Iterator<Integer> iter = inventory.all(drugid).keySet().iterator();
 								ItemStack[] contents = inventory.getContents();
 								while(iter.hasNext()){
 									cop.getInventory().addItem(new ItemStack(contents[iter.next()]));
 								}
 								inventory.removeItem(new ItemStack(drugid, 2305));
 								cop.sendMessage(plugin.getConfig().getString("cop-found-msg").replaceAll("&", "").replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()).replaceAll("%player%", frisked.getName()));
 								frisked.sendMessage(plugin.getConfig().getString("player-found-msg").replaceAll("&", "").replaceAll("%cop%", cop.getName()).replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()));
 
 								if(cop.hasPermission("friskstick.jail")) {
 
 									jailed.jail(frisked.getName());
 
 								}
 
 								found = true;
 
 							}
 
 						}
 
 						index++;
 
 					}
 
 					index = 0;
 
 					if(!found) {
 
 						cop.sendMessage(plugin.getConfig().getString("cop-not-found-msg").replaceAll("&", "").replaceAll("%player%", frisked.getName()));
 						frisked.sendMessage(plugin.getConfig().getString("player-not-found-msg").replaceAll("&", "").replaceAll("%cop%", cop.getName()));
 
 						if(cop.getHealth() >= 2) {
 
 							cop.setHealth(cop.getHealth() - 2);
 
 						} else {
 
 							cop.setHealth(0);
 
 						}
 
 					}
 
 				} else {
 
 					cop.sendMessage("That player cannot be frisked!");
 
 				}
 
 			}
 
 		}
 
 	}
 
 }
