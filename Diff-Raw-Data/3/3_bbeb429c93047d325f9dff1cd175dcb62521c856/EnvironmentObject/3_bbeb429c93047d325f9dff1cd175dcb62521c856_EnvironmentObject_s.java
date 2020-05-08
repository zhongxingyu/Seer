 package vooga.fighter.model.objects;
 
 import vooga.fighter.model.loaders.EnvironmentObjectLoader;
 import vooga.fighter.model.utils.UpdatableLocation;
 
 /**
  * Represents an environment object like a block or platform.
  * 
  * More behavior will be added.
  * 
  * @author James Wei, alanni, David Le
  * 
  */
 public class EnvironmentObject extends GameObject {
 	private String myName;
 
 	/**
 	 * Constructs a new EnvironmentObject without a given center; used for level editor.
 	 */
 	public EnvironmentObject(String name) {
 		super();
 		init(name);
 	}
 	
     /**
      * Constructs a new EnvironmentObject with the given image, center, and size.
      * In the future this will use the object loader to read from XML.
      */
     public EnvironmentObject(String name, UpdatableLocation center) {
         super();
        init(name);
         setLocation(center);
     }
     
     private void init(String name) {
     	myName = name;
         setLoader(new EnvironmentObjectLoader(name, this));
         setCurrentState("default");
         getCurrentState().setLooping(true);
         setImageData();
     }
     
     /**
      * return the name of this type of environment object.
      * @return myName
      */
     public String getName() {
     	return myName;
     }
 
     /**
      * Updates the environment object. Behavior to be added.
      */
     public void completeUpdate() {
         
     }
 
     /**
      * Returns false for now.
      */
     public boolean shouldBeRemoved() {
         return false;
     }
     
     /**
      * Dispatches a colliding object to allow for proper collision handling. 
      */
     public void dispatchCollision(GameObject other) {
         other.handleCollision(this);
     }
     
     /**
      * Collision with another CharacterObject.
      */
     public void handleCollision(CharacterObject other) {
         System.out.println("EnvironmentObject handleCollision : Environment collided with character");
     }
     
     /**
      * Collision with an AttackObject.
      */
     public void handleCollision(AttackObject other) {
         System.out.println("EnvironmentObject handleCollision : Environment collided with attack");
     }
     
     /**
      * Collision with an EnvironmentObject.
      */
     public void handleCollision(EnvironmentObject other) {
         System.out.println("EnvironmentObject handleCollision : Environment collided with environment");
     }
     
     public void tellDelegate() {
     	
     }
 
 }
