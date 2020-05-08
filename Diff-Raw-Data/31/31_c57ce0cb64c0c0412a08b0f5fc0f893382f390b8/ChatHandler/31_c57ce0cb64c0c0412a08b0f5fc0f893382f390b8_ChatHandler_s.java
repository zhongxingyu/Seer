 package net.milkycraft;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatHandler implements Listener {
 
	ColoredGroups cg;
 
 	public ChatHandler(ColoredGroups cg) {
 		this.cg = cg;
 	}
 
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onChat(AsyncPlayerChatEvent e) {
 		final String group = cg.getGroup(e.getPlayer());
		for (final ChatProfile c : cg.getChatProfiles()) {
			if (c.getGroup().equalsIgnoreCase(group)) {
 				e.setFormat(c.getFormat());
				break;
 			}
 		}
 		if (e.getFormat().equals("<%1$s> %2$s")) {
 			e.setFormat("[" + group + "]" + "%1$s: %2$s");
 		}
 	}
 
 }
