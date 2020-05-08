 package com.tehbeard.map.schematic.blocks;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum BlockType{
 	//blocks that rely on other blocks being there
 	
 	CHEST(54,0,BlockRotation.CONTAINER_PISTON),
 	FURNACE_OFF(61,0,BlockRotation.CONTAINER_PISTON),
 	FURNACE_ON(62,0,BlockRotation.CONTAINER_PISTON),
 	
 	//redstone
 	REDSTONE_WIRE(55,1),
 	STONE_PRESSURE_PLATE(70,1),
 	WOODEN_PRESSURE_PLATE(72,1),
 	GOLD_PRESSURE_PLATE(147,1),
 	IRON_PRESSURE_PLATE(148,1),
 	
 	REDSTONE_TORCH_OFF(75,1,BlockRotation.WALL_MOUNTED),
 	REDSTONE_TORCH_ON(76,1,BlockRotation.WALL_MOUNTED),
 	STONE_BUTTON(77,1,BlockRotation.WALL_MOUNTED),
 	WOOD_BUTTON(143,1,BlockRotation.WALL_MOUNTED),
 	
 	REDSTONE_REPEATER_OFF(93,1,BlockRotation.REPEATER),
 	REDSTONE_REPEATER_ON(94,1,BlockRotation.REPEATER),
 	REDSTONE_COMPARATOR_OFF(149,1,BlockRotation.REPEATER),
 	REDSTONE_COMPARATOR_ON(150,1,BlockRotation.REPEATER),
 	TRAP_DOOR(96,1,BlockRotation.TRAPDOOR),
 	PISTON_BASE(29,1,BlockRotation.CONTAINER_PISTON),
 	PISTON_STICKY_BASE(33,1,BlockRotation.CONTAINER_PISTON),
 	TRIPWIRE_HOOK(131,1,BlockRotation.HOOK),
 	TRIPWIRE(132,1),
 	
 	//rail
 	POWERED_RAIL(27,1,BlockRotation.RAIL),
 	DETECTOR_RAIL(28,1,BlockRotation.RAIL),
 	MINECART_TRACKS(66,1,BlockRotation.RAIL),
 	ACTIVATOR_RAIL(157,1,BlockRotation.RAIL),
 	
 	
 	
 	//nature
 	SAPLING(6,1),
 	LONG_GRASS(31,1),
 	DEAD_BUSH(32,1),
 	YELLOW_FLOWER(37,1),
 	RED_FLOWER(38,1),
 	BROWN_MUSHROOM(39,1),
 	RED_MUSHROOM(40,1),
 	CROPS(59,1),
 	VINE(106,1,BlockRotation.VINES),
 	LILY_PAD(111,1),
 	NETHER_WART(115,1),
 	ANVIL(145,1,BlockRotation.ANVIL),
 	CARROTS(141,1),
 	POTATOS(142,1),
 	
 	
 	//MISC
 	BED(26,1,BlockRotation.BED),
 	TORCH(50,1,BlockRotation.WALL_MOUNTED),
 	FIRE(51,1),
 	LADDER(65,1,BlockRotation.CONTAINER_PISTON),
 	LEVER(69,1,BlockRotation.WALL_MOUNTED),
 	SNOW(78,1),
 	PORTAL(90,1),
 	
 	
 	
 	
 	
 	//blocks affected by redstone/need blocks above to be there
 	PISTON_EXTENSION(34,2,BlockRotation.CONTAINER_PISTON),
 	PISTON_MOVING_PIECE(36,2,BlockRotation.CONTAINER_PISTON),
 	SIGN_POST(63,2,BlockRotation.SIGN_POST),
 	WOODEN_DOOR(64,2,BlockRotation.DOOR),
 	WALL_SIGN(68,2,BlockRotation.CONTAINER_PISTON),
 	IRON_DOOR(71,2,BlockRotation.DOOR),
 	CACTUS(81,2),
 	REED(83,2),
 	CAKE_BLOCK(92,2),
 	DISPENSER(23,2,BlockRotation.CONTAINER_PISTON),
 	DROPPER(158,2,BlockRotation.CONTAINER_PISTON),
 	HOPPER(154,2,BlockRotation.CONTAINER_PISTON);
 	
 	public final int typeId;
 	public final int order;
 	public BlockRotation rotate;
 
 	BlockType(int id,int order){
 		this(id,order,null);
 	}
 	BlockType(int id,int order,BlockRotation rotate){
 		this.typeId=id;
 		this.order = order;
 		this.rotate = rotate;
 	}
 
 	public static int getOrder(int typeId){
 		BlockType type = byId.get(typeId);
 		return type!=null ? type.order : 0;
 	}
 	
 	public static BlockType getByID(int typeId){
 		return byId.get(typeId);
 	}
 
 	private static Map<Integer,BlockType> byId = new HashMap<Integer,BlockType>();
 	static{
 		for(BlockType type : EnumSet.allOf(BlockType.class)){
 			byId.put(type.typeId, type);
 		}
 	}
 	
 	
 	
 
 }
