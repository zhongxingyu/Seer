 package com.qzx.au.extras;
 
 import net.minecraft.item.ItemStack;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ChromaRegistry {
 	private static ChromaRegistry registry = new ChromaRegistry();
 
 	private List<ChromaRecipe> recipes = new ArrayList<ChromaRecipe>();
 	private int nr_recipes = 0;
 
 	private ChromaRegistry(){}
 
 	public static void addRecipe(ChromaButton button, int dyeConsumption, ItemStack input, ItemStack output){
 		// ignore duplicate recipes (same button and input)
 		if(ChromaRegistry.hasRecipe(button, input)){
			System.err.println("AU EXTRAS: ignoring duplicate Chroma Infuser recipe");
 			return;
 		}
 		ChromaRegistry.registry.recipes.add(new ChromaRecipe(button, dyeConsumption, input, output));
 		ChromaRegistry.registry.nr_recipes++;
 	}
 
 	public static boolean hasRecipe(ChromaButton findButton, ItemStack findInput){
 		if(findInput == null) return false;
 
 		List<ChromaRecipe> recipes = ChromaRegistry.registry.recipes;
 		int findItemID = findInput.getItem().itemID;
 		int findDamage = findInput.getItemDamage();
 		for(int i = 0; i < ChromaRegistry.registry.nr_recipes; i++){
 			ChromaRecipe recipe = recipes.get(i);
 			ItemStack recipeInput = recipe.input;
 			if(findItemID == recipeInput.getItem().itemID && findDamage == recipeInput.getItemDamage()){
 				if(findButton == null || findButton == recipe.button)
 					return true;
 			}
 		}
 		return false;
 	}
 	public static boolean hasRecipe(ItemStack findInput){
 		return ChromaRegistry.hasRecipe(null, findInput);
 	}
 
 	public static ChromaRecipe getRecipe(ChromaButton findButton, ItemStack findInput){
 		if(findButton == null || findInput == null) return null;
 
 		List<ChromaRecipe> recipes = ChromaRegistry.registry.recipes;
 		int findItemID = findInput.getItem().itemID;
 		int findDamage = findInput.getItemDamage();
 		for(int i = 0; i < ChromaRegistry.registry.nr_recipes; i++){
 			ChromaRecipe recipe = recipes.get(i);
 			ItemStack recipeInput = recipe.input;
 			if(findItemID == recipeInput.getItem().itemID && findDamage == recipeInput.getItemDamage())
 				if(findButton == recipe.button)
 					return recipes.get(i);
 		}
 		return null;
 	}
 }
