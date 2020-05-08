 package com.araeosia.Chat;
 
 import com.araeosia.Chat.AraeosiaChat.MsgType;
 import com.araeosia.Chat.utils.Channel;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class ChatListener implements Listener, CommandExecutor {
 
 	private AraeosiaChat plugin;
 
 	public ChatListener(AraeosiaChat plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(final PlayerJoinEvent event) {
 		plugin.database.handlePlayerJoin(event.getPlayer().getName());
 		plugin.bot.sendMessage(plugin.formatMessage(MsgType.JOIN, event.getPlayer(), plugin.serverName, plugin.getChatter(event.getPlayer().getName()).getCurrentChannel()));
 	}
 
 	@EventHandler
 	public void onPlayerLeave(final PlayerQuitEvent event) {
 		if(plugin.chatters.contains(plugin.getChatter(event.getPlayer().getName()))){
 			plugin.chatters.remove(plugin.getChatter(event.getPlayer().getName()));
 		}
		plugin.bot.sendMessage(plugin.formatMessage(MsgType.LEAVE, event.getPlayer(), plugin.serverName, plugin.getChatter(event.getPlayer().getName()).getCurrentChannel()));
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("ch")) {
 			sender.sendMessage(plugin.getChatter(sender.getName()).toString());
 			return true;
 		}
 		return false;
 	}
 
 	@EventHandler
 	// Todo A better way to do this, or more use for this
 	public void onPlayerCommandPreProccesEvent(final PlayerCommandPreprocessEvent e) {
 		if (e.getMessage().startsWith("/me ")) {
 			Player player = e.getPlayer();
 			String output = "* " + player.getDisplayName() + e.getMessage();
 
 			e.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerChatEvent(final PlayerChatEvent e) {
 		Player player = e.getPlayer();
 		Channel c = plugin.getChatter(player.getName()).getCurrentChannel();
 		plugin.bot.sendMessage(plugin.formatMessage(MsgType.MESSAGE, player, e.getMessage(), c));
 		plugin.handleLocalMessage(player, e.getMessage(), true, c);
 		e.setCancelled(true);
 	}
 }
