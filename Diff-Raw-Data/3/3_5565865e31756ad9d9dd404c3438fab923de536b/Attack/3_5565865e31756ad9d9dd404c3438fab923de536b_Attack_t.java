 package edgruberman.bukkit.obituaries;
 
 import java.util.UUID;
 
 import org.bukkit.Material;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 
 /** damage from entity source */
 public class Attack extends Damage {
 
     protected final Entity damager;
     protected final ItemStack weapon;
 
     public Attack(final Coroner coroner, final EntityDamageEvent damage) {
         super(coroner, damage);
 
         if (!(damage instanceof EntityDamageByEntityEvent)) {
             this.damager = null;
             this.weapon = null;
             return;
         }
 
         // death event could be thrown after source item is no longer in damager's hand
         final EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) damage;
         this.damager = edbee.getDamager();
         if (this.damager instanceof HumanEntity) {
             final HumanEntity damagerPlayer = (HumanEntity) this.damager;
             this.weapon = damagerPlayer.getItemInHand().clone();
         } else {
             this.weapon = null;
         }
     }
 
     @Override
     public boolean isDamagerLiving() {
         return this.damager instanceof LivingEntity;
     }
 
     @Override
     public String describeAsKiller() {
         final String damager = Attack.describeEntity(this.damager);
 
         String weapon = null;
         if (this.weapon == null || this.weapon.getType() == Material.AIR) {
             weapon = Main.courier.format("weapon.defaults." + this.damager.getType().name());
         } else {
             weapon = Translator.formatItem(this.weapon);
         }
 
         final String result = Main.courier.format("weapon.format", damager, weapon);
         return ( result != null && weapon != null ? result : damager );
     }
 
     /** exclude weapon information, use projectile shooter */
     @Override
     public String describeAsAccomplice() {
         if (this.damager instanceof Projectile) {
             final LivingEntity shooter = ((Projectile) this.damager).getShooter();
             return Translator.describeEntity(shooter);
         }
 
         return Translator.describeEntity(this.damager);
     }
 
     @Override
     public UUID getDamager() {
         if (this.damager instanceof Projectile) {
             final Projectile projectile = (Projectile) this.damager;
            final LivingEntity shooter = projectile.getShooter();
            return ( shooter != null ? shooter.getUniqueId() : null );
         }
 
         return ( this.damager != null ? this.damager.getUniqueId() : null );
     }
 
 
 
     /**
      * Describes an entity under the context of being damaged by it. Players will
      * use their display names. Other entities will default to their Bukkit
      * class name if a config.yml localized name does not match. Projectiles,
      * Tameables, and Vehicles will include descriptions of their owners if
      * a format in the language file is specified.
      *
      * Examples:
      *   Player = EdGruberman
      *   Arrow = EdGruberman with an arrow
      *   Fireball = a ghast with a fireball
      */
     public static String describeEntity(final Entity entity) {
         String description = Translator.describeEntity(entity);
 
         if (entity instanceof Tameable) {
             final AnimalTamer tamer = ((Tameable) entity).getOwner();
             if (tamer instanceof Entity) {
                 description = Main.courier.format("owners.Tameable", description, Attack.describeEntity((Entity) tamer));
             }
         }
 
         if (entity instanceof Projectile) {
             final LivingEntity shooter = ((Projectile) entity).getShooter();
             String shooterName = null;
             if (shooter == null) {
                 shooterName = Translator.describeMaterial(Material.DISPENSER);
             } else if (shooter instanceof Entity) {
                 shooterName = Attack.describeEntity(shooter);
             }
             description = Main.courier.format("owners.Projectile", description, shooterName);
         }
 
         // Vehicle
         if (!entity.isEmpty()) {
             description = Main.courier.format("owners.Vehicle", description, Attack.describeEntity(entity.getPassenger()));
         }
 
         return description;
     }
 
 }
