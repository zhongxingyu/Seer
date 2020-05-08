 
 package com.quartercode.basicexpression.command;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.StorageMinecart;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import com.quartercode.basicexpression.BasicExpressionPlugin;
 import com.quartercode.basicexpression.util.BasicExpressionConfig;
 import com.quartercode.minecartrevolution.expression.ExpressionCommand;
 import com.quartercode.minecartrevolution.expression.ExpressionCommandInfo;
 import com.quartercode.minecartrevolution.util.TypeArray;
 import com.quartercode.minecartrevolution.util.TypeArray.Type;
 import com.quartercode.quarterbukkit.api.ItemData;
 
 public class FarmCommand extends ExpressionCommand {
 
     private final BasicExpressionPlugin plugin;
 
     public FarmCommand(final BasicExpressionPlugin plugin) {
 
         this.plugin = plugin;
     }
 
     @Override
     protected ExpressionCommandInfo createInfo() {
 
         return new ExpressionCommandInfo(new TypeArray(Type.STRING), "fa", "farm");
     }
 
     @Override
     public boolean canExecute(final Minecart minecart) {
 
         return minecart instanceof StorageMinecart;
     }
 
     @Override
     public void execute(final Minecart minecart, final Object parameter) {
 
         final String[] parameters = String.valueOf(parameter).split(" ");
         int radius = (int) plugin.getConfiguration().getLong(BasicExpressionConfig.FARM_DEFAULT_RADIUS);
 
         try {
             if (parameters.length >= 2 && (plugin.getConfiguration().getLong(BasicExpressionConfig.FARM_MAX_RADIUS) < 0 || Integer.parseInt(parameters[1]) <= plugin.getConfiguration().getLong(BasicExpressionConfig.FARM_MAX_RADIUS))) {
                 radius = Integer.parseInt(parameters[1]);
             }
         }
         catch (final NumberFormatException e) {
             radius = 5;
         }
 
         farm((StorageMinecart) minecart, parameters[0], radius);
     }
 
     public void farm(final StorageMinecart minecart, final String type, final int radius) {
 
         if (type.equalsIgnoreCase("wood")) {
             final int[] destroyIds = { 18, 17 };
             final int[] collectIds = { 6, 17, 260 };
             final int plantId = 6;
 
             for (final int destroyId : destroyIds) {
                 final World world = minecart.getWorld();
                 final Location location = minecart.getLocation().getBlock().getLocation();
 
                 for (int y = (int) (location.getY() - radius); y <= radius * 2 + location.getY(); y++) {
                     for (int x = (int) (location.getX() - radius); x <= radius * 2 + location.getX(); x++) {
                         for (int z = (int) (location.getZ() - radius); z <= radius * 2 + location.getZ(); z++) {
                             if (world.getBlockTypeIdAt(x, y, z) == destroyId) {
 	world.getBlockAt(x, y, z).breakNaturally();
 	for (final int collectId : collectIds) {
 	    collectItems(minecart, minecart, radius, collectId);
 	}
 
 	if (world.getBlockTypeIdAt(x, y - 1, z) != 0) {
 	    for (int storageCounter = 0; storageCounter < minecart.getInventory().getSize(); storageCounter++) {
 	        if (minecart.getInventory().getItem(storageCounter) != null && minecart.getInventory().getItem(storageCounter).getTypeId() == plantId) {
 	            final ItemStack newItemStack = new ItemStack(minecart.getInventory().getItem(storageCounter));
 	            newItemStack.setAmount(minecart.getInventory().getItem(storageCounter).getAmount() - 1);
 	            minecart.getInventory().setItem(storageCounter, newItemStack);
 	            if (minecart.getInventory().getItem(storageCounter).getAmount() <= 0) {
 	                minecart.getInventory().setItem(storageCounter, null);
 	            }
 	            world.getBlockAt(x, y, z).setTypeId(plantId);
 	        }
 	    }
 	}
                             }
                         }
                     }
                 }
             }
         } else if (type.equalsIgnoreCase("wheat")) {
             final int[] destroyIds = { 59 };
             final int[] collectIds = { 295, 296 };
             final int plantItemId = 295;
             final int plantBlockId = 59;
             for (final int destroyId : destroyIds) {
                 final World world = minecart.getWorld();
                 final Location location = minecart.getLocation().getBlock().getLocation();
 
                 for (int y = (int) (location.getY() - radius); y <= radius * 2 + location.getY(); y++) {
                     for (int x = (int) (location.getX() - radius); x <= radius * 2 + location.getX(); x++) {
                         for (int z = (int) (location.getZ() - radius); z <= radius * 2 + location.getZ(); z++) {
                             if (world.getBlockTypeIdAt(x, y, z) == destroyId && world.getBlockAt(x, y, z).getData() == 7) {
 	world.getBlockAt(x, y, z).breakNaturally();
 	for (final int collectId : collectIds) {
 	    collectItems(minecart, minecart, radius, collectId);
 	}
 
 	for (int storageCounter = 0; storageCounter < minecart.getInventory().getSize(); storageCounter++) {
 	    if (minecart.getInventory().getItem(storageCounter) != null && minecart.getInventory().getItem(storageCounter).getTypeId() == plantItemId) {
 	        final ItemStack newItemStack = new ItemStack(minecart.getInventory().getItem(storageCounter));
 	        newItemStack.setAmount(minecart.getInventory().getItem(storageCounter).getAmount() - 1);
 	        minecart.getInventory().setItem(storageCounter, newItemStack);
 	        if (minecart.getInventory().getItem(storageCounter).getAmount() <= 0) {
 	            minecart.getInventory().setItem(storageCounter, null);
 	        }
 	        world.getBlockAt(x, y, z).setTypeId(plantBlockId);
 	    }
 	}
                             }
                         }
                     }
                 }
             }
         } else if (type.equalsIgnoreCase("pumpkin")) {
             final int destroyId = 86;
             final int collectId = 86;
             final World world = minecart.getWorld();
             final Location location = minecart.getLocation().getBlock().getLocation();
 
             for (int y = (int) (location.getY() - radius); y <= radius * 2 + location.getY(); y++) {
                 for (int x = (int) (location.getX() - radius); x <= radius * 2 + location.getX(); x++) {
                     for (int z = (int) (location.getZ() - radius); z <= radius * 2 + location.getZ(); z++) {
                         if (world.getBlockTypeIdAt(x, y, z) == destroyId) {
                             world.getBlockAt(x, y, z).breakNaturally();
                         }
                     }
                 }
             }
 
             collectItems(minecart, minecart, radius, collectId);
         } else if (type.equalsIgnoreCase("melon")) {
             final int destroyId = 103;
             final int collectId = 103;
             final World world = minecart.getWorld();
             final Location location = minecart.getLocation().getBlock().getLocation();
 
             for (int y = (int) (location.getY() - radius); y <= radius * 2 + location.getY(); y++) {
                 for (int x = (int) (location.getX() - radius); x <= radius * 2 + location.getX(); x++) {
                     for (int z = (int) (location.getZ() - radius); z <= radius * 2 + location.getZ(); z++) {
                         if (world.getBlockTypeIdAt(x, y, z) == destroyId) {
                             world.getBlockAt(x, y, z).breakNaturally();
                         }
                     }
                 }
             }
 
             collectItems(minecart, minecart, radius, collectId);
         } else if (type.equalsIgnoreCase("sugar") || type.equalsIgnoreCase("sugarcane")) {
             final int[] destroyIds = { 83 };
             final int[] collectIds = { 338 };
             for (final int destroyId : destroyIds) {
                 final World world = minecart.getWorld();
                 final Location location = minecart.getLocation().getBlock().getLocation();
 
                 for (int y = (int) (location.getY() - radius); y <= radius * 2 + location.getY(); y++) {
                     for (int x = (int) (location.getX() - radius); x <= radius * 2 + location.getX(); x++) {
                         for (int z = (int) (location.getZ() - radius); z <= radius * 2 + location.getZ(); z++) {
                             if (world.getBlockTypeIdAt(x, y, z) == destroyId && world.getBlockTypeIdAt(x, y - 1, z) == destroyId) {
 	world.getBlockAt(x, y, z).breakNaturally();
                             }
                         }
                     }
                 }
             }
 
             for (final int collectId : collectIds) {
                 collectItems(minecart, minecart, radius, collectId);
             }
         }
     }
 
     private void collectItems(final InventoryHolder inventory, final Minecart minecart, final int radius, final int collectId) {
 
         if (inventory.getInventory().firstEmpty() < 0) {
             return;
         }
 
         for (final Entity entity : minecart.getNearbyEntities(radius, radius, radius)) {
             if (entity instanceof Item) {
                 final Item item = (Item) entity;
                 if (item.isDead()) {
                     continue;
                 } else if (new ItemData(item.getItemStack()).getMaterial().getId() != collectId) {
                     continue;
                 }
 
                 inventory.getInventory().addItem(new ItemStack[] { item.getItemStack() });
                 item.remove();
             }
         }
     }
 
 }
