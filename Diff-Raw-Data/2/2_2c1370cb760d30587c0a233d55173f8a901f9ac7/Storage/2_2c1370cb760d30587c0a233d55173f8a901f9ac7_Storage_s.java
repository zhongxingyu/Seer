 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package asdlks.sd;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author user
  */
 public class Storage {
     ArrayList <String> answers;
     ArrayList <String> questions;
     
     int currText;
    
     Storage() {
         currText = 0;
         answers = new ArrayList <String>();
         questions = new ArrayList <String>();
         
         answers.add("ans");
         answers.add("ans1");
         answers.add("ans2");
         answers.add("ans3");
         
         questions.add("quest");   
        questions.add("quest1"); 
         questions.add("quest2"); 
         questions.add("quest3"); 
     }
     
     public String getCurrAnswer() {
         return answers.get(currText);
     }
     public String getCurrQuection() {
         return questions.get(currText);
     }
     public void moveLeft() {
         if (currText > 0)
             currText--;
     }
     public void moveRight() {
         if (currText < answers.size() - 1)
             currText++;
     }
     
 }
