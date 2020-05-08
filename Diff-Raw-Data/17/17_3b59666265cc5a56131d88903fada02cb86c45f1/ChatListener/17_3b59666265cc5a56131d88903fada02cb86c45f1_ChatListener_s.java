 package com.minecraftdimensions.bungeesuitechat.listeners;
 
 import com.minecraftdimensions.bungeesuitechat.BungeeSuiteChat;
 import com.minecraftdimensions.bungeesuitechat.Utilities;
 import com.minecraftdimensions.bungeesuitechat.managers.ChannelManager;
 import com.minecraftdimensions.bungeesuitechat.managers.PlayerManager;
 import com.minecraftdimensions.bungeesuitechat.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuitechat.objects.ServerData;
 import com.palmergames.bukkit.TownyChat.channels.Channel;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatListener implements Listener {
 
     @EventHandler( priority = EventPriority.LOWEST )
     public void setFormatChat( AsyncPlayerChatEvent e ) {
         if ( e.isCancelled() ) {
             return;
         }
         BSPlayer p = PlayerManager.getPlayer( e.getPlayer() );
         if ( p == null ) {
             Bukkit.getConsoleSender().sendMessage( ChatColor.DARK_RED + "Player did not connect properly through BungeeCord, Chat canceled!" );
             e.setCancelled( true );
             return;
         }
         if ( !ChannelManager.playerHasPermissionToTalk( p ) ) {
             e.setCancelled( true );
             e.getPlayer().sendMessage( ChatColor.RED + "You do not have permission to talk in this channel" );
             return;
         }
         e.setFormat( p.getChannelFormat() );
         if ( ChannelManager.isLocal( p.getChannel() ) ) {
             e.getRecipients().removeAll( ChannelManager.getNonLocal( e.getPlayer() ) );
             e.getRecipients().removeAll( ChannelManager.getIgnores( e.getPlayer() ) );
         } else if ( ChannelManager.isServer( p.getChannel() ) ) {
             e.getRecipients().clear();
             e.getRecipients().addAll( ChannelManager.getServerPlayers() );
             e.getRecipients().removeAll( ChannelManager.getIgnores( e.getPlayer() ) );
         } else if ( ChannelManager.isGlobal( p.getChannel() ) ) {
             e.getRecipients().clear();
             e.getRecipients().addAll( ChannelManager.getGlobalPlayers() );
             e.getRecipients().removeAll( ChannelManager.getIgnores( e.getPlayer() ) );
         } else if ( ChannelManager.isAdmin( p.getChannel() ) ) {
             e.getRecipients().clear();
             e.getRecipients().addAll( ChannelManager.getAdminPlayers() );
         } else if ( ServerData.usingFactions() && ChannelManager.isFactionChannel( p.getChannel() ) ) {
             if ( ChannelManager.isFaction( p.getChannel() ) ) {
                 e.getRecipients().clear();
                 e.getRecipients().addAll( ChannelManager.getFactionPlayers( e.getPlayer() ) );
             } else if ( ChannelManager.isFactionAlly( p.getChannel() ) ) {
                 e.getRecipients().clear();
                 e.getRecipients().addAll( ChannelManager.getFactionAllyPlayers( e.getPlayer() ) );
             }
         }
     }
 
     @EventHandler( priority = EventPriority.HIGH )
     public void setVariables( AsyncPlayerChatEvent e ) {
         if ( e.isCancelled() ) {
             return;
         }
         e.setFormat( Utilities.ReplaceVariables( e.getPlayer(), e.getFormat() ) );
         e.setMessage( Utilities.SetMessage( e.getPlayer(), e.getMessage() ) );
     }
 
     @EventHandler( priority = EventPriority.MONITOR )
     public void setLogChat( AsyncPlayerChatEvent e ) {
         if ( e.isCancelled() ) {
             return;
         }
         BSPlayer p = PlayerManager.getPlayer( e.getPlayer() );
         if ( p == null ) {
             e.setCancelled( true );
             return;
         }
         if(BungeeSuiteChat.townyChat){
         	for (Channel channel : BungeeSuiteChat.tc.getChannelsHandler().getAllChannels().values()) {
 				if (BungeeSuiteChat.tc.getTowny().hasPlayerMode(e.getPlayer(), channel.getName())) {
	            	 e.getRecipients().addAll( PlayerManager.getChatSpies(e.getPlayer()) );
 	                 Utilities.logChat( String.format( e.getFormat(), p.getDisplayingName(), e.getMessage() ) );
 	                 return;
 				}
         	}
         }
         if ( ChannelManager.isGlobal( p.getChannel() ) ) {
             ChannelManager.sendGlobalChat( e.getPlayer().getName(), String.format( e.getFormat(), p.getDisplayingName(), e.getMessage() ) );
         } else if ( ChannelManager.isAdmin( p.getChannel() ) ) {
             ChannelManager.sendAdminChat( String.format( e.getFormat(), p.getDisplayingName(), e.getMessage() ) );
         } else {
            e.getRecipients().addAll( PlayerManager.getChatSpies(e.getPlayer()) );
             Utilities.logChat( String.format( e.getFormat(), p.getDisplayingName(), e.getMessage() ) );
         } 
 
     }
 
 
 }
