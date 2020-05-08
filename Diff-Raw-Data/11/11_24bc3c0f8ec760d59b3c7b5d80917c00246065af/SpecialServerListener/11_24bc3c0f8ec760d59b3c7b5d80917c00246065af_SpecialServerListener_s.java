 /**
  * User: YBogomolov
  * Date: 29.07.11
  * Time: 16:13
  */
 
 package com.github.doodlez.bukkit.globalquest.listeners;
 
 import com.github.doodlez.bukkit.globalquest.GlobalQuestPlugin;
 import com.github.doodlez.bukkit.globalquest.utilities.PrivateFieldHelper;
import net.minecraft.server.CraftingManager;
import net.minecraft.server.CraftingRecipe;
import net.minecraft.server.ItemStack;
import net.minecraft.server.ShapedRecipes;
 import org.bukkit.Material;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 
 import org.bukkit.plugin.Plugin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SpecialServerListener extends ServerListener {
     @Override
     public void onPluginEnable(PluginEnableEvent event) {
         Plugin plugin = event.getPlugin();
 
         if (plugin.getDescription().getName().equals("GlobalQuestPlugin")) {
             try {
                 CraftingManager craftingManager = CraftingManager.getInstance();
                 List<CraftingRecipe> craftList = craftingManager.b();
 
                 List<CraftingRecipe> editedList = new ArrayList<CraftingRecipe>();
 
                 for (CraftingRecipe recipe : craftList) {
                     if (recipe instanceof ShapedRecipes) {
                         ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;
                         ItemStack recipeResult = shapedRecipe.b();
                         String resultName = recipeResult.getItem().j();
 
                         if (GlobalQuestPlugin.blockedRecipes.contains(resultName)) {
                             if (GlobalQuestPlugin.isDebugEnabled)
                                 System.out.print(resultName + " is blocked!");
                             GlobalQuestPlugin.backupRecipes.put(resultName, recipe);
                         }
                         else {
                             editedList.add(recipe);
                         }
                     }
                 }
 
                 try {
                     if (PrivateFieldHelper.setPrivateValue(CraftingManager.class, craftingManager, "b", editedList))
                         System.out.print("All right, set edited list!");
                     else
                         System.out.print("Nope.");
 
                     if (PrivateFieldHelper.setBookStack(1))
                         System.out.print("Set book's maxStackSize to 1.");
                     else
                         System.out.print("Cannot set book's maxStackSize to 1.");
                 }
                 catch (NoSuchFieldException e) {
                     e.printStackTrace();
                 }
             }
             catch (Exception ignored) {}
         }
     }
 
 }
