 import java.awt.Image;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public abstract class Animal extends Organism {
     protected int age;
     protected double hunger;
     protected int breedingTime;
     
     protected abstract double getMaxHunger();
     protected abstract int getMaxAge();
     protected abstract int getSightDistance();
     protected abstract int getMoveDistance();
     public abstract double getCalories();
     public abstract void act(Grid grid);
     public abstract Image getImage();
     public abstract int getMaxBreedingTime();
     public abstract void addMyType(Grid grid, GridSquare square);
     
     public void step(Grid grid){
         age++;
         hunger+= getCalories()/4;
         if(isOld() || isStarving()) {
             if(isOld()){
                 Debug.echo("Animal at "+getLocation()+" died due to old age");
             } else {
                 Debug.echo("Animal at "+getLocation()+" died due to hunger");
             }
             grid.removeAnimal(getLocation());
         } else {
             if (!breed(grid)){
                 act(grid);
             }
         }
     }
 
     public boolean breed(Grid grid) {
         List<DistanceSquarePair> reachableSquares = grid.getAdjacentSquares(getLocation(), getMoveDistance());
         Collections.shuffle(reachableSquares);
 
         if (breedingTime > 0) breedingTime--;
         if (breedingTime == 0 && hunger <= getMaxHunger()/2) {
             for (DistanceSquarePair pair : reachableSquares) {
                 if (pair.gridSquare.getAnimal() == null) {
                     addMyType(grid, pair.gridSquare);
                     breedingTime = getMaxBreedingTime();
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean isOld(){
         return age >= getMaxAge();
     }
     public boolean isStarving(){
         return hunger >= getMaxHunger();
     }
     protected void eat(double amount){
         hunger -= amount;
         if(hunger < 0) {
             hunger = 0;
         }
     }
     protected void eat(Organism o, Grid grid){
         if(o instanceof Plant){
             move(grid, o.getLocation());
             ((Plant)o).getEaten();
             eat(o.getCalories());
         } else {
             eat(o.getCalories());
             grid.removeAnimal(o.getLocation());
             move(grid, o.getLocation());
         }
     }
     protected void move(Grid grid, Location newLocation){
         grid.removeAnimal(getLocation());
         grid.addAnimal(this, newLocation);
         setLocation(newLocation);
     }
     protected void move(Grid grid, GridSquare newGridSquare){
         move(grid, newGridSquare.getLocation());
     }
     
    protected Organism bestPreyInDistance(Grid grid, ArrayList<String> prey, int distance, boolean closest){
         GridSquare mySquare = grid.get(getLocation());
 
         Organism bestAdjacentPrey = null;
         
         if (mySquare.getPlant() != null && mySquare.getPlant().isAlive() && prey.contains(mySquare.getPlant().getClass().getName())){
             bestAdjacentPrey = mySquare.getPlant();
         }
     
         List<DistanceSquarePair> reachableSquares = grid.getAdjacentSquares(getLocation(), distance);
         List<DistanceSquarePair> preySquares = grid.getOrganismSquares(reachableSquares, prey);
         List<DistanceSquarePair> emptySquares = grid.getEmptySquares(reachableSquares);
         
         Organism temp;
         for(DistanceSquarePair pair: preySquares){
             if(emptySquares.contains(pair)){
                 temp = pair.gridSquare.getPlant();
                if (bestAdjacentPrey == null || temp.getCalories() > bestAdjacentPrey.getCalories() || (!closest && temp.getCalories() == bestAdjacentPrey.getCalories())){
                     bestAdjacentPrey = temp;
                 }
             } else {
                 temp = pair.gridSquare.getAnimal();
                 if (prey.contains(temp.getClass().getName())) {
                    if (bestAdjacentPrey == null || temp.getCalories() > bestAdjacentPrey.getCalories() || (!closest && temp.getCalories() == bestAdjacentPrey.getCalories())){
                         bestAdjacentPrey = temp;
                     }
                 } else {
                     //I want to eat the plant, but the square is occupied... Oh well.
                 }
             }
         }
         return bestAdjacentPrey;
     }
     
     protected void init(Location loc) {
         setLocation(loc);
         hunger = getMaxHunger() * 0.75;
         age = 0;
         breedingTime = getMaxBreedingTime();
     }
 }
