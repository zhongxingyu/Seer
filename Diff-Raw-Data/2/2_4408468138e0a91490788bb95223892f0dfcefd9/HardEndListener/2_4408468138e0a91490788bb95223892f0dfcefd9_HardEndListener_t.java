 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener
  * Created: 2013/02/10 4:06:31
  */
 package net.syamn.sakuracmd.listener;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.manager.Worlds;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.Power;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 /**
  * HardEndListener (HardEndListener.java)
  * @author syam(syamn)
  */
 public class HardEndListener implements Listener{
     private SakuraCmd plugin;
     public HardEndListener (final SakuraCmd plugin){
         this.plugin = plugin;
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamage(final EntityDamageEvent event) {
         final Entity ent = event.getEntity();
         
         if (!ent.getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         
         // ドラゴンへのダメージ
         if (ent.getType() == EntityType.ENDER_DRAGON || ent.getType() == EntityType.COMPLEX_PART) {
             if (event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION) {
                 event.setCancelled(true);
                 event.setDamage(0); // 爆発ダメージ無視
             }else{
                 event.setDamage(event.getDamage() / 3); // ダメージ1/3
             }
         }
         
         // ドラゴンがダメージを受けた
         if (ent.getType() == EntityType.ENDER_DRAGON) {
             final Location dragonLocation = ent.getLocation();
             
             final List<Player> inWorldPlayers= new ArrayList<Player>();
             for (final Player p : ent.getWorld().getPlayers()){
                 if (!PlayerManager.getPlayer(p).hasPower(Power.INVISIBLE)){
                     inWorldPlayers.add(p);
                 }
             }
             
             // 毒グモ3匹ランダムターゲットで召還
             for (short i = 0; i < 6; i++) {
                 CaveSpider caveSpider = (CaveSpider) ent.getWorld().spawnEntity(dragonLocation, EntityType.CAVE_SPIDER);
                 caveSpider.setNoDamageTicks(200);
             }
             
             // ガスト3匹ランダムターゲットで召還
             for (short i = 0; i < 4; i++) {
                 Ghast ghast = (Ghast) ent.getWorld().spawnEntity(dragonLocation, EntityType.GHAST);
                 ghast.setNoDamageTicks(200);
             }
             
             // ゾンビ5匹ランダムターゲットで召還
             for (short i = 0; i < 6; i++) {
                 Zombie zombie = (Zombie) ent.getWorld().spawnEntity(dragonLocation, EntityType.ZOMBIE);
                 zombie.setNoDamageTicks(200);
             }
             
             // 帯電クリーパー3匹召還
             for (short i = 0; i < 6; i++) {
                 Creeper creeper = (Creeper) ent.getWorld().spawnEntity(dragonLocation, EntityType.CREEPER);
                 creeper.setNoDamageTicks(200);
                 creeper.setPowered(true);
             }
             
             // ランダムプレイヤーの真上にTNTをスポーン
             for (short i = 0; i < 20; i++) {
                 Random rnd = new Random(); // 乱数宣言
                 if (inWorldPlayers.size() < 1) return;
                 Location targetLoc = inWorldPlayers.get(rnd.nextInt(inWorldPlayers.size())).getLocation(); // ターゲットプレイヤー確定と座標取得
                 Location tntloc = new Location(targetLoc.getWorld(), targetLoc.getX(), dragonLocation.getY(), targetLoc.getZ());
                 ent.getWorld().spawn(tntloc, TNTPrimed.class);
             }
         }
         
         // TNT -> MOBダメージ無効
         if (ent != null && (event.getCause().equals(DamageCause.ENTITY_EXPLOSION) || event.getCause().equals(DamageCause.BLOCK_EXPLOSION))){
             if ((ent instanceof LivingEntity) && !(ent instanceof Player)){
                 event.setDamage(0);
                 event.setCancelled(true);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void voidFireDamageToMonsters(final EntityDamageEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         final Entity ent = event.getEntity();
         if ((ent instanceof LivingEntity) && !(ent instanceof Player)){
             if (DamageCause.FIRE.equals(event.getCause()) || DamageCause.FIRE_TICK.equals(event.getCause())){
                 event.setDamage(0);
                 event.setCancelled(true);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         
         final Entity ent = event.getEntity();
         final Entity attacker = event.getDamager();
         
         // エンダークリスタルが矢によってダメージを受けた
         if (ent.getType() == EntityType.ENDER_CRYSTAL) {
             switch(attacker.getType()){
                 case ARROW:
                 case PRIMED_TNT:
                     event.setDamage(0);
                     event.setCancelled(true);
                     break;
             }
             
             if (attacker.getType() == EntityType.ARROW){
                 final Projectile arrow = (Arrow) attacker;
                 if (arrow.getShooter() instanceof Player) {
                     Util.message((Player) arrow.getShooter(), "&c矢ではクリスタルを破壊できません！");
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void addPlayerDamage(final EntityDamageByEntityEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         if (event.getEntity() instanceof Player){
             event.setDamage(event.getDamage() + 8);
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onProjectileHit(final ProjectileHitEvent event) {
         if (event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
            if (EntityType.ARROW.equals(event.getEntityType()) && (((Arrow) event.getEntity()).getShooter().getType() == EntityType.SKELETON)) {
                 event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), (float) 3.0, true);
                 event.getEntity().remove(); // 規模1.0の炎有りの爆発をスケルトンの弓に与える
             }
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerInteract(final PlayerInteractEvent event) {
         if (event.getPlayer().getWorld().getName().equals(Worlds.hard_end)){
             if (event.getClickedBlock().getType() == Material.BED_BLOCK && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 event.setCancelled(true);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onItemSpawn(final ItemSpawnEvent event) {
         final Item item = event.getEntity();
         if (item.getWorld().getName().equals(Worlds.hard_end) && item.getItemStack().getType() == Material.ENDER_STONE) {
             event.setCancelled(true); // 負荷対策
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
         if (event.getPlayer().getWorld().getName().equals(Worlds.hard_end)) {
             event.getPlayer().setNoDamageTicks(200); // ハードエンドに移動したら10秒間無敵
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onExplosionPrime(final ExplosionPrimeEvent event) { // 爆発時の威力を高める
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
             
         // デフォルト: CREEPER:3.0 / CHARGED_CREEPER:6.0 / PRIMED_TNT:4.0 / FIREBALL:1.0(Fire:true)
         switch (event.getEntityType()) {
             case CREEPER: // クリーパー
                 event.setRadius((float) 9.0);
                 break;
             /*
              * TODO:Breaking 1.4.2 case FIREBALL: // ガストの火の玉
              * event.setRadius((float) 3.0); event.setFire(true); break;
              */
             case PRIMED_TNT: // TNT
                 event.setRadius((float) 7.0);
                 event.setFire(true);
                 break;
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityExplode(final EntityExplodeEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         
         if (EntityType.PRIMED_TNT.equals(event.getEntityType())) {
             final Location baseLoc = event.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getLocation();
             
             // 基準座標を元に 3x3 まで走査する
             Block block;
             for (int x = baseLoc.getBlockX() - 1; x <= baseLoc.getBlockX() + 1; x++) {
                 for (int z = baseLoc.getBlockZ() - 1; z <= baseLoc.getBlockZ() + 1; z++) {
                     for (int y = baseLoc.getBlockY() - 1; y <= baseLoc.getBlockY() + 1; y++) {
                         block = baseLoc.getWorld().getBlockAt(x, y, z);
                         if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                             block.setType(Material.AIR);
                         }
                     }
                 }
             }
         }
     }
 }
