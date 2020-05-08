 package com.herocraftonline.dev.heroes.skill;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.permissions.Permission;
 
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
     private Permission permission;
 
     public OutsourcedSkill(Heroes plugin, String name) {
         super(plugin, name);
        setConfig(SkillManager.allSkillsConfig.getConfigurationSection(getName()));
         registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Monitor);
     }
 
     public void setPermissions(String[] permissions) {
         this.permissions = permissions;
         // Because Permissions can only be loaded once, 
         this.permission = plugin.getServer().getPluginManager().getPermission(getName());
         if (permission != null)
             plugin.getServer().getPluginManager().removePermission(this.permission);
 
         Map<String, Boolean> children = new HashMap<String, Boolean>();
         for (String s : permissions) {
             children.put(s, true);
         }
         this.permission = new Permission(getName(), "Permission-Skill " + getName(), children);
         plugin.getServer().getPluginManager().addPermission(this.permission);
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
         if (hero.getHeroClass().hasSkill(getName()) || (hero.getSecondClass() != null && hero.getSecondClass().hasSkill(getName()))) {
             if (hero.getLevel(this) >= getSetting(hero, Setting.LEVEL.node(), 1, true) && !Heroes.properties.disabledWorlds.contains(world)) {
                 if (Heroes.perms.getName().equals("Permissions3") || Heroes.perms.getName().equals("PermissionsEx")) {
                     for (String permission : permissions) {
                         if (!Heroes.perms.has(world, playerName, permission)) {
                             Heroes.perms.playerAddTransient(player, permission);
                         }
                     }
                 }
                 Heroes.perms.playerAddTransient(player, this.permission.getName());
             } else {
                 if (Heroes.perms.getName().equals("Permissions3") || Heroes.perms.getName().equals("PermissionsEx")) {
                     for (String permission : permissions) {
                         if (Heroes.perms.has(world, playerName, permission)) {
                             Heroes.perms.playerRemoveTransient(player, permission);
                         }
                     }
                 }
                 Heroes.perms.playerRemoveTransient(player, this.permission.getName());
             }
         } else {
             if (permissions == null) {
                 Heroes.log(Level.SEVERE, "No permissions detected for skill: " + this.getName() + " fix your config!");
                 return;
             }
             if (Heroes.perms.getName().equals("Permissions3") || Heroes.perms.getName().equals("PermissionsEx")) {
                 for (String permission : permissions) {
                     if (Heroes.perms.has(world, playerName, permission)) {
                         Heroes.perms.playerRemoveTransient(player, permission);
                     }
 
                 }
             }
             Heroes.perms.playerRemoveTransient(player, this.permission.getName());
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
