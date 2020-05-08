 package net.dmulloy2.suffixesplus.listeners;
 
 import net.dmulloy2.suffixesplus.SuffixesPlus;
 import net.dmulloy2.suffixesplus.util.FormatUtil;
 import net.dmulloy2.suffixesplus.util.Util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  * @author dmulloy2
  */
 
 public class ChatListener implements Listener
 {
 	private final SuffixesPlus plugin;
 	public ChatListener(SuffixesPlus plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerChat(AsyncPlayerChatEvent event)
 	{
		Player p1 = event.getPlayer();
		if (! event.getMessage().contains(ChatColor.DARK_GREEN + "[" + ChatColor.AQUA + "LISTENING" + ChatColor.DARK_GREEN + "]"))
 		{
 			// Only send to players not listening
			for (String name : plugin.getListenedToBy().get(p1.getName()))
 			{
 				Player p2 = Util.matchPlayer(name);
 				event.getRecipients().remove(p2);
 			}
 
			if (! plugin.getListenedToBy().get(p1.getName()).isEmpty())
 			{
				p1.chat(FormatUtil.format("&6&l[&bLISTENING&6&l] {0}&r", event.getMessage()));
 			}
 		}
 		else
 		{
 			event.getRecipients().clear();
			for (String name : plugin.getListenedToBy().get(p1.getName()))
 			{
 				Player p2 = Util.matchPlayer(name);
 				event.getRecipients().add(p2);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		plugin.createListenedToBy(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		onPlayerDisconnect(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerKick(PlayerKickEvent event)
 	{
		if (! event.isCancelled())
 		{
 			onPlayerDisconnect(event.getPlayer());
 		}
 	}
 
 	public void onPlayerDisconnect(Player player)
 	{
 		plugin.removeListenedToBy(player);
 	}
 }
