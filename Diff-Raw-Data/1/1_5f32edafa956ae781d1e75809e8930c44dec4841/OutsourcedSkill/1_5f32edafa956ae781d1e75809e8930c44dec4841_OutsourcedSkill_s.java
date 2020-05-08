 package com.herocraftonline.dev.heroes.command.skill;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ClassChangeEvent;
 import com.herocraftonline.dev.heroes.api.HeroLoadEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.SkillSettings;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class OutsourcedSkill extends Skill {
 
     protected String[] permissions;
 
     public OutsourcedSkill(Heroes plugin, String name, String[] permissions, String usage) {
         super(plugin);
         this.name = name;
         this.permissions = permissions;
         this.usage = usage;
         this.minArgs = 0;
         this.maxArgs = 0;
         registerEvent(Type.CUSTOM_EVENT, new SkillCustomListener(), Priority.Normal);
     }
 
     public void tryLearningSkill(Hero hero) {
         if (Heroes.Permissions == null) {
             return;
         }
 
         HeroClass heroClass = hero.getHeroClass();
         Player player = hero.getPlayer();
 
         String world = player.getWorld().getName();
         String playerName = player.getName();
         SkillSettings settings = heroClass.getSkillSettings(name);
         System.out.println("  " + name + " (lvl " + settings.LevelRequirement + ")");
         if (settings != null && meetsLevelRequirement(hero, settings.LevelRequirement)) {
             for (String permission : permissions) {
                 System.out.println("    " + permission);
                 if (!Heroes.Permissions.has(player, permission)) {
                     System.out.println("  adding perm " + permission);
                     Heroes.Permissions.addUserPermission(world, playerName, permission);
                 }
             }
         } else {
             for (String permission : permissions) {
                 System.out.println("    " + permission);
                 if (Heroes.Permissions.has(player, permission)) {
                     System.out.println("  removing perm " + permission);
                     Heroes.Permissions.removeUserPermission(world, playerName, permission);
                 }
             }
         }
         Heroes.Permissions.save(world);
     }
 
     public class SkillCustomListener extends CustomEventListener {
 
         @Override
         public void onCustomEvent(Event event) {
             if (event instanceof ClassChangeEvent) {
                 ClassChangeEvent subEvent = (ClassChangeEvent) event;
                 tryLearningSkill(subEvent.getHero());
             } else if (event instanceof LevelEvent) {
                 LevelEvent subEvent = (LevelEvent) event;
                 tryLearningSkill(subEvent.getHero());
             } else if (event instanceof HeroLoadEvent) {
                 HeroLoadEvent subEvent = (HeroLoadEvent) event;
                 tryLearningSkill(subEvent.getHero());
             }
         }
 
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {}
 
 }
