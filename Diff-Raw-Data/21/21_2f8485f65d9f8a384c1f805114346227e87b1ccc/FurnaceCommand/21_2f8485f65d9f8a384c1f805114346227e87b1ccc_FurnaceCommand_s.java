 
 package com.quartercode.basicexpression.command;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Furnace;
 import org.bukkit.entity.Minecart;
 import org.bukkit.inventory.FurnaceInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import com.quartercode.minecartrevolution.expression.ExpressionCommand;
 import com.quartercode.minecartrevolution.expression.ExpressionCommandInfo;
 import com.quartercode.minecartrevolution.util.AliasUtil;
 import com.quartercode.minecartrevolution.util.TypeArray;
 import com.quartercode.minecartrevolution.util.TypeArray.Type;
 import com.quartercode.quarterbukkit.api.ItemData;
 
 public class FurnaceCommand extends ExpressionCommand {
 
     private static final List<ItemData> FUELS = new ArrayList<ItemData>();
 
     static {
 
         FUELS.add(new ItemData(Material.COAL, (byte) 0));
         FUELS.add(new ItemData(Material.COAL, (byte) 1));
         FUELS.add(new ItemData(Material.WOOD));
         FUELS.add(new ItemData(Material.WOOD_STEP));
         FUELS.add(new ItemData(Material.SAPLING));
         FUELS.add(new ItemData(Material.WOOD_AXE));
         FUELS.add(new ItemData(Material.WOOD_HOE));
         FUELS.add(new ItemData(Material.WOOD_PICKAXE));
         FUELS.add(new ItemData(Material.WOOD_SPADE));
         FUELS.add(new ItemData(Material.WOOD_SWORD));
         FUELS.add(new ItemData(Material.WOOD_PLATE));
         FUELS.add(new ItemData(Material.WOOD_BUTTON));
         FUELS.add(new ItemData(Material.STICK));
         FUELS.add(new ItemData(Material.FENCE));
         FUELS.add(new ItemData(Material.FENCE_GATE));
         FUELS.add(new ItemData(Material.WOOD_STAIRS));
         FUELS.add(new ItemData(Material.TRAP_DOOR));
         FUELS.add(new ItemData(Material.WORKBENCH));
         FUELS.add(new ItemData(Material.BOOKSHELF));
         FUELS.add(new ItemData(Material.CHEST));
         FUELS.add(new ItemData(Material.JUKEBOX));
         FUELS.add(new ItemData(Material.NOTE_BLOCK));
         FUELS.add(new ItemData(Material.HUGE_MUSHROOM_1));
         FUELS.add(new ItemData(Material.HUGE_MUSHROOM_2));
         FUELS.add(new ItemData(Material.BLAZE_ROD));
         FUELS.add(new ItemData(Material.LAVA_BUCKET));
     }
 
     public static boolean isFuel(final ItemData itemData) {
 
         return FUELS.contains(itemData);
     }
 
     public FurnaceCommand() {
 
     }
 
     @Override
     protected ExpressionCommandInfo createInfo() {
 
         return new ExpressionCommandInfo(new TypeArray(Type.STRING), "fu", "furnace");
     }
 
     @Override
     public boolean canExecute(final Minecart minecart) {
 
         return minecart instanceof InventoryHolder;
     }
 
     @Override
     public void execute(final Minecart minecart, final Object parameter) {
 
         Furnace furnace = null;
 
         Location minecartLocation = minecart.getLocation();
         minecartLocation.setX(minecart.getLocation().getX() + 1.0D);
         if (minecartLocation.getBlock().getType() == Material.FURNACE || minecartLocation.getBlock().getType() == Material.BURNING_FURNACE) {
             furnace = (Furnace) minecartLocation.getBlock().getState();
         }
         minecartLocation = minecart.getLocation();
         minecartLocation.setX(minecart.getLocation().getX() - 1.0D);
         if (minecartLocation.getBlock().getType() == Material.FURNACE || minecartLocation.getBlock().getType() == Material.BURNING_FURNACE) {
             furnace = (Furnace) minecartLocation.getBlock().getState();
         }
         minecartLocation = minecart.getLocation();
         minecartLocation.setZ(minecart.getLocation().getZ() + 1.0D);
         if (minecartLocation.getBlock().getType() == Material.FURNACE || minecartLocation.getBlock().getType() == Material.BURNING_FURNACE) {
             furnace = (Furnace) minecartLocation.getBlock().getState();
         }
         minecartLocation = minecart.getLocation();
         minecartLocation.setZ(minecart.getLocation().getZ() - 1.0D);
         if (minecartLocation.getBlock().getType() == Material.FURNACE || minecartLocation.getBlock().getType() == Material.BURNING_FURNACE) {
             furnace = (Furnace) minecartLocation.getBlock().getState();
         }
 
         if (furnace == null) {
             return;
         }
 
         final InventoryHolder inventoryMinecart = (InventoryHolder) minecart;
         String data = String.valueOf(parameter);
 
         if (String.valueOf(parameter).startsWith("+")) {
             data = data.replaceAll("\\+", "").trim();
         } else if (String.valueOf(parameter).startsWith("-")) {
             data = data.replaceAll("-", "").trim();
         } else {
             return;
         }
 
         final List<String> items = new ArrayList<String>();
         for (final String part : data.split(",")) {
             if (!part.isEmpty()) {
                 items.add(part);
             }
         }
 
         if (String.valueOf(parameter).startsWith("+")) {
             if (items.size() > 0) {
                 for (final String item : items) {
                     transferToFurnace(inventoryMinecart.getInventory(), furnace.getInventory(), item);
                 }
             } else {
                 transferToFurnace(inventoryMinecart.getInventory(), furnace.getInventory(), null);
             }
         } else if (String.valueOf(parameter).startsWith("-")) {
             if (items.size() > 0) {
                 for (final String item : items) {
                     transferToInventory(furnace.getInventory(), inventoryMinecart.getInventory(), item);
                 }
             } else {
                 transferToInventory(furnace.getInventory(), inventoryMinecart.getInventory(), null);
             }
         }
     }
 
     private void transferToFurnace(final Inventory fromInventory, final FurnaceInventory toInventory, final String string) {
 
         for (int counter = 0; counter < fromInventory.getSize(); counter++) {
             if (fromInventory.getItem(counter) != null) {
                 int slot;
                if (FUELS.contains(new ItemData(fromInventory.getItem(counter))) && (string.equalsIgnoreCase("fuel") || AliasUtil.equals(new ItemData(fromInventory.getItem(counter)), string))) {
                     slot = 1;
                } else if (string.equalsIgnoreCase("item") || AliasUtil.equals(new ItemData(fromInventory.getItem(counter)), string)) {
                     slot = 0;
                 } else {
                     continue;
                 }
 
                 if (toInventory.getItem(slot) == null || ItemData.equals(fromInventory.getItem(counter), toInventory.getItem(slot))) {
                    final int overplus = fromInventory.getItem(counter).getAmount() + (toInventory.getItem(slot) == null ? 0 : toInventory.getItem(slot).getAmount()) - 64;
                     final ItemStack fuelItemStack = new ItemStack(fromInventory.getItem(counter));
                     if (overplus > 0) {
                        fuelItemStack.setAmount(64);
                         final ItemStack itemStack = new ItemStack(fromInventory.getItem(counter));
                         itemStack.setAmount(overplus);
                         fromInventory.setItem(counter, itemStack);
                     } else {
                         fromInventory.setItem(counter, new ItemStack(Material.AIR));
                     }
                     toInventory.setItem(slot, fuelItemStack);
                 }
             }
         }
     }
 
     private void transferToInventory(final FurnaceInventory fromInventory, final Inventory toInventory, final String string) {
 
         if (fromInventory.getResult() != null && AliasUtil.equals(new ItemData(fromInventory.getResult()), string)) {
             final List<ItemStack> overplus = new ArrayList<ItemStack>(toInventory.addItem(new ItemStack[] { fromInventory.getResult() }).values());
             fromInventory.setResult(new ItemStack(Material.AIR));
 
             for (final ItemStack itemStack : overplus) {
                 fromInventory.setResult(itemStack);
             }
         }
     }
 
 }
