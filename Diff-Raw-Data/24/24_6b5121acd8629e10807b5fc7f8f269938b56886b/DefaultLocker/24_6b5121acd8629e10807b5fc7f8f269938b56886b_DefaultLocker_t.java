 package industrialscience.modules.research.backend;
 
import java.util.Arrays;

 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 
 public class DefaultLocker implements RecipeLocker {
     private Object[] recipe;
     private ItemStack istack;
 
     public DefaultLocker(Object[] recipe, ItemStack istack) {
    	if(recipe==null){
        	this.recipe=new Object[0];
        }
        else{
        	this.recipe=Arrays.copyOf(recipe, recipe.length);
        }
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
