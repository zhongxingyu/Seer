 package com.example.zootypers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Observable;
 
 import org.apache.commons.io.FileUtils;
 
 /** 
  * 
  * The Model class for Single Player store a list of words for the UI to display.
  * It keeps track of word and letter the user has typed and updates the view accordingly.
  * 
  * @author winglam, nhlien93, dyxliang
  * 
  */
 
 public class SinglePlayerModel extends Observable {
 
   // stores an array of words 
   private String[] wordsList;
   
   // array of indices that refers to strings inside wordsList
   private int[] wordsDisplayed;
   
   // index of a string inside wordsDisplayed (should NEVER be used on wordsList!)
   private int currWordIndex;
   
   // index of letter that has been parsed from the currWordIndex
   private int currLetterIndex;
   
   // index of the next word to pull from wordsList, (should ONLY be used with wordsList)
   private int nextWordIndex;
   
   // keep track of the user's current score
   private int score;
 
   // number of words displayed on the view
   private final int numWordsDisplayed = 5;
 
   private SinglePlayerUI view;
   
   /**
    * Constructs a new SinglePlayerModel that takes in the ID of an animal and background,
    * and also what the difficulty level is. The constructor will initialize the words list
    * and fills in what words the view should display on the screen.
    * 
    * @param animalID, the string ID of a animal that is selected by the user
    * @param backgroudID, the string ID of a background that is selected by the user
    * @param diff, the difficulty level that is selected by the user
    */
   public SinglePlayerModel(final String animalID, final String backgroudID, 
       final States.difficulty diff) {
     
     // generates the words list according to difficulty chosen
     fillWordsList(diff);
     
     //initialize all the fields to default starting values
     wordsDisplayed = new int[numWordsDisplayed];
     nextWordIndex = numWordsDisplayed;
     score = 0;
     currWordIndex = -1;
     currLetterIndex = -1;
 
     // putting first five words into wordsDisplayed
     for (int i = 0; i < numWordsDisplayed; i++) {
       wordsDisplayed[i] = i;
     }
     
     //creates a new singlePlayerUI view to add as observer
     view = new SinglePlayerUI();
     this.addObserver(view);
   }
 
   /*
    * Reads different files according to the difficulty passed in and
    * parsed the words in the chosen file into wordsList.
    * 
    * @param diff, the difficulty level that the user has chosen
    */
   private void fillWordsList(final States.difficulty diff) {
     File f;
     if (diff == States.difficulty.EASY) {
       f = new File("4words.txt");
     } else if (diff == States.difficulty.MEDIUM) {
       f = new File("5words.txt");
     } else {
       f = new File("6words.txt");
     }
 
     //read entire file as string, parsed into array by new line
     try {
       String contents = FileUtils.readFileToString(f);
       String[] parsedWords = contents.split("\n");
       wordsList = parsedWords;
     } catch (IOException e) {
       e.printStackTrace();
     }
 
   }
 
   /**
    * The typedLetter method handles what words and letter the user has
    * typed so far and notify the view to highlight typed letter or fetch 
    * a new word from the wordsList for the view to display accordingly.
    * 
    * @param letter, the letter that the user typed on the Android soft-keyboard
    */
   public final void typedLetter(final char letter) {
     // currently not locked on to a word
     if (currWordIndex == -1) {
       for (int i = 0; i < wordsDisplayed.length; i++) {
         // if any of the first character in wordsDisplayed matched letter
         if (wordsList[wordsDisplayed[i]].charAt(0) == letter) {
           currWordIndex = i;
           currLetterIndex = 1;
           setChanged();
           notifyObservers(States.update.HIGHLIGHT);
           return;
         }
       }
     // locked on to a word being typed (letter == the index of current letter index in the word)
     } else if (wordsList[wordsDisplayed[currWordIndex]].charAt(currLetterIndex) == letter) {
       
       // store length of current word
       int wordLen = wordsList[wordsDisplayed[currWordIndex]].length();
 
       // word is completed after final letter is typed
      if (currLetterIndex + 1 >= wordLen) {
         score += wordLen;
         updateWordsDisplayed();
         currLetterIndex = -1;
         currWordIndex = -1;
       } else {
         currLetterIndex += 1;
         setChanged();
         notifyObservers(States.update.HIGHLIGHT);
       }
       return;
     }
 
     // wrong letter typed
     setChanged();
     notifyObservers(States.update.WRONG_LETTER);
   }
 
   /*
    *  Replace the current word on display with a new word from list.
    *  post: nextWordIndex will always be set to a valid index of wordsList
    */
   private void updateWordsDisplayed() {
     if (nextWordIndex >= wordsList.length) {
       nextWordIndex = 0;
     }
     wordsDisplayed[currWordIndex] = nextWordIndex;
 
     nextWordIndex += 1;
 
     setChanged();
     notifyObservers(States.update.FINISHED_WORD);
   }
 
   /**
    * @return current score of the player
    */
   public final int getScore() {
     return score;
   }
 
   /**
    * @return the string representation of the current word the player is locked to,
    * null if player is not locked to a word
    */
   public final String getCurrWord() {
     if (currWordIndex == -1) {
       return null;
     }
 
     return wordsList[wordsDisplayed[currWordIndex]];
   }
 
   /**
    * @return the index of the word the player is currently locked to within the words displayed
    */
   public final int getCurrWordIndex() {
     return currWordIndex;
   }
 
   /**
    * @return the index of the letter the player is expected to type in the locked word
    */
   public final int getCurrLetterIndex() {
     return currLetterIndex;
   }
 }
