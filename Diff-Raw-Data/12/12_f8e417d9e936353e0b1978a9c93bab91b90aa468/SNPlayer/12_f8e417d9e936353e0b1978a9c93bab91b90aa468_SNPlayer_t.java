 package com.TeamNovus.Supernaturals.Player;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.util.BlockIterator;
 
 import com.TeamNovus.Persistence.Annotations.Table;
 import com.TeamNovus.Persistence.Annotations.Columns.Column;
 import com.TeamNovus.Persistence.Annotations.Columns.Id;
 import com.TeamNovus.Persistence.Annotations.Relationships.OneToMany;
 import com.TeamNovus.Supernaturals.SNClasses;
 import com.TeamNovus.Supernaturals.SNEntities;
 import com.TeamNovus.Supernaturals.Entity.SNEntity;
 import com.TeamNovus.Supernaturals.Events.EntityEffectTickEvent;
 import com.TeamNovus.Supernaturals.Events.EntityEffectTriggerEvent;
 import com.TeamNovus.Supernaturals.Events.PlayerClassChangeEvent;
 import com.TeamNovus.Supernaturals.Events.PlayerClassChangeEvent.ChangeClassCause;
 import com.TeamNovus.Supernaturals.Events.PlayerLevelUpEvent;
 import com.TeamNovus.Supernaturals.Events.PlayerManaChangeEvent;
 import com.TeamNovus.Supernaturals.Player.Class.Ability;
 import com.TeamNovus.Supernaturals.Player.Class.Power;
 import com.TeamNovus.Supernaturals.Player.Statistics.Cooldown;
 
 @Table(name = "sn_players")
 public class SNPlayer implements Serializable {
 	private static final long serialVersionUID = 1L;
 			
 	@Id
 	@Column(name = "id")
 	private Integer id;
 	
 	@Column(name = "name")
 	private String name;
 
 	// Data Values:
 	@Column(name = "mana")
 	private Integer mana;
 	
 	@Column(name = "max_mana")
 	private Integer maxMana;
 	
 	@Column(name = "food_level")
 	private Integer foodLevel;
 	
 	@Column(name = "max_food_level")
 	private Integer maxFoodLevel;
 
 	// Class:
 	@Column(name = "player_class_name")
 	private String playerClassName;
 	
 	@Column(name = "binding")
 	private Integer binding;
 
 	// Powers:
 	@OneToMany
 	private List<Cooldown> cooldowns = new ArrayList<Cooldown>();
 	
 	// Leveling:
 	@Column(name = "experience")
 	private Integer experience;
 	
 	@Column(name = "attribute_points")
 	private Integer attributePoints;
 
 	// Stats:
 	@Column(name = "kills")
 	private Integer kills;
 	
 	@Column(name = "deaths")
 	private Integer deaths;
 	
 	// Attributes:
 	private Integer healthAttribute;
 	private Integer healthRegenAttribute;
 	private Integer manaAttribute;
 	private Integer manaRegenAttribute;
 	private Integer speedAttribute;
 	private Integer attackAttribute;
 	private Integer defenseAttribute;
 	
 	@Column(name = "verbose")
 	private Boolean verbose;
 	
 	@Column(name = "gui")
 	private Boolean gui;
 	
 	public SNPlayer() {		
 		// Data Values:
 		this.mana = 0;
 		this.foodLevel = 20;
 
 		this.maxMana = 0;
 		this.maxFoodLevel = 20;
 
 		// Class:
 		this.playerClassName = SNClasses.i.getBaseClass().getName();
 		this.binding = 0;
 
 		// Leveling:
 		this.experience = 0;
 		this.attributePoints = 0;
 
 		// Stats:
 		this.kills = 0;
 		this.deaths = 0;
 		
 		// Attributes:
 		this.healthAttribute = 0;
 		this.healthRegenAttribute = 0;
 		this.manaAttribute = 0;
 		this.manaRegenAttribute = 0;
 		this.speedAttribute = 0;
 		this.attackAttribute = 0;
 		this.defenseAttribute = 0;			
 		
 		this.verbose = true;
 		this.gui = true;
 	}
 	
 	public SNPlayer(Player p) { 
 		this();
 		this.name = p.getName();
 	}
 	
 	/**
 	 * Get the players id
 	 * 
 	 */
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * Set the players id
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Gets the players name.
 	 * 
 	 * @return The players name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the players name.
 	 * 
 	 * @param name - The players new name. 
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Gets the players current mana.
 	 * 
 	 * @return Integer with the players mana.
 	 */
 	public Integer getMana() {
 		return mana;
 	}
 	
 	public void setMana(Integer mana) {
 		setMana(mana, false);
 	}
 
 	/**
 	 * Sets the players current mana.
 	 * 
 	 * @param mana - The players new mana.
 	 */
 	public void setMana(Integer mana, boolean fire) {	
 		if(fire) {
 			PlayerManaChangeEvent event = new PlayerManaChangeEvent(getPlayer(), mana - getMana());
 			
 			Bukkit.getPluginManager().callEvent(event);
 			
 			mana = getMana() + event.getAmount();
 			
 			if(event.isCancelled())
 				return;
 		}
 		
 		this.mana = mana;
 		
 		updateGUI();
 	}
 
 	/**
 	 * Gets the players maximum mana.
 	 * 
 	 * @return The players maximum mana.
 	 */
 	public Integer getMaxMana() {
 		return maxMana;
 	}
 
 	/**
 	 * Sets the players maximum mana.
 	 * 
 	 * @param maxMana - The players new maximum mana.
 	 */
 	public void setMaxMana(Integer maxMana) {
 		this.maxMana = maxMana;
 	}
 
 	/**
 	 * Gets the players health.
 	 * 
 	 * @return The players current health.
 	 */
 	public double getHealth() {
 		return getPlayer().getHealth();
 	}
 
 	/**
 	 * Sets the players health.
 	 * 
 	 * @param health - The players new health.
 	 */
 	public void setHealth(double health) {
 		if(health > getMaxHealth())
 			health = getMaxHealth();
 
 		if(health < 0)
 			health = 0;
 		
 		getPlayer().setHealth(health);
		getPlayer().setHealthScaled(false);
 	}
 
 	/**
 	 * Gets the players maximum health.
 	 * 
 	 * @return The players maximum health.
 	 */
 	public double getMaxHealth() {
 		return getPlayer().getMaxHealth();
 	}
 
 	/**
 	 * Sets the players maximum health.
 	 * 
 	 * @param maxHealth - The players new maximum health.
 	 */
 	public void setMaxHealth(Integer maxHealth) {
 		if(maxHealth <= 0)
 			maxHealth = 20;
 		
 		getPlayer().setMaxHealth(maxHealth);
 	}
 
 	/**
 	 * Gets the players food level.
 	 * 
 	 * @return The players food level.
 	 */
 	public Integer getFoodLevel() {
 		return foodLevel;
 	}
 
 	/**
 	 * Sets the players food level.
 	 * 
 	 * @param foodLevel - The players new food level.
 	 */
 	public void setFoodLevel(Integer foodLevel) {
 		this.foodLevel = foodLevel;
 		
 		updateFoodLevel();
 	}
 
 	/**
 	 * Gets the players maximum food level.
 	 * 
 	 * @return The players maximum food level.
 	 */
 	public Integer getMaxFoodLevel() {
 		return maxFoodLevel;
 	}
 
 	/**
 	 * Sets the players maximum food level.
 	 * 
 	 * @param maxFoodLevel - The players new maximum food level.
 	 */
 	public void setMaxFoodLevel(Integer maxFoodLevel) {
 		this.maxFoodLevel = maxFoodLevel;
 		
 		updateFoodLevel();
 	}
 
 	/**
 	 * Updates the players hunger bar to @foodLevel.
 	 * 
 	 */
 	public void updateFoodLevel() {
 		if(maxFoodLevel <= 0)
 			maxFoodLevel = 20;
 
 		if(foodLevel > maxFoodLevel)
 			foodLevel = maxFoodLevel;
 
 		if(foodLevel < 0)
 			foodLevel = 0;
 		
 		// This synchronizes the players food level to their hunger bar.
 		if(isOnline())
 			getPlayer().setFoodLevel(foodLevel * 20 / maxFoodLevel);
 	}
 
 	/**
 	 * Gets the players walking speed.
 	 * 
 	 * @return The players speed.
 	 */
 	public Float getSpeed() {		
 		return getPlayer().getWalkSpeed();
 	}
 
 	/**
 	 * Sets the players walking speed.
 	 * 
 	 * @param speed - The players new walking speed.
 	 */
 	public void setSpeed(Float speed) {		
 		getPlayer().setWalkSpeed(speed);
 	}
 
 	/**
 	 * Gets the players class.
 	 * 
 	 * @return The players class.
 	 */
 	public SNClass getPlayerClass() {
 		return SNClasses.i.getExactClass(playerClassName);
 	}
 	
 	public String getPlayerClassName() {
 		return playerClassName;
 	}
 
 	public void setPlayerClassName(String playerClassName) {
 		this.playerClassName = playerClassName;
 	}
 	
 	/**
 	 * Sets the players class.
 	 * 
 	 * @param playerClassName - The new player class.
 	 * @param fire - Call the PlayerClassChangeEvent.
 	 */
 	public void setPlayerClass(SNClass playerClass, boolean fire) {
 		if(fire) {
 			PlayerClassChangeEvent event = new PlayerClassChangeEvent(getPlayer(), ChangeClassCause.CODE, getPlayerClass(), playerClass);
 			
 			Bukkit.getPluginManager().callEvent(event);
 			
 			// Change the target class if modified.
 			playerClassName = event.getTo().getName();
 			
 			if(event.isCancelled())
 				return;
 		}
 
 		this.playerClassName = playerClass.getName();
 		
 		syncFields(fire);
 	}
 	
 	/**
 	 * Gets the available classes for the player to join.
 	 * 
 	 * @return The joinable classes for the players current level.
 	 */
 	public List<SNClass> getJoinableClasses() {
 		return getPlayerClass().getJoinableClasses(getLevel());
 	}
 
 	/**
 	 * Gets the players available powers for their current level.
 	 * 
 	 * @return The available powers for their level.
 	 */
 	public List<Power> getPowers() {		
 		return getPlayerClass().getPowers(getLevel());
 	}
 
 	/**
 	 * Gets the players available abilities for their current level.
 	 * 
 	 * @return The available abilities for their level.
 	 */
 	public List<Ability> getAbilities() {
 		return getPlayerClass().getAbilities(getLevel());
 	}
 	
 	/**
 	 * Gets the players power cooldowns.
 	 * 
 	 * @return The players power cooldowns.
 	 */
 	public List<Cooldown> getCooldowns() {		
 		return cooldowns;
 	}
 	
 	public void setCooldowns(ArrayList<Cooldown> cooldowns) {
 		this.cooldowns = cooldowns;
 	}
 	
 	public void setCooldown(Cooldown cooldown) {		
 		cooldowns.add(cooldown);
 	}
 	
 	/**
 	 * Gets the remaining cooldown for a power.
 	 * 
 	 * @param power - The power to check.
 	 * @return The players remaining cooldown.
 	 */
 	public int getRemainingCooldownTicks(Power power) {
 		for(Cooldown cooldown : cooldowns) {
 			if(cooldown.getPower().equals(power.getName())) {	
 				return cooldown.getRemainingTicks();
 			}
 		}
 		
 		return 0;
 	}
 
 	/**
 	 * Synchronizes the players maximum values with the maximum race values for their level.
 	 * 
 	 * @param heal - Determines whether to set the players values to their max values.
 	 */
 	public void syncFields(boolean heal) {
 		setSpeed(getPlayerClass().getSpeed(getLevel()));
 		setMaxMana(getPlayerClass().getMaxMana(getLevel()));
 		setMaxHealth(getPlayerClass().getMaxHealth(getLevel()));
 		setMaxFoodLevel(getPlayerClass().getMaxFoodLevel(getLevel()));
 		
		
 		if(heal) {
 			setMana(getMaxMana(), false);
 			setHealth(getMaxHealth());
 			setFoodLevel(getMaxFoodLevel());
		} else {
			setMana(getMana());
			setHealth(getHealth());
			setFoodLevel(getFoodLevel());
		}		
 	}
 
 	/**
 	 * Gets the players power binding.
 	 * 
 	 * @return The players power binding.
 	 */
 	public Integer getBinding() {
 		return binding;
 	}
 
 	/**
 	 * Sets the players power binding.
 	 * 
 	 * @param binding - The players new power binding.
 	 */
 	public void setBinding(Integer binding) {
 		this.binding = binding;
 	}
 
 	/**
 	 * Sets players binding to the id of the next available power.
 	 * 
 	 */
 	public void setNextBinding() {
 		if (binding + 1 >= getPowers().size()) {
 			binding = 0;
 		} else {
 			binding++;
 		}
 	}
 
 	/**
 	 * Gets the players selected power.
 	 * 
 	 * @return The players selected power.
 	 */
 	public Power getSelectedPower() {
 		try {
 			return getPowers().get(binding);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	// Leveling:
 	/**
 	 * Gets the players experience.
 	 * 
 	 * @return The players experience.
 	 */
 	public Integer getExperience() {
 		return experience;
 	}
 
 	/**
 	 * Sets the players experience.
 	 * 
 	 * @param experience - The players new experience.
 	 */
 	public void setExperience(Integer experience) {
 		int lastLevel = getLevel();
 		
 		this.experience = experience;
 		
 		if(getLevel() > lastLevel && isOnline()) {
 			PlayerLevelUpEvent event = new PlayerLevelUpEvent(getPlayer());
 			
 			Bukkit.getPluginManager().callEvent(event);
 		}	
 		
 		updateGUI();
 	}
 	
 	/**
 	 * Gets the required experience for @level.
 	 * 
 	 * @param level - The level to get the required experience for.
 	 * @return The required experience for @level.
 	 */
 	public Integer getExperienceFor(Integer level) {
 		return 25 * level * level - 25 * level;
 	}
 	
 	/**
 	 * Gets the total required experience for @level.
 	 * 
 	 * @param level - The level to get the total required experience for.
 	 * @return The total required experience for @level.
 	 */
 	public Integer getTotalExperienceFor(Integer level) {
 		int exp = 0;
 		
 		for (int i = 0; i <= level; i++) {
 			exp += 25 * i * i - 25 * i;
 		}
 		
 		return exp;
 	}
 
 	/**
 	 * Gets the players current level based on their experience.
 	 * 
 	 * @return The players current level.
 	 */
 	public Integer getLevel() {
 		int level = 1;
 		
 		while(experience - getTotalExperienceFor(level) > 0) {
 			level++;
 		}
 		
 		return level;		
 	}
 
 	/**
 	 * Set the players level.
 	 * 
 	 * @param level - The new level.
 	 */
 	public void setLevel(Integer level) {
 		setExperience(getExperienceFor(level));
 	}
 	
 	/**
 	 * Get the total number of kills.
 	 * 
 	 * @return - The total number of kills.
 	 */
 	public Integer getKills() {
 		return kills;
 	}
 	
 	/**
 	 * Set the total number of kills.
 	 * 
 	 * @param kills - The new number of kills.
 	 */
 	public void setKills(Integer kills) {
 		this.kills = kills;
 	}
 	
 	public double getKD() {
 		if(kills == 0) {
 			return -deaths;
 		} else if(deaths == 0) {
 			return kills;
 		} else {
 			return 1.0 * kills / deaths;
 		}
 	}
 	
 	/**
 	 * Get the total number of player deaths.
 	 * 
 	 * @return - The total number of player deaths.
 	 */
 	public Integer getDeaths() {
 		return deaths;
 	}
 	
 	/**
 	 * Set the total number of player deaths.
 	 * 
 	 * @param deaths - The total number of player deaths.
 	 */
 	public void setDeaths(Integer deaths) {
 		this.deaths = deaths;
 	}
 
 	/**
 	 * Get the players available attributes points.
 	 * 
 	 * @return The players available attribute points.
 	 */
 	public Integer getAttributePoints() {
 		return attributePoints;
 	}
 
 	/**
 	 * Sets the players attribute points
 	 * 
 	 * @param attributePoints - The players new attribute points.
 	 */
 	public void setAttributePoints(Integer attributePoints) {
 		this.attributePoints = attributePoints;
 	}
 
 	/**
 	 * Gets the players health attribute.
 	 * 
 	 * @return The players health attribute.
 	 */
 	public Integer getHealthAttribute() {
 		return healthAttribute;
 	}
 	
 	/**
 	 * Sets the players health attribute.
 	 * 
 	 * @param healthAttribute - The new health attribute
 	 */
 	public void setHealthAttribute(Integer healthAttribute) {
 		this.healthAttribute = healthAttribute;
 	}
 	
 	/**
 	 * Gets the players health regen attribute.
 	 * 
 	 * @return The players health regen attribute.
 	 */
 	public Integer getHealthRegenAttribute() {
 		return healthRegenAttribute;
 	}
 	
 	/**
 	 * Sets the players health regen attribute.
 	 * 
 	 * @param healthRegenAttribute - The new health regen attribute
 	 */
 	public void setHealthRegenAttribute(Integer healthRegenAttribute) {
 		this.healthRegenAttribute = healthRegenAttribute;
 	}
 	
 	/**
 	 * Gets the players mana attribute.
 	 * 
 	 * @return The players mana attribute.
 	 */
 	public Integer getManaAttribute() {
 		return manaAttribute;
 	}
 	
 	/**
 	 * Sets the players mana attribute.
 	 * 
 	 * @param manaAttribute - The new mana attribute
 	 */
 	public void setManaAttribute(Integer manaAttribute) {
 		this.manaAttribute = manaAttribute;
 	}
 	
 	/**
 	 * Gets the players mana regen attribute.
 	 * 
 	 * @return The players mana regen attribute.
 	 */
 	public Integer getManaRegenAttribute() {
 		return manaRegenAttribute;
 	}
 	
 	/**
 	 * Sets the players mana regen attribute.
 	 * 
 	 * @param manaRegenAttribute - The new mana regen attribute
 	 */
 	public void setManaRegenAttribute(Integer manaRegenAttribute) {
 		this.manaRegenAttribute = manaRegenAttribute;
 	}
 	
 	/**
 	 * Gets the players speed attribute.
 	 * 
 	 * @return The players speed attribute.
 	 */
 	public Integer getSpeedAttribute() {
 		return speedAttribute;
 	}
 	
 	/**
 	 * Sets the players speed attribute.
 	 * 
 	 * @param speedAttribute - The new speed attribute
 	 */
 	public void setSpeedAttribute(Integer speedAttribute) {
 		this.speedAttribute = speedAttribute;
 	}
 	
 	/**
 	 * Gets the players attack attribute.
 	 * 
 	 * @return The players attack attribute.
 	 */
 	public Integer getAttackAttribute() {
 		return attackAttribute;
 	}
 	
 	/**
 	 * Sets the players attack attribute.
 	 * 
 	 * @param attackAttribute - The new attack attribute
 	 */
 	public void setAttackAttribute(Integer attackAttribute) {
 		this.attackAttribute = attackAttribute;
 	}
 	
 	/**
 	 * Gets the players defense attribute.
 	 * 
 	 * @return The players defense attribute.
 	 */
 	public Integer getDefenseAttribute() {
 		return defenseAttribute;
 	}
 	
 	/**
 	 * Sets the players defense attribute.
 	 * 
 	 * @param defenseAttribute - The new defense attribute
 	 */
 	public void setDefenseAttribute(Integer defenseAttribute) {
 		this.defenseAttribute = defenseAttribute;
 	}
 	
 	/**
 	 * Returns true if the player has verbose mode enabled.
 	 * 
 	 * @return True if verbose is true, false otherwise.
 	 */
 	public Boolean isVerbose() {
 		return verbose;
 	}
 	
 	/**
 	 * Sets the players verbose mode.
 	 * 
 	 * @param verbose - The players new verbose mode.
 	 */
 	public void setVerbose(Boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	/**
 	 * Returns true if the player has gui enabled.
 	 * 
 	 * @return True if gui is true, false otherwise.
 	 */
 	public Boolean isUsingGUI() {
 		return gui;
 	}
 	
 	/**
 	 * Sets the players gui toggle.
 	 * 
 	 * @param verbose - The players new gui toggle.
 	 */
 	public void setUsingGUI(Boolean gui) {
 		this.gui = gui;
 	}
 	
 	public void updateGUI() {
 		if(isOffline())
 			return;
 		
 		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
 		Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
 		
 		if(isUsingGUI()) {
 			if(scoreboard.getObjective("stats") == null)
 				scoreboard.registerNewObjective("stats", "dummy");
 			
 			Objective stats = scoreboard.getObjective("stats");
 			stats.setDisplaySlot(DisplaySlot.SIDEBAR);	
 			
 			// Mana Numbers
 			int mana = getMana();
 			int maxMana = getMaxMana();
 			
 			// Exp Numbers
 			int exp = getExperience() - getTotalExperienceFor(getLevel() - 1);
 			int maxExp = getTotalExperienceFor(getLevel()) - getTotalExperienceFor(getLevel() - 1);
 						
 			String manaDisplay = ChatColor.BLUE + "Mana:  " + ChatColor.RESET + new Double((mana * 100.0)/(maxMana * 1.0)).intValue() + "%";
 			String expDisplay = ChatColor.GOLD + "Exp:   " + ChatColor.RESET + new Double((exp * 100.0)/(maxExp * 1.0)).intValue() + "%";
 //			
 //			List<String> effects = new ArrayList<String>();
 //			for (int i = 0; i < getEntity().getEffects().size(); i++) {
 //				Effect effect = getEntity().getEffects().get(i);
 //				
 //				String effectDisplay = effect.getType().toString() + " - " + (effect.getDuration() - effect.getElapsed());
 //				if(effectDisplay.length() > 16) {
 //					effectDisplay = effectDisplay.substring(0, 15);
 //				}
 //				
 //				effects.add(effectDisplay);
 //			}
 //			
 			if(manaDisplay.length() > 16) {
 				manaDisplay = manaDisplay.substring(0, 15);
 			}
 			
 			if(expDisplay.length() > 16) {
 				expDisplay = expDisplay.substring(0, 15);
 			}
 			
 			// Display
 			stats.setDisplayName(" " + getPlayerClass().getColor() + getPlayerClassName() + ChatColor.RED + " " + getLevel() + "/" + getPlayerClass().getMaxLevel());
 			stats.getScore(Bukkit.getOfflinePlayer(manaDisplay)).setScore(2);
 			stats.getScore(Bukkit.getOfflinePlayer(expDisplay)).setScore(1);
 		} else {
 			scoreboard.clearSlot(DisplaySlot.PLAYER_LIST);
 			scoreboard.clearSlot(DisplaySlot.BELOW_NAME);
 			scoreboard.clearSlot(DisplaySlot.SIDEBAR);	
 		}
 		
 		getPlayer().setScoreboard(scoreboard);
 	}
 	
 	/**
 	 * Gets the SNEntity representation of the player. This is 
 	 * used to add or remove entity effects for the player.
 	 * 
 	 * @return The SNEntity of the player.
 	 */
 	public SNEntity getEntity() {
 		return SNEntities.i.get(getPlayer());
 	}
 	
 	public void tick() {
 		if(!(isOnline())) return;
 		
 		Iterator<Ability> abilityIterator = getAbilities().iterator();
 		while (abilityIterator.hasNext()) {
 			Ability ability = abilityIterator.next();
 			
 			Bukkit.getPluginManager().callEvent(new EntityEffectTickEvent(getPlayer(), ability));
 			
 			if (ability.isPeriodic()) {				
 				if (ability.getElapsed() % ability.getPeriod() == 0) {
 					Bukkit.getPluginManager().callEvent(new EntityEffectTriggerEvent(getPlayer(), ability));
 					
 					// Reset elapsed to ensure least amount of integer usage.
 					ability.setElapsed(0);
 				}
 			}
 			
 			ability.setElapsed(ability.getElapsed() + 1);
 		}
 	}
 	
 	public LivingEntity getTargetEntity(Integer range) {
 		if(isOffline()) return null;
 		
 		// Get the nearby entities
 		List<org.bukkit.entity.Entity> near = getPlayer().getNearbyEntities(range, range, range);
 		List<LivingEntity> entities = new ArrayList<LivingEntity>(); 
 		for (org.bukkit.entity.Entity e : near) {
 			if (e instanceof LivingEntity) {
 				entities.add((LivingEntity) e);
 			}
 		}
 
 		// Find the target
 		LivingEntity target = null;		
 		BlockIterator iterator = new BlockIterator(getPlayer(), range);
 
 		Block b;
 		Location l;
 		int bx, by, bz;
 		double ex, ey, ez;
 		// Loop through the players line of sight
 		while (iterator.hasNext()) {
 			b = iterator.next();
 			bx = b.getX();
 			by = b.getY();
 			bz = b.getZ();
 
 			// Check each entity in the range to see if its near the line of sight
 			for (LivingEntity e : entities) {
 				l = e.getLocation();
 				ex = l.getX();
 				ey = l.getY();
 				ez = l.getZ();
 
 				// Check if the entity is near the line of sight
 				if ((bx-.75 <= ex && ex <= bx+1.75) && (bz-.75 <= ez && ez <= bz+1.75) && (by-1 <= ey && ey <= by+2.5)) {
 					target = e;
 					return target;
 				}
 			}
 		}
 
 		return null;
 	}
 	
 	// Bukkit:
 	public Player getPlayer() {
 		return Bukkit.getPlayerExact(name);
 	}
 
 	public Boolean isOnline() {
 		return getPlayer() != null;
 	}	
 
 	public Boolean isOffline() {
 		return !(isOnline());
 	}
 
 	public void sendMessage(String message) {
 		if (isOnline()) {
 			getPlayer().sendMessage(message);
 		}
 	}
 
 	public void sendMessage(String[] messages) {
 		for (String message : messages) {
 			sendMessage(message);
 		}
 	}
 
 	// Java:
 	public String toString() {
 		return "SNPlayer [id=" + id + ", name=" + name + ", mana=" + mana
 				+ ", maxMana=" + maxMana + ", foodLevel=" + foodLevel
 				+ ", maxFoodLevel=" + maxFoodLevel + ", playerClassName="
 				+ playerClassName + ", binding=" + binding + ", cooldowns="
 				+ cooldowns + ", experience=" + experience
 				+ ", attributePoints=" + attributePoints + ", healthAttribute="
 				+ healthAttribute + ", healthRegenAttribute="
 				+ healthRegenAttribute + ", manaAttribute=" + manaAttribute
 				+ ", manaRegenAttribute=" + manaRegenAttribute
 				+ ", speedAttribute=" + speedAttribute + ", attackAttribute="
 				+ attackAttribute + ", defenseAttribute=" + defenseAttribute
 				+ ", verbose=" + verbose + ", gui=" + gui + "]";
 	}
 	
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		SNPlayer other = (SNPlayer) obj;
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		return true;
 	}
 }
