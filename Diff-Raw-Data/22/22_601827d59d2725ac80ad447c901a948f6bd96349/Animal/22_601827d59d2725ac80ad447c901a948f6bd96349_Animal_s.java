 package summative2013.lifeform;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import summative2013.Summative;
 import summative2013.Summative.TERRAIN;
 import static summative2013.lifeform.Lifeform.summative;
 import summative2013.memory.Memory;
 import summative2013.phenomena.Disease;
 import summative2013.phenomena.Weather.WEATHER;
 import java.util.LinkedList;
 
 /**
  * Parent class of all animal moving lifeform living creatures
  *
  * @author 322303413
  */
 public abstract class Animal extends Lifeform {
 
     /**
      * Stores hunger level, from 0-99
      */
     protected int hunger;
 
     /**
      * @return the inGroup
      */
     public LinkedList<Animal> getInGroup() {
         return inGroup;
     }
 
     /**
      * @return the outGroup
      */
     public LinkedList<Animal> getOutGroup() {
         return outGroup;
     }
 
     /**
      * Creates the gender enum thing
      */
     public enum GENDER {
 
         MALE, FEMALE
     };
 
     /**
      * Some kind of direction variable
      */
     public enum DIRECTION {
 
         NORTH, SOUTH, EAST, WEST, CENTER
     };
     /**
      * Stores gender
      */
     protected GENDER gender;
     /**
      * Store the friends happy no fighting membership club
      */
     protected LinkedList inGroup;
     /**
      * Stores the angry enemies murder hatred villain gang
      */
     protected LinkedList outGroup;
     /**
      * Stores the location the animal is currently moving to
      */
     protected Point destination;
     /**
      * Stores how messed up that animal losing its mind crazies asylum bar level
      */
     protected int depravity;
     /**
      * The nearby organisms
      */
     protected ArrayList<Lifeform> nearbyLife;
     /**
      * The closest water
      */
     protected Point water;
     /**
      * Stores vision range
      */
     protected int sight;
     /**
      * Stores nearest prey
      */
     protected Point food;
     /**
      * stores the nearest mate
      */
     protected Point mate;
     /**
      * Stores past memories and regrets and nostalgic moments in the sun of
      * childhood glorious
      */
     protected ArrayList<Memory> knowledge;
     /**
      * Stores eating food delicious untermensch inferior prey animals
      */
     protected ArrayList<Lifeform> preyList;
     /**
      * Stores how diseased plague leprosy shun contagion SARS level of multiple
      */
     protected ArrayList<Disease> disease;
 
     public Animal() {
         hunger = 50;
         gender = GENDER.MALE;
         depravity = 0;
         knowledge = new ArrayList<Memory>();
         disease = new ArrayList<Disease>();
     }
 
     /**
      * Refreshes to store all the nearby lifeforms
      */
     public void findNearbyLife() {
         nearbyLife = new ArrayList<Lifeform>();
         for (int x = -sight; x <= sight; x++) {
             for (int y = -sight; y <= sight; y++) {
                 if (Math.abs(x) + Math.abs(y) <= sight) {
                     if (summative.lifeGet(new Point(location.x + x, location.y + y)) != null) {
                         nearbyLife.add(summative.lifeGet(new Point(location.x + x, location.y + y)));
                     }
                 }
             }
         }
     }
 
     /**
      * Refreshes nearby terrain
      */
     public void findWater() {
         ArrayList<Point> waterList = new ArrayList<Point>();
         water = null;
         for (int x = -sight; x <= sight; x++) {
             for (int y = -sight; y <= sight; y++) {
                 if (Math.abs(x) + Math.abs(y) <= sight) {
                     if (summative.terrainGet(new Point(location.x + x, location.y + y)) == Summative.TERRAIN.SEA) {
                         waterList.add(new Point(location.x + x, location.y + y));
                     }
                 }
             }
         }
         if (waterList.size() > 0) {
             water = waterList.get(0);
             for (Point p : waterList) {
                 if (Math.abs(p.x - location.x) + Math.abs(p.y - location.y) < Math.abs(water.x - location.x) + Math.abs(water.y - location.y)) {
                     water = p;
                 }
             }
         }
     }
 
     /**
      * Finds the closest prey
      */
     public void findFood(ArrayList<Lifeform> list) {
         ArrayList<Point> foodList = new ArrayList<Point>();
         food = null;
         for (Lifeform l : list) {
             for (Lifeform m : preyList) {
                 if (l.getClass().equals(m.getClass())) {
                     foodList.add(l.location);
                 }
             }
         }
         if (foodList.size() > 0) {
             food = foodList.get(0);
             for (Point p : foodList) {
                 if (Math.abs(p.x - location.x) + Math.abs(p.y - location.y) < Math.abs(food.x - location.x) + Math.abs(food.y - location.y)) {
                     food = p;
                 }
             }
         }
     }
 
     public void findMate(ArrayList<Lifeform> list) {
         ArrayList<Point> mateList = new ArrayList<Point>();
         mate = null;
         for (Lifeform l : list) {
             if (l.getClass().equals(this.getClass())) {
                 mateList.add(l.location);
             }
         }
 
         if (mateList.size() > 0) {
             mate = mateList.get(0);
             for (Point p : mateList) {
                 if (Math.abs(p.x - location.x) + Math.abs(p.y - location.y) < Math.abs(mate.x - location.x) + Math.abs(mate.y - location.y)) {
                     mate = p;
                 }
             }
         }
     }
 
     public boolean isPrey(Lifeform l) {
         for (Lifeform life : preyList) {
             if (life.getClass().equals(l.getClass())) {
                 return true;
             }
         }
         return false;
     }
 
     public void setDestination() {
         if (thirst < 50 && hunger < 50) {
             destination = mate;
         } else if (thirst >= hunger) {
             destination = water;
         } else {
             destination = food;
         }
     }
 
     public DIRECTION getDirection(Point p) {
         if (p.x == location.x && p.y == location.y) {
             return DIRECTION.CENTER;
         } else if (Math.abs(p.x - location.x) > Math.abs(p.y - location.y)) {
             if (p.x > location.x) {
                 return DIRECTION.EAST;
             } else {
                 return DIRECTION.WEST;
             }
         } else if (p.y > location.y) {
             return DIRECTION.NORTH;
         } else {
             return DIRECTION.SOUTH;
         }
     }
 
     public GENDER getGender() {
         return gender;
     }
 
     public boolean canMate(Animal l) {
         if (l.getClass() == this.getClass() && l.getGender() != this.getGender()) {
             return true;
         }
         return false;
     }
 
     public boolean drowning() {
         int drownery = 0;
 
         for (int x = -1; x <= 1; x++) {
             for (int y = -1; y <= 1; y++) {
                 if (Math.abs(x) + Math.abs(y) <= 1) {
                     if (summative.terrainGet(new Point(location.x + x, location.y + y)) == TERRAIN.SEA) {
                         drownery = drownery + 1;
                     }
                 }
             }
         }
         if (drownery == 5) {
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public void act(WEATHER Weather) {
         findNearbyLife();
         findWater();
         findFood(nearbyLife);
         findMate(nearbyLife);
         setDestination();
 
         hunger = hunger + 5;
         thirst = thirst + 5;
 
         if (getDirection(destination) == DIRECTION.NORTH) {
             location.y = location.y + 1;
         } else if (getDirection(destination) == DIRECTION.SOUTH) {
             location.y = location.y - 1;
         } else if (getDirection(destination) == DIRECTION.WEST) {
             location.x = location.x - 1;
         } else if (getDirection(destination) == DIRECTION.EAST) {
             location.x = location.x + 1;
         } else {
         }
         if (Math.abs(destination.x - location.x) <= 1 && Math.abs(destination.y - location.y) <= 1
                 && Math.abs(destination.x - location.x) + Math.abs(destination.y - location.y) < 2) {
             {
                 if (summative.terrainGet(destination) == TERRAIN.SEA) {
                     thirst = 0;
                 } else if (isPrey(summative.lifeGet(destination))) {
                     hunger = hunger - 30;
                     if (summative.lifeGet(destination).getClass().equals(new Tree().getClass())) {
                         Tree temp = (Tree) (summative.lifeGet(destination));
                         if (temp.getCurrent() <= 0) {
                             temp.changeHealth(-50);
                         } else {
                             temp.changeCurrent(-1);
                         }
                     } else if (isPrey(summative.grassGet(destination))) {
                        errorerrorerrorerrorerror
                     } else {
                         summative.assistedSuicide(destination);
                     }
                 } else if (summative.lifeGet(destination).getMobile()) {
                     if (canMate((Animal) (summative.lifeGet(destination)))) //reproduction code
                     {
                         hunger = hunger + 30;
                         reproduce();
                     }
                 }
             }
         }
         if (hunger > 100 || thirst > 100) {
             suicide();
         } else if (drowning()) {
             suicide();
         }
     }
 }
