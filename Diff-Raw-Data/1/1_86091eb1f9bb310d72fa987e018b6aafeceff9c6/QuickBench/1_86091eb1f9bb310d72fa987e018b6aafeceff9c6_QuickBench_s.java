 /*
 Copyright (c) 2012, Mushroom Hostage
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.exphc.QuickBench;
 
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.lang.Byte;
 import java.lang.reflect.*;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.inventory.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.*;
 
 import net.minecraft.server.CraftingManager;
 
 import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 
 // A crafting recipe that lets you see into its ingredients!
 class TransparentRecipe {
     static QuickBench plugin; 
     static Class IElectricItem;
 
     // Each ingredient which must be present; outer = all, inner = any, e.g. (foo OR bar) AND (baz) AND (quux)
     // The inner list is the alternatives; it may just have one ItemStack, or more
     // This is expressiveness (not provided by Bukkit wrappers) is necessary to support ore dictionary recipes
     // There may be nulls -- which are skipped during matching, but used for ordering on the grid
     ArrayList<ArrayList<ItemStack>> ingredientsList;
 
     // For shaped recipes, width of the crafting matrix to put the ingredients on
     // This is used to shape the 1-dimensional ingredientsList into a 2-dimensional matrix
     int width;
 
     // Crafting result output for matching - this is NOT computed, and should only be used for matching!
     // Not for getting the item made after crafting - for that, see canCraft PrecraftedResult output
     // i.e., this is an _input_ to searching the recipe's output, not the _output_ of crafting itself
     ItemStack outputMatch;
 
     // Name of recipe class for debugging
     String className;
 
     // Internal recipe so we can call it to get computed crafting result
     net.minecraft.server.CraftingRecipe/*MCP IRecipe*/ opaqueRecipe;
 
     @SuppressWarnings("unchecked")
     public TransparentRecipe(net.minecraft.server.CraftingRecipe/*MCP IRecipe*/ opaqueRecipe) {
 
         this.opaqueRecipe = opaqueRecipe;
 
         // Get recipe result - for matching only (not for adding, for that again see PrecraftedResult)
         outputMatch = new CraftItemStack(opaqueRecipe.b()); // MCP getResult()
 
         // Get recipe ingredients
         ingredientsList = new ArrayList<ArrayList<ItemStack>>();
 
         width = 3; // assume on 3x3 grid for shapeless recipes
 
         className = opaqueRecipe.getClass().getName();
 
         // For vanilla recipes, Bukkit's conversion wrappers are fine
         if (opaqueRecipe instanceof net.minecraft.server.ShapelessRecipes) {
             ShapelessRecipe shapelessRecipe = ((net.minecraft.server.ShapelessRecipes)opaqueRecipe).toBukkitRecipe();
             List<ItemStack> ingredientList = shapelessRecipe.getIngredientList();
 
             // Shapeless recipes are a simple list of everything we need, 1:1
             for (ItemStack ingredient: ingredientList) {
                 if (ingredient != null) {
                     ArrayList<ItemStack> innerList = new ArrayList<ItemStack>();
                     innerList.add(ingredient);    // no alternatives, 1-element set
                     ingredientsList.add(innerList);
                 }
             }
         } else if (opaqueRecipe instanceof net.minecraft.server.ShapedRecipes) {
             ShapedRecipe shapedRecipe = ((net.minecraft.server.ShapedRecipes)opaqueRecipe).toBukkitRecipe();
             Map<Character,ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
 
             // Shaped recipes' order doesn't matter for us, but the count of each ingredient in the map does
             for (String shapeLine: shapedRecipe.getShape()) {
                 for (int i = 0; i < shapeLine.length(); i += 1) {
                     char code = shapeLine.charAt(i);
                     if (code == ' ') {
                         // positional placeholder
                         ingredientsList.add(null);
                         continue;
                     }
 
                     ItemStack ingredient = ingredientMap.get(code);
                     if (ingredient == null) {
                         // positional placeholder
                         ingredientsList.add(null);
                         continue;
                     }
 
                     ArrayList<ItemStack> innerList = new ArrayList<ItemStack>();
                     innerList.add(ingredient);    // no alternatives, 1-element set
                     ingredientsList.add(innerList);
                 }
             }
 
             // Get width of shaped recipes
             try {
                 Field field = opaqueRecipe.getClass().getDeclaredField("width");
                 field.setAccessible(true);
                 width = field.getInt(opaqueRecipe);
             } catch (Exception e) {
                 plugin.logger.warning("Failed to reflect on net.minecraft.server.ShapedRecipes width for "+outputMatch);
                 e.printStackTrace();
                 throw new IllegalArgumentException(e);
             }
         } else if (className.equals("forge.oredict.ShapedOreRecipe")) {
             // Forge ore recipes.. we're on our own
             Object[] inputs = null;
             try {
                 Field field = opaqueRecipe.getClass().getDeclaredField("input");
                 field.setAccessible(true);
                 inputs = (Object[])field.get(opaqueRecipe);
             } catch (Exception e) {
                 plugin.logger.warning("Failed to reflect on forge.oredict.ShapedOreRecipe for "+outputMatch);
                 e.printStackTrace();
                 throw new IllegalArgumentException(e);
             }
             if (inputs == null) {
                 throw new IllegalArgumentException("Uncaught error reflecting on forge.oredict.ShapedOreRecipe");
             }
 
             for (Object input: inputs) {
                 // Each element is either a singular item, or list of possible items (populated from ore dictionary)
                 // Fortunately, our data structure is similar, just need to convert the types
                 
                 ArrayList<ItemStack> innerList = new ArrayList<ItemStack>();
 
                 if (input instanceof net.minecraft.server.ItemStack) {
                     innerList.add(new CraftItemStack((net.minecraft.server.ItemStack)input));
                 } else if (input instanceof ArrayList) {
                     for (net.minecraft.server.ItemStack alternative: (ArrayList<net.minecraft.server.ItemStack>)input) {
                         innerList.add(new CraftItemStack(alternative));
                     }
                 } else if (input == null) {
                     // positional placeholder
                     ingredientsList.add(null);
                     continue;
                 } else {
                     throw new IllegalArgumentException("forge.oredict.ShapedOreRecipe unknown input: " + input + ", in "+inputs);
                 }
 
                 ingredientsList.add(innerList);
             }
 
             // Get width of shaped recipes
             try {
                 Field field = opaqueRecipe.getClass().getDeclaredField("width");
                 field.setAccessible(true);
                 width = field.getInt(opaqueRecipe);
             } catch (Exception e) {
                 plugin.logger.warning("Failed to reflect on forge.oredict.ShapedOreRecipe width for "+outputMatch);
                 e.printStackTrace();
                 throw new IllegalArgumentException(e);
             }
         } else if (className.equals("forge.oredict.ShapelessOreRecipe")) {
             // Forge ore shapeless is very similar, except inputs are a list instead of array
             ArrayList inputs = null;
             try {
                 Field field = opaqueRecipe.getClass().getDeclaredField("input");
                 field.setAccessible(true);
                 inputs = (ArrayList)field.get(opaqueRecipe);
             } catch (Exception e) {
                 plugin.logger.warning("Failed to reflect on forge.oredict.ShapelessOreRecipe for "+outputMatch);
                 e.printStackTrace();
                 throw new IllegalArgumentException(e);
             }
             if (inputs == null) {
                 throw new IllegalArgumentException("Uncaught error reflecting on forge.oredict.ShapelessOreRecipe");
             }
 
             for (Object input: inputs) {
                 ArrayList<ItemStack> innerList = new ArrayList<ItemStack>();
                 if (input instanceof net.minecraft.server.ItemStack) {
                     innerList.add(new CraftItemStack((net.minecraft.server.ItemStack)input));
                 } else if (input instanceof ArrayList) {
                     for (net.minecraft.server.ItemStack alternative: (ArrayList<net.minecraft.server.ItemStack>)input) {
                         innerList.add(new CraftItemStack(alternative));
                     }
                 } else {
                     throw new IllegalArgumentException("forge.oredict.ShapelessOreRecipe unknown input: " + input + ", in "+inputs);
                 }
                 ingredientsList.add(innerList);
             }
 
         } else if (className.equals("ic2.common.AdvShapelessRecipe") || className.equals("ic2.common.AdvRecipe")) {
             // IndustrialCraft^2 shapeless and shaped recipes have the same ingredients list
             // This is for 1.97 - for 1.95b see QuickBench 2.1
 
             boolean hidden = false;
 
             Object[] inputs = null;
             try {
                 Field field = opaqueRecipe.getClass().getDeclaredField("input");
                 field.setAccessible(true);
                 inputs = (Object[])field.get(opaqueRecipe);
             } catch (Exception e) {
                 plugin.logger.warning("Failed to reflect on ic2.common.AdvRecipe for "+outputMatch);
                 e.printStackTrace();
                 throw new IllegalArgumentException(e);
             }
             if (inputs == null) {
                 throw new IllegalArgumentException("Uncaught error reflecting on ic2.common.AdvRecipe");
             }
 
             for (Object input: inputs) {
                 ArrayList<ItemStack> innerList = new ArrayList<ItemStack>();
 
                 // see also ic2.common.AdvRecipe.resolveOreDict, calls forge.oredict.OreDictionary.getOres((String)obj), gets a list
                 // if the ingredient was a string, or wraps ItemStack in 1-element list if is an ItemStack
                 if (input instanceof String) {
                     ArrayList<net.minecraft.server.ItemStack> alternatives = forge.oredict.OreDictionary.getOres((String)input)/*MCPC only!*/;
                     for (net.minecraft.server.ItemStack alternative: alternatives) {
                         innerList.add(new CraftItemStack(alternative));
                     }
                 } else if (input instanceof net.minecraft.server.ItemStack) {
                     innerList.add(new CraftItemStack((net.minecraft.server.ItemStack)input));
                 } else if (input instanceof Boolean) {  // TODO: not detected?
                     hidden = ((Boolean)input).booleanValue();
                     plugin.log("hidden = " + hidden); // TODO: option to skip secret hidden recipes (UU matter, nukes)
                 } else if (input == null) {
                     // positional placeholder
                     ingredientsList.add(null);
                     continue;
                 } else {
                     throw new IllegalArgumentException("ic2.common.AdvRecipe unknown input: " + input + ", in "+inputs);
                 }
 
                 // and also public ItemStack b(InventoryCrafting inventorycrafting) = getCraftingResult in IC2 - it transfers charge to/from electric items
 
                 ingredientsList.add(innerList);
             }
 
             // Get width of shaped recipes
             if (className.equals("ic2.common.AdvRecipe")) {
                 try {
                     Field field = opaqueRecipe.getClass().getDeclaredField("width");
                     field.setAccessible(true);
                     width = field.getInt(opaqueRecipe);
                 } catch (Exception e) {
                     plugin.logger.warning("Failed to reflect on ic2.common.AdvRecipe width for "+outputMatch);
                     e.printStackTrace();
                     throw new IllegalArgumentException(e);
                 }
             }
         } else {
             throw new IllegalArgumentException("Unsupported recipe class: " + className + " of " + opaqueRecipe);
         }
 
         // TODO: eloraam.core.CoverRecipe (RedPower)
         // TODO: codechicken.enderstorage.EnderChestRecipe (EnderStorage)
         // TODO: nuclearcontrol.StorageArrayRecipe (IC2 Nuclear Control 1.1.10+)
 
     }
 
     /** Get whether array of item stacks has all of the recipe inputs. 
     Returns null if not, otherwise a PrecraftingResult with output and updated inputs. 
     */
     public PrecraftedResult canCraft(final ItemStack[] inputs) {
         plugin.log("- testing class="+className+" w="+width+" outputMatch="+describeItem(outputMatch)+" inputs=" + inputs + " vs ingredientsList=" + ingredientsList);
 
         // Clone inventory so don't modify original - but we'll modify accum, taking away what we need for crafting
         ItemStack[] accum = cloneItemStacks(inputs);
 
         // ... and putting them here, positionally matching the recipe ingredientsList
         List<ItemStack> takenItems = new ArrayList<ItemStack>();
 
         // Remove items as we go, ensuring we can successfully remove everything
         for (ArrayList<ItemStack> alternativeIngredients: ingredientsList) {
             boolean have = false;
 
             if (alternativeIngredients == null) {
                 // positional placeholder, we don't care
                 takenItems.add(null);
                 continue;
             }
 
             // Do we have any of the ingredients?
             for (ItemStack ingredient: alternativeIngredients) {
                 if (ingredient == null) {
                     continue;
                 }
 
                 ItemStack takenItem = takeItem(accum, ingredient);
 
                 plugin.log("  ~ taking "+describeItem(ingredient)+" takenItem="+describeItem(takenItem));
                 // TODO: ensure taken item has all important tags - test IC2 lapotron crafting, should take energy crystal with charge right? XXX
                 // TODO: appears not - tag loss? try crafting fully charged energy crystal (30241:1) into lapotron - should get partly charged 30240:23
                 // not discharged :27 -> :26
 
                 if (takenItem != null) {
                     // Take it!
                     takenItems.add(takenItem);
                     have = true;
                     break;
                 }
             }
 
             if (!have) {
                 plugin.log(" - can't craft, missing any of " + alternativeIngredients);
                 return null;
             }
         }
         plugin.log(" + craftable with "+inputs);
 
         // Synthesize a crafting grid
         int size = ingredientsList.size();
         int height = (int)(Math.ceil(size * 1.0 / width));
 
         plugin.log(" + recipe size "+size+" = "+width+"x"+height);
 
         // always make it 3x3, since all recipes can fit on it
         int gridWidth = 3, gridHeight = 3;
         net.minecraft.server.InventoryCrafting inventoryCrafting = new net.minecraft.server.InventoryCrafting(new DeafContainer(), gridHeight, gridWidth);
         // .. put taken items onto the grid
         for (int i = 0; i < takenItems.size(); i += 1) {
             if (takenItems.get(i) == null) {
                 continue;
             }
 
             net.minecraft.server.ItemStack takenItem = ((CraftItemStack)(takenItems.get(i))).getHandle().cloneItemStack(); // preserve tags
 
             // rescale recipe to fit on larger grid
             int x = i % width;
             int y = i / width;
 
             int at = x + y * gridWidth;
 
             inventoryCrafting.setItem(at, takenItem);
         }
 
         // .. and feed it to the recipe to get the actual result
         net.minecraft.server.ItemStack rawFinalResult = opaqueRecipe.b/*MCP getCraftingResult*/(inventoryCrafting);
 
         if (rawFinalResult == null) {
             // This shouldn't happen.. it means we matched the recipe inputs, but then tried to arrange
             // them on the grid and the recipe said it didn't know what we were talking about..
             throw new IllegalArgumentException("IRecipe getCraftingResult("+inventoryCrafting+") unexpectedly returned null trying to craft"+outputMatch);
         }
 
         ItemStack finalResult = new CraftItemStack(rawFinalResult);
 
         plugin.log("  ++ finalResult="+rawFinalResult+" == "+finalResult);
 
         return new PrecraftedResult(finalResult, accum);
     }
 
     /** Take an item from an inventory.
     Mutates inventory, removing it from the stack.
     Returns the taken item. 
     */
     private static ItemStack takeItem(ItemStack[] inventory, ItemStack matchItem) {
         if (matchItem.getAmount() != 1) {
             // we only expect ingredients to be of one item (otherwise, canCraft alternative loop is broken)
             throw new IllegalArgumentException("unexpected quantity from takeItem: " + describeItem(matchItem)+ ", getAmount="+matchItem.getAmount()+" != 1");
         }
     
         int i = 0;
 
         for (ItemStack slot: inventory) {
             // matching item? (ignores tags)
             if (slot != null && itemMatches(slot, matchItem)) {
                 // take one and return it
                 if (slot.getAmount() == 1) {
                     inventory[i] = null;
                 } else {
                     slot.setAmount(slot.getAmount() - 1);
                 }
 
                 // Split off a taken item
                 // This is needed so that the electric item is matched by id only, but you get it back with the charge tags, too
                 // (returned result has more information than matchItem you're looking for)
                 CraftItemStack takenItem = new CraftItemStack(((CraftItemStack)slot).getHandle().cloneItemStack()); // preserve tags
                 takenItem.setAmount(1);
 
                 return takenItem;
             }
 
             i += 1;
         }
 
         return null;
     }
 
     /** Return whether an item matches given criteria.
     Must have same type ID. matchItem damage can be -1 to match any damage (wildcard).
     Electric items match based on type ID only, as if the damage wildcard was set.
     Note this is NOT commutative; the 'matchItem' criteria is less general than the 'item'!
     */
     public static boolean itemMatches(ItemStack item, ItemStack matchItem) {
         return item.getTypeId() == matchItem.getTypeId() &&
             (matchItem.getDurability() == -1 ||
             isElectricItem(matchItem) ||        // IC2 electric items not matched on damage, see AdvRecipe (test with MFE recipe & energy crystals)
             (item.getDurability() == matchItem.getDurability()));
     }
 
     /** Return whether item is from IC2 and can hold an electric charge. */
     public static boolean isElectricItem(ItemStack item) {
         if (IElectricItem == null) {
             // IC2 isn't installed
             return false;
         }
 
         int id = item.getTypeId();
 
         net.minecraft.server.Item rawItem = net.minecraft.server.Item.byId[id];
 
         // this is like 'instanceof IElectricItem', but dynamic
         boolean isElectric = IElectricItem.isInstance(rawItem);
 
         //plugin.log("is electric? " + item + " = " + isElectric + " raw="+rawItem);
 
         return isElectric;
     }
 
     /** Show human-readable description of item, for debugging purposes.
     This is way better than ItemStack toString()
     */
     public static String describeItem(ItemStack item) {
         if (item == null) {
             return "ItemStack null";
         }
         String s = "ItemStack "+item.getAmount()+"x"+item.getTypeId()+":"+item.getDurability()+" ("+item.getType()+")";
         if (!(item instanceof CraftItemStack)) {
             return s + " (not CraftItemStack!)";
         } else {
             net.minecraft.server.ItemStack realItem = ((CraftItemStack)item).getHandle();
             s += " tag="+realItem.tag; // TODO: dump
 
             return s;
         }
     }
 
     private static ItemStack[] cloneItemStacks(ItemStack[] original) {
         // TODO: better way to deep copy array?
         ItemStack[] copy = new ItemStack[original.length];
         for (int i = 0; i < original.length; i += 1) {
             if (original[i] != null) {
                 //copy[i] = original[i].clone(); // NEVER USE Bukkit ItemStack clone!!! loses tags
                 if (original[i] instanceof CraftItemStack) {
                     copy[i] = new CraftItemStack(((CraftItemStack)original[i]).getHandle().cloneItemStack()); // preserve tags
                 } else {
                     plugin.log("cloneItemStack not CraftItemStack: " + original[i]);
                     copy[i] = original[i].clone();
                 }
             }
         }
 
         return copy;
     }
 
     /** Return all items which can be crafted using given inputs from player. */
     public static ArrayList<PrecraftedResult> precraft(final ItemStack[] inputs) {
         ArrayList<PrecraftedResult> outputs = new ArrayList<PrecraftedResult>();
         int recipeCount = 0;
 
         // TODO: have a pure Bukkit API fallback in case things go wrong (like in QuickBench 2.x series; uses iterator / Bukkit.getServer().getRecipesFor, etc.)
         List opaqueRecipes = net.minecraft.server.CraftingManager.getInstance().getRecipies();
 
         for (Object recipeObject: opaqueRecipes) {
             net.minecraft.server.CraftingRecipe opaqueRecipe = (net.minecraft.server.CraftingRecipe)recipeObject;
 
             try {
                 TransparentRecipe recipe = new TransparentRecipe(opaqueRecipe);
 
                 PrecraftedResult precraftedResult = recipe.canCraft(inputs);
 
                 if (precraftedResult != null) { 
                     // TODO: should we de-duplicate multiple recipes to same result? I'm thinking not, to support different ingredient inputs (positional)
                     // (or have an option to)
                     outputs.add(precraftedResult);
                 }
                 // TODO: XXX: get and save updated inventory! precraftedResult.inventory
                 // need to return any items back to the user, for example: vanilla cake, RP2 wool card, diamond drawplate
             } catch (Exception e) {
                 plugin.log("precraft skipping recipe: "+opaqueRecipe);
                 e.printStackTrace();
             }
         }
 
         plugin.log("Total recipes: " + recipeCount + ", craftable: " + outputs.size());
 
         return outputs;
     }
 }
 
 /** A container (event handler) for listening to crafting matrix changes that doesn't really listen. */
 class DeafContainer extends net.minecraft.server.Container {
     @Override
     public boolean b/*MCP canInteractWith*/(net.minecraft.server.EntityHuman entityhuman) {
         return true;
     }
 }
 
 class PrecraftedResult {
     // What you get out of crafting
     // This is the computed output from getCraftingResult
     ItemStack computedOutput;
 
     // The complete altered player inventory, with recipe inputs removed/updated
     ItemStack[] updatedInventory;
 
     public PrecraftedResult(ItemStack computedOutput, ItemStack[] updatedInventory) {
         this.computedOutput = computedOutput;
         this.updatedInventory = updatedInventory;
     }
 
     public String toString() {
         return "PrecraftedResult computedOutput="+computedOutput+", inventory="+updatedInventory;
     }
 }
 
 class QuickBenchListener implements Listener {
     QuickBench plugin;
 
     final int QUICKBENCH_BLOCK_ID;
     final byte QUICKBENCH_BLOCK_DATA;
     final int QUICKBENCH_ITEM_ID;
     final static Enchantment QUICKBENCH_ITEM_TAG = Enchantment.FIRE_ASPECT;
     final String QUICKBENCH_TITLE;
 
     // Map from Player UUID to array list of precrafted results being displayed to the user
     Map<UUID, ArrayList<PrecraftedResult>> openPrecraftedResults;
 
     public QuickBenchListener(QuickBench plugin) {
         this.plugin = plugin;
 
         QUICKBENCH_BLOCK_ID = plugin.getConfig().getInt("quickBench.blockId", Material.LAPIS_BLOCK.getId());
         QUICKBENCH_BLOCK_DATA = (byte)plugin.getConfig().getInt("quickBench.blockData", 1);
         QUICKBENCH_ITEM_ID = plugin.getConfig().getInt("quickBench.itemId", Material.WORKBENCH.getId());
         QUICKBENCH_TITLE = plugin.getConfig().getString("quickBench.title", "QuickBench");
 
         openPrecraftedResults = new HashMap<UUID, ArrayList<PrecraftedResult>>();
 
         loadRecipe();
 
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
    
     public boolean isQuickBench(Block block) {
         return block.getTypeId() == QUICKBENCH_BLOCK_ID && block.getData() == QUICKBENCH_BLOCK_DATA;
     }
 
     public boolean isQuickBench(ItemStack item) {
         return item.getTypeId() == QUICKBENCH_ITEM_ID && item.containsEnchantment(QUICKBENCH_ITEM_TAG);
     }
 
     public ItemStack getQuickBenchItem() {
         ItemStack item = new ItemStack(QUICKBENCH_ITEM_ID, 1);
         item.addUnsafeEnchantment(QUICKBENCH_ITEM_TAG, 1);
 
         return item;
     }
 
     private void loadRecipe() {
         if (plugin.getConfig().getBoolean("quickBench.enableCrafting", true)) {
             ShapelessRecipe recipe = new ShapelessRecipe(getQuickBenchItem());
 
             recipe.addIngredient(1, Material.WORKBENCH);
             recipe.addIngredient(1, Material.BOOK);
 
             Bukkit.addRecipe(recipe);
         }
     }
 
     // Open clicked QuickBench
     @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true) 
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         Block block = event.getClickedBlock();
 
         if (block != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && isQuickBench(block)) {
             if (!player.hasPermission("quickbench.use")) {
                 String message = plugin.getConfig().getString("quickbench.useDeniedMessage", "You do not have permission to use this QuickBench.");
                 if (message != null) {
                     player.sendMessage(message);
                 }
                 return;
             }
 
             ArrayList<PrecraftedResult> precraftedResults = TransparentRecipe.precraft(player.getInventory().getContents());
 
             // Store computed results for actually crafting when click
             openPrecraftedResults.put(player.getUniqueId(), precraftedResults);
 
             final int ROW_SIZE = 9;
             int rows = (int)Math.max(plugin.getConfig().getInt("quickBench.minSizeRows", 0), Math.ceil(precraftedResults .size() * 1.0 / ROW_SIZE));
 
             // Note: >54 still shows dividing line on client, but can interact
             Inventory inventory = Bukkit.createInventory(player, ROW_SIZE * rows, QUICKBENCH_TITLE);
 
             for (int i = 0; i < Math.min(precraftedResults.size(), inventory.getSize()); i += 1) {
                 inventory.setItem(i, precraftedResults.get(i).computedOutput);
             }
 
             player.openInventory(inventory);
 
             // don't let, for example, place a block AND open the QuickBench..
             event.setCancelled(true);
         }
     }
 
     public List<Recipe> getRecipesForX(ItemStack item) {
         // XXX: either implement, or replace with click-location-based tracking (more reliable? for charging)
         return null;
     }
 
     // XXX: replace by TransparentRecipe
     Collection<ItemStack> getRecipeInputs(Recipe recipe) {
         return (recipe instanceof ShapedRecipe) ?  ((ShapedRecipe)recipe).getIngredientMap().values() : ((ShapelessRecipe)recipe).getIngredientList();
     }
 
     @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
     public void onInventoryClickWrapper(InventoryClickEvent event) {
         // If something goes wrong, deny the event to try to avoid duping items
         try {
             onInventoryClick(event);
         } catch (Exception e) {
             plugin.logger.warning("onInventoryClick exception: " + e);
             e.printStackTrace();
 
             event.setResult(Event.Result.DENY);
         }
     }
 
     // Craft on inventory click
     // do NOT add EventHandler here
     public void onInventoryClick(InventoryClickEvent event) {
         InventoryView view = event.getView();
 
         if (view == null || view.getTitle() == null || !view.getTitle().equals(QUICKBENCH_TITLE)) {
             return;
         }
 
         // Click to craft
 
         HumanEntity player = event.getWhoClicked();
         ItemStack clickedItem = event.getCurrentItem();
 
         plugin.log("click "+event);
         plugin.log("cur item = "+clickedItem);
         plugin.log("shift = "+event.isShiftClick());
         // TODO: shift-click to craft all?
         plugin.log("raw slot = "+event.getRawSlot());
 
         if (event.getRawSlot() >= view.getTopInventory().getSize()) {
             // clicked player inventory (bottom)
 
             if (event.isShiftClick()) {
                 // shift-click would player inventory -> quickbench, deny
                 event.setResult(Event.Result.DENY);
             }
 
             // otherwise, let manipulate their own player inventory
             return;
         }
 
         if (clickedItem == null || clickedItem.getType() == Material.AIR) {
             // dropped item (raw slot -999) or empty slot
             event.setResult(Event.Result.DENY);
             return;
         }
 
         Inventory playerInventory = view.getBottomInventory();
         ItemStack[] playerContents = playerInventory.getContents();
 
         // Lookup the item they clicked from the precrafted results, based on the slot index
         ArrayList<PrecraftedResult> precraftedResults = openPrecraftedResults.get(player.getUniqueId());
         if (precraftedResults == null) {
             plugin.logger.warning("Player "+player+" clicked without an open QuickBench");
             event.setResult(Event.Result.DENY);
             return;
         }
 
         PrecraftedResult precraftedResult = precraftedResults.get(event.getRawSlot());
         if (precraftedResult == null) {
             plugin.logger.warning("Player "+player+" clicked a slot without any result");
             event.setResult(Event.Result.DENY);
             return;
         }
 
         plugin.log("precraftedResult = "+precraftedResult);
 
         // ..expected to click the same item in the inventory as we recorded in its slot
         // if not, then our server-side state is out of sync with the client or there's a bug somewhere
         if (!TransparentRecipe.itemMatches(precraftedResult.computedOutput, clickedItem)) {
             plugin.logger.warning("Player "+player+" clicked "+clickedItem+" but expected "+precraftedResult);
             event.setResult(Event.Result.DENY);
             return;
         }
 
 
         // Remove crafting inputs ingredients (pre-computed from existing player inventory)
         player.getInventory().setContents(precraftedResult.updatedInventory);
 
         // add to player inventory when clicked
         HashMap<Integer,ItemStack> overflow = view.getBottomInventory().addItem(precraftedResult.computedOutput);
 
         // drop excess items on the floor (easier than denying the event.. maybe better?)
         for (ItemStack excessItem: overflow.values()) {
             player.getWorld().dropItemNaturally(player.getLocation(), excessItem);
         }
 
 /* TODO: call this for real - postcraft()
         // Post-crafting hooks:
         // SlotCrafting onPickupFromSlot(ItemStack) = SlotResult c
         // FMLServerHandler.instance().onItemCrafted(thePlayer, par1ItemStack, craftMatrix);
         // ForgeHooks.onTakenFromCrafting(thePlayer, par1ItemStack, craftMatrix);
         // hooks added in https://github.com/MinecraftForge/MinecraftForge/blob/master/forge/patches/minecraft_server/net/minecraft/src/SlotCrafting.java.patch
         // vanilla also checks: getContainerItem, doesContainerItemLeaveCraftingGrid.. for cake recipe (milk buckets -> empty bucket)
         // so we really should call SlotResult c(ItemStack) here
 
         net.minecraft.server.EntityHuman entityhuman = ((org.bukkit.craftbukkit.entity.CraftPlayer)player).getHandle();
         net.minecraft.server.IInventory inventoryExtractFrom = null; // TODO: needed?
         int slotIndex = 0;
         int xDisplayPosition = 0;
         int yDisplayPosition = 0;
 
         net.minecraft.server.SlotResult slotResult = new net.minecraft.server.SlotResult(
             entityhuman,  // "The player that is using the GUI where this slot resides"
             inventoryCrafting, // "The craft matrix inventory linked to this result slot"
             inventoryExtractFrom, // "The inventory we want to extract a slot from."
             slotIndex,  // "The index of the slot in the inventory"
             xDisplayPosition,  // "display position of the inventory slot on the screen x axis"
             yDisplayPosition); // "display position of the inventory slot on the screen y axis"
 
         slotResult.c(rawFinalResult); // MCP onPickupFromSlot - mutates inventoryCrafting
 
         for (net.minecraft.server.ItemStack leftoverItem: inventoryCrafting.getContents()) {
             plugin.log(" ! leftover: " + leftoverItem + " = " + new CraftItemStack(leftoverItem));
         }
         */
 
 
 
 
         // Update crafting results with new possibilities
         // TODO: what's the deal with some items disappearing? plantballs
         ArrayList<PrecraftedResult> newPrecraftedResults = TransparentRecipe.precraft(player.getInventory().getContents());
         openPrecraftedResults.put(player.getUniqueId(), newPrecraftedResults);
 
         if (newPrecraftedResults.size() > view.getTopInventory().getSize()) {
             // TODO: improve.. but can't resize window? close and reopen
             ((Player)player).sendMessage("More crafting outputs available than shown here - reopen to see full list!");
             newPrecraftedResults = (ArrayList<PrecraftedResult>)newPrecraftedResults.subList(0, view.getTopInventory().getSize());
         }
 
         view.getTopInventory().setContents(itemStackArray(newPrecraftedResults));
 
 
         // don't let pick up
         event.setResult(Event.Result.DENY);
     }
 
     public ItemStack[] itemStackArray(List<PrecraftedResult> list) {
         ItemStack[] array = new ItemStack[list.size()];
 
         // TODO: list.toArray()? returns Object[]
         for (int i = 0; i < list.size(); i += 1) {
             array[i] = list.get(i).computedOutput;
            i += 1;
         }
 
         return array;
     }
 
 
     @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true) 
     public void onInventoryClose(InventoryCloseEvent event) {
         InventoryView view = event.getView();
         HumanEntity player = event.getPlayer();
 
         if (view == null || view.getTitle() == null || !view.getTitle().equals(QUICKBENCH_TITLE)) {
             // not for us
             return;
         }
 
         openPrecraftedResults.remove(player.getUniqueId());
 
         Inventory playerInventory = view.getTopInventory();
         Inventory benchInventory = view.getBottomInventory();
     }
 
     // QuickBench block <-> item
 
     // Place item -> block
     @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
     public void onBlockPlace(BlockPlaceEvent event) {
         ItemStack item = event.getItemInHand();
 
         if (isQuickBench(item)) {
             if (event.getPlayer().hasPermission("quickbench.place")) {
                 // place quickbench item as lapis block
                 event.getBlockPlaced().setTypeIdAndData(QUICKBENCH_BLOCK_ID, QUICKBENCH_BLOCK_DATA, true);
             } else {
                 String message = plugin.getConfig().getString("quickbench.placeDeniedMessage", "You do not have permission to place this QuickBench.");
                 if (message != null) {
                     event.getPlayer().sendMessage(message);
                 }
 
                 event.setCancelled(true);
             }
         }
     }
 
     // Break block -> item
     @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
     public void onBlockBreak(BlockBreakEvent event) {
         Block block = event.getBlock();
 
         if (block != null && isQuickBench(block)) {
             if (event.getPlayer().hasPermission("quickbench.destroy")) {
                 // break tagged lapis block as quickbench item
                 ItemStack item = getQuickBenchItem();
 
                 block.setType(Material.AIR);
                 block.getWorld().dropItemNaturally(block.getLocation(), item);
 
                 event.setCancelled(true);
             } else {
                 String message = plugin.getConfig().getString("quickbench.destroyDeniedMessage", "You do not have permission to destroy this QuickBench.");
                 if (message != null) {
                     event.getPlayer().sendMessage(message);
                 }
 
                 event.setCancelled(true);
             }
         }
     }
 }
 
 public class QuickBench extends JavaPlugin {
     Logger logger = Logger.getLogger("Minecraft");
 
     public void onEnable() {
         TransparentRecipe.plugin = this;
   
         // IC2's electric item interface, for items that can be charged
         boolean hasElectricItem = true;
         try {
             TransparentRecipe.IElectricItem = Class.forName("ic2.api.IElectricItem");
         } catch (ClassNotFoundException e) {
             hasElectricItem = false;
         }
         if (hasElectricItem) {
             log("Found ic2.api.IElectricItem: " + TransparentRecipe.IElectricItem);
         }
 
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
 
         new QuickBenchListener(this);
     }
 
     public void onDisable() {
     }
 
     public void log(String message) {
         if (getConfig().getBoolean("verbose", false)) {
             logger.info(message);
         }
     }
 
 }
