 package editor.model;
 
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 
 import model.Room;
 import model.object.Item;
 import model.object.Monster;
 import model.object.Player;
 import start.GameDriver;
 import view.FirstPersonItem;
 import view.FirstPersonMonster;
 import view.FirstPersonRoom;
 import xml.XMLWriter;
 import editor.controller.EditorUpdateObject;
 
 /**
  * LevelEditor models the state of the LevelEditor
  * @author sean
  *
  */
 public class LevelEditor extends Observable implements Observer {
 
 	private HashMap<String, FirstPersonRoom> rooms;
 	private HashMap<String, Monster> monsters;
 	private HashMap<String, Item> items;
 	private String roomsArray[][];
 	private Player player;
 
 	private int maxX;
 	private int maxY;
 
 	private int x;
 	private int y;
 
 	private static final String SOUTH = "south";
 	private static final String EAST = "east";
 	private static final String WEST = "west";
 	private static final String NORTH = "north";
 
 	private static final String REMOVE_ITEM = "removeItem";
 	private static final String ADD_ITEM = "addItem";
 	private static final String REMOVE_MONSTER = "removeMonster";
 	private static final String ADD_MONSTER = "addMonster";
 	private static final String REMOVE_EXIT = "removeExit";
 	private static final String ADD_EXIT = "addExit";
 	private static final String REMOVE_ROOM = "removeRoom";
 	private static final String ADD_ROOM = "addRoom";
 	private static final String LOOK = "look";
 	private static final String SAVE = "save";
 	private static final String PLAY = "play";
 
 	public LevelEditor(int maxX, int maxY) {
 		rooms = new HashMap<String, FirstPersonRoom>();
 		monsters = new HashMap<String, Monster>();
 		items = new HashMap<String, Item>();
 		roomsArray = new String[maxX][maxY];
 		player = new Player();
 
 		// Initialize the size of the game
 		this.maxX = maxX;
 		this.maxY = maxY;
 
 		// Initialize the position to (0,0)
 		x = 0;
 		y = 0;
 
 		// Initialize the whole roomsArray to null (no rooms to begin with)
 		for (int i = 0; i < maxX; i++) {
 			for (int j = 0; j < maxY; j++) {
 				roomsArray[i][j] = null;
 			}
 		}
 
 		// Initialize the monsters
 		Monster kracken = new FirstPersonMonster("Kracken", 10, "Kracken.png");
 		monsters.put("Kracken", kracken);
 		Monster grendel = new FirstPersonMonster("Grendel", 8, "Grendle.png");
 		monsters.put("Grendel", grendel);
 		Monster goblin = new FirstPersonMonster("Goblin", 3, "TrollBig.png");
 		monsters.put("Goblin", goblin);
 
 		// Create the items
 		Item plant = new FirstPersonItem("Plant", 2.0, "Plant1.png");
 		items.put("Plant", plant);
 		Item sword = new FirstPersonItem("Sword", 7.0, "Sword1.png");
 		items.put("Sword", sword);
 		Item pogoStick = new FirstPersonItem("PogoStix", 5.0, "PogoStick1.png");
 		items.put("PogoStix", pogoStick);
 
 		update();
 	}
 
 	public void addRoom(FirstPersonRoom room) {
 		rooms.put(room.getDescription(), room);
 		roomsArray[x][y] = room.getDescription();
 		update();
 	}
 
 	public void removeRoom() {
 		// Remove any exits first
 		removeExit(NORTH);
 		removeExit(SOUTH);
 		removeExit(EAST);
 		removeExit(WEST);
 
 		rooms.remove(roomsArray[x][y]);
 		roomsArray[x][y] = null;
 		update();
 	}
 
 	public void addExit(String direction) {
 		// Check that the specified room exist
 		if (roomsArray[x][y] == null) {
 			return;
 		}
 
 		Room currentRoom = rooms.get(roomsArray[x][y]);
 		Room neighbor;
 
 		boolean north = direction.equals(NORTH);
 		boolean south = direction.equals(SOUTH);
 		boolean west = direction.equals(WEST);
 		boolean east = direction.equals(EAST);
 
 		if (north) {
 			if (!checkXY(x - 1, y)) {
 				return;
 			}
 
 			// Check that the specified rooms exist
 			if (roomsArray[x - 1][y] == null) {
 				return;
 			}
 			neighbor = rooms.get(roomsArray[x - 1][y]);
 			currentRoom.setExits(NORTH, neighbor);
 			neighbor.setExits(SOUTH, currentRoom);
 
 		} else if (south) {
 			if (!checkXY(x + 1, y)) {
 				return;
 			}
 
 			// Check that the specified rooms exist
 			if (roomsArray[x + 1][y] == null) {
 				return;
 			}
 			neighbor = rooms.get(roomsArray[x + 1][y]);
 
 			currentRoom.setExits(SOUTH, neighbor);
 			neighbor.setExits(NORTH, currentRoom);
 
 		} else if (west) {
 			if (!checkXY(x, y - 1)) {
 				return;
 			}
 
 			// Check that the specified rooms exist
 			if (roomsArray[x][y - 1] == null) {
 				return;
 			}
 			neighbor = rooms.get(roomsArray[x][y - 1]);
 
 			currentRoom.setExits(WEST, neighbor);
 			neighbor.setExits(EAST, currentRoom);
 
 		} else if (east) {
 			if (!checkXY(x, y + 1)) {
 				return;
 			}
 
 			// Check that the specified rooms exist
 			if (roomsArray[x][y + 1] == null) {
 				return;
 			}
 			neighbor = rooms.get(roomsArray[x][y + 1]);
 
 			currentRoom.setExits(EAST, neighbor);
 			neighbor.setExits(WEST, currentRoom);
 		}
 		update();
 	}
 
 	public void removeExit(String direction) {
 		if (direction == null || direction.equals("null")) {
 			return;
 		}
 
 		// Check that the specified rooms exist
 		if (roomsArray[x][y] == null
 				|| rooms.get(roomsArray[x][y]).getExit(direction) == null) {
 			return;
 		}
 
 		Room currentRoom = rooms.get(roomsArray[x][y]);
 		Room neighbor = currentRoom.getExit(direction);
 
 		boolean north = direction.equals(NORTH);
 		boolean south = direction.equals(SOUTH);
 		boolean west = direction.equals(WEST);
 		boolean east = direction.equals(EAST);
 
 		if (north) {
 			currentRoom.removeExit(NORTH);
 			neighbor.removeExit(SOUTH);
 		} else if (south) {
 			currentRoom.removeExit(SOUTH);
 			neighbor.removeExit(NORTH);
 		} else if (west) {
 			currentRoom.removeExit(WEST);
 			neighbor.removeExit(EAST);
 		} else if (east) {
 			currentRoom.removeExit(EAST);
 			neighbor.removeExit(WEST);
 		}
 
 		update();
 	}
 
 	public void addMonster(String monsterName) {
 		Room room = rooms.get(roomsArray[x][y]);
 		Monster monster = monsters.get(monsterName);
 		if (room != null && monster != null) {
 			room.addMonster(monster, player.getLookingDirection());
 		}
 		update();
 	}
 
 	public void removeMonster() {
 		Room room = rooms.get(roomsArray[x][y]);
 		if (room != null) {
 			room.removeMonsterByDirection(player.getLookingDirection());
 		}
 		update();
 	}
 
 	public void addItem(String itemName) {
 		Room room = rooms.get(roomsArray[x][y]);
 		Item item = items.get(itemName);
 		if (room != null && item != null) {
 			room.addItem(item, player.getLookingDirection());
 		}
 		update();
 	}
 
 	public void removeItem() {
 		Room room = rooms.get(roomsArray[x][y]);
 		if (room != null) {
 			room.removeItemByDirection(player.getLookingDirection());
 		}
 		update();
 	}
 
 	public void look(String lookingDirection) {
 		player.setLookingDirection(lookingDirection);
 	}
 
 	private boolean checkXY(int x, int y) {
 		return x < maxX && y < maxY && x >= 0 && y >= 0;
 	}
 
 	private void update() {
 		setChanged();
 		notifyObservers(new EditorUpdateObject(roomsArray, rooms, x, y, player));
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 instanceof Point) {
 			Point point = (Point) arg1;
 			int tempX = (int) point.getX();
 			int tempY = (int) point.getY();
 
 			if (tempX >= 0 && tempX < maxX && tempY >= 0 && tempY < maxY) {
 				x = tempX;
 				y = tempY;
 			}
 
 			update();
 		}
 
 		if (arg1 instanceof String) {
 			String source = (String) arg1;
 			String[] temp = source.split(",");
 
 			if (temp[0].equals(ADD_ROOM)) {
 				String name = JOptionPane
 						.showInputDialog("Please enter a name for the room:");
 				if (name != null && !name.equals("") && !name.equals("null")
 						&& !name.equals(" ")) {
 					FirstPersonRoom room = new FirstPersonRoom(name);
 					addRoom(room);
 				}
 			} else if (temp[0].equals(REMOVE_ROOM)) {
 				removeRoom();
 			} else if (temp[0].equals(REMOVE_MONSTER)) {
 				removeMonster();
 			} else if (temp[0].equals(REMOVE_ITEM)) {
 				removeItem();
 			} else if (temp[0].equals(ADD_EXIT)) {
 				addExit(temp[1]);
 			} else if (temp[0].equals(REMOVE_EXIT)) {
 				removeExit(temp[1]);
 			} else if (temp[0].equals(ADD_MONSTER)) {
 				addMonster(temp[1]);
 			} else if (temp[0].equals(ADD_ITEM)) {
 				addItem(temp[1]);
 			} else if (temp[0].equals(LOOK)) {
 				look(temp[1]);
 			} else if (temp[0].equals(SAVE)) {
 				save(temp[1], temp[2]);
 			} else if (temp[0].equals(PLAY)) {
 				play(temp[1]);
 			}
 
 			update();
 		}
 	}
 
 	private void play(String room) {
 
 		if (rooms.get(room) == null) {
 			JOptionPane.showMessageDialog(null,
 					"Error: Please enter a valid name of an exisiting room");
 			return;
 		}
 
 		Player player = new Player();
 		player.setCurrentRoom(rooms.get(room));
 
 		GameDriver driver = new GameDriver();
 		driver.startGame(player, rooms);
 
 	}
 
 	public Set<String> getItems() {
 		return items.keySet();
 	}
 
 	public Set<String> getMonsters() {
 		return monsters.keySet();
 	}
 
 	private void save(String startingRoom, String name) {
 
 		if (rooms.get(startingRoom) == null) {
 			JOptionPane.showMessageDialog(null,
 					"Error: Please enter a valid name of an exisiting room");
 			return;
 		}
 
 		if (name == null || name.equals("")) {
 			JOptionPane.showMessageDialog(null,
 					"Error: Please enter a valid name for the level");
 			return;
 		}
 
		XMLWriter writer = new XMLWriter(rooms, name, startingRoom);
 		
 		JOptionPane.showMessageDialog(null,
 				"Saved successfully!");
 	}
 
 }
