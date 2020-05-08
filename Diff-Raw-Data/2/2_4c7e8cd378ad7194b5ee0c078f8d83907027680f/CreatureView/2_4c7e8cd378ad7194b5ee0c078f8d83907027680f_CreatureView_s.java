 package ogo.spec.game.graphics.view;
 
 import ogo.spec.game.model.*;
 
 /**
  *
  * @author s102877
  */
 public class CreatureView {
 
     Creature creature;
     Tile previousLocation;
     Timer timer;
     int t0;
     double unit;
     double animationLength;
 
     public CreatureView(Creature creature, Timer timer) {
         this.creature = creature;
         this.timer = timer;
     }
 
     public void move(double animationLength) {
         previousLocation = creature.getPath().getCurrentTile();
         t0 = timer.getTime();
         this.animationLength = animationLength;
         unit = timer.getSleepTime() / animationLength;
     }
 
     public Vector getCurrentLocation() {
         final Tile currentTile = creature.getPath().getCurrentTile();
         if (previousLocation == null) {
            return new Vector(currentTile.getX() + 0.5, currentTile.getY() + 0.5, 0);
         } else {
             final double scalar = unit * (timer.getTime() - t0);
             if (scalar < 1) {
                 double x = (currentTile.getX() - previousLocation.getX()) * scalar;
                 double y = (currentTile.getY() - previousLocation.getY()) * scalar;
                 double z = 0;
                 Vector V = new Vector(x, y, z); //vector to move over
                 x = previousLocation.getX();
                 y = previousLocation.getY();
                 z = 0;
                 Vector P = new Vector(x, y, z); //previous location
 
                 /*System.out.println("Previous location:" + previousLocation);
                  System.out.println("Current location:" + creature.getPath().getCurrentTile());
                  System.out.println("Draw at:" + P.add(V));
 
                  System.out.println("Unit:" + unit);
                  System.out.println("Timer:" + timer.getTime());
                  System.out.println("t0:" + t0);
 
                  System.out.println();*/
                 return P.add(V);
             } else {
                 previousLocation = currentTile;
                 t0 = timer.getTime();
                 unit = timer.getSleepTime() / animationLength;
 
                 return new Vector(creature.getPath().getCurrentTile().getX(),
                         creature.getPath().getCurrentTile().getY(),
                         0);
             }
         }
     }
 }
