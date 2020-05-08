 package com.herocraftonline.dev.heroes.command.skill;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ClassChangeEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
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
         this.description = usage;
         registerEvent(Type.CUSTOM_EVENT, new SkillCustomListener(), Priority.Normal);
     }
 
     public void tryLearningSkill(Hero hero) {
        tryLearningSkill(hero,hero.getHeroClass());
    }

    public void tryLearningSkill(Hero hero, HeroClass heroClass) {
         if (Heroes.Permissions == null) {
             return;
         }
 
         Player player = hero.getPlayer();
         String world = player.getWorld().getName();
         String playerName = player.getName();
         ConfigurationNode settings = heroClass.getSkillSettings(name);
         if (settings != null) {
             if (meetsLevelRequirement(hero, getSetting(heroClass, SETTING_LEVEL, 1))) {
                 for (String permission : permissions) {
                     if (!Heroes.Permissions.has(player, permission)) {
                         Heroes.Permissions.addUserPermission(world, playerName, permission);
                     }
                 }
             } else {
                 for (String permission : permissions) {
                     if (Heroes.Permissions.has(player, permission)) {
                         Heroes.Permissions.removeUserPermission(world, playerName, permission);
                     }
                }
            }
        } else {
            for (String permission : permissions) {
                if (Heroes.Permissions.has(player, permission)) {
                    Heroes.Permissions.removeUserPermission(world, playerName, permission);
                 }
             }
         }
     }
 
     public class SkillCustomListener extends CustomEventListener {
         @Override
         public void onCustomEvent(Event event) {
             if (event instanceof ClassChangeEvent) {
                 ClassChangeEvent subEvent = (ClassChangeEvent) event;
                tryLearningSkill(subEvent.getHero(),subEvent.getTo());
             } else if (event instanceof LevelEvent) {
                 LevelEvent subEvent = (LevelEvent) event;
                 tryLearningSkill(subEvent.getHero());
             }
         }
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {}
 
 }
