 package com.minecraftdimensions.bungeesuite.listeners;
 
 
 import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
 import com.minecraftdimensions.bungeesuite.managers.ChatManager;
 import com.minecraftdimensions.bungeesuite.managers.IgnoresManager;
 import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
 import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuite.objects.Messages;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.event.LoginEvent;
 import net.md_5.bungee.api.event.PlayerDisconnectEvent;
 import net.md_5.bungee.api.event.ServerConnectedEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 
 import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
 
 public class ChatListener implements Listener {
	public static List<String> BlockedCommands = Arrays.asList("/l","/lc","/localchannel","/lchannel","/channellocal",
		"/s","/sc","/serverchannel","/schannel","/channelserver",
		"/g","/globalchat","/globalchannel","/gchannel");
	
     @EventHandler
     public void playerLogin( ServerConnectedEvent e ) throws SQLException {
         ChatManager.loadPlayersChannels( e.getPlayer(), e.getServer() );
         ChatManager.sendPlayer( e.getPlayer().getName(), e.getServer(), true );
         IgnoresManager.sendPlayersIgnores( PlayerManager.getPlayer( e.getPlayer() ), e.getServer() );
         BSPlayer p = PlayerManager.getPlayer( e.getPlayer() );
         if ( p != null ) {
             p.updateDisplayName();
         }
 
     }
 
     @EventHandler
     public void playerLogin( LoginEvent e ) throws SQLException {
         if ( !e.isCancelled() ) {
             if ( ChatConfig.broadcastProxyConnectionMessages ) {
                 PlayerManager.sendBroadcast( Messages.PLAYER_CONNECT_PROXY.replace( "{player}", e.getConnection().getName() ) );
             }
         }
     }
 
     @EventHandler
     public void playerChat( ChatEvent e ) throws SQLException {
        BSPlayer p = PlayerManager.getPlayer( e.getSender().toString() );
        if ( e.isCommand()) {
        	if(BlockedCommands.contains(e.getMessage().split(" ")[0].toLowerCase())){
                if ( ChatManager.MuteAll ) {
                    p.sendMessage( Messages.MUTED );
                    e.setCancelled( true );
                }
                if ( p.isMuted() ) {
                    p.sendMessage( Messages.MUTED );
                    e.setCancelled( true );
                    System.out.println("muted");
                }	
        	}
             return;
         }
         if ( ChatManager.MuteAll ) {
             p.sendMessage( Messages.MUTED );
             e.setCancelled( true );
         }
         if ( p.isMuted() ) {
             p.sendMessage( Messages.MUTED );
             e.setCancelled( true );
         }
     }
 
     @EventHandler
     public void playerLogout( PlayerDisconnectEvent e ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( e.getPlayer() );
         if ( ChatConfig.broadcastProxyConnectionMessages ) {
             PlayerManager.sendBroadcast( Messages.PLAYER_DISCONNECT_PROXY.replace( "{player}", p.getDisplayingName() ) );
         }
     }
 
 }
