 /*
  * Fireworks display on levelling up.
  * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.Hoot215.updater;
 
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class AutoUpdaterQueue implements Runnable
   {
     private LinkedBlockingQueue<CommandSender> queue =
         new LinkedBlockingQueue<CommandSender>();
     private AutoUpdater autoUpdater;
     private final String pluginName;
     
     public AutoUpdaterQueue(AutoUpdater autoUpdater, String pluginName)
       {
         this.autoUpdater = autoUpdater;
         this.pluginName = pluginName;
       }
     
     public void start ()
       {
         new Thread(this).start();
       }
     
     public void add (CommandSender sender)
       {
         queue.add(sender);
       }
     
     @Override
     public void run ()
       {
         while (true)
           {
             try
               {
                 CommandSender sender = queue.take();
                 if (autoUpdater.updateCheck())
                   {
                     sender.sendMessage(ChatColor.GREEN + "A newer version of "
                         + ChatColor.RED + pluginName + ChatColor.GREEN
                         + " is available!" + ChatColor.GRAY + " (v"
                         + ChatColor.DARK_GREEN + autoUpdater.getNewestVersion()
                         + ChatColor.GRAY + ")");
                     String site = autoUpdater.getSite();
                     if (!site.isEmpty())
                       {
                         sender.sendMessage(ChatColor.GREEN
                             + "Download it here: " + ChatColor.BLUE + site);
                       }
                   }
                 else
                   {
                     sender.sendMessage(ChatColor.GRAY + "No newer version of "
                         + ChatColor.RED + pluginName + ChatColor.GRAY
                         + " was found");
                   }
               }
             catch (NullPointerException e)
               {
               }
             catch (InterruptedException e)
               {
                 e.printStackTrace();
               }
           }
       }
   }
