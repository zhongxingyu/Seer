 package it.chalmers.tendu.gamemodel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.defaults.GameIds;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Timer;
 
 public class NumberGame extends MiniGame {
 
 	private ArrayList<Integer> answerList;
 	private ArrayList<Integer> answerAndDummyList;
 	private int nbrCorrectAnswer;
 
 	// private HashMap<Player, List<Integer>>
 
 	public NumberGame(int addTime, Difficulty difficulty) {
 		super(addTime, difficulty, GameIds.NUMBER_GAME);
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
 		answerAndDummyList = divideAndConquer(answerList);
 	}
 
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
 
 	// TODO divide answerlist between players and populate with dummy numbers.
 
 	private ArrayList<Integer> divideAndConquer(ArrayList<Integer> list) {
		ArrayList<Integer> newList = new ArrayList();
 		for (int i = 0; i < list.size(); i++) {
 			newList.add(list.get(i));
 		}
 		int i = 0;
 		while (i < (8 - list.size())) {
 			int randomNbr = 1 + (int) (Math.random() * 99);
 			if (!(list.contains(randomNbr))) {
 				newList.add(randomNbr);
 				i++;
 			}
 		}
 		// TODO populate list with original number and dummy numbers.
 		Collections.shuffle(newList);
 		return newList;
 	}
 
 	public ArrayList<Integer> getAnswerList() {
 		return answerList;
 	}
 
 	public ArrayList<Integer> getDummyList() {
 		return answerAndDummyList;
 	}
 	
 }
