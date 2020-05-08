 package me.limebyte.battlenight.core.listeners;
 
 import java.util.List;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
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
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class CheatListener extends ListenerUsingAPI {
 
     public CheatListener(BattleNightAPI api) {
         super(api);
     }
 
     // //////////////////
     // General Events //
     // //////////////////
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         Player player = event.getPlayer();
         if (getAPI().getBattle().containsPlayer(player) && !SafeTeleporter.telePass.contains(player.getName())) {
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
                     if (!ConfigManager.get(Config.MAIN).getBoolean("Teleportation.Unknown", false)) {
                         event.setCancelled(true);
                         Messenger.tell(player, Message.NO_TELEPORTING);
                     }
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
         Battle battle = getAPI().getBattle();
         if (battle.containsPlayer(player)) {
             event.setCancelled(true);
             Messenger.tell(player, Message.NO_CHEATING);
         }
 
         if (battle.containsSpectator(player)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onInventoryClick(InventoryClickEvent event) {
         Player player = (Player) event.getWhoClicked();
         Battle battle = getAPI().getBattle();
         if (battle.containsPlayer(player)) {
             if (event.getCursor() != null && event.getSlotType() == SlotType.OUTSIDE) {
                 event.setCancelled(true);
                 Messenger.tell(player, Message.NO_CHEATING);
             }
         }
 
         if (battle.containsSpectator(player)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (event.isCancelled()) return;
 
         Player player = event.getPlayer();
         Battle battle = getAPI().getBattle();
         if (!battle.containsPlayer(player) || !battle.containsSpectator(player)) return;
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
     public void onEntityShootBow(EntityShootBowEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
         Battle battle = getAPI().getBattle();
 
         if (battle.isInProgress()) return;
         if (battle.containsPlayer(player)) {
             event.setCancelled(true);
             Messenger.tell(player, Message.NO_CHEATING);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onProjectileLaunch(ProjectileLaunchEvent event) {
         Battle battle = getAPI().getBattle();
         if (battle.isInProgress()) return;
         Projectile projectile = event.getEntity();
         if (projectile.getShooter() instanceof Player) {
             Player thrower = (Player) projectile.getShooter();
             if (battle.containsPlayer(thrower)) {
                 event.setCancelled(true);
                 Messenger.tell(thrower, Message.NO_CHEATING);
             }
         }
     }
 
     /** Spectator Events **/
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onInventoryOpen(InventoryOpenEvent event) {
        if (getAPI().getBattle().containsSpectator((Player) event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (getAPI().getBattle().containsSpectator((Player) event.getPlayer())) {
             event.setCancelled(true);
         }
     }
 }
