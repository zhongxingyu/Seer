 package clueBoard;
 import java.awt.Color;
 import java.awt.Point;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.lang.reflect.Field;
 //Naomi and Brandon
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 
 public class ClueGame {
 	private ArrayList<ComputerPlayer> computer;
 	private Solution answer;
 	private ArrayList<Card> cards;
 	private static ArrayList<Card> fullDeck;
 	private HumanPlayer human;
 	private boolean turn;
 	private Player currentPlayer;
 	private String playerFile;
 	private String cardFile;
 
 	public ClueGame() {
 		computer = new ArrayList<ComputerPlayer>();
 		cards = new ArrayList<Card>();
 		fullDeck = new ArrayList<Card>();
 		human = new HumanPlayer();
 		setPlayerFile("Players.txt");
 		setCardFile("Cards.txt");
 	}
 	
 	public void deal(){
 		selectAnswer();
 		int dealt = 0;
 		while(!cards.isEmpty()) {
 			int index = dealt % 6;
 			Player dealtTo = null;
 			Card toBeDealt;
 			switch(index) {
 			case 0:
 				dealtTo = human;
 				break;
 			case 1:
 				dealtTo = computer.get(0);
 				break;
 			case 2:
 				dealtTo = computer.get(1);
 				break;
 			case 3:
 				dealtTo = computer.get(2);
 				break;
 			case 4:
 				dealtTo = computer.get(3);
 				break;
 			case 5:
 				dealtTo = computer.get(4);
 				break;
 			}
 			Random roller = new Random();
 			int cardIndex = roller.nextInt(cards.size());
 			toBeDealt = cards.get(cardIndex);
 			dealtTo.acceptCard(toBeDealt);
 			cards.remove(cardIndex);
 			dealt++;
 		}
 	}
 	
 	public void loadConfigFiles(){
 		try {
 			loadPlayerConfigFile(playerFile);
 			loadCardConfigFile(cardFile);
 		} catch (FileNotFoundException e) {
 			System.out.println(e);
 		} catch (BadConfigFormatException e) {
 			System.out.println(e);
 		}
 		
 		
 	}
 	
 	public void selectAnswer(){
 		String person;
 		String room;
 		String weapon;
 		ArrayList<Card> people = new ArrayList<Card>();
 		ArrayList<Card> rooms = new ArrayList<Card>();
 		ArrayList<Card> weapons = new ArrayList<Card>();
 		for(Card c : cards) {
 			if(c.getType() == Card.cardType.PERSON)
 				people.add(c);
 			else if(c.getType() == Card.cardType.ROOM)
 				rooms.add(c);
 			else
 				weapons.add(c);
 		}
 		Card selected = null;
 		Random roller = new Random();
 		
 		int index = roller.nextInt(people.size());
 		selected = people.get(index);
 		person = selected.getCard();
 		cards.remove(selected);
 		
 		index = roller.nextInt(rooms.size());
 		selected = rooms.get(index);
 		room = selected.getCard();
 		cards.remove(selected);
 		
 		index = roller.nextInt(weapons.size());
 		selected = weapons.get(index);
 		weapon = selected.getCard();
 		cards.remove(selected);
 		
 		answer = new Solution(person,weapon,room);
 	}
 	
 	public Card handleSuggestion(Solution suggestion){
 		ArrayList<Player> players = new ArrayList<Player>();
 		ArrayList<Card> clues = new ArrayList<Card>();
 		if(currentPlayer != human)
 			players.add(human);
 		for(ComputerPlayer c : computer) {
 			if(currentPlayer != c)
 				players.add(c);
 		}
 		Random roller = new Random();

 		while(!players.isEmpty()) {
 			int disproverIndex = roller.nextInt(players.size());
 			Player disprover = players.get(disproverIndex);
 			clues.add(disprover.disproveSuggestion(suggestion));
 			players.remove(disprover);
 		}
 		if(clues.size() == 0)
 			return null;
 		else {
 			int cardIndex = roller.nextInt(clues.size());
 			return clues.get(cardIndex);
 		}
 	}
 	
 	public boolean checkAccusation(Solution solution){
 		boolean correct = true;
 		if(solution.getPerson() != answer.getPerson()) {
 			correct = false;
 		}
 		else if(solution.getRoom() != answer.getRoom()) {
 			correct = false;
 		}
 		else if(solution.getWeapon() != answer.getWeapon()) {
 			correct = false;
 		}
 		return correct;
 	}
 
 	
 	//load separate config files to test for exceptions
 	public void loadPlayerConfigFile(String file) throws FileNotFoundException, BadConfigFormatException {
 		FileReader playerReader = new FileReader(file);
 		Scanner playerScanner = new Scanner(playerReader);
 		HumanPlayer human;
 		ComputerPlayer currentComputer;
 		final int numColumns = 4;
 		int row = 0, column = 0;
 		int numRows = 0;
 		Point location;
 		String name;
 		java.awt.Color color;
 		
 		while(playerScanner.hasNextLine()) {
 			String inputLine = playerScanner.nextLine();
 			String[] parts = inputLine.split(",");
 			
 			//if first line, create human player
 			if(numRows == 0) {
 				//check for all data
 				if(parts.length != numColumns) 
 					throw new BadConfigFormatException("Too few columns in player legend file");
 				else {
 					name = parts[0];
 					row = Integer.parseInt(parts[1]);
 					column = Integer.parseInt(parts[2]);
 					location = new Point(row,column);
 					color = convertColor(parts[3]);
 					human = new HumanPlayer(name, location, color);
 					this.human = human;
 				}
 				
 			}
 			//otherwise make computer player
 			else {
 				//check for all data
 				if(parts.length != numColumns) 
 					throw new BadConfigFormatException("Too few columns in player legend file");
 				else {
 					name = parts[0];
 					row = Integer.parseInt(parts[1]);
 					column = Integer.parseInt(parts[2]);
 					location = new Point(row,column);
 					color = convertColor(parts[3]);
 					currentComputer = new ComputerPlayer(name, location, color);
 					this.computer.add(currentComputer);
 				}
 			}
 			numRows++;
 		}
 		playerScanner.close();
 	}
 	
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
 	
 	public void loadCardConfigFile(String file) throws FileNotFoundException, BadConfigFormatException {
 		FileReader cardReader = new FileReader(file);
 		Scanner cardScanner = new Scanner(cardReader);
 		Card toAdd;
 		final int numColumns = 2;
 		String name;
 		Card.cardType type = null;
 		while(cardScanner.hasNextLine()) {
 			String line = cardScanner.nextLine();
 			String[] parts = line.split(",");
 			if(parts.length != numColumns) {
 				throw new BadConfigFormatException("Error in card file");
 			}
 			else {
 				name = parts[0];
 				if(parts[1].equals("WEAPON")) 
 					type = Card.cardType.WEAPON;
 				else if (parts[1].equals("PERSON"))
 					type = Card.cardType.PERSON;
 				else if (parts[1].equals("ROOM")) 
 					type = Card.cardType.ROOM;
 				else
 					throw new BadConfigFormatException("invalid card type");
 				toAdd = new Card(name, type);
 				this.cards.add(toAdd);
 				this.fullDeck.add(toAdd);
 			}
 		}
 	}
 
 	
 	
 	
 	
 	
 	//Getters and Setters for tests
 	public Solution getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(Solution answer) {
 		this.answer = answer;
 	}
 
 	public ArrayList<Card> getCards() {
 		return cards;
 	}
 	
 	public static ArrayList<Card> getFullDeck() {
 		return fullDeck;
 	}
 
 	public void setComputer(ArrayList<ComputerPlayer> computer) {
 		this.computer = computer;
 	}
 
 	public ArrayList<ComputerPlayer> getComputer() {
 		return computer;
 	}
 
 	public HumanPlayer getHuman() {
 		return human;
 	}
 	
 	
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 
 	public void setCurrentPlayer(Player currentPlayer) {
 		this.currentPlayer = currentPlayer;
 	}
 
 	public String getPlayerFile() {
 		return playerFile;
 	}
 
 	public void setPlayerFile(String playerFile) {
 		this.playerFile = playerFile;
 	}
 
 	public String getCardFile() {
 		return cardFile;
 	}
 
 	public void setCardFile(String cardFile) {
 		this.cardFile = cardFile;
 	}
 }
