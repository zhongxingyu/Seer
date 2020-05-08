 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Random;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroesEventListener;
 import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.effects.Harmful;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillCurse extends TargettedSkill {
 
     private String applyText;
     private String expireText;
     private String missText;
     private Random rand = new Random();
     
    public SkillCurse(Heroes plugin) {
         super(plugin, "Curse");
         setDescription("Curses your target causing their attacks to miss");
         setUsage("/skill curse <target>");
         setArgumentRange(0, 1);
         setIdentifiers(new String[]{"skill curse"});
         
         registerEvent(Type.CUSTOM_EVENT, new SkillEventListener(), Priority.Highest);
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 5000); //in milliseconds
         node.setProperty("miss-chance", .50); //decimal representation of miss-chance
         node.setProperty("miss-text", "%target% misses an attack!");
         node.setProperty("apply-text", "%target% has been cursed!");
         node.setProperty("expire-text", "%target% has recovered from the curse!");
         return node;
     }
     
     @Override
     public void init() {
         super.init();
         missText = getSetting(null, "miss-text", "%target% misses an attack!").replace("%target%", "$1");
         applyText = getSetting(null, "apply-text", "%target% has recovered from the curse!").replace("%target%", "$1");
         expireText = getSetting(null, "expire-text", "%target% has recovered from the poison!").replace("%target%", "$1");
     }
     
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         
         return false;
     }
     
     public class CurseEffect extends ExpirableEffect implements Dispellable, Harmful {
 
         private final double missChance;
         
         public CurseEffect(Skill skill, long duration, double missChance) {
             super(skill, "Curse", duration);
             this.missChance = missChance;
         }
         
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void apply(Creature creature) {
             super.apply(creature);
             broadcast(creature.getLocation(), applyText, Messaging.getCreatureName(creature).toLowerCase());
         }
         
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
 
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
         
         @Override
         public void remove(Creature creature) {
             super.remove(creature);
             broadcast(creature.getLocation(), expireText, Messaging.getCreatureName(creature).toLowerCase());
         }
 
         public double getMissChance() {
             return missChance;
         }
     }
     
     public class SkillEventListener extends HeroesEventListener {
         
         @Override
         public void onWeaponDamage(WeaponDamageEvent event) {
             if (event.isCancelled() || event.getDamage() == 0) return;
             
             Hero hero = null;
             Creature creature = null;
             if (event.getDamager() instanceof Player) {
                 hero = getPlugin().getHeroManager().getHero((Player) event.getDamager());
             } else if (event.getDamager() instanceof Creature) {
                 creature = (Creature) event.getDamager();
             } else if (event.getDamager() instanceof Projectile) {
                 Projectile proj = (Projectile) event.getDamager();
                 if (proj.getShooter() == null) return;
                 if (proj.getShooter() instanceof Player) {
                     hero = getPlugin().getHeroManager().getHero((Player) proj.getShooter());
                 } else if (proj.getShooter() instanceof Creature) {
                     creature = (Creature) proj.getShooter();
                 }
             }
             if (hero != null) {
                 if (hero.getEffect("Curse") != null) {
                     CurseEffect cEffect = (CurseEffect) hero.getEffect("Curse");
                     if (rand.nextDouble() < cEffect.missChance) {
                         event.setCancelled(true);
                         broadcast(hero.getPlayer().getLocation(), missText, hero.getPlayer().getDisplayName());
                     }
                 }
             } else if (creature != null) {
                 for ( Effect effect : getPlugin().getHeroManager().getCreatureEffects(creature)) {
                     if (effect instanceof CurseEffect) {
                         CurseEffect cEffect = (CurseEffect) effect;
                         if (rand.nextDouble() < cEffect.missChance) {
                             event.setCancelled(true);
                             broadcast(creature.getLocation(), missText, Messaging.getCreatureName(creature).toLowerCase());
                         }
                     }
                 }
             }
         }
     }
 }
