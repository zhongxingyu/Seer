 package vooga.fighter.objects;
 
 import vooga.fighter.objects.utils.UpdatableLocation;
 
 
 /**
  * Represents an environment object like a block or platform.
  * 
  * More behavior will be added.
  * 
  * @author james, alanni
  * 
  */
 public class EnvironmentObject extends GameObject {
 
     /**
      * Constructs a new EnvironmentObject with the given image, center, and size.
      * In the future this will use the object loader to read from XML.
      */
     private UpdatableLocation myStartingLocation;
 
     public EnvironmentObject(UpdatableLocation startingLocation) {
         super();
         myStartingLocation = startingLocation;
        setImageData();
         // setLoader(new EnvironmentObjectLoader(objectId));
     }
 
     /**
      * Updates the environment object. Behavior to be added.
      */
     public void update () {
         // super.update();
     }
 
     /**
      * Returns false for now.
      */
     public boolean shouldBeRemoved () {
         return false;
     }
 
     @Override
     public void applyCollideEffect (GameObject o) {
         if (this.compare(o) > 0) {
             if (o instanceof AttackObject) {
                 ((AttackObject) o).endCounter();
             }
         }
     }
 
 }
