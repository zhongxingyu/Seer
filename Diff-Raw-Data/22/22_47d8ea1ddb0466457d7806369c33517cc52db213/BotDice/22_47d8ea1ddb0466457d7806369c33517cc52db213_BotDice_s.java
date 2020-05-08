 package pl.edu.agh.to1.dice.logic.players.ai;
 
import pl.edu.agh.to1.dice.logic.Dice;
 
 /**
  * Class inheriting from Dice, provides simple api for ai methods
  * Author: Piotr Turek
  */
 public class BotDice extends Dice {
     public void setScore(int score) {
         this.score = score;
     }
 }
