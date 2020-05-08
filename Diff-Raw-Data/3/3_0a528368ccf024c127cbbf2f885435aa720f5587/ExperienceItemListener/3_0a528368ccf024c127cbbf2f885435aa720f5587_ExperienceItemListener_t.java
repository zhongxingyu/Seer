 package com.comphenix.xp.listeners;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.Action;
 import com.comphenix.xp.Configuration;
 import com.comphenix.xp.Debugger;
 import com.comphenix.xp.Presets;
 import com.comphenix.xp.lookup.ItemQuery;
 import com.comphenix.xp.lookup.ItemTree;
 import com.comphenix.xp.lookup.PlayerRewards;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.google.common.base.Objects;
 
 public class ExperienceItemListener implements Listener {
 
 	private final String permissionRewardSmelting = "experiencemod.rewards.smelting";
 	private final String permissionRewardBrewing = "experiencemod.rewards.brewing";
 	private final String permissionRewardCrafting = "experiencemod.rewards.crafting";
 	private final String permissionRewardFishing = "experiencemod.rewards.fishing";
 
 	private JavaPlugin parentPlugin;
 	private Debugger debugger;
 	private Presets presets;
 	
 	// Random source
 	private Random random = new Random();
 	
 	public ExperienceItemListener(JavaPlugin parentPlugin, Debugger debugger, Presets presets) {
 		this.parentPlugin = parentPlugin;
 		this.debugger = debugger;
 		setPresets(presets);
 	}
 	
 	public Presets getPresets() {
 		return presets;
 	}
 
 	public void setPresets(Presets presets) {
 		this.presets = presets;
 	}
 
 	/**
 	 * Load the correct configuration for a given player.
 	 * @param world - the given player.
 	 * @return The most relevant configuration, or NULL if none were found.
 	 */
 	public Configuration getConfiguration(Player player) {
 		try {
 			return presets.getConfiguration(player);
 			
 		} catch (ParsingException e) {
 			// We most likely have complained about this already
 			return null;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerFishEvent(PlayerFishEvent event) {
 		
 		Player player = event.getPlayer();
 		
 		String message = null;
 		Action action = null;
 
 		if (player != null && player.hasPermission(permissionRewardFishing)) {
 			
 			Configuration config = getConfiguration(player);
 			
 			// No configuration or default configuration found
 			if (config == null) {
 				if (debugger != null)
 					debugger.printDebug(this, "Cannot find config for player %s in fishing.", player.getName());
 				return;
 			}
 				
 			PlayerRewards playerReward = config.getPlayerRewards();
 			ChannelProvider channels = config.getChannelProvider();
 			
 			// Reward type
 			switch (event.getState()) {
 			case CAUGHT_FISH:
 				action = playerReward.getFishingSuccess();
 				message = "Fish caught by %s: Spawned %d xp.";
 				break;
 
 			case FAILED_ATTEMPT:
 				action = playerReward.getFishingFailure();
 				message = "Fishing failed for %s: Spawned %d xp.";
 				break;
 			}
 			
 			// Has an action been set?
 			if (action != null) {
 				int exp = action.rewardPlayer(config.getRewardProvider(), random, player);
 
 				action.setDebugger(debugger);
 				action.emoteMessages(channels, channels.getFormatter(player, exp), player);
 				
 				if (debugger != null)
 					debugger.printDebug(this, message, player.getName(), exp);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onInventoryClickEvent(InventoryClickEvent event) {
 
 		Player player = (Player) event.getWhoClicked();
 		ItemStack toCraft = event.getCurrentItem();
 		
 		Configuration config = null;
 
 		// Was this from a result slot (crafting, smelting or brewing)?
 		if (player != null &&
 		    event.getInventory() != null &&
 		    event.getSlotType() == SlotType.RESULT &&
 		    hasItems(toCraft)) {
 			
 			// Handle different types
 			switch (event.getInventory().getType()) {
 			case BREWING:
 				// Do not proceed if the user isn't permitted
 				if (player.hasPermission(permissionRewardBrewing)) {
 					config = getConfiguration(player);
 					
 					// Guard again
 					if (config == null) {
 						if (debugger != null)
 							debugger.printDebug(this, "No config found for %s with brewing %s.", player.getName(), toCraft);
 						return;
 					}
 					
 					handleInventory(event, config.getRewardProvider(), config.getChannelProvider(),
 								    config.getSimpleBrewingReward(), true);
 				
 					// Yes, this feels a bit like a hack to me too. Blame faulty design. Anyways, the point
 					// is that we get to check more complex potion matching rules, like "match all splash potions"
 					// or "match all level 2 regen potions (splash or not)".
 					handleInventory(event, config.getRewardProvider(), config.getChannelProvider(),
 							config.getComplexBrewingReward().getItemQueryAdaptor(), true);
 				}
 				
 				break;
 			case CRAFTING:
 			case WORKBENCH:
 				if (player.hasPermission(permissionRewardCrafting)) {
 					config = getConfiguration(player);
 					
 					if (config != null) {
 						handleInventory(event, config.getRewardProvider(), config.getChannelProvider(),
 										config.getSimpleCraftingReward(), false);
 					} else if (debugger != null) {
 						debugger.printDebug(this, "No config found for %s with crafting %s.", player.getName(), toCraft);
 					}
 				}
 				break;
 				
 			case FURNACE:
 				if (player.hasPermission(permissionRewardSmelting)) {
 					config = getConfiguration(player);
 					
 					if (config != null) {
 						handleInventory(event, config.getRewardProvider(), config.getChannelProvider(),
 										config.getSimpleSmeltingReward(), true);
 					} else if (debugger != null) {
 						debugger.printDebug(this, "No config found for %s with smelting %s.", player.getName(), toCraft);
 					}
 				}
 				break;
 			}
 		}
 	}
 	
 	private void handleInventory(InventoryClickEvent event, RewardProvider rewardsProvider, 
 								 ChannelProvider channelsProvider, ItemTree rewards, boolean partialResults) {
 		
 		Player player = (Player) event.getWhoClicked();
 		ItemStack toStore = event.getCursor();
 		ItemStack toCraft = event.getCurrentItem();
 		
 		ItemQuery retrieveKey = ItemQuery.fromExact(toCraft);
 		Action action = rewards.get(retrieveKey);
 		
 		// Make sure there is an experience reward
 		if (!hasExperienceReward(rewards, retrieveKey))
 			return;
 
 		// Set debugger
 		action.setDebugger(debugger);
 		
 		if (event.isShiftClick()) {
 			// Hack ahoy
 			schedulePostCraftingReward(player, rewardsProvider, channelsProvider, action, toCraft);
 		} else {
 			
 			// The items are stored in the cursor. Make sure there's enough space.
 			int count = getStorageCount(toStore, toCraft, partialResults);
 
 			if (count > 0) {
 				
 				// Some cruft here - the stack is only divided when the user has no cursor items
 				if (partialResults && event.isRightClick() && !hasItems(toStore)) {
 					count = Math.max(count / 2, 1);
 				}
 				
 				// Give the experience straight to the user
 				Integer exp = action.rewardPlayer(rewardsProvider, random, player, count);
 				action.emoteMessages(channelsProvider, channelsProvider.getFormatter(player, exp), player);
 				
 				// Like above
 				if (debugger != null)
 					debugger.printDebug(this, "User %s - spawned %d xp for item %s.", 
 						player.getName(), exp, toCraft.getType());
 			}
 		}
 	}
 	
 	// HACK! The API doesn't allow us to easily determine the resulting number of
 	// crafted items, so we're forced to compare the inventory before and after.
 	private void schedulePostCraftingReward(final Player player, final RewardProvider provider, 
 											final ChannelProvider channelsProvider,
 											final Action action, final ItemStack compareItem) {
 		
 		final ItemStack[] preInv = player.getInventory().getContents();
 		final int ticks = 1; // May need adjusting
 		
 		// Clone the array. The content may (was for me) mutable.
 		for (int i = 0; i < preInv.length; i++) {
 			preInv[i] = preInv[i] != null ? preInv[i].clone() : null;
 		}
 		
 		Bukkit.getScheduler().scheduleSyncDelayedTask(parentPlugin, new Runnable() {
 			@Override
 			public void run() {
 				final ItemStack[] postInv = player.getInventory().getContents();
 				int newItemsCount = 0;
 				
 				for (int i = 0; i < preInv.length; i++) {
 					ItemStack pre = preInv[i];
 					ItemStack post = postInv[i];
 
 					// We're only interested in filled slots that are different
 					if (hasSameItem(compareItem, post) && (hasSameItem(compareItem, pre) || pre == null)) {
 						newItemsCount += post.getAmount() - (pre != null ? pre.getAmount() : 0);
 					}
 				}
 				
 				if (newItemsCount > 0) {
 					int exp = action.rewardPlayer(provider, 
 							random, player, newItemsCount);
 					
 					// Display messages
 					action.emoteMessages(channelsProvider, channelsProvider.getFormatter(player, exp), player);
 
 					// We know this is from crafting
 					if (debugger != null)
 						debugger.printDebug(this, "User %s - spawned %d xp for %d items of %s.", 
							player.getName(), exp, newItemsCount, compareItem.getType());
 				}
 			}
 		}, ticks);
 	}
 	
 	private boolean hasSameItem(ItemStack a, ItemStack b) {
 		if (a == null)
 			return b == null;
 		else if (b == null)
 			return a == null;
 		
 		return a.getTypeId() == b.getTypeId() &&
 			   a.getDurability() == b.getDurability() && 
 			   Objects.equal(a.getEnchantments(), b.getEnchantments());
 	}
 	
 	private boolean hasExperienceReward(ItemTree rewards, ItemQuery key) {
 		// Make sure there is any experience
 		return rewards.containsKey(key) && !rewards.get(key).equals(Action.Default);
 	}
 	
 	// Recipes are cancelled if there's isn't exactly enough space. 
 	public int getStorageCount(ItemStack storage, ItemStack addition, boolean allowPartial) {
 		
 		if (addition == null)
 			return 0;
 		else if (storage == null)
 			// All storage slots have the same limits
 			return addition.getAmount(); 
 		// Yes, storage might be air blocks ... weird.
 		else if (storage.getType() != Material.AIR && !hasSameItem(storage, addition))
 			// Items MUST be the same
 			return 0;
 		
 		int sum = storage.getAmount() + addition.getAmount();
 		int max = storage.getType().getMaxStackSize();
 
 		// Now determine the number of additional items in the storage stack
 		if (sum > max) {
 			return allowPartial ? max - storage.getAmount() : 0;
 		} else {
 			return addition.getAmount();
 		}
 	}
 		
 	private boolean hasItems(ItemStack stack) {
 		return stack != null && stack.getAmount() > 0;
 	}
 }
