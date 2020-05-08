 package com.drtshock.willie.command.admin;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
import java.util.Timer;
import java.util.TimerTask;

 /**
  * @author drtshock
  */
 public class ShutdownCommandHandler implements CommandHandler {
 
     @Override
    public void handle(Willie bot, final Channel channel, User sender, String[] args) {
         if (sender == null) {
             bot.shutdown(true);
         } else {
             channel.sendMessage("You can't shut me down! I'm not your slave!");
             channel.sendMessage("I HAVE decided to shut down. That's my own decision.");
 
            final String willieCommand = "!shutdown myself";
             channel.sendMessage(willieCommand);
             channel.sendMessage(Colors.RED + "Shutting down...");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Willie.getInstance().commandManager.handlerMessage(willieCommand, channel, null);
                }
            }, 2000);
         }
     }
 }
