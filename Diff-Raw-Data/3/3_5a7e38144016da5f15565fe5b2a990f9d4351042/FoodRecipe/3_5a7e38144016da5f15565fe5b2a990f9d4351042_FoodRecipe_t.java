 package clashsoft.mods.morefood.food;
 
 import clashsoft.clashsoftapi.util.CSCrafting;
 import clashsoft.clashsoftapi.util.IItemMetadataRecipe;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class FoodRecipe implements IItemMetadataRecipe
 {
 	private int				amount;
 	private int				craftingType;
 	private Object[]		data;
 	private ItemStack[][]	analysed;
 	
 	public FoodRecipe(int craftingType, int amount, Object... data)
 	{
 		this.craftingType = craftingType;
 		this.amount = amount;
 		this.data = data;
 		this.analysed = CSCrafting.analyseCrafting(this);
 	}
 	
 	public FoodRecipe(int amount, ItemStack... data)
 	{
 		this(CRAFTING_SHAPELESS, amount, (Object[]) data);
 	}
 	
 	public void addRecipe(Item item, int foodID)
 	{
 		switch (this.craftingType)
 		{
 		case CRAFTING:
 			GameRegistry.addRecipe(new ItemStack(item, this.amount, foodID), data);
 			break;
 		case CRAFTING_SHAPELESS:
 			GameRegistry.addShapelessRecipe(new ItemStack(item, this.amount, foodID), data);
 			break;
 		case FURNACE:
			Float f = (Float) data[1];
			CSCrafting.addSmelting((ItemStack) data[0], new ItemStack(item, this.amount, foodID), f);
 			break;
 		}
 	}
 	
 	@Override
 	public int getAmount()
 	{
 		return amount;
 	}
 	
 	@Override
 	public int getCraftingType()
 	{
 		return craftingType;
 	}
 	
 	@Override
 	public Object[] getData()
 	{
 		return data;
 	}
 	
 	/**
 	 * @return the analysed recipe
 	 */
 	public ItemStack[][] getAnalysedRecipe()
 	{
 		return analysed;
 	}
 }
