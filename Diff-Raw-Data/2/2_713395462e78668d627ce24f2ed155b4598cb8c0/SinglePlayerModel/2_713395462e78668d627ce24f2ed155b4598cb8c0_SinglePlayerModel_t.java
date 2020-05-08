 package com.example.zootypers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Set;
 import org.apache.commons.io.IOUtils;
 import android.content.res.AssetManager;
 
 /** 
  * 
  * The Model class for Single Player store a list of words for the UI to display.
  * It keeps track of word and letter the user has typed and updates the view accordingly.
  * 
  * @author winglam, nhlien93, dyxliang
  * 
  */
 
 public class SinglePlayerModel extends Observable {
 
 	// number of words displayed on the view
 	private final int numWordsDisplayed;
 	
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
 
 	// allows files in assets to be accessed.
 	private AssetManager am;
 	
 	private Set<Character> currFirstLetters;
 
 	/**
 	 * Constructs a new SinglePlayerModel that takes in the ID of an animal and background,
 	 * and also what the difficulty level is. The constructor will initialize the words list
 	 * and fills in what words the view should display on the screen.
 	 * 
 	 * @param animalID, the string ID of a animal that is selected by the user
 	 * @param backgroudID, the string ID of a background that is selected by the user
 	 * @param diff, the difficulty level that is selected by the user
 	 */
 	public SinglePlayerModel(final States.difficulty diff, AssetManager am, int wordsDis) {
 		this.numWordsDisplayed = wordsDis;
 		this.am = am;
 		// generates the words list according to difficulty chosen
 		getWordsList(diff);
 
 		//initialize all the fields to default starting values
 		wordsDisplayed = new int[numWordsDisplayed];
 		nextWordIndex = 0;
 		score = 0;
 		currLetterIndex = -1;
 		currWordIndex = -1;
 	}
 
 	/*
 	 * Reads different files according to the difficulty passed in,
 	 * parsed the words in the chosen file into wordsList, and shuffles
 	 * the words in the list.
 	 * 
 	 * @param diff, the difficulty level that the user has chosen
 	 */
 	private void getWordsList(final States.difficulty diff) {
 		String file;
 		if (diff == States.difficulty.EASY) {
 			file = "4words.txt";
 		} else if (diff == States.difficulty.MEDIUM) {
 			file = "5words.txt";
 		} else {
 			file = "6words.txt";
 		}
 
 		// read entire file as string, parsed into array by new line
 		try {
 			InputStream stream = am.open(file);
 			String contents = IOUtils.toString(stream, "UTF-8");
 			wordsList = contents.split(System.getProperty("line.separator"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Shuffle the elements in the array
 		Collections.shuffle(Arrays.asList(wordsList));
 	}
 
 	/**
 	 * The populateDisplayedList method gets called once by SinglePlayer after
 	 * it added itself as an observer of this class.
 	 */
 	public void populateDisplayedList() {
 		// putting first five words into wordsDisplayed
 		currFirstLetters = new HashSet<Character>();
 		for (int i = 0; i < numWordsDisplayed; i++) {
 			while (currFirstLetters.contains(wordsList[nextWordIndex].charAt(0))) {
 				nextWordIndex++;
 			}
 			currFirstLetters.add(wordsList[nextWordIndex].charAt(0));
 			wordsDisplayed[i] = nextWordIndex;
 			currWordIndex = i;
 			setChanged();
 			notifyObservers(States.update.FINISHED_WORD);
 		}
 		nextWordIndex++;
 		currWordIndex = -1;
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
			int wordLen = wordsList[wordsDisplayed[currWordIndex]].trim().length();
 
 			// word is completed after final letter is typed
 			if ((currLetterIndex + 1) >= wordLen) {
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
 	 *  Replace the current word on display with a new word from list making
 	 *  sure that the new word will not start with the same letter as any of
 	 *  the other words being displayed.
 	 *  post: nextWordIndex will always be set to a valid index of wordsList
 	 */
 	private void updateWordsDisplayed() {
 		currFirstLetters.remove(wordsList[wordsDisplayed[currWordIndex]].charAt(0));
 		while (currFirstLetters.contains(wordsList[nextWordIndex].charAt(0))) {
 			nextWordIndex++;
 			if (nextWordIndex >= wordsList.length) {
 				nextWordIndex = 0;
 			}
 		}
 		currFirstLetters.add(wordsList[nextWordIndex].charAt(0));
 		wordsDisplayed[currWordIndex] = nextWordIndex;
 		nextWordIndex++;
 		if (nextWordIndex >= wordsList.length) {
 			nextWordIndex = 0;
 		}
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
