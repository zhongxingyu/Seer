 import java.util.*;
 import java.io.*;
 
 
 public class EvilHangMan extends BasicHangmanGame {
 	//Static constants
 	private static final int DEFAULT_SECRET_WORD_LENGTH = 6;
 	private static final int DEFAULT_NUM_GUESSES = 20;
 	
 	//Dictionary
 	private String[] Wordlist = new String[235000];// to store the dictionary
 	private int numWords = 0;// count the number of possible secret words.
 	private boolean guessResult = false;
 	private HashMap<Character, ArrayList<String>> characterToWords;
 	private HashSet<String> wordSet;
 
 	public EvilHangMan() {
 		this(DEFAULT_SECRET_WORD_LENGTH, DEFAULT_NUM_GUESSES);
 	}
 	
 	public EvilHangMan(int secretWordLength, int numGuesses) {
 		super(secretWordLength, numGuesses);
 		
 		this.characterToWords = new HashMap<Character, ArrayList<String>>();
 		this.wordSet = new HashSet<String>();
 		Scanner Scanner = null;
 		try {
 			Scanner = new Scanner(new File("dictionary.txt"));// read the dictionary
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		while (Scanner.hasNext()) {
 			
 			//Getting the next word in the dictionary
 			String dictionaryWord = Scanner.nextLine().toUpperCase();
 			
 			//Checking if dictionaryWord is the same length as the secret word
 			if (dictionaryWord.length() == secretWordLength) {
 				numWords++;
 				HashSet<Character> characterSet = new HashSet<Character>();
 				
 				//Finding all unique characters in dictionaryWord
 				for(int i = 0; i < dictionaryWord.length(); i++) {
 					if(!characterSet.contains(dictionaryWord.charAt(i))) {
 						characterSet.add(dictionaryWord.charAt(i));
 					}
 				}
 				
 				//adding (c, dictionaryWord) for all unique characters c in dictionaryWord
 				for(Character c : characterSet) {
 					ArrayList<String> stringsContainingCharacter = this.characterToWords.get(c);
					if(stringsContainingCharacter == null) {
						stringsContainingCharacter = new ArrayList<String>();
					}
 					stringsContainingCharacter.add(dictionaryWord);
 				}
 				
 				//adding dictionaryWord
 				this.wordSet.add(dictionaryWord);
 			}
 		}
 		
 		this.currentGameState = "";
 		for (int i = 0; i < this.secretWordLength; i++) {
 			currentGameState += "_ ";
 		}
 		Scanner.close();
 	}
 
 
 	@Override
 	public int numLettersRemaining() {
 		return 26; // because they never get one right!
 	}
 
 	@Override
 	public boolean isWin() {
 		return false;
 	}
 
 	public boolean makeGuess(char ch) {
 		guessResult = false;
 		this.guessedLetter = ch;
 		if (Character.isLetter(ch) && !RepeatInput(ch)) {
 			// adjust the Wordlist in order to avoid the word with the letter
 			// user guessed
 			int tempWordNum = 0;
 			for (int i = 0; i < numWords; i++) {
 				for (int j = 0; j < this.secretWordLength; j++) {
 					if (Wordlist[i].charAt(j) == ch) {
 						break;
 					} else {
 						if (j == this.secretWordLength - 1) {
 							if (Wordlist[i].charAt(j) != ch) {
 								tempWordNum++;
 							}
 						}
 					}
 				}
 			}
 			// we choose the words that don't contain the letter the user
 			// guessed, and they will be the new possible secret words.
 			String[] temp = new String[tempWordNum];
 			int tempIndex = 0;
 			for (int i = 0; i < numWords; i++) {
 				for (int j = 0; j < this.secretWordLength; j++) {
 					if (Wordlist[i].charAt(j) == ch) {
 						break;
 					} else {
 						if (j == this.secretWordLength - 1) {
 							if (Wordlist[i].charAt(j) != ch) {
 								temp[tempIndex] = Wordlist[i];
 								tempIndex++;
 							}
 						}
 					}
 				}
 			}
 			if (tempWordNum == 0) {
 
 				secretWord = Wordlist[0];
 				guessResult = true;
 			} else {
 				secretWord = temp[0];
 				numWords = tempWordNum;
 				Wordlist = temp;
 				this.remainingGuesses--;
 				guessResult = false;
 			}
 			if (!guessResult) {
 				this.guessHistory.add(this.guessedLetter);
 			}
 
 		} else return false;
 		
 		return guessResult;
 	}
 
     public boolean RepeatInput(char c)
     {
     	for (int i = 0; i < this.guessHistory.size(); i++) {
     		if (this.guessHistory.get(i) == c) return true;
     	}
     	return false;
     }
 }
