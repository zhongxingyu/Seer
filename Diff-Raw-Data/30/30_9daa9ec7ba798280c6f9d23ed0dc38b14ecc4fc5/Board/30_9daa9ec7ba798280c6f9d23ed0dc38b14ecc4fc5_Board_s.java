 package board;
 
 import java.util.HashSet;
 
 import piece.Weapon;
 
 import game.Player;
 /**
  * So well, this is going to where the gameloop is,
  * I will first initialize the board, each players hands, 
  * the 'killer' and, the players start positions (guess I 
  * I should probably make that random?)
  * 
  * Also this is where I'll have the 2D array for the board,
  * the only things I'll store on the board is if it'study part of the 
  * hallway or an entrance way, if it'study an entrance way I'll sort
  * out which room it'study trying to get into... somehow.
  * @author Potato
  *
  */
 public class Board {
 	
 	private HashSet<Player> players;
 	private HashSet<Room> rooms;
 	private HashSet<Weapon> weapons;
 	private BoardTile[][] board;
 	private boolean hasKillerBeenFound;
 	private BoardTile w; //for a wall on the board/unreachable place
 	private BoardTile hw; //a hallway on the board
 	private BoardTile st;
 	
 
 	public Board(HashSet<Player> players){
 		rooms = new HashSet<Room>(9);
 		String k = "K"; //Kitchen
 		String dr = "D"; //DiningRoom
 		String l = "l"; //Lounge
 		String br = "B"; //BallRoom
 		String c = "C"; //Conservatory
 		String b = "B"; //BilliardRoom
 		String li = "L" ;//Library
 		String s = "S"; //Study
 		String h = "H"; //Hall
 		String w = "W"; //Wall
 		String r = "-"; //Room
 		String hw = " "; //Hall Way
 		String st = "S"; //Start Point
 		
 		//Only place a player can move on is the 'hallway' or an 'entrance'

 		Room kitchen = new Room("Kitchen", players.size(), null);
 		Room diningRoom = new Room("Kitchen", players.size(), null);
 		Room lounge = new Room("Lounge", players.size(), null);
 		Room ballRoom = new Room("Ball Room", players.size(), null);
 		Room conservatory = new Room("Conservatory", players.size(), null);
 		Room billiardRoom = new Room("Billiard Room", players.size(), null);
 		Room library = new Room("Library", players.size(), null);
 		Room study = new Room("Study", players.size(), null);
 		Room hall = new Room("Hall", players.size(), null);
 
 		rooms.add(kitchen);
 		rooms.add(diningRoom);
 		rooms.add(lounge);
 		rooms.add(ballRoom);
 		rooms.add(conservatory);
 		rooms.add(billiardRoom);
 		rooms.add(library);
 		rooms.add(study);
 		rooms.add(hall);

 		
 		String[][] board = {
 				{ w, w, w, w, w, w, w, w, w,st, w, w, w,st, w, w, w, w, w, w, w, w, w},
				{ w, r, r, r, r, w, w,hw,hw,hw, w, r, w,hw,hw,hw, w, w, c, c, c, c, w},
				{ w, r, r, r, r, w,hw,hw, w, w, w, r, w, w, w,hw,hw, w, c, c, c, c, w},
				{ w, r, r, r, r, w,hw,hw, w, r, r, r, r, r, w,hw,hw, c, c, c, c, c, w},
 				{ w, r, r, r, r, w,hw,hw,br, r, r, r, r, r,br,hw,hw,hw, w, w, w, w, w},
				{ w, w, w, w, r, w,hw,hw, w, r, r, r, r, r, w,hw,hw,hw,hw,hw,hw,hw,st},
				{ w,hw,hw,hw,hw,hw,hw,hw, w,br, w, w, w, w,br,hw,hw,hw,hw,hw,hw,hw, w},
 				{ w,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw, w, w, w, w, w, w},
 				{ w, w, w, w, w,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw, b, r, r, r, r, w},
 				{ w, r, r, r, w, w, w, w,hw,hw, w, w, w, w,hw,hw,hw, w, r, r, r, r, w},
 				{ w, r, r, r, r, r, r, w,hw,hw, w, w, w, w,hw,hw,hw, w, r, r, r, r, w},
 				{ w, r, r, r, r, r, r,dr,hw,hw, w, w, w, w,hw,hw,hw, w, w, w, w, b, w},
 				{ w, r, r, r, r, r, r, w,hw,hw, w, w, w, w,hw,hw,hw,hw,hw,hw,hw,hw, w},
 				{ w, r, r, r, r, r, r, w,hw,hw, w, w, w, w,hw,hw,hw, w, w,li, w, w, w},
 				{ w, w, w, w, w, w,dr, w,hw,hw, w, w, w, w,hw,hw, w, w, r, r, r, r, w},
 				{ w,hw,hw,hw,hw,hw,hw,hw,hw,hw, w, w, w, w,hw,hw,li, r, r, r, r, r, w},
 				{st,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw, w, w, w, r, r, r, w},
 				{ w,hw,hw,hw,hw,hw,hw,hw,hw, w, w, h, h, w, w,hw,hw,hw, w, w, w, w, w},
				{ w, w, w, w, w, w, l,hw,hw, w, r, r, r, r, w,hw,hw,hw,hw,hw,hw,hw,st},
				{ w, r, r, r, r, r, w,hw,hw, w, r, r, r, r, h,hw,hw,hw,hw,hw,hw,hw, w},
				{ w, r ,r, r, r, r, w,hw,hw, w, r, r, r, r, w,hw,hw, s, s, s, s, s, s},
				{ w, r ,r, r, r, r, w,hw,hw, w, r, r, r, r, w,hw,hw, s, s, s, s, s, s},
				{ w, r ,r, r, r, r, w,hw,hw, w, r, r, r, r, w,hw,hw, s, s, s, s, s, s},
				{ w, w, w, w, w, w, w,st, w, w, w, w, w, w, w, w, w, w, s, s, s, s, s}
 				};
 		
 
 for(int i = 0; i < board.length; i++){
 	for(int j = 0; j < board[i].length; j++){
 		System.out.printf("%s ",board[i][j]);
 	}
 	System.out.println();
 }
 
 		//add rooms to deck
 		//add characters to deck
 		//shuffle deck
 		
 		
 
 		
 		hasKillerBeenFound = false;
 	}
 
 	public HashSet<Player> getPlayers() {
 		return players;
 	}
 	
 	public void gameLoop(){
 		int turnSize = players.size();
 		while(1 == 1){
 			for(int i = 1; i < turnSize + 1; i++){
 				
 			}
 		}
 	}
 }
