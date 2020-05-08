 package cooler.utils.handlers;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 import cooler.items.ModItems;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class RecipeHandler {
 
     private static ItemStack cooler = new ItemStack(
             ModItems.cooler, 1,
             OreDictionary.WILDCARD_VALUE);
 
     public static void add() {
         Handler.logName("is Loading it's Recipies");
 
         GameRegistry.addShapelessRecipe(new ItemStack(
                 ModItems.flesh), new Object[] {
                 Item.rottenFlesh, cooler });
 
         GameRegistry.addShapedRecipe(cooler, new Object[] {
                "###", "#X#", "###", '#', Item.snowball,
                'X', Item.ingotIron });
     }
 }
