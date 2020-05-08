 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillFirearrow extends ActiveSkill {
 
     public SkillFirearrow(Heroes plugin) {
         super(plugin);
         setName("Firearrow");
         setDescription("Shoots a burning arrow");
         setUsage("/skill firearrow");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill firearrow");
 
        registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage", 4);
         node.setProperty("fire-ticks", 100);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
 
         Arrow arrow = player.shootArrow();
         arrow.setFireTicks(1000);
 
         broadcastExecuteText(hero);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled()) return;
             if (event instanceof EntityDamageByProjectileEvent) {
                 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
                 Entity projectile = subEvent.getProjectile();
                 if (projectile instanceof Arrow) {
                     if (projectile.getFireTicks() > 0) {
                         Entity entity = subEvent.getEntity();
                         if (entity instanceof LivingEntity) {
                             Entity dmger = subEvent.getDamager();
                             if (dmger instanceof Player) {
                                 Hero hero = plugin.getHeroManager().getHero((Player) dmger);
                                 HeroClass heroClass = hero.getHeroClass();
                                 // Perform a check to see if any plugin is preventing us from damaging the player.
                                 EntityDamageEvent damageEvent = new EntityDamageEvent(dmger, DamageCause.CUSTOM, 0);
                                 Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                                 if (damageEvent.isCancelled()) return;
                                 // Damage the player and ignite them.
                                 LivingEntity livingEntity = (LivingEntity) entity;
                                livingEntity.setFireTicks(getSetting(heroClass, "fire-ticks", 100));
 
                                 plugin.getDamageManager().addSpellTarget((Entity) entity);
                                 int damage = getSetting(heroClass, "damage", 4);
                                event.setDamage(damage);
                             }
                         }
                     }
                 }
             }
         }
 
     }
 
 }
