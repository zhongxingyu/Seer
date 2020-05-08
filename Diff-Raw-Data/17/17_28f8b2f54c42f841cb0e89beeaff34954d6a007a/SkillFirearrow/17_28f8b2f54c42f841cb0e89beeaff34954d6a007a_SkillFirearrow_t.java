 package com.herocraftonline.dev.heroes.skill.skills;
 
 import net.minecraft.server.MathHelper;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillFirearrow extends ActiveSkill {
 
     public SkillFirearrow(Heroes plugin) {
         super(plugin);
         name = "Firearrow";
         description = "Fires a firearrow";
         usage = "/skill firearrow";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill firearrow");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Monitor);
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Location location = player.getEyeLocation();
 
         float pitch = location.getPitch() / 180.0F * 3.1415927F;
         float yaw = location.getYaw() / 180.0F * 3.1415927F;
 
         double motX = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
         double motZ = MathHelper.cos(yaw) * MathHelper.cos(pitch);
         double motY = -MathHelper.sin(pitch);
         Vector velocity = new Vector(motX, motY, motZ);
 
         Arrow snowball = player.shootArrow();
         snowball.setFireTicks(1000);
         snowball.setVelocity(velocity);
 
         notifyNearbyPlayers(location, useText, hero.getPlayer().getName(), name);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled()) {
                 return;
             }
             if (event instanceof EntityDamageByProjectileEvent) {
                 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
                 Entity projectile = subEvent.getProjectile();
                 if (projectile instanceof Snowball) {
                     if (projectile.getFireTicks() > 0) {
                         Entity entity = subEvent.getEntity();
                         if (entity instanceof LivingEntity) {
                             Entity dmger = subEvent.getDamager();
                             if (dmger instanceof Player) {
                                 Hero hero = plugin.getHeroManager().getHero((Player) dmger);
                                 HeroClass heroClass = hero.getHeroClass();
                                 // Perform a check to see if any plugin is preventing us from damaging the player.
                                 EntityDamageEvent damageEvent = new EntityDamageEvent(dmger, subEvent.getCause(), 4);
                                 Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                                 if (damageEvent.isCancelled()) {
                                     return;
                                 }
                                 // Damage the player and ignite them.
                                 LivingEntity livingEntity = (LivingEntity) entity;
                                livingEntity.setFireTicks(20);
                                 livingEntity.damage(4);
                             }
                         }
                     }
                 }
             }
         }
 
     }
 
 }
