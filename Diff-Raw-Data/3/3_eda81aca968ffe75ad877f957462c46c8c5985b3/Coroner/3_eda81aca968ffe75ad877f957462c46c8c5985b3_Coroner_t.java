 package edgruberman.bukkit.obituaries;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityCombustByBlockEvent;
 import org.bukkit.event.entity.EntityCombustByEntityEvent;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 /** tracks damages for a specific player to report on death */
 public class Coroner implements Listener {
 
     private static final double PLAYER_WIDTH = 0.6D;
     private static final double PLAYER_HEIGHT = 1.8D;
     private static final double ENTITY_NON_FLAMMABLE = 0.001D;
 
     private static final int EXPIRATION_LIVING = 300; // 15s
     private static final int EXPIRATION_ENVIRONMENT = 100; // 5s
 
     private final UUID playerId;
     private final List<Damage> damages = new ArrayList<Damage>();
 
     private int consumptionOccurred = -1;
     private ItemStack consumed = null;
 
     private String combusterAsKiller = null;
     private Object combuster = null;
 
     Coroner(final Player player) {
         this.playerId = player.getUniqueId();
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) // give everything else a chance to finalize what really happened
     public void onPlayerDamaged(final EntityDamageEvent event) {
         if (!(event.getEntity().getUniqueId().equals(this.playerId))) return;
         if (event.getEntity().isDead()) return;
 
         final Player player = (Player) event.getEntity();
         if (player.getGameMode() == GameMode.CREATIVE) return;
 
         // TODO submit ticket and fix why ENTITY_ATTACK is thrown right after ENTITY&&BLOCK_EXPLOSION
         if (event.getCause() == DamageCause.ENTITY_ATTACK) {
             final Damage last = this.getLastDamage();
             if (last != null) {
                 if (last.getCause() == DamageCause.BLOCK_EXPLOSION || last.getCause() == DamageCause.ENTITY_EXPLOSION) return;
             }
         }
 
         this.clearOldCombat();
 
         try {
             final Damage damage = Damage.create(this, event);
             this.damages.add(damage);
         } catch (final Exception e) {
             e.printStackTrace();
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onConsumption(final PlayerItemConsumeEvent consume) {
         if (!(consume.getPlayer().getUniqueId().equals(this.playerId))) return;
         this.consumed = consume.getItem();
         this.consumptionOccurred = consume.getPlayer().getTicksLived();
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onCombustion(final EntityCombustEvent combustion) {
         if (!(combustion.getEntity().getUniqueId().equals(this.playerId))) return;
 
         if (combustion instanceof EntityCombustByEntityEvent) {
             // combustible PROJECTILE (fireball), lightning, fire enchanted item (sword), a burning entity (zombie, arrow)
 
             final EntityCombustByEntityEvent combustByEntity = (EntityCombustByEntityEvent) combustion;
             final Entity combuster = combustByEntity.getCombuster();
             this.combusterAsKiller = Attack.describeEntity(combuster);
 
             if (combuster instanceof Projectile) {
                 final LivingEntity shooter = ((Projectile) combuster).getShooter();
                 this.combuster = ( shooter != null ? shooter.getUniqueId() : null );
             } else {
                 this.combuster = combuster.getUniqueId();
             }
 
 
             if (combuster instanceof LivingEntity) {
                 final LivingEntity living = (LivingEntity) combuster;
                 final ItemStack weapon = living.getEquipment().getItemInHand();
                 if (weapon != null && weapon.getTypeId() != Material.AIR.getId()) this.combusterAsKiller = Main.courier.format("weapon.format", this.combusterAsKiller, Translator.formatItem(weapon));
             }
 
             return;
         }
 
         if (combustion instanceof EntityCombustByBlockEvent) {
             // so far, only LAVA
 
             final EntityCombustByBlockEvent combustByBlock = (EntityCombustByBlockEvent) combustion;
             final Block combuster = combustByBlock.getCombuster();
 
             if (combuster == null) {
                this.combuster = null;
                 this.combusterAsKiller = Translator.describeMaterial(Material.LAVA);
                 return;
             }
 
            this.combuster = combuster.getState();
             this.combusterAsKiller = Translator.describeMaterial(combuster);
             return;
         }
 
         // simple EntityCombustEvent for fire or lava
         final Material source = Coroner.combustionSource(combustion.getEntity());
         this.combusterAsKiller = ( source != null ? Translator.describeMaterial(source) : null );
     }
 
     /** @return first combustion source found within vanilla logic of determination */
     private static Material combustionSource(final Entity entity) {
         final Location location = entity.getLocation();
 
         // Entity.move shrinks bounding box to determine if in contact "enough" with combustion source
         final int minX = (int) Math.floor(location.getX() - (Coroner.PLAYER_WIDTH / 2D) + Coroner.ENTITY_NON_FLAMMABLE);
         final int maxX = (int) Math.floor(location.getX() + (Coroner.PLAYER_WIDTH / 2D) - Coroner.ENTITY_NON_FLAMMABLE);
         final int minY = (int) Math.floor(location.getY() - (Coroner.PLAYER_HEIGHT / 2D) + Coroner.ENTITY_NON_FLAMMABLE);
         final int maxY = (int) Math.floor(location.getY() + (Coroner.PLAYER_HEIGHT / 2D) - Coroner.ENTITY_NON_FLAMMABLE);
         final int minZ = (int) Math.floor(location.getZ() - (Coroner.PLAYER_WIDTH / 2D) + Coroner.ENTITY_NON_FLAMMABLE);
         final int maxZ = (int) Math.floor(location.getZ() + (Coroner.PLAYER_WIDTH / 2D) - Coroner.ENTITY_NON_FLAMMABLE);
 
         for (int x = minX; x <= maxX; ++x) {
             for (int y = minY; y <= maxY; ++y) {
                 for (int z = minZ; z <= maxZ; ++z) {
                     final Material source = Material.getMaterial(entity.getWorld().getBlockTypeIdAt(x, y, z));
                     if (source == Material.FIRE || source == Material.LAVA || source == Material.STATIONARY_LAVA) return source;
                 }
             }
         }
 
         return null;
     }
 
     @EventHandler
     public void onPlayerDeath(final PlayerDeathEvent death) {
         if (!death.getEntity().getUniqueId().equals(this.playerId)) return;
         final String message = this.getLastDamage().formatDeath();
 
         // forcibly clear old combat
         this.damages.clear();
         this.combusterAsKiller = null;
 
         if (message == null) return; // allow default death message if no format specified
 
         death.setDeathMessage(message);
     }
 
     @EventHandler
     public void onPlayerQuit(final PlayerQuitEvent quit) {
         if (!quit.getPlayer().getUniqueId().equals(this.playerId)) return;
         HandlerList.unregisterAll(this);
     }
 
     private void clearOldCombat() {
         if (this.damages.isEmpty()) return;
 
         final Damage last = this.getLastDamage();
         final int expiration = ( last.isDamagerLiving() ? Coroner.EXPIRATION_LIVING : Coroner.EXPIRATION_ENVIRONMENT );
 
         final int age = this.getPlayer().getTicksLived() - last.getRecorded();
         if (age < expiration) return;
 
         this.damages.clear();
         this.combusterAsKiller = null;
     }
 
     public Damage getLastDamage() {
         if (this.damages.isEmpty()) return null;
         return this.damages.get(this.damages.size() - 1);
     }
 
     public List<Damage> getDamages() {
         return this.damages;
     }
 
     public Player getPlayer() {
         for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
             if (player.getUniqueId().equals(this.playerId)) return player;
         }
         return null;
     }
 
     public UUID getPlayerId() {
         return this.playerId;
     }
 
     // TODO theoretically some other entity carrying some applied effect could occur at same tick as consumption does
     public ItemStack getConsumed(final int ticks) {
         return this.consumptionOccurred == ticks ? this.consumed : null;
     }
 
     public String getCombusterAsKiller() {
         return this.combusterAsKiller;
     }
 
     public Object getCombuster() {
         return this.combuster;
     }
 
 }
