 package com.codeski.webstats;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Item;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.LongGrass;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.Sandstone;
 import org.bukkit.material.Tree;
 import org.bukkit.material.WoodenStep;
 import org.bukkit.material.Wool;
 
 public enum Material {
 	ACACIA_LEAVES,
 	ACACIA_LOG,
 	ACACIA_SAPLING,
 	ACACIA_WOOD_PLANKS,
 	ACACIA_WOOD_SLAB,
 	ACACIA_WOOD_STAIRS,
 	ACTIVATOR_RAIL,
 	AIR,
 	ALLIUM,
 	ANVIL,
 	AZURE_BLUET,
 	BEACON,
 	BED,
 	BEDROCK,
 	BIRCH_LEAVES,
 	BIRCH_LOG,
 	BIRCH_SAPLING,
 	BIRCH_WOOD_PLANKS,
 	BIRCH_WOOD_SLAB,
 	BIRCH_WOOD_STAIRS,
 	BLACK_CARPET,
 	BLACK_STAINED_CLAY,
 	BLACK_STAINED_GLASS,
 	BLACK_STAINED_GLASS_PANE,
 	BLACK_WOOL,
 	BLOCK_OF_COAL,
 	BLOCK_OF_DIAMOND,
 	BLOCK_OF_EMERALD,
 	BLOCK_OF_GOLD,
 	BLOCK_OF_IRON,
 	BLOCK_OF_QUARTZ,
 	BLOCK_OF_REDSTONE,
 	BLUE_CARPET,
 	BLUE_ORCHID,
 	BLUE_STAINED_CLAY,
 	BLUE_STAINED_GLASS,
 	BLUE_STAINED_GLASS_PANE,
 	BLUE_WOOL,
 	BOOKSHELF,
 	BREWING_STAND,
 	BRICK_SLAB,
 	BRICK_STAIRS,
 	BRICKS,
 	BROWN_CARPET,
 	BROWN_MUSHROOM,
 	BROWN_STAINED_CLAY,
 	BROWN_STAINED_GLASS,
 	BROWN_STAINED_GLASS_PANE,
 	BROWN_WOOL,
 	CACTUS,
 	CAKE,
 	CARPET,
 	CARROT,
 	CAULDRON,
 	CHEST,
 	CHISELED_QUARTZ_BLOCK,
 	CHISELED_SANDSTONE,
 	CHISELED_STONE_BRICKS,
 	CLAY,
	COAL_ORE,
 	COBBLESTONE,
 	COBBLESTONE_SLAB,
 	COBBLESTONE_STAIRS,
 	COBBLESTONE_WALL,
 	COBWEB,
 	COCOA,
 	COMMAND_BLOCK,
 	CRACKED_STONE_BRICKS,
 	CRAFTING_TABLE,
 	CREEPER_HEAD,
 	CYAN_CARPET,
 	CYAN_STAINED_CLAY,
 	CYAN_STAINED_GLASS,
 	CYAN_STAINED_GLASS_PANE,
 	CYAN_WOOL,
 	DANDELION,
 	DARK_OAK_LEAVES,
 	DARK_OAK_LOG,
 	DARK_OAK_SAPLING,
 	DARK_OAK_WOOD_PLANKS,
 	DARK_OAK_WOOD_SLAB,
 	DARK_OAK_WOOD_STAIRS,
 	DAYLIGHT_SENSOR,
 	DEAD_BUSH,
 	DETECTOR_RAIL,
 	DIAMOND_ORE,
 	DIRT,
 	DISPENSER,
 	DOUBLE_TALLGRASS,
 	DRAGON_EGG,
 	DROPPER,
 	EMERALD_ORE,
 	ENCHANTMENT_TABLE,
 	END_PORTAL,
 	END_PORTAL_BLOCK,
 	END_STONE,
 	ENDER_CHEST,
 	FARMLAND,
 	FENCE,
 	FENCE_GATE,
 	FERN,
 	FIRE,
 	FLOWER_POT,
 	FURNACE,
 	GLASS,
 	GLASS_PANE,
 	GLOWSTONE,
 	GOLD_ORE,
 	GRASS,
 	GRASS_BLOCK,
 	GRAVEL,
 	GRAY_CARPET,
 	GRAY_STAINED_CLAY,
 	GRAY_STAINED_GLASS,
 	GRAY_STAINED_GLASS_PANE,
 	GRAY_WOOL,
 	GREEN_CARPET,
 	GREEN_STAINED_CLAY,
 	GREEN_STAINED_GLASS,
 	GREEN_STAINED_GLASS_PANE,
 	GREEN_WOOL,
 	HARDENED_CLAY,
 	HAY_BLOCK,
 	HEAD,
 	HOPPER,
 	HUGE_BROWN_MUSHROOM,
 	HUGE_RED_MUSHROOM,
 	ICE,
 	IRON_BARS,
 	IRON_DOOR,
 	IRON_ORE,
 	JACK_O_LANTERN,
 	JUKEBOX,
 	JUNGLE_LEAVES,
 	JUNGLE_LOG,
 	JUNGLE_SAPLING,
 	JUNGLE_WOOD_PLANKS,
 	JUNGLE_WOOD_SLAB,
 	JUNGLE_WOOD_STAIRS,
 	LADDER,
 	LAPIS_LAZULI_BLOCK,
 	LAPIS_LAZULI_ORE,
 	LARGE_FERN,
 	LAVA,
 	LEVER,
 	LIGHT_BLUE_CARPET,
 	LIGHT_BLUE_STAINED_CLAY,
 	LIGHT_BLUE_STAINED_GLASS,
 	LIGHT_BLUE_STAINED_GLASS_PANE,
 	LIGHT_BLUE_WOOL,
 	LIGHT_GRAY_CARPET,
 	LIGHT_GRAY_STAINED_CLAY,
 	LIGHT_GRAY_STAINED_GLASS,
 	LIGHT_GRAY_STAINED_GLASS_PANE,
 	LIGHT_GRAY_WOOL,
 	LILAC,
 	LILY_PAD,
 	LIME_CARPET,
 	LIME_STAINED_CLAY,
 	LIME_STAINED_GLASS,
 	LIME_STAINED_GLASS_PANE,
 	LIME_WOOL,
 	MAGENTA_CARPET,
 	MAGENTA_STAINED_CLAY,
 	MAGENTA_STAINED_GLASS,
 	MAGENTA_STAINED_GLASS_PANE,
 	MAGENTA_WOOL,
 	MELON_BLOCK,
 	MELON_STEM,
 	MONSTER_EGG,
 	MONSTER_SPAWNER,
 	MOSS_STONE,
	MOSSY_COBBLESTONE_WALL,
 	MOSSY_STONE_BRICKS,
 	MYCELIUM,
 	NETHER_BRICK,
 	NETHER_BRICK_FENCE,
 	NETHER_BRICK_SLAB,
 	NETHER_BRICK_STAIRS,
 	NETHER_PORTAL,
 	NETHER_QUARTZ_ORE,
 	NETHER_WART,
 	NETHERRACK,
 	NOTE_BLOCK,
 	OAK_LEAVES,
 	OAK_LOG,
 	OAK_SAPLING,
 	OAK_WOOD_PLANKS,
 	OAK_WOOD_SLAB,
 	OAK_WOOD_STAIRS,
 	OBSIDIAN,
 	ORANGE_CARPET,
 	ORANGE_STAINED_CLAY,
 	ORANGE_STAINED_GLASS,
 	ORANGE_STAINED_GLASS_PANE,
 	ORANGE_TULIP,
 	ORANGE_WOOL,
 	OXEYE_DAISY,
 	PACKED_ICE,
 	PEONY,
 	PILLAR_QUARTZ_BLOCK,
 	PINK_CARPET,
 	PINK_STAINED_CLAY,
 	PINK_STAINED_GLASS,
 	PINK_STAINED_GLASS_PANE,
 	PINK_TULIP,
 	PINK_WOOL,
 	PISTON,
 	PODZOL,
 	POPPY,
 	POTATO,
 	POWERED_RAIL,
 	PUMPKIN,
 	PUMPKIN_STEM,
 	PURPLE_CARPET,
 	PURPLE_STAINED_CLAY,
 	PURPLE_STAINED_GLASS,
 	PURPLE_STAINED_GLASS_PANE,
 	PURPLE_WOOL,
 	QUARTZ_SLAB,
 	QUARTZ_STAIRS,
 	RAIL,
 	RED_CARPET,
 	RED_MUSHROOM,
 	RED_SAND,
 	RED_STAINED_CLAY,
 	RED_STAINED_GLASS,
 	RED_STAINED_GLASS_PANE,
 	RED_TULIP,
 	RED_WOOL,
 	REDSTONE_COMPARATOR,
 	REDSTONE_LAMP,
 	REDSTONE_ORE,
 	REDSTONE_REPEATER,
 	REDSTONE_TORCH,
 	REDSTONE_WIRE,
 	ROSE_BUSH,
 	SAND,
 	SANDSTONE,
 	SANDSTONE_SLAB,
 	SANDSTONE_STAIRS,
 	SHRUB,
 	SIGN_POST,
 	SKELETON_SKULL,
 	SLIME_BLOCK,
 	SMOOTH_SANDSTONE,
 	SNOW,
 	SNOW_BLOCK,
 	SOUL_SAND,
 	SPONGE,
 	SPRUCE_LEAVES,
 	SPRUCE_LOG,
 	SPRUCE_SAPLING,
 	SPRUCE_WOOD_PLANKS,
 	SPRUCE_WOOD_SLAB,
 	SPRUCE_WOOD_STAIRS,
 	STATIONARY_LAVA,
 	STATIONARY_WATER,
 	STICKY_PISTON,
 	STONE,
 	STONE_BRICK_STAIRS,
 	STONE_BRICKS,
 	STONE_BRICKS_SLAB,
 	STONE_BUTTON,
 	STONE_PRESSURE_PLATE,
 	STONE_SLAB,
 	SUGAR_CANE,
 	SUNFLOWER,
 	TNT,
 	TORCH,
 	TRAPDOOR,
 	TRAPPED_CHEST,
 	TRIPWIRE,
 	TRIPWIRE_HOOK,
 	VINES,
 	WALL_SIGN,
 	WATER,
 	WEIGHTED_PRESSURE_PLATE_HEAVY,
 	WEIGHTED_PRESSURE_PLATE_LIGHT,
 	WHEAT,
 	WHITE_STAINED_CLAY,
 	WHITE_STAINED_GLASS,
 	WHITE_STAINED_GLASS_PANE,
 	WHITE_TULIP,
 	WITHER_SKELETON_SKULL,
 	WOODEN_BUTTON,
 	WOODEN_DOOR,
 	WOODEN_PRESSURE_PLATE,
 	WOODEN_SLAB,
 	WOOL,
 	YELLOW_CARPET,
 	YELLOW_STAINED_CLAY,
 	YELLOW_STAINED_GLASS,
 	YELLOW_STAINED_GLASS_PANE,
 	YELLOW_WOOL,
 	ZOMBIE_HEAD;
 	//
 	public enum Event {
 		BLOCK_BREAK,
 		BLOCK_BURN,
 		BLOCK_DAMAGE,
 		BLOCK_DISPENSE,
 		BLOCK_EXP,
 		BLOCK_FADE,
 		BLOCK_FROM_TO,
 		BLOCK_GROW,
 		BLOCK_IGNITE,
 		BLOCK_PHYSICS,
 		BLOCK_PISTON_EXTEND,
 		BLOCK_PISTON_RETRACT,
 		BLOCK_PLACE,
 		BLOCK_REDSTONE,
 		BREW,
 		FURNACE_BURN,
 		FURNACE_SMELT,
 		ITEM_BREAK,
 		ITEM_CRAFT,
 		ITEM_DROP,
 		ITEM_PICK_UP,
 		ITEM_USE,
 		LEAVES_DECAY,
 		NOTE_PLAY,
 		SIGN_CHANGE
 	}
 
 	public static Material getMaterial(Block block) {
 		if (block.getType() == org.bukkit.Material.DOUBLE_PLANT && block.getRelative(BlockFace.DOWN).getType() == org.bukkit.Material.DOUBLE_PLANT)
 			block = block.getRelative(BlockFace.DOWN);
 		return Material.getMaterial(block.getType(), block.getState().getData(), block.getData());
 	}
 
 	public static Material getMaterial(Item item) {
 		return Material.getMaterial(item.getItemStack());
 	}
 
 	public static Material getMaterial(ItemStack item) {
 		return Material.getMaterial(item.getType(), item.getData(), item.getData().getData());
 	}
 
 	private static Material getMaterial(org.bukkit.Material type, MaterialData data, byte duckBukkit) {
 		switch (type) {
 			case GRASS:
 				return GRASS_BLOCK;
 			case DIRT:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 					case 1:
 						return DIRT;
 					case 2:
 						return PODZOL;
 				}
 			case WOOD:
 				switch (((Tree) data).getSpecies()) {
 					case GENERIC:
 						if (duckBukkit == 4) // Bukkit reports ACACIA as GENERIC, brilliant!
 							return ACACIA_WOOD_PLANKS;
 						return OAK_WOOD_PLANKS;
 					case REDWOOD:
 						if (duckBukkit == 5) // Bukkit reports DARK_OAK as REDWOOD, brilliant!
 							return DARK_OAK_WOOD_PLANKS;
 						return SPRUCE_WOOD_PLANKS;
 					case BIRCH:
 						return BIRCH_WOOD_PLANKS;
 					case JUNGLE:
 						return JUNGLE_WOOD_PLANKS;
 					case ACACIA:
 						return ACACIA_WOOD_PLANKS;
 					case DARK_OAK:
 						return DARK_OAK_WOOD_PLANKS;
 				}
 			case SAPLING:
 				switch (((Tree) data).getSpecies()) {
 					case GENERIC:
 						if (duckBukkit == 4) // Bukkit reports ACACIA as GENERIC, brilliant!
 							return ACACIA_SAPLING;
 						return OAK_SAPLING;
 					case REDWOOD:
 						if (duckBukkit == 5) // Bukkit reports DARK_OAK as REDWOOD, brilliant!
 							return DARK_OAK_SAPLING;
 						return SPRUCE_SAPLING;
 					case BIRCH:
 						return BIRCH_SAPLING;
 					case JUNGLE:
 						return JUNGLE_SAPLING;
 					case ACACIA:
 						return ACACIA_SAPLING;
 					case DARK_OAK:
 						return DARK_OAK_SAPLING;
 				}
 			case SAND:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return SAND;
 					case 1:
 						return RED_SAND;
 				}
 			case LOG:
 				// case LOG_2:
 				switch (((Tree) data).getSpecies()) {
 					case GENERIC:
 						if (duckBukkit == 4) // Bukkit reports ACACIA as GENERIC, brilliant!
 							return ACACIA_LOG;
 						return OAK_LOG;
 					case REDWOOD:
 						if (duckBukkit == 5) // Bukkit reports DARK_OAK as REDWOOD, brilliant!
 							return DARK_OAK_LOG;
 						return SPRUCE_LOG;
 					case BIRCH:
 						return BIRCH_LOG;
 					case JUNGLE:
 						return JUNGLE_LOG;
 					case ACACIA:
 						return ACACIA_LOG;
 					case DARK_OAK:
 						return DARK_OAK_LOG;
 				}
 			case LEAVES:
 				// case LEAVES_2:
 				switch (((Tree) data).getSpecies()) {
 					case GENERIC:
 						if (duckBukkit == 4) // Bukkit reports ACACIA as GENERIC, brilliant!
 							return ACACIA_LEAVES;
 						return OAK_LEAVES;
 					case REDWOOD:
 						if (duckBukkit == 5) // Bukkit reports DARK_OAK as REDWOOD, brilliant!
 							return DARK_OAK_LEAVES;
 						return SPRUCE_LEAVES;
 					case BIRCH:
 						return BIRCH_LEAVES;
 					case JUNGLE:
 						return JUNGLE_LEAVES;
 					case ACACIA:
 						return ACACIA_LEAVES;
 					case DARK_OAK:
 						return DARK_OAK_LEAVES;
 				}
 			case SANDSTONE:
 				switch (((Sandstone) data).getType()) {
 					case CRACKED:
 						return SANDSTONE;
 					case GLYPHED:
 						return CHISELED_SANDSTONE;
 					case SMOOTH:
 						return SMOOTH_SANDSTONE;
 				}
 			case BED_BLOCK:
 				return BED;
 			case LONG_GRASS:
 				switch (((LongGrass) data).getSpecies()) {
 					case DEAD:
 						return SHRUB;
 					case NORMAL:
 						return GRASS;
 					case FERN_LIKE:
 						return FERN;
 				}
 			case WOOL:
 				switch (((Wool) data).getColor()) {
 					case WHITE:
 						return WOOL;
 					case ORANGE:
 						return ORANGE_WOOL;
 					case MAGENTA:
 						return MAGENTA_WOOL;
 					case LIGHT_BLUE:
 						return LIGHT_BLUE_WOOL;
 					case YELLOW:
 						return YELLOW_WOOL;
 					case LIME:
 						return LIME_WOOL;
 					case PINK:
 						return PINK_WOOL;
 					case GRAY:
 						return GRAY_WOOL;
 					case SILVER:
 						return LIGHT_GRAY_WOOL;
 					case CYAN:
 						return CYAN_WOOL;
 					case PURPLE:
 						return PURPLE_WOOL;
 					case BLUE:
 						return BLUE_WOOL;
 					case BROWN:
 						return BROWN_WOOL;
 					case GREEN:
 						return GREEN_WOOL;
 					case RED:
 						return RED_WOOL;
 					case BLACK:
 						return BLACK_WOOL;
 				}
 			case PISTON_BASE:
 			case PISTON_EXTENSION:
 				return PISTON;
 			case YELLOW_FLOWER:
 				return DANDELION;
 			case RED_ROSE:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return POPPY;
 					case 1:
 						return BLUE_ORCHID;
 					case 2:
 						return ALLIUM;
 					case 3:
 						return AZURE_BLUET;
 					case 4:
 						return RED_TULIP;
 					case 5:
 						return ORANGE_TULIP;
 					case 6:
 						return WHITE_TULIP;
 					case 7:
 						return PINK_TULIP;
 					case 8:
 						return OXEYE_DAISY;
 				}
 			case GOLD_BLOCK:
 				return BLOCK_OF_GOLD;
 			case IRON_BLOCK:
 				return BLOCK_OF_IRON;
 			case DOUBLE_STEP:
 			case STEP:
 				switch (duckBukkit % 8) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return STONE_SLAB;
 					case 1:
 						return SANDSTONE_SLAB;
 					case 2:
 						return WOODEN_SLAB;
 					case 3:
 						return COBBLESTONE_SLAB;
 					case 4:
 						return BRICK_SLAB;
 					case 5:
 						return STONE_BRICKS_SLAB;
 					case 6:
 						return NETHER_BRICK_SLAB;
 					case 7:
 						return QUARTZ_SLAB;
 				}
 			case BRICK:
 				return BRICKS;
 			case MOSSY_COBBLESTONE:
 				return MOSS_STONE;
 			case MOB_SPAWNER:
 				return MONSTER_SPAWNER;
 			case WOOD_STAIRS:
 				return OAK_WOOD_STAIRS;
 			case DIAMOND_BLOCK:
 				return BLOCK_OF_DIAMOND;
 			case WORKBENCH:
 				return CRAFTING_TABLE;
 			case CROPS:
 				return WHEAT;
 			case SOIL:
 				return FARMLAND;
			case FURNACE:
			case BURNING_FURNACE:
				return FURNACE;
 			case WOOD_DOOR:
 				return WOODEN_DOOR;
 			case RAILS:
 				return RAIL;
 			case STONE_PLATE:
 				return STONE_PRESSURE_PLATE;
 			case IRON_DOOR_BLOCK:
 				return IRON_DOOR;
 			case WOOD_PLATE:
 				return WOODEN_PRESSURE_PLATE;
			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
				return REDSTONE_ORE;
 			case REDSTONE_TORCH_OFF:
 			case REDSTONE_TORCH_ON:
 				return REDSTONE_TORCH;
 			case SUGAR_CANE:
 			case SUGAR_CANE_BLOCK:
 				return SUGAR_CANE;
 			case CAKE:
 			case CAKE_BLOCK:
 				return CAKE;
 			case DIODE:
 			case DIODE_BLOCK_OFF:
 			case DIODE_BLOCK_ON:
 				return REDSTONE_REPEATER;
 			case STAINED_GLASS:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return WHITE_STAINED_GLASS;
 					case 1:
 						return ORANGE_STAINED_GLASS;
 					case 2:
 						return MAGENTA_STAINED_GLASS;
 					case 3:
 						return LIGHT_BLUE_STAINED_GLASS;
 					case 4:
 						return YELLOW_STAINED_GLASS;
 					case 5:
 						return LIME_STAINED_GLASS;
 					case 6:
 						return PINK_STAINED_GLASS;
 					case 7:
 						return GRAY_STAINED_GLASS;
 					case 8:
 						return LIGHT_GRAY_STAINED_GLASS;
 					case 9:
 						return CYAN_STAINED_GLASS;
 					case 10:
 						return PURPLE_STAINED_GLASS;
 					case 11:
 						return BLUE_STAINED_GLASS;
 					case 12:
 						return BROWN_STAINED_GLASS;
 					case 13:
 						return GREEN_STAINED_GLASS;
 					case 14:
 						return RED_STAINED_GLASS;
 					case 15:
 						return BLACK_STAINED_GLASS;
 				}
 			case TRAP_DOOR:
 				return TRAPDOOR;
 			case MONSTER_EGGS:
 				return MONSTER_EGG; // TODO: Decide on Monster Egg or Silverfish Block. Also, split into subtypes?
 			case SMOOTH_BRICK:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return STONE_BRICKS;
 					case 1:
 						return MOSSY_STONE_BRICKS;
 					case 2:
 						return CRACKED_STONE_BRICKS;
 					case 3:
 						return CHISELED_STONE_BRICKS;
 				}
 			case HUGE_MUSHROOM_1:
 				return HUGE_BROWN_MUSHROOM;
 			case HUGE_MUSHROOM_2:
 				return HUGE_RED_MUSHROOM;
 			case IRON_FENCE:
 				return IRON_BARS;
 			case THIN_GLASS:
 				return GLASS_PANE;
 			case SMOOTH_STAIRS:
 				return STONE_BRICK_STAIRS;
 			case MYCEL:
 				return MYCELIUM;
 			case WATER_LILY:
 				return LILY_PAD;
 			case REDSTONE_LAMP_OFF:
 			case REDSTONE_LAMP_ON:
 				return REDSTONE_LAMP;
 			case WOOD_DOUBLE_STEP:
 			case WOOD_STEP:
 				switch (((WoodenStep) data).getSpecies()) {
 					case GENERIC:
 						if (duckBukkit == 4) // Bukkit reports ACACIA as GENERIC, brilliant!
 							return ACACIA_WOOD_SLAB;
 						return OAK_WOOD_SLAB;
 					case REDWOOD:
 						if (duckBukkit == 5) // Bukkit reports DARK_OAK as REDWOOD, brilliant!
 							return DARK_OAK_WOOD_SLAB;
 						return SPRUCE_WOOD_SLAB;
 					case BIRCH:
 						return BIRCH_WOOD_SLAB;
 					case JUNGLE:
 						return JUNGLE_WOOD_SLAB;
 					case ACACIA:
 						return ACACIA_WOOD_SLAB;
 					case DARK_OAK:
 						return DARK_OAK_WOOD_SLAB;
 				}
 			case COBBLE_WALL:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return COBBLESTONE_WALL;
 					case 1:
						return MOSSY_COBBLESTONE_WALL;
 				}
 			case CARROT:
 			case CARROT_ITEM:
 				return CARROT;
 			case POTATO:
 			case POTATO_ITEM:
 				return POTATO;
 			case SKULL:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return SKELETON_SKULL;
 					case 1:
 						return WITHER_SKELETON_SKULL;
 					case 2:
 						return ZOMBIE_HEAD;
 					case 3:
 						return HEAD;
 					case 4:
 						return CREEPER_HEAD;
 				}
 			case GOLD_PLATE:
 				return WEIGHTED_PRESSURE_PLATE_LIGHT;
 			case IRON_PLATE:
 				return WEIGHTED_PRESSURE_PLATE_HEAVY;
 			case REDSTONE_COMPARATOR:
 			case REDSTONE_COMPARATOR_OFF:
 			case REDSTONE_COMPARATOR_ON:
 				return REDSTONE_COMPARATOR;
 			case QUARTZ_BLOCK:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return BLOCK_OF_QUARTZ;
 					case 1:
 						return CHISELED_QUARTZ_BLOCK;
 					case 2:
 					case 3:
 					case 4:
 						return PILLAR_QUARTZ_BLOCK;
 				}
 			case STAINED_CLAY:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return WHITE_STAINED_CLAY;
 					case 1:
 						return ORANGE_STAINED_CLAY;
 					case 2:
 						return MAGENTA_STAINED_CLAY;
 					case 3:
 						return LIGHT_BLUE_STAINED_CLAY;
 					case 4:
 						return YELLOW_STAINED_CLAY;
 					case 5:
 						return LIME_STAINED_CLAY;
 					case 6:
 						return PINK_STAINED_CLAY;
 					case 7:
 						return GRAY_STAINED_CLAY;
 					case 8:
 						return LIGHT_GRAY_STAINED_CLAY;
 					case 9:
 						return CYAN_STAINED_CLAY;
 					case 10:
 						return PURPLE_STAINED_CLAY;
 					case 11:
 						return BLUE_STAINED_CLAY;
 					case 12:
 						return BROWN_STAINED_CLAY;
 					case 13:
 						return GREEN_STAINED_CLAY;
 					case 14:
 						return RED_STAINED_CLAY;
 					case 15:
 						return BLACK_STAINED_CLAY;
 				}
 			case STAINED_GLASS_PANE:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return WHITE_STAINED_GLASS_PANE;
 					case 1:
 						return ORANGE_STAINED_GLASS_PANE;
 					case 2:
 						return MAGENTA_STAINED_GLASS_PANE;
 					case 3:
 						return LIGHT_BLUE_STAINED_GLASS_PANE;
 					case 4:
 						return YELLOW_STAINED_GLASS_PANE;
 					case 5:
 						return LIME_STAINED_GLASS_PANE;
 					case 6:
 						return PINK_STAINED_GLASS_PANE;
 					case 7:
 						return GRAY_STAINED_GLASS_PANE;
 					case 8:
 						return LIGHT_GRAY_STAINED_GLASS_PANE;
 					case 9:
 						return CYAN_STAINED_GLASS_PANE;
 					case 10:
 						return PURPLE_STAINED_GLASS_PANE;
 					case 11:
 						return BLUE_STAINED_GLASS_PANE;
 					case 12:
 						return BROWN_STAINED_GLASS_PANE;
 					case 13:
 						return GREEN_STAINED_GLASS_PANE;
 					case 14:
 						return RED_STAINED_GLASS_PANE;
 					case 15:
 						return BLACK_STAINED_GLASS_PANE;
 				}
 			case CARPET:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return CARPET;
 					case 1:
 						return ORANGE_CARPET;
 					case 2:
 						return MAGENTA_CARPET;
 					case 3:
 						return LIGHT_BLUE_CARPET;
 					case 4:
 						return YELLOW_CARPET;
 					case 5:
 						return LIME_CARPET;
 					case 6:
 						return PINK_CARPET;
 					case 7:
 						return GRAY_CARPET;
 					case 8:
 						return LIGHT_GRAY_CARPET;
 					case 9:
 						return CYAN_CARPET;
 					case 10:
 						return PURPLE_CARPET;
 					case 11:
 						return BLUE_CARPET;
 					case 12:
 						return BROWN_CARPET;
 					case 13:
 						return GREEN_CARPET;
 					case 14:
 						return RED_CARPET;
 					case 15:
 						return BLACK_CARPET;
 				}
 			case DOUBLE_PLANT:
 				switch (duckBukkit) { // Bukkit deprecated a method with no alternative, brilliant!
 					case 0:
 						return SUNFLOWER;
 					case 1:
 						return LILAC;
 					case 2:
 						return DOUBLE_TALLGRASS;
 					case 3:
 						return LARGE_FERN;
 					case 4:
 						return ROSE_BUSH;
 					case 5:
 						return PEONY;
 					default:
 				}
 			case LOG_2: // Does not return MaterialData of type Tree, brilliant!
 				switch (duckBukkit % 4) {
 					case 0:
 						return ACACIA_LOG;
 					case 1:
 						return DARK_OAK_LOG;
 				}
 			case LEAVES_2: // Does not return MaterialData of type Tree, brilliant!
 				switch (duckBukkit % 4) {
 					case 0:
 						return ACACIA_LEAVES;
 					case 1:
 						return DARK_OAK_LEAVES;
 				}
 			default:
 				try {
 					return Material.valueOf(type.toString());
 				} catch (IllegalArgumentException e) {
 					Bukkit.broadcastMessage("Material " + type + " with data " + duckBukkit + " is not supported.");
 					return null;
 				}
 		}
 	}
 }
