 package examples;
 
 import java.util.ArrayList;
 
 import line.Line;
 import play.StageCoach; 
 import configuration.PlayConfig;
 import configuration.PlayType;
 import java.util.HashMap;
 import character.Character; 
 import line.quote.Quote;
 import gll.GlobalLineNode;
 import gll.LineNodeHeader;
 import line.LineType;
 
 public class AdjacencyApproach {
 
 	private static StageCoach currentPlay; 
 	private PlayConfig playConfig;
 	//Represents the character edges
 	static HashMap<String, Character> characters = new HashMap<String, Character>();
 	static HashMap <Character, HashMap<Character, Integer>> edgeWeights = new HashMap <Character, HashMap<Character, Integer>>();
 
 	private static void previousQuoteAnalysis(Character currentCharacter, GlobalLineNode prevNodeQuote, Quote previousQuote, int totalTokenCount) {
 		if (prevNodeQuote != null) { 
 			Line previousLine = prevNodeQuote.getLine();
 			if (previousLine.getType() == LineType.QUOTE) {
 				previousQuote = (Quote) previousLine; 
 			}
 			if (previousQuote != null) {
 				// System.out.println("Previous node is not null");
 				System.out.println("The total token count is " + totalTokenCount);
 				HashMap<Character, Integer> characterMap = edgeWeights.get(currentCharacter);
 				if (characterMap == null) {
 					characterMap = new HashMap<Character, Integer>();
 				}
 				Integer characterCount = characterMap.get(previousQuote.getCharacter());
 				if (characterCount != null) {
 					characterMap.put(previousQuote.getCharacter(), characterCount.intValue() + totalTokenCount);
 				} else {
 					characterMap.put(previousQuote.getCharacter(), totalTokenCount);
 				}
 				edgeWeights.put(currentCharacter, characterMap);
 			}
 		}
 	}
 	
 	private static void nextQuoteAnalysis(Character currentCharacter, GlobalLineNode nextNodeQuote, Quote nextQuote, Quote previousQuote, int totalTokenCount) {
 		if (nextNodeQuote != null) {
 			Line nextLine = nextNodeQuote.getLine();
 			if (nextLine.getType() == LineType.QUOTE) {
 				nextQuote = (Quote) nextLine; 
 			}
 			if ((nextQuote != null && previousQuote != null && nextQuote.getCharacter() != previousQuote.getCharacter()) 
 				 || (nextQuote != null)) {
 				HashMap<Character, Integer> characterMap = edgeWeights.get(currentCharacter);
 				if (characterMap == null) {
 					characterMap = new HashMap<Character, Integer>();
 				}
 				Integer characterCount = characterMap.get(nextQuote.getCharacter());
 				if (characterCount != null) {
 					characterMap.put(nextQuote.getCharacter(), characterCount.intValue() + totalTokenCount);
 				} else {
 					characterMap.put(nextQuote.getCharacter(), totalTokenCount);
 				}
 				edgeWeights.put(currentCharacter, characterMap);
 			}
 		}
 	}
 	
 	private static void constructAdjacency() {
 		characters = currentPlay.returnCharacters(null, null);
 		for (String charName : characters.keySet()) {
 			Character currentCharacter = characters.get(charName);
 			ArrayList<Quote> quotes = currentCharacter.getQuotes();
 			for (Quote quote : quotes) {
 				int totalTokenCount = quote.getTotalTokens();
 				
 				// The fact that the GlobalLineNode is actually exposed to the client
 				// is troubling. We have to refactor this design substantially.
 				GlobalLineNode nodeQuote = quote.getGllNode();
 				GlobalLineNode prevNodeQuote = nodeQuote.getPreviousNode(LineNodeHeader.DEFAULT);
 				GlobalLineNode nextNodeQuote = nodeQuote.getNextNode(LineNodeHeader.DEFAULT);
 				
				Quote previousQuote = null;
 				Quote nextQuote = null;
 				
 				previousQuoteAnalysis(currentCharacter, prevNodeQuote, previousQuote, totalTokenCount);
 				nextQuoteAnalysis(currentCharacter, nextNodeQuote, nextQuote, previousQuote, totalTokenCount);
 			}
 		}
 	}
 	
 	private static void outputAdjacency() {
 		for (Character character : edgeWeights.keySet()) {
 			HashMap<Character, Integer> hashedCharacterMap = edgeWeights.get(character);
 			System.out.println("Character: " + character.getName());
 			for (Character targetCharacter : hashedCharacterMap.keySet()) {
 				System.out.println("   Target Character: " + targetCharacter.getName() + ", Value: " + hashedCharacterMap.get(targetCharacter));
 			}
 		}
 	}
 	
 	public static void main(String args[]) {
 		PlayConfig config = new PlayConfig(PlayType.DEFAULT);
 		config.set("fileName", args[0]);
 		
 		currentPlay = new StageCoach(config);
 		currentPlay.instantiatePlay();
 		
 		constructAdjacency();
 		outputAdjacency();
 	}
 }
