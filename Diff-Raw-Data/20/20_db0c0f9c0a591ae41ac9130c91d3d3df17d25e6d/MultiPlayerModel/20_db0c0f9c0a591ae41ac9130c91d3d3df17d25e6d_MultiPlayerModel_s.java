 package com.example.zootypers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Set;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.RefreshCallback;
 
 /** 
  * 
  * The Model class for Single Player store a list of words for the UI to display.
  * It keeps track of word and letter the user has typed and updates the view accordingly.
  * 
  * @author winglam, nhlien93, dyxliang
  * 
  */
 
 public class MultiPlayerModel extends Observable {
 
 	private static final int QUEUE_TIMEOUT = 50000; // timer set for 50 sec to wait before giving up in queue
 	private static final int SCORE_TIMEOUT = 10000; // timer set to 10 sec to wait for getting opponents score
 	private static final int RECHECK_TIME = 1000; // timer set to 1 sec to wait between checks
 	private static final int LIST_SIZE = 100;
 	// number of words displayed on the view
 	private final int numWordsDisplayed;
 
 	private String name;
 
 	private ParseObject match;
 
 	private Map<String, String> info;
 
 	// stores an array of words 
 	private List<String> wordsList;
 
 	// array of indices that refers to strings inside wordsList
 	private int[] wordsDisplayed;
 
 	private Set<Character> currFirstLetters;
 
 	// index of the next word to pull from wordsList, (should ONLY be used with wordsList)
 	private int nextWordIndex;
 
 	// index of a string inside wordsDisplayed (should NEVER be used on wordsList!)
 	private int currWordIndex;
 
 	// index of letter that has been parsed from the currWordIndex
 	private int currLetterIndex;
 	
 	private int animalName;
 
 	// maximum number of words in wordLists on Parse database
 	private static final int NUMOFWORDS = 709;
 
 	/**
 	 * Constructs a new SinglePlayerModel that takes in the ID of an animal and background,
 	 * and also what the difficulty level is. The constructor will initialize the words list
 	 * and fills in what words the view should display on the screen.
 	 * 
 	 * @param animalID, the string ID of a animal that is selected by the user
 	 * @param backgroudID, the string ID of a background that is selected by the user
 	 * @param diff, the difficulty level that is selected by the user
 	 * @throws ParseException 
 	 */
 	public MultiPlayerModel(int wordsDis, String uname, int animalName) {
 		this.animalName = animalName;
 		this.name = uname;
 		this.info = new HashMap<String, String>();
 		beginMatchMaking();
 		setWordsList();
 		this.numWordsDisplayed = wordsDis;
 		currFirstLetters = new HashSet<Character>();
 		//initialize all the fields to default starting values
 		wordsDisplayed = new int[numWordsDisplayed];
 		nextWordIndex = 0;
 		currLetterIndex = -1;
 		currWordIndex = -1;
 	}
 
 	private void beginMatchMaking() {
 		if (findOpponent()) {
 			setInfo(false);
 			try {
 				match.put("p2name", name);
 				match.put("p2animal", animalName);
 				match.put("p2score", 0);
 				match.put("p2finished", false);
 				match.save();
 			} catch (ParseException e) {
 				// TODO CONNECTION ERROR
 				e.printStackTrace();
 			}
 		} else {
 			addToQueue();
 			if (!checkStatus()) {
 				//TODO throw new NoOneInQueueException
 			}
 		}
 	}
 
 	// checks whether name can be matched to an opponent
 	// returns true if matched, false otherwise
 	private boolean findOpponent() {
 		try {
 			ParseQuery query = new ParseQuery("Matches");
 			query.whereEqualTo("p2name", "");
 			query.whereNotEqualTo("p1name", name);
 			match = query.getFirst();
 			match.refresh();
 			return true;
 		} catch (ParseException e1) {
 			return false;
 		}
 	}
 
 	private void setInfo(boolean isPOne) {
 		if (isPOne) {
 			info.put("name", "p1name");
 			info.put("animal", "p1animal");
 			info.put("score", "p1score");
 			info.put("finished", "p1finished");
 			info.put("oname", "p2name");
 			info.put("oanimal", "p2animal");
 			info.put("oscore", "p2score");
 			info.put("ofinished", "p2finished");
 		} else {
 			info.put("name", "p2name");
 			info.put("animal", "p2animal");
 			info.put("score", "p2score");
 			info.put("finished", "p2finished");
 			info.put("oname", "p1name");
 			info.put("oanimal", "p1animal");
 			info.put("oscore", "p1score");
 			info.put("ofinished", "p1finished");
 		}
 		
 	}
 
 	private void addToQueue() {
 		final int randy = (int) (Math.random() * (NUMOFWORDS));
 		try {
 			setInfo(true);
 			match = new ParseObject("Matches");
 			match.put("p1name", name);
 			match.put("p1animal", animalName);
 			match.put("p1score", 0);
 			match.put("p1finished", false);
 			match.put("p2name", "");
 			match.put("wordIndex", randy);
 			match.save();
 		} catch (ParseException e) {
 			// TODO MAKE EXCEPTION FOR WHEN CANT SAVE
 		}
 	}
 
 	private boolean checkStatus() {
 		long starttime = System.currentTimeMillis();
 		long endtime = starttime + QUEUE_TIMEOUT;
 		while(System.currentTimeMillis() < endtime) {
 			try {
				match.refresh();  		  	
 				if (!match.getString(info.get("oname")).equals("")) {
 					return true;
 				}
 				Thread.sleep(RECHECK_TIME);
 			} catch (ParseException e1) {
 				// TODO THROW EXCEPTION IF NOT CONNECTED TO INTERNET
 			} catch (InterruptedException e) {
 				// TODO SHOULD NEVER HAPPEN
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	// populates wordsList by contacting the database for LIST_SIZE amount of words
 	private void setWordsList() {
 		List<ParseObject> wordObjects = null;
 		try {
//			match.refresh();
 			checkIfInMatch();
 			ParseQuery query = new ParseQuery("WordList");
 			query.setSkip(match.getInt("wordIndex"));
 			query.setLimit(LIST_SIZE); // limit to at most 20 results
 			wordObjects= query.find();
 			if (wordObjects.size() < LIST_SIZE) {
 				ParseQuery query2 = new ParseQuery("WordList");
 				query2.setLimit(LIST_SIZE - wordObjects.size());
 				wordObjects.addAll(query2.find());
 			}
 		} catch (ParseException e1) {
 			// TODO THROW EXCEPTION IF NOT CONNECTED TO INTERNET
 		}	
 		// changing words from parse objects into a list of strings.
 		wordsList = new ArrayList<String>();
 		for (ParseObject o : wordObjects) {
 			wordsList.add(o.getString("word"));
 		}
 	}
 
 	private void checkIfInMatch() {
 		if (!match.getString(info.get("name")).equals(name)) {
 			// TODO YouGotReplacedException
 		}
 	}
 
 	/*
 	 *  Replace the current word on display with a new word from list making
 	 *  sure that the new word will not start with the same letter as any of
 	 *  the other words being displayed.
 	 *  post: nextWordIndex will always be set to a valid index of wordsList
 	 */
 	private void updateWordsDisplayed() {
 		currFirstLetters.remove(wordsList.get(wordsDisplayed[currWordIndex]).charAt(0));
 		while (currFirstLetters.contains(wordsList.get(nextWordIndex).charAt(0))) {
 			nextWordIndex++;
 			if (nextWordIndex >= wordsList.size()) {
 				nextWordIndex = 0;
 			}
 		}
 		currFirstLetters.add(wordsList.get(nextWordIndex).charAt(0));
 		wordsDisplayed[currWordIndex] = nextWordIndex;
 		nextWordIndex++;
 		if (nextWordIndex >= wordsList.size()) {
 			nextWordIndex = 0;
 		}
 
 		match.refreshInBackground(new RefreshCallback() {
 			public void done(ParseObject object, ParseException e) {
 				if (e == null) {
 					setChanged();
 					notifyObservers(States.update.OPPONENT_SCORE);
 				}
 			}
 		});
 		setChanged();
 		notifyObservers(States.update.FINISHED_WORD);
 	}
 
 
 	/**
 	 * The populateDisplayedList method gets called once by MultiPlayer after
 	 * it added itself as an observer of this class. The method populates the
 	 * displayed word list with numWordsDisplayed amount of words. 
 	 */
 	public void populateDisplayedList() {
 		// putting first five words into wordsDisplayed
 		currFirstLetters = new HashSet<Character>();
 		for (int i = 0; i < numWordsDisplayed; i++) {
 			while (currFirstLetters.contains(wordsList.get(nextWordIndex).charAt(0))) {
 				nextWordIndex++;
 			}
 			currFirstLetters.add(wordsList.get(nextWordIndex).charAt(0));
 			wordsDisplayed[i] = nextWordIndex;
 			currWordIndex = i;
 			setChanged();
 			notifyObservers(States.update.FINISHED_WORD);
 		}
 		nextWordIndex++;
 		currWordIndex = -1;
 	}
 
 	public int getOpponentAnimal() {
 		try {
 			match.refresh();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return match.getInt(info.get("oanimal"));
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
 				if (wordsList.get(wordsDisplayed[i]).charAt(0) == letter) {
 					currWordIndex = i;
 					currLetterIndex = 1;
 					setChanged();
 					notifyObservers(States.update.HIGHLIGHT);
 					return;
 				}
 			}
 			// locked on to a word being typed (letter == the index of current letter index in the word)
 		} else if (wordsList.get(wordsDisplayed[currWordIndex]).charAt(currLetterIndex) == letter) {
 
 			// store length of current word
 			int wordLen = wordsList.get(wordsDisplayed[currWordIndex]).trim().length();
 
 			// word is completed after final letter is typed
 			if ((currLetterIndex + 1) >= wordLen) {
 				int curScore = match.getInt(info.get("score"));
 				curScore += wordLen;
 				match.put(info.get("score"), curScore);
 				match.saveInBackground();
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
 
 	/**
 	 * @return current score of the player
 	 */
 	public final int getScore() {
 		return match.getInt(info.get("score"));
 	}
 
 	/**
 	 * @return the string representation of the current word the player is locked to,
 	 * null if player is not locked to a word
 	 */
 	public final String getCurrWord() {
 		if (currWordIndex == -1) {
 			return null;
 		}
 
 		return wordsList.get(wordsDisplayed[currWordIndex]);
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
 
 	public final int getOpponentScore() {
 		return match.getInt(info.get("oscore"));
 	}
 
 	public final void setUserFinish() {
 		try {
 			match.put(info.get("finished"), true);
 			match.save();
 		} catch (ParseException e) {
 			// TODO CONNECTION ERROR
 		}
 	}
 
 	// return true if my opponent has finished their game
 	public final boolean isOpponentFinished() {
 		long starttime = System.currentTimeMillis();
 		long endtime = starttime + SCORE_TIMEOUT;
 		while(System.currentTimeMillis() < endtime) {
 
 			if (match.getBoolean(info.get("ofinished"))) {
 				return true;
 			}
 			try {
 				match.refresh();
 				Thread.sleep(RECHECK_TIME);
 			} catch (ParseException e1) {
 				// TODO CONNECTION ERROR
 				e1.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO NEVER SHOULD HAPPEN
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	public void deleteUser() {
 		try {
 			match.refresh();
 			if (match.getBoolean(info.get("ofinished"))) {
 				match.delete();
 			} else {
 				match.put(info.get("finished"), true);
 				match.save();
 			}
 		} catch (ParseException e) {
 			// TODO CONNECTION ERROR
 		}
 		
 	}
 }
