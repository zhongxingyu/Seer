 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.commands.warp;
 
 import java.text.SimpleDateFormat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class cmdWarpInfo extends AbstractCommand {
 
     public cmdWarpInfo(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         warpInfo(args, player);
     }
 
     @Override
     public void execute(String[] args, ConsoleCommandSender console) {
         warpInfo(args, console);
     }
 
     private void warpInfo(String[] args, CommandSender sender) {
         String warpName = args[0];
         // SEARCH WARP
         Warp warp = Core.warpManager.getWarp(warpName);
         // NO WARP FOUND
         if (warp == null) {
             ChatUtils.writeError(sender, pluginName, "Der Warp '" + warpName + "' existiert nicht!");
             return;
         }
         // PLAYER IS NOT ALLOWED TO VIEW THE WARP
         if ((sender instanceof Player) && !warp.canUse((Player) sender)) {
             ChatUtils.writeError(sender, pluginName, "Du kannst keine Information ber den Warp '" + warp.getName() + "' abrufen!");
             return;
         }
 
         displayInformation(warp, sender);
     }
 
     private final static SimpleDateFormat FORMAT = new SimpleDateFormat("H:mm 'am' d.M.Y");
 
     private final static String SEPERATOR = "----------------------------------------";
     private final static ChatColor NAME_COLOR = ChatColor.GREEN;
     private final static ChatColor VALUE_COLOR = ChatColor.GRAY;
 
     private void displayInformation(Warp warp, CommandSender sender) {
         // HEAD
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, SEPERATOR);
         ChatUtils.writeColoredMessage(sender, NAME_COLOR, "Informationen ber Warp '" + VALUE_COLOR + warp.getName() + NAME_COLOR + "'");
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, SEPERATOR);
 
         // OWNER
         ChatUtils.writeMessage(sender, String.format("%s %s", NAME_COLOR + "Erstellt von:", VALUE_COLOR + warp.getOwner()));
 
         // CREATION DATE
        ChatUtils.writeMessage(sender, String.format("%s %s", NAME_COLOR + "Erstellt: ", VALUE_COLOR + FORMAT.format(warp.getCreationDate())));
 
         // USE MODE
        ChatUtils.writeMessage(sender, String.format("%s %s", NAME_COLOR + "Benutzbar von: ", VALUE_COLOR + useModeToText(warp.getUseMode())));
 
         // PUBLIC OR PRIVATE
         if (warp.isPublic())
            ChatUtils.writeMessage(sender, String.format("%s %s", NAME_COLOR + "Typ: ", VALUE_COLOR + "ffentlich"));
         else {
            ChatUtils.writeMessage(sender, String.format("%s %s", NAME_COLOR + "Typ: ", VALUE_COLOR + "Privat"));
             // HAS GUESTS
             if (warp.getGuests().size() > 0)
                ChatUtils.writeMessage(sender, String.format("%s %s", NAME_COLOR + "Gste: ", VALUE_COLOR + warp.getGuestList()));
         }
 
         // POSITION AND DISTANCE
         Location loc = warp.getLocation();
         ChatUtils.writeMessage(sender, String.format(NAME_COLOR + "X:" + VALUE_COLOR + " %d " + NAME_COLOR + "Y:" + VALUE_COLOR + " %d " + NAME_COLOR + "Z: " + VALUE_COLOR + "%d " + NAME_COLOR + "Welt:" + VALUE_COLOR + " %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockY(), loc.getWorld().getName()));
         if (sender instanceof Player) {
             Location loc2 = ((Player) sender).getLocation();
             ChatUtils.writeMessage(sender, String.format("%s %s m", NAME_COLOR + "Entfernung:", VALUE_COLOR + ("" + (int) (loc.distance(loc2)))));
         }
         // END OF INFORMATION
 
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, SEPERATOR);
 
     }
     private String useModeToText(byte useMode) {
         String result = "";
         if (useMode == Warp.COMMAND_USEMODE)
             result = "Befehlen";
         else if (useMode == Warp.SIGN_USEMODE)
             result = "Schildern";
         else if (useMode == (Warp.SIGN_USEMODE | Warp.COMMAND_USEMODE))
             result = "Befehlen und Schilder";
         else
             ConsoleUtils.printError(pluginName, "Unknown usemode : " + useMode);
 
         return result;
 
     }
 }
