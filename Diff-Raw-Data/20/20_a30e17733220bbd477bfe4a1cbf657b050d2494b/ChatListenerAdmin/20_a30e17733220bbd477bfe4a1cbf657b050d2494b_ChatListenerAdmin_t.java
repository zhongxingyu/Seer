 package net.smudgecraft.smudgeessentials.adminchat;
 
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
 
 	    for (Player p : SmudgeEssentials.plugin.getServer().getOnlinePlayers())
 	    {
	      if (p.hasPermission("adminchat.see"))
 	      {
	    	  p.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "[" + ChatColor.RESET + ChatColor.DARK_GRAY + "StaffChat" + ChatColor.DARK_RED + ChatColor.BOLD + "] " + ChatColor.RED + player.getName() + ChatColor.WHITE + " : " + event.getMessage());
 	      }
	      
	      event.setCancelled(true);
 	    }
 	  }
 }
