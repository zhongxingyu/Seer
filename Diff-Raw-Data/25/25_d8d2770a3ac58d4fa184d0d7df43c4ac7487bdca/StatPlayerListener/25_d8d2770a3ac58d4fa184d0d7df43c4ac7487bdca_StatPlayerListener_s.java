 package com.tehbeard.BeardStat.listeners;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import net.dragonzone.promise.Delegate;
 import net.dragonzone.promise.Promise;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.PlayerLeashEntityEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLevelChangeEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.PotionMeta;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 
 import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.containers.OnlineTimeManager;
 import com.tehbeard.BeardStat.containers.OnlineTimeManager.ManagerRecord;
 import com.tehbeard.BeardStat.containers.PlayerStatManager;
 import com.tehbeard.BeardStat.listeners.defer.DelegateIncrement;
 import com.tehbeard.BeardStat.listeners.defer.DelegateSet;
 import com.tehbeard.BeardStat.utils.MetaDataCapture;
 
 /**
  * Calls the stat manager to trigger events
  * 
  * @author James
  * 
  */
 public class StatPlayerListener extends StatListener {
 
     public StatPlayerListener(List<String> worlds, PlayerStatManager playerStatManager, BeardStat plugin) {
         super(worlds, playerStatManager, plugin);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerAnimation(PlayerAnimationEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         String world = event.getPlayer().getWorld().getName();
         if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
             Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                     event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, world, "stats", "armswing", 1));
 
         }
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerJoin(final PlayerJoinEvent event) {
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, BeardStat.GLOBAL_WORLD, "stats", "login",
                 1));
         promiseblob.onResolve(new DelegateSet(BeardStat.DEFAULT_DOMAIN, BeardStat.GLOBAL_WORLD, "stats", "lastlogin",
                 (int) (System.currentTimeMillis() / 1000L)));
         promiseblob.onResolve(new Delegate<Void, Promise<EntityStatBlob>>() {
 
             @Override
             public <P extends Promise<EntityStatBlob>> Void invoke(P params) {
 
                 if (!params.getValue().hasStat(BeardStat.DEFAULT_DOMAIN, BeardStat.GLOBAL_WORLD, "stats", "firstlogin")) {
                     params.getValue().getStat(BeardStat.DEFAULT_DOMAIN, BeardStat.GLOBAL_WORLD, "stats", "firstlogin")
                             .setValue((int) (event.getPlayer().getFirstPlayed() / 1000L));
 
                 }
 
                 return null;
             }
         });
 
         OnlineTimeManager.setRecord(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         if ((event.isCancelled() == false) && !isBlacklistedWorld(event.getPlayer().getWorld())) {
             int len = event.getMessage().length();
             Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                     event.getPlayer().getName());
 
             String world = event.getPlayer().getWorld().getName();
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, world, "stats", "chatletters", len));
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, world, "stats", "chat", 1));
 
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerDropItem(PlayerDropItemEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         MetaDataCapture.saveMetaDataMaterialStat(
                 this.getPlayerStatManager().getPlayerBlobASync(event.getPlayer().getName()), BeardStat.DEFAULT_DOMAIN,
                 event.getPlayer().getWorld().getName(), "itemdrop", event.getItemDrop().getItemStack().getType(), event
                         .getItemDrop().getItemStack().getDurability(), event.getItemDrop().getItemStack().getAmount());
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerFish(PlayerFishEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld().getName(),
                 "stats", "fishcaught", 1));
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerKick(PlayerKickEvent event) {
         if (event.isCancelled() == false) {
             Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                     event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "stats", "kicks", 1));
             promiseblob.onResolve(new DelegateSet(BeardStat.DEFAULT_DOMAIN, BeardStat.GLOBAL_WORLD, "stats",
                     "lastlogout", (int) ((new Date()).getTime() / 1000L)));
 
             addTimeOnlineAndWipe(event.getPlayer().getName());
         }
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent event) {
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateSet(BeardStat.DEFAULT_DOMAIN, BeardStat.GLOBAL_WORLD, "stats", "lastlogout",
                 (int) ((new Date()).getTime() / 1000L)));
         addTimeOnlineAndWipe(event.getPlayer().getName());
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerMove(PlayerMoveEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         if (((event.getTo().getBlockX() != event.getFrom().getBlockX())
                 || (event.getTo().getBlockY() != event.getFrom().getBlockY()) || (event.getTo().getBlockZ() != event
                 .getFrom().getBlockZ()))) {
 
             Location from;
             Location to;
 
             from = event.getFrom();
             to = event.getTo();
 
             if (from.getWorld().equals(to.getWorld())) {
                 final double distance = from.distance(to);
                 if (distance < 8) {
                     Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                             event.getPlayer().getName());
 
                     promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, from.getWorld().getName(),
                             "stats", "move", (int) Math.ceil(distance)));
 
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         MetaDataCapture.saveMetaDataMaterialStat(
                 this.getPlayerStatManager().getPlayerBlobASync(event.getPlayer().getName()), BeardStat.DEFAULT_DOMAIN,
                 event.getPlayer().getWorld().getName(), "itempickup", event.getItem().getItemStack().getType(), event
                         .getItem().getItemStack().getDurability(), event.getItem().getItemStack().getAmount());
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerPortal(PlayerPortalEvent event) {
         if ((event.isCancelled() == false) && !isBlacklistedWorld(event.getPlayer().getWorld())) {
             Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                     event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "stats", "portal", 1));
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if ((event.isCancelled() == false) && !isBlacklistedWorld(event.getPlayer().getWorld())) {
             final TeleportCause teleportCause = event.getCause();
 
             Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                     event.getPlayer().getName());
             if (teleportCause == TeleportCause.ENDER_PEARL) {
                 promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                         .getName(), "itemuse", "enderpearl", 1));
             }
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "stats", "teleport", 1));
 
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerBucketFill(PlayerBucketFillEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld().getName(),
                 "stats", "fill" + event.getBucket().toString().toLowerCase().replace("_", ""), 1));
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld().getName(),
                 "stats", "empty" + event.getBucket().toString().toLowerCase().replace("_", ""), 1));
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Material material = event.getPlayer().getItemInHand().getType();
         Entity rightClicked = event.getRightClicked();
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
 
         if ((material == Material.BUCKET) && (rightClicked instanceof Cow)) {
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "interact", "milkcow", 1));
         }
 
         if ((material == Material.BOWL) && (rightClicked instanceof MushroomCow)) {
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "interact", "milkmushroomcow", 1));
         }
 
         if ((material == Material.INK_SACK) && (rightClicked instanceof Sheep)) {
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "dye", "total", 1));
 
             /**
              * if MetaDataable, make the item string correct
              */
 
             MetaDataCapture.saveMetaDataMaterialStat(promiseblob, BeardStat.DEFAULT_DOMAIN, event.getPlayer()
                     .getWorld().getName(), "dye", event.getPlayer().getItemInHand().getType(), event.getPlayer()
                     .getItemInHand().getDurability(), 1);
 
         }
 
         if ((material == Material.INK_SACK) && (rightClicked instanceof Wolf)) {
 
             // Check it's our wolf
             Wolf wolf = (Wolf) rightClicked;
             if (!wolf.isTamed()) {
                 return;
             }
             if (wolf.getOwner() != event.getPlayer()) {
                 return;
             }
 
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "wolfdye", "total", 1));
 
             /**
              * if MetaDataable, make the item string correct
              */
 
             MetaDataCapture.saveMetaDataMaterialStat(promiseblob, BeardStat.DEFAULT_DOMAIN, event.getPlayer()
                     .getWorld().getName(), "wolfdye", event.getPlayer().getItemInHand().getType(), event.getPlayer()
                     .getItemInHand().getDurability(), 1);
 
         }
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void shearEvent(PlayerShearEntityEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         if (event.getEntity() instanceof Sheep) {
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "sheared", "sheep", 1));
         }
 
         if (event.getEntity() instanceof MushroomCow) {
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "sheared", "mushroomcow", 1));
         }
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Action action = event.getAction();
         ItemStack item = event.getItem();
         Block clickedBlock = event.getClickedBlock();
         Result result = event.useItemInHand();
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
 
         if ((item != null) && (action != null) && (clickedBlock != null)) {
 
             if (result.equals(Result.DENY) == false) {
                 /*
                  * lighter sign tnt bucket waterbucket lavabucket cakeblock
                  */
                 if ((item.getType() == Material.FLINT_AND_STEEL) || (item.getType() == Material.FLINT_AND_STEEL)
                         || (item.getType() == Material.SIGN)) {
                     promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                             .getName(), "itemuse", item.getType().toString().toLowerCase().replace("_", ""), 1));
                 }
             }
             if ((clickedBlock.getType() == Material.CAKE_BLOCK)
                     || ((clickedBlock.getType() == Material.TNT) && (item.getType() == Material.FLINT_AND_STEEL))) {
                 promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                         .getName(), "itemuse", clickedBlock.getType().toString().toLowerCase().replace("_", ""), 1));
             }
             if (clickedBlock.getType().equals(Material.CHEST)) {
                 promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                         .getName(), "stats", "openchest", 1));
             }
             if (clickedBlock.getType().equals(Material.FLOWER_POT) && (action == Action.RIGHT_CLICK_BLOCK)
                     && (clickedBlock.getData() == 0)) {
                 Material[] m = { Material.RED_ROSE, Material.YELLOW_FLOWER, Material.SAPLING, Material.RED_MUSHROOM,
                         Material.BROWN_MUSHROOM, Material.CACTUS, Material.DEAD_BUSH };
                 for (Material mm : m) {
 
                     if (mm.equals(item.getType())) {
                         MetaDataCapture.saveMetaDataMaterialStat(promiseblob, BeardStat.DEFAULT_DOMAIN, event
                                 .getPlayer().getWorld().getName(), "plant", mm, item.getDurability(), 1);
                     }
                 }
 
             }
 
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerExp(PlayerExpChangeEvent event) {
         if (!shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Player player = event.getPlayer();
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld().getName(),
                 "exp", "lifetimexp", event.getAmount()));
         promiseblob.onResolve(new DelegateSet(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld().getName(), "exp",
                 "currentexp", player.getTotalExperience() + event.getAmount()));
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerExpLevel(PlayerLevelChangeEvent event) {
         if (!shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(
                 event.getPlayer().getName());
         promiseblob.onResolve(new DelegateSet(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld().getName(), "exp",
                 "currentlvl", event.getNewLevel()));
         int change = event.getNewLevel() - event.getOldLevel();
         if (change > 0) {
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getPlayer().getWorld()
                     .getName(), "exp", "lifetimelvl", change));
         }
 
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onEnchant(EnchantItemEvent event) {
         Player player = event.getEnchanter();
 
         if (event.isCancelled() || !shouldTrack(event.getEnchanter(), event.getEnchanter().getWorld())) {
             return;
         }
 
         if ((event.isCancelled() == false) && !isBlacklistedWorld(player.getWorld())) {
             Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(player.getName());
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getEnchanter().getWorld()
                     .getName(), "enchant", "total", 1));
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, event.getEnchanter().getWorld()
                     .getName(), "enchant", "totallvlspent", event.getExpLevelCost()));
         }
     }
 
     private void addTimeOnlineAndWipe(String player) {
 
         ManagerRecord timeRecord = OnlineTimeManager.getRecord(player);
         if (timeRecord == null) {
             return;
         }
         if (timeRecord.world == null) {
             return;
         }
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(player);
         promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, timeRecord.world, "stats", "playedfor",
                 timeRecord.sessionTime()));
         OnlineTimeManager.wipeRecord(player);
 
     }
 
     @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
     public void worldJump(PlayerChangedWorldEvent event) {
         addTimeOnlineAndWipe(event.getPlayer().getName());
         OnlineTimeManager.setRecord(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onNom(PlayerItemConsumeEvent event) {
         Player player = event.getPlayer();
 
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
 
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(player.getName());
 
         if (event.getItem().getType().isEdible()) {
             String stat = "food" + event.getItem().getType().toString().toLowerCase().replaceAll("_", "");
             promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, player.getWorld().getName(),
                     "consume", stat, 1));
             return;
 
         }
         if (event.getItem().getType() == Material.POTION) {
 
             // process meta potions
             PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
             List<String> stats = new ArrayList<String>();
             if (meta != null) {
                 for (PotionEffect effect : meta.getCustomEffects()) {
                     stats.add(effect.getType().getName().toLowerCase().replaceAll("_", "") + effect.getAmplifier());
                 }
             }
 
             // processs base effect if nessecary
             if (stats.size() == 0) {
                 Collection<PotionEffect> potion = Potion.getBrewer().getEffectsFromDamage(
                         event.getItem().getDurability());
 
                 for (PotionEffect pe : potion) {
                     pe.getType().getName().toLowerCase().replaceAll("_", "");
 
                 }
             }
             for (String s : stats) {
                 promiseblob.onResolve(new DelegateIncrement(BeardStat.DEFAULT_DOMAIN, player.getWorld().getName(),
                         "consume", "potion" + s, 1));
             }
 
         }
 
     }
     
     @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled=true)
     public void onLeash(PlayerLeashEntityEvent event){
         if (event.isCancelled() || !shouldTrack(event.getPlayer(), event.getPlayer().getWorld())) {
             return;
         }
         Player player = event.getPlayer();
         Promise<EntityStatBlob> promiseblob = this.getPlayerStatManager().getPlayerBlobASync(player.getName());
 
         MetaDataCapture.saveMetaDataEntityStat(
                 promiseblob, 
                 BeardStat.DEFAULT_DOMAIN, 
                 player.getWorld().getName(), 
                 "leash", 
                 event.getEntity(), 
                 1
                 );
     }
 }
