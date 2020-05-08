 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.comphenix.xp.expressions.ParameterProviderSet;
 import com.comphenix.xp.listeners.ErrorReporting;
 import com.comphenix.xp.listeners.PlayerCleanupListener;
 import com.comphenix.xp.lookup.*;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.messages.MessagePlayerQueue;
 import com.comphenix.xp.parser.ActionParser;
 import com.comphenix.xp.parser.StringListParser;
 import com.comphenix.xp.parser.Utility;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.sections.*;
 import com.comphenix.xp.parser.text.ItemParser;
 import com.comphenix.xp.parser.text.MobParser;
 import com.comphenix.xp.parser.text.PlayerParser;
 import com.comphenix.xp.rewards.ResourceFactory;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.comphenix.xp.rewards.RewardTypes;
 
 public class Configuration implements PlayerCleanupListener, Multipliable<Configuration> {
 			
 	private static final String MULTIPLIER_SETTING = "multiplier";
 	private static final String DISABLE_REWARDS_SETTING = "default rewards disabled";
 	private static final String REWARD_TYPE_SETTING = "reward type";
 	
 	private static final String ECONOMY_DROPS_SETTING = "economy drop";
 	private static final String ECONOMY_WORTH_SETTING = "economy drop worth";
 	private static final String VIRTUAL_SCAN_RADIUS_SETTING = "virtual scan radius";
 	
 	private static final String DEFAULT_CHANNELS_SETTING = "default channels";
 	private static final String MESSAGE_MAX_RATE_SETTING = "message max rate";
 	
 	private static final String MAXIMUM_ENCHANT_LEVEL_SETTING = "maximum enchant level";
 	private static final String MAXIMUM_BOOKCASE_COUNT_SETTING = "maximum bookcase count";
 	
 	public static final int DEFAULT_ECONOMY_WORTH = 1;
 	public static final ItemStack DEFAULT_ECONOMY_DROP = null;
 	public static final boolean DEFAULT_DISABLE_REWARDS = false;
 	public static final double DEFAULT_SCAN_RADIUS = 20;
 	public static final int DEFAULT_MESSAGE_RATE = 5;
 	public static final int DEFAULT_MAXIMUM_ENCHANT_LEVEL = 30;
 	public static final int DEFAULT_MAXIMUM_BOOKCASE_COUNT = 15;
 	public static final int MAXIMUM_BOOKCASE_COUNT = 255; 
 	
 	private Debugger logger;
 	
 	private double multiplier;
 	private boolean defaultRewardsDisabled;
 	private boolean preset;
 	
 	private ItemStack economyDropItem;
 	private Integer economyItemWorth;
 	private double scanRadiusSetting;
 	
 	private RewardProvider rewardProvider;
 	private ChannelProvider channelProvider;
 	private ParameterProviderSet parameterProviders;
 	private MessagePlayerQueue messageQueue;
 
 	private LevelingRate levelingRate;
 	
 	private int maximumEnchantLevel;
 	private int maximumBookcaseCount;
 	
 	// Global settings
 	private GlobalSettings globalSettings;
 
 	// Every action/trigger type
 	private ActionTypes actionTypes;
 	
 	// Every standard reward
 	private Map<Integer, ItemTree> actionRewards = new HashMap<Integer, ItemTree>();
 	private Map<Integer, PotionTree> complexRewards = new HashMap<Integer, PotionTree>();
 	
 	private PlayerTree playerDeathDrop;
 	private MobTree experienceDrop;
 	private PlayerRewards playerRewards;
 	
 	private ItemParser itemParser;
 	private MobParser mobParser;
 	private ActionParser actionParser;
 	private PlayerParser playerParser;
 	
 	public Configuration(Debugger debugger, ActionTypes actionTypes) {
 		this.logger = debugger;
 		this.defaultRewardsDisabled = false;
 		this.actionTypes = actionTypes;
 		initialize(1);
 	}
 	
 	public Configuration(Configuration other, double newMultiplier) {
 		if (other == null)
 			throw new IllegalArgumentException("other");
 		
 		// Copy (and change) scalars
 		this.multiplier = newMultiplier;
 		this.logger = other.logger;
 		
 		this.maximumEnchantLevel = other.maximumEnchantLevel;
 		this.maximumBookcaseCount = other.maximumBookcaseCount;
 		this.defaultRewardsDisabled = other.defaultRewardsDisabled;
 		this.economyItemWorth = other.economyItemWorth;
 		this.economyDropItem = other.economyDropItem;
 		this.scanRadiusSetting = other.scanRadiusSetting;
 		
 		this.parameterProviders = other.parameterProviders;
 		this.rewardProvider = other.rewardProvider;
 		this.channelProvider = other.channelProvider;
 		this.actionParser = other.actionParser;
 		this.messageQueue = other.messageQueue;
 		this.initializeReferences();
 		
 		// Copy parsers
 		this.itemParser = other.itemParser;
 		this.mobParser = other.mobParser;
 		this.actionTypes = other.actionTypes;
 		this.playerParser = other.playerParser;
 		
 		// Copy (shallow) trees
 		this.actionRewards = copyActionsWithMultiplier(other.actionRewards, newMultiplier);
 		this.complexRewards = copyActionsWithMultiplier(other.complexRewards, newMultiplier);
 		this.experienceDrop = other.experienceDrop.withMultiplier(newMultiplier);
 		this.playerDeathDrop = other.playerDeathDrop.withMultiplier(newMultiplier);
 		this.playerRewards = other.playerRewards.withMultiplier(newMultiplier);
 		this.levelingRate = other.levelingRate;
 		this.checkRewards();
 	}
 		
 	public Configuration(Debugger debugger, RewardProvider provider, ChannelProvider channels) {
 		this.logger = debugger;
 		this.rewardProvider = provider;
 		this.channelProvider = channels;
 		this.actionParser = new ActionParser(provider);
 		this.playerParser = new PlayerParser();
 	}
 	
 	// Makes a copy of a action reward tree
 	private static <TParam extends Multipliable<TParam>> Map<Integer, TParam> copyActionsWithMultiplier(
 			Map<Integer, TParam> rewards, double newMultiplier) {
 		
 		Map<Integer, TParam> copy = new HashMap<Integer, TParam>();
 		
 		// Treat null as empty
 		if (rewards == null)
 			return copy;
 		
 		for (Entry<Integer, TParam> entry : rewards.entrySet()) {
 			copy.put(entry.getKey(), entry.getValue().withMultiplier(newMultiplier));
 		}
 		
 		return copy;
 	}
 
 	// Merges action reward trees
 	private static <TParam, TTree extends ActionTree<TParam>> void mergeActions(
 				Map<Integer, TTree> destination, Map<Integer, TTree> source) {
 		
 		// Merge all trees
 		for (Entry<Integer, TTree> entry : source.entrySet()) {
 			TTree tree = destination.get(entry.getKey());
 			
 			Integer key = entry.getKey();
 			TTree value = entry.getValue();
 			
 			if (tree == null)
 				destination.put(key, value);
 			else
 				destination.get(key).putAll(value);
 		}
 	}
 	
 	private void initializeReferences() {
 		
 		if (rewardProvider != null) {
 			rewardProvider = rewardProvider.createView(this);
 		}
 		if (channelProvider != null) {
 			channelProvider = channelProvider.createView();
 		}
 		if (messageQueue != null) {
 			messageQueue = messageQueue.createView();
 		}
 		if (actionParser != null) {
 			actionParser = actionParser.createView(rewardProvider);
 		}
 	}
 	
 	/**
 	 * Merge a list of configurations into a new configuration.
 	 * 
 	 * @param configurations - list of configurations.
 	 * @param debugger - debugger instance.
 	 * @return Merged configuration.
 	 */
 	public static Configuration fromMultiple(List<Configuration> configurations, Debugger debugger) {
 		
 		if (configurations == null)
 			return null;
 		else if (configurations.size() == 0)
 			return null;
 		else if (configurations.size() == 1)
 			return configurations.get(0);
 		
 		Configuration copy = null;
 		
 		// Merge everything in order
 		for (Configuration config : configurations) {
 			
 			// Initialize first time around
 			if (copy == null) {
 				copy = new Configuration(debugger, config.actionTypes);
 			}
 			
 			copy.experienceDrop.putAll(config.experienceDrop);
 			copy.playerDeathDrop.putAll(config.playerDeathDrop);
 			copy.playerRewards.putAll(config.playerRewards);
 			copy.levelingRate.putAll(config.levelingRate);
 			mergeActions(copy.actionRewards, config.actionRewards);
 			mergeActions(copy.complexRewards, config.complexRewards);
 			
 			// This will be the last set non-standard value
 			if (config.defaultRewardsDisabled != DEFAULT_DISABLE_REWARDS)
 				copy.defaultRewardsDisabled = config.defaultRewardsDisabled;
 			if (config.maximumEnchantLevel != DEFAULT_MAXIMUM_ENCHANT_LEVEL)
 				copy.maximumEnchantLevel = config.maximumEnchantLevel;
 			if (config.maximumBookcaseCount != DEFAULT_MAXIMUM_BOOKCASE_COUNT)
 				copy.maximumBookcaseCount = config.maximumBookcaseCount;
 			if (config.scanRadiusSetting != DEFAULT_SCAN_RADIUS)
 				copy.scanRadiusSetting = config.scanRadiusSetting;
 			if (config.economyItemWorth != DEFAULT_ECONOMY_WORTH)
 				copy.economyItemWorth = config.economyItemWorth;
 			if (!ObjectUtils.equals(config.economyDropItem, DEFAULT_ECONOMY_DROP))
 				copy.economyDropItem = config.economyDropItem;
			if (copy.messageQueue == null || !isStandardQueue(config.messageQueue)) 
				copy.messageQueue = config.messageQueue;	
			
 			copy.rewardProvider = config.rewardProvider;
 			copy.channelProvider = config.channelProvider;
 			copy.parameterProviders = config.parameterProviders;
 			copy.itemParser = config.itemParser;
 			copy.mobParser = config.mobParser;
 			copy.actionParser = config.actionParser;
 			copy.playerParser = config.playerParser;
 			
 			// Multiply all multipliers
 			copy.multiplier *= config.multiplier; 
 		}
 		
 		// Update multiplier
 		return new Configuration(copy, copy.multiplier);
 	}
 
	// See if this is a default message queue
	private static boolean isStandardQueue(MessagePlayerQueue queue) {
		return queue != null && (queue.getMessageDelay() / 1000) != DEFAULT_MESSAGE_RATE;
	}
	
 	/**
 	 * Initialize configuration from a configuration section.
 	 * @param config - configuration section to load from.
 	 */
 	public void loadFromConfig(ConfigurationSection config) {
 		
 		// Load scalar values first
 		if (config.isDouble(MULTIPLIER_SETTING))
 			multiplier = config.getDouble(MULTIPLIER_SETTING, 1);
 		else
 			multiplier = config.getInt(MULTIPLIER_SETTING, 1);
 				
 		// Initialize parsers
 		MobSectionParser mobsParser = new MobSectionParser(
 				actionParser, mobParser, parameterProviders, multiplier);
 		PlayerDeathSectionParser playerDeathParser = new PlayerDeathSectionParser(
 				actionParser, playerParser, parameterProviders, multiplier);
 		ItemsSectionParser itemsParser = new ItemsSectionParser(
 				itemParser, actionParser, actionTypes, parameterProviders, multiplier);
 		PlayerSectionParser playerParser = new PlayerSectionParser(
 				actionParser, parameterProviders, multiplier);
 		LevelsSectionParser levelsParser = new LevelsSectionParser();
 		
 		// Set debugger
 		mobsParser.setDebugger(logger);
 		itemsParser.setDebugger(logger);
 		playerParser.setDebugger(logger);
 		levelsParser.setDebugger(logger);
 		playerDeathParser.setDebugger(logger);
 		
 		// Enchanting settings
 		maximumEnchantLevel = config.getInt(MAXIMUM_ENCHANT_LEVEL_SETTING, DEFAULT_MAXIMUM_ENCHANT_LEVEL);
 		maximumBookcaseCount = config.getInt(MAXIMUM_BOOKCASE_COUNT_SETTING, DEFAULT_MAXIMUM_BOOKCASE_COUNT);
 		
 		// There's a limit to things
 		if (maximumBookcaseCount > MAXIMUM_BOOKCASE_COUNT) {
 			maximumBookcaseCount = MAXIMUM_BOOKCASE_COUNT;
 			logger.printWarning(this, "Maximum bookcase count cannot exceed 255.");
 		}
 		
 		// Whether or not to remove all default XP drops
 		defaultRewardsDisabled = config.getBoolean(DISABLE_REWARDS_SETTING, DEFAULT_DISABLE_REWARDS);
 		scanRadiusSetting = readDouble(config, VIRTUAL_SCAN_RADIUS_SETTING, DEFAULT_SCAN_RADIUS);
 		
 		// Economy item settings
 		economyItemWorth = config.getInt(ECONOMY_WORTH_SETTING, DEFAULT_ECONOMY_WORTH);
 		economyDropItem = DEFAULT_ECONOMY_DROP;
 
 		// Economy drop item
 		try {
 			String text = config.getString(ECONOMY_DROPS_SETTING, null);
 			Query drop = text != null ? itemParser.parse(text) : null;
 			
 			if (drop != null && drop instanceof ItemQuery) {
 				economyDropItem = ((ItemQuery) drop).toItemStack(1);
 			}
 			
 		} catch (ParsingException e) {
 			logger.printWarning(this, "Cannot load economy drop type: %s", e.getMessage());
 		}
 		
 		// Default message channels
 		StringListParser listParser = new StringListParser();
 		channelProvider.setDefaultChannels(listParser.parseSafe(config, DEFAULT_CHANNELS_SETTING));
 		
 		// Load reward type
 		String defaultReward = loadReward(config.getString(REWARD_TYPE_SETTING, null));
 		
 		// Use default type if nothing has been set
 		if (defaultReward != null)
 			setDefaultRewardName(defaultReward);
 	
 		loadRate(config);
 		
 		try {
 			// Load mob experience
 			experienceDrop = mobsParser.parse(config, "mobs");
 			
 			// Load player death experience
 			playerDeathDrop = playerDeathParser.parse(config, "player death");
 			
 			// Load items and potions
 			ItemsSectionResult result = itemsParser.parse(config, "items");
 			actionRewards = result.getActionRewards();
 			complexRewards = result.getComplexRewards();
 			
 			// Load player rewards
 			playerRewards = playerParser.parse(config, "player");
 			
 			// Load custom levels
 			levelingRate = levelsParser.parse(config, "levels");
 			
 		} catch (ParsingException e) {
 			// This must be because a debugger isn't attached. Damn it.
 			ErrorReporting.DEFAULT.reportError(logger, this, e);
 		}
 		
 		// Reload providers
 		initializeReferences();
 		checkRewards();
 	}
 	
 	private void loadRate(ConfigurationSection config) {
 		
 		// Load the message queue
 		double rate = readDouble(config, MESSAGE_MAX_RATE_SETTING, DEFAULT_MESSAGE_RATE);
 		long converted = 0;
 		
 		// Make sure the rate is valid
 		if (rate * 1000 > Long.MAX_VALUE)
 			logger.printWarning(this, "Message rate cannot be bigger than %d", Long.MAX_VALUE / 1000);
 		else if (rate < 0)
 			logger.printWarning(this, "Message rate cannot be negative.");
 		else
 			converted = (long) (rate * 1000);
 		
 		// Always create a message queue
 		messageQueue = new MessagePlayerQueue(converted, channelProvider, logger);
 	}
 	
 	private void initialize(double multiplier) {
 		
 		// Clear previous values
 		experienceDrop = new MobTree(multiplier);
 		playerDeathDrop = new PlayerTree(multiplier);
 		
 		// Initialize all the default rewards
 		for (Integer types : actionTypes.getTypes()) {
 			actionRewards.put(types, new ItemTree(multiplier));
 			complexRewards.put(types, new PotionTree(multiplier));
 		}
 		
 		playerParser = new PlayerParser();
 		playerRewards = new PlayerRewards(multiplier);
 		this.levelingRate = new LevelingRate();
 		this.multiplier = multiplier;
 	}
 	
 	public Collection<Action> getActions() { 
 		
 		List<Action> actions = new ArrayList<Action>();
 		
 		// Add every simple action
 		for (Integer types : actionTypes.getTypes()) {
 			ItemTree items = getActionReward(types);
 			PotionTree potions = getComplexReward(types);
 			
 			if (items != null) 
 				actions.addAll(items.getValues());
 			if (potions != null)
 				actions.addAll(potions.getValues());
 		}
 		
 		// Copy the content of every special collection
 		actions.addAll(experienceDrop.getValues());
 		actions.addAll(playerDeathDrop.getValues());
 		actions.addAll(playerRewards.getValues());
 		return actions;
 	}
 	
 	private void checkRewards() {
 		
 		Collection<Action> actions = getActions();
 		
 		// Are any rewards negative
 		if (hasNegativeRewards(actions)) {
 			logger.printWarning(this, 
 					"Cannot use negative rewards with the experience reward type.");
 		}
 		
 		// Economy rewards when no economy is registered
 		if (rewardProvider.getByEnum(RewardTypes.ECONOMY) == null &&
 			hasEconomyReward(actions)) {
 			logger.printWarning(this, "VAULT was not found. Cannot use economy.");
 		}
 	}
 
 	private boolean hasEconomyReward(Collection<Action> values) {
 		
 		// See if we have an economy reward set
 		for (Action action : values) {
 			if (action.getReward("ECONOMY") != null)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean hasNegativeRewards(Collection<Action> values) {
 		
 		// Check every range
 		for (Action action : values) {
 			
 			ResourceFactory factory = action.getReward("EXPERIENCE");
 				
 			if (factory != null && (factory.getMinimum(null, 1).getAmount() < 0))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private String loadReward(String text) {
 	
 		String parsing = Utility.getEnumName(text);
 			
 		// Load reward name
 		if (text != null && rewardProvider.containsService(parsing)) {
 			return parsing;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Reads a double or integer from the configuration section.
 	 * @param config - configuration section to read from.
 	 * @param key - the key to read.
 	 * @return The double, or NULL if none were found.
 	 */
 	private double readDouble(ConfigurationSection config, String key, double defaultValue) {
 		
 		if (config.isDouble(key))
 			return config.getDouble(key);
 		else if (config.isInt(key)) 
 			return (double) config.getInt(key);
 		else 
 			return defaultValue;
 	}
 	
 	/**
 	 * Whether or not this configuration is associated with a specified preset.
 	 * @return TRUE if it is, FALSE otherwise. 
 	 */
 	public boolean hasPreset() {
 		return preset;
 	}
 	
 	/**
 	 * Whether or not this configuration is associated with a specified preset.
 	 * @param value - new value.
 	 */
 	public void setPreset(boolean value) {
 		preset = value;
 	}
 	
 	public GlobalSettings getGlobalSettings() {
 		return globalSettings;
 	}
 
 	public void setGlobalSettings(GlobalSettings globalSettings) {
 		this.globalSettings = globalSettings;
 	}
 	
 	/**
 	 * Overrides the rate at which players gain levels.
 	 * @return Custom rules for how much experience a player needs to level up.
 	 */
 	public LevelingRate getLevelingRate() {
 		return levelingRate;
 	}
 
 	@Override
 	public Configuration withMultiplier(double newMultiplier) {
 		return new Configuration(this, newMultiplier);
 	}
 
 	@Override
 	public double getMultiplier() {
 		return multiplier;
 	}
 
 	public MobTree getExperienceDrop() {
 		return experienceDrop;
 	}
 
 	/**
 	 * Retrieves the rewards for the given action or trigger.
 	 * @param actionID - unique ID for the given action.
 	 * @return Tree of every associated reward.
 	 */
 	public ItemTree getActionReward(Integer actionID) {
 		return actionRewards.get(actionID);
 	}
 	
 	/**
 	 * Retrieves the rewards for the given action or trigger.
 	 * @param action - name for the given action.
 	 * @return Tree of every associated reward.
 	 */
 	public ItemTree getActionReward(String action) {
 		return getActionReward(actionTypes.getType(action));
 	}
 
 	/**
 	 * Sets the tree of rewards for a given action.
 	 * @param actionID - unique ID for the given action.
 	 * @return Previously associated tree of rewards, or NULL if no tree was associated.
 	 */
 	public ItemTree setActionReward(Integer actionID, ItemTree tree) {
 		return actionRewards.put(actionID, tree);
 	}
 	
 	/**
 	 * Retrieves the complex potion rewards for the given action or trigger.
 	 * @param actionID - unique ID for the given action.
 	 * @return Tree of every associated reward.
 	 */
 	public PotionTree getComplexReward(Integer actionID) {
 		return complexRewards.get(actionID);
 	}
 	
 	/**
 	 * Retrieves the complex potion rewards for the given action or trigger.
 	 * @param action - name for the given action.
 	 * @return Tree of every associated reward.
 	 */
 	public PotionTree getComplexReward(String action) {
 		return getComplexReward(actionTypes.getType(action));
 	}
 	
 	/**
 	 * Sets the tree of rewards for a given action.
 	 * @param actionID - unique ID for the given action.
 	 * @return Previously associated tree of rewards, or NULL if no tree was associated.
 	 */
 	public PotionTree setComplexReward(Integer actionID, PotionTree tree) {
 		return complexRewards.put(actionID, tree);
 	}
 	
 	public boolean isDefaultRewardsDisabled() {
 		return defaultRewardsDisabled;
 	}
 	
 	public int getMaximumEnchantLevel() {
 		return maximumEnchantLevel;
 	}
 
 	public int getMaximumBookcaseCount() {
 		return maximumBookcaseCount;
 	}
 
 	public PlayerRewards getPlayerRewards() {
 		return playerRewards;
 	}
 	
 	public RewardProvider getRewardProvider() {
 		return rewardProvider;
 	}
 	
 	public ChannelProvider getChannelProvider() {
 		return channelProvider;
 	}
 	
 	public void setRewardManager(RewardProvider rewardProvider) {
 		this.rewardProvider = rewardProvider;
 	}
 	
 	public MessagePlayerQueue getMessageQueue() {
 		return messageQueue;
 	}
 	
 	public ItemStack getEconomyDropItem() {
 		return economyDropItem;
 	}
 
 	public Integer getEconomyItemWorth() {
 		return economyItemWorth;
 	}
 	
 	public String getDefaultRewardName() {
 		if (rewardProvider == null)
 			return null;
 		else
 			return rewardProvider.getDefaultName();
 	}
 	
 	public void setDefaultRewardName(String rewardName) {
 		if (rewardProvider != null) {
 			rewardProvider.setDefaultName(rewardName);
 		}
 	}
 	
 	public ItemTree getSimpleBlockReward() {
 		return getActionReward(actionTypes.getType(ActionTypes.BLOCK));
 	}
 	
 	public ItemTree getSimpleBonusReward() {
 		return getActionReward(actionTypes.getType(ActionTypes.BONUS));
 	}
 	
 	public ItemTree getSimpleBrewingReward() {
 		return getActionReward(actionTypes.getType(ActionTypes.BREWING));
 	}
 	
 	public ItemTree getSimpleCraftingReward() {
 		return getActionReward(actionTypes.getType(ActionTypes.CRAFTING));
 	}
 	
 	public ItemTree getSimplePlacingReward() {
 		return getActionReward(actionTypes.getType(ActionTypes.PLACE));
 	}
 	
 	public ItemTree getSimpleSmeltingReward() {
 		return getActionReward(actionTypes.getType(ActionTypes.SMELTING));
 	}
 	
 	public PotionTree getComplexBrewingReward() {
 		return getComplexReward(actionTypes.getType(ActionTypes.BREWING));
 	}
 
 	public double getScanRadiusSetting() {
 		return scanRadiusSetting;
 	}
 
 	/**
 	 * Retrieves the current registered action types.
 	 * @return Registry of action types.
 	 */
 	public ActionTypes getActionTypes() {
 		return actionTypes;
 	}
 
 	/**
 	 * Sets the current registry of action types. This must be changed before configurations are loaded.
 	 * @param actionTypes - new action type registry.
 	 */
 	public void setActionTypes(ActionTypes actionTypes) {
 		this.actionTypes = actionTypes;
 	}
 	
 	public ItemParser getItemParser() {
 		return itemParser;
 	}
 
 	public void setItemParser(ItemParser itemParser) {
 		this.itemParser = itemParser;
 	}
 
 	public MobParser getMobParser() {
 		return mobParser;
 	}
 
 	public void setMobParser(MobParser mobParser) {
 		this.mobParser = mobParser;
 	}
 
 	public ActionParser getActionParser() {
 		return actionParser;
 	}
 
 	public void setActionParser(ActionParser actionParser) {
 		this.actionParser = actionParser;
 	}
 	
 	public PlayerTree getPlayerDeathDrop() {
 		return playerDeathDrop;
 	}
 
 	public void setPlayerDeathDrop(PlayerTree playerDeathDrop) {
 		this.playerDeathDrop = playerDeathDrop;
 	}
 
 	public PlayerParser getPlayerParser() {
 		return playerParser;
 	}
 
 	public void setPlayerParser(PlayerParser playerParser) {
 		this.playerParser = playerParser;
 	}
 	
 	public ParameterProviderSet getParameterProviders() {
 		return parameterProviders;
 	}
 
 	public void setParameterProviders(ParameterProviderSet parameterProviders) {
 		this.parameterProviders = parameterProviders;
 	}
 
 	// Let the message queue know
 	public void onTick() {
 		if (messageQueue != null) 
 			messageQueue.onTick();
 	}
 
 	@Override
 	public void removePlayerCache(Player player) {
 		// Removes a given player from any live buffers or caches.
 		if (messageQueue != null)
 			messageQueue.removePlayerCache(player);
 	}
 }
