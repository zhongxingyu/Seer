 package com.norcode.bukkit.buildinabox.listeners;
 
 import com.norcode.bukkit.buildinabox.*;
 import com.norcode.bukkit.buildinabox.BuildChest.UnlockingTask;
 import com.norcode.bukkit.buildinabox.events.BIABLockEvent;
 import com.norcode.bukkit.buildinabox.events.BIABPlaceEvent;
 import com.norcode.bukkit.schematica.Session;
 import com.norcode.bukkit.schematica.exceptions.SchematicSaveException;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.util.Vector;
 
 import java.util.EnumSet;
 
 public class PlayerListener implements Listener {
 
     BuildInABox plugin;
 
     EnumSet lockableBlockTypes = EnumSet.of(Material.CHEST, Material.TRAPPED_CHEST,
             Material.TRAP_DOOR, Material.WOODEN_DOOR, Material.IRON_DOOR, Material.FURNACE,
             Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.BREWING_STAND,
             Material.JUKEBOX, Material.ANVIL, Material.BURNING_FURNACE, Material.BEACON);
 
     public PlayerListener(BuildInABox plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (plugin.updater == null) return;
         if (event.getPlayer().hasPermission("biab.admin")) {
             final String playerName = event.getPlayer().getName();
             plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                 public void run() {
                     Player player = plugin.getServer().getPlayer(playerName);
                     if (player != null && player.isOnline()) {
                         switch (plugin.updater.getResult()) {
                             case UPDATE_AVAILABLE:
                                 player.sendMessage(BuildInABox.getNormalMsg("update-available", "http://dev.bukkit.org/server-mods/build-in-a-box/"));
                                 break;
                             case SUCCESS:
                                 player.sendMessage(BuildInABox.getNormalMsg("update-downloaded"));
                                 break;
                         }
                     }
                 }
             }, 20);
         }
     }
 
     @EventHandler(ignoreCancelled=true, priority = EventPriority.LOW)
     public void onPlayerSelection(PlayerInteractEvent event) {
         if (event.getItem() != null && event.getItem().getTypeId() == plugin.getConfig().getInt("selection-wand-id", 294)) { // GOLD_HOE
             if (!event.getPlayer().hasPermission("biab.select")) {
                  return;
             }
             if (event.getPlayer().hasMetadata("biab-pending-save") && event.getClickedBlock().getTypeId() == plugin.cfg.getChestBlockId()) {
                 plugin.debug("Offset selected.");
                 BuildingPlan planData = (BuildingPlan) event.getPlayer().getMetadata("biab-pending-save").get(0).value();
                 event.getPlayer().removeMetadata("biab-pending-save", plugin);
                 BuildingPlan plan = null;
                 try {
                     plan = BuildingPlan.fromClipboard(plugin, event.getPlayer(), planData.getName(), event.getClickedBlock().getLocation());
                 } catch (SchematicSaveException e) {
                     e.printStackTrace();
                     event.getPlayer().sendMessage(BuildInABox.getErrorMsg("save-failed"));
                     return;
                 }
                 plan.setFilename(planData.getFilename());
                 plan.setDisplayName(planData.getDisplayName());
                 plan.setDescription(planData.getDescription());
                 plugin.getDataStore().saveBuildingPlan(plan);
                 plan.registerPermissions();
                 event.getPlayer().sendMessage(BuildInABox.getSuccessMsg("building-plan-saved", plan.getDisplayName()));
             } else {
                 Session session = plugin.getPlayerSession(event.getPlayer());
                 if (event.getClickedBlock() != null) {
                     Vector v = event.getClickedBlock().getLocation().toVector();
                     if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                         session.getSelection().setPt1(event.getClickedBlock().getLocation());
                         event.getPlayer().sendMessage(BuildInABox.getSuccessMsg("selection-pt1-set", "X:" + v.getBlockX() + " Y:" + v.getBlockY() + " Z:" + v.getBlockZ()));
                     } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                         session.getSelection().setPt2(event.getClickedBlock().getLocation());
                         event.getPlayer().sendMessage(BuildInABox.getSuccessMsg("selection-pt2-set", "X:" + v.getBlockX() + " Y:" + v.getBlockY() + " Z:" + v.getBlockZ()));
                     } else {
                         return;
                     }
                 }
             }
             event.setCancelled(true);
             event.setUseInteractedBlock(Result.DENY);
             event.setUseItemInHand(Result.DENY);
         }
     }
 
     @EventHandler(ignoreCancelled=true, priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         Block block = event.getClickedBlock();
         if (block.getTypeId() == plugin.cfg.getChestBlockId()) {
             if (block.hasMetadata("buildInABox")) {
                 BuildChest bc = (BuildChest) block.getMetadata("buildInABox").get(0).value();
                 if (!bc.canInteract()) {
                     event.setCancelled(true);
                     event.setUseInteractedBlock(Result.DENY);
                     event.setUseItemInHand(Result.DENY);
                     return;
                 }
 
                 bc.updateActivity();
                 if (bc.isPreviewing() && bc.getPreviewingPlayer().equals(player.getName())) {
                     if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                         // Cancel
                         bc.endPreview(player);
                     } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                         // build
                         bc.build(player);
                     }
                 } else {
                     long now = System.currentTimeMillis();
                     if (bc.isLocking()) {
                         if (!bc.getLockingTask().lockingPlayer.equals(player.getName())) {
                             if (plugin.getConfig().getBoolean("allow-unlocking-others", true)) {
                                 if (player.hasPermission("biab.unlock.others")) {
                                     BIABLockEvent le = new BIABLockEvent(player, bc, (bc.getLockingTask() instanceof UnlockingTask) ? BIABLockEvent.Type.UNLOCK_CANCEL : BIABLockEvent.Type.LOCK_CANCEL);
                                     plugin.getServer().getPluginManager().callEvent(le);
                                     if (le.isCancelled()) {
                                         return;
                                     }
                                     String msgKey = "lock-attempt-cancelled";
                                     if (bc.getLockingTask() instanceof UnlockingTask) {
                                         msgKey = "un" + msgKey;
                                     }
                                     player.sendMessage(BuildInABox.getNormalMsg(msgKey, bc.getLockingTask().lockingPlayer));
                                     bc.getLockingTask().cancel();
                                 } else {
                                     player.sendMessage(BuildInABox.getErrorMsg("no-permission"));
                                 }
                             }
                         } else {
                             bc.getLockingTask().cancel();
                         }
                     } else if (player.hasMetadata("biab-permanent-timeout")) {
                         long timeout = player.getMetadata("biab-permanent-timeout").get(0).asLong();
                         if (timeout > now) {
                             if (plugin.getConfig().getBoolean("protect-buildings", true)) {
                                 bc.unprotect();
                                 plugin.getDataStore().deleteChest(bc.getId());
                             }
                         }
                         player.removeMetadata("biab-permanent-timeout", plugin);
                     } else if (now - bc.getLastClicked() < plugin.getConfig().getInt("double-click-interval",2000)
                             && bc.getLastClickType().equals(event.getAction())
                             && player.getName().equals(bc.getLastClickedBy())) {
                         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                             // pick up
                             if (!bc.isLocked()) {
                                 if (player.hasPermission("biab.pickup." + bc.getPlan().getName().toLowerCase())) {
                                     bc.pickup(player);
                                 } else {
                                     player.sendMessage(BuildInABox.getErrorMsg("no-permission"));
                                 }
                             }
                         } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                             // lock/unlock
                             if (plugin.getConfig().getBoolean("allow-locking", true)) {
                                 if (bc.isLocked()) {
                                     if (bc.getLockedBy().equals(player.getName())) {
                                         if (player.hasPermission("biab.unlock." + bc.getPlan().getName().toLowerCase())) {
                                             bc.unlock(player);
                                         } else {
                                             player.sendMessage(BuildInABox.getErrorMsg("no-permission"));
                                         }
                                     } else if (plugin.getConfig().getBoolean("allow-unlocking-others")) {
                                         if (player.hasPermission("biab.unlock.others")) {
                                             bc.unlock(player);
                                         } else {
                                             player.sendMessage(BuildInABox.getErrorMsg("no-permission"));
                                         }
                                     }
                                 } else {
                                     if (player.hasPermission("biab.lock." + bc.getPlan().getName().toLowerCase())) {
                                         bc.lock(player);
                                     } else {
                                         player.sendMessage(BuildInABox.getErrorMsg("no-permission"));
                                     }
                                 }
                             }
                         }
                         bc.setLastClicked(-1);
                         bc.setLastClickType(null);
                         bc.setLastClickedBy(null);
                     } else {
                         bc.setLastClicked(now);
                         bc.setLastClickType(event.getAction());
                         bc.setLastClickedBy(event.getPlayer().getName());
                         player.sendMessage(bc.getDescription());
                     }
                 }
                 event.setCancelled(true);
                 event.setUseInteractedBlock(Result.DENY);
                 event.setUseItemInHand(Result.DENY);
             }
         }
     }
 
 
     @EventHandler(ignoreCancelled=true)
     public void onPlaceEnderchest(final BlockPlaceEvent event) {
         if (event.getBlock().getTypeId() == plugin.cfg.getChestBlockId()) {
             if (plugin.getConfig().getBoolean("prevent-placing-enderchests", false)) {
                 ChestData data = plugin.getDataStore().fromItemStack(event.getItemInHand());
                 if (data == null) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGH)
     public void onBlockPlace(final BlockPlaceEvent event) {
         ChestData data = plugin.getDataStore().fromItemStack(event.getItemInHand());
         if (data != null) {
             if (!event.getPlayer().hasPermission("biab.place." + data.getPlanName().toLowerCase())) {
                 event.getPlayer().sendMessage(BuildInABox.getErrorMsg("no-permission"));
                 event.setCancelled(true);
                 event.setBuild(false);
                 return;
             }
             BIABPlaceEvent placeEvent = new BIABPlaceEvent(event.getPlayer(), event.getBlock().getLocation(), event.getItemInHand(), data);
             plugin.getServer().getPluginManager().callEvent(placeEvent);
             if (placeEvent.isCancelled()) {
                 event.setCancelled(true);
                 return;
             }
             data.setLocation(event.getBlock().getLocation());
             data.setLastActivity(System.currentTimeMillis());
             final BuildChest bc = new BuildChest(data);
             event.getBlock().setMetadata("buildInABox", new FixedMetadataValue(plugin, bc));
             event.getPlayer().getInventory().setItemInHand(null);
             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                 public void run() {
                     if (event.getPlayer().isOnline()) {
                         bc.preview(event.getPlayer());
                         plugin.checkCarrying(event.getPlayer());
                     }
                 }
             }, 1);
         } else if (plugin.getConfig().getBoolean("prevent-placing-enderchests", false)) {
             event.setCancelled(true);
             event.setBuild(false);
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerQuit(PlayerQuitEvent event) {
         event.getPlayer().removeMetadata("biab-selection-session", plugin);
     }
 
     @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
     public void onFakeBlockPlace(final BlockPlaceEvent event) {
         if (event instanceof FakeBlockPlaceEvent) {
             // prevent them from getting logged.
             ((FakeBlockPlaceEvent) event).setWasCancelled(event.isCancelled());
             event.setCancelled(true);
         }
     }
 
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerInteractLocked(PlayerInteractEvent event) {
         if (plugin.cfg.isLockingEnabled()) {
             if (lockableBlockTypes.contains(event.getClickedBlock().getType())) {
                 if (event.getClickedBlock().hasMetadata("biab-block")) {
                     BuildChest bc = (BuildChest) event.getClickedBlock().getMetadata("biab-block").get(0).value();
                     if (bc.isLocked() && !bc.getLockedBy().equals(event.getPlayer().getName())) {
                         event.getPlayer().sendMessage(BuildInABox.getErrorMsg("building-is-locked", bc.getPlan().getDisplayName(), bc.getLockedBy()));
                         event.setCancelled(true);
                         event.setUseInteractedBlock(Result.DENY);
                     }
                 }
             }
         }
     }
 }
