 package openwolves.api.utils;
 
 import java.util.ArrayList;
import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.item.crafting.ShapelessRecipes;
import cpw.mods.fml.relauncher.ReflectionHelper;
 
 public class OWCraftingUtils 
 {
 	public static IRecipe addShapelessRecipe(ItemStack par1ItemStack, Object ... par2ArrayOfObj)
 	{
 		ArrayList arraylist = new ArrayList();
 		Object[] aobject = par2ArrayOfObj;
 		int i = par2ArrayOfObj.length;
 
 		for (int j = 0; j < i; ++j)
 		{
 			Object object1 = aobject[j];
 
 			if (object1 instanceof ItemStack)
 			{
 				arraylist.add(((ItemStack)object1).copy());
 			}
 			else if (object1 instanceof Item)
 			{
 				arraylist.add(new ItemStack((Item)object1));
 			}
 			else
 			{
 				if (!(object1 instanceof Block))
 				{
					throw new RuntimeException("Invalid shapeless recipy!");
 				}
 
 				arraylist.add(new ItemStack((Block)object1));
 			}
 		}
 		
 		ShapelessRecipes shapelessrecipes = new ShapelessRecipes(par1ItemStack, arraylist);
		//List recipes = (List)ReflectionHelper.getPrivateValue(CraftingManager.class, null, new String[] {"recipes"});
 		
		//recipes.add(shapelessrecipes);
 		return shapelessrecipes;
 	}
 }
