 package me.limebyte.battlenight.core.listeners;
 
 import java.util.List;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class CheatListener implements Listener {
 
     // //////////////////
     // General Events //
     // //////////////////
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         Player player = event.getPlayer();
         if (BattleNight.getBattle().usersTeam.containsKey(player.getName()) && !SafeTeleporter.telePass.contains(player.getName())) {
             switch (event.getCause()) {
                 case COMMAND:
                     if (!ConfigManager.get(Config.MAIN).getBoolean("Teleportation.Commands", false)) {
                         event.setCancelled(true);
                         Messenger.tell(player, Message.NO_TELEPORTING);
                     }
                     break;
                 case PLUGIN:
                     if (!ConfigManager.get(Config.MAIN).getBoolean("Teleportation.Plugins", false)) {
                         event.setCancelled(true);
                         Messenger.tell(player, Message.NO_TELEPORTING);
                     }
                    Messenger.tell(player, Message.NO_TELEPORTING);
                     break;
                 case ENDER_PEARL:
                     if (!ConfigManager.get(Config.MAIN).getBoolean("Teleportation.EnderPearls", true)) {
                         event.setCancelled(true);
                         Messenger.tell(player, Message.NO_TELEPORTING);
                     }
                     break;
                 case NETHER_PORTAL:
                 case END_PORTAL:
                     if (!ConfigManager.get(Config.MAIN).getBoolean("Teleportation.Portals", false)) {
                         event.setCancelled(true);
                         Messenger.tell(player, Message.NO_TELEPORTING);
                     }
                     break;
                 case UNKNOWN:
                     event.setCancelled(true);
                     break;
                 default:
                     event.setCancelled(true);
                     break;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerDropItem(PlayerDropItemEvent event) {
         Player player = event.getPlayer();
         if (BattleNight.getBattle().usersTeam.containsKey(player.getName())) {
             event.setCancelled(true);
             Messenger.tell(player, Message.NO_CHEATING);
         }
 
         if (BattleNight.getBattle().spectators.contains(player.getName())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (event.isCancelled()) return;
 
         Player player = event.getPlayer();
         if (!BattleNight.getBattle().usersTeam.containsKey(player.getName()) || !BattleNight.getBattle().spectators.contains(player.getName())) return;
         if (!ConfigManager.get(Config.MAIN).getBoolean("Commands.Block", true)) return;
 
         List<String> whitelist = ConfigManager.get(Config.MAIN).getStringList("Commands.Whitelist");
         whitelist.add("bn");
 
         String[] cmdArg = event.getMessage().split(" ");
         String cmdString = cmdArg[0].trim().substring(1).toLowerCase();
 
         try {
             Command command = Bukkit.getServer().getPluginCommand(cmdString);
 
             // Check if the command is listed
             if (whitelist.contains(command.getLabel().toLowerCase())) return;
 
             // Check if there are any aliases listed
             if (!command.getAliases().isEmpty()) {
                 for (String alias : command.getAliases()) {
                     if (whitelist.contains(alias.toLowerCase())) return;
                 }
             }
         } catch (NullPointerException e) {
             // Check if the command is listed
             if (whitelist.contains(cmdString)) return;
         }
 
         // Its not listed so block it
         event.setCancelled(true);
         Messenger.tell(player, Message.NO_COMMAND);
         return;
     }
 
     // //////////////////
     // Lounge Events //
     // //////////////////
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onProjectileLaunch(ProjectileLaunchEvent event) {
         if (!BattleNight.getBattle().isInLounge()) return;
         final Projectile projectile = event.getEntity();
         if (projectile.getShooter() instanceof Player) {
             final Player thrower = (Player) projectile.getShooter();
             if (BattleNight.getBattle().usersTeam.containsKey(thrower.getName())) {
                 event.setCancelled(true);
                 Messenger.tell(thrower, Message.NO_CHEATING);
             }
         }
     }
 
     /** Spectator Events **/
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onInventoryOpen(InventoryOpenEvent event) {
         String name = event.getPlayer().getName();
         if (BattleNight.getBattle().spectators.contains(name)) {
             event.setCancelled(true);
         }
     }
 }
