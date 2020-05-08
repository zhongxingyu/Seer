 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of Contao2.
  * 
  * Contao2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * Contao2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Contao2.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.contao2.commands.user;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.contao2.manager.DatabaseManager;
 import de.minestar.contao2.units.MCWarning;
 import de.minestar.contao2.units.PlayerWarnings;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 
 public class cmdRemoveWarning extends AbstractCommand {
 
     private DatabaseManager databaseManager;
 
     public cmdRemoveWarning(String syntax, String arguments, String node, DatabaseManager databaseManager) {
         super(Core.NAME, syntax, arguments, node);
         this.databaseManager = databaseManager;
     }
 
     @Override
     public void execute(String[] args, Player player) {
         removeWarning(args, player);
     }
 
     @Override
     public void execute(String[] args, ConsoleCommandSender console) {
         removeWarning(args, console);
     }
 
     private void removeWarning(String[] args, CommandSender sender) {
 
         String playerName = args[0];
         int warningNumber = 0;
         try {
             warningNumber = Integer.parseInt(args[1]);
         } catch (Exception e) {
             ChatUtils.writeError(sender, pluginName, args[1] + " ist keine Zahl!");
             return;
         }
 
         if (!databaseManager.isMCNickInMCTable(playerName)) {
             ChatUtils.writeError(sender, pluginName, "Spieler '" + playerName + "' nicht gefunden!");
             return;
         }
 
         PlayerWarnings warnings = databaseManager.getsManager().getWarnings(playerName);
         if (warnings == null) {
             ChatUtils.writeError(sender, pluginName, "Spieler '" + playerName + "' hat keine Verwarnungen!");
             return;
         }
 
         MCWarning warning = warnings.getWarning(warningNumber);
         if (warning == null) {
             ChatUtils.writeError(sender, pluginName, "Verwarnungsnummer nicht gefunden!");
             return;
         }
 
         if (databaseManager.removeWarning(playerName, warning.getDate())) {
             databaseManager.getsManager().getWarnings(playerName).removeWarning(warningNumber);
             ChatUtils.writeSuccess(sender, pluginName, "Verwarnung wurde erfolgreich gelscht!");
         } else
            ChatUtils.writeError(sender, pluginName, "Fehler beim lschen der Verwarnung!");
     }
 }
