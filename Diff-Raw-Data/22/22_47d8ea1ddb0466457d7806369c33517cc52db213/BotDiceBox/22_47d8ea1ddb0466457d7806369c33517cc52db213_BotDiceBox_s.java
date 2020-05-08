 package pl.edu.agh.to1.dice.logic.players.ai;
 
import pl.edu.agh.to1.dice.logic.Dice;
import pl.edu.agh.to1.dice.logic.DiceBox;
 
 import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 public class BotDiceBox extends DiceBox {
     public BotDiceBox(int amount) {
         super(0);
         for (int i = 0; i < amount; i++) {
             dices.add(new BotDice());
         }
     }
 
     public List<Dice> getDices() {
         return dices;
     }
 }
