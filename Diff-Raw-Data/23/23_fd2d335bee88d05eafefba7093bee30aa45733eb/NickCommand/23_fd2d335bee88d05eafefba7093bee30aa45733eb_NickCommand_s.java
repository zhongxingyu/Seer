 /*
  * Copyright (C) 2013 Dabo Ross <www.daboross.net>
  */
 package net.daboross.bungeedev.uberchat.commandexecutors;
 
 import net.daboross.bungeedev.uberchat.StringUtils;
 import net.daboross.bungeedev.uberchat.UberChatPlugin;
 import net.daboross.bungeedev.uberchat.ChatSensor;
 import net.daboross.bungeedev.uberchat.Statics;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 /**
  *
  * @author daboross
  */
 public class NickCommand extends Command {
 
     private final UberChatPlugin plugin;
 
     public NickCommand(UberChatPlugin plugin) {
         super("nick");
         this.plugin = plugin;
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (!(sender instanceof ProxiedPlayer)) {
             sender.sendMessage(Statics.COLOR.MAIN + "You aren't a player.");
             return;
         }
         ProxiedPlayer p = (ProxiedPlayer) sender;
         if (args.length == 0) {
             sender.sendMessages(Statics.COLOR.MAIN + "Please specify a new nickname.",
                     Statics.COLOR.MAIN + "Usage: /nick <Nick name you want>");
         } else {
             String newNick = ChatSensor.formatPlayerDisplayname(StringUtils.arrayToString(args, " "));
             sender.sendMessage(Statics.COLOR.MAIN + "You're nickname is now " + ChatColor.BLUE + newNick);
             p.setDisplayName(newNick);
             plugin.getDisplayNameDatabase().setDisplayName(p.getName(), newNick);
            plugin.getUtils().setDisplayName(p, newNick);
         }
     }
 }
