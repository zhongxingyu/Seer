 package com.herocraftonline.dev.heroes.skill;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.minecraft.server.DamageSource;
 import net.minecraft.server.EntityLiving;
 
 import org.bukkit.Bukkit;
 import org.bukkit.EntityEffect;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.craftbukkit.entity.CraftLivingEntity;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 /**
  * The root class of the skill heirarchy. This class implements the basic functionality of every Heroes skill including
  * configuration handling, area-based player notifications and event registration. Because this class extends
  * {@link com.herocraftonline.dev.heroes.command.BaseCommand}, the constructor of every skill should define a name,
  * description, usage, min and max arguments and at least one identifier. Any registered events must provide an event
  * listener, usually created as an inner class.
  * </br>
  * </br>
  * <b>Skill Framework:</b>
  * <ul>
  * <li>{@link ActiveSkill}</li>
  * <ul>
  * <li>{@link TargettedSkill}</li>
  * </ul>
  * <li>{@link PassiveSkill}</li> <li>{@link OutsourcedSkill}</li> </ul>
  * <b>Note:</b> All skill identifiers <i>must</i> begin with <i>skill</i>, e.g. "skill fireball".
  */
 public abstract class Skill extends BasicCommand {
 
     public final Heroes plugin;
     private Configuration defaultConfig = new MemoryConfiguration();
 
     private final Set<SkillType> types = EnumSet.noneOf(SkillType.class);
 
     /**
      * The constructor of every skill must define:
      * <ul>
      * <li><code>name</code></li>
      * <li><code>description</code></li>
      * <li><code>usage</code></li>
      * <li><code>minArgs</code></li>
      * <li><code>maxArgs</code></li>
      * <li><code>identifiers</code></li>
      * <li><code>notes</code> (optional)</li>
      * </ul>
      * 
      * @param plugin
      *            the active Heroes instance
      */
     public Skill(Heroes plugin, String name) {
         super(name);
         this.plugin = plugin;
     }
 
     /**
      * Gets hero-specific description of the skill.  For use with level-based skill data.
      * @param hero
      * @return
      */
     public abstract String getDescription(Hero hero);
 
     public void addSpellTarget(Entity o, Hero hero) {
         plugin.getDamageManager().addSpellTarget(o, hero, this);
     }
 
     /**
      * Helper method that broadcasts a message to all players within 30 blocks of the specified source. These messages
      * can be suppressed by players on an individual basis.
      * 
      * @param source
      *            the <code>Location</code> to measure from
      * @param message
      *            the content of the message
      * @param args
      *            any text in the message of the format $<i>n</i> where <i>n</i>
      *            is an integer will be replaced with the <i>n</i>th element of
      *            this array
      */
     public void broadcast(Location source, String message, Object... args) {
         if (message == null || message.isEmpty() || message.equalsIgnoreCase("off")) {
             return;
         }
 
         final Player[] players = plugin.getServer().getOnlinePlayers();
         for (Player player : players) {
             Location playerLocation = player.getLocation();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (hero.isSuppressing(this)) {
                 continue;
             }
             if (source.getWorld().equals(playerLocation.getWorld())) {
                 if (playerLocation.distance(source) < 30) {
                     Messaging.send(player, message, args);
                 }
             }
         }
     }
 
     /**
      * Tests if the target is damagable from a source. Also adds a last-damage cause to the target which allows proper
      * xp.
      * Returns if the damage check was successful.
      * 
      * @param player
      * @param target
      * @return
      */
     public boolean damageCheck(Player player, LivingEntity target) {
        if (player.equals(target))
             return false;
 
         EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
        if (damageEntityEvent.isCancelled())
             return false;
 
         //Reverse damage check to make sure the target can damage the player - this prevents the player from attacking the target while invulnerable
         damageEntityEvent = new EntityDamageByEntityEvent(target, player, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled())
             return false;
         return true;
     }
 
     /**
      * The end of the execution path of a skill, this method is called whenever a command with a registered identifier
      * is used.
      * 
      * @param sender
      *            the <code>CommandSender</code> issuing the command
      * @param args
      *            the arguments provided with the command
      */
     @Override
     public abstract boolean execute(CommandSender sender, String identifier, String[] args);
 
     /**
      * Creates and returns a <code>ConfigurationNode</code> containing all the default data for the skill. By default,
      * this configuration is empty.
      * 
      * @return an empty configuration
      */
     public ConfigurationSection getDefaultConfig() {
         return defaultConfig;
     }
 
     public Set<SkillType> getTypes() {
         return Collections.unmodifiableSet(this.types);
     }
 
     /**
      * An initialization method called after all configuration data is loaded.
      */
     public abstract void init();
 
     @Override
     public boolean isShownOnHelpMenu() {
         return false;
     }
 
     public boolean isType(SkillType type) {
         return types.contains(type);
     }
 
     protected void setTypes(SkillType... types) {
         this.types.addAll(Arrays.asList(types));
     }
 
     public static void knockBack(LivingEntity target, LivingEntity attacker, int damage) {
         EntityLiving el = ((CraftLivingEntity )target).getHandle();
         EntityLiving aEL = ((CraftLivingEntity) attacker).getHandle();
         el.velocityChanged = true;
         double d0 = aEL.locX - el.locX;
         double d1;
 
         for (d1 = aEL.locZ - el.locZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
             d0 = (Math.random() - Math.random()) * 0.01D;
         }
 
         el.au = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - el.yaw;
         el.a(aEL, damage, d0, d1);
         target.playEffect(EntityEffect.HURT);
     }
 
     public static boolean damageEntity(LivingEntity target, LivingEntity attacker, int damage, DamageCause cause) {
         if (target.isDead() || target.getHealth() <= 0) {
             return false;
         }
         //Do it ourselves cause bukkit is stubborn
         ((CraftLivingEntity) target).setNoDamageTicks(0);
 
         if (cause != DamageCause.ENTITY_ATTACK) {
             EntityDamageByEntityEvent edbe = new EntityDamageByEntityEvent(attacker, target, cause, damage);
             Bukkit.getServer().getPluginManager().callEvent(edbe);
             if (edbe.isCancelled()) {
                 return false;
             }
 
             knockBack(target, attacker, edbe.getDamage());
             target.setLastDamageCause(edbe);
             int oldHealth = target.getHealth();
             int newHealth = oldHealth - edbe.getDamage();
             if (newHealth < 0) {
                 newHealth = 0;
             }
             EntityLiving el = ((CraftLivingEntity) target).getHandle();
             el.setHealth(newHealth);
             if (newHealth == 0) {
                 if (attacker instanceof Player) {
                     el.die(DamageSource.playerAttack(((CraftPlayer) attacker).getHandle()));
                 } else {
                     el.die(DamageSource.mobAttack(((CraftLivingEntity) attacker).getHandle()));
                 }
             }
         } else {
             target.damage(damage, attacker);
         }
         return true;
     }
 
     /**
      * Checks if the player has enough of the specified reagent in their inventory
      * 
      * @param player
      * @param itemStack
      * @return
      */
     protected boolean hasReagentCost(Player player, ItemStack itemStack) {
         if (itemStack == null || itemStack.getAmount() == 0) {
             return true;
         }
         int amount = 0;
         for (ItemStack stack : player.getInventory().all(itemStack.getType()).values()) {
             amount += stack.getAmount();
             if (amount >= itemStack.getAmount())
                 return true;
         }
         return false;
     } 
 
     protected ItemStack getReagentCost(Hero hero) {
         // Reagent stuff
         int reagentCost = SkillConfigManager.getUseSetting(hero, this, Setting.REAGENT_COST, 0, true);
         String reagentName = SkillConfigManager.getUseSetting(hero, this, Setting.REAGENT, (String) null);
         ItemStack itemStack = null;
         if (reagentCost > 0 && reagentName != null && reagentName != "") {
             String[] vals = reagentName.split(":");
             try {
                 int id = Integer.parseInt(vals[0]);
                 byte sub = 0;
                 if (vals.length > 1) {
                     sub = (byte) Integer.parseInt(vals[1]);
                 }
                 itemStack = new ItemStack(id, reagentCost, sub);
             } catch (NumberFormatException e) {
                 Heroes.log(Level.SEVERE, "Invalid skill reagent defined in " + getName() + ". Please switch to new format ID:DAMAGE");
             }
         }  
         return itemStack;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null)
             return false;
         else if (obj.toString().equals(getName()))
             return true;
         else if (!(obj instanceof Skill))
             return false;
 
         Skill s = (Skill) obj;
         return s.getName().equals(getName());
     }
 
     @Override
     public int hashCode() {
         return getName().hashCode();
     }
 }
