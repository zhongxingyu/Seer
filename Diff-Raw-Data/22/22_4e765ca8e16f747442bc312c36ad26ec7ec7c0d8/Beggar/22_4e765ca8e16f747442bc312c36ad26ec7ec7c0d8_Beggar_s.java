 package drinker.worldObjects;
 
 import drinker.Point2D;
 import drinker.WorldObject;
 import drinker.utils.Pair;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Beggar extends WorldObject {
 
     private final BottleHouse bottleHouse;
     private boolean isWithBottle;
     private Bottle bottle;
 
     public Beggar(BottleHouse bottleHouse, int x, int y) {
         super(x, y);
         this.bottleHouse = bottleHouse;
         isWithBottle = false;
 
     }
 
     @Override
     public char draw() {
         return 'Z';
     }
 
     @Override
     public void onTick() {
 
         if (isWithBottle) {
             homeCase();
         } else {
             newBottleCase();
         }
 
     }
 
     void homeCase() {
 
        Pair<Point2D, Integer> pair = world.findDirectionOnClosestPath(this, bottle);
        if(pair == null){
             return;
         }
 
         Point2D direction = pair.first;
 
         if (direction.isZero()) {
             isWithBottle = false;
         }
        
         world.removeObject(bottle);
         super.x += direction.x;
         super.y += direction.y;
         bottle.setX(super.x);
         bottle.setY(super.y);
         world.addObject(bottle);
        
     }
 
     void newBottleCase() {
 
         ArrayList<Bottle> list = world.getBottles();
 
         if (list.isEmpty()) {
             return;
         }
 
         Point2D dir = null;
 
         int minDist = Integer.MAX_VALUE;
 
         for (Bottle bottle : list) {
             Pair<Point2D, Integer> pair = world.findDirectionOnClosestPath(this, bottle);
             if (pair == null) {
                 continue;
             }
             if (pair.first.isZero()) {
                 takeNewBottle(bottle);
                 return;
             }
             if (pair.second < minDist) {
                 dir = pair.first;
             }
         }
         if (dir == null) {
             return;
         }
        
         super.x += dir.x;
         super.y += dir.y;
 
 
     }
 
     void takeNewBottle(Bottle bottle) {
         this.bottle = bottle;
         this.isWithBottle = true;
         homeCase();
     }
 
     @Override
     public boolean isMovable() {
         return true;
     }
 
 }
