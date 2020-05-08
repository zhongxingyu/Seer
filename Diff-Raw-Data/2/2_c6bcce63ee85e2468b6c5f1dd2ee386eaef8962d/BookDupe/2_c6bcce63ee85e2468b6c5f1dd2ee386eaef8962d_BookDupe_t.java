 package com.github.Indiv0.BookDupe;
 
 import java.io.IOException;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 public class BookDupe extends JavaPlugin {
 
     // Initializes an ItemCraftListener.
     public final ItemCraftListener blockListener = new ItemCraftListener();
 
     public void onEnable() {
         // Retrieves an instance of the PluginManager.
         PluginManager pm = getServer().getPluginManager();
 
         // Registers the blockListener with the PluginManager.
         pm.registerEvents(this.blockListener, this);
 
         // Creates a new recipe to serve as the basis for the book duplication
         // feature.
         ItemStack craftable = new ItemStack(Material.BOOK_AND_QUILL);
         ShapelessRecipe recipe = new ShapelessRecipe(craftable);
         recipe.addIngredient(Material.WRITTEN_BOOK);
         recipe.addIngredient(Material.BOOK_AND_QUILL);
 
         // Adds the recipe to the server.
         getServer().addRecipe(recipe);
 
         // Creates another recipe to serve as a secondary recipe for book
         // duplication.
         recipe = new ShapelessRecipe(craftable);
         recipe.addIngredient(Material.WRITTEN_BOOK);
         recipe.addIngredient(Material.INK_SACK);
         recipe.addIngredient(Material.FEATHER);
         recipe.addIngredient(Material.BOOK);
 
         // Adds the recipe to the server.
         getServer().addRecipe(recipe);
 
         // Enable PluginMetrics.
         enableMetrics();
         
         // Prints a message to the server confirming successful initialization
         // of the plugin.
         PluginDescriptionFile pdfFile = this.getDescription();
         getLogger()
                 .info(pdfFile.getName() + " " + pdfFile.getVersion()
                         + " is enabled.");
     }
     
     private void enableMetrics()
     {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException ex) {
            System.out.println("An error occured while attempting to connect to PluginMetrics.");
         }
     }
 }
