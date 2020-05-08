 package me.limebyte.battlenight.core.listeners;
 
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.battle.Team;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.chat.Messaging;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class DeathListener implements Listener {
     // Called when player dies
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerDeath(PlayerDeathEvent event) {
         final Player player = event.getEntity();
         String name = player.getName();
 
         if (BattleNight.getBattle().usersTeam.containsKey(name)) {
             event.getDrops().clear();
             event.setDeathMessage("");
 
             Metadata.set(player, "respawn", true);
 
             if (!BattleNight.getBattle().isInLounge()) {
                 String colouredName = getColouredName(player);
 
                 try {
                     Player killer = player.getKiller();
                     Messaging.tellEveryone(getColouredName(killer) + ChatColor.GRAY + " killed " + colouredName + ".", true);
                 } catch (final NullPointerException error) {
                     Messaging.tellEveryone(colouredName + ChatColor.GRAY + " was killed.", true);
 
                     if (ConfigManager.get(Config.MAIN).getBoolean("Debug", false)) {
                         Messaging.debug(Level.WARNING, "Could not find killer for player: " + colouredName);
                     }
                 }
             }
 
             Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BattleNight.getInstance(), new Runnable() {
                 @Override
                 public void run() {
                     BattleNight.getBattle().removePlayer(player, true, null, null);
                 }
            }, 20L);
         }
 
         if (BattleNight.getInstance().getAPI().getBattle().containsPlayer(player)) {
             Metadata.set(player, "HandleRespawn", true);
             BattleNight.getInstance().getAPI().getBattle().onPlayerDeath(event);
         }
     }
 
     private String getColouredName(Player player) {
         String name = player.getName();
 
         if (BattleNight.getBattle().usersTeam.containsKey(name)) {
             Team team = BattleNight.getBattle().usersTeam.get(name);
             return team.getColour() + name;
         } else {
             return ChatColor.DARK_GRAY + name;
         }
     }
 }
