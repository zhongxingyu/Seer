 package com.infinitewarp.reasonablerecipes;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 * @todo JavaDoc this class
  */
 public class ReasonableRecipes extends JavaPlugin {
     private List<Recipe> customRecipes;
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         initializeRecipes();
         for (Recipe recipe : customRecipes) {
             getServer().addRecipe(recipe);
         }
     }
 
     private void initializeRecipes() {
         customRecipes = new ArrayList<Recipe>();
 
         for (String recipeDefinition : getConfig().getStringList("shapeless-recipes")) {
            Material result = Material.getMaterial(getResultFromRecipeDefinition(recipeDefinition));
            ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(result.getId()));
            for (String input : getInputsFromRecipeDefinition(recipeDefinition)) {
                recipe.addIngredient(1, Material.getMaterial(input), -1);
             }
            customRecipes.add(recipe);
            getLogger().info("Initialized shapeless recipe '" + recipeDefinition + "'");
         }
     }
 
     private String[] getInputsFromRecipeDefinition(String recipeDefinition) {
         return (String[]) recipeDefinition.replace(" ", "").split("=")[1].split("\\+");
     }
 
     private String getResultFromRecipeDefinition(String recipeDefinition) {
         return recipeDefinition.replace(" ", "").split("=")[0];
     }
 
 }
