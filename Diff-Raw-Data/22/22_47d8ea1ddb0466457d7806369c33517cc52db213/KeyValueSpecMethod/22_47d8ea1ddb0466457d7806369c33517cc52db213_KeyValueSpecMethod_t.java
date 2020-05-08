 package pl.edu.agh.to1.dice.logic.players.ai.freezing;
 
import pl.edu.agh.to1.dice.logic.dices.Dice;
import pl.edu.agh.to1.dice.logic.dices.DiceBox;
 import pl.edu.agh.to1.dice.logic.figures.IFigure;
 import pl.edu.agh.to1.dice.logic.players.ai.BotDice;
 import pl.edu.agh.to1.dice.logic.players.ai.BotDiceBox;
 import pl.edu.agh.to1.dice.utils.Pair;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 public class KeyValueSpecMethod extends AbstractSpecMethod {
 
     private final Integer keyValue;
 
     public KeyValueSpecMethod(Integer keyValue) {
         this.keyValue = keyValue;
     }
 
     @Override
     protected Pair<List<Integer>, Double> getValueCountsAndScore(IFigure figure, DiceBox diceBox, List<Integer> toFreeze) {
         List<Integer> valueCounts = new LinkedList<Integer>();
         int freeToRoll = 5 - toFreeze.size(), i;
         for (i = 0; i < freeToRoll/2; i++) valueCounts.add(6);
         for (i = freeToRoll/2; i < freeToRoll; i++) valueCounts.add(1);
         BotDiceBox probable = new BotDiceBox(5);
         List<Dice> dices = probable.getDices();
         for (i = 0; i < toFreeze.size(); i++) {
             ((BotDice)dices.get(i)).setScore(keyValue);
         }
         int j = 0;
         while (j < freeToRoll/2) {
             ((BotDice)dices.get(i++)).setScore(6);
             j++;
         }
         while (i < toFreeze.size()) {
             ((BotDice)dices.get(i++)).setScore(1);
         }
         if (freeToRoll % 2 != 0) {
             ((BotDice)dices.get(toFreeze.size()-1)).setScore((int)3.5 * freeToRoll - probable.sum() + 1);
         }
 
         return new Pair<List<Integer>, Double>(valueCounts, (double) figure.getScore(probable));
     }
 
     @Override
     protected List<Integer> getIndexesToFreeze(IFigure figure, DiceBox diceBox) {
         final List<Integer> toFreeze = new LinkedList<Integer>();
         int id = 0;
         for (Dice dice : diceBox) {
             if (dice.getScore() == keyValue) {
                 toFreeze.add(id);
             }
             id++;
         }
         return toFreeze;
     }
 
     public Integer getKeyValue() {
         return keyValue;
     }
 }
