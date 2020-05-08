 package net.robbytu.banjoserver.bungee.listeners;
 
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 import net.robbytu.banjoserver.bungee.Main;
 import net.robbytu.banjoserver.bungee.auth.AuthChatListener;
 import net.robbytu.banjoserver.bungee.chat.SharedChatHandler;
 import net.robbytu.banjoserver.bungee.consoles.ConsoleGateway;
 import net.robbytu.banjoserver.bungee.directsupport.TicketChatHandler;
 import net.robbytu.banjoserver.bungee.mute.MuteChatListener;
 
 public class ChatListener implements Listener {
     @EventHandler
     public static void handleChat(ChatEvent event) {
         if(!event.isCancelled()) AuthChatListener.handleChat(event);
         if(!event.isCancelled()) TicketChatHandler.handleChat(event);
         if(!event.isCancelled()) MuteChatListener.handleChat(event);
         if(!event.isCancelled() && event.isCommand())
             for(ServerInfo server : Main.instance.getProxy().getServers().values())
                 if(server.getAddress().equals(event.getReceiver().getAddress()))
                     for(ProxiedPlayer player : server.getPlayers())
                         if(player.getAddress().equals(event.getSender().getAddress()))
                             ConsoleGateway.dispatchLog(server.getName(), player.getName() + ": " + ((event.getMessage().startsWith("/login ")) ? "/login ***" : event.getMessage()));
        if(!event.isCancelled()) SharedChatHandler.handleChat(event);
     }
 }
