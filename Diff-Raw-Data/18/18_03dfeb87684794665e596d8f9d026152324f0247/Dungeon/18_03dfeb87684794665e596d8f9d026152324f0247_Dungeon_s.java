 package rpisdd.rpgme.gamelogic.dungeon.model;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 
 public class Dungeon {
 	public static final int DUNGEON_DIMMENSION = 5;
 	public static final int MIN_NUM_ROOMS = 4;
 
 	boolean generated;
 	int level;
 	public int start_x;
 	public int start_y;
 	Room map[][];
 
 	public Dungeon(int _level) {
 		this.level = _level;
 		this.generated = false;
 		Log.d("Debug", "Dungeon Constructor Called");
 	}
 
 	public Room[][] getMap() {
 		return map;
 	}
 
 	public void GenerateMap() {
 		boolean setStairs = false;
 		int roomCount = 0;
 
 		ArrayList<Room> roomsToExpand;
 		Random rand = new Random();
 		// Keep trying until an adequate dungeon is generated
 		while (roomCount < MIN_NUM_ROOMS) {
 			map = new Room[DUNGEON_DIMMENSION][DUNGEON_DIMMENSION];
 			setStairs = false;
 			roomCount = 0;
 
 			start_x = rand.nextInt(DUNGEON_DIMMENSION);
 			start_y = rand.nextInt(DUNGEON_DIMMENSION);
 			Room startRoom = new Room(null, start_x, start_y);
 			map[start_y][start_x] = startRoom;
 
 			roomsToExpand = new ArrayList<Room>();
 			roomsToExpand.add(startRoom);
 			// Expand on rooms until no new rooms have been added.
 			while (!roomsToExpand.isEmpty()) {
 				// Pick the next room to expand on
 				Room currentRoom = roomsToExpand.get(roomsToExpand.size() - 1);
 				int x = currentRoom.getX();
 				int y = currentRoom.getY();
 
 				Log.d("Debug", "Looking at: X: " + x + "  Y: " + y);
 
 				// remove the currentlyExpanded room
 				roomsToExpand.remove(roomsToExpand.size() - 1);
 				// Generate possible adjacent rooms
 				if (rand.nextInt(3) == 0) {
 					// Up
 					Log.d("Debug", "Trying to make room at: " + x + ","
 							+ (y - 1));
 					Room newRoom = this.generateRoom(x, y - 1);
 					if (newRoom != null) {
 						if (newRoom.getContent() != null) {
 							if (newRoom.getContent().getRoomType() == RoomType.STAIRS) {
 								setStairs = true;
 							}
 						}
 						roomsToExpand.add(newRoom);
 						roomCount++;
 						Log.d("Debug", "Up room added");
 					}
 				}
 				if (rand.nextInt(3) == 0) {
 					// Right
 					Log.d("Debug", "Trying to make room at: " + (x + 1) + ","
 							+ y);
 					Room newRoom = this.generateRoom(x + 1, y);
 					if (newRoom != null) {
 						if (newRoom.getContent() != null) {
 							if (newRoom.getContent().getRoomType() == RoomType.STAIRS) {
 								setStairs = true;
 							}
 						}
 						roomCount++;
 						roomsToExpand.add(newRoom);
 						Log.d("Debug", "Right room added");
 					}
 				}
 				if (rand.nextInt(3) == 0) {
 					// Down
 					Log.d("Debug", "Trying to make room at: " + x + ","
 							+ (y + 1));
 					Room newRoom = this.generateRoom(x, y + 1);
 					if (newRoom != null) {
 						if (newRoom.getContent() != null) {
 							if (newRoom.getContent().getRoomType() == RoomType.STAIRS) {
 								setStairs = true;
 							}
 						}
 						roomCount++;
 						roomsToExpand.add(newRoom);
 						Log.d("Debug", "Down room added");
 					}
 				}
 				if (rand.nextInt(3) == 0) {
 					// Left
 					Log.d("Debug", "Trying to make room at: " + (x - 1) + ","
 							+ y);
 					Room newRoom = this.generateRoom(x - 1, y);
 					if (newRoom != null) {
 						if (newRoom.getContent() != null) {
 							if (newRoom.getContent().getRoomType() == RoomType.STAIRS) {
 								setStairs = true;
 							}
 						}
 						roomCount++;
 						roomsToExpand.add(newRoom);
 						Log.d("Debug", "Left room added");
 					}
 				}
 			}
 			// Check to make sure enough rooms have been made
 			if (roomCount >= MIN_NUM_ROOMS) {
 				Log.d("Debug", "Generated at least " + MIN_NUM_ROOMS + "rooms");
 				// If stairs have not been set yet, pick any room
 				// excluding the start room and change it into a stairs room
 				while (!setStairs) {
 					Log.d("Debug", "Trying to place stairs");
 					int stairs_x = rand.nextInt(DUNGEON_DIMMENSION);
 					int stairs_y = rand.nextInt(DUNGEON_DIMMENSION);
 					if (!(stairs_x == start_x && stairs_y == start_y)) {
 						Room targetRoom = this.getRoom(stairs_x, stairs_y);
 						if (targetRoom != null) {
 							map[stairs_y][stairs_x] = new Room(new Stairs(),
 									stairs_x, stairs_y);
 							setStairs = true;
 						}
 					}
 				}
 			} else {
 				Log.d("Debug",
 						"FAILURE: didn't make enough rooms, trying again");
 			}
 		}
 		this.generated = true;
 	}
 
 	private Room generateRoom(int x_, int y_) {
 		if (inBounds(x_, y_)) {
 			if (!this.roomExists(x_, y_)) {
 				Room newRoom = new Room(generateRoomContent(), x_, y_);
 				this.map[y_][x_] = newRoom;
 				return newRoom;
 			} else {
 				Log.d("Debug", "ROOM ALREADY EXISTS");
 				return null;
 			}
 		} else {
 			Log.d("Debug", "ROOM NOT IN RANGE");
 			return null;
 		}
 	}
 
 	private RoomContent generateRoomContent() {
 		Random contentRand = new Random();
 		int contentNum = contentRand.nextInt(100);
 		RoomContent content = null;
 
 		if (contentNum < 5) {
 			content = new Stairs();
 		} else if (contentNum < 30) {
 			content = new Treasure(false, this.level);
 		} else if (contentNum < 80) {
 			content = Monster.selectMonster(this);
 		} else {
 			content = null;
 		}
 		return content;
 	}
 
 	// Load the saved dungeon
 	public void load(SharedPreferences p) {
 		map = new Room[DUNGEON_DIMMENSION][DUNGEON_DIMMENSION];
 
 		String stringRep = p.getString("Dungeon", "NOTGENERATED");
 		if (stringRep.compareTo("NOTGENERATED") == 0) {
 			this.level = 1;
 			this.GenerateMap();
 		} else {
 			Log.d("Debug", "Dungeon string: " + stringRep);
 			// Change the string representation into a dungeon
 			String[] rooms = stringRep.split("\\|");
 			Log.d("Debug", "Number of rooms: " + rooms.length);
 			for (String rum : stringRep.split("\\|")) {
 				Log.d("Debug", "Room test: " + rum + "\n");
 			}
 			this.level = Integer.parseInt(rooms[0]);
 			for (int r = 1; r < rooms.length; r++) {
 				Log.d("Debug", "Room string: " + rooms[r] + "\n");
 				String[] roomArgs = rooms[r].split("#");
 				Room newRoom = Room.getFromStringRepresentation(roomArgs);
 				this.map[newRoom.getY()][newRoom.getX()] = newRoom;
 				Log.d("Debug", "Done a loop\n");
 			}
 		}
 		this.generated = true;
 	}
 
 	public void save(Editor e) {
 		if (this.isGenerated()) {
 			String stringRep = "";
 			stringRep += this.level + "|";
 			for (int i = 0; i < DUNGEON_DIMMENSION; ++i) {
 				for (int j = 0; j < DUNGEON_DIMMENSION; ++j) {
 					Room currentRoom = this.getRoom(i, j);
 					if (currentRoom != null) {
 						stringRep += currentRoom.getStringRepresentation();
 						stringRep += "|";
 					}
 				}
 			}
 			e.putString("Dungeon", stringRep);
 		} else {
 			Log.d("Debug", "Dungeon not generated yet. Can't save it.\n");
 		}
 	}
 
 	public Room getRoom(int x, int y) {
 		if (!inBounds(x, y)) {
 			return null;
 		}
 		return map[y][x];
 	}
 
	// Visits a room. If that room is cleared
 	// then allow the player to visit nearby rooms
 	public void visitRoom(int x, int y, Activity activity) {
 		if (roomExists(x, y)) {
 
 			if (map[y][x].visit(activity)) {
 				if (roomExists(x + 1, y)) {
 					map[y][x + 1].setVisitable();
 				}
 				if (roomExists(x - 1, y)) {
 					map[y][x - 1].setVisitable();
 				}
 				if (roomExists(x, y + 1)) {
 					map[y + 1][x].setVisitable();
 				}
 				if (roomExists(x, y - 1)) {
 					map[y - 1][x].setVisitable();
 				}
 			}
 
 		}
 	}
 
 	public boolean roomExists(int x, int y) {
 		return inBounds(y, x) && map[y][x] != null;
 	}
 
 	public boolean isGenerated() {
 		return generated;
 	}
 
 	public int getLevel() {
 		return this.level;
 	}
 
 	public void incLevel() {
 		this.level++;
 	}
 
 	public static boolean inBounds(int x, int y) {
 		return x >= 0 && y >= 0 && x < DUNGEON_DIMMENSION
 				&& y < DUNGEON_DIMMENSION;
 	}
 
 }
