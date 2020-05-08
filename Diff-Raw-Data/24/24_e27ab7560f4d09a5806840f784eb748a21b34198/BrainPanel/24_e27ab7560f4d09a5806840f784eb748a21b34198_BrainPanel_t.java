 package org.i4qwee.chgk.trainer.view;
 
 import org.i4qwee.chgk.trainer.controller.time.TimeButtonActionListener;
 
 import javax.swing.*;
 
 /**
  * User: 4qwee
  * Date: 29.10.11
  * Time: 11:10
  */
 public class BrainPanel extends JPanel
 {
     private QuestionPanel questionPanel;
     private TimerButton timerButton;
 
     public BrainPanel()
     {
         BoxLayout mainBoxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
         setLayout(mainBoxLayout);
 
         questionPanel = new QuestionPanel();
         timerButton = new TimerButton();
        timerButton.getTimeButton().addActionListener(new TimeButtonActionListener(questionPanel));
 
         add(questionPanel);
         add(timerButton);
     }
 }
