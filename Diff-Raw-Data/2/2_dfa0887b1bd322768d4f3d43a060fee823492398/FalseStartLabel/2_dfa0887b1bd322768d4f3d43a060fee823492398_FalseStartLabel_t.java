 package org.i4qwee.chgk.trainer.view;
 
 import org.i4qwee.chgk.trainer.controller.brain.manager.AnswerSideManager;
 import org.i4qwee.chgk.trainer.model.enums.AnswerSide;
 
 import javax.swing.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: 4qwee
  * Date: 10/23/12
  * Time: 9:19 AM
  */
 public class FalseStartLabel extends JLabel
 {
     private static FalseStartLabel instance = new FalseStartLabel();
 
     private FalseStartLabel()
     {
         super();
        setFont(DefaultUIProvider.getQuestionPriceFont());
     }
 
     public static FalseStartLabel getInstance()
     {
         return instance;
     }
 
     public void setFalseStart(AnswerSide answerSide)
     {
         String name = AnswerSideManager.getInstance().getAnswersName();
 
         String message = "Фальстарт у ";
 
         if (name != null)
             message += name;
         else
             message += answerSide.toString();
 
         setText(message);
     }
 
     public void clear()
     {
         setText("");
     }
 }
