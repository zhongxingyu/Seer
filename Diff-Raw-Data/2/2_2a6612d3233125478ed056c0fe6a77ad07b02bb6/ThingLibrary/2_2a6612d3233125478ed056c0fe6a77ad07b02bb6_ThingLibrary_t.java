 package ui.isometric.builder.things;
 
 import game.GameThing;
 import game.GameWorld;
 import game.Location;
 
 import java.awt.image.BufferedImage;
 import java.util.*;
 
 import ui.isometric.libraries.IsoInventoryImageLibrary;
 import ui.isometric.libraries.IsoRendererLibrary;
 import util.Direction;
 
 /**
  * A class to manage all the ThingCreators
  * 
  * @author melby
  *
  */
 public class ThingLibrary {
 	/**
 	 * A class that generates new GroundThings
 	 * @author melby
 	 *
 	 */
 	public static class GroundCreator implements ThingCreator {
 		private String renderer;
 		private boolean willBlock;
 		
 		/**
 		 * Create a ground tile with a given renderer
 		 * @param rendererName
 		 * @param block
 		 */
 		public GroundCreator(String rendererName, boolean block) {
 			renderer = rendererName;
 			willBlock = block;
 		}
 		
 		/**
 		 * Create a GroundCreator with a given renderer name
 		 * @param rendererName
 		 */
 		public GroundCreator(String rendererName) {
 			this(rendererName, false);
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			return new game.things.GroundTile(w, renderer, willBlock);
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(renderer, Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;{add(renderer);}};
 		}
 
 		@Override
 		public String description() {
 			return "Ground: "+renderer;
 		}
 	}
 	
 	/**
 	 * A class that generates new Players
 	 * @author melby
 	 *
 	 */
 	public static class PlayerCreator implements ThingCreator {
 		private String characterName;
 		
 		/**
 		 * Create a player with a given renderer
 		 * @param characterName
 		 */
 		public PlayerCreator(String characterName) {
 			this.characterName = characterName;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.Player player = new game.things.Player(w, characterName);
 			return player;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName("character_"+characterName+"_empty", Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;
 				{
 					add("character_"+characterName+"_empty");
 					add("character_"+characterName+"_empty_attack");
 					add("character_"+characterName+"_empty_die");
 					add("character_"+characterName+"_sword");
 					add("character_"+characterName+"_sword_attack");
 					add("character_"+characterName+"_sword_die");
 				}};
 		}
 
 		@Override
 		public String description() {
 			return "Player: "+characterName;
 		}
 	}
 	
 	/**
 	 * A class that generates walls
 	 * @author melby
 	 *
 	 */
 	public static class WallCreator implements ThingCreator {
 		private String renderer;
 		
 		/**
 		 * Create a wall with a given renderer
 		 * @param rendererName
 		 */
 		public WallCreator(String rendererName) {
 			renderer = rendererName;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.Wall wall = new game.things.Wall(w, renderer);
 			return wall;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(renderer, Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;{add(renderer);}};
 		}
 
 		@Override
 		public String description() {
 			return "Wall: "+renderer;
 		}
 	}
 	
 	/**
 	 * A class that generates doors
 	 * @author melby
 	 *
 	 */
 	public static class DoorCreator implements ThingCreator {
 		private String openR;
 		private String closedR;
 		private boolean open;
 		
 		/**
 		 * Create a door with given renderer + open state
 		 * @param closedR - closed renderer
 		 * @param openR - open renderer
 		 * @param open - open state
 		 */
 		public DoorCreator(String closedR, String openR, boolean open) {
 			this.openR = openR;
 			this.closedR = closedR;
 			this.open = open;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.Door door = new game.things.Door(w, closedR, openR, open);
 			return door;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(open?openR:closedR, Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;{add(openR);add(closedR);}};
 		}
 
 		@Override
 		public String description() {
 			return "Door ("+(open?"open":"closed")+"): "+openR;
 		}
 	}
 	
 	/**
 	 * A class that generates spawn points
 	 * @author melby
 	 *
 	 */
 	public static class SpawnPointCreator implements ThingCreator {
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.SpawnPoint spawn = new game.things.SpawnPoint(w);
 			return spawn;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName("spawn_point", Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;{add("spawn_point");}};
 		}
 
 		@Override
 		public String description() {
 			return "Spawn Point";
 		}
 	}
 	
 	/**
 	 * A class that generates openable furniture
 	 * @author melby
 	 *
 	 */
 	public static class OpenableFurnitureCreator implements ThingCreator {
 		private String renderer;
 		private boolean open;
 		
 		/**
 		 * Create a OpenableFurnitureCreator with the given renderer and open state
 		 * @param renderer
 		 * @param open
 		 */
 		public OpenableFurnitureCreator(String renderer, boolean open) {
 			this.renderer = renderer;
 			this.open = open;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.OpenableFurniture fur = new game.things.OpenableFurniture(w, renderer, open, null);
 			return fur;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(renderer+"_closed", Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;
 				{
 					add(renderer+"_open");
 					add(renderer+"_closed");
 				}
 			};
 		}
 
 		@Override
 		public String description() {
 			return "Openable Furniture ("+(open?"open":"closed")+"): "+renderer;
 		}
 	}
 	
 	/**
 	 * A class that generates equipment
 	 * @author melby
 	 *
 	 */
 	public static class EquipmentCreator implements ThingCreator {
 		private String renderer;
 		private int attack;
 		private game.things.EquipmentGameThing.Slot type;
 		private int strength;
 		private int defense;
 		private int delay;
 		private String name;
 		
 		/**
 		 * Create an EquipmentCreator with the given parameters
 		 * @param renderer
 		 * @param attack
 		 * @param strength
 		 * @param defense
 		 * @param delay
 		 * @param name
 		 * @param type
 		 */
 		public EquipmentCreator(String renderer, int attack, int strength, int defense, int delay, String name, game.things.EquipmentGameThing.Slot type) {
 			this.renderer = renderer;
 			this.attack = attack;
 			this.strength = strength;
 			this.defense = defense;
 			this.delay = delay;
 			this.name = name;
 			this.type = type;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.EquipmentGameThing equip = new game.things.EquipmentGameThing(w, attack, strength, defense, delay, type, name, renderer);
 			return equip;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoInventoryImageLibrary.imageForName(renderer);
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;{add(renderer);}};
 		}
 
 		@Override
 		public String description() {
 			return name+" (attack:"+attack+" strength:"+strength+" defense:"+defense+" delay:"+((float)defense/1000.0f)+"s)";
 		}
 	}
 	
 	/**
 	 * A class that generates Valuable game things
 	 * @author melby
 	 *
 	 */
 	public static class ValuableThingCreator implements ThingCreator {
 		private String renderer;
 		private String name;
 		private int value;
 		
 		/**
 		 * Create a ValuableThingCreator with the given renderer, name and value
 		 * @param renderer
 		 * @param name
 		 * @param value
 		 */
 		public ValuableThingCreator(String renderer, String name, int value) {
 			this.renderer = renderer;
 			this.name = name;
 			this.value = value;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.PickupGameThing pick = new game.things.Valuable(w, name, renderer, value);
 			return pick;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(renderer, Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;{add(renderer);}};
 		}
 
 		@Override
 		public String description() {
 			return name+" worth "+value;
 		}
 	}
 	
 	/**
 	 * A class that generates Coins
 	 * @author melby
 	 *
 	 */
 	public static class CoinThingCreator implements ThingCreator {
 		private int amount;
 		
 		/**
 		 * Create a CoinThingCreator with the amount of coins
 		 * @param amount
 		 */
 		public CoinThingCreator(int amount) {
 			this.amount = amount;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.Coins coin = new game.things.Coins(w, amount);
 			return coin;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName("coins_gold", Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;
 				{
 					add("coins_gold");
 					add("coins_bronze");
 					add("coins_silver");
 				}
 			};
 		}
 
 		@Override
 		public String description() {
 			return amount+" "+(amount>1?"coins":"coin");
 		}
 	}
 	
 	/**
 	 * A class that Creates NPC's
 	 * @author melby
 	 *
 	 */
 	public static class NPCCreator implements ThingCreator {		
 		private String renderer;
 		private String name;
 		private int distance;
 
 		public NPCCreator(String renderer, String name, int distance) {
 			this.renderer = renderer;
 			this.name = name;
 			this.distance = distance;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w, Location l) {
 			game.things.Enemy enemy = new game.things.Enemy(w, renderer, name, l, distance);
 			return enemy;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName("character_"+renderer+"_empty", Direction.NORTH).image();
 		}
 		
 		@Override
 		public Set<String> rendererNames() {
 			return new HashSet<String>(){private static final long serialVersionUID = 1L;
 				{
 					add("character_"+renderer+"_empty");
					add("character_"+renderer+"_empty_attack");
 					add("character_"+renderer+"_empty_die");
 				}
 			};
 		}
 
 		@Override
 		public String description() {
 			return "NPC: "+name;
 		}
 	}
 	
 	private static List<ThingCreator> creators = null;
 	private static List<ThingCreator> unmodifiable = null;
 	
 	/**
 	 * Initialize all the internal ThingCreators
 	 */
 	private static void setupCreators() {
 		synchronized(ThingLibrary.class) {
 			if(creators == null) {
 				creators = new ArrayList<ThingCreator>();
 				unmodifiable = Collections.unmodifiableList(creators);
 				
 				creators.add(new GroundCreator("ground_grey_1"));
 				creators.add(new GroundCreator("ground_grey_2"));
 				creators.add(new GroundCreator("ground_grey_trash_1"));
 				creators.add(new GroundCreator("ground_grey_green_dots_1"));
 				creators.add(new GroundCreator("ground_grey_patch_1"));
 				creators.add(new GroundCreator("ground_grey_dark_dots_1"));
 				creators.add(new GroundCreator("ground_grey_dark_circle_1"));
 				creators.add(new GroundCreator("ground_grey_red_dots_1"));
 				creators.add(new GroundCreator("ground_grey_greenish_1"));
 				creators.add(new GroundCreator("ground_grey_greenish_2"));
 				creators.add(new GroundCreator("ground_grey_pool_1", true));
 				creators.add(new GroundCreator("ground_grey_pools_1", true));
 				creators.add(new GroundCreator("ground_grey_pools_2", true));
 				creators.add(new GroundCreator("ground_grey_rock_1", true));
 				creators.add(new GroundCreator("ground_grey_rock_2", true));
 				creators.add(new GroundCreator("ground_grey_rock_3", true));
 				creators.add(new GroundCreator("ground_grey_stones_1"));
 				creators.add(new GroundCreator("ground_grey_spikes_1", true));
 				creators.add(new GroundCreator("ground_grey_spikes_2", true));
 				creators.add(new GroundCreator("ground_grey_spikes_3", true));
 				
 				creators.add(new GroundCreator("ground_grey_water_corner", true));
 				creators.add(new GroundCreator("ground_grey_water_two_sides", true));
 				creators.add(new GroundCreator("ground_grey_water_one_side", true));
 				creators.add(new GroundCreator("ground_grey_water_island_1", true));
 				creators.add(new GroundCreator("ground_grey_water_rock_1", true));
 				
 				creators.add(new GroundCreator("ground_grey_mushrooms_1"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_2"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_3"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_4"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_5"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_6"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_7"));
 				creators.add(new GroundCreator("ground_grey_mushrooms_8"));
 				
 				creators.add(new GroundCreator("water_1", true));
 				
 				creators.add(new GroundCreator("ground_grey_road_corner_1"));
 				creators.add(new GroundCreator("ground_grey_road_end_1"));
 				creators.add(new GroundCreator("ground_grey_road_straight_1"));
 				creators.add(new GroundCreator("ground_grey_road_t_1"));
 				creators.add(new GroundCreator("ground_grey_road_x_1"));
 				
 				creators.add(new GroundCreator("ground_grey_tile_1_corner_1"));
 				creators.add(new GroundCreator("ground_grey_tile_1_one_side_1"));
 				creators.add(new GroundCreator("ground_grey_tile_1_two_sides_1"));
 				creators.add(new GroundCreator("ground_tile_1"));
 				creators.add(new GroundCreator("ground_tile_1_greenish_1"));
 				
 				creators.add(new WallCreator("wall_brown_1_corner"));
 				creators.add(new WallCreator("wall_brown_1_x"));
 				creators.add(new WallCreator("wall_brown_1_t"));
 				creators.add(new WallCreator("wall_brown_1_straight"));
 				
 				creators.add(new DoorCreator("wall_brown_1_door_closed", "wall_brown_1_door_open", false));
 				
 				creators.add(new WallCreator("wall_grey_1_corner"));
 				creators.add(new WallCreator("wall_grey_1_x"));
 				creators.add(new WallCreator("wall_grey_1_t"));
 				creators.add(new WallCreator("wall_grey_1_straight"));
 				
 				creators.add(new DoorCreator("wall_grey_1_door_closed", "wall_grey_1_door_open", false));
 				
 				creators.add(new WallCreator("wall_grey_2_corner"));
 				creators.add(new WallCreator("wall_grey_2_x"));
 				creators.add(new WallCreator("wall_grey_2_t"));
 				creators.add(new WallCreator("wall_grey_2_straight"));
 				
 				creators.add(new DoorCreator("wall_grey_2_door_closed", "wall_grey_2_door_open", false));
 				
 				creators.add(new WallCreator("wall_grey_3_corner"));
 				creators.add(new WallCreator("wall_grey_3_x"));
 				creators.add(new WallCreator("wall_grey_3_t"));
 				creators.add(new WallCreator("wall_grey_3_straight"));
 				
 				creators.add(new DoorCreator("wall_grey_3_door_closed", "wall_grey_3_door_open", false));
 				
 				creators.add(new WallCreator("wall_grey_4_corner"));
 				creators.add(new WallCreator("wall_grey_4_x"));
 				creators.add(new WallCreator("wall_grey_4_t"));
 				creators.add(new WallCreator("wall_grey_4_straight"));
 				
 				creators.add(new DoorCreator("wall_grey_4_door_closed", "wall_grey_4_door_open", false));
 				
 				creators.add(new GroundCreator("ground_grey_tile_2_corner"));
 				creators.add(new GroundCreator("ground_grey_tile_2_one_side"));
 				creators.add(new GroundCreator("ground_grey_tile_2_two_sides"));
 				creators.add(new GroundCreator("ground_grey_tile_2_loose_1"));
 				creators.add(new GroundCreator("ground_grey_tile_2_loose_2"));
 				creators.add(new GroundCreator("ground_tile_2"));
 				creators.add(new GroundCreator("ground_tile_2_2"));
 				creators.add(new GroundCreator("ground_tile_2_greenish_1"));
 				creators.add(new GroundCreator("ground_tile_2_trash_1"));
 				creators.add(new GroundCreator("ground_tile_2_gravel_1"));
 				creators.add(new GroundCreator("ground_tile_2_green_dots_1"));
 				creators.add(new GroundCreator("ground_tile_2_red_dots_1"));
 				
 				creators.add(new GroundCreator("ground_tile_1_tile_2_corner"));
 				creators.add(new GroundCreator("ground_tile_1_tile_2_one_side"));
 				creators.add(new GroundCreator("ground_tile_1_tile_2_two_sides"));
 				
 				creators.add(new WallCreator("plant_1"));
 				creators.add(new WallCreator("plant_2"));
 				creators.add(new WallCreator("plant_3"));
 				creators.add(new WallCreator("plant_4"));
 				creators.add(new WallCreator("plant_5"));
 				creators.add(new WallCreator("plant_6"));
 				
 				creators.add(new WallCreator("stake_skull_1"));
 				creators.add(new WallCreator("stake_skull_2"));
 				creators.add(new WallCreator("stake_skull_3"));
 				
 				creators.add(new WallCreator("ground_grey_obelisk_1"));
 				creators.add(new WallCreator("ground_grey_obelisk_2"));
 				creators.add(new WallCreator("ground_grey_tree_1"));
 				creators.add(new WallCreator("ground_grey_tree_2"));
 				creators.add(new WallCreator("ground_grey_tree_3"));
 				creators.add(new WallCreator("ground_grey_tree_4"));
 				creators.add(new WallCreator("ground_grey_tree_5"));
 				
 				creators.add(new OpenableFurnitureCreator("barrel_1", false));
 				creators.add(new OpenableFurnitureCreator("chest_1", false));
 				creators.add(new OpenableFurnitureCreator("chest_2", false));
 				creators.add(new OpenableFurnitureCreator("chest_3", false));
 				creators.add(new OpenableFurnitureCreator("box_1", false));
 				creators.add(new OpenableFurnitureCreator("cupboard_1", false));
 				
 				creators.add(new WallCreator("barrel_2"));
 				creators.add(new WallCreator("barrel_3"));
 				
 				creators.add(new EquipmentCreator("sword_1", 5, 0, 0, 1500, "Sword_1", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_2", 5, 0, 0, 1500, "Sword_2", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_3", 5, 0, 0, 1500, "Sword_3", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_4", 5, 0, 0, 1500, "Sword_4", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_5", 5, 0, 0, 1500, "Sword_5", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_6", 5, 0, 0, 1500, "Sword_6", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_7", 5, 0, 0, 1500, "Sword_7", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_8", 5, 0, 0, 1500, "Sword_8", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_9", 5, 0, 0, 1500, "Sword_9", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_10", 5, 0, 0, 1500, "Sword_10", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_11", 5, 0, 0, 1500, "Sword_11", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				creators.add(new EquipmentCreator("sword_12", 5, 0, 0, 1500, "Sword_12", game.things.EquipmentGameThing.Slot.WEAPON)); // TODO: name
 				
 				creators.add(new EquipmentCreator("shield_bronze", 0, 0, 5, 1500, "Bronze Shield", game.things.EquipmentGameThing.Slot.SHIELD));
 				creators.add(new EquipmentCreator("shield_long", 0, 0, 5, 1500, "Long Shield", game.things.EquipmentGameThing.Slot.SHIELD));
 				creators.add(new EquipmentCreator("shield_plate", 0, 0, 5, 1500, "Plate Shield", game.things.EquipmentGameThing.Slot.SHIELD));
 				creators.add(new EquipmentCreator("shield_wood", 0, 0, 5, 1500, "Wood Shield", game.things.EquipmentGameThing.Slot.SHIELD));
 				
 				creators.add(new EquipmentCreator("helmet_iron", 0, 0, 5, 1500, "Iron Helmet", game.things.EquipmentGameThing.Slot.HELMET));
 				creators.add(new EquipmentCreator("helmet_leather", 0, 0, 5, 1500, "Leather Helmet", game.things.EquipmentGameThing.Slot.HELMET));
 				
 				creators.add(new EquipmentCreator("gauntlets_iron", 0, 0, 5, 1500, "Iron Gauntlets", game.things.EquipmentGameThing.Slot.GAUNTLET));
 				creators.add(new EquipmentCreator("gauntlets_leather", 0, 0, 5, 1500, "Leather Gauntlets", game.things.EquipmentGameThing.Slot.GAUNTLET));
 				creators.add(new EquipmentCreator("gauntlets_silk", 0, 0, 5, 1500, "Silk Gauntlets", game.things.EquipmentGameThing.Slot.GAUNTLET));
 				
 				// TODO: cloak
 				
 				creators.add(new EquipmentCreator("boots_leather_shoes", 0, 0, 5, 1500, "Leather Shoes", game.things.EquipmentGameThing.Slot.BOOTS));
 				creators.add(new EquipmentCreator("boots_leather", 0, 0, 5, 1500, "Leather Boots", game.things.EquipmentGameThing.Slot.BOOTS));
 				creators.add(new EquipmentCreator("boots_steel", 0, 0, 5, 1500, "Steel Boots", game.things.EquipmentGameThing.Slot.BOOTS));
 				
 				creators.add(new EquipmentCreator("armour_chain", 0, 0, 5, 1500, "Chainmail", game.things.EquipmentGameThing.Slot.ARMOUR));
 				creators.add(new EquipmentCreator("armour_leather", 0, 0, 5, 1500, "Leather Armour", game.things.EquipmentGameThing.Slot.ARMOUR));
 				creators.add(new EquipmentCreator("armour_plate", 0, 0, 5, 1500, "Plate Armour", game.things.EquipmentGameThing.Slot.ARMOUR));
 				creators.add(new EquipmentCreator("armour_steel", 0, 0, 5, 1500, "Steel Armour", game.things.EquipmentGameThing.Slot.ARMOUR));
 				creators.add(new EquipmentCreator("armour_tunic", 0, 0, 5, 1500, "Tunic", game.things.EquipmentGameThing.Slot.ARMOUR));
 				
 				// TODO: stairs creator
 				
 				creators.add(new ValuableThingCreator("crystal_green", "Green Crystal", 0));
 				creators.add(new ValuableThingCreator("herbs_1", "Herbs", 0));
 				creators.add(new ValuableThingCreator("herbs_2", "Herbs", 0));
 				creators.add(new ValuableThingCreator("herbs_3", "Herbs", 0));
 				creators.add(new ValuableThingCreator("ruby", "Ruby", 0));
 				creators.add(new ValuableThingCreator("bar_gold", "Gold Bar", 0));
 				creators.add(new ValuableThingCreator("bar_steel", "Steel Bar", 0));
 				creators.add(new ValuableThingCreator("emerald", "Emerald", 0));
 				creators.add(new ValuableThingCreator("amber", "Amber", 0));
 				
 				creators.add(new CoinThingCreator(1));
 				
 				creators.add(new NPCCreator("bob", "Sir Robert", 10));
 				
 				creators.add(new SpawnPointCreator());
 				
 				ThingCreatorChecker.check();
 			}
 		}
 	}
 	
 	/**
 	 * Get all the ThingCreators
 	 * @return - an immutable list of ThingCreators
 	 */
 	public static List<ThingCreator> creators() {
 		synchronized(ThingLibrary.class) {
 			if(creators == null) {
 				setupCreators();
 			}
 		}
 		
 		return unmodifiable;
 	}
 }
