 // on branch GridNames
 // *
 package clueGame;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 
 //graphics imports
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.LayoutManager;
 import java.awt.MenuBar;
 import java.awt.Panel;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 //team imports
 import clueGame.Card.CardType;
 
 public class Board extends JPanel{
 	
 
 ////////////////////////////////
 //  configuration files
 	private static final String boardConfigFile = "config/ClueLayout.csv";
 	private static final String boardLegendFile = "config/ClueLegend.txt";
 //	CluePlayers
 	private static final String boardPlayersFile = "config/CluePlayers.txt";
 	private static final String boardCardsFile = "config/ClueCards.txt";
 // Graphics
 	
 	// this copy of SCALER is referenced by all classes
 	public static final int SCALER = 25;
 	// used for graphics in ComputerPlayer and HumanPlayer
 	public static final int GRID_COLUMNS = 23; 
 	
 //Game play globals
 	private int playerNumber;
 	private int playerLocation;
 
 	
 // Graphics Notes
 	/* Two types of graphics are used - LayoutManager and bitmap. 
 	 *  paintComponent(Graphics g) is in board.java and calls draw() in itself and it extended classes.
 	 *  Cluegame.java handles the main frame, addElements() handles the panels. 
 	 * */
 //
 ////////////////////////////////
 	
 ////////////////////////////////
 //  declaration of variables
 //
 	
 	ArrayList<BoardCell> cells = new ArrayList<BoardCell>();
 	Map<Character,String> rooms = new HashMap<Character,String>();	
 	
 	private Map<Integer, LinkedList<Integer>> adjList = new HashMap<Integer, LinkedList<Integer>>();
 	private HashSet<BoardCell> targets;
 	private ArrayList<Integer> targetsIndex = new ArrayList<Integer>();
 	
 	int numRows;
 	int numColumns;
 	int index;
 	
 	private boolean[] visited;
 	
 
 	
 //
 ////////////////////////////////
 	
 ////////////////////////////////
 //  ClueGameBoard part 1
 //  constructor with initial setup shenanigans
 //
 	
 	public Board() throws FileNotFoundException, BadConfigFormatException{
 		loadConfigFiles();
 		calcAdjacencies();
 //panel graphics
 		addGridElements();
 		
 		accusationDialog = new AccusationDialog();
 		accusationDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		//board panel graphics are all in draw() and paintComponents method()
 		//playGame();
 		
 	}
 	
 	public Board (LayoutManager layout) {
 		super(layout);
 	}
 
 	public void loadConfigFiles() throws FileNotFoundException, BadConfigFormatException {
 		loadConfigLegend();
 		loadConfigBoard();
 		loadCluePlayerConfigFiles();
 	}
 	
 	public void loadConfigLegend() {
 		try {
 			FileReader reader = new FileReader(boardLegendFile);
 			Scanner in = new Scanner(reader);
 			while (in.hasNext()) {
 				String input = in.nextLine();
 				String[] tokens = input.split(",");
 				if (tokens.length != 2) {
 					throw new BadConfigFormatException("Error with legend file.");
 				}
 				Character key = new Character(tokens[0].charAt((0)));
 				rooms.put(key, tokens[1].trim());
 			}
 		} catch (BadConfigFormatException e) {
 			System.out.println(e);
 		} catch (FileNotFoundException e) {
 			System.out.println(e);
 		}
 	}
 	
 	public void loadConfigBoard() {
 		try {
 			FileReader reader = new FileReader(boardConfigFile);
 			Scanner in = new Scanner(reader);
 			while (in.hasNext()) {
 				String input = in.nextLine();
 				
 				String[] tokens = input.split(",");
 				
 				if (input.length() < 1) {
 					throw new BadConfigFormatException("Error with board config file.");
 				}
 				
 				for (int i = 0; i < tokens.length; i++) {
 					if (tokens[i].equalsIgnoreCase("W")) {
 						cells.add(new WalkwayCell(numRows, i));
 						
 					} else {
 						cells.add(new RoomCell(numRows, i, tokens[i], this.getRoomName(tokens[i])));  // added 4th parameter for graphics
 					}
 				}
 				
 				numRows++;
 			}
 			
 			numColumns = (cells.size() / numRows);
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e);
 		} catch (BadConfigFormatException e) {
 			System.out.println(e);
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void calcAdjacencies() {
 		for (int x = 0; x < numRows; x++) {
 			for (int y = 0; y < numColumns; y++) {
 				LinkedList<Integer> list = new LinkedList<Integer>();
 				
 				int index = calcIndex(x, y);
 				
 				if(getCellAt(index).isWalkway()){
 					if (x > 0) {
 						int attempt = calcIndex(x-1, y);
 						if (validMove(index, attempt)){
 							list.add(attempt);
 						}
 					}
 					if (y > 0){
 						int attempt = calcIndex(x, y-1);
 						if (validMove(index, attempt)){
 							list.add(attempt);
 						}
 					}
 					if (x != numRows-1) {
 						int attempt = calcIndex(x+1, y);
 						if (validMove(index, attempt)){
 							list.add(attempt);
 						}
 					}
 					if (y != numColumns-1) {
 						int attempt = calcIndex(x, y+1);
 						if (validMove(index, attempt)){
 							list.add(attempt);
 						}
 					}
 				}
 				
 				else if(getCellAt(index).isRoom()){
 					RoomCell r = getRoomCellAt(x, y);
 					
 					if(r.isDoorway()){
 						switch (r.doorDirection){
 						case DOWN:
 							if(validMove(index, calcIndex(x+1, y))){
 								list.add(calcIndex(x+1, y));
 							}
 							break;
 						case UP:
 							if(validMove(index,calcIndex(x-1, y))){
 								list.add(calcIndex(x-1, y));
 							}
 							break;
 						case LEFT:
 							if(validMove(index, calcIndex(x, y-1))){
 								list.add(calcIndex(x, y-1));
 							}
 							break;
 						case RIGHT:
 							if(validMove(index,calcIndex(x, y+1))){
 								list.add(calcIndex(x, y+1));
 							}
 							break;
 						default:
 							
 						}
 					}
 				}
 						
 				adjList.put(index, list);
 			}
 		}
 	}
 //	helper method for calcAdjancencies
 	public boolean validMove(int current, int attempt){
 		if (getCellAt(attempt).isWalkway()){
 			return true;
 		}
 		else if(getCellAt(attempt).isRoom()){
 			RoomCell r = getRoomCellAt(attempt);
 			
 			if(r.isDoorway()){
 				switch (r.doorDirection){
 				case DOWN:
 					if (current == calcIndex(r.row+1, r.column)){
 						return true;
 					}
 					break;
 				case UP:
 					if (current == calcIndex(r.row-1, r.column)){
 						return true;
 					}
 					break;
 				case LEFT:
 					if (current == calcIndex(r.row, r.column-1)){
 						return true;
 					}
 					break;
 				case RIGHT:
 					if (current == calcIndex(r.row, r.column+1)){
 						return true;
 					}
 					break;
 				default:
 					
 				}
 			}
 		}
 	
 		return false;
 	}
 	
 //
 ////////////////////////////////
 	
 ////////////////////////////////
 //	ClueGameBoard part i
 //	calcIndex instance method and getters
 //	
 	
 //	instance method calcIndex
 	public int calcIndex(int row, int column) { return ((numColumns)*row) + column; }
 //	get get get your getters
 	public RoomCell getRoomCellAt(int row, int column) {
 		index = calcIndex(row, column);
 		if ((cells.get(index)).isRoom()) {
 			
 			return (RoomCell) cells.get(index);
 		} 
 		
 		return null;
 	}
 	
 	public RoomCell getRoomCellAt(int index) {
 		if ((cells.get(index)).isRoom()) {
 			return (RoomCell) cells.get(index);
 		}
 		
 		return null;
 	}
 
 	public BoardCell getCellAt(int index) { return cells.get(index); }
 
 	public Map<Character, String> getRooms() { return rooms; }
 	
 	//graphics - for printing room name on grid.
 	public String getRoomName(String tokens) { return rooms.get( tokens.charAt(0));	}
 
 	public int getNumRows() { return numRows; }
 
 	public int getNumColumns() { return numColumns; }
 	
 //	
 ////////////////////////////////
 	
 ////////////////////////////////
 //	ClueGameBoard part ii	
 //	calcTargets and getters
 //	
 	
 	public void calcTargets(int index, int steps) {
 		visited = new boolean[getNumColumns()*getNumRows()];
 		targets = new HashSet<BoardCell>();
 		
 		LinkedList<Integer> path = new LinkedList<Integer>();
 		
 		visited[index] = true;
 		
 		visitTargets(adjList.get(index), path, steps);
 	}
 //	helper method for calcTargets, recursive
 	public void visitTargets(LinkedList<Integer> adjacents, LinkedList<Integer> path, int steps) {
 		
 		LinkedList<Integer> adjacentsClone = new LinkedList<Integer>();
 		adjacentsClone.addAll(adjacents);
 		
  		for (Iterator<Integer> itr = adjacentsClone.iterator(); itr.hasNext();) {
 			
 			int current = itr.next();
 			
 			if (getCellAt(current).isRoom()) {
 				targets.add(getCellAt(current));
 			}
 			
 			else {
 			
 				path.addLast(current);
 				visited[current] = true;
 				
 				if (path.size() == steps) { targets.add(getCellAt(current)); }
 				else {
 					LinkedList<Integer>	list = new LinkedList<Integer>();
 					list.addAll(adjList.get(current));
 					
 					for (Iterator<Integer> itr2 = list.iterator(); itr2.hasNext();){
 						int node = itr2.next();
 						
 						if(visited[node]) {
 							itr2.remove();
 						}
 					}
 					
 					if (list.size() > 0) {
 						visitTargets(list, path, steps);
 					}
 				}
 				
 				visited[current] = false;
 				
 				path.removeLast();
 				
 			}
 			
  		}
  		
 	}
 //	getters
 	public HashSet<BoardCell> getTargets() { return targets; }
 	
 	public LinkedList<Integer> getAdjList(int cell) { return adjList.get(cell); }
 
 //	
 ////////////////////////////////
 	
 ////////////////////////////////
 //	CluePlayers
 //	
 	
 //	variables
 	public ArrayList<Player> allPlayers = new ArrayList<Player>();
 	public Player getPlayer(int index) { return allPlayers.get(index); }
 	
 	public ArrayList<Card> deck = new ArrayList<Card>();
 	public ArrayList<Card> dealDeck = new ArrayList<Card>();
 	
 	public ArrayList<Card> cardsSeen = new ArrayList<Card>();
 	public ArrayList<Card> solution = new ArrayList<Card>();
 	
 	public AccusationDialog accusationDialog;
 	
 //	variables to hold list of cards, list of computer 
 //	players, one human player, and an indicator of whose turn it is
 	
 //	i say we have one ArrayList of Players, instantiate in order of file
 	
 ////////////////////////////////
 //	GameSetup section
 //	
 
 	public void loadCluePlayerConfigFiles() throws FileNotFoundException, BadConfigFormatException {
 		// create players
 		loadCluePlayers();
 		// generate cards
 		loadClueCards();
 		// deal cards
 		dealClueCards();
 	}
 	
 	public void loadCluePlayers() throws FileNotFoundException, BadConfigFormatException {
 		FileReader reader = new FileReader(boardPlayersFile);
 		Scanner in = new Scanner(reader);
 		while (in.hasNext()) {
 			String input = in.nextLine();
 			String[] tokens = input.split(",");
 			if (tokens.length != 4) { throw new BadConfigFormatException("Unexpected notation in players file."); }
 			
 			if (tokens[0].equalsIgnoreCase("human")) { allPlayers.add(new HumanPlayer(tokens[1], tokens[2], Integer.parseInt(tokens[3]))); }
 			else if (tokens[0].equalsIgnoreCase("computer")) { allPlayers.add(new ComputerPlayer(tokens[1], tokens[2], Integer.parseInt(tokens[3]))); }
 			else { throw new BadConfigFormatException("Unexpected notation in players file."); }
 		}
 	}
 	
 	public void loadClueCards() throws FileNotFoundException, BadConfigFormatException {
 		FileReader reader = new FileReader(boardCardsFile);
 		Scanner in = new Scanner(reader);
 		while (in.hasNext()) {
 			String input = in.nextLine();
 			String[] tokens = input.split(",");
 			if (tokens.length != 2) { throw new BadConfigFormatException("Unexpected notation in cards file."); }
 			
 			if (tokens[0].equalsIgnoreCase("person")) { deck.add(new Card(tokens[1], CardType.PERSON)); }
 			else if (tokens[0].equalsIgnoreCase("room")) { deck.add(new Card(tokens[1], CardType.ROOM)); }
 			else if (tokens[0].equalsIgnoreCase("weapon")) { deck.add(new Card(tokens[1], CardType.WEAPON)); }
 			else { throw new BadConfigFormatException("Unexpected notation in cards file."); }
 		}
 	}
 	
 	public void dealClueCards() {
 		
 		Random hazard = new Random();
 		int playerIndex = 0;
 		Card someCard;
 
 		dealDeck.addAll(deck);
 		
 		// create solution set
 		while (true) {
 			someCard = dealDeck.get(hazard.nextInt(dealDeck.size()));
 			if (someCard.type == CardType.PERSON){
 				dealDeck.remove(someCard);
 				solution.add(someCard);
 				break;
 			}
 		}
 		while (true) {
 			someCard = dealDeck.get(hazard.nextInt(dealDeck.size()));
 			if (someCard.type == CardType.ROOM){
 				dealDeck.remove(someCard);
 				solution.add(someCard);
 				break;
 			}
 		}
 		while (true) {
 			someCard = dealDeck.get(hazard.nextInt(dealDeck.size()));
 			if (someCard.type == CardType.WEAPON){
 				dealDeck.remove(someCard);
 				solution.add(someCard);
 				break;
 			}
 		}
 		
 		// deal out rest of cards
 		while (!dealDeck.isEmpty()) {
 			someCard = dealDeck.get(hazard.nextInt(dealDeck.size()));
 			dealDeck.remove(someCard);
 			allPlayers.get(playerIndex).cards.add(someCard);
 			
 			++playerIndex;
 			if (playerIndex == allPlayers.size()) playerIndex = 0;
 		}
 		
 		for(Card temp : solution)
 			System.out.println(temp.name);
 	}
 
 //	
 ////////////////////////////////
 
 ////////////////////////////////
 //	 GameAction section
 //	
 	
 	// return true if accusation is true, false otherwise
 	public boolean checkAccusation(Card person, Card room, Card weapon){
 		ArrayList<Card> accusation = new ArrayList<Card>();
 		boolean personMatch = false, roomMatch = false, weaponMatch = false;
 		
 		accusation.add(person);
 		accusation.add(room);
 		accusation.add(weapon);		
 		
 		for(Card temp : solution){
 			if(person.name.equalsIgnoreCase(temp.name))
 				personMatch = true;
 			else if(room.name.equalsIgnoreCase(temp.name))
 				roomMatch = true;
 			else if(weapon.name.equalsIgnoreCase(temp.name))
 				weaponMatch = true;
 		}
 		
 		if (personMatch && roomMatch && weaponMatch) return true;
 		else return false;
 	}
 	
 	//returns a card from a player or null card if no players have any of suggested cards
 	public Card disproveSuggestion(int currentPlayer, String person, String room, String weapon) {
 		Card someCard;
 		ArrayList<Player> playersToCheck = new ArrayList<Player>();
 		playersToCheck.addAll(allPlayers);
 		playersToCheck.remove(currentPlayer);
 		Collections.shuffle(playersToCheck);
 		
 		for (Player somePlayer : playersToCheck) {
 			someCard = somePlayer.disproveSuggestion(person);
 			if (someCard.type != CardType.NULL) return someCard;
 			someCard = somePlayer.disproveSuggestion(room);
 			if (someCard.type != CardType.NULL) return someCard;
 			someCard = somePlayer.disproveSuggestion(weapon);
 			if (someCard.type != CardType.NULL) return someCard;
 		}
 		
 		return new Card();
 	}
 	
 	// this method is in Board and not ComputerPlayer
 	// this is because computers work together anyways.
 	// this method needs to be updated to take in the location of the computer player as well
 	// but more on that when we know exactly how it's going down
 	public Card makeSuggestion(int indexOfComputerPlayer) {
 		Card someCard;
 		Card personCard;
 		Card roomCard;
 		Card weaponCard;
 		Random hazard = new Random();
 		
 		ArrayList<Card> haveNotSeen = new ArrayList<Card>();
 		haveNotSeen.addAll(deck);
 		haveNotSeen.removeAll(allPlayers.get(indexOfComputerPlayer).cards);
 		haveNotSeen.removeAll(cardsSeen);
 		
 		while (true) {
 			someCard = haveNotSeen.get(hazard.nextInt(haveNotSeen.size()));
 			if (someCard.type == CardType.PERSON) {
 				personCard = someCard;
 				break;
 			}
 		}
 		while (true) {
 			someCard = haveNotSeen.get(hazard.nextInt(haveNotSeen.size()));
 			if (someCard.type == CardType.ROOM){
 				roomCard = someCard;
 				break;
 			}
 		}
 		while (true) {
 			someCard = haveNotSeen.get(hazard.nextInt(haveNotSeen.size()));
 			if (someCard.type == CardType.WEAPON){
 				weaponCard = someCard;
 				break;
 			}
 		}
 		
 		someCard = disproveSuggestion(indexOfComputerPlayer, personCard.name, roomCard.name, weaponCard.name);
 		cardsSeen.add(someCard);
 		return someCard;
 	}
 	
 	public int rollDie() {
 		Random rand = new Random();
 		int pick = (rand.nextInt(6) + 1);// generator.nextInt(6) + 1;
 	       //System.out.println("Die: " + Math.abs(pick));
 	       return Math.abs(pick);
 	}
 	
 	public int getCplayerMove() {
 		Random rand = new Random();
		int pick = (rand.nextInt(targetsIndex.size()));// generator.nextInt(6);
 		return targetsIndex.get(Math.abs(pick));
 	}
 	
 	
 //	
 ////////////////////////////////
 // Graphics methods
 	//panels
 		
 		GameControlPanel gameControlPanel = new GameControlPanel();
 		static Board board;
 
 	// paintComponent is called automatically when the frame needs
 	// to display (e.g., when the program starts)
 	
 	public void paintComponent(Graphics g) {
 		//draw cells
 		for(int i = 0; i < (numRows * numColumns) ; i++) {
 			cells.get(i).draw(g); 
 		}
 		
 		//draw targets
 		for(int i = 0; i < targetsIndex.size(); i++) {
 			if(allPlayers.get(playerNumber).isHuman()) {cells.get(targetsIndex.get(i)).drawTargets(g);}
 		}
 		targetsIndex.clear();  //clear the previous turn
 		
 		//draw players
 		for(int i = 0; i < allPlayers.size(); i++) { 
 			allPlayers.get(i).draw(g);
 		}
 	}	
 	
 	
 	
 		public void addGridElements() {
 		setLayout(new BorderLayout());
 		
 		//Pass info to playerDisplay
 		ArrayList<Card> displayCards = new ArrayList<Card>();
 		displayCards = allPlayers.get(0).getPlayerCards();
 		PlayerDisplay playerDisplay = new PlayerDisplay(displayCards);
 		//GameControlPanel gameControlPanel = new GameControlPanel(3);
 		
 		
 		//next 5 lines for debugging to console screen - shows what cards a player has
 //		int playerNumber = 2;
 //		for(int i = 0; i < displayCards.size(); i++) {
 //			displayCards = allPlayers.get(playerNumber).getPlayerCards();
 //			System.out.println("size: " + displayCards.size());
 //			System.out.println("Player: " + allPlayers.get(playerNumber).name.toString()  +","  + displayCards.get(i).type.toString() + ", "  + displayCards.get(i).name.toString());
 //		}
 
 
 		playerDisplay.setVisible(true);
 		add(playerDisplay, BorderLayout.EAST);
 		gameControlPanel.setVisible(true);
 		add(gameControlPanel, BorderLayout.SOUTH);
 	}
 		
 		
 		
 		/* precondition: all setups are finished, player is ready to start the game. this is a
 		 * with no exit condition. yet.
 		 */
 		public void playGame() {
 			playerNumber = 0;
 			playerLocation = 0;
 			int increment = 1;  // used for skipping a player if they do a suggestion
 			boolean humanPlayer = false;
 			//System.out.println("in playGame");
 			Card roomCard, personCard, weaponCard;
 			while(true) {
 				if(gameControlPanel.getNextButton()) {
 					//System.out.println("Next button passed to board");
 					int dieNumber = rollDie();
 					gameControlPanel.setDieRoll(dieNumber);
 					// who is current player. done
 					// get player index. done
 					
 					// human or computer
 					playerNumber =  gameControlPanel.getPlayerNumber();
 					playerLocation = allPlayers.get(playerNumber).getLocation();
 					humanPlayer = allPlayers.get(playerNumber).isHuman();
 					System.out.println("This player # " + playerNumber + ", this player location: " + playerLocation);
 					System.out.println("Player type: " +humanPlayer);
 					//System.out.println(", this player location: " + allPlayers.get(playerNumber).getLocation());
 					calcTargets(playerLocation, dieNumber );  //post: targets populated
 					
 					
 					for(BoardCell temp : targets) { // refactor into calcTargets if time
 						//targetsIndex controls where blue squares are drawn
 						targetsIndex.add(calcIndex(temp.row, temp.column));
 						//System.out.println(calcIndex(temp.row, temp.column));
 					}
 					
 					repaint();  // show blue squares
 					
 					// blue squares shown; possible targets known
 					if(humanPlayer)  { //true if human
 						
 					}
 					
 					if(humanPlayer == false) {  //computer player
 						//pick a square from targets, make an suggestion or accusation
 						int nextLocation = getCplayerMove();
 						System.out.println("GetPlayerMove: " + nextLocation);
 						
 						allPlayers.get(playerNumber).setLocation(nextLocation);
 						
 					}
 					repaint();
 					
 					//set up for next turn
 					gameControlPanel.setPlayerNumber(increment);
 					
 				}
 				if(gameControlPanel.getAccButton()) {
 					//System.out.println("Accusation button passed to board");
 					accusationDialog.setVisible(true);
 					roomCard = new Card(accusationDialog.roomCombo.getSelectedItem().toString(), Card.CardType.ROOM);
 					personCard = new Card(accusationDialog.personCombo.getSelectedItem().toString(), Card.CardType.PERSON);
 					weaponCard = new Card(accusationDialog.weaponCombo.getSelectedItem().toString(), Card.CardType.WEAPON);
 					if(checkAccusation(personCard,roomCard,weaponCard)){
 						JOptionPane.showMessageDialog(null, "You are Correct! Congratulations!");
 						break;
 					} else
 						JOptionPane.showMessageDialog(null, "Wrong Answer, Try again.");
 					repaint();
 				}
 			}
 
 		}
 
 
 	
 ////////////////////////////////
 	
 ////////////////////////////////
 //	main method, for debugging purposes
 //	
 
 
 	public static void main(String[] args) throws FileNotFoundException, BadConfigFormatException {
 		System.out.println("Hello world!!\n");
 		
 		JOptionPane.showMessageDialog(null, "You are Miss Scarlet, Press Next Player to begin.");
 		
 		//Board board = new Board();
 		Board board = new Board();
 		ClueGame clueGame= new ClueGame(); 
  		clueGame.setContentPane(board); //new Board()
 		clueGame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		clueGame.setVisible(true);
 		
 		board.playGame();
 		
 		System.exit(0);
 	
 		System.out.println("\nGoodbye world..");
 	}
 	
 //	
 ////////////////////////////////
 //		  END OF FILE		  //	
 ////////////////////////////////
 	
 }
