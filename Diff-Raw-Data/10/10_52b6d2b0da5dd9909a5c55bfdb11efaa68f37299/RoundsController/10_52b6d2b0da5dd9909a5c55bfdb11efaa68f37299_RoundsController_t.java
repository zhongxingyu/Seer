 package org.i4qwee.chgk.trainer.controller.brain.controller;
 
 import org.i4qwee.chgk.trainer.controller.brain.listener.GameStateListener;
 import org.i4qwee.chgk.trainer.controller.brain.manager.GameStateManager;
 import org.i4qwee.chgk.trainer.controller.brain.manager.RoundManager;
 import org.i4qwee.chgk.trainer.model.enums.GameState;
 
 /**
  * User: 4qwee
  * Date: 06.01.12
  * Time: 15:30
  */
 public class RoundsController implements GameStateListener
 {
     private static final RoundsController instance = new RoundsController();
 
     private final GameStateManager gameStateManager = GameStateManager.getInstance();
     private final RoundManager roundManager = RoundManager.getInstance();
 
    private boolean isFinishedOnce = false;

     private RoundsController()
     {
         gameStateManager.addListener(this);
     }
 
     public static RoundsController getInstance()
     {
         return instance;
     }
 
     public void onGameStageChanged(GameState gameState)
     {
        if (isFinishedOnce && gameState == GameState.WAIT_START_TIMER)
             roundManager.setRound(roundManager.getRound() + 1);
        else if (gameState == GameState.FINISHED)
            isFinishedOnce = true;
     }
 }
