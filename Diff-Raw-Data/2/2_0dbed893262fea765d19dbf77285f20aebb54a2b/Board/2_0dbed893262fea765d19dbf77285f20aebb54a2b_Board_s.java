 package player;
 
 import java.awt.Color;
 import java.awt.Point;
 
 import java.util.List;
 import java.util.Scanner;
 import java.util.concurrent.Semaphore;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import util.Graph;
 
 import main.QBoard;
 import ai.AI;
 
 public class Board {
 	public final static Color BUTTON_DEFAULT_COLOR = new Color(220,220,220);
 	public static final Color WALL_COLOR = Color.black;  //This will eventually be switched to brown
 
 	GameState currentState;		// holds the state of the game in a single variable.
 	private QBoard gui;			// allows board to communicate with the gui
 	private AI ai;				// allows board to communicate with the ai
 	public Semaphore sem;		// used to tell the ai when it's turn is, needs a better name
 	public JFrame winFrame;		// holds the message when a player wins the game
 	private boolean netPlay; 	// If you are playing on the net.
 	private String moveForNetwork;		//
 	private boolean networkMadeLastMove;
 	public Semaphore moveMadeForNetwork;
 
 
 	//TODO: Create a single initialize method that handles all of the stuff that is in all of the constructors.
 	// default constructor, assumes 2 players all using their default colors
 	// will probably only be used for testing
 	public Board() {
 		int pl = 2;
 		Player[] players = new Player[pl];
 		for (int i = 0; i < pl; i++) {
 			players[i] = new Player(i, 10, Player.color[i], 0);
 		}
 		initialize(players);
 	}
 
 	// constructor that will make a 2 player game where player 0 is using the gui, and player 1 is an ai opponent
 	public Board(boolean usingAI) {
 		Player[] players = new Player[2];
 		players[0] = new Player(0, 10, Color.blue, Player.GUI_PLAYER);
 		if (usingAI) {
 			players[1] = new Player(1, 10, Color.red, Player.AI_PLAYER);	
 		}
 		else {
 			players[1] = new Player(1, 10, Color.red, Player.GUI_PLAYER);
 		}
 		initialize(players);
 	}
 
 	// this is the constructor that will be used most often
 	public Board(int numOfPlayers, Color[] colArray, int[] playerTypes) {
 		Player[] players = new Player[numOfPlayers];
 		int pl = players.length;
 		for (int i = 0; i < pl; i++) {
 			players[i] = new Player(i, 20/pl, colArray[i], playerTypes[i]);
 		}
 		initialize(players);	
 	}
 
 	//Using for network Play, Designed for Move Server
 	public Board(int numOfPlayers, int playerNumber) {
 		Player[] players = new Player[numOfPlayers];
 		int pl = players.length;
 		for (int i = 0; i < pl; i++) {
 			if(playerNumber == i){
 				players[i] = new Player(i, 20/pl, Player.color[i], Player.AI_PLAYER);
 			}else 
 				players[i] = new Player(i, 20/pl, Player.color[i], Player.NET_PLAYER);
 		}
 		netPlay = true;
 
 		initialize(players);	
 	}
 
 	private void initialize(Player[] pls) {
 		int[][] walls = new int[8][8];
 		currentState = new GameState(walls, pls,0, initializeGraph());
 		initializeAIIfNeeded();
 		newGUI();
 		if(netPlay){
 			return;
 		}else{
 			requestMove();
 		}
 	}
 
 	/**
 	 * Initializes an AI if there are any Players marked as being an AI_PLAYER.
 	 */
 	private void initializeAIIfNeeded() {
 		Player[] players = currentState.getPlayerArray();
 		boolean isNeeded = false;
 		for (int i = 0; i < players.length; i++) {
 			if (players[i].getPlayerType() == Player.AI_PLAYER) {
 				isNeeded = true;
 			}
 		}
 		if (isNeeded) {
 			sem = new Semaphore(0);
 			ai = new AI(this, sem);
 			ai.start();
 		}
 
 	}
 
 	// creates a graph containing 81 nodes, each representing a space on the board, and add edges between
 	// nodes representing spaces directly adjacent to each other
 	private Graph initializeGraph() {
 		Graph graph = new Graph<Point>();
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				graph.addNode(new Point(i,j));
 			}
 		}
 
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 9; j++) {
 				graph.addEdge(new Point(i, j), new Point(i + 1, j));
 			}
 		}
 
 
 
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 9; j++) {
 				graph.addEdge(new Point(j, i), new Point(j, i + 1));
 			}
 		}
 		return graph;
 	}
 
 	/**
 	 * Returns true if the specified Player has any walls left.  Otherwise, it returns false.
 	 * @param player
 	 * 		An int representing which Player we're interested in.
 	 * @return
 	 * 		true if the Player has walls left, false otherwise.
 	 */
 	public boolean hasWalls(int player) {
 		return currentState.hasWalls(player);
 	}
 
 	//TODO:  Keep/Update this method when needed.
 	/**
 	 * Returns the number of walls the specified Player has left.
 	 * @param player
 	 * 		This is the number of the Player we're interested in.
 	 * @return
 	 * 		the number of walls the Player has left.
 	 */
 	public int numberOfWalls(int player){
 		return currentState.numberOfWalls(player);
 	}
 
 	/**
 	 * This method returns a GameState Object which contains a copy of all the necessary variables in the game.
 	 * @return
 	 * 		Returns a GameState Object representing the game's current State.
 	 */
 	public GameState getCurrentState() {
 		return currentState;
 		//return new GameState(walls, players, turn, graph);
 	}
 
 
 	//TODO: Update to make the returned String have "MOVE?" at the beginning of it.
 
 	/**
 	 * This method will convert a String containing a move in the format we've been using in our gui into one 
 	 * matching the format that will be used when we send moves over a network.
 	 * 
 	 * The format of a "gui String"  is: <op> X Y
 	 * Where op is either M,V, or H depending on what type of move it is, and X and Y are coordinates.
 	 * 
 	 * The format of the "net string" is: <op> (Y1, X1) (Y2, X2)
 	 * Where op is either M for piece being moved, or W for a wall being placed.
 	 * 
 	 * If op is M, then:
 	 * 		Y1 and X1 are the coordinates of the current player.
 	 * 		Y2 and X2 represent the coordinates of the location the player wants to move to.
 	 * 
 	 * If op is W, then:
 	 * 		Y1 and X1 represent the location of either the top or left edge of the wall being placed.
 	 * 		Y2 and X2 represent the location of either the bottom or right edge of the wall.
 	 * 
 	 * One major difference between these two formats is that converting between the two switches the x and y axis of the board.
 	 * 
 	 * @param guiString
 	 * 		This is a String representing a move in the "gui String" format.
 	 * 
 	 * @return
 	 * 		This returns a String representing a move in the "net String" format.
 	 */
 	public String convertGUIStringToNetString(String guiString) {
 		Player[] players = currentState.getPlayerArray();
 		Scanner sc = new Scanner(guiString);
 		String firstChar = sc.next();
 		String netString = "";
 		int x = Integer.parseInt(sc.next());
 		int y = Integer.parseInt(sc.next());
 
 		if (firstChar.charAt(0) == 'M') {
 			netString += ("MOVED M (" + players[currentState.getTurn()].getY() + ", " + players[currentState.getTurn()].getX() + ")");
 			netString += " (" + y + ", " + x + ")";
 		}
 
 		if (firstChar.charAt(0) == 'V') {
 			netString += ("MOVED W (" + y + ", " + (x+1) + ")");
 			netString += " (" + (y+2) + ", " + (x+1) + ")";
 		}
 
 		if (firstChar.charAt(0) == 'H') {
 			netString += ("MOVED W (" + (y+1) + ", " + x + ")");
 			netString += " (" + (y+1) + ", " + (x+2) + ")";
 		}
 
 		return netString;
 	}
 
 	//TODO: Make it read Strings that start with "MOVED" or "MOVE?"
 	/**
 	 * This method converts a "Net String" to a "GUI String" so that it can be processed correctly.
 	 * 
 	 * The format of a "gui String"  is: <op> X Y
 	 * Where op is either M,V, or H depending on what type of move it is, and X and Y are coordinates.
 	 * 
 	 * The format of the "net string" is: MOVED <op> (Y1, X1) (Y2, X2) or MOVE <op> (Y1, X1) (Y2, X2)
 	 * Where op is either M for piece being moved, or W for a wall being placed.
 	 * 
 	 * If op is M, then:
 	 * 		Y1 and X1 are the coordinates of the current player.
 	 * 		Y2 and X2 represent the coordinates of the location the player wants to move to.
 	 * 
 	 * If op is W, then:
 	 * 		Y1 and X1 represent the location of either the top or left edge of the wall being placed.
 	 * 		Y2 and X2 represent the location of either the bottom or right edge of the wall.
 	 * 
 	 * One major difference between these two formats is that converting between the two switches the x and y axis of the board.
 	 * 
 	 * @param netStr
 	 * 		This is a String containing a move in the "net String" format.
 	 *
 	 * @return
 	 * 		This returns A String containing the same move in the "GUI String" format.
 	 */
 	public String convertNetStringToGUIString(String netStr) {
 		System.out.println("Recieve string to translate : " + netStr);
 		String netString = removePunctuation(netStr);
 		Scanner sc = new Scanner(netString);
 
 		//sc.next();						//skips MOVE/MOVED since we already know it's a move of some sort.
 		String firstChar = sc.next();
 
 		String GUIString = "";
 
 		//needed to determine if a wall is horizontal or vertical
 		sc.next();
 		int x2 = Integer.parseInt(sc.next());
 
 		int y = Integer.parseInt(sc.next());
 		int x = Integer.parseInt(sc.next());
 
 		if (firstChar.charAt(0) == 'M') {
 			GUIString = "M " + x + " " + y;
 		}
 
 		if (firstChar.charAt(0) == 'W') {
 			//if it's a vertical wall
 			if (x == x2)
 				GUIString = "V " + (x-1) + " " + (y-2);  
 			//otherwise it must be horizontal
 			else
 				GUIString = "H " + (x-2) + " " + (y-1);
 		}
 
 		return GUIString;
 	}
 
 	/**
 	 * This method takes a String and replaces all of the parentheses and commas with spaces.
 	 * 
 	 * @param str
 	 * 		This is the String we want to parentheses and commas from.
 	 * @return
 	 * 		Returns a String similar to the original String, but with all the paretheses and commas replaced with spaces.
 	 */
 	private String removePunctuation(String oldStr) {
 		String newStr = oldStr;
 		newStr = newStr.replace('(', ' ');
 		newStr = newStr.replace(')', ' ');
 		newStr = newStr.replace(',', ' ');
 		newStr = newStr.replace('<', ' ');
 		newStr = newStr.replace('>', ' ');
 		return newStr;
 	}
 
 	/**
 	 * Method which takes in a GUIString representing a move and determines whether or not it is legal.
 	 * 
 	 * @param input
 	 * 		This is the String representing the move.
 	 * @return
 	 * 		true if the move is legal, false if the move is illegal.
 	 */
 	public boolean isStringLegal(String input) {
 		return getCurrentState().isStringLegal(input);
 	}
 
 	public int[][] getWallArray() {
 		return currentState.getWalls();
 	}
 
 	/**
 	 * Returns the number associated with the Player whose turn it is.
 	 * @return
 	 * 		an int representing which Player's turn it is.
 	 */
 	public int getTurn() {
 		return currentState.getTurn();
 	}
 
 
 	/**
 	 * Returns an int representing the current Player's type.
 	 * @return
 	 * 		an int representing a Player type.
 	 */
 	public int getCurrentPlayerType() {
 		return currentState.getCurrentPlayerType();
 	}
 
 	// makes a new board appear on screen and sets default locations of the players 
 	public void newGUI() {
 		Player players[] = currentState.getPlayerArray();
 		gui = new QBoard(this);
 		for (int i = 0; i < players.length; i++) {
 			gui.setColorOfSpace(players[i].getLocation(), players[i].getColor());
 		}
 	}
 
 	/**
 	 * Reads in a String representing a move and makes it.  Don't call this without calling the isStringLegal method
 	 * first.
 	 * @param input
 	 * 		This is a GUI String representing a move.
 	 */
 	public void readStringFromGUI(String input) {
 		Player[] players = currentState.getPlayerArray();
 		showMoves(players[getTurn()],false);
 		System.out.println("before if netplay, input is :" + input);
 		if (netPlay){
 			System.out.println("before if !networkmadelasemove");
 
 			if(!networkMadeLastMove){
 				System.out.println("after if !networkmadelastmove");
 
 				moveForNetwork = input;
 				moveMadeForNetwork.release();
 				return;
 			}}
 
 		//makes the actual move
 		currentState = currentState.move(input);
 		showMoves(currentState.getPlayerArray()[(getTurn()+players.length-1)%players.length], false);
 		showMoves(players[getTurn()], true);
 
 		//enableAndChangeColor(players[getTurn()].getLocation(), false, players[getTurn()].getColor());
 
 		if(hasWon()){
 				winWindow();
 		}
 
 		gui.setStatus();
 		if (input.startsWith("V") || input.startsWith("H"))
 			updateWalls();
 		if(!netPlay)
 			requestMove();
 
 	}
 
 	public boolean hasWon(){
		if(currentState.getPlayerArray()[getTurn()].hasWon()){
 			return true;
 		}
 		return false;
 	}
 
 	public void winWindow(){
 		JOptionPane.showMessageDialog(winFrame,
 				"Player " + (getTurn()) + " has won!");
 		System.exit(0);
 	}
 
 
 	/**
 	 * Reads in a String representing a move from over the network and makes the move.
 	 * 
 	 * @param input
 	 * 		This is the String representing the move in the Net String format.
 	 */
 	public String readStringFromNet(String input) {
 		moveForNetwork = "";
 		moveMadeForNetwork = new Semaphore(0);
 
 		if(input.contains("MOVE?")){
 			networkMadeLastMove = false;
 			requestMove();
 			System.out.println("before while !mmfn");
 
 			moveMadeForNetwork.acquireUninterruptibly();
 
 			System.out.println("get pass while");
 			return convertGUIStringToNetString(moveForNetwork);
 		}else if(input.contains("WINNER")){
 			
 		}else if(input.contains("REMOVE")){
 			
 		}
 		else{
 			networkMadeLastMove = true;
 			input = convertNetStringToGUIString(input);
 			readStringFromGUI(input);
 		}
 		return "";
 	}
 
 	/**
 	 * Determines whether a move from over the network was legal.
 	 * Currently assumes that the location of the current Player is correct.
 	 * @param input
 	 * 		The String representing the move.
 	 * @return
 	 * 		true if the move is legal.  False otherwise.
 	 */
 	public boolean isStringFromNetLegal(String input) {
 		input = convertNetStringToGUIString(input);
 		return isStringLegal(input);
 	}
 
 	/**
 	 * Called after a move is made.  Prompts the next Player to make their move.
 	 * If the Player is using the GUI, spaces will be highlighted.
 	 * If the Player is an AI, they'll be called.
 	 * If the Player if over a network, it will respond appropriately when it's implemented.
 	 */
 	private void requestMove() {
 		Player[] players = currentState.getPlayerArray();
 		if (getCurrentPlayerType() == Player.GUI_PLAYER)
 			showMoves(players[getTurn()], true);
 		else if (getCurrentPlayerType() == Player.AI_PLAYER)
 			sem.release();
 
 	}
 
 	/**
 	 * This method makes the gui show the walls.
 	 */
 	private void updateWalls() {
 		int[][] walls = currentState.getWalls();
 		Player[] players = currentState.getPlayerArray();
 		int turn = (getTurn() + players.length - 1) % players.length;
 		for (int i = 0; i < walls.length; i++)
 			for (int j = 0; j < walls.length; j++) {
 				if (walls[i][j] == 1 && gui.getVertWallColor(new Point(i, j)).equals(BUTTON_DEFAULT_COLOR)) {
 					gui.setVertWallColor(new Point(i,j), players[turn].getColor());
 					gui.setVertWallColor(new Point(i,j+1), players[turn].getColor());
 				}else if (walls[i][j] == 2 && gui.getHoriWallColor(new Point(i, j)).equals(BUTTON_DEFAULT_COLOR)) {
 					gui.setHoriWallColor(new Point(i,j), players[turn].getColor());
 					gui.setHoriWallColor(new Point(i+1,j), players[turn].getColor());
 				}
 			}
 
 	}
 
 	// this method can show the available moves a player can make if b is true, this needs to be called again with
 	// b being false to stop showing the moves a player could make.
 	private void showMoves(Player pl, boolean b) {
 		showMoves(pl, b, 0);
 	}
 
 	// rec is the number of times a recursive call was made it should probably get a better name
 	private void showMoves(Player pl, boolean b, int rec) {
 		Player[] players = currentState.getPlayerArray();
 		if (rec >= players.length)
 			return;
 		Color c;
 		if (b == true) {
 			int turn = currentState.getTurn();
 			int re = Math.min((players[turn].getColor().getRed() + 255)/2, 240);
 			int gr = Math.min((players[turn].getColor().getGreen() + 255)/2, 240);
 			int bl = Math.min((players[turn].getColor().getBlue() + 255)/2, 240);
 			c = new Color(re, gr, bl);
 		} else {
 			c = BUTTON_DEFAULT_COLOR;
 		}
 
 		Point[] adjacentSpaces = new Point[4];
 		adjacentSpaces[0] = pl.up();
 		adjacentSpaces[1] = pl.down();
 		adjacentSpaces[2] = pl.left();
 		adjacentSpaces[3] = pl.right();
 
 		for (int i = 0; i < adjacentSpaces.length; i++) {
 			if (adjacentSpaces[i] != null) {
 				if (!isBlocked(pl.getLocation(), adjacentSpaces[i])) {
 					int PID = PlayerOnSpace(adjacentSpaces[i]);
 					if (PID >= 0)
 						showMoves(players[PID], b, rec+1);
 					else
 						enableAndChangeColor(adjacentSpaces[i], b, c);			
 				}
 			}
 		}
 
 		enableAndChangeColor(pl.getLocation(), false, pl.getColor());
 	}
 
 	/**
 	 * Returns true if a wall is between two adjacent spaces.
 	 * 
 	 * Do not use this method if the spaces are not adjacent to each other.
 	 * 
 	 * @param p1
 	 * 		the first Point
 	 * @param p2
 	 * 		the second Point
 	 * @return
 	 * 		true if there is a wall between two Points.  Returns false otherwise.
 	 */
 	private boolean isBlocked(Point p1, Point p2) {
 		return currentState.isBlocked(p1, p2);
 	}
 
 	/**
 	 * Returns an int containing the ID of the Player currently on the space passed in.
 	 * If no Player is on that space, -1 is returned.
 	 * @param p
 	 * 		A Point representing the location being checked.
 	 * @return
 	 * 		the ID of the Player if there is one on the space.  returns -1 if there is no Player.
 	 */
 	private int PlayerOnSpace(Point p) {
 		Player[] players = currentState.getPlayerArray();
 		for (int i = 0; i < players.length; i++) {
 			if (p.getLocation().equals(players[i].getLocation())) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	// just a small helper method
 	private void enableAndChangeColor(Point p, boolean b, Color c) {
 		gui.setColorOfSpace(p, c);
 	}
 
 	public int getNumOfPlayers(){
 		return currentState.getPlayerArray().length;
 	}
 
 	public static void main(String[] args) {
 		new Board(true);
 	}
 }
