 package org.i4qwee.chgk.trainer.controller.time;
 
 import org.i4qwee.chgk.trainer.model.GameState;
import org.i4qwee.chgk.trainer.model.GameStateSingleton;
 import org.i4qwee.chgk.trainer.view.TimerButton;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * User: 4qwee
  * Date: 27.10.11
  * Time: 7:23
  */
 public class Timer implements Observer
 {
     private static final int TIMER_DELAY = 13;
 
     private javax.swing.Timer timer;
     private int time;
     private JButton timeButton;
 
     public Timer(TimerButton timerButton)
     {
         this.timeButton = timerButton.getTimeButton();
        GameStateSingleton.getInstance().addObserver(this);
 
         timer = new javax.swing.Timer(TIMER_DELAY, new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 time += TIMER_DELAY;
                 setTimeLabelText();
             }
         });
     }
 
     private void setTimeLabelText()
     {
         long tmpTime = time % 10L;
         String timeText = Long.toString(tmpTime);
         tmpTime = (time % 100L - time % 10L) / 10L;
         timeText = Long.toString(tmpTime) + timeText;
         tmpTime = (time % 1000L - time % 100L) / 100L;
         timeText = ":" + Long.toString(tmpTime) + timeText;
         tmpTime = (time % 10000L - time % 1000L) / 1000L;
         timeText = Long.toString(tmpTime) + timeText;
         tmpTime = (time % 60000L - time % 10000L) / 10000L;
         timeText = ":" + Long.toString(tmpTime) + timeText;
         tmpTime = (time % 600000L - time % 60000L) / 60000L;
         timeText = Long.toString(tmpTime) + timeText;
 
         timeButton.setText(timeText);
     }
 
     public void timerStart()
     {
         timer.start();
     }
 
     public void timerStop()
     {
         timer.stop();
     }
 
     public void timerRestart()
     {
         timer.stop();
         time = 0;
         timeButton.setText(TimerButton.INIT_TEXT);
     }
 
     public void update(Observable o, Object arg)
     {
         if (!(arg instanceof GameState))
             return;
 
         GameState gameState = (GameState) arg;
 
         switch (gameState)
         {
             case INIT:
                 break;
             case WAIT_START_TIMER:
                 timerRestart();
                 break;
             case RUNNING:
                 timerStart();
                 break;
             case PAUSED:
                 timerStop();
                 break;
             case FINISHED:
                 timerStop();
                 break;
         }
     }
 }
