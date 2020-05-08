 package net.smudgecraft.smudgeessentials.adminchat;
 
import java.util.Set;

 import net.smudgecraft.smudgeessentials.SmudgeEssentials;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatListenerAdmin implements Listener
 {
 
 	  @EventHandler(priority=EventPriority.HIGHEST)
 	  public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
 	    if (event.isCancelled())
 	      return;
 	    Player player = event.getPlayer();
 	    if (!AcCommand.playersInChat.containsKey(player))
 	    {
 	      return;
 	    }
 
	    String msg = event.getMessage();
	    Set<Player> things = event.getRecipients();
 	    for (Player p : SmudgeEssentials.plugin.getServer().getOnlinePlayers())
 	    {
	      if (!p.hasPermission("adminchat.see"))
 	      {
	        things.remove(p);
	      }
	      else if (!things.contains(p)) {
	        things.add(p);
 	      }

	      event.setFormat("%2$s");
	      event.setMessage("<" + ChatColor.RED + "STAFFCHAT " + ChatColor.WHITE + player.getName() + "> " + msg);
 	    }
 	  }
 }
