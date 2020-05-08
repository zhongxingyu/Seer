 /**
  *
  * @author Indivisible0
  */
 package com.github.indiv0.bookdupe;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
import com.github.indiv0.bukkitutils.UtilManager;
 
 public class BookDupe extends JavaPlugin {
     public final UtilManager utilManager = new UtilManager();
     private final int CONFIG_VERSION = 1;
 
     @Override
     public void onLoad() {
         // Initialize all utilities.
         utilManager.initialize(this, CONFIG_VERSION);
     }
 
     @Override
     public void onEnable() {
         // Registers the ItemCraftListener with the PluginManager.
         utilManager.getListenerUtil().registerListener(new ItemCraftListener(this));
 
         // Create two recipes to serve as the basis of the plugin.
         ItemStack result = new ItemStack(Material.BOOK_AND_QUILL);
 
         addShapelessRecipe(result, new Material[] {
                 Material.WRITTEN_BOOK,
                 Material.BOOK_AND_QUILL
         });
         addShapelessRecipe(result, new Material[] {
                 Material.WRITTEN_BOOK,
                 Material.INK_SACK,
                 Material.FEATHER,
                 Material.BOOK
         });
     }
 
     private void addShapelessRecipe(ItemStack result, Material[] materials) {
         // Initializes the recipe to be used to produce the result.
         ShapelessRecipe recipe = new ShapelessRecipe(result);
 
         // Adds all of the required materials to the recipe.
         for (Material material : materials)
             recipe.addIngredient(material);
 
         // Adds the recipe to the server's recipe list.
         getServer().addRecipe(recipe);
     }
 }
