 package net.LoadingChunks.SyncingFeeling.Tasks;
 
 import net.LoadingChunks.SyncingFeeling.SyncingFeeling;
 import net.LoadingChunks.SyncingFeeling.Inventory.SerializableInventory;
 
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class RecoverTask extends BukkitRunnable {
 	
 	private Player p;
 	private SyncingFeeling plugin;
 	
 	public RecoverTask(SyncingFeeling plugin, Player p) {
 		this.p = p;
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void run() {
 		if(p.hasPermission("sync.do")) {
			plugin.getLogger().info("Checking " + p.getDisplayName() + "'s inventory...");
 			SerializableInventory.recover(p.getPlayer());
 		}
 	}
 
 }
