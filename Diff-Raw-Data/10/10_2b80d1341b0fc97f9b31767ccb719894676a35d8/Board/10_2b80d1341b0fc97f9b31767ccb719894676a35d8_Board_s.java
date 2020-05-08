 package board;
 
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 
 import Cluedo.*;
 
 public class Board {
 	private int width = 25;
 	private int height = 25;
 	private Tile[][] board;
 	private Map<Player, Point> playerMap = new HashMap<Player, Point>();
 	private Set<Player> players = new HashSet<Player>();
 	private List<RoomTile> rooms= new ArrayList<RoomTile>();
 
 	private Map<String, Doorway> doorways = new HashMap<String, Doorway>();
 
 	
 	
 	/**
 	 * Creates a new board without players
 	 */
 	public Board(){
 		board = new Tile[height][width];
 	}
 
 	/**
 	 * Creates a new board and assigns the players to their starting position
 	 * 
 	 * @param p
 	 */
 	public Board(List<Player> p) {
 	
 		players.addAll(p);
 		
 		board = new Tile[height][width];
 		this.assignPlayers();
 	}
 
 	/**
 	 * returns the name of the room that the player is in or null if the player is in a hall way.
 	 * @param player
 	 * @return
 	 */
 	
 	public String getRoom(Player player) {
 		Point p = playerMap.get(player);
 		int x = p.x;
 		int y = p.y;
 		if(board[y][x] instanceof Doorway){
 			return  ((Doorway) board[y][x]).getRoom();
 		}
 		
 		return null;
 	}
 
 	/** 
 	 * prints out the board including player positions to the console 
 	 * 
 	 * 
 	 */
 	
 	public void printBoard(){
 		System.out.print(toString());
 	}
 	
 	
 	/**
 	 * returns true if the players location is one of the Rooms
 	 * @param p
 	 * @return
 	 */
 	public boolean isRoom(Point p){
 		int x = p.x;
 		int y = p.y;
 		if(board[y][x] instanceof Doorway){ 
 			return true; // can't have a doorway without being a room
 		}
 		return false;
 		
 	}
 	
 	
 
 	/**
 	 * returns true if players location is in one of the four Rooms at the corners of the house
 	 * @param p
 	 * @return
 	 */	
 	public boolean canUseSecretPassage(Player p){
 		int x = playerMap.get(p).x;
 		int y = playerMap.get(p).y;
 		if(!(board[y][x] instanceof Doorway)){
 			return false;
 		}
 		String name = board[y][x].getRoom();
 		if(name.equals("Spa") ||  name.equals("Guest House") || 
 		   name.equals("Conservatory") ||  name.equals("Kitchen")){
 			return true;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * moves the player into the room opposite to the one they are in now using the secret passage .
 	 * They must be in one of the four rooms on the corners of the Map
 	 * @param p
 	 */
 	
 	public void useSecretPassage(Player p){
 		
 		if(getRoom(p).equals("Spa")){
 			playerMap.put(p, new Point(20,20));
 		}else if(getRoom(p).equals("Guest House")){
 			playerMap.put(p, new Point(4,6));
 		}else if(getRoom(p).equals("Conservatory")){
 			playerMap.put(p, new Point(6,22));
 		}else if(getRoom(p).equals("Kitchen")){
 			playerMap.put(p, new Point(22,8));
 		}
 		
 		
 	}
 	
 	
 
 	/**
 	 * Moves player one step in the given direction returns true if possible, false if otherwise
 	 * @param p
 	 * @param direction
 	 * @return
 	 */
 
 	public boolean move(Player p, String direction){
 	
 		
 		Point current = playerMap.get(p);
 		
 		
 		int x= current.x;
 		int y= current.y;
 		
 		if(direction.equalsIgnoreCase("north")){
 			Point newposition = playerMap.get(p);
 			x =   newposition.x;
 			y =   newposition.y-1;
 		}else if(direction.equalsIgnoreCase("south")){
 			
 			Point newposition = playerMap.get(p);
 			x =   newposition.x;
 			y =   newposition.y+1;
 		}else if(direction.equalsIgnoreCase("east")){
 			
 			Point newposition = playerMap.get(p);
 			x =   newposition.x+1;
 			y =   newposition.y;
 		}else if(direction.equalsIgnoreCase("west")){
 			Point newposition = playerMap.get(p);
 			x =   newposition.x-1;
 			y =   newposition.y;
		} 
 		// watch out it  board[y][x] because the second array in the 2d array reads from left to right
 		if(x>=0 && y>= 0 && x<25 && y<25 && board[y][x].canMove()){
 				// handle move
 				playerMap.put(p, new Point(x ,y));
 				return true;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * moves player to location on board specified by params
 	 * 
 	 * @param p
 	 */
 	public void moveTo(Player p, int x, int y){
 		playerMap.put(p, new Point(x,y));
 	}
 	
 	
 	
 	/**
 	 * Iterates through current players and returns their positions
 	 * 
 	 */
 	public void printPlayerLocations(){
 		for (Map.Entry<Player, Point> e : playerMap.entrySet()){
 			String plyr = e.getKey().getName();
 			System.out.printf("%s is at location x: %d y: %d ", plyr, e.getValue().x, e.getValue().y);
 		}
 	}
 
 
 	/**
 	 * Moves the player and weapon to the room in the suggestion
 	 *
 	 * @param p
 	 * @param s
 	 */
 
 	public void evaluateSuggestions(Player p , Suggestion s){
 		// move
 		String name = s.getRoom().getName();
 		Doorway d = doorways.get(name);
 		Point point = new Point(d.x, d.y);
 		String accusedname = s.getCharacter().getName();
 		Player accused=null; 
 		// check if one of the players in the game is accused
 		for(Player pl : players){
 			if(pl.getName().equals(accusedname)){
 				accused = pl;		
 			}
 		}
 		// else create a new player
 		if(accused==null){
 			accused = new Player(accusedname);
 		}
 		playerMap.put(accused, point);
 
 		
 	}
 	
 
 	/**
 	 *  reads in the board from the 'board' file which is an ascii representation
 	 *  of the board. 
 	 *  	'#' is represents a wall
 	 *  	'd' represents a doorway 
 	 *   	' ' is a hallway tile
 	 * 
 	 */
 	
 	public void createBoard(){
 		this.createRooms();
 		
 		try{
 			// read in the board from board file
 			BufferedReader br = new BufferedReader(new FileReader("board.txt"));
 		
 			for(int i =0; i<25; i++){
 				// turns each line into an array
 				String line = br.readLine();
 				char[] temp = line.toCharArray();
 				// then hands over to the readBoardLine method which chops it up into
 				// an array of tiles and adds to the 2d array of tiles
 				board[i] = readBoardLine(temp, i);
 
 			}
 			
 			br.close();
 			//just to check if it works, will delete this	
 			System.out.println(this.toString());
 			
 		}catch (IOException e){
 			System.out.println(e.getStackTrace());
 		}
 	}
 	
 	/**
 	 * Builds a string of characters representing the board
 	 */
 		
 	@Override
 	public String toString(){
 		if(board==null){
 			return "null";
 		}else{
 			StringBuilder sb = new StringBuilder();
 				
 			for(int i = 0; i<board[0].length;i++){
 				for(int j = 0;j<board[i].length; j++){
 					if(playerIsAtPosition(j, i)!=null){
 						sb.append(this.getInitial(playerIsAtPosition(j, i)));
 					}
 					
 					else if(board[i][j] instanceof Wall){
 						sb.append('#');
 					}
 					else if(board[i][j] instanceof HallTile){
 						sb.append(' ');
 					}
 					else if(board[i][j] instanceof Doorway){
 						sb.append('d');
 					}else if(board[i][j] instanceof RoomTile){
 						sb.append('r');
 					}
 				}
 				sb.append('\n');
 			}	
 			return sb.toString();
 		}
 	}
 	
 	
 	
 	/**
 	 * Returns the player at specified position or null if
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	
 	private Player playerIsAtPosition(int x, int y){
 		for (Map.Entry<Player, Point> e : playerMap.entrySet()){
 			if(e.getValue().x==x && e.getValue().y==y){
 				return e.getKey();
 			}
 		}
 		return null;
 	}
 	
 	
 	
 	
 	/**
 	 * helper method that converts a char array into a an array of tiles which represent one row on the board
 	 * if the corresponding tile is a room tile the name of the room gets stored as a string inside the RoomTile object
 	 * 
 	 * 
 	 * 
 	 * @param info
 	 * @return
 	 */
 	
 	private Tile[] readBoardLine(char[] info, int row){
 		Tile[] tiles = new Tile[25];
 		int chartile;
 		
 	
 		for(int i = 0; i< info.length ;i++){		
 			chartile = info[i];
 			Tile d;
 			String name;
 			switch(chartile){
 				case '#': 
 					Tile t = new Wall();
 					tiles[i] = t;
 					break;
 				case ' ':
 					Tile h = new HallTile();
 					tiles[i]= h;
 					break;
 				case 'f':
 					Tile w = new Wall();
 					tiles[i] = w;
 					break;
 				case '1':
 					// create spa
 					name = "Spa";
 					d = new Doorway(name,this, rooms.get(0),i, row  );
 					doorways.put(name, (Doorway) d);
 					tiles[i] = d;
 					break;
 				case '2':
 					// create theatre
 					name = "Theatre";
 					d = new Doorway(name, this, rooms.get(1),i, row );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '3':
 					// create Living room
 					
 					name = "Living Room";
 					d = new Doorway( name, this, rooms.get(2), i, row );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '4':
 					// conservatory
 					name = "Conservatory";
 					d = new Doorway(name, this, rooms.get(3), i, row   );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '5':
 					//Patio
 					name = "Patio";
 					d = new Doorway(name, this, rooms.get(4),i, row  );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '6':
 					// Hall
 					name = "Hall";
 					d = new Doorway(name, this, rooms.get(5),i, row  );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '7':
 					// Kitchen
 					name = "Kitchen";
 					d = new Doorway( name, this, rooms.get(6) ,i, row  );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '8':
 					// Dining room
 					name = "Dining Room";
 					d = new Doorway(name, this, rooms.get(7),i, row  );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case '9':
 					// Guest House
 					name = "Guest House";
 					d = new Doorway( name, this, rooms.get(8) ,i, row  );
 					tiles[i] = d;
 					doorways.put(name, (Doorway) d);
 					break;
 				case 'd':
 					// pool
 					name = "Pool";
 					d = new Doorway(name, this, rooms.get(9), i, row) ;
 					tiles[i] = d; // assign to board
 					doorways.put(name, (Doorway) d) ;
 					
 					break;
 					
 				case 'r':
 					// spa
 					d = new RoomTile();
 					tiles[i] = d;
 					break;
 								
 				default:
 					break;
 			}
 		}
 		return tiles;
 	}
 
 	
 	
 	
 	/**
 	 * creates all the rooms without weapons
 	 * 
 	 * 
 	 */
 	private void createRooms(){
 		
 		String[] roomNames = { "Spa", "Theatre", "Living Room", "Conservatory", "Patio", "Hall", "Kitchen", "Dining Room", "Guest House", "Pool" };
 		
 	
 		for (int i = 0; i< roomNames.length; i++){
 			RoomTile r = new RoomTile( roomNames[i], null);
 			rooms.add(r);
 		}
 	}
 
 	/**
 	 *   puts players at their start locations
 	 * 
 	 */
 
 	private void assignPlayers(){
 		Point location;
 		for(Player p : players){
 			if(p.getName().equals("Kasandra Scarlett")){
 				location = new Point(18, 24);
 				playerMap.put(p, location);
 			}
 			if(p.getName().equals("Jack Mustard")){
 				location = new Point(7,24);
 				playerMap.put(p, location);
 			}
 			if(p.getName().equals("Diane White")){
 				location = new Point(0,19);
 				playerMap.put(p, location);
 			}
 			if(p.getName().equals("Jacob Green")){
 				location = new Point(0, 9);
 				playerMap.put(p, location);
 			}
 			if(p.getName().equals("Eleanor Peacock")){
 				location = new Point(6, 0);
 				playerMap.put(p, location);
 			}
 			if(p.getName().equals("Victor Plum")){
 				location = new Point(20, 0);
 				playerMap.put(p, location);
 			}
 		}
 	}
 	
 	
 	private char getInitial(Player p){
 		String[] i = p.getName().split(" ");
 		
 		
 		return i[1].charAt(0);
 	}
 
 
 	public static void main(String args[]) throws IOException{
 	
 		List<Player> players = new ArrayList<Player>();
 		
 		Player playa = new Player("Jack Mustard");
 		players.add(playa);
 		Board b = new Board(players);
 		b.createBoard();
 		for(int i = 0; i<2; i++){
 			b.move(playa, "north");
 		}
 		b.move(playa, "west");
 	//	b.move(playa, "west");
 		b.printPlayerLocations();
 		System.out.println(b.toString());
 		System.out.println(b.getRoom(playa));
 		b.move(playa, "east");
 		System.out.println(b.toString());
 		
 		
 		
 
 	}
 
 
 }
