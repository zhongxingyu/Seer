 package com.herocraftonline.dev.heroes.skill;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 /**
  * A triggered skill that requires a target. TargettedSkills define a maximum distance setting. A target can be supplied
  * as the first argument to the command. If no such argument is provided, then the skill will use whatever target the
  * player is looking at within the configurable maximum distance, if any. The primary method to be overridden by
  * TargettedSkills is {@link #use(Hero, LivingEntity, String[])}, which is called by {@link #use(Hero, String[])} after
  * determining the target.
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
  * </br></br>
  * See {@link ActiveSkill} for an overview of command triggered skills.
  */
 public abstract class TargettedSkill extends ActiveSkill {
 
     /**
      * When defining your own constructor, be sure to assign the name, description, usage, argument bounds and
      * identifier fields as defined in {@link com.herocraftonline.dev.heroes.command.BaseCommand}. Remember that each
      * identifier must begin with <i>skill</i>.
      * 
      * @param plugin
      *            the active Heroes instance
      */
     public TargettedSkill(Heroes plugin, String name) {
         super(plugin, name);
     }
 
     /**
      * Creates and returns a <code>ConfigurationNode</code> containing the default usage text and targetting range. When
      * using additional configuration settings in your skills, be sure to override this method to define them with
      * defaults.
      * 
      * @return a default configuration
      */
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection section = super.getDefaultConfig();
         section.set(Setting.USE_TEXT.node(), "%hero% used %skill% on %target%!");
         section.set(Setting.MAX_DISTANCE.node(), 15);
         return section;
     }
 
     /**
      * Loads and stores the skill's usage text from the configuration. By default, this text is
      * "%hero% used %skill% on %target!" where %hero%, %skill% and %target% are replaced with the Hero's, skill's and
      * target's names, respectively.
      */
     @Override
     public void init() {
         String useText = SkillConfigManager.getRaw(this, Setting.USE_TEXT.node(), "%hero% used %skill% on %target%!");
         useText = useText.replace("%hero%", "$1").replace("%skill%", "$2").replace("%target%", "$3");
         setUseText(useText);
     }
 
     /**
      * The heart of any TargettedSkill, this method defines what actually happens when the skill is used.
      * 
      * @param hero
      *            the {@link Hero} using the skill
      * @param args
      *            the arguments provided with the command
      * @return <code>true</code> if the skill executed properly, <code>false</code> otherwise
      */
     public abstract SkillResult use(Hero hero, LivingEntity target, String[] args);
 
     /**
      * Handles target acquisition before calling {@link #use(Hero, LivingEntity, String[])}.
      * 
      * @param hero
      *            the {@link Hero} using the skill
      * @param args
      *            the arguments provided with the command
      * @return <code>true</code> if the skill executed properly, <code>false</code> otherwise
      */
     @Override
     public SkillResult use(Hero hero, String[] args) {
         int maxDistance = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE, 15, false);
         double distBonus = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE, 0.0, false) * hero.getSkillLevel(this);
         maxDistance += (int) distBonus;
         LivingEntity target = getTarget(hero, maxDistance, args);
         if (target == null) {
             return SkillResult.INVALID_TARGET_NO_MSG;
         } else if (args.length > 1 && target != null) {
             args = Arrays.copyOfRange(args, 1, args.length);
         }
 
         return use(hero, target, args);
     }
 
     public SkillResult useDelayed(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         int maxDistance = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE, 15, false);
         if (!player.getWorld().equals(target.getWorld()) || player.getLocation().distance(target.getLocation()) > maxDistance) {
             Messaging.send(player, "Target is out of range!");
             return SkillResult.FAIL;
         }
         return use(hero, target, args);
     }
     
     protected void broadcastExecuteText(Hero hero, LivingEntity target) {
         Player player = hero.getPlayer();
         broadcast(player.getLocation(), getUseText(), player.getDisplayName(), getName(), target == player ? "themself" : getEntityName(target));
     }
 
     /**
      * Returns the pretty name of a <code>LivingEntity</code>.
      * 
      * @param entity
      *            the entity
      * @return the pretty name of the entity
      */
     public static String getEntityName(LivingEntity entity) {
         return entity instanceof Player ? ((Player) entity).getName() : entity.getClass().getSimpleName().substring(5);
     }
 
     /**
      * Returns the first LivingEntity in the line of sight of a Player.
      * 
      * @param player
      *            the player being checked
      * @param maxDistance
      *            the maximum distance to search for a target
      * @return the player's target or null if no target is found
      */
     public static LivingEntity getPlayerTarget(Player player, int maxDistance) {
         if (player.getLocation().getBlockY() > player.getLocation().getWorld().getMaxHeight() )
             return null;
         List<Block> lineOfSight = player.getLineOfSight(Util.transparentIds, maxDistance);
         List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);
         for (Entity entity : nearbyEntities) {
             if (entity instanceof LivingEntity) {
                 Location eLoc = entity.getLocation();
                 for (Block block : lineOfSight) {
                     Location bLoc = block.getLocation();
                     if (eLoc.getBlockX() == bLoc.getBlockX() && eLoc.getBlockZ() == bLoc.getBlockZ() && Math.abs(eLoc.getBlockY() - bLoc.getBlockY()) < 2)
                         return (LivingEntity) entity;
                 }
             }
         }
         return null;
     }
 
     private LivingEntity getTarget(Hero hero, int maxDistance, String[] args) {
         Player player = hero.getPlayer();
         LivingEntity target = null;
         if (args.length > 0) {
             target = plugin.getServer().getPlayer(args[0]);
             if (target == null) {
                 Messaging.send(player, "Invalid target!");
                 return null;
             }
             if (target.getLocation().toVector().distance(player.getLocation().toVector()) > maxDistance) {
                 Messaging.send(player, "Target is too far away.");
                 return null;
             }
             if (!inLineOfSight(player, (Player) target)) {
                 Messaging.send(player, "Sorry, target is not in your line of sight!");
                 return null;
             }
             if (target.isDead() || target.getHealth() == 0)
                 Messaging.send(player, "You can't target the dead!");
             return null;
         }
         if (target == null) {
             target = getPlayerTarget(player, maxDistance);
         }
         if (target == null) {
             // don't self-target harmful skills
             if (this.isType(SkillType.HARMFUL)) {
                 return null;
             }
             target = player;
         }
 
         // Do a PvP check automatically for any harmful skill
         if (this.isType(SkillType.HARMFUL)) {
             if (player.equals(target) || hero.getSummons().contains(target) || !damageCheck(player, target)) {
                 Messaging.send(player, "Sorry, You can't damage that target!");
                return target;
             }
         }
         return target;
     }
 
     @Override
     protected boolean addDelayedSkill(Hero hero, int delay, String identifier, String[] args) {
         final Player player = hero.getPlayer();
         int maxDistance = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE, 15, false);
         LivingEntity target = getTarget(hero, maxDistance, args);
         if (target == null) {
             return false;
         } else if (args.length > 1 && target != null) {
             args = Arrays.copyOfRange(args, 1, args.length);
         }
 
         DelayedSkill dSkill = new DelayedTargettedSkill(identifier, player, delay, this, target, args);
         broadcast(player.getLocation(), "$1 begins to use $2 on $3!", player.getDisplayName(), getName(), Messaging.getLivingEntityName(target));
         plugin.getHeroManager().getDelayedSkills().put(hero, dSkill);
         hero.setDelayedSkill(dSkill);
         return true;
     }
 
     /**
      * Helper method to check whether a player is in another player's line of sight.
      * 
      * @param a
      *            the source
      * @param b
      *            the target
      * @return <code>true</code> if <code>b</code> is in <code>a</code>'s line of sight; <code>false</code> otherwise
      */
     public static boolean inLineOfSight(Player a, Player b) {
         if (a == b)
             return true;
 
         Location aLoc = a.getEyeLocation();
         Location bLoc = b.getEyeLocation();
         int distance = Location.locToBlock(aLoc.toVector().distance(bLoc.toVector())) - 1;
         if (distance > 120)
             return false;
         Vector ab = new Vector(bLoc.getX() - aLoc.getX(), bLoc.getY() - aLoc.getY(), bLoc.getZ() - aLoc.getZ());
         Iterator<Block> iterator = new BlockIterator(a.getWorld(), aLoc.toVector(), ab, 0, distance + 1);
         while (iterator.hasNext()) {
             Block block = iterator.next();
             Material type = block.getType();
             if (type != Material.AIR && type != Material.WATER)
                 return false;
         }
         return true;
     }
 
 }
