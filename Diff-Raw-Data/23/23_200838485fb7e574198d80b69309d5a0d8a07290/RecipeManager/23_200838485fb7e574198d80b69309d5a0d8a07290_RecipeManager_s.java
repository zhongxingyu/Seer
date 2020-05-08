 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.bukkit.Material;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.inventory.CraftingInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 class RecipeManager
 {
 	private TreeMap<String, ArrayList<JavaPlugin>> pluginsByRecipeHash = new TreeMap<String, ArrayList<JavaPlugin>>();
 	private HashMap<JavaPlugin, ArrayList<Recipe>> recipesByPlugin = new HashMap<JavaPlugin, ArrayList<Recipe>>();
 	private HashMap<Material, TreeMap<String, Recipe>> recipesByResult = new HashMap<Material, TreeMap<String, Recipe>>();
 	
 	public void registerCustomRecipes(ArrayList<Recipe> recipes, JavaPlugin plugin)
 	{
 		if ( recipes == null )
 			return;
 		
 		recipesByPlugin.put(plugin, recipes);
 		
 		for ( Recipe recipe : recipes )
 		{
 			String hash = determineHash(recipe);
 			ArrayList<JavaPlugin> matches = pluginsByRecipeHash.get(hash);
 			if ( matches == null )
 			{
 				matches = new ArrayList<JavaPlugin>();
 				pluginsByRecipeHash.put(hash,  matches);
 			}
 			matches.add(plugin);
 			
 			Material mat = recipe.getResult().getType();
 			TreeMap<String, Recipe> byResult = recipesByResult.get(mat);
 			if ( byResult == null )
 			{
 				byResult = new TreeMap<String, Recipe>();
 				recipesByResult.put(mat, byResult);
 			}
 			byResult.put(hash, recipe);
 
 			plugin.getServer().addRecipe(recipe);
 		}
 	}
 
 	private String determineHash(Recipe recipe)
 	{
 		StringBuilder sb = new StringBuilder();
 		if ( recipe instanceof ShapedRecipe )
 		{
 			ShapedRecipe sr = (ShapedRecipe)recipe;
 			for ( String s : sr.getShape() )
 			{
 				sb.append(';');
				sb.append(s);
			}
			for ( Map.Entry<Character, ItemStack> entry : sr.getIngredientMap().entrySet() )
			{
				sb.append(';');
				sb.append(entry.getKey());
				sb.append(entry.getValue().hashCode());
 			}
 		}
 		else if ( recipe instanceof ShapelessRecipe )
 		{
 			ShapelessRecipe sr = (ShapelessRecipe)recipe;
 			for ( ItemStack ingredient : sr.getIngredientList() )
 			{
 				sb.append(';');
 				sb.append(ingredient.hashCode());
 			}
 		}
 		else 
 		{
 			Killer.instance.log.warning("Unexpected recipe - neither ShapedRecipe or ShapelessRecipe: " + recipe.toString());
 			return null;
 		}
 		
 		sb.append(';');
 		sb.append(recipe.getResult().hashCode());
 
 		return sb.toString();
 	}
 		
 	public void handleCraftEvent(PrepareItemCraftEvent event)
 	{
 		Recipe recipe = event.getRecipe();
 		
 		TreeMap<String, Recipe> recipes = recipesByResult.get(recipe.getResult().getType());
 		if ( recipes == null )
 			return; // got no custom recipes for this material
 		
 		String hash = determineHash(recipe);
 		if ( !recipes.containsKey(hash) )
 			return; // this specific recipe isn't a custom one
 		
 		// you can't craft a custom recipe if you're not in a game
 		Game game = Killer.instance.getGameForWorld(event.getView().getPlayer().getWorld());
 		if ( game == null )
 		{
 			event.getInventory().setResult(null);
 			return;
 		}
 		
 		// if the current game's plugin defines this recipe, it's allowed
 		JavaPlugin gamePlugin = game.getGameMode().modulePlugin;
 		ArrayList<JavaPlugin> matchingPlugins = pluginsByRecipeHash.get(hash);
 		if ( matchingPlugins == null || matchingPlugins.contains(gamePlugin) )
 			return;
 		
 		// otherwise, we need to see if there's a recipe defined by the current game's plugin that could look the same in the craft inventory 
 		ArrayList<Recipe> pluginRecipes = recipesByPlugin.get(gamePlugin);
 		CraftingInventory inventory = event.getInventory();
 		if ( pluginRecipes == null || pluginRecipes.size() == 0 )
 		{
 			inventory.setResult(null);
 			return;
 		}
 				
 		for ( Recipe other : pluginRecipes )
 			if ( canCraft(inventory, other) )
 			{
 				// this doesn't account for the if the "new" recipe uses fewer items than the current one
 				inventory.setResult(other.getResult());
 				return;
 			}
 		
 		inventory.setResult(null);
 	}
 	
 	private boolean canCraft(CraftingInventory inventory, Recipe recipe)
 	{
 		if ( recipe instanceof ShapedRecipe )
 			return canCraft(inventory, (ShapedRecipe)recipe);
 		else if ( recipe instanceof ShapelessRecipe )
 			return canCraft(inventory, (ShapelessRecipe)recipe);
 		else
 			return false;
 	}
 	
 	// what follows is a reproduction of net.minecraft.server.ShapedRecipes.a(InventoryCrafting, World) and net.minecraft.server.ShapelessRecipes.a(InventoryCrafting, World).
 	// ...because to actually create the relevant stuff to call those manually would be longer, believe it or not.
 	
 	private boolean canCraft(CraftingInventory inventory, ShapedRecipe recipe)
 	{
 		int width = recipe.getShape()[0].length(), height = recipe.getShape().length;
 		for (int xOffset = 0; xOffset <= 3 - width; ++xOffset) {
             for (int yOffset = 0; yOffset <= 3 - height; ++yOffset) {
                 if (canCraft(inventory, recipe, xOffset, yOffset, true)
                  || canCraft(inventory, recipe, xOffset, yOffset, false))
                 	return true;
             }
         }
 
         return false;
 	}
 	
 	private boolean canCraft(CraftingInventory inventory, ShapedRecipe recipe, int xOffset, int yOffset, boolean flag)
 	{
		ItemStack[] items = (ItemStack[])recipe.getIngredientMap().values().toArray();
 		
 		int width = recipe.getShape()[0].length(), height = recipe.getShape().length;
         for (int x = 0; x < 3; ++x) {
             for (int y = 0; y < 3; ++y) {
                 int x1 = x - xOffset;
                 int y1 = y - yOffset;
                 ItemStack itemstack = null;
 
                 if (x1 >= 0 && y1 >= 0 && x1 < width && y1 < height) {
                     if (flag) {
                         itemstack = items[width - x1 - 1 + y1 * width];
                     } else {
                         itemstack = items[x1 + y1 * width];
                     }
                 }
 
                 ItemStack itemstack1 = inventory.getItem(x + y * 3);
 
                 if (itemstack1 != null || itemstack != null) {
                     if (itemstack1 == null && itemstack != null || itemstack1 != null && itemstack == null) {
                         return false;
                     }
 
                     if (itemstack.getType() != itemstack1.getType()) {
                         return false;
                     }
 
                     if (itemstack.getDurability() != 32767 && itemstack.getDurability() != itemstack1.getDurability()) {
                         return false;
                     }
                 }
             }
         }
 
         return true;
     }
 	
 	public boolean canCraft(CraftingInventory inventory, ShapelessRecipe recipe)
 	{
         List<ItemStack> ingredients = recipe.getIngredientList();
 
         for (int i = 0; i < inventory.getSize(); ++i)
         {
             ItemStack itemstack = inventory.getItem(i);
 
             if (itemstack != null) {
                 boolean flag = false;
                 Iterator<ItemStack> iterator = ingredients.iterator();
 
                 while (iterator.hasNext()) {
                     ItemStack itemstack1 = iterator.next();
 
                     if (itemstack.getType() == itemstack1.getType() && (itemstack1.getDurability() == 32767 || itemstack.getDurability() == itemstack1.getDurability())) {
                         flag = true;
                         ingredients.remove(itemstack1);
                         break;
                     }
                 }
 
                 if (!flag) {
                     return false;
                 }
             }
         }
 
         return ingredients.isEmpty();
     }
 }
