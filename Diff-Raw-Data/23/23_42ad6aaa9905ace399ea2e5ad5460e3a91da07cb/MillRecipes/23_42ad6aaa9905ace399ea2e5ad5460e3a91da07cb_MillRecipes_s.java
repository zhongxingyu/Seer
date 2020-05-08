 package adanaran.mods.bfr.crafting;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import adanaran.mods.bfr.BFR;
 
 public class MillRecipes {
 
 	private static final MillRecipes instance = new MillRecipes();
 	private Map millingList = new HashMap();
 	private HashMap<List<Integer>, ItemStack> metaMillingList = new HashMap<List<Integer>, ItemStack>();
 
 	private MillRecipes() {
 		this.addMilling(Item.wheat.itemID, new ItemStack(BFR.itemFlour));
		this.addMilling(Block.reed.blockID, new ItemStack(Item.sugar));
 		this.addMilling(Item.dyePowder.itemID, 3, new ItemStack(BFR.itemCocoaPowder));
 	}
 
 	public static MillRecipes getInstance() {
 		return instance;
 	}
 
 	public void addMilling(int inputId, ItemStack result) {
 		this.millingList.put(Integer.valueOf(inputId), result);
 	}
 	
 	public void addMilling(int itemID, int metadata, ItemStack itemstack)
     {
         metaMillingList.put(Arrays.asList(itemID, metadata), itemstack);
     }
 
 	public Map getMillingList() {
 		return this.millingList;
 	}
 
 	public ItemStack getSmeltingResult(ItemStack item) {
 		if (item == null) {
 			return null;
 		}
 		ItemStack ret = (ItemStack)metaMillingList.get(Arrays.asList(item.itemID, item.getItemDamage()));
         if (ret != null) 
         {
             return ret;
         }
 		return (ItemStack) millingList.get(Integer.valueOf(item.itemID));
 	}
 
 }
