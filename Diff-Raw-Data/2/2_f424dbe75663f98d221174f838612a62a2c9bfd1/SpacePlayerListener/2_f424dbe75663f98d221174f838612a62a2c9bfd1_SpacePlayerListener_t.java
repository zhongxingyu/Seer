 // Package Declaration
 package me.iffa.bananaspace.listeners;
 
 // Java Imports
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 // BananaSpace Imports
 import me.iffa.bananaspace.BananaSpace;
 import me.iffa.bananaspace.api.SpaceConfigHandler;
 import me.iffa.bananaspace.api.SpaceMessageHandler;
 import me.iffa.bananaspace.api.event.area.AreaEnterEvent;
 import me.iffa.bananaspace.api.event.area.AreaLeaveEvent;
 import me.iffa.bananaspace.api.event.misc.SpaceSuffocationEvent;
 import me.iffa.bananaspace.api.event.misc.TeleportToSpaceEvent;
 import me.iffa.bananaspace.runnables.SpaceRunnable2;
 
 // Bukkit Imports
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * PlayerListener for general space related actions.
  * 
  * @author iffa
  */
 public class SpacePlayerListener extends PlayerListener {
     // Variables
     public static Map<Player, Integer> taskid = new HashMap<Player, Integer>();
     public static Map<Player, Boolean> isUsed = new HashMap<Player, Boolean>();
     private final Map<Player, Boolean> inArea = new HashMap<Player, Boolean>();
     private final Map<Player, Boolean> fixDupe = new HashMap<Player, Boolean>();
     private int taskInt;
     private final BananaSpace plugin;
 
     /**
      * Constructor for SpacePlayerListener.
      * 
      * @param plugin BananaSpace 
      */
     public SpacePlayerListener(BananaSpace plugin) {
         this.plugin = plugin;
     }
 
     /**
      * Called when a player attempts to teleport.
      * 
      * @param event Event data
      */
     @Override
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (event.isCancelled()) {
             return;
         }
         Player player = event.getPlayer();
         if (!fixDupe.containsKey(event.getPlayer())) {
             if (BananaSpace.worldHandler.isSpaceWorld(event.getTo().getWorld()) && event.getTo().getWorld() != player.getWorld()) {
                 if (!plugin.getEconomy().enter(player)) {
                     SpaceMessageHandler.sendNotEnoughMoneyMessage(player);
                     event.setCancelled(true);
                     return;
                 }
                 if (SpaceConfigHandler.isHelmetGiven()) {
                     event.getPlayer().getInventory().setHelmet(
                             new ItemStack(SpaceConfigHandler.getHelmetBlock(), 1));
                 }
                 if (SpaceConfigHandler.isSuitGiven()) {
                     BananaSpace.getPlayerHandler().giveSpaceSuit(SpaceConfigHandler.getArmorType(), player);
                 }
                 /* Notify listeners start */
                 TeleportToSpaceEvent e = new TeleportToSpaceEvent("TeleportToSpaceEvent", event.getPlayer(), event.getFrom(), event.getTo());
                 Bukkit.getServer().getPluginManager().callEvent(e);
                 if (e.isCancelled()) {
                     event.setCancelled(true);
                 }
                 /* Notify listeners end */
                 SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + event.getPlayer().getName() + "' teleported to space.");
                 fixDupe.put(event.getPlayer(), true);
             } else if (!BananaSpace.worldHandler.isSpaceWorld(event.getTo().getWorld())
                     && BananaSpace.worldHandler.isSpaceWorld(event.getFrom().getWorld())) {
                 if (!plugin.getEconomy().exit(player)) {
                     event.setCancelled(true);
                     return;
                 }
                 if (SpaceConfigHandler.isHelmetGiven()) {
                    event.getPlayer().getInventory().setHelmet(new ItemStack(0, 1));
                 }
                 if (SpaceConfigHandler.isSuitGiven()) {
                     BananaSpace.getPlayerHandler().giveSpaceSuit("null", player);
                 }
             }
         } else {
             fixDupe.clear();
         }
     }
 
     /**
      * Called when a player attempts to move.
      * 
      * @param event Event data
      */
     @Override
     public void onPlayerMove(PlayerMoveEvent event) {
         if (event.isCancelled()) {
             return;
         }
         if (BananaSpace.worldHandler.isInAnySpace(event.getPlayer())) {
             int i = 0;
             Block block = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.UP);
             boolean insideArea = false;
             while (i < SpaceConfigHandler.getRoomHeight(event.getPlayer().getWorld())) {
                 if (block.getTypeId() != 0) {
                     insideArea = true;
                     i = 0;
                     break;
                 }
                 i++;
                 block = block.getRelative(BlockFace.UP);
             }
             if (insideArea == true) {
                 if (inArea.containsKey(event.getPlayer())) {
                     if (inArea.get(event.getPlayer()) == false) {
                         inArea.put(event.getPlayer(), true);
                         /* Notify listeners start */
                         AreaEnterEvent e = new AreaEnterEvent(event.getPlayer());
                         Bukkit.getServer().getPluginManager().callEvent(e);
                         /* Notify listeners end */
                         SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + event.getPlayer().getName() + "' entered an area.");
                     }
                 } else {
                     inArea.put(event.getPlayer(), true);
                     /* Notify listeners start */
                     AreaEnterEvent e = new AreaEnterEvent(event.getPlayer());
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     /* Notify listeners end */
                 }
                 if (isUsed.containsKey(event.getPlayer())) {
                     if (isUsed.get(event.getPlayer()) == true) {
                         BananaSpace.scheduler.cancelTask(taskid.get(event.getPlayer()));
                         isUsed.put(event.getPlayer(), false);
                     }
                 }
             } else {
                 if (inArea.containsKey(event.getPlayer())) {
                     if (inArea.get(event.getPlayer()) == true) {
                         inArea.put(event.getPlayer(), false);
                         /* Notify listeners start */
                         AreaLeaveEvent e = new AreaLeaveEvent(event.getPlayer());
                         Bukkit.getServer().getPluginManager().callEvent(e);
                         /* Notify listeners end */
                         SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + event.getPlayer().getName() + "' left an area.");
                     }
                 } else {
                     inArea.put(event.getPlayer(), false);
                     /* Notify listeners start */
                     AreaLeaveEvent e = new AreaLeaveEvent(event.getPlayer());
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     /* Notify listeners end */
                 }
                 if (!event.getPlayer().hasPermission("bananaspace.ignoresuitchecks")) {
                     if (SpaceConfigHandler.getRequireHelmet(event.getPlayer().getWorld()) && SpaceConfigHandler.getRequireSuit(event.getPlayer().getWorld())) {
                         checkNeedsSuffocation(SuitCheck.BOTH, event.getPlayer());
                         return;
                     } else if (SpaceConfigHandler.getRequireHelmet(event.getPlayer().getWorld())) {
                         checkNeedsSuffocation(SuitCheck.HELMET_ONLY, event.getPlayer());
                     } else if (SpaceConfigHandler.getRequireSuit(event.getPlayer().getWorld())) {
                         checkNeedsSuffocation(SuitCheck.SUIT_ONLY, event.getPlayer());
                     }
                 }
             }
         } else {
             if (isUsed.containsKey(event.getPlayer())) {
                 if (isUsed.get(event.getPlayer()) == true) {
                     BananaSpace.scheduler.cancelTask(taskid.get(event.getPlayer()));
                 }
             }
         }
     }
 
     /**
      * Checks if a player has a spacesuit (of the given armortype)
      * 
      * @param p Player
      * @param armortype Can be diamond, chainmail, gold, iron or leather
      * 
      * @return true if the player has a spacesuit of the type
      */
     private boolean hasSuit(Player p, String armortype) {
         if (armortype.equalsIgnoreCase("diamond")) {
             // Diamond
             if (p.getInventory().getBoots().getType() != Material.DIAMOND_BOOTS || p.getInventory().getChestplate().getType() != Material.DIAMOND_CHESTPLATE || p.getInventory().getLeggings().getType() != Material.DIAMOND_LEGGINGS) {
                 return false;
             }
             return true;
         } else if (armortype.equalsIgnoreCase("chainmail")) {
             // Chainmail
             if (p.getInventory().getBoots().getType() != Material.CHAINMAIL_BOOTS || p.getInventory().getChestplate().getType() != Material.CHAINMAIL_CHESTPLATE || p.getInventory().getLeggings().getType() != Material.CHAINMAIL_LEGGINGS) {
                 return false;
             }
             return true;
         } else if (armortype.equalsIgnoreCase("gold")) {
             // Gold
             if (p.getInventory().getBoots().getType() != Material.GOLD_BOOTS || p.getInventory().getChestplate().getType() != Material.GOLD_CHESTPLATE || p.getInventory().getLeggings().getType() != Material.GOLD_LEGGINGS) {
                 return false;
             }
             return true;
         } else if (armortype.equalsIgnoreCase("iron")) {
             // Iron
             if (p.getInventory().getBoots().getType() != Material.IRON_BOOTS || p.getInventory().getChestplate().getType() != Material.IRON_CHESTPLATE || p.getInventory().getLeggings().getType() != Material.IRON_LEGGINGS) {
                 return false;
             }
             return true;
         } else if (armortype.equalsIgnoreCase("leather")) {
             // Leather
             if (p.getInventory().getBoots().getType() != Material.LEATHER_BOOTS || p.getInventory().getChestplate().getType() != Material.LEATHER_CHESTPLATE || p.getInventory().getLeggings().getType() != Material.LEATHER_LEGGINGS) {
                 return false;
             }
             return true;
         }
         return false;
     }
 
     /**
      * Called when a player quits the game.
      * 
      * @param event Event data
      */
     @Override
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (taskid.containsKey(event.getPlayer())) {
             if (BananaSpace.scheduler.isCurrentlyRunning(taskid.get(event.getPlayer()))) {
                 BananaSpace.scheduler.cancelTask(taskid.get(event.getPlayer()));
                 SpaceMessageHandler.debugPrint(Level.INFO, "Cancelled suffocation task for player '" + event.getPlayer().getName() + "'. (reason: left server)");
             }
         }
     }
 
     /**
      * Enum to make things easier.
      */
     private enum SuitCheck {
 
         HELMET_ONLY,
         SUIT_ONLY,
         BOTH;
     }
 
     /**
      * Checks if a player should start suffocating.
      * 
      * @param suit SuitCheck
      * @param player Player
      */
     private void checkNeedsSuffocation(SuitCheck suit, Player player) {
         if (suit == SuitCheck.SUIT_ONLY) {
             if (isUsed.containsKey(player)) {
                 if (isUsed.get(player) == true && hasSuit(player, SpaceConfigHandler.getArmorType())) {
                     BananaSpace.scheduler.cancelTask(taskid.get(player));
                     isUsed.put(player, false);
                 } else if (isUsed.get(player) == false && !hasSuit(player, SpaceConfigHandler.getArmorType())) {
                     /* Notify listeners start */
                     SpaceSuffocationEvent e = new SpaceSuffocationEvent("SpaceSuffocationEvent", player);
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                         return;
                     }
                     /* Notify listeners end */
                     SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + player.getName() + "' started suffocating in space.");
                     SpaceRunnable2 task = new SpaceRunnable2(player);
                     taskInt = BananaSpace.scheduler.scheduleSyncRepeatingTask(plugin, task, 20L, 20L);
                     taskid.put(player, taskInt);
                     isUsed.put(player, true);
 
                 }
             } else {
                 if (hasSuit(player, SpaceConfigHandler.getArmorType())) {
                     isUsed.put(player, false);
                 } else {
                     /* Notify listeners start */
                     SpaceSuffocationEvent e = new SpaceSuffocationEvent("SpaceSuffocationEvent", player);
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                         return;
                     }
                     /* Notify listeners end */
                     SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + player.getName() + "' started suffocating in space.");
                     SpaceRunnable2 task = new SpaceRunnable2(player);
                     taskInt = BananaSpace.scheduler.scheduleSyncRepeatingTask(plugin, task, 20L, 20L);
                     taskid.put(player, taskInt);
                     isUsed.put(player, true);
                 }
             }
         } else if (suit == SuitCheck.HELMET_ONLY) {
             if (isUsed.containsKey(player)) {
                 if (isUsed.get(player) == true && player.getInventory().getHelmet().getTypeId() == SpaceConfigHandler.getHelmetBlock()) {
                     BananaSpace.scheduler.cancelTask(taskid.get(player));
                     isUsed.put(player, false);
                 } else if (isUsed.get(player) == false && player.getInventory().getHelmet().getTypeId() != SpaceConfigHandler.getHelmetBlock()) {
                     /* Notify listeners start */
                     SpaceSuffocationEvent e = new SpaceSuffocationEvent("SpaceSuffocationEvent", player);
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                         return;
                     }
                     /* Notify listeners end */
                     SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + player.getName() + "' started suffocating in space.");
                     SpaceRunnable2 task = new SpaceRunnable2(player);
                     taskInt = BananaSpace.scheduler.scheduleSyncRepeatingTask(plugin, task, 20L, 20L);
                     taskid.put(player, taskInt);
                     isUsed.put(player, true);
 
                 }
             } else {
                 if (player.getInventory().getHelmet().getTypeId() == SpaceConfigHandler.getHelmetBlock()) {
                     isUsed.put(player, false);
                 } else {
                     /* Notify listeners start */
                     SpaceSuffocationEvent e = new SpaceSuffocationEvent("SpaceSuffocationEvent", player);
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                         return;
                     }
                     /* Notify listeners end */
                     SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + player.getName() + "' started suffocating in space.");
                     SpaceRunnable2 task = new SpaceRunnable2(player);
                     taskInt = BananaSpace.scheduler.scheduleSyncRepeatingTask(plugin, task, 20L, 20L);
                     taskid.put(player, taskInt);
                     isUsed.put(player, true);
                 }
             }
         } else if (suit == SuitCheck.BOTH) {
             if (isUsed.containsKey(player)) {
                 if (isUsed.get(player) == true && player.getInventory().getHelmet().getTypeId() == SpaceConfigHandler.getHelmetBlock() && hasSuit(player, SpaceConfigHandler.getArmorType())) {
                     BananaSpace.scheduler.cancelTask(taskid.get(player));
                     isUsed.put(player, false);
                 } else if (isUsed.get(player) == false && player.getInventory().getHelmet().getTypeId() != SpaceConfigHandler.getHelmetBlock() || !hasSuit(player, SpaceConfigHandler.getArmorType())) {
                     /* Notify listeners start */
                     SpaceSuffocationEvent e = new SpaceSuffocationEvent("SpaceSuffocationEvent", player);
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                         return;
                     }
                     /* Notify listeners end */
                     SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + player.getName() + "' started suffocating in space.");
                     SpaceRunnable2 task = new SpaceRunnable2(player);
                     taskInt = BananaSpace.scheduler.scheduleSyncRepeatingTask(plugin, task, 20L, 20L);
                     taskid.put(player, taskInt);
                     isUsed.put(player, true);
                 }
             } else {
                 if (player.getInventory().getHelmet().getTypeId() == SpaceConfigHandler.getHelmetBlock() && hasSuit(player, SpaceConfigHandler.getArmorType())) {
                     isUsed.put(player, false);
                 } else {
                     /* Notify listeners start */
                     SpaceSuffocationEvent e = new SpaceSuffocationEvent("SpaceSuffocationEvent", player);
                     Bukkit.getServer().getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                         return;
                     }
                     /* Notify listeners end */
                     SpaceMessageHandler.debugPrint(Level.INFO, "Player '" + player.getName() + "' started suffocating in space.");
                     SpaceRunnable2 task = new SpaceRunnable2(player);
                     taskInt = BananaSpace.scheduler.scheduleSyncRepeatingTask(plugin, task, 20L, 20L);
                     taskid.put(player, taskInt);
                     isUsed.put(player, true);
                 }
             }
         }
     }
 }
