 package com.comphenix.xp.listeners;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.BrewEvent;
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
 import com.comphenix.xp.messages.MessagePlayerQueue;
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
 				config.getMessageQueue().enqueue(player, action, channels.getFormatter(player, exp));
 				
 				if (debugger != null)
 					debugger.printDebug(this, message, player.getName(), exp);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBrewEvent(BrewEvent event) {
 		
 		// Reset the potion markers
 		if (event != null &&
 			event.getContents() != null) {
 			
 			for (ItemStack stack : event.getContents().getContents()) {
 			
 				// Find all potions in the brewing stand
 				if (hasItems(stack) && stack.getType() == Material.POTION) {
 					
 					PotionMarker marker = new PotionMarker(stack.getDurability());
 					
 					// Reset potion markers
 					marker.reset();
 					stack.setDurability(marker.toDurability());
 				}
 			}
 			
 			if (debugger != null)
 				debugger.printDebug(this, "Reset potion markers in brewing stand %s", getLocationString(event.getBlock()) );
 		}
 	}
 	
 	// Convert a block to a more readable format
 	private String getLocationString(Block block) {
 		return String.format("x=%d, y=%d, z=%d", block.getX(), block.getY(), block.getZ());
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onInventoryClickEvent(InventoryClickEvent event) {
 
 		Player player = (Player) event.getWhoClicked();
 		ItemStack toCraft = event.getCurrentItem();
 		
 		Configuration config = null;
 
 		// Crafting, smelting and potion check
 		boolean isCraftResult = event.getSlotType() == SlotType.RESULT;
 		boolean isPotionResult = event.getSlot() < 3;
 
 		// Make sure we have a player, inventory and item
 		if (player != null && event.getInventory() != null && hasItems(toCraft)) {
 			
 			// Handle different types
 			switch (event.getInventory().getType()) {
 			case BREWING:
 				// Do not proceed if the user isn't permitted
 				if (isPotionResult && player.hasPermission(permissionRewardBrewing)) {
 					config = getConfiguration(player);
 					
 					// Guard again
 					if (config == null) {
 						if (debugger != null)
 							debugger.printDebug(this, "No config found for %s with brewing %s.", player.getName(), toCraft);
 						return;
 					}
 					
 					// Prepare rewards and actions
 					RewardableAction future = potionItemReward(config);
 					Action simpleAction = getAction(toCraft, config.getSimpleBrewingReward());
 					
 					// Yes, this feels a bit like a hack to me too. Blame faulty design. Anyways, the point
 					// is that we get to check more complex potion matching rules, like "match all splash potions"
 					// or "match all level 2 regen potions (splash or not)".
 					Action complexAction = getAction(toCraft, config.getComplexBrewingReward().getItemQueryAdaptor());
 					
 					handleInventory(event, simpleAction, future, true);
 					handleInventory(event, complexAction, future, true);
 				}
 				
 				break;
 			case CRAFTING:
 			case WORKBENCH:
 				if (isCraftResult && player.hasPermission(permissionRewardCrafting)) {
 					
 					config = getConfiguration(player);
 
 					if (config != null) {
 						RewardableAction future = genericItemReward(config);
 						Action action = getAction(toCraft, config.getSimpleCraftingReward());
 						
 						handleInventory(event, action, future, false);
 						
 					} else if (debugger != null) {
 						debugger.printDebug(this, "No config found for %s with crafting %s.", player.getName(), toCraft);
 					}
 				}
 				break;
 				
 			case FURNACE:
 				if (isCraftResult && player.hasPermission(permissionRewardSmelting)) {
 					config = getConfiguration(player);
 					
 					if (config != null) {
 						RewardableAction future = genericItemReward(config);
 						Action action = getAction(toCraft, config.getSimpleSmeltingReward());
 						
 						handleInventory(event, action, future, true);
 						
 					} else if (debugger != null) {
 						debugger.printDebug(this, "No config found for %s with smelting %s.", player.getName(), toCraft);
 					}
 				}
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Retrieves the most relevant action, or NULL if no action can be found.
 	 * @param toCraft Item to query after.
 	 * @param rewards Tree of rewards and actions.
 	 * @return The most relevant action, or NULL.
 	 */
 	private Action getAction(ItemStack toCraft, ItemTree rewards) {
 		
 		ItemQuery retrieveKey = ItemQuery.fromExact(toCraft);
 		
 		if (hasExperienceReward(rewards, retrieveKey)) {
 			return rewards.get(retrieveKey);
 		} else {
 			return null;
 		}
 	}
 	
 	private void handleInventory(InventoryClickEvent event, Action action, 
 								 RewardableAction rewardAction, boolean partialResults) {
 		
 		Player player = (Player) event.getWhoClicked();
 		ItemStack toStore = event.getCursor();
 		ItemStack toCraft = event.getCurrentItem();
 		
 		// Nothing do do
 		if (action == null)
 			return;
 		
 		// Set debugger
 		action.setDebugger(debugger);
 		
 		if (event.isShiftClick()) {
 			// Hack ahoy
 			schedulePostCraftingReward(player, action, rewardAction, toCraft);
 		} else {
 			
 			// The items are stored in the cursor. Make sure there's enough space.
 			int count = getStorageCount(toStore, toCraft, partialResults);
 
 			if (count > 0) {
 				
 				// Some cruft here - the stack is only divided when the user has no cursor items
 				if (partialResults && event.isRightClick() && !hasItems(toStore)) {
 					count = Math.max(count / 2, 1);
 				}
 				
 				rewardAction.performAction(player, toCraft, action, count);
 			}
 		}
 	}
 	
 	private RewardableAction potionItemReward(Configuration config) {
 		
 		final RewardableAction fundamental = genericItemReward(config);
 		
 		// Next, mark all potions as "consumed" or "rewarded"
 		return new RewardableAction() {
 			
 			@Override
 			public void performAction(Player player, ItemStack stack, Action action, int count) {
 				
 				PotionMarker marker = new PotionMarker(stack.getDurability());
 				
 				// Only reward potions once
 				if (!marker.hasBeenRewarded()) {
 					fundamental.performAction(player, stack, action, count);
 				
 					marker.setBeenRewarded(true);
 					stack.setDurability(marker.toDurability());
 				}
 			}
 		};	
 	}
 	
 	private RewardableAction genericItemReward(Configuration config) {
 		
 		final RewardProvider rewardsProvider = config.getRewardProvider();
 		final ChannelProvider channelsProvider = config.getChannelProvider();
 		final MessagePlayerQueue messageQueue = config.getMessageQueue();
 		
 		// Create a rewardable future action handler
 		return new RewardableAction() {
 			@Override
 			public void performAction(Player player, ItemStack stack, Action action, int count) {
 
 				// Give the experience straight to the user
 				Integer exp = action.rewardPlayer(rewardsProvider, random, player, count);
				messageQueue.enqueue(player, action, channelsProvider.getFormatter(player, exp));
 				
 				// Like above
 				if (debugger != null)
 					debugger.printDebug(this, "User %s - spawned %d xp for item %s.", 
 						player.getName(), exp, stack.getType());
 			}
 		};
 	}
 	
 	// HACK! The API doesn't allow us to easily determine the resulting number of
 	// crafted items, so we're forced to compare the inventory before and after.
 	private void schedulePostCraftingReward(final Player player, final Action action,
 											final RewardableAction rewardAction, 
 											final ItemStack compareItem) {
 		
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
 				
 				// The most relevant item stack to return
 				ItemStack last = compareItem;
 				
 				for (int i = 0; i < preInv.length; i++) {
 					ItemStack pre = preInv[i];
 					ItemStack post = postInv[i];
 
 					// We're only interested in filled slots that are different
 					if (hasSameItem(compareItem, post) && (hasSameItem(compareItem, pre) || pre == null)) {
 						newItemsCount += post.getAmount() - (pre != null ? pre.getAmount() : 0);
 						
 						if (post.getAmount() > 0)
 							last = post;
 					}
 				}
 				
 				if (newItemsCount > 0) {
 					rewardAction.performAction(player, last, action, newItemsCount);
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
