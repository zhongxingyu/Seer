 package org.i4qwee.chgk.trainer.view;
 
 import org.i4qwee.chgk.trainer.controller.brain.ScoreManagerSingleton;
 import org.i4qwee.chgk.trainer.controller.questions.GameStateSingleton;
 import org.i4qwee.chgk.trainer.model.enums.AnswerSide;
 import org.i4qwee.chgk.trainer.model.enums.GameState;
 import org.i4qwee.chgk.trainer.model.events.NamesChangedEvent;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseListener;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * User: 4qwee
  * Date: 29.10.11
  * Time: 13:09
  */
 public class ScorePanel extends JPanel implements Observer
 {
     public static final int MAX_HEIGHT = 100;
 
     SingleScorePanel leftScorePanel;
     SingleScorePanel rightScorePanel;
 
     public ScorePanel(TimerButtonPanel timerButtonPanel)
     {
         super();
         ScoreManagerSingleton.getInstance().addObserver(this);
         GameStateSingleton.getInstance().addObserver(this);
 
         BoxLayout boxLayout = new BoxLayout(this, BoxLayout.X_AXIS);
         setLayout(boxLayout);
 
         setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_HEIGHT));
 
         leftScorePanel = new SingleScorePanel();
         rightScorePanel = new SingleScorePanel();
 
         add(leftScorePanel);
         add(timerButtonPanel);
         add(rightScorePanel);
     }
 
     public void addMouseListener(MouseListener mouseListener)
     {
         super.addMouseListener(mouseListener);
         leftScorePanel.addMouseListener(mouseListener);
         rightScorePanel.addMouseListener(mouseListener);
     }
 
     public void update(Observable o, Object arg)
     {
         if (arg != null)
         {
             if (arg instanceof AnswerSide)
             {
                 switch ((AnswerSide) arg)
                 {
                     case LEFT:
                         selectSingleScorePanel(true);
                         break;
                     case RIGHT:
                         selectSingleScorePanel(false);
                         break;
                 }
             }
             else if (arg instanceof GameState)
             {
                 if (arg == GameState.FINISHED || arg == GameState.RUNNING)
                     removeSelection();
             }
             else if (arg instanceof NamesChangedEvent)
             {
                 setNames(((NamesChangedEvent) arg).getLeftName(), ((NamesChangedEvent) arg).getRightName());
             }
         }
         else
         {
            switch (ScoreManagerSingleton.getInstance().getAnswerSide())
            {
                case LEFT:
                    leftScorePanel.setScore(ScoreManagerSingleton.getInstance().getLeftScore());
                    break;
                case RIGHT:
                    rightScorePanel.setScore(ScoreManagerSingleton.getInstance().getRightScore());
                    break;
            }
         }
     }
 
     private void selectSingleScorePanel(boolean isLeft)
     {
         SingleScorePanel singleScorePanel;
 
         if (isLeft)
             singleScorePanel = leftScorePanel;
         else
             singleScorePanel = rightScorePanel;
 
         singleScorePanel.setBackground(Color.magenta);
     }
 
     private void removeSelection()
     {
         leftScorePanel.setBackground(Color.white);
         rightScorePanel.setBackground(Color.white);
     }
 
     public void setNames(String leftName, String rightName)
     {
         leftScorePanel.setName(leftName);
         rightScorePanel.setName(rightName);
     }
 
     public String getLeftName()
     {
         return leftScorePanel.getName();
     }
 
     public String getRightName()
     {
         return rightScorePanel.getName();
     }
 }
