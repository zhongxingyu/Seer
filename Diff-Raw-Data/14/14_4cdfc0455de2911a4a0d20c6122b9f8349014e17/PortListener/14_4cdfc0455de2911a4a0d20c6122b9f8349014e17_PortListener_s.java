 package net.catharos.port.listener;
 
 import net.catharos.port.PortPlugin;
 import net.catharos.port.PortSign;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class PortListener implements Listener {
 	public PortListener(JavaPlugin plugin) {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void interact(PlayerInteractEvent event) {
 		if(event.isCancelled()) return;
 		
 		Block block = event.getClickedBlock();
 		if(block.getType() != Material.ENCHANTMENT_TABLE && block.getData() != 1) return;
 		
 		// Cancel the event
 		event.setCancelled(true);
 		
 		Location loc = block.getLocation();
 		Player player = event.getPlayer();
 		
 		// Get sign
 		PortSign sign = PortPlugin.getInstance().getOrCreatePortSignAt(loc.subtract(0, 2, 0));
 		if(sign == null) return;
 		
 		// Off we go!
 		player.teleport(sign.getTarget());
 		
 		// TODO add denizen script activation
 	}
 	
 	@EventHandler
 	public void sign(SignChangeEvent event) {
 		if(event.isCancelled()) return;
 		
 		if(event.getLine(0).equalsIgnoreCase("[cPort]")) {
 			if(!event.getPlayer().hasPermission("cport.create")) {
 				event.getPlayer().sendMessage(ChatColor.DARK_RED + "You don't have permission to create such a sign!");
 				return;
 			}
 			
 			Block table = event.getBlock().getLocation().getWorld().getBlockAt(event.getBlock().getLocation().add(0, 2, 0));
 			if(!(table.getType() != Material.ENCHANTMENT_TABLE)) {
 				event.getPlayer().sendMessage(ChatColor.DARK_RED + "Missing enchantment table (Place it 2 blocks above)!");
 				return;
 			}
 			
 			PortSign sign = PortPlugin.getInstance().getOrCreatePortSignAt(event.getBlock().getLocation());
 			
 			if(sign != null) {
 				event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Success! Linked to: " + sign.getTarget());
 			} else {
 				event.getPlayer().sendMessage(ChatColor.DARK_RED + "Error creating sign. Please check console!");
 			}
 		}
 	}
 }
