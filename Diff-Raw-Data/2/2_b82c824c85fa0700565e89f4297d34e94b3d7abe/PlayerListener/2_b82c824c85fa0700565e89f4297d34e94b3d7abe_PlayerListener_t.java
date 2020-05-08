 package net.illusiononline.EmeraldEconomy;
 
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener{
 	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public PlayerListener(EmeraldEconomy plugin){
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		if (player == null) return;
 		Integer money = EmeraldEconomy.getSQLManager().getBalance(player.getName());
 		if (money == null) {
			Boolean _w = EmeraldEconomy.getSQLManager().newUnit(player.getName());
 			if (_w)
 				log.info("Creating new Economy Unit for Player: "+player.getName());
 			else {
 				log.info("Failed to create Economy Unit for Player: "+player.getName());
 				player.sendMessage(ChatColor.RED+"Economy Unit Creation Failed: Please Relogg!");
 			}
 				
 		} else
 			player.sendMessage(ChatColor.AQUA+"<------------>\n"+player.getName()+"'s Balance: "+(money)+"\n<------------>");
 	}
 }
