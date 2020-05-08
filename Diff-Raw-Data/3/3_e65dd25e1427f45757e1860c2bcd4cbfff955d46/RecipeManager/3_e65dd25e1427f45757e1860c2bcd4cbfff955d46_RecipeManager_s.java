 package com.ftwinston.KillerMinecraft;
 
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
 			sb.append('1');
 			ShapedRecipe sr = (ShapedRecipe)recipe;
 			Map<Character, ItemStack> ingredients = sr.getIngredientMap();
 			for ( String s : sr.getShape() )
 			{
 				for ( int i=0 ; i<s.length(); i++ )
 				{
 					sb.append(';');
					sb.append(ingredients.get(new Character(s.charAt(i))).hashCode());
 				}
 				sb.append(';');
 			}
 		}
 		else if ( recipe instanceof ShapelessRecipe )
 		{
 			sb.append('0');
 			ShapelessRecipe sr = (ShapelessRecipe)recipe;
 			for ( ItemStack ingredient : sr.getIngredientList() )
 			{
 				sb.append(';');
 				sb.append(ingredient.hashCode());
 			}
 		}
 		else 
 		{
 			KillerMinecraft.instance.log.warning("Unexpected recipe - neither ShapedRecipe or ShapelessRecipe: " + recipe.toString());
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
 		Game game = KillerMinecraft.instance.getGameForWorld(event.getView().getPlayer().getWorld());
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
 
 		// for the sake of the "canCraft" checks looking through the inventory, we set the result to null now.
 		inventory.setResult(null);
 		
 		for ( Recipe other : pluginRecipes )
 			if ( canCraft(inventory, other) )
 			{
 				inventory.setResult(other.getResult());
 				return;
 			}
 	}
 	
 	private boolean canCraft(CraftingInventory inventory, Recipe recipe)
 	{
 		if ( recipe instanceof ShapedRecipe )
 			return canCraftShaped(inventory, (ShapedRecipe)recipe);
 		else if ( recipe instanceof ShapelessRecipe )
 			return canCraftShapeless(inventory, (ShapelessRecipe)recipe);
 		else
 			return false;
 	}
 	
 	// what follows is a reproduction of net.minecraft.server.ShapedRecipes.a(InventoryCrafting, World) and net.minecraft.server.ShapelessRecipes.a(InventoryCrafting, World).
 	// ...because to actually create the relevant stuff to call those manually would be longer, believe it or not.
 	
 	
 	private boolean canCraftShaped(CraftingInventory inventory, ShapedRecipe recipe)
 	{
 		String[] shape = recipe.getShape();
 		int recipeWidth = shape[0].length(), recipeHeight = shape.length;
 		for ( int i=1; i<recipeHeight; i++ )
 			recipeWidth = Math.max(recipeWidth, shape[i].length());
 		Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();
 		
 		ItemStack[] inputItems = inventory.getMatrix();
 		int inventoryWidth = inputItems.length < 9 ? 2 : 3, inventoryHeight = inventoryWidth; // assume the inventory is always 2x2 or 3x3
 		
 		int xOffsetRange = inventoryWidth - recipeWidth, yOffsetRange = inventoryHeight - recipeHeight; 
 		
 		for ( int xOffset = 0; xOffset <= xOffsetRange; xOffset ++ )
 			for ( int yOffset = 0; yOffset <= yOffsetRange; yOffset ++ )
 				if ( canCraftShaped(recipeWidth, recipeHeight, inventoryWidth, inventoryHeight, xOffset, yOffset, inputItems, ingredientMap, shape, false)
 					|| canCraftShaped(recipeWidth, recipeHeight, inventoryWidth, inventoryHeight, xOffset, yOffset, inputItems, ingredientMap, shape, true))
 					return true;
 		
 		return false;
 	}
 
 	private boolean canCraftShaped(int recipeWidth, int recipeHeight, int inventoryWidth, int inventoryHeight, int xOffset, int yOffset, ItemStack[] inputItems, Map<Character, ItemStack> ingredientMap, String[] shape, boolean mirror)
 	{
 		for ( int x = 0; x<inventoryWidth; x++ )
 			for ( int y = 0; y<inventoryHeight; y++ )
 			{
 				int recipeX = x - xOffset, recipeY = y - yOffset;
 				ItemStack inventoryItem = inputItems[x + y*inventoryWidth];
 				if ( recipeX < 0 || recipeX >= recipeWidth || recipeY < 0 || recipeY >= recipeHeight )
 				{
 					if ( inventoryItem != null && inventoryItem.getType() != Material.AIR )
 						return false;
 					continue;
 				}
 				
                 ItemStack recipeItem = ingredientMap.get(mirror
                 		? shape[recipeY].charAt(recipeWidth-recipeX-1)
                 		: shape[recipeY].charAt(recipeX));
                 
                 if (inventoryItem != null || recipeItem != null) {
                     if ((inventoryItem == null && recipeItem != null) || (inventoryItem != null && recipeItem == null))
                         return false;
 
                     if (recipeItem.getType() != inventoryItem.getType())
                         return false;
 
                     if (recipeItem.getDurability() != 32767 && recipeItem.getDurability() != inventoryItem.getDurability())
                         return false;
                 }
 			}
 		
 		return true;
 	}
 	
 	
 	public boolean canCraftShapeless(CraftingInventory inventory, ShapelessRecipe recipe)
 	{
         List<ItemStack> ingredients = recipe.getIngredientList();
         for (int i = 0; i < inventory.getSize(); ++i)
         {
             ItemStack itemstack = inventory.getItem(i);
 
             if (itemstack == null)
             	continue;
             
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
 
             if (!flag)
                 return false;
         }
         
         return ingredients.isEmpty();
     }
 }
