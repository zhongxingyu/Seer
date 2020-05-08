 package hangman;
 
 import java.io.File;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.HashMap;
 //import java.util.List;
 //import java.util.ArrayList;
 import java.util.Scanner;
 import java.io.FileNotFoundException;
 import java.lang.StringBuilder;
 import java.util.Iterator;
 
 
 public class EvilHangmanGameImpl implements EvilHangmanGame {
   //private List<Character> guessedChars;	
   private StringBuilder guessedChars;
   private Set<String> dict;
 	private boolean[] chars;
   private int remainingGuesses;  
   private StringBuilder secretWord; // This is the winning world  
   
   
   /* Constructor 
    * Set up array of 26 to know which chars have been used;
    */
   public EvilHangmanGameImpl() {
     guessedChars = new StringBuilder("Used letters: ");
     chars = new boolean[26];
     for (int i = 0; i < 26; i++) {
       chars[i] = false;
     }
   }
 
 	/**
 	 * Starts a new game of evil hangman using words from <code>dictionary</code>
 	 * with length <code>wordLength</code>
 	 * 
 	 * @param dictionary Dictionary of words to use for the game
 	 * @param wordLength Number of characters in the word to guess
 	 */
 	public void startGame(File dictionary, int wordLength) {
     loadDictionary(dictionary, wordLength);
     secretWord = new StringBuilder(wordLength);
     for (int i = 0; i < wordLength; i++) {
       secretWord.append('-');
     }
   
   }
 	
 
 	/**
 	 * Make a guess in the current game.
 	 * 
 	 * @param guess The character being guessed
 	 * @return The set of strings that satisfy all the guesses made so far
 	 * in the game, including the guess made in this call. The game could claim
 	 * that any of these words had been the secret word for the whole game. 
 	 * 
 	 * @throws GuessAlreadyMadeException If the character <code>guess</code> 
 	 * has already been guessed in this game.
 	 */
 	public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
     HashMap<String, HashSet<String>> setSets = new HashMap<String, HashSet<String>>();
     // iterate through the current set of words; make a pattern based off char
     // positions in the current word and if it exists in the hashmap then add, else 
     // make a new hashset and insert it
     Iterator<String> it = dict.iterator();
     while (it.hasNext()) {
       String word = it.next();
       StringBuilder tmp = new StringBuilder(word);
       for (int i = 0; i < tmp.length(); i++) {
         char ch = tmp.charAt(i);
         if (ch != guess) {
           tmp.setCharAt(i, '-');
         }
       }
       if (setSets.containsKey(tmp.toString())) {
         setSets.get(tmp.toString()).add(word);
       //  System.out.println("CONTAINS Key "+tmp);
       } else {
         setSets.put(tmp.toString(), new HashSet<String>());
         setSets.get(tmp.toString()).add(word);
       }
     }
     Iterator<String> bilbo = setSets.keySet().iterator();
     HashSet<String> frodo = new HashSet<String>();
     String key = "";
     while (bilbo.hasNext()) {
       String keySearch = bilbo.next();
       //System.out.println("KEY = "+keySearch + " Size: "+ setSets.get(keySearch).size());
       HashSet<String> tmp = setSets.get(keySearch);
       if (tmp.size() > frodo.size()) {
         frodo = tmp;
         key = keySearch;
         //System.out.println("frodo replaced by tmp");
       } else if (tmp.size() == frodo.size()) {
         /* 1. Choose group which no letters appear
          * 2. Choose with fewest letters.
          * 3. Choose rightmost letter (repeat if needed)
          */
         if (!keySearch.contains(String.valueOf(guess))) {
           frodo = tmp;
           key = keySearch;
         } else if (countChars(keySearch,guess) > countChars(key,guess)) {
           frodo = tmp;
           key = keySearch;
         } else if (countChars(keySearch,guess) == countChars(key,guess)) {
           
         }
       }
     }
     // if key returned has only '-----' then print letter not found
     if (key.contains(String.valueOf(guess)) == false) {
       System.out.println("Sorry, there are no "+guess+"'s");
     } else {
       // else 
       // update guessed word based off what is guessed here.
       // and print found letter
       StringBuilder k = new StringBuilder(key);
       int numChar = 0;
       for (int i = 0; i < secretWord.length(); i++ ) {
         char ch = k.charAt(i);
         if (ch != '-') {
           secretWord.setCharAt(i, ch);
           numChar++;
         }
       }
       System.out.println("Yes, there is (are) " + numChar + " " + guess);
     }
     return frodo;
   }
 
   public char getCharInput() {
     // make it lowercase
     // if used letter already prompt again to get new letter
     // if invalid input prompt for new input
     // check if it is a char
     // add char to array of used chars
     // append to string guessedChars
     System.out.print("Enter Guess: ");
     Scanner in = new Scanner(System.in);
     char c = (char) in.next().charAt(0);
     Character ch = new Character(c);
     ch = Character.toLowerCase(ch);
     int numValue = Character.getNumericValue(ch) - 10;
     if (numValue < 0 || numValue > 26) {
       System.out.println("Invalid Input");
       getCharInput();
     }
     if (chars[numValue] == true) {
       System.out.println("You already used that letter.");
       getCharInput();
     } else {
       chars[numValue] = true;
       guessedChars.append(" " + ch);
       remainingGuesses--;
     }
     return ch;
   }
   
   public void playGame(int numGuesses) {
     remainingGuesses = numGuesses;
     for (int i = 0; i < numGuesses; i++) {
       System.out.println("You have "+remainingGuesses+" guesses left");
       System.out.println(guessedChars.toString());
       System.out.println("Word: " + secretWord.toString());
       char guess = getCharInput();
       System.out.println(guess); 
       try {
         dict = makeGuess(guess);  
         //System.out.println(dict.toString());
       } catch (GuessAlreadyMadeException e) {
         e.printStackTrace();
       }
       if (!(secretWord.toString().contains(String.valueOf('-')))) {
       //win
         System.out.println("You Win!");
         System.out.println("The correct word is "+secretWord);
         System.exit(-9);
       }
     }
     System.out.println("You lose!");
     String[] foo = dict.toArray(new String[dict.size()]);
     System.out.println("The word was: "+foo[0]);
   }
 
   private void loadDictionary(File dictionary, int wordLength) {
     try {
       if (dictionary.length() == 0) {
         System.out.println("Dictionary Empty");
         System.exit(-9);
       }
       //guessedChars = new ArrayList<Character>();
       dict = new HashSet<String>();
       Scanner sc = new Scanner(dictionary);
       while (sc.hasNext()) {
         String tmp = sc.next();
         if (tmp.length() == wordLength) {
           dict.add(tmp);
         }
       }
     } catch (FileNotFoundException e) {
       System.out.println("No Such File");
     }
   }
 
   private int countChars(String haystack, char needle) {
     int count = 0;
     for (int i = 0; i < haystack.length(); i++) {
       if (haystack.charAt(i) == needle) {
         count++;
       }
     }
     return count;
   }
 	
 }
