 package com.comphenix.xp.rewards.items;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
 import org.bukkit.inventory.ItemStack;
 
 import com.comphenix.xp.Range;
 import com.comphenix.xp.lookup.ItemQuery;
 import com.comphenix.xp.rewards.ResourceFactory;
 import com.comphenix.xp.rewards.ResourceHolder;
 
 public class ItemsFactory implements ResourceFactory {
 
 	// Going above this seems ridiculous
 	public static final int MAX_STACKS = 100;
 	
 	private List<Entry> entries = new ArrayList<Entry>();
 	private double multiplier;
 	
 	public ItemsFactory() {
 		this(new ArrayList<Entry>(), 1);
 	}
 		
 	public ItemsFactory(double multiplier) {
 		this(new ArrayList<Entry>(), multiplier);
 	}
 	
 	private ItemsFactory(List<Entry> entries, double multiplier) {
 		this.entries = entries;
 		this.multiplier = multiplier;
 	}
 	
 	public void addItems(ItemQuery item, Range range) {
 		if (range == null)
 			throw new NullArgumentException("range");
 		if (item == null)
 			throw new NullArgumentException("item");
 		if (!item.hasItemID())
 			throw new IllegalArgumentException("Item must have an ID.");
 		if (range.getMaximum() > 64 * MAX_STACKS)
 			throw new IllegalArgumentException(String.format("Range cannot exceed %d.", 64 * MAX_STACKS));
 		
 		entries.add(new Entry(item, range));
 	}
 	
 	@Override
 	public ResourceHolder getResource(Random rnd, int count) {
 
 		List<ItemStack> stacks = new ArrayList<ItemStack>();
 		
 		// Find the correct item ID and metadata
 		for (Entry entry : entries) {
 			fillInventory(rnd, stacks, entry, count);
 		}
 		
 		return new ItemsHolder(stacks);
 	}
 	
 	private void fillInventory(Random rnd, List<ItemStack> inventory, Entry entry, int count) {
 		
 		int itemID = RandomSampling.getRandomElement(entry.getItemID());
 		int data = RandomSampling.getRandomElement(entry.getDurability(), 0);
 		int amount = entry.getRange().sampleInt(rnd) * count;
 
 		while (amount > 0) {
 			ItemStack stack = new ItemStack(itemID, 1, (short) data);
 			Integer size = stack.getMaxStackSize();
 			
 			if (amount / size > MAX_STACKS) {
 				// NO, stop!
 				return;
 			}
 			
 			// Now, see how big this stack can become
 			stack.setAmount(Math.min(amount, size));
 			
 			amount -= stack.getAmount();
 			inventory.add(stack);
 		}
 	}
 	
 	private List<ItemStack> fromEntry(Entry entry) {
 		
 		List<ItemStack> newList = new ArrayList<ItemStack>();
 		
 		// Create list
 		fillInventory(RandomSampling.getThreadRandom(), newList, entry, 1);
 		return newList;
 	}
 	
 	
 	@Override
 	public ResourceFactory withMultiplier(double newMultiplier) {
 		return new ItemsFactory(entries, newMultiplier);
 	}
 
 	@Override
 	public double getMultiplier() {
 		return multiplier;
 	}
 
 	@Override
 	public ResourceHolder getResource(Random rnd) {
 		// Default value
 		return getResource(rnd, 1);
 	}
 	
 	@Override
 	public ResourceHolder getMinimum(int count) {
 		
 		Entry minimumEntry = null;
 		int minimumRange = Integer.MAX_VALUE;
 		
 		// Find the entry with the lowest resource count. 
 		for (Entry entry : entries) {
 			int range = entry.getRange().getMinimum() * count;
 			
 			if (minimumRange > range) {
 				minimumRange = range;
 				minimumEntry = entry;
 			}
 		}
 		
 		return new ItemsHolder(fromEntry(minimumEntry));
 	}
 
 	@Override
 	public ResourceHolder getMaximum(int count) {
 
 		Entry maximumValue = null;
 		int maximumRange = Integer.MIN_VALUE;
 		
 		// Find the entry with the highest resource count
 		for (Entry entry : entries) {
 			int range = entry.getRange().getMaximum() * count;
 			
 			if (maximumRange < range) {
 				maximumRange = range;
 				maximumValue = entry;
 			}
 		}
 		
 		return new ItemsHolder(fromEntry(maximumValue));
 	}
 	
	@Override
	public String toString() {
		return StringUtils.join(entries, ", ");
	}
	
 	// An item and the amount to award
 	private class Entry {
 		private ItemQuery item;
 		private Range range;
 		
 		public Entry(ItemQuery item, Range range) {
 			this.item = item;
 			this.range = range;
 		}
 
 		public Range getRange() {
 			return range;
 		}
 		
 		public List<Integer> getItemID() {
 			return item.getItemID();
 		}
 		
 		public List<Integer> getDurability() {
 			return item.getDurability();
 		}
		
		@Override
		public String toString() {
			return String.format("%s %s", range, item);
		}
 	}
 }
