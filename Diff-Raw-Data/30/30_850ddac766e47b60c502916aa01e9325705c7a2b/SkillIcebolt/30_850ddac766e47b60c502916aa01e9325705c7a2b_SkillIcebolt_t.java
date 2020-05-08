 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 
 import net.minecraft.server.MathHelper;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.Vector;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillIcebolt extends ActiveSkill {
     private HashSet<Snowball> snowballs = new HashSet<Snowball>();
 
     public SkillIcebolt(Heroes plugin) {
         super(plugin);
         setName("Icebolt");
        setDescription("Fires a snowball that hurts the player and if they're on fire, puts them out");
         setUsage("/skill icebolt");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill icebolt");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
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
 
         Snowball snowball = player.throwSnowball();
         snowball.setVelocity(velocity);
         snowballs.add(snowball);
 
         notifyNearbyPlayers(location, getUseText(), hero.getPlayer().getName(), getName());
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
                     if (snowballs.contains((Snowball) projectile)) {
                         snowballs.remove((Snowball) projectile);
                         // Damage Event //
                         EntityDamageEvent damageEvent = new EntityDamageEvent(event.getEntity(), DamageCause.ENTITY_ATTACK, 0);
                         Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                         if (damageEvent.isCancelled()) {
                             return;
                         }
                         LivingEntity lEntity = (LivingEntity) event.getEntity();
                         event.getEntity().setFireTicks(0);
                         lEntity.damage(3);
                     }
                 }
             }
         }
 
     }
 
 }
