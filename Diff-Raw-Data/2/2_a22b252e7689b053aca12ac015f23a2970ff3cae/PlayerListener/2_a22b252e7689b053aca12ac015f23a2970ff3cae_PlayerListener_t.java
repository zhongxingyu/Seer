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
 
 package de.minestar.contao2.listener;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import de.minestar.contao2.manager.DatabaseManager;
 import de.minestar.contao2.manager.PlayerManager;
 import de.minestar.contao2.manager.StatisticManager;
 import de.minestar.contao2.statistics.FreeLoginFailStat;
 import de.minestar.contao2.statistics.LoginStat;
 import de.minestar.contao2.statistics.LogoutStat;
 import de.minestar.contao2.units.ContaoGroup;
 import de.minestar.contao2.units.Settings;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarGroup;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.events.PlayerChangedGroupEvent;
 import de.minestar.minestarlibrary.stats.StatisticHandler;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class PlayerListener implements Listener {
 
     private PlayerManager playerManager;
     private DatabaseManager databaseManager;
     private StatisticManager statisticManager;
 
     private HashMap<String, ContaoGroup> oldGroups;
 
     public PlayerListener(PlayerManager playerManager, DatabaseManager databaseManager, StatisticManager statisticManager) {
         this.playerManager = playerManager;
         this.databaseManager = databaseManager;
         this.statisticManager = statisticManager;
 
         this.oldGroups = new HashMap<String, ContaoGroup>();
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerJoin(PlayerJoinEvent event) {
         this.playerManager.updatePlayer(event.getPlayer());
         this.playerManager.updateOnlineLists();
         if (Settings.showWelcomeMsg()) {
             this.playerManager.printOnlineList(event.getPlayer());
             this.statisticManager.printStatistics(event.getPlayer());
         }
         this.statisticManager.printWarnings(event.getPlayer());
 
         // GET MINESTAR-PLAYER
         MinestarPlayer thisPlayer = MinestarCore.getPlayer(event.getPlayer().getName());
 
         // PRINT INFO, IF GROUPS ARE DIFFERENT
         ContaoGroup oldGroup = this.oldGroups.get(event.getPlayer().getName());
         ContaoGroup currentGroup = ContaoGroup.getGroup(thisPlayer.getGroup());
 
         // IGNORE ADMINS
         if (currentGroup.equals(ContaoGroup.ADMIN))
             return;
 
         if (!currentGroup.equals(oldGroup)) {
             if (currentGroup.isHigher(oldGroup)) {
                 // UPDGRADE
                 PlayerUtils.sendMessage(event.getPlayer(), ChatColor.GREEN, "Du wurdest automatisch folgender Gruppe zugewiesen: " + currentGroup.name() + " (vorherige Gruppe: " + oldGroup.name() + ")");
             } else {
                 // DOWNGRADE
                 PlayerUtils.sendMessage(event.getPlayer(), ChatColor.RED, "Du wurdest automatisch folgender Gruppe zugewiesen: " + currentGroup.name() + " (vorherige Gruppe: " + oldGroup.name() + ")");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerPreLogin(PlayerPreLoginEvent event) {
 
         // GET MINESTAR-PLAYER
         MinestarPlayer thisPlayer = MinestarCore.getPlayer(event.getName());
 
         // IGNORE ADMINS
         if (thisPlayer.getMinestarGroup().equals(MinestarGroup.ADMIN)) {
             // FIRE STATISTIC
             StatisticHandler.handleStatistic(new LoginStat(event.getName(), MinestarGroup.ADMIN.getName(), true));
             return;
         }
 
         // SAVE OLD GROUP
         this.oldGroups.put(event.getName(), ContaoGroup.getGroup(thisPlayer.getGroup()));
 
         // PERFORM CONTAOCHECK
         this.databaseManager.performContaoCheck(thisPlayer.getPlayerName(), thisPlayer.getGroup());
 
         // PERFORM CHECK FOR FREE SPACE
         if (thisPlayer.getGroup().equalsIgnoreCase(ContaoGroup.FREE.getName())) {
             if (this.playerManager.getFreeSlots() < 1) {
                event.disallow(Result.KICK_OTHER, Settings.getNoFreeSlotsMsg());
 
                 // FIRE STATISTIC
                 StatisticHandler.handleStatistic(new FreeLoginFailStat(event.getName()));
             }
         }
 
         // FIRE STATISTIC
         StatisticHandler.handleStatistic(new LoginStat(event.getName(), MinestarCore.getPlayer(event.getName()).getGroup(), event.getResult().equals(Result.ALLOWED)));
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerQuit(PlayerQuitEvent event) {
         this.onPlayerDisconnect(event.getPlayer());
     }
 
     private void onPlayerDisconnect(Player player) {
         this.playerManager.removePlayer(player.getName());
         this.playerManager.updateOnlineLists();
 
         // FIRE STATISTIC
         StatisticHandler.handleStatistic(new LogoutStat(player.getName(), MinestarCore.getPlayer(player).getGroup()));
     }
 
     // ON PLAYER CHAT
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         // EVENT IS CANCELLED? => RETURN
         if (event.isCancelled())
             return;
 
         event.setFormat("%2$s");
 
         Player player = event.getPlayer();
         Boolean isHidden = MinestarCore.getPlayer(player).getBoolean("adminstuff.hide");
 
         // Hidden player are talking like ghosts...
         if (isHidden != null && isHidden) {
             event.setMessage(ChatColor.ITALIC + event.getMessage());
             return;
         }
 
         ChatColor col = ChatColor.GRAY;
         ChatColor prefixColor = ChatColor.WHITE;
         String prefix = "";
 
         ContaoGroup group = this.playerManager.getGroup(player);
         switch (group) {
             case ADMIN :
                 col = Settings.getAdminColor();
                 break;
             case MOD :
                 col = Settings.getModColor();
                 prefix = Settings.getModPrefix();
                 prefixColor = Settings.getModPrefixColor();
                 break;
             case PAY :
                 col = Settings.getPayColor();
                 break;
             case FREE :
                 col = Settings.getFreeColor();
                 break;
             case PROBE :
                 col = Settings.getProbeColor();
                 break;
             case X :
                 col = Settings.getXColor();
                 break;
         }
 
         event.setMessage(prefixColor + prefix + col + player.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage());
     }
 
     @EventHandler
     public void onPlayerChangedGroup(PlayerChangedGroupEvent event) {
         this.playerManager.movePlayer(event);
     }
 }
