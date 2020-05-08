 package net.robbytu.banjoserver.bungee.chat;/* vim: set expandtab tabstop=4 shiftwidth=4 softtabstop=4: */
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.robbytu.banjoserver.bungee.Main;
 import net.robbytu.banjoserver.bungee.perms.Permissions;
 
 public class SharedChatHandler {
     public static void handleChat(ChatEvent event) {
         ProxiedPlayer sender = null;
 
         for(ServerInfo server : Main.instance.getProxy().getServers().values())
             if(server.getAddress().equals(event.getReceiver().getAddress()))
                 for(ProxiedPlayer player : server.getPlayers())
                     if(player.getAddress().equals(event.getSender().getAddress()))
                         sender = player;
 
         if(sender == null) return;
 
         String userPrefix = Permissions.getPrefixForUser(sender.getDisplayName());
        String userMessage = event.getMessage();
 
         for(ServerInfo server : Main.instance.getProxy().getServers().values())
             for(ProxiedPlayer player : server.getPlayers()) {
                String chatMessage = ChatColor.DARK_GRAY + "[" + sender.getServer().getInfo().getName() + "] ";
 
                 if(server.getName().equals(sender.getServer().getInfo().getName()))
                    chatMessage += ChatColor.WHITE + "<" + userPrefix + " " + sender.getName() + ChatColor.WHITE + "> " + userMessage;
                 else
                    chatMessage += ChatColor.GRAY + ChatColor.stripColor("<" + userPrefix + " " + sender.getName() + "> " + userMessage);
 
                 player.sendMessage(chatMessage);
             }
     }
 }
