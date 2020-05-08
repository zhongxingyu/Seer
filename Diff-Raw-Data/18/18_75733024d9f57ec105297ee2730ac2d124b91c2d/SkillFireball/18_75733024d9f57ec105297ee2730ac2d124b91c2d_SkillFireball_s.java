 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.common.CombustEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillFireball extends ActiveSkill {
 
     public SkillFireball(Heroes plugin) {
         super(plugin, "Fireball");
         setDescription("You shoot a ball of fire that deals $1 damage and lights your target on fire");
         setUsage("/skill fireball");
         setArgumentRange(0, 0);
         setIdentifiers("skill fireball");
         setTypes(SkillType.FIRE, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
         Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.DAMAGE.node(), 4);
         node.set("fire-ticks", 100);
         return node;
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Snowball snowball = player.throwSnowball();
         snowball.setFireTicks(1000);
         broadcastExecuteText(hero);
         return SkillResult.NORMAL;
     }
 
     public class SkillEntityListener implements Listener {
 
         private final Skill skill;
 
         public SkillEntityListener(Skill skill) {
             this.skill = skill;
         }
 
         @EventHandler()
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                 return;
             }
 
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             Entity projectile = subEvent.getDamager();
             if (!(projectile instanceof Snowball) || projectile.getFireTicks() < 1) {
                 return;
             }
 
             LivingEntity entity = (LivingEntity) subEvent.getEntity();
             Entity dmger = ((Snowball) projectile).getShooter();
             if (dmger instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) dmger);
 
                 if (!damageCheck((Player) dmger, entity)) {
                     event.setCancelled(true);
                     return;
                 }
 
                 // Damage the player and ignite them.
                 entity.setFireTicks(SkillConfigManager.getUseSetting(hero, skill, "fire-ticks", 100, false));
                 if (entity instanceof Player) {
                     plugin.getHeroManager().getHero((Player) entity).addEffect(new CombustEffect(skill, (Player) dmger));
                 } else
                     plugin.getEffectManager().addEntityEffect(entity, new CombustEffect(skill, (Player) dmger));
 
                 addSpellTarget(entity, hero);
                 int damage = SkillConfigManager.getUseSetting(hero, skill, Setting.DAMAGE, 4, false);
                 event.setDamage(damage);
             }
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         int damage = SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE, 1, false);
         return getDescription().replace("$1", damage + "");
     }
 }
