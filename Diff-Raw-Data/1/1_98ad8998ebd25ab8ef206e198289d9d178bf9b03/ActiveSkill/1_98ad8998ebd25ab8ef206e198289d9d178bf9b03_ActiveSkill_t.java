 package com.herocraftonline.dev.heroes.skill;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillCompleteEvent;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.api.SkillResult.ResultType;
 import com.herocraftonline.dev.heroes.api.SkillUseEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.hero.HeroManager;
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
         if (!(sender instanceof Player)) {
             return false;
         }
 
         String name = this.getName();
         Player player = (Player) sender;
         HeroManager hm = plugin.getHeroManager();
         Hero hero = hm.getHero(player);
         if (hero == null) {
             Messaging.send(player, "You are not a hero.");
             return false;
         }
         HeroClass heroClass = hero.getHeroClass();
         HeroClass secondClass = hero.getSecondClass();
         if (!heroClass.hasSkill(name) && (secondClass == null || !secondClass.hasSkill(name))) {
             Messaging.send(player, "Your classes don't have the skill: $1.", name);
             return true;
         }
         int level = SkillConfigManager.getUseSetting(hero, this, Setting.LEVEL, 1, true);
         if (hero.getSkillLevel(this) < level) {
             messageAndEvent(hero, new SkillResult(ResultType.LOW_LEVEL, true, level));
             return true;
         }
 
         long time = System.currentTimeMillis();
         Long global = hero.getCooldown("global");
         if (global != null && time < global) {
             messageAndEvent(hero, new SkillResult(ResultType.ON_GLOBAL_COOLDOWN, true, (global - time) / 1000));
             return true;
         }
         int skillLevel = hero.getSkillLevel(this);
         int cooldown = SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN, 0, true);
         double coolReduce = SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE, 0.0, false) * skillLevel;
         cooldown -= (int) coolReduce;
         if (cooldown > 0) {
             Long expiry = hero.getCooldown(name);
             if (expiry != null && time < expiry) {
                 long remaining = expiry - time;
                 messageAndEvent(hero, new SkillResult(ResultType.ON_COOLDOWN, true, name, remaining / 1000));
                 return false;
             }
         }
         int manaCost = SkillConfigManager.getUseSetting(hero, this, Setting.MANA, 0, true);
         double manaReduce = SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE, 0.0, false) * skillLevel;
         manaCost -= (int) manaReduce;
 
         // Reagent stuff
         ItemStack itemStack = getReagentCost(hero);
 
         int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, true);
         double healthReduce = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, 0.0, false) * skillLevel;
         healthCost -= (int) healthReduce;
         int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA, 0, true);
         double stamReduce = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE, 0.0, false) * skillLevel;
         staminaCost -= (int) stamReduce;
 
         SkillUseEvent skillEvent = new SkillUseEvent(this, player, hero, manaCost, healthCost, staminaCost, itemStack, args);
         plugin.getServer().getPluginManager().callEvent(skillEvent);
         if (skillEvent.isCancelled()) {
             messageAndEvent(hero, SkillResult.CANCELLED);
             return true;
         }
 
         // Update manaCost with result of SkillUseEvent
         manaCost = skillEvent.getManaCost();
         if (manaCost > hero.getMana()) {
             messageAndEvent(hero, SkillResult.LOW_MANA);
             return true;
         }
 
         // Update healthCost with results of SkillUseEvent
         healthCost = skillEvent.getHealthCost();
         if (healthCost > 0 && hero.getHealth() <= healthCost) {
             messageAndEvent(hero, SkillResult.LOW_HEALTH);
             return true;
         }
 
         //Update staminaCost with results of SkilluseEvent
         staminaCost = skillEvent.getStaminaCost();
         if (staminaCost > 0 && hero.getPlayer().getFoodLevel() < staminaCost) {
             messageAndEvent(hero, SkillResult.LOW_STAMINA);
             return true;
         }
 
         itemStack = skillEvent.getReagentCost();
         if (itemStack != null && itemStack.getAmount() != 0 && !hasReagentCost(player, itemStack)) {
             String reagentName = itemStack.getType().name().toLowerCase().replace("_", " ");
             messageAndEvent(hero, new SkillResult(ResultType.MISSING_REAGENT, true, String.valueOf(itemStack.getAmount()), reagentName));
             return true;
         }
 
         int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY, 0, true);
         DelayedSkill dSkill = null;
         if (delay > 0 && !hm.getDelayedSkills().containsKey(hero)) {
             if (addDelayedSkill(hero, delay, identifier, args)) {
                 messageAndEvent(hero, SkillResult.START_DELAY);
                 if (Heroes.properties.slowCasting) {
                     hero.addEffect(new SlowEffect(this, "Casting", delay, 2, false, "", "", hero));
                 }
                 return true;
             } else {
                 // Generic return if the adding of the delayed skill failed - the failure should send it's own message
                 return true;
             }
         } else if (hm.getDelayedSkills().containsKey(hero)) {
             dSkill = hm.getDelayedSkills().get(hero);
             if (!dSkill.getSkill().equals(this)) {
                 hm.getDelayedSkills().remove(hero);
                 hero.setDelayedSkill(null);
                 hero.removeEffect(hero.getEffect("Casting"));
                 broadcast(player.getLocation(), "$1 has stopped using $2!", player.getDisplayName(), dSkill.getSkill().getName());
                 //If the new skill is also a delayed skill lets add it to the warmups and proceed
                 if (delay > 0) {
                     addDelayedSkill(hero, delay, identifier, args);
                     if (Heroes.properties.slowCasting) {
                         hero.addEffect(new SlowEffect(this, "Casting", delay, 2, false, "", "", hero));
                     }
                     messageAndEvent(hero, SkillResult.START_DELAY);
                     return true;
                 }
                dSkill = null;
             } else if (!dSkill.isReady()) {
                 Messaging.send(sender, "You have already begun to use that skill!");
                 return true;
             } else {
                 hero.removeEffect(hero.getEffect("Casting"));
                 hm.addCompletedSkill(hero);
             }
         }
 
         SkillResult skillResult;
         if (dSkill instanceof DelayedTargettedSkill) {
             skillResult = ((TargettedSkill) this).useDelayed(hero, ((DelayedTargettedSkill) dSkill).getTarget(), args);
         } else {
             skillResult = use(hero, args);
         }
 
         if (skillResult.type == ResultType.NORMAL){
             time = System.currentTimeMillis();
             // Set cooldown
             if (cooldown > 0) {
                 hero.setCooldown(name, time + cooldown);
             }
 
             if (Heroes.properties.globalCooldown > 0) {
                 hero.setCooldown("global", Heroes.properties.globalCooldown + time);
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
 
             if (staminaCost > 0) {
                 player.setFoodLevel(player.getFoodLevel() - staminaCost);
             }
 
             // Only charge the item cost if it's non-null
             if (itemStack != null && itemStack.getAmount() > 0) {
                 player.getInventory().removeItem(itemStack);
                 player.updateInventory();
             }
 
         }
         messageAndEvent(hero, skillResult);
         return true;
     }
 
     /**
      * Creates and returns a <code>ConfigurationNode</code> containing the default usage text. When using additional
      * configuration settings in your skills, be sure to override this method to define them with defaults.
      * 
      * @return a default configuration
      */
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection section = super.getDefaultConfig();
         section.set(Setting.USE_TEXT.node(), "%hero% used %skill%!");
         return section;
     }
 
     protected boolean addDelayedSkill(Hero hero, int delay, final String identifier, final String[] args) {
         final Player player = hero.getPlayer();
         DelayedSkill dSkill = new DelayedSkill(identifier, player, delay, this, args);
         broadcast(player.getLocation(), "$1 begins to use $2!", player.getDisplayName(), getName());
         plugin.getHeroManager().getDelayedSkills().put(hero, dSkill);
         hero.setDelayedSkill(dSkill);
         return true;
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
         String useText = SkillConfigManager.getRaw(this, Setting.USE_TEXT, "%hero% used %skill%!");
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
      * @return {@link SkillResult} if skill completed normally, or with an execution error
      */
     public abstract SkillResult use(Hero hero, String[] args);
 
     private void awardExp(Hero hero) {
         if (hero.canGain(ExperienceType.SKILL)) {
             hero.gainExp(SkillConfigManager.getUseSetting(hero, this, Setting.EXP, 0, false), ExperienceType.SKILL);
         }
     }
 
     protected void broadcastExecuteText(Hero hero) {
         Player player = hero.getPlayer();
         broadcast(player.getLocation(), getUseText(), player.getDisplayName(), getName());
     }
 
     /**
      * Uses the {@link SkillResult} arguments to display a message to the skill-user or ignore the message altogether
      * 
      * @param hero - {@link Hero} using the skill
      * @param sr - the {@link SkillResult} that holds the {@link ResultType} and information
      */
     private void messageAndEvent(Hero hero, SkillResult sr) {
         Player player = hero.getPlayer();
         if (sr.showMessage)
             switch (sr.type) {
             case INVALID_TARGET:
                 Messaging.send(player, "Invalid Target!");
                 break;
             case LOW_HEALTH:
                 Messaging.send(player, "Not enough health!");
                 break;
             case LOW_LEVEL:
                 Messaging.send(player, "You must be level $1 to do that.", sr.args[0]);
                 break;
             case LOW_MANA: 
                 Messaging.send(player, "Not enough mana!");
                 break;
             case LOW_STAMINA:
                 Messaging.send(player, "You are too fatigued!");
                 break;
             case ON_COOLDOWN:
                 Messaging.send(hero.getPlayer(), "Sorry, $1 still has $2 seconds left on cooldown!", sr.args[0], sr.args[1]);
                 break;
             case ON_GLOBAL_COOLDOWN:
                 Messaging.send(hero.getPlayer(), "Sorry, you must wait $1 seconds longer before using another skill.", sr.args[0]);
                 break;
             case MISSING_REAGENT:
                 Messaging.send(player, "Sorry, you need to have $1 $2 to use $3!", sr.args[0], sr.args[1], getName());
                 break;
             case SKIP_POST_USAGE:
                 return;
             default: 
                 break;
             }
 
         SkillCompleteEvent sce = new SkillCompleteEvent(hero, this, sr);
         plugin.getServer().getPluginManager().callEvent(sce);
     }
 }
