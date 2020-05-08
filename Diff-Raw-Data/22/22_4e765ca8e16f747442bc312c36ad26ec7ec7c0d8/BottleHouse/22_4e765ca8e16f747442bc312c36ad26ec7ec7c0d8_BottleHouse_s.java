 package drinker.worldObjects;
 
 import drinker.WorldObject;
 
 public class BottleHouse extends WorldObject {
 
     public BottleHouse(int x, int y) {
         super(x, y);
     }
 
     @Override
     public char draw() {
        return 'B';
     }
 
 }
