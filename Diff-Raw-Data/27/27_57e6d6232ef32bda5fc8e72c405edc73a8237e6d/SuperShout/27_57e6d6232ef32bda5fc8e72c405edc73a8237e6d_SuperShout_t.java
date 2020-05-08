 /*
  * SuperShout: Shout plugin for Bukkit. Copyright (C) 2012 Andrew Stevanus
  * (Hoot215) <hoot893@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.Hoot215.SuperShout;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SuperShout extends JavaPlugin
   {
    private Logger logger;
     private static Chat chat = null;
     
     public void info (String message)
       {
        this.logger.info(message);
       }
     
     public void warning (String message)
       {
        this.logger.warning(message);
       }
     
     public void severe (String message)
       {
        this.logger.severe(message);
       }
     
     private boolean setupChat ()
       {
         RegisteredServiceProvider<Chat> chatProvider =
             getServer().getServicesManager().getRegistration(
                 net.milkbowl.vault.chat.Chat.class);
         if (chatProvider != null)
           {
            SuperShout.chat = chatProvider.getProvider();
           }
         
        return (SuperShout.chat != null);
       }
     
     @Override
     public boolean onCommand (CommandSender sender, Command command,
       String label, String[] args)
       {
         if (command.getName().equalsIgnoreCase("shout"))
           {
             if (sender.hasPermission("supershout.shout"))
               {
                 if (args.length == 0)
                   {
                     sender.sendMessage("§cToo few arguements!");
                     return false;
                   }
                 else
                   {
                     String name =
                         sender instanceof Player ? SuperShout.chat
                             .getPlayerPrefix((Player) sender).replace("&", "§")
                             + " " + ((Player) sender).getName() : sender
                             .getName();
                     String message = "§7:§f";
                     for (String s : args)
                       {
                         message += " " + s.replace("&", "§");
                       }
                     this.getServer()
                         .broadcastMessage("§4[S] " + name + message);
                     return true;
                   }
               }
             else
               {
                 sender.sendMessage(command.getPermissionMessage());
                 return true;
               }
           }
         return false;
       }
     
     @Override
     public void onDisable ()
       {
        this.info("is now disabled!");
       }
     
     @Override
     public void onEnable ()
       {
        this.logger = this.getLogger();
         if (this.setupChat())
           {
             this.info("Vault integration enabled!");
           }
         else
           {
             this.severe("Vault integration not enabled!");
             this.getServer().getPluginManager().disablePlugin(this);
            return;
           }
        this.info("is now enabled!");
       }
   }
