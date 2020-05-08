 package com.herocraftonline.dev.heroes.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import net.minecraft.server.MathHelper;
 import net.minecraft.server.Vec3D;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 
 @SuppressWarnings("serial")
 public final class Util {
 
     // Default Item Maps
     public final static List<String> swords;
     public final static List<String> axes;
     public final static List<String> shovels;
     public final static List<String> picks;
     public final static List<String> hoes;
     public final static List<String> weapons;
     public final static List<String> armors;
     public final static List<String> tools;
 
     // Blocks that we consider transparent for skills
     public final static Set<Material> transparentBlocks;
 
     // Byte Set of transparents
     public final static HashSet<Byte> transparentIds;
 
     // Random number generator
     public final static Random rand = new Random();
 
     public final static HashMap<String, Location> deaths;
 
     static {
         swords = new ArrayList<String>(5);
         swords.add("WOOD_SWORD");
         swords.add("STONE_SWORD");
         swords.add("IRON_SWORD");
         swords.add("GOLD_SWORD");
         swords.add("DIAMOND_SWORD");
 
         axes = new ArrayList<String>(5);
         axes.add("WOOD_AXE");
         axes.add("STONE_AXE");
         axes.add("IRON_AXE");
         axes.add("GOLD_AXE");
         axes.add("DIAMOND_AXE");
 
         shovels = new ArrayList<String>(5);
         shovels.add("WOOD_SPADE");
         shovels.add("STONE_SPADE");
         shovels.add("IRON_SPADE");
         shovels.add("GOLD_SPADE");
         shovels.add("DIAMOND_SPADE");
 
         picks = new ArrayList<String>(5);
         picks.add("WOOD_PICKAXE");
         picks.add("STONE_PICKAXE");
         picks.add("IRON_PICKAXE");
         picks.add("GOLD_PICKAXE");
         picks.add("DIAMOND_PICKAXE");
 
         hoes = new ArrayList<String>(5);
         hoes.add("WOOD_HOE");
         hoes.add("STONE_HOE");
         hoes.add("IRON_HOE");
         hoes.add("GOLD_HOE");
         hoes.add("DIAMOND_HOE");
 
         tools = new ArrayList<String>(2);
         tools.add("SHEARS");
         tools.add("FISHING_ROD");
         tools.addAll(shovels);
         tools.addAll(hoes);
 
         weapons = new ArrayList<String>(26);
         weapons.addAll(picks);
         weapons.addAll(axes);
         weapons.addAll(swords);
         weapons.addAll(tools);
         weapons.add("BOW");
 
         armors = new ArrayList<String>(21);
         armors.add("LEATHER_HELMET");
         armors.add("LEATHER_LEGGINGS");
         armors.add("LEATHER_BOOTS");
         armors.add("LEATHER_CHESTPLATE");
         armors.add("IRON_HELMET");
         armors.add("IRON_LEGGINGS");
         armors.add("IRON_CHESTPLATE");
         armors.add("IRON_BOOTS");
         armors.add("CHAINMAIL_HELMET");
         armors.add("CHAINMAIL_LEGGINGS");
         armors.add("CHAINMAIL_BOOTS");
         armors.add("CHAINMAIL_CHESTPLATE");
         armors.add("GOLD_HELMET");
         armors.add("GOLD_LEGGINGS");
         armors.add("GOLD_CHESTPLATE");
         armors.add("GOLD_BOOTS");
         armors.add("DIAMOND_HELMET");
         armors.add("DIAMOND_LEGGINGS");
         armors.add("DIAMOND_CHESTPLATE");
         armors.add("DIAMOND_BOOTS");
         armors.add("PUMPKIN");
 
         transparentBlocks = new HashSet<Material>(22);
         transparentBlocks.add(Material.AIR);
         transparentBlocks.add(Material.SNOW);
         transparentBlocks.add(Material.REDSTONE_WIRE);
         transparentBlocks.add(Material.TORCH);
         transparentBlocks.add(Material.REDSTONE_TORCH_OFF);
         transparentBlocks.add(Material.REDSTONE_TORCH_ON);
         transparentBlocks.add(Material.RED_ROSE);
         transparentBlocks.add(Material.YELLOW_FLOWER);
         transparentBlocks.add(Material.SAPLING);
         transparentBlocks.add(Material.LADDER);
         transparentBlocks.add(Material.STONE_PLATE);
         transparentBlocks.add(Material.WOOD_PLATE);
         transparentBlocks.add(Material.CROPS);
         transparentBlocks.add(Material.LEVER);
         transparentBlocks.add(Material.WATER);
         transparentBlocks.add(Material.STATIONARY_WATER);
         transparentBlocks.add(Material.RAILS);
         transparentBlocks.add(Material.POWERED_RAIL);
         transparentBlocks.add(Material.DETECTOR_RAIL);
         transparentBlocks.add(Material.DIODE_BLOCK_OFF);
         transparentBlocks.add(Material.DIODE_BLOCK_ON);
 
         transparentIds = new HashSet<Byte>(22);
         transparentIds.add((byte) Material.AIR.getId());
         transparentIds.add((byte) Material.SNOW.getId());
         transparentIds.add((byte) Material.REDSTONE_WIRE.getId());
         transparentIds.add((byte) Material.TORCH.getId());
         transparentIds.add((byte) Material.REDSTONE_TORCH_OFF.getId());
         transparentIds.add((byte) Material.REDSTONE_TORCH_ON.getId());
         transparentIds.add((byte) Material.RED_ROSE.getId());
         transparentIds.add((byte) Material.YELLOW_FLOWER.getId());
         transparentIds.add((byte) Material.SAPLING.getId());
         transparentIds.add((byte) Material.LADDER.getId());
         transparentIds.add((byte) Material.STONE_PLATE.getId());
         transparentIds.add((byte) Material.WOOD_PLATE.getId());
         transparentIds.add((byte) Material.CROPS.getId());
         transparentIds.add((byte) Material.LEVER.getId());
         transparentIds.add((byte) Material.WATER.getId());
         transparentIds.add((byte) Material.STATIONARY_WATER.getId());
         transparentIds.add((byte) Material.RAILS.getId());
         transparentIds.add((byte) Material.POWERED_RAIL.getId());
         transparentIds.add((byte) Material.DETECTOR_RAIL.getId());
         transparentIds.add((byte) Material.DIODE_BLOCK_OFF.getId());
         transparentIds.add((byte) Material.DIODE_BLOCK_ON.getId());
 
         deaths = new LinkedHashMap<String, Location>() {
 
             private static final int MAX_ENTRIES = 50;
 
             @Override
             protected boolean removeEldestEntry(Map.Entry<String, Location> eldest) {
                 return size() > MAX_ENTRIES;
             }
         };
     }
 
     /**
      * Converts an entity into its CreatureType
      * 
      * @param entity
      * @return
      */
     public static CreatureType getCreatureFromEntity(Entity entity) {
         CreatureType type = null;
         if (entity == null)
             return type;
         try {
             Class<?>[] interfaces = entity.getClass().getInterfaces();
             for (Class<?> c : interfaces) {
                 if (LivingEntity.class.isAssignableFrom(c)) {
                     type = CreatureType.fromName(c.getSimpleName());
                     break;
                 }
             }
         } catch (IllegalArgumentException e) {}
         return type;
     }
 
     /**
      * Tests whether the entity is nearby a spawner
      * 
      * @param entity
      * @param radius
      * @return
      */
     public static boolean isNearSpawner(Entity entity, int radius) {
         Location location = entity.getLocation();
 
         for (int i = 0 - radius; i <= radius; i++) {
             for (int j = 0 - radius; j <= radius; j++) {
                 for (int k = 0 - radius; k <= radius; k++) {
                     if (location.getBlock().getRelative(i, j, k).getType().equals(Material.MOB_SPAWNER))
                         return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Grab the first empty INVENTORY SLOT, skips the Hotbar.
      * 
      * @param player
      * @return
      */
     public static int firstEmpty(ItemStack[] inventory) {
         for (int i = 9; i < inventory.length; i++) {
             if (inventory[i] == null)
                 return i;
         }
         return -1;
     }
 
     /**
      * Move the selected Item to an available slot, if a slot does not exist then we remove it from the inventory.
      * Returns if the item is still in the player's inventory
      * @param player
      * @param slot
      * @param item
      * @return
      */
     public static boolean moveItem(Hero hero, int slot, ItemStack item) {
         Player player = hero.getPlayer();
         PlayerInventory inv = player.getInventory();
         int empty = firstEmpty(inv.getContents());
         if (empty == -1) {
             player.getWorld().dropItemNaturally(player.getLocation(), item);
             if (slot != -1)
                 inv.clear(slot);
             return false;
         } else {
             inv.setItem(empty, item);
             if (slot != -1)
                 inv.clear(slot);
             Messaging.send(player, "You are not trained to use a $1.", MaterialUtil.getFriendlyName(item.getType()));
             return true;
         }
     }
 
     /**
      * Synchronize the Clients Inventory with the Server. This is dealt during a scheduler so it happens after ANY
      * changes are made.
      * Synchronizing during changes often results in the client losing Sync.
      * 
      * @param player
      */
     public static void syncInventory(final Player player, Heroes plugin) {
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
             @Override
             @SuppressWarnings("deprecation")
             public void run() {
                 player.updateInventory();
             }
         });
     }
 
     /**
      * Checks if the material is a Weapon/Tool
      * 
      * @param mat
      * @return
      */
     public static boolean isWeapon(Material mat) {
         switch (mat) {
         case IRON_AXE:
         case IRON_HOE:
         case IRON_PICKAXE:
         case IRON_SPADE:
         case IRON_SWORD:
         case STONE_AXE:
         case STONE_HOE:
         case STONE_PICKAXE:
         case STONE_SPADE:
         case STONE_SWORD:
         case GOLD_AXE:
         case GOLD_HOE:
         case GOLD_PICKAXE:
         case GOLD_SPADE:
         case GOLD_SWORD:
         case WOOD_AXE:
         case WOOD_HOE:
         case WOOD_PICKAXE:
         case WOOD_SPADE:
         case WOOD_SWORD:
         case DIAMOND_AXE:
         case DIAMOND_HOE:
         case DIAMOND_PICKAXE:
         case DIAMOND_SPADE:
         case DIAMOND_SWORD:
         case BOW:
         case FISHING_ROD:
         case SHEARS:
             return true;
         default:
             return false;
         }
     }
 
     /**
      * Checks if the material given is an armor item
      * 
      * @param mat
      * @return
      */
     public static boolean isArmor(Material mat) {
         switch (mat) {
         case LEATHER_HELMET:
         case LEATHER_LEGGINGS:
         case LEATHER_BOOTS:
         case LEATHER_CHESTPLATE:
         case IRON_HELMET:
         case IRON_LEGGINGS:
         case IRON_CHESTPLATE:
         case IRON_BOOTS:
         case CHAINMAIL_HELMET:
         case CHAINMAIL_LEGGINGS:
         case CHAINMAIL_BOOTS:
         case CHAINMAIL_CHESTPLATE:
         case GOLD_HELMET:
         case GOLD_LEGGINGS:
         case GOLD_CHESTPLATE:
         case GOLD_BOOTS:
         case DIAMOND_HELMET: 
         case DIAMOND_LEGGINGS:
         case DIAMOND_CHESTPLATE:
         case DIAMOND_BOOTS:
         case PUMPKIN:
             return true;
         default:
             return false;
         }
     }
 
     public static void disarmCheck(Hero hero, Heroes plugin) {
         ItemStack[] contents = hero.getPlayer().getInventory().getContents();
         boolean changed = false;
         for (int i = 0; i < 9; i++) {
             if (contents[i] == null)
                 continue;
             if (Util.isWeapon(contents[i].getType())) {
                 Util.moveItem(hero, i, contents[i]);
                 changed = true;
             }
         }
         if (changed)
             syncInventory(hero.getPlayer(), plugin);
     }
 
     public static void moveFile(File from, File to) {
         if (!from.exists())
             return;
         OutputStream output = null;
         InputStream input = null;
         try {
             to.getParentFile().mkdirs();
             to.createNewFile();
             output = new FileOutputStream(to, false);
             input = new FileInputStream(from);
             int out;
             while ((out = input.read()) != -1) {
                 output.write(out);
             }
             input.close();
             output.close();
             from.delete();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 input.close();
                 output.close();
             } catch (IOException e) {
             }
         }
     }
 
     public static int getMCExperience(int level) {
         return level * 7;
     }
 
     public static Integer toInt(Object val) {
         if (val instanceof String) {
             try {
                 return Integer.valueOf((String) val);
             } catch (NumberFormatException e) {
                 return null;
             }
         } else if (!(val instanceof Number)) {
             return null;
         }
 
         if (val instanceof Integer) {
             return (Integer) val;
         } else if (val instanceof Double) {
             return ((Double) val).intValue();
         } else if (val instanceof Float) {
             return ((Float) val).intValue();
         } else if (val instanceof Long) {
             return ((Long) val).intValue();
         } else if (val instanceof BigDecimal) {
             return ((BigDecimal) val).intValue();
         } else {
             return null;
         }
     }
 
     public static int toIntNonNull(Object val, String name) {
         Integer newVal = toInt(val);
         if (newVal == null)
             throw new IllegalArgumentException(name + " must be a numeral!");
         else
             return newVal;
     }
 
     public static double toDoubleNonNull(Object val, String name) {
         Double newVal = toDouble(val);
         if (newVal == null)
             throw new IllegalArgumentException(name + " must be a numeral!");
         else
             return newVal;
     }
 
     public static Double toDouble(Object val) {
         if (val instanceof String) {
             try {
                 return Double.valueOf((String) val);
             } catch (NumberFormatException e) {
                 return null;
             }
         } else if (!(val instanceof Number)) {
             return null;
         }
 
         if (val instanceof Integer) {
             return ((Integer) val).doubleValue();
         } else if (val instanceof Double) {
             return ((Double) val);
         } else if (val instanceof Float) {
             return ((Float) val).doubleValue();
         } else if (val instanceof Long) {
             return ((Long) val).doubleValue();
         } else if (val instanceof BigDecimal) {
             return ((BigDecimal) val).doubleValue();
         } else {
             return null;
         }
     }
 
     public static String stringDouble(double d) {
         return String.valueOf(formatDouble(d));
     }
 
     public static double formatDouble(double d) {
         int val = (int) (d * 1000);
         return val / 1000d;
     }
 
     public static Vec3D getLocation(Player player) {
         Location playerLoc = player.getLocation();
         float rotationYaw = playerLoc.getYaw();
         float rotationPitch = playerLoc.getPitch();
 
         float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
         float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
         float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
         float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
         return Vec3D.create(f3 * f5, f7, f1 * f5);
     }
 
     public static ItemStack itemFromString(String[] vals, int amount) {
         ItemStack stack = null;
         try {
             int id = Integer.parseInt(vals[0]);
             byte sub = 0;
             if (vals.length > 1) {
                 sub = (byte) Integer.parseInt(vals[1]);
             }
             stack = new ItemStack(id, amount, sub);
         } catch (NumberFormatException e) {
         }
         return stack;
         
     }
 }
