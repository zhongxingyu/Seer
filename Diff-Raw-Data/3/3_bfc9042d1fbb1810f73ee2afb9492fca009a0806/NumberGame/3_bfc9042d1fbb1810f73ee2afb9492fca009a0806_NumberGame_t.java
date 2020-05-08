 package it.chalmers.tendu.gamemodel.numbergame;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.MiniGame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Timer;
 
 public class NumberGame extends MiniGame {
 
 	private static int PLAYER_COUNT = 4;
 	private ArrayList<Integer> answerList;
 	private Map<Integer, ArrayList<Integer>> playerLists;
 	private int nbrCorrectAnswer;
 
 	public NumberGame(int addTime, Difficulty difficulty) {
 		super(addTime, difficulty, GameId.NUMBER_GAME);
 		nbrCorrectAnswer = 0;
 		switch (difficulty) {
 		case ONE:
 			this.addTime(30);
 			answerList = createAnswer(4);
 			break;
 		case TWO:
 			this.addTime(30);
 			answerList = createAnswer(8);
 			break;
 		default:
 			// TODO:
 			Gdx.app.debug("NumberGame Class", "Fix this switch case");
 		}
 		playerLists = divideAndConquer(answerList);
 	}
 
 	/**
 	 * Check if the number chosen is the right one according to the answerList
 	 * and sets gamestate to gameWon if all the numbers in answerList have been
 	 * correctly guessed.
 	 * 
 	 * @param num
 	 * @return
 	 */
 	public boolean checkNbr(int num) {
 		// TODO make sure it can't go out of bounds (make it prettier)
 		if (nbrCorrectAnswer < answerList.size()) {
 			if (answerList.get(nbrCorrectAnswer) == num) {
 				nbrCorrectAnswer++;
 				if (nbrCorrectAnswer == answerList.size()) {
 					gameWon();
 				}
 				return true;
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a list with random numbers from 1-99 that represents the correct
 	 * answer in the game.
 	 * 
 	 * @param length
 	 * @return
 	 */
 	private ArrayList<Integer> createAnswer(int length) {
 		ArrayList<Integer> answerList = new ArrayList<Integer>();
 		int i = 0;
 		while (i < length) {
 			int randomNbr = 1 + (int) (Math.random() * 99);
 			if (!(answerList.contains(randomNbr))) {
 				answerList.add(randomNbr);
 				i++;
 			}
 		}
 		return answerList;
 	}
 
 	/**
 	 * Give each player a part of the answer.
 	 * 
 	 * @param list
 	 * @return
 	 */
 	private Map<Integer, ArrayList<Integer>> divideAndConquer(
 			ArrayList<Integer> list) {
 
 		Map<Integer, ArrayList<Integer>> newMap = new HashMap<Integer, ArrayList<Integer>>();
 
 		ArrayList<Integer> temp = new ArrayList<Integer>(answerList);
 
 		Collections.shuffle(temp);
 
 		for (int i = 0; i < PLAYER_COUNT; i++) {
 			ArrayList<Integer> newList = new ArrayList<Integer>();
 
 			for (int j = 0; j < answerList.size() / PLAYER_COUNT; j++) {
 				Integer r = temp.remove(0);
 				newList.add(r);
 			}
 			popAndShuffleList(newList);
 			newMap.put(i, newList);
 		}
 		return newMap;
 
 	}
 
 	/**
 	 * Fills up an array with random numbers until there are eight different
 	 * numbers in the array total and shuffles them.
 	 * 
 	 * @param list
 	 */
 	private void popAndShuffleList(ArrayList<Integer> list) {
 		int i = 0;
		int length = list.size();
		while (i < (8 - length)) {
 			int randomNbr = 1 + (int) (Math.random() * 99);
 			if (!(list.contains(randomNbr))) {
 				list.add(randomNbr);
 				i++;
 			}
 		}
 		Collections.shuffle(list);
 	}
 
 	public ArrayList<Integer> getAnswerList() {
 		return answerList;
 	}
 	
 	/**
 	 * Return the indicated players list of numbers.
 	 * @param player
 	 * @return
 	 */
 	public ArrayList<Integer> getPlayerList(int player){
 		return playerLists.get(player);
 	}
 }
