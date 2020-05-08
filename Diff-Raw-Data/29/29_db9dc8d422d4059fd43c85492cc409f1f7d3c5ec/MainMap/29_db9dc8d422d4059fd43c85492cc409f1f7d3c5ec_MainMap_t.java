 package main;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import main.components.AIComponent;
 import main.components.FighterComponent;
 import main.components.ItemComponent;
 import main.helpers.Rect;
 import main.objects.Entity;
 import main.objects.Tile;
 import net.slashie.libjcsi.CSIColor;
 import net.slashie.libjcsi.ConsoleSystemInterface;
 
 /**
  * Map class. Used to generate dungeon and draw map.
  * 
  * @author prokk
  * 
  */
 public class MainMap {
 	// private static Logger log = Logger.getLogger(MainMap.class.getName());
 
 	// //// SIZE OF MAPS
 	public final static int MAP_WIDTH = 100;
 	public final static int MAP_HEIGHT = 100;
 
 	public final static int CAMERA_WIDTH = 80;
 	public final static int CAMERA_HEIGHT = 40;
 
 	// ///// DUNGEON GENERATOR PARAMETERS
 	private final static int ROOM_MAX_SIZE = 15;
 	private final static int ROOM_MIN_SIZE = 6;
 	private final static int MAX_ROOMS = 50;
 	private final static int MAX_ROOM_MONSTERS = 3;
 	private final static int MAX_ROOM_ITEMS = 2;
 
 	private static MainMap instance = new MainMap();
 
 	/**
 	 * 
 	 * @return instance of map
 	 */
 	public static MainMap getInstance() {
 		return instance;
 	}
 
 	private int dungeon_level = 1;
 	private Entity stairs;
 
 	private static Tile[][] map = MainGame.getInstance().getMap();
 	private static ConsoleSystemInterface csi = MainGame.getCSI();
 	private static Entity player = MainGame.getInstance().getPlayer();
 
 	private ArrayList<Entity> objects = new ArrayList<Entity>();
 
 	/**
 	 * 
 	 * @return objects on the map
 	 */
 	public ArrayList<Entity> getObjects() {
 		return objects;
 	}
 
 	/**
 	 * 
 	 * @return current level of the dungeon
 	 */
 	public int getCurrentLevel() {
 		return dungeon_level;
 	}
 
 	/**
 	 * Dungeon generator. Creates room in random position. Creates another and
 	 * connects to the previous using tunnels. Repeats several times. Spawns
 	 * player in first rooms and stairs in last room. Fills the room with
 	 * mosters and items.
 	 */
 	public void generateMap() {
 		// / fill the map with blocking tiles
 		for (int x = 0; x < MAP_WIDTH; x++)
 			for (int y = 0; y < MAP_HEIGHT; y++)
 				map[x][y] = new Tile(true);
 
 		// initialise array for rooms
 		Rect[] rooms = new Rect[MAX_ROOMS];
 		Random rand = new Random();
 
 		int num_rooms = 0;
 
 		int roomCenterX = 0;
 		int roomCenterY = 0;
 		for (int r = 0; r < MAX_ROOMS; r++) {
 			// randomizing room size
 			int w = rand.nextInt((ROOM_MAX_SIZE - ROOM_MIN_SIZE))
 					+ ROOM_MIN_SIZE;
 			int h = rand.nextInt((ROOM_MAX_SIZE - ROOM_MIN_SIZE))
 					+ ROOM_MIN_SIZE;
 
 			// randomizing room position
 			int x = rand.nextInt(MAP_WIDTH - w - 1);
 			int y = rand.nextInt(MAP_HEIGHT - h - 1);
 
 			// Rectangle class makes rectangles easier to work with
 			Rect new_room = new Rect(x, y, w, h);
 
 			// check if new room doesn't intersect with older ones
 			boolean failed = false;
 			for (Rect other_room : rooms) {
 				if (other_room != null)
 					if (new_room.intersect(other_room)) {
 						failed = true;
 						break;
 					}
 			}
 
 			if (!failed) {
 				// this means there are no intersections, so this room is valid
 				createRoom(new_room); // create rooms
 				placeObjects(new_room); // create objects in room
 
 				// save room center for tunnel generating
 				roomCenterX = new_room.getCenterX();
 				roomCenterY = new_room.getCenterY();
 
 				if (num_rooms == 0) {
 					// set player in first room
 					player.setX(roomCenterX);
 					player.setY(roomCenterY);
 				} else {
 
 					// get previous room center
 					int prevRoomCenterX = rooms[num_rooms - 1].getCenterX();
 					int prevRoomCenterY = rooms[num_rooms - 1].getCenterY();
 
 					// connect current and previous room with tunnel
 					if (rand.nextInt(1) == 1) {
 						create_h_tunnel(prevRoomCenterX, roomCenterX,
 								prevRoomCenterY);
 						create_v_tunnel(prevRoomCenterY, roomCenterY,
 								roomCenterX);
 					} else {
 
 						create_v_tunnel(prevRoomCenterY, roomCenterY,
 								prevRoomCenterX);
 						create_h_tunnel(prevRoomCenterX, roomCenterX,
 								roomCenterY);
 					}
 				}
 
 				// append new room to array
 				rooms[num_rooms] = new_room;
 				num_rooms++;
 
 			}
 
 		}
 		stairs = new Entity(roomCenterX, roomCenterY, '<', "stairs",
 				CSIColor.WHITE, false);
 		objects.add(stairs);
 
 		for (int x = 1; x < MAP_WIDTH - 1; x++)
 			for (int y = 1; y < MAP_HEIGHT - 1; y++)
 				if (map[x][y].isBlockedSight())
 					if (!map[x - 1][y - 1].isBlockedSight()
 							|| !map[x - 1][y].isBlockedSight()
 							|| !map[x - 1][y + 1].isBlockedSight()
 							|| !map[x][y - 1].isBlockedSight()
 							|| !map[x][y + 1].isBlockedSight()
 							|| !map[x + 1][y - 1].isBlockedSight()
 							|| !map[x + 1][y].isBlockedSight()
 							|| !map[x + 1][y + 1].isBlockedSight())
 						map[x][y].setWall(true);
 
 	}
 
 	/**
 	 * Create a room in a rect
 	 * 
 	 * @param room
 	 *            rect
 	 */
 	public static void createRoom(Rect room) {
 		for (int x = room.getX1() + 1; x < room.getX2(); x++)
 			for (int y = room.getY1() + 1; y < room.getY2(); y++) {
 				// fillig the room with empty tiles
 				map[x][y].setBlocked(false);
 				map[x][y].setBlockedSight(false);
 			}
 	}
 
 	/**
 	 * Create horizontal tunnel
 	 * 
 	 * @param x1
 	 *            position of start
 	 * @param x2
 	 *            position of end
 	 * @param y
 	 *            position of both
 	 */
 	public static void create_h_tunnel(int x1, int x2, int y) {
 		// horizontal tunnel. min() and max() are used in case x1>x2
 
 		for (int x = Math.min(x1, x2); x < Math.max(x1, x2) + 1; x++) {
 			map[x][y].setBlocked(false);
 			map[x][y].setBlockedSight(false);
 		}
 	}
 
 	/**
 	 * Creates vertical tunnel
 	 * 
 	 * @param y1
 	 *            position of start
 	 * @param y2
 	 *            position of end
 	 * @param x
 	 *            position of both
 	 */
 	public static void create_v_tunnel(int y1, int y2, int x) {
 		for (int y = Math.min(y1, y2); y < Math.max(y1, y2) + 1; y++) {
 			map[x][y].setBlocked(false);
 			map[x][y].setBlockedSight(false);
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param room
 	 *            rect
 	 */
 	public void placeObjects(Rect room) {
 
 		// / first placing monsters
 		Random rand = new Random();
 		int num_Monsters = rand.nextInt(MAX_ROOM_MONSTERS);
 		for (int i = 0; i < num_Monsters; i++) {
 			// random object position
 			int x = rand.nextInt(room.getX2() - room.getX1()) + room.getX1();
 			int y = rand.nextInt(room.getY2() - room.getY1()) + room.getY1();
 			if (!isBlocked(x, y))
 
 				if (rand.nextInt(100) < 80) { // 80% - orc
					Entity monster = new Entity(x, y, 'O', "orc",
 							CSIColor.LIME_GREEN, true);
 					FighterComponent fighter_component = new FighterComponent(
							monster, 8 + 3 * dungeon_level,
 							30 + 7 * dungeon_level, 0, 2 + dungeon_level);
					AIComponent ai_component = new AIComponent(monster);
					monster.setFighterComponent(fighter_component);
					monster.setAIComponent(ai_component);
					objects.add(monster);
 				} else { // 20% - troll
					Entity monster = new Entity(x, y, 'T', "troll",
 							CSIColor.GREEN, true);
 					FighterComponent fighter_component = new FighterComponent(
							monster, 12 + 3 * dungeon_level,
 							100 + 10 * dungeon_level,
 							(int) (1 + dungeon_level * 0.5f), 4 + dungeon_level);
					AIComponent ai_component = new AIComponent(monster);
					monster.setFighterComponent(fighter_component);
					monster.setAIComponent(ai_component);
					objects.add(monster);
 				}
 		}
 
 		// // now placing items
 		int num_items = rand.nextInt(MAX_ROOM_ITEMS);
 		for (int i = 0; i < num_items; i++) {
 			int x = rand.nextInt(room.getX2() - room.getX1() - 1)
 					+ room.getX1() + 1;
 			int y = rand.nextInt(room.getY2() - room.getY1() - 1)
 					+ room.getY1() + 1;
 			if (!isBlocked(x, y)) {
 				int dice = rand.nextInt(100);
 				if (dice < 60) {
 					Entity item = new Entity(x, y, '!', "healing potion",
 							CSIColor.VIOLET, false);
 					ItemComponent itemComponent = new ItemComponent(item,
 							ItemComponent.HEALING_POTION);
 					item.setItemComponent(itemComponent);
 					objects.add(item);
 				} else if (dice > 60 && dice < 80) {
 					Entity item = new Entity(x, y, '!', "mana potion",
 							CSIColor.VIOLET, false);
 					ItemComponent itemComponent = new ItemComponent(item,
 							ItemComponent.MANA_POTION);
 					item.setItemComponent(itemComponent);
 					objects.add(item);
 				} else if (dice > 80 && dice < 95) {
 					Entity item = new Entity(x, y, '!', "lightning scroll",
 							CSIColor.VIOLET, false);
 					ItemComponent itemComponent = new ItemComponent(item,
 							ItemComponent.LIGHTNING);
 					item.setItemComponent(itemComponent);
 					objects.add(item);
 				} else {
 					Entity item = new Entity(x, y, '!', "confusion scroll",
 							CSIColor.VIOLET, false);
 					ItemComponent itemComponent = new ItemComponent(item,
 							ItemComponent.CONFUSION);
 					item.setItemComponent(itemComponent);
 					objects.add(item);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks if the Tile is blocked with something
 	 * 
 	 * @param x
 	 *            coord of tile
 	 * @param y
 	 *            coord of tile
 	 * @return blocked
 	 */
 	public boolean isBlocked(int x, int y) {
 		if (map[x][y].isBlocked())
 			return true;
 
 		for (Entity object : objects) {
 			if (object.blocks() && (x == object.getX()) && (y == object.getY()))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Draw map
 	 */
 	public void drawMap() {
 		FieldOfView.getInstance().calculate();
 
 		for (int x = 0; x < CAMERA_WIDTH; x++)
 			for (int y = 0; y < CAMERA_HEIGHT; y++) {
 				int map_x = x + camera_x;
 				int map_y = y + camera_y;
 
 				if (FieldOfView.getInstance().getFov()[map_x][map_y]) { // is
 																		// visible
 					map[map_x][map_y].setVisited(true);
 
 					if (!map[map_x][map_y].isWall())
 						csi.print(x, y, ".", CSIColor.BROWNER);
 					else
 						csi.print(x, y, "#", CSIColor.BROWNER);
 
 					for (Entity object : objects)
 						if (object.getX() == map_x && object.getY() == map_y)
 							object.draw();
 				} else if (map[map_x][map_y].wasVisited()) {
 					if (!map[map_x][map_y].isWall())
 						csi.print(x, y, ".", CSIColor.BISTRE);
 					else
 						csi.print(x, y, "#", CSIColor.BISTRE);
 				}
 
 			}
 	}
 
 	/**
 	 * Move the player with the distance. Attack if there is a target in the
 	 * moving position
 	 * 
 	 * @param dx
 	 *            distance
 	 * @param dy
 	 *            distance
 	 */
 	public void playerMoveOrAttack(int dx, int dy) {
 		int x = player.getX() + dx;
 		int y = player.getY() + dy;
 
 		Entity target = null;
 		for (Entity object : objects)
 			if ((object.getX() == x) && (object.getY() == y)) {
 				target = object;
 				break;
 			}
 
 		if (target != null) {
 			if (target.getName().equals("orc")
 					|| target.getName().equals("troll")) {
 				player.getFighterComponent().attack(target);
 				return;
 			}
 		}
 		MainGame.getInstance().getPlayer().move(dx, dy);
 
 	}
 
 	/**
 	 * Proceed to the next level
 	 */
 	public void nextLevel() {
 		if (stairs.getX() == player.getX() && stairs.getY() == player.getY()) {
 			player.getFighterComponent().healFor(
 					player.getFighterComponent().getMaxHP() / 2);
 			dungeon_level++;
 			objects.clear();
 			MainGame.getInstance().newMessage(
 					"You proceed to level " + dungeon_level, CSIColor.GOLDEN);
 			generateMap();
 			drawMap();
 		} else
 			return;
 	}
 
 	/**
 	 * Grab item under players feet
 	 */
 	public void grabItem() {
 		Entity object;
 		for (int i = 0; i < objects.size(); i++) {
 			object = objects.get(i);
 			if (object.getX() == player.getX()
 					&& object.getY() == player.getY()) {
 				if (object.getItemComponent() != null)
 					object.getItemComponent().pickUp();
 			}
 		}
 	}
 
 	/**
 	 * Find closest monster in range
 	 * 
 	 * @param max_range
 	 *            to search in
 	 * @return entity
 	 */
 	public Entity getClosestMonster(int max_range) {
 		Entity closest = null;
 		int closest_dist = max_range + 1;
 
 		for (Entity object : objects) {
 			if (object.getFighterComponent() != null && object != player) {
 				int dist = (int) player.distanceTo(object);
 				if (dist < closest_dist) {
 					closest = object;
 					closest_dist = dist;
 				}
 			}
 		}
 
 		return closest;
 	}
 
 	private static int camera_x = 0;
 	private static int camera_y = 0;
 
 	/**
 	 * Move the Camera on the map
 	 * 
 	 * @param target_x
 	 *            position
 	 * @param target_y
 	 *            position
 	 */
 	public void moveCamera(int target_x, int target_y) {
 		int x = target_x - CAMERA_WIDTH / 2;
 		int y = target_y - CAMERA_HEIGHT / 2;
 		if (x < 0)
 			x = 0;
 		if (y < 0)
 			y = 0;
 		if (x > (MAP_WIDTH - CAMERA_WIDTH - 1))
 			x = MAP_WIDTH - CAMERA_WIDTH - 1;
 		if (y > (MAP_HEIGHT - CAMERA_HEIGHT - 1))
 			y = MAP_HEIGHT - CAMERA_HEIGHT - 1;
 		camera_x = x;
 		camera_y = y;
 	}
 
 	/**
 	 * Convert map coord to camera coord
 	 * 
 	 * @param x
 	 * @return map coord x
 	 */
 	public int toCameraCoordX(int x) {
 		return x - camera_x;
 	}
 
 	/**
 	 * Convert map coord to camera coord
 	 * 
 	 * @param y
 	 * @return map coord y
 	 */
 	public int toCameraCoordY(int y) {
 		return y - camera_y;
 	}
 }
