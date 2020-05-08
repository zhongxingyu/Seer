 package com.hotmail.shinyclef.shinyutilities;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Author: Shinyclef
  * Date: 25/06/12
  * Time: 12:34 AM
  */
 
 public class CmdExecutor implements CommandExecutor
 {
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         switch (command.getName().toLowerCase())
         {
             case "msg":
                 return PrivateMessage.processPrivateMessage(sender, args);
 
             case "r":
                 return PrivateMessage.reply(sender, args);
 
             case "mm":
                 return PrivateMessage.messageLast(sender, args);
 
             case "spy":
                 return PrivateMessage.toggleSpy(sender, args);
 
             case "mute":
                 return Mute.mute(sender, args);
 
             case "unmute":
                 return Mute.unmute(sender, args);
 
             case "mutelist":
                 return Mute.mutelist(sender, args);
 
             case "busy":
                return Busy.busy(sender, args);
 
             case "bookimport":
                 return BookImport.importBook(sender, args);
 
             default:
                 return false;
         }
     }
  }
 
