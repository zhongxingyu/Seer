 package cooler.utils.handlers;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cooler.items.ModItems;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class RecipeHandler {
 
     public static void add() {
         Handler.logName("is Loading it's Recipies");
         
         GameRegistry.addShapelessRecipe(new ItemStack(ModItems.flesh),
                new Object[] { Item.rottenFlesh });
     }
 }
