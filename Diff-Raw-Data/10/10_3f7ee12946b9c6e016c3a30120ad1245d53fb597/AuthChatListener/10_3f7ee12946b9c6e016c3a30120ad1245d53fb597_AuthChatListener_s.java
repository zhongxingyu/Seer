 package net.robbytu.banjoserver.bungee.auth;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.robbytu.banjoserver.bungee.Main;
 
 public class AuthChatListener {
     public static void handleChat(ChatEvent event) {
         ProxiedPlayer sender = null;
         for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
             if(player.getAddress().equals(event.getSender().getAddress())) {
                 sender = player;
             }
         }
 
         if(AuthProvider.isAuthenticated(sender)) return;
         if(!event.isCommand()) event.setCancelled(true);
        if(!event.getMessage().substring(0, 7).equalsIgnoreCase("/login ") && !event.getMessage().substring(0, 10).equalsIgnoreCase("/register ")) event.setCancelled(true);
 
         if(event.isCancelled()) sender.sendMessage(ChatColor.RED + "" + ((AuthProvider.isRegistered(sender.getName()))
                ? "Je bent niet ingelogd." + ChatColor.GRAY + "Log in met /login [wachtwoord]."
                : "Je bent niet geregistreerd." + ChatColor.GRAY + "Registreer met /register [wachtwoord]."));
     }
 }
