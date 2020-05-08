 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 
 public class Dungeon {
 	
 	ArrayList<Room> rooms;
 	Room entryRoom;
 	Room exitRoom;
 	
 	public Dungeon(){
 		rooms = new ArrayList<Room>();
 		entryRoom = null;
 		exitRoom = null;
 	}
 	public Dungeon(ArrayList<Room> rooms){
 		this.rooms = rooms;
 		entryRoom = exitRoom = null;
 	}
 	public Dungeon(String filename){
 		rooms = new ArrayList<Room>();
 		//sets entry and exit as well
 		rooms = dungeonFile(filename);
 	}
 
 	public void setEntryRoom(Room entry){
 		entryRoom = entry;
 	}
 	
 	public void setExitRoom(Room exit){
 		exitRoom = exit;
 	}
 	
 	public void addRoom(Room toAdd){
 		rooms.add(toAdd);
 	}
 	
 	public Room getRoom(String name){
 		int index = 0;
 		assert((index = findRoom(name)) >= 0);
 		return rooms.get(index);
 		
 	}
 	public int findRoom(String name){
 		for(Room r:rooms){
 			if(r.getName().equalsIgnoreCase(name))
 				return rooms.indexOf(r);
 		}
 		return -1;
 	}
 	public Room getEntryRoom(){
 		
 		assert(entryRoom != null);
 		return entryRoom;
 		
 	}
 	public Room getExitRoom(){
 		assert(exitRoom != null);
 		return exitRoom;
 	}
 	
 	
 	public ArrayList<Room> dungeonFile(String filename){
 		int i, numRooms, xSize, ySize;
 		String name, desc;
 		
 		ArrayList<Room> ret = new ArrayList<Room>();
 		Scanner scan = Util.readFile(filename);
 		numRooms = scan.nextInt();
		
 		for(i = 0; i < numRooms; i++){
			scan.nextLine();
 			name = scan.nextLine();
 			xSize = scan.nextInt();
 			ySize = scan.nextInt();
 			Room currentRoom = new Room(xSize, ySize, name);
 			desc = readDescription(scan);
 			currentRoom.setDescription(desc);
 			/*need to read the items and store them somewhere
 			 * this call gets through that part of the file*/
 			readItems(scan);
 			currentRoom.setConnectingRooms(readRooms(scan));
 			rooms.add(currentRoom);
 		}
 		
 		entryRoom = rooms.get(findRoom(scan.nextLine()));
 		exitRoom = rooms.get(findRoom(scan.nextLine()));
 		
 		return ret;
 	}
 	private String readDescription(Scanner scan){
 		StringBuilder str = new StringBuilder();
 		String check = "";
 		boolean done = false;
 		while(!done){
 		check = scan.nextLine();
 		if(check.contains("#") == false)
 			str.append(check);
 		else
 			done = true;
 		}
 		return str.toString();
 		
 	}
 	private ArrayList<Item> readItems(Scanner scan){
 		ArrayList<Item> item = new ArrayList<Item>();
 		String check;
 		boolean done = false;
 		while(!done){
 			check = scan.nextLine();
 			if(check.contains("#") == false){
 				//item.add(new UserItem(check));
 			}
 			else
 				done = true;
 		}			
 		return item;
 	}
 	
 	private HashMap<Direction, Room> readRooms(Scanner scan){
 		String check = "";
 		HashMap<Direction, Room> map = new HashMap<Direction, Room>();
 		Direction dir;
 		Room room;
 		boolean done = false;
 		while(!done){
 			check = scan.nextLine();
 			if(check.contains("#") == false){
 				dir = findDirection(check);
 				int index = findRoom(scan.nextLine());
 				if(index > 0)
 				{
 					room = rooms.get(index);
 					map.put(dir, room);
 				}
 			}
 			else
 				done = true;
 		}			
 		return map;
 		
 	}
 	private Direction findDirection(String str){
 		
 		if(str.toUpperCase().equals("NORTH"))
 			return Direction.NORTH;
 		if(str.toUpperCase().equals("SOUTH"))
 			return Direction.SOUTH;
 		if(str.toUpperCase().equals("EAST"))
 			return Direction.EAST;
 		if(str.toUpperCase().equals("WEST"))
 			return Direction.WEST;
 		else
 			return null;
 		
 	}
 	
 }
