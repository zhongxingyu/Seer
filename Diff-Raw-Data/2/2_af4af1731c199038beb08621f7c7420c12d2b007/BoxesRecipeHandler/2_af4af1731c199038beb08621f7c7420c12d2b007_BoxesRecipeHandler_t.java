 package ml.boxes.nei;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ml.boxes.Registry;
 import ml.boxes.recipe.RecipeBox;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraftforge.oredict.OreDictionary;
 import codechicken.nei.InventoryCraftingDummy;
 import codechicken.nei.NEIClientUtils;
 import codechicken.nei.PositionedStack;
 import codechicken.nei.recipe.ShapedRecipeHandler;
 
 public class BoxesRecipeHandler extends ShapedRecipeHandler {
 
 	public class CachedBoxesRecipe extends CachedShapedRecipe{
 		public CachedBoxesRecipe(Object [] rec) {
 			super(3, 3, rec, recipe.getRecipeOutput());
 			cycle();
 		}
 				
 		private void cycle(){
			List<PositionedStack> ingreds = getIngredients();
             for(int i = 0; i < 9; i++)
                 invCrafting.setInventorySlotContents(i, i < ingreds.size() ? ingreds.get(i).item : null);
 			this.result = new PositionedStack(recipe.getCraftingResult(invCrafting), 119, 24);
 		}		
 	}
 	
 	private InventoryCrafting invCrafting = new InventoryCraftingDummy();
 	private RecipeBox recipe = new RecipeBox();
 	private final CachedBoxesRecipe cached;
 	
 	public BoxesRecipeHandler() {
 		ItemStack cb = new ItemStack(Registry.ItemResources);
 		List<ItemStack> dyes = new ArrayList<ItemStack>();
 		for (int i=0; i<16; i++){
 			dyes.addAll(OreDictionary.getOres(OreDictionary.getOreID(new ItemStack(Item.dyePowder, 1, i))));
 		}
 		cached = new CachedBoxesRecipe(new Object[]{cb,cb,cb, cb,dyes,cb, cb,cb,cb});
 	}
 	
 	@Override
 	public void loadCraftingRecipes(String outputId, Object... results) {
 		if (outputId == "crafting"){
 			arecipes.add(cached);
 		} else {
 			super.loadCraftingRecipes(outputId, results);
 		}
 	}
 
 	@Override
 	public void loadCraftingRecipes(ItemStack result) {
 		if (result.isItemEqual(recipe.getRecipeOutput())){
 			arecipes.add(cached);
 		}
 	}
 
 	@Override
 	public void loadUsageRecipes(ItemStack ingredient) {
 		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
 		if (cached.contains(cached.ingredients, ingredient)){
 			cached.setIngredientPermutation(cached.ingredients, ingredient);
 			arecipes.add(cached);
 		}
 	}
 
 	@Override
 	public void onUpdate() {
 		if(!NEIClientUtils.shiftKey())
         {
             cycleticks++;
             if(cycleticks%20 == 0)
                 for(CachedRecipe crecipe : arecipes)
                     ((CachedBoxesRecipe)crecipe).cycle();
         }
 	}
 }
