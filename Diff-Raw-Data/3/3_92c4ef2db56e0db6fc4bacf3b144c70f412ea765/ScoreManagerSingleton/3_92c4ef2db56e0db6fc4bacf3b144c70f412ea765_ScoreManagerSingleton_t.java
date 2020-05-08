 package org.i4qwee.chgk.trainer.controller.brain;
 
 import org.i4qwee.chgk.trainer.controller.brain.manager.*;
 import org.i4qwee.chgk.trainer.model.enums.AnswerState;
 import org.i4qwee.chgk.trainer.model.enums.GameState;
 
 import java.util.Observable;
 
 /**
  * User: 4qwee
  * Date: 30.10.11
  * Time: 20:36
  */
 public class ScoreManagerSingleton extends Observable
 {
     private static ScoreManagerSingleton ourInstance = new ScoreManagerSingleton();
 
     private final ScoreManager scoreManager = ScoreManager.getInstance();
     private final PriceManager priceManager = PriceManager.getInstance();
     private final RoundManager roundManager = RoundManager.getInstance();
     private final AnswerSideManager answerSideManager = AnswerSideManager.getInstance();
     private final AnswerStateManager answerStateManager = AnswerStateManager.getInstance();
     private final GameStateManager gameStateManager = GameStateManager.getInstance();
 
     public static ScoreManagerSingleton getInstance()
     {
         return ourInstance;
     }
 
     private ScoreManagerSingleton()
     {
     }
 
     public void setFalseStart()
     {
         answerStateManager.setAnswerState(AnswerState.ONE_ANSWERED);
         gameStateManager.setGameState(GameState.RUNNING);
     }
 
     public void answer(boolean isCorrect)
     {
         if (isCorrect)
         {
             switch (answerSideManager.getAnswerSide())
             {
                 case LEFT:
                     scoreManager.increaseLeftScore(priceManager.getPrice());
                     break;
                 case RIGHT:
                     scoreManager.increaseRightScore(priceManager.getPrice());
                     break;
             }
 
             priceManager.setPrice(1);
             answerStateManager.setAnswerState(AnswerState.NOBODY_ANSWERED);
             gameStateManager.setGameState(GameState.FINISHED);
             setChanged();
             notifyObservers();
         }
         else
         {
             switch (answerStateManager.getAnswerState())
             {
                 case NOBODY_ANSWERED:
                     answerStateManager.setAnswerState(AnswerState.ONE_ANSWERED);
                     gameStateManager.setGameState(GameState.RUNNING);
                     break;
                 case ONE_ANSWERED:
                     noOneAnswered();
                     break;
             }
         }
     }
 
     public void noOneAnswered()
     {
         answerStateManager.setAnswerState(AnswerState.NOBODY_ANSWERED);
         priceManager.setPrice(priceManager.getPrice() + 1);
        gameStateManager.setGameState(GameState.FINISHED);
     }
 
     public void newGame()
     {
         gameStateManager.setGameState(GameState.INIT);
         roundManager.setRound(1);
 
         scoreManager.setLeftScore(0);
         scoreManager.setRightScore(0);
 
         priceManager.setPrice(1);
     }
 }
