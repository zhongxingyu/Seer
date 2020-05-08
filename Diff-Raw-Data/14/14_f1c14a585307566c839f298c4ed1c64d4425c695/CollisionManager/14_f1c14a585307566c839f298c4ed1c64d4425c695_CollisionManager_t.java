 package vooga.fighter.util;
 
 import java.awt.Rectangle;
 import java.lang.reflect.Method;
 import java.util.List;

 import vooga.fighter.model.objects.AttackObject;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.EnvironmentObject;
 import vooga.fighter.model.objects.GameObject;
import vooga.fighter.model.objects.MapObject;
 import vooga.fighter.model.objects.MenuObject;
 import vooga.fighter.model.objects.MouseClickObject;
 
 
 /**
  * Detects collisions between all the game objects. Collision handling is achieved
  * in the game objects themselves, and
  * 
  * @author James Wei, alanni
  * @modified Matthew Parides
  */
 public class CollisionManager {
 
     CollisionDetector myCollisionDetector;
 
     public CollisionManager () {
         myCollisionDetector = new CollisionDetector();
     }
 
     /**
      * Checks for collisions between the game objects.
      */
     public void checkCollisions (List<GameObject> myObjects) {
         for (int i = 0; i < myObjects.size() - 1; i++) {
             for (int j = i + 1; j < myObjects.size(); j++) {
                 GameObject o1 = myObjects.get(i);
                 GameObject o2 = myObjects.get(j);
                if (!(o1 instanceof MapObject || o2 instanceof MapObject)){
                	Rectangle o1Rect = o1.getCurrentState().getCurrentRectangle();
                	Rectangle o2Rect = o1.getCurrentState().getCurrentRectangle();
	                if (myCollisionDetector.quickDetectCollision(o1Rect, o2Rect)) {
	                    handleCollisions(o1, o2);
	                }
                 }
             }
         }
     }
 
     /**
      * Delegates to specific collision methods based on the runtime type of our
      * colliding game objects using reflection.
      */
     public void handleCollisions (GameObject o1, GameObject o2) {
         try {
             Class<?>[] runtimeClasses = new Class[] { o1.getClass(), o2.getClass() };
             Object[] parameters = new Object[] { o1, o2 };
             Method method = this.getClass().getMethod("collide", runtimeClasses);
             method.invoke(this, parameters);
         }
         catch (Exception e) {
 
         }
     }
 
     /**
      * Handles collisions between two attack objects.
      * 
      */
     public void collide (AttackObject o1, AttackObject o2) {
         if (o1.getCurrentState().hasPriority(o2.getCurrentState())) {
             o1.endCounter();
         }
     }
 
     /**
      * Handles collisions between a character object and an attack object.
      * Destroys object on collision with character object
      */
     public void collide (CharacterObject o1, AttackObject o2) {
         collide(o2, o1);
     }
 
     /**
      * Handles collisions between an attack object and a character object.
      */
     public void collide (AttackObject o1, CharacterObject o2) {
         if (!o1.getOwner().equals(o2)) {
             int remaining = o1.inflictDamage(o2);
             o1.addTargetForEffects(o2);
         }
         o1.endCounter();
     }
 
     /**
      * Handles collisions between an environment object and an attack object.
      */
     public void collide (EnvironmentObject o1, AttackObject o2) {
     }
 
     /**
      * Handles collisions between an attack object and an environment object.
      */
     public void collide (AttackObject o1, EnvironmentObject o2) {
         o1.endCounter();
     }
 
     /**
      * Handles collisions between a character object and an environment object.
      */
     public void collide (CharacterObject o1, EnvironmentObject o2) {
         collide(o2, o1);
     }
 
     /**
      * Handles collisions between an environment object and a character object.
      */
     public void collide (EnvironmentObject o1, CharacterObject o2) {
         Rectangle o1Rect = o1.getCurrentState().getCurrentRectangle();
         Rectangle o2Rect = o2.getCurrentState().getCurrentRectangle();
         if (myCollisionDetector.hitBottom(o2Rect, o1Rect)) {
             o2.moveBack();
         }
     }
 
     /**
      * Handles collisions between a Menu object and an MouseClick object.
      */
     public void collide (MenuObject o1, MouseClickObject o2) {
         o1.tellDelegate();
     }
 
     /**
      * Handles collisions between an MouseClick object and a Menu object.
      */
     public void collide (MouseClickObject o1, MenuObject o2) {
         o2.tellDelegate();
     }
 
 }
