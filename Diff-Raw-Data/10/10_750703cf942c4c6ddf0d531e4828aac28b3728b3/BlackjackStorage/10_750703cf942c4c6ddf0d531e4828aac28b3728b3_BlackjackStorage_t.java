 package storage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import cards.*;
 
 /**
  * BlackjackStorage handles blackjack game saving and loading. It contains methods to save and load decks, save and load hands,
  * and save and load playernames.
  * @author JackGivesBack
  */
 public class BlackjackStorage {
 
 	/**
 	 * saveDeck writes the deck to a file, named according to the gameID that is given
 	 * @param deck The Deck object of the game that calls saveDeck
 	 * @param gameID An integer representing the game's ID, used to determine the name of the same
 	 * @return true if saveDeck succeeded, otherwise false
 	 */
 	public static boolean saveDeck(Deck deck, int gameID) {
 		try {
 			FileWriter fstream = new FileWriter("saves/" + gameID + ".csv");
 			BufferedWriter writer = new BufferedWriter(fstream);
 			
 			writer.write("Deck,");
 			while (deck.size() > 1) {
 				Card card = deck.draw();
 				writer.write(card.toString() + ":");
 			}
 			writer.write(deck.draw().toString());
 			writer.write("\r\n");
 			writer.close();
 			return true;
 			
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * loadPlayerNames searches through a gameID file and retrieves all the names present in the saved game
	 * @param gameId An integer representing game to save
 	 * @return An ArrayList<String> of all the playerNames found
 	 */
 	public static ArrayList<String> loadPlayerNames(int gameId) {
 		ArrayList<String> string = new ArrayList<String>();
 		try {
 			Scanner reader = new Scanner(new File("saves/" + gameId + ".csv"));
 
 			// Discard the first line, which always stores the state of the deck
 			reader.nextLine();
 			while (reader.hasNextLine()) {
 				String[] parsedLine = reader.nextLine().split(",");
 				string.add(parsedLine[0]);
 			}
 			reader.close();
 			return string;
 		} catch (IOException e) {
 			System.out.println("Could not load player's name");
 			return string;
 		}
 	}
 
 	/**
 	 * savePlayerNames appends playerNames to a gameID file
 	 * @param username String of the playername to save
 	 * @param gameID Integer representing the game to save
 	 * @return true if the save was successful, otherwise false
 	 */
 	public static boolean savePlayerNames(String username, int gameID) {
 		try {
 			FileWriter fstream = new FileWriter("saves/" + gameID + ".csv", true);
 			BufferedWriter writer = new BufferedWriter(fstream);
 			
 			writer.write(username + "\r\n");
 			writer.close();
 			return true;	
 			
 		} catch (IOException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * saveHand saves the state of a player's hand to a gameID file
 	 * @param username A string of the player's username to save to file
 	 * @param hand The state of the player's hand (the cards it contains)
 	 * @param gameID Integer representing the game to save
 	 * @return true if the save was successful, false otherwise
 	 */
 	public static boolean saveHand(String username, Hand hand, int gameID) {
 		try {
 			FileWriter fstream = new FileWriter("saves/" + gameID + ".csv", true);
 			BufferedWriter writer = new BufferedWriter(fstream);
 
 			writer.write(username + ",");
 			while (hand.getNumberCards() > 1) {
 				Card card = hand.removeCard(0);
 				writer.write(card + ":");
 			}
 			writer.write(hand.removeCard(0).toString());
 			writer.write("\r\n");
 			writer.close();
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * loadHand loads a player's hand from a gameID file
 	 * @param username String of the playername to save
 	 * @param gameID Integer representing the game to save
 	 * @return A Hand object containing the cards associated with the player in the saved game
 	 */
 	public static Hand loadHand(String username, int gameID) {
 		Hand hand = new Hand();
 		try {
 			Scanner reader = new Scanner(new File("saves/" + gameID + ".csv"));
 			while (reader.hasNextLine()) {
 				String[] parsedLine = reader.nextLine().split(",");
 				if (parsedLine[0].equals(username)) {
 					String[] cardsList = parsedLine[1].split(":");
 					for (String UnparsedCard : cardsList) {
 						String parsedCard[] = UnparsedCard.split(" ");
 						hand.addCard(Card.fromString(parsedCard[0], parsedCard[2]));
 						
 					}
 				}
 			}
 			reader.close();
 			return hand;
 		} catch (IOException e) {
 			System.out.println("Could not load hand");
 			return hand;
 		} 
 	}
 	
 	/**
 	 * loadDeck loads a deck from a gameID file
 	 * @param gameID Integer representing the game to save
 	 * @return A Deck object of the state of the deck in the saved game
 	 */
 	public static Deck loadDeck(int gameID) {
 		Deck deck = new Deck();
 		try {
 			Scanner reader = new Scanner(new File("saves/" + gameID + ".csv"));
 
 			String[] parsedLine = reader.nextLine().split(",");
 			String[] cards = parsedLine[1].split(":");
 			for (String UnparsedCard : cards) {
 				String parsedCard[] = UnparsedCard.split(" ");
 				deck.addCard(Card.fromString(parsedCard[0], parsedCard[2]));
 			}
 			reader.close();
 			return deck;
 		} catch (IOException e){
 			return deck;
 		}
 	}
 }
