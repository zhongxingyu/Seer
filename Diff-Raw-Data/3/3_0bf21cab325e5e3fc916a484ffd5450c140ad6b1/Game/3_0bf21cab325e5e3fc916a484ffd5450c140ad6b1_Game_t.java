 package com.example.mastermind;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 public class Game {
 	private Context context;
 	private String[] colors = {"red", "orange", "yellow", "green", "blue", "violet", "blank"};
 	//private ArrayList<String> answer = new ArrayList<String>();
 	//private ArrayList<String> guesses = new ArrayList<String>();
 	private String[] answer;
 	private String[] guesses;
 	private Integer round = 0;
 	private Integer choice = 0;
 	private Result result = new Result();
 	private Boolean solved = false;
 	private Boolean gameOver = false;
 	private Boolean optionDup = false, optionBlank = false;
 	private Integer numGuesses = 0, guessLength = 0;
 	private ButtonColorManager bcm;
 
 	public Game(Context context) {
 		this.context = context;
 		bcm = new ButtonColorManager(this.context);
 	}
 	
 	public String[] getColors() {
 		return colors;
 	}
 	
 	public void setColors(String[] colors) {
 		this.colors = colors;
 	}
 
 	public String[] getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(String[] answer) {
 		this.answer = answer;
 	}
 
 	public String[] getGuesses() {
 		return guesses;
 	}
 
 	public void setGuesses(String[] guesses) {
 		this.guesses = guesses;
 	}
 
 	public Integer getRound() {
 		return round;
 	}
 
 	public void setRound(Integer round) {
 		this.round = round;
 	}
 
 	public Integer getChoice() {
 		return choice;
 	}
 	
 	public void setChoice(Integer choice) {
 		this.choice = choice;
 	}
 	
 	public Result getResult() {
 		return result;
 	}
 
 	public void setResult(Result result) {
 		this.result = result;
 	}
 
 	public Boolean getSolved() {
 		return solved;
 	}
 
 	public void setSolved(Boolean solved) {
 		this.solved = solved;
 	}
 
 	public Boolean getGameOver() {
 		return gameOver;
 	}
 
 	public void setGameOver(Boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 	
 	// game utilities	
 	private int getRandomInt(Integer min, Integer max) {
 		Random random = new Random();
 		int myInt = random.nextInt(max) + min;
 		return myInt;
 		//return Math.floor(Math.random() * (max - min + 1)) + min;
 	}
 	
 	private void chooseAnswer(Boolean dup, Boolean blank) {
 		ArrayList<Integer> available = new ArrayList<Integer>();
 		int max = (blank)? 7 : 6;
 		for (int i=0;i<max;i++) {
 			available.add(i);
 		}
 		int chosen;
 
 		for (int i=0; i<guessLength; i++) {
 			chosen = getRandomInt(1, available.size());
 			int indexToGet = available.get(chosen-1);
 			this.answer[i] = this.colors[indexToGet];
 			if (!dup) {
 				//available.splice(chosen-1, 1);
 				available.remove(chosen-1);
 			}
 		}
 	}
 	public void startGame(Integer numGuesses, Integer guessLength, Integer numChoices, Boolean allowDupes, Boolean allowBlanks) {
 		this.numGuesses = numGuesses;
 		this.guessLength = guessLength;
 		this.optionDup = allowDupes;
 		this.optionBlank = allowBlanks;
 		this.round = 0;
 		this.choice = 0;
 		this.answer = new String[guessLength];
 		this.guesses = new String[guessLength];
 		chooseAnswer(optionDup, optionBlank);
 		clearHints();
 		this.solved = false;
 		this.gameOver = false;
 		this.result = new Result();
 	}
 	private void clearHints() {
 		// TODO Hmmm, need a hint class?
 	}
 	
 	public void nextRound() {
 		if (solved || gameOver) {
 			return;
 		} else {
 			//increment the round
 			this.round += 1;
 			//display the next round (happens in gameactivity on submit currently
 			
 			//reset some variables
 			this.choice = 0;
 			// initialize the guesses
 			for (int i=0; i<guessLength; i++) {
 				this.guesses[i] = "";
 			}
 			this.result = new Result();
 			clearHints();
 		}
 	}
 	
 	// this is for apk<11
 	// TODO for now, infer that the first available position is what they want to populate
 	public void userChoice(Integer colorViewId, ViewGroup currentGuessRow) {
 		if (solved || gameOver) { // Button to play again
 			Toast.makeText(context, "Game over. Play again?", Toast.LENGTH_LONG).show();
 		} else if (choice>=guessLength) {
 			Toast.makeText(context, "You've filled up your guess. Hit the submit button.", Toast.LENGTH_LONG).show();
 		} else {
 			// we always allow duplicates in the guesses
 			clearHints();
 			// TODO any way to get this on evaluate?
 			String colorName = bcm.getColorNameFromViewId(colorViewId);
 			guesses[choice] = colorName;
 			// put that color in the current guess position (choice)
 			View currentChoice = currentGuessRow.getChildAt(choice);
 			int colorId = bcm.getColorIdFromViewId(colorViewId);
 			bcm.setBackground(currentChoice, colorId);
 			// increment the choice
 			choice += 1;
 		}
 	}
 	
 	// this is for apk>=11
 	public void userChoice(Integer colorViewId, Integer guessPositionId) {
 		if (solved || gameOver) { // Button to play again
 			Toast.makeText(context, "Game over. Play again?", Toast.LENGTH_LONG).show();
 		} else if (choice>=guessLength) {
 			Toast.makeText(context, "You've filled up your guess. Hit the submit button.", Toast.LENGTH_LONG).show();
 		} else {
 			// we always allow duplicates in the guesses
 			clearHints();
 			String colorName = bcm.getColorNameFromViewId(colorViewId);
 			GameActivity parent = (GameActivity) context;
 			int i;
 			for (i=1; i<=guessLength;i++) {
 				ViewGroup guessRow = (ViewGroup) parent.findViewById(R.id.game_guessRow1);
 				if (guessPositionId == guessRow.getChildAt(i-1).getId()) {
 					break;
 				}
 			}
 			int guessPosition = i-1;
 			guesses[guessPosition] = colorName;
 			choice += 1;
 		}
 	}
 	
 	public void evaluate() {
 		int i, j, k;
 		String[] answerCopy = answer.clone();
 		clearHints();
 		
 		if (solved||gameOver) {
 			//shouldn't happen because submit button should disappear
 			return;
 		}
 		
 		// process all the possible blacks first
 		for (i=answerCopy.length; i>0; i--) {
 			if (guesses[i-1] == answerCopy[i-1]) {
 				result.setBlack(result.getBlack()+1);
 				guesses[i-1] = "";
 				answerCopy[i-1] = "";
 			}
 		}
 
 		// then process any remaining whites
 		for (j = guessLength - result.getBlack(); j>0; j--) {
 			String colorToFind = guesses[j-1];
			if (colorToFind == "") {
				continue;
			}
 			for (k=0; k<answerCopy.length; k++) {
 				if (answerCopy[k] == colorToFind) {
 					result.setWhite(result.getWhite()+1);
 					// don't mess up the position of answers, just replace match with ""
 					answerCopy[k] = "";
 					break;
 				}
 			}
 		}
 	}
 	
 }
