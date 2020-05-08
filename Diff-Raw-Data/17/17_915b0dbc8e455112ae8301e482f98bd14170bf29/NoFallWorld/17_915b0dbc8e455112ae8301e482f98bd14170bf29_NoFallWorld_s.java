 package me.JayzaSapphire;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NoFallWorld extends JavaPlugin implements Listener {
 
     public List<String> worlds;
     private HashMap<String, ItemStack[]> items = new HashMap<String, ItemStack[]>();
     private HashMap<String, ItemStack[]> armor = new HashMap<String, ItemStack[]>();
     private HashSet<String> dead = new HashSet<String>();
     
     @Override
 	public void onEnable() {
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
         worlds = getConfig().getStringList("worlds");
         
         getServer().getPluginManager().registerEvents(this, this);
         
         getCommand("nfw").setExecutor(new NfwExecutor(this));
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player player = (Player) event.getEntity();
 		String world = player.getWorld().getName();
 		
 		if (player.getLastDamageCause().getCause() == DamageCause.VOID && worlds.contains(world.toLowerCase())) {
 			items.put(player.getName(), player.getInventory().getContents());
 			armor.put(player.getName(), player.getInventory().getArmorContents());
			
			event.setDroppedExp(0);
			event.setKeepLevel(true);
 			
 			dead.add(player.getName());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		
 		if (dead.contains(player.getName())) {
 			restore(player);
 		}
 	}
 	
 	private void restore(final Player player) {
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			public void run() {
 				
 				if (items.containsKey(player.getName())) {
 					player.getInventory().setContents(items.get(player.getName()));
 					items.remove(player.getName());
 				}
 				
 				if (armor.containsKey(player.getName())) {
 					player.getInventory().setArmorContents(armor.get(player.getName()));
 					armor.remove(player.getName());
 				}
 				
 				if (dead.contains(player.getName())) {
 					dead.remove(player.getName());
 				}
 			}
 		}, 10);
 	}
 	
 	public void onDisable() {
 		
 	}
 }
