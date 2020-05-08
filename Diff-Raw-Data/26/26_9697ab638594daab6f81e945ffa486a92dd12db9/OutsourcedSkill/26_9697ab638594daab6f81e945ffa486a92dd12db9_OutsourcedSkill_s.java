 package com.herocraftonline.dev.heroes.skill;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ClassChangeEvent;
 import com.herocraftonline.dev.heroes.api.HeroChangeLevelEvent;
 import com.herocraftonline.dev.heroes.api.HeroesEventListener;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 /**
  * Allows any plugin to be adapted into a Heroes skill via permissions restrictions. These permission based skills are
  * automatically created based on data in the permission skills section of the server's classes.yml file. Listed
  * permissions are automatically applied and removed when a player becomes eligible (correct class and level) for the
  * skill as defined in the config. There should not be any need to extend this class.
  * </br>
  * </br>
  * <b>Skill Framework:</b>
  * <ul>
  * <li>{@link ActiveSkill}</li>
  * <ul>
  * <li>{@link ActiveEffectSkill}</li>
  * <li>{@link TargettedSkill}</li>
  * </ul>
  * <li>{@link PassiveSkill}</li> <li>{@link OutsourcedSkill}</li> </ul>
  */
 public class OutsourcedSkill extends Skill {
 
     private String[] permissions;
 
     public OutsourcedSkill(Heroes plugin, String name) {
         super(plugin, name);
         setConfig(getDefaultConfig());
         registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Monitor);
     }
     
     public void setPermissions(String[] permissions) {
         this.permissions = permissions;
     }
     
     /**
      * Serves no purpose for an outsourced skill.
      */
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         return true;
     }
 
     /**
      * Serves no purpose for an outsourced skill.
      */
     @Override
     public void init() {}
 
     /**
      * Grants this skill's associated permissions to the provided {@link Hero} if it is the level and the provided class
      * has the skill.
      * 
      * @param hero
      *            the <code>Hero</code> attempting to learn the skill
      * @param heroClass
      *            the {@link HeroClass} to check for this skill
      */
     public void tryLearningSkill(Hero hero) {
         Player player = hero.getPlayer();
         String world = player.getWorld().getName();
         String playerName = player.getName();
         if (hero.getHeroClass().getSkillSettings(getName()) != null || (hero.getSecondClass() != null && hero.getSecondClass().getSkillSettings(getName()) != null)) {
             if (hero.getLevel() >= getSetting(hero, Setting.LEVEL.node(), 1, true)) {
                 for (String permission : permissions) {
                     if (Heroes.Permissions != null && !hasPermission(world, playerName, permission)) {
                         addPermission(world, playerName, permission);
                     }
                     hero.addPermission(permission);
                 }
             } else {
                 for (String permission : permissions) {
                     if (Heroes.Permissions != null && hasPermission(world, playerName, permission)) {
                         removePermission(world, playerName, permission);
                     }
                     hero.removePermission(permission);
                 }
             }
         } else {
             for (String permission : permissions) {
                 if (Heroes.Permissions != null && hasPermission(world, playerName, permission)) {
                     removePermission(world, playerName, permission);
                 }
                 hero.removePermission(permission);
             }
         }
     }
 
     private void addPermission(String world, String player, String permission) {
         try {
             // Heroes.Permissions.safeGetUser(world, player).addPermission(permission); -- Incase we need it.
             Heroes.Permissions.safeGetUser(world, player).addTransientPermission(permission);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private boolean hasPermission(String world, String player, String permission) {
         try {
             return Heroes.Permissions.safeGetUser(world, player).hasPermission(permission);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return false;
     }
 
     private void removePermission(String world, String player, String permission) {
         try {
             Heroes.Permissions.safeGetUser(world, player).removePermission(permission);
             Heroes.Permissions.safeGetUser(world, player).removeTransientPermission(permission);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Monitors level and class change events and tries to give or remove the skill's permissions when appropriate.
      */
     public class SkillHeroListener extends HeroesEventListener {
 
         @Override
         public void onClassChange(final ClassChangeEvent event) {
             if (event.isCancelled())
                 return;
             
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
                 @Override
                 public void run() {
                     tryLearningSkill(event.getHero());
                 }
                 
             }, 1);
         }
 
         @Override
         public void onHeroChangeLevel(final HeroChangeLevelEvent event) {
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
                 @Override
                 public void run() {
                     tryLearningSkill(event.getHero());
                 }
                 
             }, 1);
         }
     }
 }
