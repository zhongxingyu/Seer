 
 package hawksmachinery.common.api;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.world.World;
 import net.minecraftforge.oredict.OreDictionary;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMRecipes
 {
 	private static Map washerSecondaries = new HashMap();
 	private static Map washerRarities = new HashMap();
 	private static Map quantityMapping = new HashMap();
 	private static List forgeRecipes = new ArrayList();
 	
 	/**
 	 * 
 	 * Instead of IDs, processing now uses enums.
 	 * 
 	 * @author Elusivehawk
 	 */
 	public enum HMEnumProcessing
 	{
 		CRUSHING(new HashMap()),
 		
 		CRUSHING_EXPLOSIVES(new HashMap()),
 		
 		WASHING(new HashMap()),
 		
 		SINTERER(new HashMap()),
 		
 		HM_E2MM(new HashMap());
 		
 		private Map processingList;
 		
 		HMEnumProcessing(Map recipeList)
 		{
 			this.processingList = recipeList;
 			
 		}
 		
 		public Map getRecipeList()
 		{
 			return this.processingList;
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * Adds a processing recipe.
 	 * 
 	 * @param input The input.
 	 * @param output The output.
 	 * @param processType What type of processing this recipe is for.
 	 */
 	public static void addHMProcessingRecipe(ItemStack input, ItemStack output, HMEnumProcessing processType)
 	{
 		processType.getRecipeList().put(Arrays.asList(input.getItem(), input.isItemStackDamageable() ? 0 : input.getItemDamage(), input.isItemEnchanted(), input.stackTagCompound != null), output);
 		quantityMapping.put(Arrays.asList(input.getItem(), input.getItemDamage(), processType), (Integer)input.stackSize);
 		
 	}
 	
 	/**
 	 * 
 	 * Forge Ore Dictionary supported version.
 	 * 
 	 * @param input
 	 * @param output
 	 * @param processingType
 	 */
 	public static void addHMFoDProcessingRecipe(String input, ItemStack output, HMEnumProcessing processType)
 	{
 		for (ItemStack ore : OreDictionary.getOres(input))
 		{
 			if (ore != null)
 			{
 				addHMProcessingRecipe(ore, output, processType);
 			}
 			
 		}
 		
 	}
 	
 	public static void addHMWashingSecondary(ItemStack input, Object output, boolean isCommon)
 	{
 		if (isCommon)
 		{
 			washerSecondaries.put(Arrays.asList(input.getItem(), input.getItemDamage()), output);
 		}
 		else
 		{
 			washerRarities.put(Arrays.asList(input.getItem(), input.getItemDamage()), output);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * Adds a new recipe to the Star and Endium Forge.
 	 * 
 	 * @param recipe The recipe.
 	 */
 	public static void addHMForgeRecipe(IRecipe recipe)
 	{
 		forgeRecipes.add(recipe);
 	}
 	
 	public static ItemStack getResult(ItemStack input, HMEnumProcessing processType)
 	{
		if (input == null)
		{
			return null;
		}
		else
 		{
 			ItemStack output = (ItemStack)processType.getRecipeList().get(Arrays.asList(input.getItem(), input.isItemStackDamageable() ? 0 : input.getItemDamage(), input.isItemEnchanted(), input.stackTagCompound != null));
 			
 			if (output != null)
 			{
 				if (input.isItemEqual(output) && input.isItemStackDamageable() && output.isItemStackDamageable())
 				{
 					output.setItemDamage(input.getItemDamage());
 				}
 				
 			}
 			
			return (input.stackSize < getQuantity(input, processType)) ? null : output;
			
 		}
 		
 	}
 	
 	public static int getQuantity(ItemStack item, HMEnumProcessing processType)
 	{
 		return (Integer)quantityMapping.get(Arrays.asList(item.getItem(), item.getItemDamage(), processType));
 	}
 	
 	public static ItemStack getWashingSecondaryResult(ItemStack item, Random random)
 	{
 		int secondaryChance = random.nextInt(100);
 		
 		if (item == null)
 		{
 			return null;
 		}
 		else
 		{
 			if (secondaryChance < 5)
 			{
 				ItemStack ret = (ItemStack)washerRarities.get(Arrays.asList(item.getItem(), item.getItemDamage()));
 				
 				if (ret != null) 
 				{
 					return ret;
 				}
 				
 			}
 			else if (secondaryChance < 10)
 			{
 				ItemStack ret = (ItemStack)washerSecondaries.get(Arrays.asList(item.getItem(), item.getItemDamage()));
 				
 				if (ret != null) 
 				{
 					return ret;
 				}
 				
 			}
 			
 			return null;
 		}
 		
 	}
 	
 	public static ItemStack getForgeResult(InventoryCrafting matrix, World world)
 	{
 		for (int counter = 0; counter < forgeRecipes.size(); ++counter)
 		{
 			IRecipe recipe = (IRecipe)forgeRecipes.get(counter);
 			
 			if (recipe != null)
 			{
 				if (recipe.matches(matrix, world))
 				{
 					return recipe.getCraftingResult(matrix);
 				}
 				
 			}
 			
 		}
 		
 		return null;
 	}
 	
 }
