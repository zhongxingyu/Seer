 package com.drtshock.willie.command;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import com.drtshock.willie.Willie;
 
 public class KickCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
         if(args.length != 0) {
            if(channel.getVoices().contains(sender) || channel.getOps().contains(sender)) {
                 if(channel.getUsers().contains(bot.getUser(args[0]))) {
                     if(args.length == 1) {
                         bot.kick(channel, bot.getUser(args[0]));
                     } else {
                         StringBuilder sb = new StringBuilder();
                         for(String arg:args) {
                             if(arg != args[0]) {
                                 sb.append(arg).append(" ");
                             }
                         }
                         String reason = sb.toString().trim();
                         bot.kick(channel, bot.getUser(args[0]), reason);
                     }
                 } else {
                     bot.sendNotice(sender, "That user is not in the channel!");
                 }
             } else {
                 bot.sendNotice(sender, "You do not have permission to do that!");
             }
         } else {
             bot.sendNotice(sender, "Usage: !kick <user>");
         }
     }
 
 }
