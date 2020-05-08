 package com.herocraftonline.dev.heroes;
 
 import java.util.List;
 
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class HLevelListener extends CustomEventListener {
 
     private Heroes plugin;
 
     public HLevelListener(Heroes heroes) {
         this.plugin = heroes;
     }
 
     @Override
     public void onCustomEvent(Event event) {
         if (event instanceof LevelEvent) {
             LevelEvent subEvent = (LevelEvent) event;
             Hero hero = subEvent.getHero();
             HeroClass heroClass = hero.getHeroClass();
            hero.syncHealth();
            
             int level = subEvent.getTo();
             List<BaseCommand> sortCommands = plugin.getCommandManager().getCommands();
             for (BaseCommand command : sortCommands) {
                 if (command instanceof Skill) {
                     Skill skill = (Skill) command;
                     if (heroClass.hasSkill(skill.getName())) {
                         int levelRequired = skill.getSetting(heroClass, "level", 1);
                         if (levelRequired == level) {
                             Messaging.send(subEvent.getHero().getPlayer(), "You have learned $1.", skill.getName());
                         }
                     }
                 }
             }
         }
     }
 }
