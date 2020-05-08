 package dev.mCraft.Coinz.Listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dev.mCraft.Coinz.Coinz;
 
 public class InventoryListener implements Listener {
 	
 	private Coinz plugin = Coinz.instance;
 	
 	private SpoutPlayer player;
 	private short recipe;
 	
 	public InventoryListener() {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onMyRecipeCraft(InventoryCraftEvent event) {
 		player = (SpoutPlayer)event.getPlayer();
 		recipe = event.getResult().getDurability();
 		
		if (!plugin.perm.playerHas(player, "coinz.craft.teller") && recipe == plugin.tellerRec.getResult().getDurability()) {
 			player.sendNotification("So sorry!", "Cant craft that", plugin.TellerBlock, 2500);
 			event.setCancelled(true);
 		}
 		if (!plugin.perm.playerHas(player, "coinz.craft.vault") && recipe == plugin.vaultRec.getResult().getDurability()) {
 			player.sendNotification("So sorry!", "Cant craft that", plugin.VaultBlock, 2500);
 			event.setCancelled(true);
 		}
 		if (!plugin.perm.playerHas(player, "coinz.craft.goldcoin") && recipe == plugin.MakeGC2.getResult().getDurability()) {
 			player.sendNotification("So sorry!", "Cant craft that", plugin.GoldCoin, 2500);
 			event.setCancelled(true);
 		}
 	}
 
 }
