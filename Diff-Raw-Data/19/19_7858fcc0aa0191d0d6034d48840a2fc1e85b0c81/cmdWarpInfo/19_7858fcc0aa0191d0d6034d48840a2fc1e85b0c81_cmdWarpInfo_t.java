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
 
     private void displayInformation(Warp warp, CommandSender sender) {
         // HEAD
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, "------------------------------");
        ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Informationen ber Warp '" + ChatColor.GRAY + warp.getName() + ChatColor.BLUE + "'");
 
         // OWNER
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s", ChatColor.BLUE + "Erstellt von:", ChatColor.GRAY + warp.getOwner()));
 
         // CREATION DATE
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s", ChatColor.BLUE + "Erstellt: ", ChatColor.GRAY + FORMAT.format(warp.getCreationDate())));
 
         // USE MODE
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s", ChatColor.BLUE + "Benutzbar von: ", ChatColor.GRAY + useModeToText(warp.getUseMode())));
 
         // PUBLIC OR PRIVATE
         if (warp.isPublic())
             ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s", ChatColor.BLUE + "Typ: ", ChatColor.GRAY + "ffentlich"));
         else {
             ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s", ChatColor.BLUE + "Typ: ", ChatColor.GRAY + "Privat"));
             // HAS GUESTS
             if (warp.getGuests().size() > 0)
                 ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s", ChatColor.BLUE + "Gste: ", ChatColor.GRAY + warp.getGuestList()));
         }
 
         // POSITION AND DISTANCE
         Location loc = warp.getLocation();
         ChatUtils.writeInfo(sender, String.format(ChatColor.BLUE + "X:" + ChatColor.GRAY + " %d " + ChatColor.BLUE + "Y:" + ChatColor.GRAY + " %d " + ChatColor.BLUE + "Z: " + ChatColor.GRAY + "%d " + ChatColor.BLUE + "Welt:" + ChatColor.GRAY + " %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockY(), loc.getWorld().getName()));
         if (sender instanceof Player) {
             Location loc2 = ((Player) sender).getLocation();
             ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, String.format("%s %s m", ChatColor.BLUE + "Entfernung: ", (int) (loc.distance(loc2))));
         }
         // END OF INFORMATION
 
         ChatUtils.writeColoredMessage(sender, ChatColor.WHITE, "------------------------------");
 
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
