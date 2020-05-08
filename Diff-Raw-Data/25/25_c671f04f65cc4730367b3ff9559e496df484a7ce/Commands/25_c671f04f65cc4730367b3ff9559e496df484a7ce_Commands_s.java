 /*  Craft Inc. BorderProtection
     Copyright (C) 2013  Paul Schulze, Tobias Ottenweller
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package de.craftinc.borderprotection;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.IOException;
 
 public class Commands implements CommandExecutor
 {
     private BorderManager borderManager;
 
     public Commands( BorderManager borderManager )
     {
         this.borderManager = borderManager;
     }
 
     @Override
     public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
     {
         // Check if command comes from a player.
         if ( !( sender instanceof Player ) )
         {
             sender.sendMessage(Messages.commandIssuedByNonPlayer);
             return true;
         }
 
         // command for all actions
         if ( command.getName().equalsIgnoreCase("cibp") )
         {
             // help
             if ( args.length == 0 || ( args.length > 0 && args[0].equalsIgnoreCase("help") ) )
             {
                 sender.sendMessage(Messages.helpGeneral);
                 return true;
             }
 
             // set
             if ( ( args.length == 2 || args.length == 3 ) && args[0].equalsIgnoreCase("set") )
             {
                 if ( !sender.hasPermission("craftinc.borderprotection.set") )
                 {
                     sender.sendMessage(Messages.noPermissionSet);
                     return false;
                 }
                 World world = ( (Player) sender ).getWorld();
 
                 // set <distance>
                 if ( args.length == 2 )
                 {
                     try
                     {
                         borderManager.setBorder(( (Player) sender ).getWorld(), Double.parseDouble(args[1]));
                         Border border = Border.getBorders().get(world);
                         sender.sendMessage(Messages.borderCreationSuccessful);
                         sender.sendMessage(Messages.borderInfo(world.getName(), border.toString()));
                     }
                     catch ( Exception e )
                     {
                         sender.sendMessage(e.getMessage());
                     }
                 }
                 // set <point1> <point2>
                 else
                 {
                     try
                     {
                         borderManager.setBorder(( (Player) sender ).getWorld(), args[1], args[2]);
                         Border border = Border.getBorders().get(world);
                         sender.sendMessage(Messages.borderCreationSuccessful);
                         sender.sendMessage(Messages.borderInfo(world.getName(), border.toString()));
                     }
                     catch ( Exception e )
                     {
                         sender.sendMessage(e.getMessage());
                     }
                 }
 
                 // save the new border
                 try
                 {
                     Border.saveBorders();
                 }
                 catch ( IOException e )
                 {
                     sender.sendMessage(Messages.borderSaveException);
                 }
                 return true;
             }
 
             // get
             if ( args.length == 1 && ( args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("info") ) )
             {
                 World world = ( (Player) sender ).getWorld();
 
                 // exit and send the player a message if no border is set
                 if ( !Border.getBorders().containsKey(world) )
                 {
                     sender.sendMessage(Messages.borderInfoNoBorderSet);
                     return true;
                 }
 
                 Border border = Border.getBorders().get(world);
 
                 sender.sendMessage(Messages.borderInfo(world.getName(), border.toString()));
                 return true;
             }
 
             // on
             if ( args.length == 1 && ( args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off") ) )
             {
                 World world = ( (Player) sender ).getWorld();
                 Border border = Border.getBorders().get(world);
 
                if (border != null)
                 {
                    if (args[0].equalsIgnoreCase("on")) {
                         border.enable();
                         sender.sendMessage(Messages.borderEnabled);
                    } else {
                         border.disable();
                         sender.sendMessage(Messages.borderDisabled);
                     }
                 }
                 else
                 {
                     sender.sendMessage(Messages.borderInfoNoBorderSet);
                 }
 
                 // save the new border
                 try
                 {
                     Border.saveBorders();
                 }
                 catch ( IOException e )
                 {
                     sender.sendMessage(Messages.borderEnableDisableException);
                 }
                 return true;
             }
         }
 
         sender.sendMessage(Messages.helpGeneral);
         return false;
     }
 }
