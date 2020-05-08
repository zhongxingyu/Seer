 package projetx;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import org.newdawn.slick.Image;
 
 enum COLLIDE {
 
     COLLIDE_LEFT,
     COLLIDE_RIGHT,
     COLLIDE_HAUT,
     COLLIDE_BAS,
     NONE
 }
 
 /**
  *
  * @author mica
  */
 public class Physics {
 
     private double gravity;
     List<Player> movables;
     List<Obstacle> platforms;
 
     public double getGravity() {
         return gravity;
     }
 
     public void setGravity(double gravity) {
         this.gravity = gravity;
     }
 
     Physics(double _gravity) {
         gravity = _gravity;
     }
 
     public void updateMovable(List<Player> _players) {
         movables = _players;
     }
 
     public void updatePlatforms(List<Obstacle> _platforms) {
         platforms = _platforms;
     }
 
     public void computePhysics(double TimeSinceLastFrame) {
         COLLIDE collide = COLLIDE.NONE;
         Point2D nextPoint;
         Point2D oldPoint;
         Sprite plateform;
         Player otherMovable;
         Sprite collider = null;
 
         for (Player currentMovable : movables) {
             collider = null;
             collide = COLLIDE.NONE;
             oldPoint = currentMovable.getCoords();
             
             if (currentMovable.isOnAPlatform() == true) {
                 //Si on est sur une plateforme
                 if (currentMovable.isWantsToJump()) {
                     //Si on saute
                     currentMovable.setSpeed(currentMovable.getSpeed().getX(),-currentMovable.getVerticalSpeed());
                     currentMovable.setIsOnAPlatform(false);
                     currentMovable.setWantsToJump(false);
                 }
             }
 
             if(currentMovable.getSpeed().getY() != 0.1)
                 System.out.println("VAd: " + currentMovable.getSpeed());
             nextPoint = addVectors(currentMovable.getCoords(), scalarCross( currentMovable.getSpeed(), TimeSinceLastFrame));
 
             if (currentMovable.isWantsToGoLeft()) {
                nextPoint.setLocation(nextPoint.getX() - currentMovable.getLateralSpeed(), nextPoint.getY());
                 currentMovable.setWantsToGoLeft(false);
             }
             if (currentMovable.isWantsToGoRight()) {
                nextPoint.setLocation(nextPoint.getX() + currentMovable.getLateralSpeed(), nextPoint.getY());
                 currentMovable.setWantsToGoRight(false);
             }
 
             currentMovable.setCoords(nextPoint);
 
             Point2D correctedPoint = new Point2D.Double();
  
             //On teste si le movable collide dans sa prochaine position
             currentMovable.setIsOnAPlatform(false);
             
             for (int i = 0; i < platforms.size(); i++) {
                 plateform = platforms.get(i);
                 collider = plateform;
                 //collide = IsColliding(currentMovable, plateform);
                 collide = IsCollidingObstacle(currentMovable,oldPoint, plateform);
 
 
                 if (collide != COLLIDE.NONE) {
                     switch (collide) {
                         case NONE:
                             
                             //correctedPoint = nextPoint;
                             break;
                         case COLLIDE_BAS:
                             if(currentMovable.getSpeed().getY() >= 0){
                                 correctedPoint.setLocation(currentMovable.getCoords().getX(), collider.getCoords().getY() + collider.getImage().getHeight()/2.0 - currentMovable.getImage().getHeight());
                                 currentMovable.setCoords(correctedPoint);
                                 currentMovable.setSpeed(currentMovable.getSpeed().getX(), 0);
                                 currentMovable.setIsOnAPlatform(true);
                             }
                             break;
                     }
                 }
             }
 
 
             for (int i = 0; i < movables.size(); i++) {
                 otherMovable = movables.get(i);
                 if (otherMovable != currentMovable) {
                     collide = IsColliding(currentMovable, otherMovable);
                     collider = otherMovable;
 
                     if (collide != COLLIDE.NONE) {
                         switch (collide) {
                             case NONE:
                                 //correctedPoint = nextPoint;
                                 break;
                             case COLLIDE_BAS:
                                 correctedPoint.setLocation(currentMovable.getCoords().getX(), collider.getCoords().getY() - currentMovable.getImage().getHeight());
                                 currentMovable.setCoords(correctedPoint);
                                 currentMovable.setSpeed(currentMovable.getSpeed().getX(), 0);
                                 currentMovable.setIsOnAPlatform(true);
                                 break;
                             case COLLIDE_LEFT:
                                 correctedPoint.setLocation(collider.getCoords().getX() + collider.getImage().getWidth(), currentMovable.getCoords().getY());
                                 currentMovable.setCoords(correctedPoint);
                                 break;
                             case COLLIDE_RIGHT:
                                 correctedPoint.setLocation(collider.getCoords().getX() - currentMovable.getImage().getWidth(), currentMovable.getCoords().getY());
                                 currentMovable.setCoords(correctedPoint);
                                 break;
                             case COLLIDE_HAUT:
                                 //correctedPoint = nextPoint;
                                 //correctedPoint.setLocation(currentMovable.getCoords().getX(), collider.getCoords().getY() + collider.getImage().getHeight());
                                 //currentMovable.setCoords(correctedPoint);
                                 break;
                         }
                     }
                 }
             }
            currentMovable.setSpeed(currentMovable.getSpeed().getX(), currentMovable.getSpeed().getY()+ gravity);
         }
     }
 
     private COLLIDE IsColliding(Sprite obj1, Sprite obj2) {
         //boolean isColliding = false;
         COLLIDE collide = COLLIDE.NONE;
 
         //nextPoint
         if ((obj1.getCoords().getX() + obj1.getImage().getWidth() > obj2.getCoords().getX() && obj1.getCoords().getX() + obj1.getImage().getWidth() < obj2.getCoords().getX() + obj2.getImage().getWidth())) { //coté droit Aligné suivant y
             //Collide a droite
             if ((obj1.getCoords().getY() + obj1.getImage().getHeight() > obj2.getCoords().getY() && obj1.getCoords().getY() + obj1.getImage().getHeight() < obj2.getCoords().getY() + obj2.getImage().getHeight())) { //Aligné suivant x
                 //collide en bas a droite
                 if (obj1.getCoords().getY() < obj2.getCoords().getY()) {
                     //Objet a mettre au dessus
                     collide = COLLIDE.COLLIDE_BAS;
                 } else {
                     //Objet collide sur le coté droit
                     collide = COLLIDE.COLLIDE_RIGHT;
                 }
             } else {
                 if (obj1.getCoords().getY() > obj2.getCoords().getY() && obj1.getCoords().getY() < obj2.getCoords().getY() + obj2.getImage().getHeight()) {
                     //collide en haut a droite
                     collide = COLLIDE.COLLIDE_HAUT;
 
                 }
             }
         } else {
             if ((obj1.getCoords().getX() > obj2.getCoords().getX() && obj1.getCoords().getX() < obj2.getCoords().getX() + obj2.getImage().getWidth())) {//Coté gauche aligné
                 // Collide a gauche
                 if ((obj1.getCoords().getY() + obj1.getImage().getHeight() > obj2.getCoords().getY() && obj1.getCoords().getY() + obj1.getImage().getHeight() < obj2.getCoords().getY() + obj2.getImage().getHeight())) { //Aligné suivant x
                     //collide en bas a gauche
                     if (obj1.getCoords().getY() < obj2.getCoords().getY()) {
                         //Objet a mettre au dessus
                         collide = COLLIDE.COLLIDE_BAS;
                     } else {
                         //Objet collide sur le coté gauche
                         collide = COLLIDE.COLLIDE_LEFT;
                     }
                 } else {
                     if (obj1.getCoords().getY() > obj2.getCoords().getY() && obj1.getCoords().getY() < obj2.getCoords().getY() + obj2.getImage().getHeight()) {
                         //collideen haut a gauche
                         collide = COLLIDE.COLLIDE_HAUT;
                     }
                 }
             }
         }
         return collide;
     }
 
     private COLLIDE IsCollidingObstacle(Sprite obj1, Point2D oldPoint, Sprite obstacle) {
         //boolean isColliding = false;
         COLLIDE collide = COLLIDE.NONE;
 
         double halfHeight = obstacle.getImage().getHeight()/2.0;
 
         //nextPoint
         if ((obj1.getCoords().getX() + obj1.getImage().getWidth() > obstacle.getCoords().getX() && obj1.getCoords().getX() + obj1.getImage().getWidth() < obstacle.getCoords().getX() + obstacle.getImage().getWidth()) || (obj1.getCoords().getX() > obstacle.getCoords().getX() && obj1.getCoords().getX() < obstacle.getCoords().getX() + obstacle.getImage().getWidth())) {
             //Objet les uns sur les autres
             if ((obj1.getCoords().getY() + obj1.getImage().getHeight() >= obstacle.getCoords().getY() + halfHeight) && (oldPoint.getY()+ obj1.getImage().getHeight() <=  obstacle.getCoords().getY()+ halfHeight )) { //Aligné suivant x
                 //Objet a mettre au dessus
                 collide = COLLIDE.COLLIDE_BAS;
             }
         }
         return collide;
     }
 
     private Point2D addVectors(Point2D a, Point2D b) {
         Point2D c = new Point2D.Double();// a.getX() + b.getX(), a.getY() + b.getY());
         c.setLocation(a.getX() + b.getX(), a.getY() + b.getY());
         return c;
     }
 
     private Point2D scalarCross(Point2D a, double l) {
         Point2D b = new Point2D.Double(a.getX() * l, a.getY() * l);
         return b;
     }
 }
