 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroesWeaponDamageEvent;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillBackstab extends PassiveSkill {
 
     public SkillBackstab(Heroes plugin) {
         super(plugin, "Backstab");
         setDescription("You are more lethal when attacking from behind!");
         setArgumentRange(0, 0);
 
         registerEvent(Type.CUSTOM_EVENT, new CustomListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("attack-bonus", 1.5);
         return node;
     }
 
     public class CustomListener extends CustomEventListener {
 
         @Override
         public void onCustomEvent(Event event) {
             if (!(event instanceof HeroesWeaponDamageEvent)) return;
             
             HeroesWeaponDamageEvent subEvent = (HeroesWeaponDamageEvent) event;
             if (subEvent.getDamager() instanceof Player) {
                 Player player = (Player) subEvent.getDamager();
                 Hero hero = getPlugin().getHeroManager().getHero(player);
                 if (hero.hasEffect(getName())) {
                     if (subEvent.getEntity().getLocation().getDirection().dot(player.getLocation().getDirection()) <= 0) return;
                     
                     subEvent.setDamage((int) (subEvent.getDamage() * getSetting(hero.getHeroClass(), "attack-bonus", 1.5)));
                     String name = "";
                     if (subEvent.getEntity() instanceof Player)
                         name = ((Player) subEvent.getEntity()).getName();
                     else if (subEvent.getEntity() instanceof Creature)
                         name = "a " + Messaging.getCreatureName((Creature) subEvent.getEntity()).toLowerCase();
                         
                    broadcast(player.getLocation(), player + " backstabbed " + name, player.getDisplayName());
                 }
             }
         }
     }
 }
