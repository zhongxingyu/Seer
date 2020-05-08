 package clueGame;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import controlGUIs.ClueControlGUI;
 
 public class ClueGame extends JPanel {
 
 	private String playerData;
 	private String cardData;
 	private Solution solution;
 	private ArrayList<Card> deck;
 	private ArrayList<Player> players;
 
 	private Board board;
 	private ArrayList<Card> checkDeck;
 
 	// ----------------------------------------------------------------
 
 	public ClueGame(String playerData, String cardData, 
 			String sheetName, String legendFile){
 		this.playerData = playerData;
 		this.cardData = cardData;
 		// Don't forget to make the board!
 		board = new Board(sheetName, legendFile);
 		board.loadConfigFiles();
 		// Does this need to happen??
 		board.calcAdjacencies();
 
 		// ----------------------------------------------------------------
 		// The GUI part is here
 		
 		setSize(new Dimension(500, 500));
 		//setTitle("The Game of Clue");
 		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		add(board, BorderLayout.CENTER);
 		//ClueControlGUI control = new ClueControlGUI();
 		//add(control, BorderLayout.SOUTH);
 	}
 
 	//public static void main(String [] args){
 		//ClueGame game = new ClueGame("PlayerData.txt", "cardConfig.txt", "BoardLayout.csv", "legend.txt");
 		//game.setVisible(true);
 		//game.loadConfigFiles();
 		//game.deal();
 
 		//Board board = game.getBoard();
 		//board.calcAdjacencies();
 		//game.repaint();
 		//board.repaint();
 	//}
 	
 	// -------------------------------------------------------------------------------------------
 
 	public char passRoomInformation(int index){
 		return (board.getCellAt(index)).getInitial();
 	}
 
 	public void deal(){
 		checkDeck = deck;
 		Collections.shuffle(deck);
 
 		//these are indicators that solution doesn't have these types of cards, YET
 		boolean suspect = false;
 		boolean weapon = false;
 		boolean room = false;
 		String s = "";
 		String w = "";
 		String r = "";
 
 		while (!deck.isEmpty() && (!suspect || !weapon || !room)){
 			Card c = deck.remove(0);
 			if (!suspect && c.getType() == Card.CardType.SUSPECT){
 				s = c.getName();
 				suspect = true;
 			}
 			else if (!weapon && c.getType() == Card.CardType.WEAPON){
 				w = c.getName();
 				weapon = true;
 			}
 			else if (!room && c.getType() == Card.CardType.ROOM){
 				r = c.getName();
 				room = true;
 			}
 			else{
 				deck.add(c);
 			}
 		}
 
 		solution = new Solution(s,r,w);
 		Collections.shuffle(deck);
 
 		int i = 0;
 		while (!deck.isEmpty()) {
 			players.get(i % players.size()).addCard(deck.remove(0));
 			i++;
 		}
 	}
 
 	public void loadConfigFiles(){
 		loadPlayers();
 		loadCards();
 	}
 
 	// For working with Colors
 	// Be sure to trim the color, we don't want spaces around the name
 	public Color convertColor(String strColor) {
 		Color color; 
 		try {     
 			// We can use reflection to convert the string to a color
 			Field field = Class.forName("java.awt.Color").getField(strColor.trim());     
 			color = (Color)field.get(null); } 
 		catch (Exception e) {  
 			color = null; // Not defined } 
 		}
 		return color;
 	}
 
 	public void loadPlayers() {
 		players = new ArrayList<Player>();
 		try{
 			FileReader reader = new FileReader(playerData);
 			Scanner in = new Scanner(reader);
 			String tempName;
 			Color tempColor;
 			int tempStart;
 			boolean humanSet = false;
 			while(in.hasNext()){
 				String[] tempLine = in.nextLine().split(", ");
 				tempName = tempLine[0];
 				tempColor = convertColor(tempLine[1]);
 				tempStart = Integer.parseInt(tempLine[2]);
 				if (!humanSet) {
 					players.add(new HumanPlayer(tempName,tempColor,tempStart));
 					humanSet = true;
 				} else {
 					players.add(new ComputerPlayer(tempName,tempColor,tempStart));
 				}
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	public void loadCards() {
 		deck = new ArrayList<Card>();
 		try {
 			FileReader reader = new FileReader(cardData);
 			Scanner in = new Scanner(reader);
 			String tempName;
 			while(in.hasNext()) {
 				String[] tempLine = in.nextLine().split(", ");
 				String tempTypeAsStr = tempLine[0];
 				tempName = tempLine[1];
 				if (tempTypeAsStr.equals("WEAPON"))
 					deck.add(new Card(Card.CardType.WEAPON, tempName));
 				else if (tempTypeAsStr.equals("SUSPECT"))
 					deck.add(new Card(Card.CardType.SUSPECT, tempName));
 				else if (tempTypeAsStr.equals("ROOM"))
 					deck.add(new Card(Card.CardType.ROOM, tempName));
 				else
 					throw new BadConfigFormatException("Bad Card Configuration");
 			}
 
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage());
 		} catch (BadConfigFormatException b) {
 			System.out.println(b.getMessage());
 		}
 	}
 
 	public void selectAnswer(){
		
 	}
 
 	public Card handleSuggestion(Card suspect, Card weapon, Card room, Player suggester){
 		// probably calls disproveSuggestion() on the array 'players'
 		// will loop through 'players' and each will try to disprove the suggestion (except the suggester)
 		// will return the first instance of a valid disproval or null if none occurred
 
 		for (Player p : getPlayers())
 			if (!p.equals(suggester)){
 				Card c = p.disproveSuggestion(suspect, weapon, room);
 				if (c!=null)
 					return c;
 			}
 		return null;
 	}
 
 	// Overloaded function for working with strings instead
 	// Note: disproveSuggestion also has an overloaded version called disproveSuggestions
 	public Card handleSuggestions(String suspect, String weapon, String room, Player cp2) {
 
 		for (Player p : getPlayers()){
 			if (!p.equals(cp2)){
 				Card c = p.disproveSuggestions(room, weapon, suspect);
 				if (c!=null)
 					return c;
 			}
 		}
 		return null;
 	}
 
 	public ArrayList<Card> getDeck(){
 		return deck;
 	}
 
 	public ArrayList<Player> getPlayers(){
 		return players;
 	}
 
 	public boolean checkAccusation(Solution s){
 		return s.equals(solution);
 	}
 
 	public Solution getSolution() {
 		// ONLY FOR TESTING
 		return solution;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public void setPlayers(ArrayList<Player> players) {
 		this.players = players;
 	}
 	public void setSolution(Solution s){
 		solution = s;
 	}
 	public void setCheckDeck(ArrayList<Card> c){
 		checkDeck = c;
 	}
 
 
 }
