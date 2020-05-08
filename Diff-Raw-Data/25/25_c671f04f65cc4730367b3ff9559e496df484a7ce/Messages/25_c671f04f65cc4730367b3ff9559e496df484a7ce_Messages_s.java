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
 
 import org.bukkit.ChatColor;
 
 public class Messages
 {
     private static final String NEWLINE = "\n";
 
     private static String makeCmd( String command, String explanation, String... args )
     {
         StringBuilder sb = new StringBuilder();
 
         // command
         sb.append(ChatColor.YELLOW);
         sb.append(command);
         if ( args.length > 0 )
         {
             sb.append(" ");
             sb.append(ChatColor.BLUE);
             for ( int i = 0; i < args.length; i++ )
             {
                 String s = args[i];
                 sb.append(s);
                 if ( i != args.length - 1 )
                 {
                     sb.append(" ");
                 }
             }
         }
 
         sb.append(ChatColor.YELLOW);
         sb.append(": ");
         sb.append(ChatColor.WHITE);
         sb.append(explanation);
         sb.append(NEWLINE);
 
         return sb.toString();
     }
 
     private static String borderExplanation =
             "One day the holy mods and administrators will expand the border. It is then your mission to explore " +
             "strange new worlds, to seek out new life and new civilizations, to boldly go where no one has gone before.";
 
     public static String borderMessage =
             "Sorry Dude! This is the border... the final frontier! " + borderExplanation + NEWLINE +
             makeCmd("/cibp get", "shows the borders of the current world");
 
     public static String borderTeleportMessage =
             "Sorry Dude! You cannot teleport outside the border. " + borderExplanation + NEWLINE +
             makeCmd("/cibp get", "shows the borders of the current world");
 
     public static String helpGeneral =
             ChatColor.GREEN + "CraftInc BorderProtection - Usage:" + NEWLINE +
             makeCmd("help", "shows this help") +
             makeCmd("get | info", "shows the border of the current world") +
             makeCmd("on | off", "enables/disables the border of the current world") +
             makeCmd("set", "Border rectangle edges will be this far away from point of origin.", "<integer>") +
             makeCmd("set", "Border rectangle is defined by the two points. A point is specified as: x,z",
                     "<point1>", "<point2>");
 
     public static String borderCreationSuccessful
             = ChatColor.YELLOW + "New border was set " +
               ChatColor.GREEN + "successfully" +
               ChatColor.YELLOW + "!";
 
     public static String commandIssuedByNonPlayer
             = ChatColor.RED + "Only a player can use CraftInc BorderProtection commands!";
 
     public static String borderInfo( String worldName, String borderDef )
     {
 
         return ChatColor.WHITE + "Borders of world " +
                ChatColor.YELLOW + worldName +
                ChatColor.WHITE + ": " +
                ChatColor.YELLOW + borderDef;
     }
 
     public static String borderInfoNoBorderSet =
             ChatColor.YELLOW + "No border in this world.";
 
     public static String noPermissionSet =
            ChatColor.RED + "Sorry, you don't have permission to set the border.";
 
     public static String borderEnabled =
             ChatColor.YELLOW + "Border enabled.";
 
     public static String borderDisabled =
             ChatColor.YELLOW + "Border disabled.";
 
     public static String borderSaveException =
             ChatColor.RED + "Error: Could not save border on server. After the next reload this border will be lost!";
 
     public static String borderEnableDisableException =
             ChatColor.RED + "Error: Could not save border state on server. After the next reload this border state will be lost!";
 }
