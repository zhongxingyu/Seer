 package darkknightcz.InviteEm.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import darkknightcz.InviteEm.InviteEm;
 import darkknightcz.InviteEm.MySQL;
 
 public class InviteEmBanListener implements Listener {
 	@SuppressWarnings("unused")
 	private InviteEm plugin;
 	private MySQL db;
 
 	public InviteEmBanListener(InviteEm plugin) {
 		this.plugin = plugin;
 		this.db = plugin.getDb();
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void gotBan(PlayerCommandPreprocessEvent event) {
 		if (event.getMessage().contains("/ban")) {
 			String[] args = event.getMessage().split(" ");
 			if (args.length > 1) {
 				if (args[0].toLowerCase().equals("/ban")) {
 					String ref = db.warn(args[1].toLowerCase());
 					if (ref != null) {
 						if(Bukkit.getPlayer(ref).isOnline()){
							punish(ref);
 						}
 					}
 				} else {
 					return;
 				}
 			}
 		}
 	}
 
	private void punish(String ref) {
		/* TODO: PUNISH SYSTEM */		
 	}
 	
 	
 }
