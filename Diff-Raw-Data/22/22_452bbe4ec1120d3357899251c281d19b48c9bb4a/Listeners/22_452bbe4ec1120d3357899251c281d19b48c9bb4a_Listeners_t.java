 package gmail.theultimatebudgie.liteperms;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class Listeners implements Listener{
 	LitePerms plugin;
 	
 	public Listeners(LitePerms plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		LitePlayer lite = new LitePlayer(p.getName().toLowerCase(), p.addAttachment(plugin));
 		LitePlayer.addPlayer(lite);
 		lite.setGroup(plugin.getYaml().getPlayerGroup(p.getName()));
 		lite.setExtraPermissions(plugin.getYaml().getPlayerExtraPermissions(p.getName()));
 		lite.setChatPrefix(plugin.getYaml().getPlayerChatPrefix(p.getName()));
 		lite.updatePermissions();
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player p = event.getPlayer();
 		LitePlayer lite = LitePlayer.getPlayerByName(p.getName());
 		lite.remove(p);
 	}
 	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onChatEvent(AsyncPlayerChatEvent event) {
 		if (LitePlayer.getPlayerByName(event.getPlayer().getName()).getChatPrefix() != null) {
 			event.setFormat(LitePlayer.getPlayerByName(event.getPlayer().getName()).getChatPrefix() + 
 					event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage());
 		} else {
 			event.setFormat(event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage());
 		}
 	}
 }
