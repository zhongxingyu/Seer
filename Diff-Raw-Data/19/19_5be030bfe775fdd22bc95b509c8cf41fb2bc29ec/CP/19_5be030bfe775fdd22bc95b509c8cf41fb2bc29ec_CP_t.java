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
 
 package org.mcmmoirc.command;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.gmail.nossr50.datatypes.PlayerProfile;
 import com.gmail.nossr50.util.Permissions;
 import com.gmail.nossr50.util.Users;
 
 import org.mcmmoirc.mcMMOIRC;
 
 public class CP implements CommandExecutor
 {
     protected mcMMOIRC mirc;
     
     public CP(mcMMOIRC mirc)
     {
         this.mirc = mirc;
     }
     
     public boolean onCommand(CommandSender sender, Command command,
                              String label, String[] args)
     {
         PlayerProfile pp;
         String msg, party;
         Player p;
         int i;
         
         if(args.length < 1) {
             mirc.partyExec.onCommand(sender, command, label, new String[0]);
             return true;
         }
         
         if(sender instanceof Player) {
             p  = (Player) sender;
             pp = Users.getProfile(p);
             
             if(!Permissions.getInstance().party(p) || !pp.inParty())
                 return true;
             
             i     = 0;
            party = pp.getParty().getName();
         } else {
             if(args.length < 2)
                 return true;
             
             i     = 1;
             party = args[0];
         }
         
         for(msg = args[i], i++; i < args.length; i++)
             msg = msg.concat(" " + args[i]);
         
         mirc.partyMessage(sender, "chat", party, msg);
         return true;
     }
 }
