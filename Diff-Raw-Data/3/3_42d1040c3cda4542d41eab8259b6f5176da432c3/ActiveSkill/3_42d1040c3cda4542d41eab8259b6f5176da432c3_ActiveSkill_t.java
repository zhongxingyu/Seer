 package com.herocraftonline.dev.heroes.skill;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillUseEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 /**
  * A skill that performs an action in direct response to a user command. All skill identifiers <i>must</i>
  * begin with <i>skill</i>, e.g. "skill fireball", in order to be recognized. ActiveSkills define four default settings:
  * mana, cooldown, experience and usage text. Mana is deducted and a cooldown is activated when the
  * {@link #use(Hero, String[]) use} method returns <code>true</code>. The {@link #execute(CommandSender, String[])
  * execute} automatically handles class, level, mana and cooldown checks on a player attempting to use a skill and
  * should not be overridden. If all of these checks pass, the <code>use</code> method is called, which should contain
  * the heart of the skill's behavior that is unique to each skill.
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
 public abstract class ActiveSkill extends Skill {
 
     private String useText;
     private boolean awardExpOnCast = true;
     private static Map<Player, Skill> delayedSkillUsers = new HashMap<Player, Skill>();
 
     /**
      * When defining your own constructor, be sure to assign the name, description, usage, argument bounds and
      * identifier fields as defined in {@link com.herocraftonline.dev.heroes.command.BaseCommand}. Remember that each
      * identifier must begin with <i>skill</i>.
      * 
      * @param plugin
      *            the active Heroes instance
      */
     public ActiveSkill(Heroes plugin, String name) {
         super(plugin, name);
     }
 
     /**
      * Called whenever a command with an identifier registered to this skill is used. This implementation performs all
      * necessary class, level, mana and cooldown checks. This method should <i>not</i> be overridden unless you really
      * know what you're doing. If all checks pass, this method calls {@link #use(Hero, String[]) use}. If
      * <code>use</code> returns <code>true</code>, this method automatically deducts mana, awards experience and sets a
      * cooldown.
      * 
      * @param sender
      *            the <code>CommandSender</code> issuing the command
      * @param args
      *            the arguments provided with the command
      */
     @SuppressWarnings("deprecation")
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         if (!(sender instanceof Player))
             return false;
 
         String name = this.getName();
         Player player = (Player) sender;
         Hero hero = plugin.getHeroManager().getHero(player);
         if (hero == null) {
             Messaging.send(player, "You are not a hero.");
             return false;
         }
         HeroClass heroClass = hero.getHeroClass();
        HeroClass secondClass = hero.getSecondClass();
        if (!heroClass.hasSkill(name) && (secondClass == null || !heroClass.hasSkill(name))) {
             Messaging.send(player, "$1s cannot use $2.", heroClass.getName(), name);
             return false;
         }
         int level = getSetting(hero, Setting.LEVEL.node(), 1, true);
         if (hero.getLevel() < level) {
             Messaging.send(player, "You must be level $1 to use $2.", String.valueOf(level), name);
             return false;
         }
 
         long time = System.currentTimeMillis();
         Long global = hero.getCooldown("Global");
         if (global != null && time < global) {
             Messaging.send(hero.getPlayer(), "Sorry, you must wait $1 seconds longer before using another skill.", (global - time) / 1000);
             return false;
         }
         int cooldown = getSetting(hero, Setting.COOLDOWN.node(), 0, true);
         if (cooldown > 0) {
             Long expiry = hero.getCooldown(name);
             if (expiry != null && time < expiry) {
                 long remaining = expiry - time;
                 Messaging.send(hero.getPlayer(), "Sorry, $1 still has $2 seconds left on cooldown!", name, remaining / 1000);
                 return false;
             }
         }
         int manaCost = getSetting(hero, Setting.MANA.node(), 0, true);
         String reagentName = getSetting(hero, Setting.REAGENT.node(), (String) null);
         ItemStack itemStack = null;
         if (reagentName != null) {
             if (Material.matchMaterial(reagentName) != null) {
                 int reagentCost = getSetting(hero, Setting.REAGENT_COST.node(), 0, true);
                 itemStack = new ItemStack(Material.matchMaterial(reagentName), reagentCost);
             }
         }
 
         int healthCost = getSetting(hero, Setting.HEALTH_COST.node(), 0, true);
 
         SkillUseEvent skillEvent = new SkillUseEvent(this, player, hero, manaCost, healthCost, itemStack, args);
         plugin.getServer().getPluginManager().callEvent(skillEvent);
         if (skillEvent.isCancelled())
             return false;
 
         // Update manaCost with result of SkillUseEvent
         manaCost = skillEvent.getManaCost();
         if (manaCost > hero.getMana()) {
             Messaging.send(player, "Not enough mana!");
             return false;
         }
 
         // Update healthCost with results of SkillUseEvent
         healthCost = skillEvent.getHealthCost();
         if (healthCost > 0 && hero.getHealth() <= healthCost) {
             Messaging.send(player, "Not enough health!");
             return false;
         }
 
         itemStack = skillEvent.getReagentCost();
         if (itemStack != null) {
             if (itemStack.getAmount() != 0 && !hasReagentCost(player, itemStack)) {
                 reagentName = itemStack.getType().name().toLowerCase().replace("_", " ");
                 Messaging.send(player, "Sorry, you need to have $1 $2 to use that skill!", itemStack.getAmount(), reagentName);
                 return false;
             }
         }
 
         int delay = getSetting(hero, Setting.DELAY.node(), 0, true);
         if (delay > 0 && !delayedSkillUsers.containsKey(sender)) {
             addDelayedSkill(hero, delay, identifier, args);
             return false;
         } else if (delayedSkillUsers.containsKey(sender)) {
             Skill skill = delayedSkillUsers.get(sender);
             int taskId = hero.getDelayedSkillTaskId();
             if (skill != this || (taskId != -1 && plugin.getServer().getScheduler().isQueued(taskId))) {
                 plugin.getServer().getScheduler().cancelTask(taskId);
                 broadcast(player.getLocation(), "$1 has stopped using $2!", player.getDisplayName(), skill.getName());
                 //If the new skill is also a delayed skill lets 
                 if (delay > 0) {
                     addDelayedSkill(hero, delay, identifier, args);
                     return false;
                 }
             }
             delayedSkillUsers.remove(sender);
             hero.setDelayedSkillTaskId(-1);
         }
 
         if (use(hero, args)) {
             // Set cooldown
             if (cooldown > 0) {
                 hero.setCooldown(name, time + cooldown);
             }
 
             if (plugin.getConfigManager().getProperties().globalCooldown > 0) {
                 hero.setCooldown("Global", plugin.getConfigManager().getProperties().globalCooldown + time);
             }
 
             // Award XP for skill usage
             if (this.awardExpOnCast) {
                 this.awardExp(hero);
             }
 
             // Deduct mana
             hero.setMana(hero.getMana() - manaCost);
             if (hero.isVerbose() && manaCost > 0) {
                 Messaging.send(hero.getPlayer(), ChatColor.BLUE + "MANA " + Messaging.createManaBar(hero.getMana()));
             }
 
             // Deduct health
             if (healthCost > 0) {
                 plugin.getDamageManager().addSpellTarget(player, hero, this);
                 player.damage(healthCost, player);
             }
 
             // Only charge the item cost if it's non-null
             if (itemStack != null) {
                 player.getInventory().removeItem(itemStack);
                 player.updateInventory();
             }
 
             return true;
         } else
             return false;
     }
 
     /**
      * Creates and returns a <code>ConfigurationNode</code> containing the default usage text. When using additional
      * configuration settings in your skills, be sure to override this method to define them with defaults.
      * 
      * @return a default configuration
      */
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = Configuration.getEmptyNode();
         node.setProperty(Setting.USE_TEXT.node(), "%hero% used %skill%!");
         return node;
     }
 
     private void addDelayedSkill(Hero hero, int delay, final String identifier, final String[] args) {
         final Player player = hero.getPlayer();
         delayedSkillUsers.put(player, this);
         broadcast(player.getLocation(), "$1 begins to use $2!", player.getDisplayName(), getName());
         int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 execute(player, identifier, args);
             }
         }, delay * 20 / 1000);
         hero.setDelayedSkillTaskId(taskId);
     }
 
     /**
      * Returns the text to be displayed when the skill is successfully used. This text is pulled from the
      * {@link #SETTING_USETEXT} entry in the skill's configuration during initialization.
      * 
      * @return the usage text
      */
     public String getUseText() {
         return useText;
     }
 
     /**
      * Loads and stores the skill's usage text from the configuration. By default, this text is "%hero% used %skill%!"
      * where %hero% and %skill% are replaced with the Hero's and skill's names, respectively.
      */
     @Override
     public void init() {
         String useText = getSetting(null, Setting.USE_TEXT.node(), "%hero% used %skill%!");
         useText = useText.replace("%hero%", "$1").replace("%skill%", "$2");
         setUseText(useText);
     }
 
     /**
      * Changes the stored usage text. This can be used to override the message found in the skill's configuration.
      * 
      * @param useText
      *            the new usage text
      */
     public void setUseText(String useText) {
         this.useText = useText;
     }
 
     /**
      * The heart of any ActiveSkill, this method defines what actually happens when the skill is used. See
      * {@link #execute(CommandSender, String[]) execute} for a brief explanation of the execution process.
      * 
      * @param hero
      *            the {@link Hero} using the skill
      * @param args
      *            the arguments provided with the command
      * @return <code>true</code> if the skill executed properly, <code>false</code> otherwise
      */
     public abstract boolean use(Hero hero, String[] args);
 
     private void awardExp(Hero hero) {
         if (hero.hasExperienceType(ExperienceType.SKILL)) {
             hero.gainExp(this.getSetting(hero, Setting.EXP.node(), 0, false), ExperienceType.SKILL);
         }
     }
 
     protected void broadcastExecuteText(Hero hero) {
         Player player = hero.getPlayer();
         broadcast(player.getLocation(), getUseText(), player.getDisplayName(), getName());
     }
 
     /**
      * Checks if the player has enough of the specified reagent in their inventory
      * 
      * @param player
      * @param itemStack
      * @return
      */
     protected boolean hasReagentCost(Player player, ItemStack itemStack) {
         int amount = 0;
         for (ItemStack stack : player.getInventory().all(itemStack.getType()).values()) {
             amount += stack.getAmount();
         }
         return amount >= itemStack.getAmount();
     }
     
     /**
      * Gets the currently delayed skill associated with a player
      * @param player
      * @return
      */
     public static Skill getDelayedSkill(Player player) {
         return delayedSkillUsers.get(player);
     }
     
     /**
      * Removes a player from the delayed skill use map
      * @param player
      */
     public static void removeDelayedSkill(Player player) {
         delayedSkillUsers.remove(player);
     }
 }
