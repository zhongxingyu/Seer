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
        questions.add("Рядом с берегом со спущенной на воду веревочной лестницей стоит корабль. У лестницы 10 ступенек.Расстояние между ступеньками 30 см.Cамая нижняя ступенька касается поверхности воды.Океан сегодня очень спокоен,но начинается прилив,который поднимает воду за час на 15 см. Через сколько времени покроется водой третья ступенька веревочной лестницы? "); 
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
