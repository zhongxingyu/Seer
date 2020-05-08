 package it.chalmers.tendu.gamemodel;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 
 /*TODO This class shall select a random minigame with the selected difficulty
  *It shall also keep track of previously played games to avoid playing the
  *same game (on the specified difficulty) more than once;
  */
 public class MiniGameFactory {
 
 	public static MiniGame createMiniGame(int addTime, Difficulty difficulty) {
 		MiniGame miniGame = null;
 		switch (difficulty) {
 		case ONE:
 			miniGame = new NumberGame(addTime, Difficulty.ONE);
 		case TWO:
 //			miniGame = new ShapesGame(addTime, Difficulty.TWO);
 			miniGame = new NumberGame(addTime, Difficulty.TWO);
 		case THREE:
 			break;
 		case FOUR:
 			break;
 		case FIVE:
 			break;
 		}
 		return miniGame;
 
 	}
 }
