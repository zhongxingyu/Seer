 package vooga.fighter.util;
 
 import java.awt.Rectangle;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.List;
 import vooga.fighter.model.objects.AttackObject;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.EnvironmentObject;
 import vooga.fighter.model.objects.GameObject;
 import vooga.fighter.model.objects.MapObject;
 import vooga.fighter.model.objects.MenuObject;
 import vooga.fighter.model.objects.MouseClickObject;
 import vooga.fighter.model.objects.MouseObject;
 
 /**
  * Detects collisions between all the game objects. Collision handling is achieved
  * in the game objects themselves, and 
  * 
  * @author James Wei, alanni
  * @modified Matthew Parides
  */
 public class CollisionManager {
 	
 	CollisionDetector myCollisionDetector;
 	
 	public CollisionManager(){
 		myCollisionDetector = new CollisionDetector();
 	}
 	
     /**
      * Checks for collisions between the game objects.
      */
     public void checkCollisions(List<GameObject> myObjects) { 
         for (int i = 0; i < myObjects.size() - 1; i++) {
             for (int j = i + 1; j < myObjects.size(); j++) {
                 GameObject o1 = myObjects.get(i);
                 GameObject o2 = myObjects.get(j);
                 if (o1 instanceof MapObject || o2 instanceof MapObject) {
                     continue;
                 }
                 if (myCollisionDetector.quickDetectCollision(o1.getCurrentState().getCurrentRectangle(), 
                     		o2.getCurrentState().getCurrentRectangle()))
                 	handleCollisions(o1,o2);
                 
                 }
             }
         }
 
 //    /**
 //     * Applies collisions between collided game objects. Collisions are handled
 //     * through an implementation of the visitor design pattern.
 //     * 
 //     * Note: if you want to use the old visitor framework, uncomment this method
 //     * and comment out the handleCollisions method with reflection below.
 //     */
 //    public void handleCollisions(GameObject o1, GameObject o2) {
 //        o1.dispatchCollision(o2);
 //    }    
     
     /**
      * Delegates to specific collision methods based on the runtime type of our
      * colliding game objects using reflection.
      */
     public void handleCollisions(GameObject o1, GameObject o2) {
         try {            
             Class<?>[] runtimeClasses = new Class[]{o1.getClass(), o2.getClass()};
             Object[] parameters = new Object[]{o1, o2};
             Method method = this.getClass().getMethod("collide", runtimeClasses);
             method.invoke(this, parameters);
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         }
     }        
     
     /**
      * Handles collisions between two character objects.
      */
     public void collide(CharacterObject o1, CharacterObject o2) {
     	System.out.println("Character Colliding");
 //    	if (o1.getCurrentState().hasPriority(o2.getCurrentState())){
 //    		o2.pushBack(o1.getMovingDirection());
 //    	}
 //    	else{
 //    		o1.pushBack(o2.getMovingDirection()); 
 //    	}
     }
     
     /**
      * Handles collisions between two attack objects.
      * 
      */
     public void collide(AttackObject o1, AttackObject o2) {
     	if (o1.getCurrentState().hasPriority(o2.getCurrentState())){
     		o1.endCounter();
     	}
     }
     
     /**
      * Handles collisions between two environment objects.
      */
     public void collide(EnvironmentObject o1, EnvironmentObject o2) {
         
     }
     
     /**
      * Handles collisions between a character object and an attack object.
      * Destroys object on collision with character object 
      */
     public void collide(CharacterObject o1, AttackObject o2) {
     	collide(o2,o1);
     }
     
     /**
      * Handles collisions between an attack object and a character object.
      */
     public void collide(AttackObject o1, CharacterObject o2) {
     	if (o1.getOwner()!=o2){
     		o1.inflictDamage(o2);
     	}
     	o1.endCounter();
     }
     
     /**
      * Handles collisions between an environment object and an attack object.
      */
     public void collide(EnvironmentObject o1, AttackObject o2) {
 //    	collide(o2, o1);
     }
     
     /**
      * Handles collisions between an attack object and an environment object.
      */
     public void collide(AttackObject o1, EnvironmentObject o2) {
     	o1.endCounter();
     }
     
     /**
      * Handles collisions between a character object and an environment object.
      */
     public void collide(CharacterObject o1, EnvironmentObject o2) {
     	collide(o2, o1);
     }
     
     /**
      * Handles collisions between an environment object and a character object.
      */
     public void collide(EnvironmentObject o1, CharacterObject o2) {
     	if(myCollisionDetector.hitBottom(o2.getCurrentState().getCurrentRectangle(),
     			o1.getCurrentState().getCurrentRectangle())){
     		o2.jump();
    		System.out.println("HITBOTTOMCOLLISIONMANAGER");
     	}
    	System.out.println("COLLISIONMANAGER");
     }
     
     /**
      * Handles collisions between a Menu object and an MouseClick object.
      */
     public void collide(MenuObject o1, MouseClickObject o2) {
     	o1.tellDelegate();
     }
     
     /**
      * Handles collisions between an environment object and a mouseobject.
      */
     public void collide(EnvironmentObject o1, MouseClickObject o2) {
     	collide(o2, o1);
     }
     
     /**
      * Handles collisions between an environment object and an attack object.
      */
     public void collide(MouseClickObject o1, EnvironmentObject o2) {
 //    	o2.tellDelegate();
     }
     
     /**
      * Handles collisions between an MouseClick object and a Menu object.
      */
     public void collide(MouseClickObject o1, MenuObject o2) {
     	o2.tellDelegate();
     }
 
     /**
      * Handles collisions between an MouseClick object and MouseClick object...
      */
     public void collide(MouseClickObject o1, MouseClickObject o2) {
     }
     
     public void collide(MenuObject o1, MouseObject o2) {
     }
     
     public void collide(MouseObject o1, MenuObject o2) {
     }
     
     public void collide(MouseObject o1, MouseObject o2) {
     }
     
     public void collide(MenuObject o1, MenuObject o2) {
     }
 }
