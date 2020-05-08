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
 
 package de.minestar.contao2.commands.ppay;
 
 import java.text.SimpleDateFormat;
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.contao2.manager.DatabaseManager;
 import de.minestar.contao2.manager.PlayerManager;
 import de.minestar.contao2.units.ContaoGroup;
 import de.minestar.contao2.units.MCUser;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarGroup;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdPayWeek extends AbstractExtendedCommand {
 
     private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
     private PlayerManager playerManager;
     private DatabaseManager databaseManager;
 
     public cmdPayWeek(String syntax, String arguments, String node, PlayerManager pManager, DatabaseManager databaseManager) {
         super(Core.NAME, syntax, arguments, node);
         this.description = "Use your free payweek (only new FreeUsers)";
         this.playerManager = pManager;
         this.databaseManager = databaseManager;
     }
 
     @Override
     public void execute(String[] args, Player player) {
         MinestarPlayer msPlayer = MinestarCore.getPlayer(player);
 
         // ONLY FREEUSERS CAN USE THIS COMMAND
         if (!msPlayer.getMinestarGroup().equals(MinestarGroup.FREE)) {
             PlayerUtils.sendError(player, Core.NAME, "Free-User only!");
             return;
         }
 
         // GET CONTAOUSER
        MCUser user = databaseManager.getIngameData(player.getName());
         if (user == null) {
             PlayerUtils.sendError(player, Core.NAME, "Fehler: Minecraftnick nicht gefunden");
             return;
         }
 
         // GUTSCHEIN EINGELST?
         if (this.databaseManager.hasUsedFreeWeek(player.getName())) {
             PlayerUtils.sendError(player, Core.NAME, "Du hast deinen Gutschein bereits eingelst!");
             return;
         }
 
         // PAYUSER-DATUM SETZEN
         this.databaseManager.setExpDateInMCTable(dateFormat.format((System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7))), user.getContaoID());
 
         // CONTAO GRUPPE AUF PAY SETZEN
         databaseManager.updateContaoGroup(ContaoGroup.PAY, user.getContaoID());
 
         // GUTSCHEIN ALS EINGELST SETZEN
         this.databaseManager.setFreeWeekUsed(player.getName());
 
         // UPDATE GROUPMANAGER-GROUP
         this.playerManager.updateGroupManagerGroup(player.getName(), ContaoGroup.PAY);
     }
 }
