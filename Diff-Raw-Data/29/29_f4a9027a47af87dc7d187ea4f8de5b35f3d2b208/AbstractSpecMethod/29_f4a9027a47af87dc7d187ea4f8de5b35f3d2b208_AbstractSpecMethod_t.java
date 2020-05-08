 package pl.edu.agh.to1.dice.logic.players.ai.freezing;
 
 import pl.edu.agh.to1.dice.logic.DiceBox;
 import pl.edu.agh.to1.dice.logic.figures.IFigure;
 import pl.edu.agh.to1.dice.utils.Pair;
 
 import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 public abstract class AbstractSpecMethod implements ISpecMethod {
     @Override
     public Pair<List<Integer>, Double> computeQuality(IFigure figure, DiceBox diceBox) {
         final List<Integer> toFreeze = getIndexesToFreeze(figure, diceBox);
         final Pair<List<Integer>, Double> countsAndScore = getValueCountsAndScore(figure, diceBox, toFreeze);
         final List<Integer> possibleValueCounts = countsAndScore.getLeft();
         final double possibleScore = countsAndScore.getRight();
 
         double probability = 1.;
         int nonZero = 0;
         for (Integer count : possibleValueCounts) {
            if (count != 0) {
                nonZero++;
                probability *= (double) count / 6.;
            }
         }
         probability *= (double) factorial(nonZero);
 
         return new Pair<List<Integer>, Double>(toFreeze, possibleScore * probability);
     }
 
     private int factorial(int n) {
         int ret = 1;
         for (int i = 1; i <= n; i++) {
             ret *= i;
         }
         return ret;
     }
 
 
     protected abstract Pair<List<Integer>, Double> getValueCountsAndScore(IFigure figure, DiceBox diceBox, List<Integer> toFreeze);
 
     protected abstract List<Integer> getIndexesToFreeze(IFigure figure, DiceBox diceBox);
 }
