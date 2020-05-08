 package de.minestar.contao2.listener;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import de.minestar.contao2.manager.DatabaseManager;
 import de.minestar.contao2.manager.PlayerManager;
 import de.minestar.contao2.manager.StatisticManager;
 import de.minestar.contao2.units.ContaoGroup;
 import de.minestar.contao2.units.Settings;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class PlayerListener implements Listener {
     private Settings settings;
     private PlayerManager playerManager;
     private DatabaseManager databaseManager;
     private StatisticManager statisticManager;
 
     private HashMap<String, ContaoGroup> oldGroups;
 
     public PlayerListener(PlayerManager playerManager, DatabaseManager databaseManager, StatisticManager statisticManager, Settings settings) {
         this.playerManager = playerManager;
         this.databaseManager = databaseManager;
         this.statisticManager = statisticManager;
         this.settings = settings;
         this.oldGroups = new HashMap<String, ContaoGroup>();
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerJoin(PlayerJoinEvent event) {
         this.playerManager.updatePlayer(event.getPlayer());
         this.playerManager.updateOnlineLists();
         if (this.settings.isShowWelcomeMSG()) {
             this.playerManager.printOnlineList(event.getPlayer());
             this.statisticManager.printStatistics(event.getPlayer());
         }
         this.statisticManager.printWarnings(event.getPlayer());
 
         // GET MINESTAR-PLAYER
         MinestarPlayer thisPlayer = MinestarCore.getPlayer(event.getPlayer().getName());
 
         // PRINT INFO, IF GROUPS ARE DIFFERENT
         ContaoGroup oldGroup = this.oldGroups.get(event.getPlayer().getName());
         ContaoGroup currentGroup = ContaoGroup.getGroup(thisPlayer.getGroup());
 
         if (!currentGroup.equals(oldGroup)) {
             if (currentGroup.isGroupHigher(oldGroup)) {
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
         if (thisPlayer.getGroup().equalsIgnoreCase(ContaoGroup.ADMIN.getName()))
             return;
 
         // SAVE OLD GROUP
         this.oldGroups.put(event.getName(), ContaoGroup.getGroup(thisPlayer.getGroup()));
 
         // PERFORM CONTAOCHECK
         this.databaseManager.performContaoCheck(thisPlayer.getPlayerName(), thisPlayer.getGroup());
 
         // PERFORM CHECK FOR FREE SPACE
         if (thisPlayer.getGroup().equalsIgnoreCase(ContaoGroup.FREE.getName())) {
             if (this.playerManager.getFreeSlots() < 1) {
                 event.setKickMessage(this.settings.getNoFreeSlotsMSG());
                 event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
             }
         }
     }

     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerQuit(PlayerQuitEvent event) {
         this.onPlayerDisconnect(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerKick(PlayerKickEvent event) {
         // EVENT IS CANCELLED? => RETURN
         if (event.isCancelled())
             return;
 
         this.onPlayerDisconnect(event.getPlayer());
     }
 
     private void onPlayerDisconnect(Player player) {
         this.playerManager.removePlayer(player.getName());
         this.playerManager.updateOnlineLists();
     }
 
     // ON PLAYER CHAT
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerChat(PlayerChatEvent event) {
         // EVENT IS CANCELLED? => RETURN
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         String message = event.getMessage();
 
         ChatColor col = ChatColor.GRAY;
 
         ContaoGroup group = this.playerManager.getGroup(player);
         switch (group) {
             case ADMIN :
                 col = ChatColor.RED;
                 break;
             case PAY :
                 col = ChatColor.AQUA;
                 break;
             case FREE :
                 col = ChatColor.GREEN;
                 break;
             case PROBE :
                 col = ChatColor.DARK_PURPLE;
                 break;
             case X :
                 col = ChatColor.DARK_GRAY;
                 break;
         }
 
         event.setFormat("%2$s");
         event.setMessage(col + player.getDisplayName() + ChatColor.WHITE + ": " + message);
     }
 }
