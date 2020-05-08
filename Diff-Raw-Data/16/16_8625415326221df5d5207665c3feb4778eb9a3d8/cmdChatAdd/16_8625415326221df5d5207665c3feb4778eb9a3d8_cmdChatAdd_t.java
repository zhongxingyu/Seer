 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package de.minestar.AdminStuff.commands;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.ASPlayer;
 import de.minestar.AdminStuff.manager.PlayerManager;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 // TODO: Rename
 public class cmdChatAdd extends AbstractExtendedCommand {
 
     private PlayerManager pManager;

     public cmdChatAdd(String syntax, String arguments, String node, PlayerManager pManager) {
         super(Core.NAME, syntax, arguments, node);
         this.pManager = pManager;
     }
 
     @Override
     /**
      * Representing the command <br>
      * /chat <Player 1.. Player n><br>
      * Reply a player
      * 
      * @param player
      *            Called the command
      * @param split
      * 	          list of Players
      */
     public void execute(String[] args, Player player) {
 
         if (args == null || args.length == 0)
             deleteRecipientList(player);
         else
             createRecipientList(args, player);
     }
 
     private void deleteRecipientList(Player player) {
         // ADD PLAYER, IF NOT FOUND
         ASPlayer thisPlayer = pManager.getPlayer(player);
 
         // DELETE RECIPIENTLIST
         thisPlayer.clearRecipients();
         PlayerUtils.sendSuccess(player, pluginName, "Jetzt empfaengt jeder deine Nachrichten");
     }
 
     private void createRecipientList(String[] args, Player player) {
         // ADD PLAYER, IF NOT FOUND
         ASPlayer thisASPlayer = pManager.getPlayer(player);
 
         // ADD RECIPIENTLIST
         StringBuilder output = new StringBuilder(256);
         Set<String> recipientSet = new HashSet<String>();
         Player temp = null;
         for (String arg : args) {
             temp = PlayerUtils.getOnlinePlayer(arg);
             if (temp != null) {
                 recipientSet.add(temp.getName());
                 output.append(temp.getName());
                 output.append(", ");
             }
         }
         if (recipientSet.isEmpty()) {
             PlayerUtils.sendError(player, pluginName, "Kein Empfaenger gefunden!");
             return;
         }
         // delete the last ", "
         output.delete(output.length() - 2, output.length());
 
         // only this player will recieve the messages
         thisASPlayer.setRecipients(recipientSet);
         PlayerUtils.sendSuccess(player, pluginName, "Folgende Spieler erhalten jetzt deine Nachrichten:");
         PlayerUtils.sendInfo(player, output.toString());
     }
 }
