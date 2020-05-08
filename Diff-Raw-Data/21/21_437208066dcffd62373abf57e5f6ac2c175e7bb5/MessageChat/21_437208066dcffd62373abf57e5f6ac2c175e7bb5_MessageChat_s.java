 package com.untamedears.recivchat.mode;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.untamedears.recivchat.CivChat;
 
 public class MessageChat implements ChatMode, Replyable {
 	private String recipient;
 	
	public MessageChat(String recipient) {
 		this.recipient = recipient;
 	}
 
 	@Override
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player sender = event.getPlayer();
 		Player player = Bukkit.getPlayerExact(recipient);
 		
 		if(player != null) {
 			if(CivChat.instance.getPlayerManager().isIgnoring(recipient, player.getName())) {
 				sender.sendMessage(ChatColor.YELLOW + "This player is ignoring you. You have been returned to normal chat.");
 				
 				CivChat.instance.getPlayerManager().removePlayerChatMode(sender.getName());
 				
 				event.setCancelled(true);
 				
 				return;
 			}
 			
 			event.getRecipients().clear();
 			event.getRecipients().add(player);
 			
 			event.setFormat(ChatColor.DARK_PURPLE + "From %s: %s");
 			
 			CivChat.instance.getPlayerManager().updateLastReplyable(recipient, this);
 			
 			sender.sendMessage(ChatColor.DARK_PURPLE + "To " + recipient + ": " + event.getMessage());
 			
 			CivChat.instance.logChatLine("[p->p] " + sender.getName() + " -> " + player.getName() + ": " + event.getMessage());
 		}
 		else {
 			sender.sendMessage(ChatColor.YELLOW + "This player is no longer online. You have been returned to normal chat.");
 			
 			CivChat.instance.getPlayerManager().removePlayerChatMode(sender.getName());
 			
 			event.setCancelled(true);
 		}
 	}
 
 	@Override
 	public void directMessage(CommandSender sender, String message) {
 		Player player = Bukkit.getPlayerExact(recipient);
 		
 		if(player != null) {
 			if(CivChat.instance.getPlayerManager().isIgnoring(recipient, player.getName())) {
 				sender.sendMessage(ChatColor.YELLOW + "This player is ignoring you.");
 				
 				return;
 			}
 			
 			player.sendMessage(ChatColor.DARK_PURPLE + "From " + sender.getName() + ": " + message);
 			
 			CivChat.instance.getPlayerManager().updateLastReplyable(recipient, this);
 			
 			sender.sendMessage(ChatColor.DARK_PURPLE + "To " + recipient + ": " + message);
 			
 			CivChat.instance.logChatLine("[p->p] " + sender.getName() + " -> " + player.getName() + ": " + message);
 		}
 		else {
 			sender.sendMessage(ChatColor.YELLOW + "The specified player is not online.");
 		}
 	}
 
 	@Override
 	public ChatMode getReplyChat() {
		return new MessageChat(recipient);
 	}
 }
