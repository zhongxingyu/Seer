 package team.bukkitserverforbukkitpeople.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import team.bukkitserverforbukkitpeople.Main;
 
 public class PlayerListener implements Listener {
 
 	Main plugin;
 	
 	public PlayerListener(Main main) {
 	    this.plugin = main;
 	}
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event) {
             event.getPlayer().sendMessage(plugin.getConfig().getString("messages.join", "Welcome <player>!").replaceAll("&", "�").replaceAll("<player>", event.getPlayer().getName()));
 	}
 	
 	@EventHandler
 	public void onChat(AsyncPlayerChatEvent event) {
             //Make messages colorable
            event.setMessage(ChatColor.translateAlternateColorCodes("&".charAt(0), event.getMessage()));
 	} 
 	
 	@EventHandler
 	public void onHit(EntityDamageByEntityEvent event) {
 		Entity entity = event.getEntity();
 		Entity damagerEnt = event.getDamager();
 		if(entity instanceof Player && damagerEnt instanceof Player) {
 			Player p  = (Player) entity;
 			Player damager = (Player) damagerEnt;
 			if(plugin.pc.getPlayers().getBoolean(damager.getName()+".pvp-opt", false)) {
 				damager.sendMessage(ChatColor.RED+"You are opted out of pvp. Use /pvpopt <in/out> to change this status.");
 				event.setCancelled(true);
 				return;
 			}
 			if(plugin.pc.getPlayers().getBoolean(p.getName()+".pvp-opt", false)) {
 				damager.sendMessage(ChatColor.RED+"That player has pvp disabled!");
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 	
 }
