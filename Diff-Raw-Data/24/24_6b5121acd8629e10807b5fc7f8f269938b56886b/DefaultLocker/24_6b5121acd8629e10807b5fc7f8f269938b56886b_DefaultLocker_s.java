 package industrialscience.modules.research.backend;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 
 public class DefaultLocker implements RecipeLocker {
     private Object[] recipe;
     private ItemStack istack;
 
     public DefaultLocker(Object[] recipe, ItemStack istack) {
        this.recipe = recipe;
         this.istack = istack;
     }
 
     @Override
     public void lock() {
     }
 
     @Override
     public void unlock() {
         CraftingManager.getInstance().addRecipe(istack, recipe);
     }
 
 }
