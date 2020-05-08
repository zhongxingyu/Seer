 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicInteractiveCommand;
 import com.herocraftonline.dev.heroes.command.BasicInteractiveCommandState;
 import com.herocraftonline.dev.heroes.command.InteractiveCommandState;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class ResetCommand extends BasicInteractiveCommand {
 
     Heroes plugin;
     private Set<Player> pendingResets = new HashSet<Player>();
 
     public ResetCommand(Heroes plugin) {
         super("Reset Class");
         this.plugin = plugin;
         this.setStates(new InteractiveCommandState[] { new StateA(), new StateB() });
         setDescription("Resets your XP and path");
         setUsage("/hero reset");
         setPermission("heroes.reset");
     }
 
     @Override
     public String getCancelIdentifier() {
         return "hero cancel";
     }
 
     @Override
     public void onCommandCancelled(CommandSender executor) {
         if (!(executor instanceof Player))
             return;
         pendingResets.remove(executor);
     }
 
     class StateA extends BasicInteractiveCommandState {
 
         public StateA() {
             super("hero reset");
             this.setArgumentRange(0, 0);
         }
 
         @Override
         public boolean execute(CommandSender executor, String identifier, String[] args) {
             if (!(executor instanceof Player))
                 return false;
 
             Player player = (Player) executor;
             HeroClass defaultClass = plugin.getClassManager().getDefaultClass();
 
             Messaging.send(executor, "This will reset all earned XP, both classes and reset your class to: " + defaultClass.getName());
             Messaging.send(executor, "Please §8/hero confirm §7 or §8/hero cancel §7this selection.");
 
             pendingResets.add(player);
             return true;
         }
 
     }
 
     class StateB extends BasicInteractiveCommandState {
 
         public StateB() {
             super("hero confirm");
             this.setArgumentRange(0, 0);
         }
 
         @Override
         public boolean execute(CommandSender executor, String identifier, String[] args) {
             if (!(executor instanceof Player))
                 return false;
             Player player = (Player) executor;
 
             Hero hero = plugin.getHeroManager().getHero(player);
             HeroClass defaultClass = plugin.getClassManager().getDefaultClass();
 
             // Reset Everything
             hero.clearEffects();
             hero.clearExperience();
             hero.clearCooldowns();
             hero.clearSummons();
             hero.clearBinds();
 
             hero.setHeroClass(defaultClass, false); // Set the hero to the default class
             hero.setHeroClass(null, true); // Null the second class
             hero.syncHealth(); // re-sync health just in-case the display isn't 100% accurate
            
             plugin.getHeroManager().performSkillChecks(hero);
             if (Heroes.properties.prefixClassName) {
                 player.setDisplayName("[" + hero.getHeroClass().getName() + "]" + player.getName());
             }
 
             Messaging.send(player, "Welcome to the path of the $1!", defaultClass.getName());
             plugin.getHeroManager().saveHero(hero);
             return true;
         }
     }
 }
