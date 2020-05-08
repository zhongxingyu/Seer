 import java.util.ArrayList;
 import java.util.List;
 import java.awt.Image;
 import java.util.Collections;
 
 public class Fox extends Animal {
     private static double calories;
     private static final int maxBreedingTime = 25; 
     private static final int sightDistance = 6;
     private static final int moveDistance = 2;
     private static double maxHunger;
     private static final int maxAge = 100;
     
     private static ArrayList<String> prey = new ArrayList<String>();
     private static ArrayList<String> predators = new ArrayList<String>();
     
     public Fox(Location loc){
         Debug.echo("Constructing a new Fox object");
         init(loc);
     }
 
     public void act(Grid grid){
         Debug.echo("Here is where the Fox would act");
         
         GridSquare mySquare = grid.get(getLocation());
         List<DistanceSquarePair> visibleSquares = grid.getAdjacentSquares(getLocation(), sightDistance);
         List<DistanceSquarePair> predatorSquares = grid.getOrganismSquares(visibleSquares, predators);
         
         if(predatorSquares.size() > 0) {
             GridSquare predatorSquare = predatorSquares.get(0).gridSquare;
             GridSquare moveSquare = grid.getOptimalMoveSquare(getLocation(), predatorSquare.getLocation(), moveDistance*2, false);
             move(grid, moveSquare);
             return;
         }
         
        Organism bestAdjacentPrey = bestPreyInDistance(grid, prey, moveDistance);
        Organism bestVisiblePrey = bestPreyInDistance(grid, prey, sightDistance);
         
         if(bestAdjacentPrey != null && bestAdjacentPrey.getCalories() == bestVisiblePrey.getCalories()) {
             eat(bestAdjacentPrey, grid);
             return;
         } else if (bestVisiblePrey != null) {
             //Move toward bestVisiblePrey?
             GridSquare moveSquare = grid.getOptimalMoveSquare(getLocation(), bestVisiblePrey.getLocation(), moveDistance*2, true);
             move(grid, moveSquare);
             return;
         }
         
         //No prey in sight. Wander?
         List<DistanceSquarePair> emptyReachableSquares = grid.getEmptySquares(getLocation(), moveDistance);
         if (emptyReachableSquares.size() > 0) {
             move(grid, emptyReachableSquares.get(Util.randInt(emptyReachableSquares.size())).gridSquare);
         }
     }
 
     public void addMyType(Grid grid, GridSquare square) {
         grid.addAnimal(new Fox(square.getLocation()), square);
     }
 
     public static void addPrey(String p)     { prey.add(p);      }
     public static void addPredator(String p) { predators.add(p); }
     public static void setCalories(double c)    { 
         calories = c;     
         maxHunger = c * 50;
     }
 
     public int getMaxBreedingTime() {
         return maxBreedingTime;
     }
 
     protected double getMaxHunger()    { return maxHunger;                     }
     protected int getMaxAge()       { return maxAge;                        }
     protected int getSightDistance(){ return sightDistance;                 }
     protected int getMoveDistance() { return moveDistance;                  }
     
     public double getCalories()        { return calories;                      }
     public String toString()        { return "Fox";                         }
     public Image getImage()         { return Resources.imageByName("Fox");  }
 }
