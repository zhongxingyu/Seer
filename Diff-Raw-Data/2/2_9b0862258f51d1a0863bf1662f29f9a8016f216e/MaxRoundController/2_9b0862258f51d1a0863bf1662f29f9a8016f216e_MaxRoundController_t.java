 package org.i4qwee.chgk.trainer.controller.brain.controller;
 
 import org.i4qwee.chgk.trainer.controller.brain.ScoreManagerSingleton;
 import org.i4qwee.chgk.trainer.controller.brain.listener.RoundListener;
 import org.i4qwee.chgk.trainer.controller.brain.manager.NamesManager;
 import org.i4qwee.chgk.trainer.controller.brain.manager.RoundManager;
 import org.i4qwee.chgk.trainer.controller.brain.manager.ScoreManager;
 
 import javax.swing.*;
 
 /**
  * User: 4qwee
  * Date: 06.01.12
  * Time: 13:08
  */
 public class MaxRoundController implements RoundListener
 {
     private static final MaxRoundController instance = new MaxRoundController();
 
     private final RoundManager roundManager = RoundManager.getInstance();
     private final ScoreManager scoreManager = ScoreManager.getInstance();
     private final NamesManager namesManager = NamesManager.getInstance();
 
     private MaxRoundController()
     {
         roundManager.addListener(this);
     }
 
     public static MaxRoundController getInstance()
     {
         return instance;
     }
 
     public void onRoundChanged(int round, int maxRound)
     {
        if (maxRound != 0 && round > maxRound)
         {
             String leftName = namesManager.getLeftName();
             
             if (leftName == null || leftName.equals(""))
                 leftName = "Левый";
             
             String rightName = namesManager.getRightName();
             
             if (rightName == null || rightName.equals(""))
                 rightName = "Правый";
 
             if (scoreManager.getLeftScore() > scoreManager.getRightScore())
                 JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), leftName + " выиграл!", "", JOptionPane.PLAIN_MESSAGE);
             else if (scoreManager.getLeftScore() < scoreManager.getRightScore())
                 JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), rightName + " выиграл!", "", JOptionPane.PLAIN_MESSAGE);
             else
                 JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Ничья!", "", JOptionPane.PLAIN_MESSAGE);
 
             ScoreManagerSingleton.getInstance().newGame();
         }
     }
 }
