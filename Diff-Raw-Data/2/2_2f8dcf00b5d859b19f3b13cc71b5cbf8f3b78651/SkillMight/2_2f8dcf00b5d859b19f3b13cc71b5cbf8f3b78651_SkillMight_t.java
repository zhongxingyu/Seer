 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroesWeaponDamageEvent;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.nijiko.coelho.iConomy.util.Messaging;
 
 public class SkillMight extends ActiveSkill {
 
     public SkillMight(Heroes plugin) {
         super(plugin, "Might");
         setDescription("You increase your party's damage with weapons!");
         setArgumentRange(0, 0);
        setUsage("/skill might");
        setIdentifiers(new String[]{"skill might"});
 
         registerEvent(Type.CUSTOM_EVENT, new CustomListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage-bonus", 1.25);
         node.setProperty("range", 10);
         node.setProperty("duration", 600000); //in Milliseconds - 10 minutes
         return node;
     }
     
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         int duration = getSetting(hero.getHeroClass(), "duration", 600000);
         double damageBonus = getSetting(hero.getHeroClass(), "damage-bonus", 1.25);
         
         MightEffect mEffect = new MightEffect(this, duration, damageBonus);
         if (hero.hasEffect("Might")) {
             if (((MightEffect) hero.getEffect("Might")).getDamageBonus() > mEffect.getDamageBonus()) {
                 return false;
             }   
         }
         hero.addEffect(mEffect);
         
         if (hero.getParty() != null) {
             int rangeSquared = getSetting(hero.getHeroClass(), "range", 10)^2;
             for (Hero pHero : hero.getParty().getMembers()) {
                 if (pHero.getPlayer().getLocation().distanceSquared(player.getLocation()) > rangeSquared) continue;
                 if (hero.hasEffect("Might")) {
                     if (((MightEffect) pHero.getEffect("Might")).getDamageBonus() > mEffect.getDamageBonus()) {
                         continue;
                     }   
                 }
                 pHero.addEffect(mEffect);
             }
         }
         
         broadcastExecuteText(hero);
         return true;
     }
     
     public class MightEffect extends ExpirableEffect implements Dispellable {
 
         private final double damageBonus;
         
         public MightEffect(Skill skill, long duration, double damageBonus) {
             super(skill, "Might", duration);
             this.damageBonus = damageBonus;
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             Messaging.send(player, "Your muscles bulge with power!");
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             Messaging.send(player, "Your muscles shrink back to their normal size!");
         }
         
         public double getDamageBonus() {
             return damageBonus;
         }
     }
     
     public class CustomListener extends CustomEventListener {
 
         @Override
         public void onCustomEvent(Event event) {
             if (!(event instanceof HeroesWeaponDamageEvent)) return;
             HeroesWeaponDamageEvent subEvent = (HeroesWeaponDamageEvent) event;
 
             if (subEvent.getCause() != DamageCause.ENTITY_ATTACK)  return;
             
             if (subEvent.getDamager() instanceof Player) {
                 Player player = (Player) subEvent.getDamager();
                 Hero hero = getPlugin().getHeroManager().getHero(player);
 
                 if (hero.hasEffect("Might")) {
                     double damageBonus = ((MightEffect) hero.getEffect("Might")).getDamageBonus();
                     subEvent.setDamage((int) (subEvent.getDamage() * damageBonus));
                 }
             } else if (subEvent.getDamager() instanceof Projectile) {
                 if (((Projectile) subEvent.getDamager()).getShooter() instanceof Player) {
                     Player player = (Player) ((Projectile)subEvent.getDamager()).getShooter();
                     Hero hero = getPlugin().getHeroManager().getHero(player);
                     
                     if (hero.hasEffect("Might")) {
                         double damageBonus = ((MightEffect) hero.getEffect("Might")).getDamageBonus();
                         subEvent.setDamage((int) (subEvent.getDamage() * damageBonus));
                     }
                 }
             }
         }
     }
 }
     
