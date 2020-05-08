package com.drtshock.willie.command.admin;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 /**
  *
  * @author drtshock
  */
 public class ShutdownCommandHandler implements CommandHandler {
     
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
         channel.sendMessage("Away with me!");
         bot.shutdown(true);
     }
 }
