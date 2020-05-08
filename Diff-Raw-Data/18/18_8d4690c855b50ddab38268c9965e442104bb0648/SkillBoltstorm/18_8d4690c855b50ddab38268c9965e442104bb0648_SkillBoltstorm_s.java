 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class SkillBoltstorm extends ActiveSkill {
     
     private String applyText;
     private String expireText;
     private Random rand;
     
     public SkillBoltstorm(Heroes plugin) {
         super(plugin, "Boltstorm");
         setDescription("Calls bolts of lightning down upon nearby enemies.");
         setUsage("/skill boltstorm");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill boltstorm"});
         rand = new Random();
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("range", 7); //radius
         node.setProperty("duration", 10000); //in milliseconds
         node.setProperty("period", 1000); //in milliseconds
         node.setProperty("damage", 4); //Per-tick damage
         node.setProperty("apply-text", "%hero% has summoned a boltstorm!");
         node.setProperty("expire-text", "%hero%'s boltstorm has subsided!");
         return node;
     }
     
     @Override
     public boolean use(Hero hero, String[] args) {
         int period = getSetting(hero.getHeroClass(), "period", 1000);
         int duration = getSetting(hero.getHeroClass(), "duration", 10000);
         hero.addEffect(new BoltStormEffect(this, period, duration));
         return true;
     }
     
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%hero% has summoned a boltstorm!").replace("%hero%", "$1");
         expireText = getSetting(null, "expire-text", "%hero%'s boltstorm has subsided!").replace("%hero%", "$1");
     }
     
     public class BoltStormEffect extends PeriodicEffect implements Dispellable {
 
         public BoltStormEffect(Skill skill, long period, long duration) {
             super(skill, "Boltstorm", period, duration);
 
         }
         
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
         
         @Override
         public void remove(Hero hero) {
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
         
         @Override 
         public void tick(Hero hero) {
             super.tick(hero);
             
             Player player = hero.getPlayer();
             int range = getSetting(hero.getHeroClass(), "range", 7);
             
             List<LivingEntity> targets = new ArrayList<LivingEntity>();
             for (Entity entity: player.getNearbyEntities(range, range, range)) {
                 if (hero.getParty() != null && entity instanceof Player) { //Party check
                     Hero targetHero = getPlugin().getHeroManager().getHero((Player) entity);
                     if (hero.getParty().isPartyMember(targetHero))
                         continue;
                 } 
                 if (entity.equals(player)) { // never target the caster
                     continue;
                 }
                 if (entity instanceof LivingEntity)
                     targets.add((LivingEntity) entity);
             }
             if (targets.isEmpty()) return;
             int damage = getSetting(hero.getHeroClass(), "damage", 4);
             LivingEntity target = targets.get(rand.nextInt(targets.size()));
             getPlugin().getDamageManager().addSpellTarget(target);
            target.damage(damage, player);
             target.getWorld().strikeLightningEffect(target.getLocation());
         }
     }
 }
