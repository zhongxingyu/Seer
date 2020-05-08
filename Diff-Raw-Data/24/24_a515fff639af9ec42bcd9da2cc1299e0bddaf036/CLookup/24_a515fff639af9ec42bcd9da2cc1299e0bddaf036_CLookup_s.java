 /*
  * Copyright 2012 James Geboski <jgeboski@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jgeboski.vindicator.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import org.jgeboski.vindicator.api.TargetObject;
 import org.jgeboski.vindicator.api.VindicatorAPI;
 import org.jgeboski.vindicator.exception.APIException;
 import org.jgeboski.vindicator.Message;
 import org.jgeboski.vindicator.Vindicator;
 
 public class CLookup implements CommandExecutor
 {
     protected Vindicator vind;
 
     public CLookup(Vindicator vind)
     {
         this.vind = vind;
     }
 
     public boolean onCommand(CommandSender sender, Command command,
                              String label, String[] args)
     {
         TargetObject[] tos;
 
         if(!vind.hasPermissionM(sender, "vindicator.lookup"))
             return true;
 
         if(args.length < 1) {
             Message.info(sender, command.getUsage());
             return true;
         }
 
         try {
             tos = vind.api.lookup(args[0]);
         } catch(APIException e) {
             Message.severe(sender, e.getMessage());
             return true;
         }
 
        Message.info(sender, "The account record of %s:", args[0]);
 
         if(tos.length < 1) {
            Message.info(sender, "  The account of %s has no records");
             return true;
         }
 
         for(TargetObject to : tos) {
             if(to.hasFlag(TargetObject.BAN)) {
                Message.info(sender, "  %s[%s] Ban (by: %s): %s",
                             ChatColor.RED, to.getTimeStr(), to.getIssuer(),
                             to.getMessage());
            } else if(to.hasFlag(TargetObject.NOTE)) {
                Message.info(sender, "  %s[%s] Note #%d (by: %s): %s",
                             ChatColor.YELLOW, to.getId(), to.getTimeStr(),
                              to.getIssuer(), to.getMessage());
             }
         }
 
         return true;
     }
 }
