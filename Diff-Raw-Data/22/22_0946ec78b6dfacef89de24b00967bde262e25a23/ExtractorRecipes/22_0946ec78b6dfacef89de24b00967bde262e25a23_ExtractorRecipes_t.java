 package mods.nordwest.common;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 
 public class ExtractorRecipes {
 	private static final ExtractorRecipes extractBase = new ExtractorRecipes();
 
 	/** The list of extractor results. */
 	private HashMap<List<Integer>, ItemStack> metaExtractingList = new HashMap<List<Integer>, ItemStack>();
 	private HashMap<List<Integer>, ItemStack> metaExtractingBonusList = new HashMap<List<Integer>, ItemStack>();
 	private HashMap<List<Integer>, Integer> metaExtractingChanceList = new HashMap<List<Integer>, Integer>();
 	private HashMap<List<Integer>, Float> metaExperience = new HashMap<List<Integer>, Float>();
 
 	/**
 	 * Used to call methods addSmelting and getSmeltingResult.
 	 */
 	public static final ExtractorRecipes extracting() {
 		return extractBase;
 	}
 
 	private ExtractorRecipes() {
		//this.addExtracting(Block.leaves.blockID, 0, new ItemStack(Item.ingotIron), 50, new ItemStack(Item.ingotIron), 0.7F);
 		this.addExtracting(CustomItems.expaniteItem.itemID, 0, new ItemStack(Item.expBottle), 1, new ItemStack(CustomItems.expaniteItem), 0.0F);
 
 	}
 
 	/**
 	 * A metadata sensitive version of adding a furnace recipe.
 	 */
 	public void addExtracting(int itemID, int metadata, ItemStack itemstack, float experience) {
 		metaExtractingList.put(Arrays.asList(itemID, metadata), itemstack);
 		metaExperience.put(Arrays.asList(itemID, metadata), experience);
 	}
 
 	public void addExtracting(int itemID, int metadata, ItemStack itemstack, int chance, ItemStack bonus, float experience) {
 		metaExtractingList.put(Arrays.asList(itemID, metadata), itemstack);
 		metaExperience.put(Arrays.asList(itemID, metadata), experience);
 		metaExtractingBonusList.put(Arrays.asList(itemID, metadata), bonus);
 		metaExtractingChanceList.put(Arrays.asList(itemID, metadata), chance);
 	}
 
 	/**
 	 * Used to get the resulting ItemStack form a source ItemStack
 	 * @param item The Source ItemStack
 	 * @return The result ItemStack
 	 */
 	public ItemStack getExtractingResult(ItemStack item) {
 		if (item == null) {
 			return null;
 		}
 		ItemStack ret = (ItemStack) metaExtractingList.get(Arrays.asList(item.itemID, item.getItemDamage()));
 		if (ret != null) {
 			return ret;
 		}
 		return null;
 	}
 
 	public ItemStack getExtractingBonusResult(ItemStack item) {
 		if (item == null) {
 			return null;
 		}
 		ItemStack ret =metaExtractingBonusList.get(Arrays.asList(item.itemID, item.getItemDamage()));
 		if (ret != null) {
 			return ret;
 		}
 		return null;
 	}
 
 	public int getExtractingBonusChance(ItemStack item) {
 		if (item == null) {
 			return 0;
 		}
 		int ret = metaExtractingChanceList.get(Arrays.asList(item.itemID, item.getItemDamage()));
 		return ret;
 
 	}
 
 	/**
 	 * Grabs the amount of base experience for this item to give when pulled from the furnace slot.
 	 */
 	public float getExperience(ItemStack item) {
 		if (item == null || item.getItem() == null) {
 			return 0;
 		}
 		float ret = item.getItem().getSmeltingExperience(item);
 		if (ret < 0 && metaExperience.containsKey(Arrays.asList(item.itemID, item.getItemDamage()))) {
 			ret = metaExperience.get(Arrays.asList(item.itemID, item.getItemDamage()));
 		}
 		return (ret < 0 ? 0 : ret);
 	}
 
 	public Map<List<Integer>, ItemStack> getMetaExtractingList() {
 		return metaExtractingList;
 	}
 
 	public HashMap<List<Integer>, ItemStack> getExtractingBonusList() {
 		return this.metaExtractingBonusList;
 	}
 	public HashMap<List<Integer>, Integer> getExtractingChanceList() {
 		return this.metaExtractingChanceList;
 	}
 }
