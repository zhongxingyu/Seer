 package com.mewin.WGBlockRestricter;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 
 /**
  *
  * @author mewin
  */
 public enum BlockMaterial
 {
     ANY, 
     
     STONE,
     GRASS,
     DIRT,
     COBBLESTONE,
     WOOD,
     SAPLING,
     BEDROCK,
     SAND,
     GRAVEL,
     GOLD_ORE,
     IRON_ORE,
     COAL_ORE,
     LOG,
     LEAVES,
     SPONGE,
     GLASS,
     LAPIS_ORE,
     LAPIS_BLOCK,
     DISPENSER,
     SANDSTONE,
     NOTE_BLOCK,
     BED_BLOCK,
     POWERED_RAIL,
     DETECTOR_RAIL,
     PISTON_STICKY_BASE,
     WEB,
     LONG_GRASS,
     DEAD_BUSH,
     PISTON_BASE,
     PISTON_EXTENSION,
     WOOL,
     PISTON_MOVING_PIECE,
     YELLOW_FLOWER,
     RED_ROSE,
     BROWN_MUSHROOM,
     RED_MUSHROOM,
     GOLD_BLOCK,
     IRON_BLOCK,
     DOUBLE_STEP,
     STEP,
     BRICK,
     TNT,
     BOOKSHELF,
     MOSSY_COBBLESTONE,
     OBSIDIAN,
     TORCH,
     FIRE,
     MOB_SPAWNER,
     WOOD_STAIRS,
     CHEST,
     REDSTONE_WIRE,
     DIAMOND_ORE,
     DIAMOND_BLOCK,
     WORKBENCH,
     CROPS,
     SOIL,
     FURNACE,
     BURNING_FURNACE,
     SIGN_POST,
     WOODEN_DOOR,
     LADDER,
     RAILS,
     COBBLESTONE_STAIRS,
     WALL_SIGN,
     LEVER,
     STONE_PLATE,
     IRON_DOOR_BLOCK,
     WOOD_PLATE,
     REDSTONE_ORE,
     GLOWING_REDSTONE_ORE,
     REDSTONE_TORCH_OFF,
     REDSTONE_TORCH_ON,
     STONE_BUTTON,
     SNOW,
     ICE,
     SNOW_BLOCK,
     CACTUS,
     CLAY,
     SUGAR_CANE_BLOCK,
     JUKEBOX,
     FENCE,
     PUMPKIN,
     NETHERRACK,
     SOUL_SAND,
     GLOWSTONE,
     PORTAL,
     JACK_O_LANTERN,
     CAKE_BLOCK,
     DIODE_BLOCK_OFF,
     DIODE_BLOCK_ON,
     LOCKED_CHEST,
     TRAP_DOOR,
     MONSTER_EGGS,
     SMOOTH_BRICK,
     HUGE_MUSHROOM_1,
     HUGE_MUSHROOM_2,
     IRON_FENCE,
     THIN_GLASS,
     MELON_BLOCK,
     PUMPKIN_STEM,
     MELON_STEM,
     VINE,
     FENCE_GATE,
     BRICK_STAIRS,
     SMOOTH_STAIRS,
     MYCEL,
     WATER_LILY,
     NETHER_BRICK,
     NETHER_FENCE,
     NETHER_BRICK_STAIRS,
     NETHER_WARTS,
     ENCHANTMENT_TABLE,
     BREWING_STAND,
     CAULDRON,
     ENDER_PORTAL,
     ENDER_PORTAL_FRAME,
     ENDER_STONE,
     DRAGON_EGG,
     REDSTONE_LAMP_OFF,
     REDSTONE_LAMP_ON,
     WOOD_DOUBLE_STEP,
     WOOD_STEP,
     COCOA,
     SANDSTONE_STAIRS,
     EMERALD_ORE,
     ENDER_CHEST,
     TRIPWIRE_HOOK,
     TRIPWIRE,
     EMERALD_BLOCK,
     SPRUCE_WOOD_STAIRS,
     BIRCH_WOOD_STAIRS,
     JUNGLE_WOOD_STAIRS,
     COMMAND,
     BEACON,
     COBBLE_WALL,
     FLOWER_POT,
     CARROT,
     POTATO,
     WOOD_BUTTON,
     SKULL,
     ANVIL, 
     TRAPPED_CHEST, 
     GOLD_PLATE, 
     IRON_PLATE, 
     REDSTONE_COMPARATOR_OFF, 
     REDSTONE_COMPARATOR_ON, 
     DAYLIGHT_DETECTOR, 
     QUARTZ_ORE, 
     HOPPER, 
     QUARTZ_BLOCK, 
     QUARTZ_STAIRS, 
     ACTIVATOR_RAIL, 
     DROPPER,
     REDSTONE_BLOCK, 
     
     //Minecraft 1.6.1
    STAINED_CLAY, 
     HAY_BLOCK, 
     CARPET, 
     HARD_CLAY, 
     COAL_BLOCK, 
     
     STONE_BRICK(Material.SMOOTH_BRICK), 
     WATER(Material.WATER, Material.STATIONARY_WATER, Material.WATER_BUCKET),
     LAVA(Material.LAVA, Material.STATIONARY_LAVA, Material.LAVA_BUCKET),
     DIODE(Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF), 
     REDSTONE_COMPARATOR(Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON), 
     REDSTONE_TORCH(Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF), 
     REDSTONE_LAMP(Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON), 
     SIGN(Material.SIGN_POST, Material.WALL_SIGN), 
     PISTON(Material.PISTON_BASE, Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.PISTON_STICKY_BASE), 
     
     //for hanging events
     PAINTING, 
     ITEM_FRAME;
     
     
     private Material[] mats;
     
     private BlockMaterial()
     {
         mats = new Material[0];
         try
         {
             Material mat = Material.valueOf(this.name());
             if (mat != null)
             {
                 addMaterial(mat);
             }
         }
         catch(Exception ex)
         {
             
         }
     }
     
     private BlockMaterial(Material ... mats)
     {
         this.mats = mats;
         for (Material mat : mats)
         {
             addMaterial(mat);
         }
     }
     
     private void addMaterial(Material mat)
     {
         if (!ByMaterial.byMaterial.containsKey(mat))
         {
             ByMaterial.byMaterial.put(mat, new ArrayList<BlockMaterial>());
         }
         ByMaterial.byMaterial.get(mat).add(this);
     }
     
     public Material[] getMaterials()
     {
         return mats;
     }
     
     public static final class ByMaterial
     {
         public static EnumMap<Material, ArrayList<BlockMaterial>> byMaterial = new EnumMap<Material, ArrayList<BlockMaterial>>(Material.class);
     }
 }
     
