 package net.robbytu.banjoserver.bungee.auth;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.LoginEvent;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.util.concurrent.TimeUnit;
 
 public class LoginAlert {
     public static void handle(final LoginEvent event) {
         Main.instance.getProxy().getScheduler().schedule(Main.instance, new Runnable() {
             @Override
             public void run() {
                 final ProxiedPlayer target = Main.instance.getProxy().getPlayer(event.getConnection().getName());
                 boolean registered = AuthProvider.isRegistered(target.getName());
 
                 target.sendMessage(ChatColor.GREEN + "Welkom " + ((registered) ? "terug " : "") + "in de Banjoserver, " + target.getName() + "!");
                 target.sendMessage(ChatColor.GRAY + "Gebruik " + ((registered) ? ChatColor.WHITE + "/login [wachtwoord]" + ChatColor.GRAY + " om in te loggen." : ChatColor.WHITE + "/register [wachtwoord]" + ChatColor.GRAY + " om te registreren."));
                 target.sendMessage(" ");
 
                 Main.instance.getProxy().getScheduler().schedule(Main.instance, new Runnable() {
                     @Override
                     public void run() {
                         if(!AuthProvider.isAuthenticated(target)) {
                             target.disconnect("Om overbelasting van onze servers te voorkomen moet je binnen 30 seconden inloggen.");
                         }
                     }
                 }, 29, TimeUnit.SECONDS);
             }
         }, 1, TimeUnit.SECONDS);
     }
 }
