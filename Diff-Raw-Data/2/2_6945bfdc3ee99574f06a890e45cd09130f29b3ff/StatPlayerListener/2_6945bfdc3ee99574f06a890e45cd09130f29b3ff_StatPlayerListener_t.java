 package me.tehbeard.BeardStat.listeners;
 
 import java.util.Date;
 import java.util.List;
 
 import me.tehbeard.BeardStat.BeardStat;
 
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
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
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Calls the stat manager to trigger events
  * @author James
  *
  */
 public class StatPlayerListener implements Listener {
 
     List<String> worlds;
     private PlayerStatManager playerStatManager;
 
     public StatPlayerListener(List<String> worlds,PlayerStatManager playerStatManager){
         this.worlds = worlds;
         this.playerStatManager = playerStatManager;
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerAnimation(PlayerAnimationEvent event) {
         if(event.getAnimationType()==PlayerAnimationType.ARM_SWING){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","armswing",1));
 
         }
 
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerJoin(final PlayerJoinEvent event) {
         Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
         promiseblob.onResolve(new DelegateIncrement("stats","login",1));
        //promiseblob.onResolve(new DelegateSet("stats","login",(int)(System.currentTimeMillis()/1000L)));
         promiseblob.onResolve(new Delegate<Void, Promise<PlayerStatBlob>>() {
 
             public <P extends Promise<PlayerStatBlob>> Void invoke(P params) {
                 if(!params.getValue().hasStat("stats", "firstlogin")){
                     params.getValue().getStat("stats","firstlogin").setValue((int)(event.getPlayer().getFirstPlayed()/1000L));    
                 }
                 return null;
             }
         });
 
 
         BeardStat.self().getStatManager().setLoginTime(event.getPlayer().getName(), System.currentTimeMillis());
 
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerChat(AsyncPlayerChatEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             int len = event.getMessage().length();
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","chatletters",len));
             promiseblob.onResolve(new DelegateIncrement("stats","chat",1));
 
 
 
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerDropItem(PlayerDropItemEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 
             MetaDataCapture.saveMetaDataMaterialStat(playerStatManager.getPlayerBlobASync(event.getPlayer().getName()), 
                     "itemdrop", 
                     event.getItemDrop().getItemStack().getType(), 
                     event.getItemDrop().getItemStack().getDurability(), 
                     event.getItemDrop().getItemStack().getAmount());
 
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerFish(PlayerFishEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","fishcaught",1));
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerKick(PlayerKickEvent event){
         if(event.isCancelled()==false){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","kicks",1));
             promiseblob.onResolve(new DelegateSet("stats","lastlogout",(int)((new Date()).getTime()/1000L)));
 
             calc_timeonline_and_wipe(event.getPlayer().getName());
         }
 
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent event) {
         Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
         promiseblob.onResolve(new DelegateSet("stats","lastlogout",(int)((new Date()).getTime()/1000L)));
         calc_timeonline_and_wipe(event.getPlayer().getName());
 
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerMove(PlayerMoveEvent event) {
         if(event.isCancelled()==false &&
                 (event.getTo().getBlockX() != event.getFrom().getBlockX() || 
                 event.getTo().getBlockY() != event.getFrom().getBlockY() || 
                 event.getTo().getBlockZ() != event.getFrom().getBlockZ() )&& 
                 !worlds.contains(event.getPlayer().getWorld().getName())){
 
             Location from;
             Location to;
 
             from = event.getFrom();
             to = event.getTo();
 
             if(from.getWorld().equals(to.getWorld())){
                 final double distance = from.distance(to);
                 if(distance < 8){
                     Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
                     promiseblob.onResolve(new DelegateIncrement("stats","move",(int)Math.ceil(distance)));
 
 
                 }
             }
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 
             MetaDataCapture.saveMetaDataMaterialStat(playerStatManager.getPlayerBlobASync(event.getPlayer().getName()), 
                     "itempickup", 
                     event.getItem().getItemStack().getType(), 
                     event.getItem().getItemStack().getDurability(), 
                     event.getItem().getItemStack().getAmount());
 
 
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerPortal(PlayerPortalEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","portal",1));
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerTeleport(PlayerTeleportEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             final TeleportCause teleportCause = event.getCause();
 
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             if(teleportCause == TeleportCause.ENDER_PEARL){
                 promiseblob.onResolve(new DelegateIncrement("itemuse","enderpearl",1));
             }
             promiseblob.onResolve(new DelegateIncrement("stats","teleport",1));
 
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerBucketFill(PlayerBucketFillEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","fill"+ event.getBucket().toString().toLowerCase().replace("_",""),1));
 
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("stats","empty"+ event.getBucket().toString().toLowerCase().replace("_",""),1));
 
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 
 
             Material material = event.getPlayer().getItemInHand().getType();
             Entity rightClicked = event.getRightClicked();
 
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
 
 
             if(material == Material.BUCKET && rightClicked instanceof Cow){
                 promiseblob.onResolve(new DelegateIncrement("interact", "milkcow",1));
             }
 
             if(material == Material.BOWL && rightClicked instanceof MushroomCow){
                 promiseblob.onResolve(new DelegateIncrement("interact", "milkmushroomcow",1));
             }
 
             if(material == Material.INK_SACK && rightClicked instanceof Sheep){
                 promiseblob.onResolve(new DelegateIncrement("dye", "total",1));
 
                 /**
                  * if MetaDataable, make the item string correct
                  */
 
                 MetaDataCapture.saveMetaDataMaterialStat(promiseblob, 
                         "dye", 
                         event.getPlayer().getItemInHand().getType(), 
                         event.getPlayer().getItemInHand().getDurability(), 
                         1);
 
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void shearEvent(PlayerShearEntityEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             if(event.getEntity() instanceof Sheep){
                 promiseblob.onResolve(new DelegateIncrement("sheared", "sheep",1));
             }
 
             if(event.getEntity() instanceof MushroomCow){
                 promiseblob.onResolve(new DelegateIncrement("sheared", "mushroomcow",1));
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerInteract(PlayerInteractEvent event){
 
         if(event.getClickedBlock()!=null){
             if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
                 Action action = event.getAction();
                 ItemStack item = event.getItem();
                 Block clickedBlock = event.getClickedBlock();
                 Result result = event.useItemInHand();
 
                 Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
 
                 if(item !=null &&
                         action!=null &&
                         clickedBlock!=null){
 
                     if(result.equals(Result.DENY)==false){
                         /*lighter
 							  sign
 							  tnt
 							  bucket
 							  waterbucket
 							  lavabucket
 							  cakeblock*/
                         if(item.getType()==Material.FLINT_AND_STEEL ||
                                 item.getType()==Material.FLINT_AND_STEEL ||
                                 item.getType()==Material.SIGN 
                                 ){
                             promiseblob.onResolve(new DelegateIncrement("itemuse",item.getType().toString().toLowerCase().replace("_",""),1));
                         }
                     }
                     if(clickedBlock.getType() == Material.CAKE_BLOCK||
                             (clickedBlock.getType() == Material.TNT && item.getType()==Material.FLINT_AND_STEEL)){
                         promiseblob.onResolve(new DelegateIncrement("itemuse",clickedBlock.getType().toString().toLowerCase().replace("_",""),1));
                     }
                     if(clickedBlock.getType().equals(Material.CHEST)){
                         promiseblob.onResolve(new DelegateIncrement("stats","openchest",1));
                     }
 
 
                 }
 
 
 
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerExp(PlayerExpChangeEvent event){
         if(!worlds.contains(event.getPlayer().getWorld().getName())){
             Player player = event.getPlayer();
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateIncrement("exp","lifetimexp",event.getAmount()));
             promiseblob.onResolve(new DelegateSet("exp","currentexp",player.getTotalExperience() + event.getAmount()));
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerExpLevel(PlayerLevelChangeEvent event){
         if(!worlds.contains(event.getPlayer().getWorld().getName())){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(event.getPlayer().getName());
             promiseblob.onResolve(new DelegateSet("exp","currentlvl",event.getNewLevel()));
             int change = event.getNewLevel() - event.getOldLevel();
             if(change > 0){
                 promiseblob.onResolve(new DelegateIncrement("exp","lifetimelvl",change));
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEnchant(EnchantItemEvent event){
 
         Player player = event.getEnchanter();
 
         if(event.isCancelled()==false && !worlds.contains(player.getWorld().getName())){
             Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(player.getName());
             promiseblob.onResolve(new DelegateIncrement("enchant","total",1));
             promiseblob.onResolve(new DelegateIncrement("enchant","totallvlspent",event.getExpLevelCost()));
         }
     }
 
     private void calc_timeonline_and_wipe(String player){
 
         int seconds = BeardStat.self().getStatManager().getSessionTime(player);
         Promise<PlayerStatBlob> promiseblob = playerStatManager.getPlayerBlobASync(player);
         promiseblob.onResolve(new DelegateIncrement("stats","playedfor",seconds));
         BeardStat.self().getStatManager().wipeLoginTime(player);		
 
     }
 
 
 
 
 
 }
