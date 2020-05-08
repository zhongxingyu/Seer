 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveEffectSkill;
 
 public class SkillReflect extends ActiveEffectSkill {
 
     public SkillReflect(Heroes plugin) {
         super(plugin);
         setName("Reflect");
        setDescription("Skill - Reflect");
         setUsage("/skill reflect");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill reflect");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("reflected-amount", 0.5);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         applyEffect(hero);
         notifyNearbyPlayers(player.getLocation(), getUseText(), playerName, getName());
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent)) {
                 return;
             }
 
             EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
             Entity defender = edbe.getEntity();
             Entity attacker = edbe.getDamager();
             if (attacker instanceof LivingEntity && defender instanceof Player) {
                 Player defPlayer = (Player) defender;
                 Hero hero = plugin.getHeroManager().getHero(defPlayer);
                 if (hero.hasEffect(getName())) {
                     if (attacker instanceof Player) {
                         Player attPlayer = (Player) attacker;
                         if (plugin.getHeroManager().getHero(attPlayer).hasEffect(getName())) {
                             event.setCancelled(true);
                             return;
                         }
                     }
                     LivingEntity attEntity = (LivingEntity) attacker;
                     int damage = (int) (event.getDamage() * getSetting(hero.getHeroClass(), "reflected-amount", 0.5));
                     attEntity.damage(damage, defender);
                 }
             }
         }
     }
 }
