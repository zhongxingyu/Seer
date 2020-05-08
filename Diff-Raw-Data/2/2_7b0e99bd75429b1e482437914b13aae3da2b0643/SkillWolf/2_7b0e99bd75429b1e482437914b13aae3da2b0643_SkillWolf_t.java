 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTameEvent;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillWolf extends PassiveSkill {
 
     public SkillWolf(Heroes plugin) {
         super(plugin, "Wolf");
        setDescription("Ability to tame wolves!");
         setUsage("/skill wolf <release|summon>");
         setArgumentRange(0, 1);
         setIdentifiers("skill wolf");
         setTypes(SkillType.SUMMON, SkillType.KNOWLEDGE);
 
         registerEvent(Type.ENTITY_TAME, new SkillEntityListener(this), Priority.Highest);
     }
 
 
     public class SkillEntityListener extends EntityListener {
 
         private final SkillWolf skill;
 
         SkillEntityListener(SkillWolf skill) {
             this.skill = skill;
         }
 
         @Override
         public void onEntityTame(EntityTameEvent event) {
             Heroes.debug.startTask("HeroesSkillListener.Wolf");
             AnimalTamer owner = event.getOwner();
             Entity animal = event.getEntity();
             if (event.isCancelled() || !(animal instanceof Wolf) || !(owner instanceof Player)) {
                 Heroes.debug.stopTask("HeroesSkillListener.Wolf");
                 return;
             }
 
             Player player = (Player) owner;
             Hero hero = plugin.getHeroManager().getHero(player);
             if (!hero.canUseSkill(skill.getName())) {
                 Messaging.send(player, "You can't tame wolves!");
                 event.setCancelled(true);
             }
             Heroes.debug.stopTask("HeroesSkillListener.Wolf");
         }
     }
 }
