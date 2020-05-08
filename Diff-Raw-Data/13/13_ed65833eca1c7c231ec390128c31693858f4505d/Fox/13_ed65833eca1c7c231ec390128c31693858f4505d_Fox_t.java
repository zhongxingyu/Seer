 import java.util.ArrayList;
 import java.util.List;
 import java.awt.Image;
 import java.util.Collections;
 
 public class Fox extends Animal {
     private static final int sightDistance = 10;
     private static final int moveDistance = 2;
     private static final int maxHunger = 100;
     private static final int maxAge = 100;
     
     private static ArrayList<String> prey = new ArrayList<String>();
     private static ArrayList<String> predators = new ArrayList<String>();
     
     public Fox(Location loc){
         Debug.echo("Constructing a new Fox object");
         setLocation(loc);
         hunger = 0;
         age = 0;
     }
 
     public void act(Grid grid){
         Debug.echo("Here is where the Fox would act");
         
         GridSquare mySquare = grid.get(getLocation());
         List<DistanceSquarePair> visibleSquares = grid.getAdjacentSquares(getLocation(), sightDistance);
         List<DistanceSquarePair> predatorSquares = grid.getOrganismSquares(visibleSquares, predators);
         List<DistanceSquarePair> preySquares = grid.getOrganismSquares(visibleSquares, prey);
         List<DistanceSquarePair> emptySquares = grid.getEmptySquares(visibleSquares);
         
         Plant myPlant = mySquare.getPlant();
         if(predatorSquares.size() > 0) {
             Debug.echo("OH SHIT RUN?");
         }
         if (myPlant != null && myPlant.isAlive() && prey.contains(myPlant.getClass().getName())){
             myPlant.getEaten(10);
             eat(10);
             return;
         } else {
             List<DistanceSquarePair> reachableSquares = grid.getAdjacentSquares(getLocation(), moveDistance);
             Collections.shuffle(reachableSquares);
             for(DistanceSquarePair pair: reachableSquares){
                 if(preySquares.contains(pair)){
                     if(emptySquares.contains(pair)){
                         move(grid, pair.gridSquare);
                 
                         mySquare = grid.get(getLocation());
                         myPlant = mySquare.getPlant();
                         myPlant.getEaten(10);
                         eat(10);
                         return;
                     } else {
                         grid.removeAnimal(pair.gridSquare);
                         move(grid, pair.gridSquare);
                         eat(20);
                         return;
                     }
                 }
             }
             List<DistanceSquarePair> emptyReachableSquares = emptySquares;
             emptyReachableSquares.retainAll(reachableSquares);
            if (emptyReachableSquares.size() > 0) {
                move(grid, emptyReachableSquares.get(Util.randInt(emptyReachableSquares.size())).gridSquare);
            }
         }
     }
 
     public Image getImage(){
         return Resources.imageByName("Fox");
     }
 
     public static void addPrey(String p)     { prey.add(p);      }
     public static void addPredator(String p) { predators.add(p); }
 
     protected int getMaxHunger(){
         return maxHunger;
     }
 
     protected int getMaxAge(){
         return maxAge;
     }
 
     protected int getSightDistance(){
         return sightDistance;
     }
 
     protected int getMoveDistance(){
         return moveDistance;
     }
     
     public String toString(){
         return "Fox";
     }
 }
