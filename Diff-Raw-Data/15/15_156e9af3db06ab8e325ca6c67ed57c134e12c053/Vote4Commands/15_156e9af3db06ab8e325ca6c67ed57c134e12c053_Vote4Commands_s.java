 package net.craftminecraft.bungee.vote4commands;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.util.concurrent.AtomicLongMap;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.api.plugin.Plugin;
 import net.md_5.bungee.event.EventHandler;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 
 import com.vexsoftware.votifier.model.VotifierEvent;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.event.ServerConnectedEvent;
 import net.md_5.bungee.api.plugin.Command;
 
 public class Vote4Commands extends Plugin implements Listener {
     private AtomicLongMap<String> voters = AtomicLongMap.create();
     private MainConfig config;
     private Set<String> voted = Sets.newHashSet();
 
     @Override
     public void onEnable() {
         this.getProxy().getPluginManager().registerListener(this, this);
         this.config = new MainConfig(this);
         try {
             this.config.init();
         } catch (InvalidConfigurationException ex) {
             this.getLogger().log(Level.SEVERE, "Invalid configuration!", ex);
         }
 
         // Confirmvote command
         this.getProxy().getPluginManager().registerCommand(this, new Command("confirmvote") {
             @Override
             public void execute(CommandSender sender, String[] args) {
                 if (!(sender instanceof ProxiedPlayer)) {
                     sender.sendMessage("You need to be online to use this command!");
                     return;
                 }
                 
                 ProxiedPlayer p = (ProxiedPlayer) sender;
                 String username = p.getName().toLowerCase();
 
                 if (voters.get(username) > 0) {
                     Vote4Commands.this.giveReward(p);
                     voters.decrementAndGet(username);
                 }
             }
         });
 
         if (config.votenag_enabled) {
             this.getProxy().getScheduler().schedule(this, new Runnable() {
 
                 public void run() {
                     for (ProxiedPlayer p : Vote4Commands.this.getProxy().getPlayers()) {
                         if (!Vote4Commands.this.voted.contains(p.getName().toLowerCase())) {
                             p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                                     config.votenag_message));
                         }
                     }
                 }
            }, config.votenag_interval, TimeUnit.MINUTES);
         }
     }
 
     @EventHandler
     public void onVote(VotifierEvent ev) {
         String username = ev.getVote().getUsername().toLowerCase();
         voters.incrementAndGet(username);
         voted.add(username);
         for (ProxiedPlayer p : this.getProxy().getPlayers()) {
             if (p.getName().equalsIgnoreCase(username)) {
                 p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                         config.confirmvotemessage));
                break;
             }
         }
     }
 
     @EventHandler
     public void onServerChange(ServerConnectedEvent ev) {
         if (voters.get(ev.getPlayer().getName().toLowerCase()) > 0) {
             ev.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                     config.confirmvotemessage));
         }
     }
 
     @EventHandler
     public void onChat(ChatEvent ev) {
         if (!(ev.getSender() instanceof ProxiedPlayer)) {
             return;
         }
         if (!ev.isCommand()) {
             return;
         }
         ProxiedPlayer sender = (ProxiedPlayer) ev.getSender();
         if (sender.hasPermission("vote4item.bypass")) {
             return;
         }
 
         String[] words = ev.getMessage().split(" ");
         if (sender.hasPermission("vote4item.bypass." + words[0])) {
             return;
         }
 
         for (String command : config.commands) {
             String[] commandwords = command.split(" ");
             if (words[0].equalsIgnoreCase(commandwords[0])) {
                 ev.setCancelled(true);
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                         config.blockedmessage));
             }
         }
     }
     
     public void giveReward(ProxiedPlayer p) {
         for (String command : config.commands) {
             p.chat(command);
         }
     }
 }
