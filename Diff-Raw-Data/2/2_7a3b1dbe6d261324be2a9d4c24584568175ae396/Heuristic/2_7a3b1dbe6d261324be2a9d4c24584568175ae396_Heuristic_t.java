 package src.pathfinder.core.util;
 
 /**
  * Author: Tom
  * Date: 03/09/13
  * Time: 00:47
  */
 public enum Heuristic {
     MANHATTAN(new HeuristicAlgorithm() {
         @Override
         public double getCost(int a, int b) {
             return Math.abs(Structure.TILE.getX(a) - Structure.TILE.getX(b)) + Math.abs(Structure.TILE.getY(a) - Structure.TILE.getY(b));
         }
     }),
     DIAGONAL(new HeuristicAlgorithm() {
         @Override
         public double getCost(int a, int b) {
            return Math.max(Math.abs(Structure.TILE.getX(a) - Structure.TILE.getX(b)), Math.abs(Structure.TILE.getY(a) - Structure.TILE.getY(b)));
         }
     }),
     ABSOULTE(new HeuristicAlgorithm() {
         @Override
         public double getCost(int a, int b) {
             return Math.sqrt(Math.pow(Structure.TILE.getX(a) - Structure.TILE.getX(b), 2) + Math.pow(Structure.TILE.getY(a) - Structure.TILE.getY(b), 2));
         }
     });
 
     private final HeuristicAlgorithm algorithm;
 
     private Heuristic(final HeuristicAlgorithm algorithm) {
         this.algorithm = algorithm;
     }
 
     public HeuristicAlgorithm getAlgorithm() {
         return algorithm;
     }
 
     public interface HeuristicAlgorithm {
         public double getCost(final int a, final int b);
     }
 }
