 package vooga.fighter.model.objects;
 
 import util.Location;
 import util.Pixmap;
 import vooga.fighter.model.loaders.ObjectLoader;
 import vooga.fighter.model.utils.ImageDataObject;
 import vooga.fighter.model.utils.State;
 import vooga.fighter.model.utils.UpdatableLocation;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Represents a single object in the game.
  * 
  * @author James Wei, alanni, David Le
  * 
  */
 public abstract class GameObject {
     
     private long myInstanceId;
     private ObjectLoader myLoader;
     private UpdatableLocation myCenter;
     private State myCurrentState;
     private ImageDataObject myImageData;
     private Map<String,State> myStates;
     private Map<String,Integer> myProperties;
 
     /**
      * Constructs a new GameObject. All fields are initially empty, and must be
      * populated with an ObjectLoader.
      */
     public GameObject() {
 //        myInstanceId = System.currentTimeMillis();
         myStates = new HashMap<String,State>();
         myProperties = new HashMap<String,Integer>();
         myLoader = null;
         myCurrentState = null;
         myImageData = null;
     }
     
     /**
      * Returns the game object's instance id. This id is unique to this particular
      * instantiation of the object.
      */
     public long getInstanceId() {
         return myInstanceId;
     }
 
     /**
      * Sets game object's center.
      */
     public void setLocation(UpdatableLocation ul) {
         myCenter = ul;
     }
 
     /**
      * Returns game object's center.
      */
     public UpdatableLocation getLocation() {
         return myCenter;
     }
 
     /**
      * Adds a property for this object. Overwrites any existing value.
      */
     public void addProperty(String key, int value) {
         myProperties.put(key, value);
     }
 
     /**
      * Returns a property for this object. Returns -1 if property does not exist.
      */
     public int getProperty(String key) {
         if (myProperties.containsKey(key)) {
             return myProperties.get(key);
         } else {
             return -1;
         }
     }
     
     /**
      * Clears all properties.
      */
     public void clearProperties() {
         myProperties.clear();
     }
     
     /**
      * Adds a state for this object. Overwrites any existing value.
      */
     public void addState(String key, State value) {
         myStates.put(key, value);
     }
     
     /**
      * Returns a state for this object. Returns null if it doesn't exist.
      */
     protected State getState(String key) {
         if (myStates.containsKey(key)) {
             return myStates.get(key);
         } else {
             return null;
         }
     }
     
     /**
      * Sets the current state for this object. Resets the current state after
      * switching.
      */
     public void setCurrentState(String key) {
         myCurrentState = getState(key);
         myCurrentState.resetState();
     }
     
     /**
      * Gets the current state for this object.
      */
    protected State getCurrentState() {
         return myCurrentState;
     }
 
     /**
      * Clears all states.
      */
     protected void clearStates() {
         myStates.clear();
     }
     
     /**
      * Sets the object loader for this object.
      */
     protected void setLoader(ObjectLoader loader) {
         myLoader = loader;
     }
     
     /**
      * Returns the object loader for this object.
      */
     protected ObjectLoader getLoader() {
         return myLoader;
     }
     
     /**
      * Sets image data for this object. Size, Location, and Image must not be
      * null for this object before calling this method, otherwise, this method
      * returns null.
      */
     public void setImageData() {
         Pixmap myCurrentImage = myCurrentState.getCurrentImage();
         Dimension myCurrentSize = myCurrentState.getCurrentSize();
         Location myCurrentLocation = myCenter.getLocation();
         if (!(myCurrentSize == null || myCurrentImage == null || myCenter == null)) {
             myImageData = new ImageDataObject(myCurrentImage, myCurrentLocation, myCurrentSize);
         }
     }
     
     /**
      * Sets image data to the information from an ImageDataObject
      */
     public void setImageData(ImageDataObject image){
     	myImageData= new ImageDataObject(image.getMyImage(), image.getMyLocation(),image.getMySize() );
     }
     
     /**
      * Returns image data for this object.
      */
     public ImageDataObject getImageData() {
         return myImageData;
     }
     
     /**
      * Updates the object for the game loop. Should be overridden by subclasses if
      * necessary, but all overrides should first call superclass method.
      */
     public void update() {
     	setImageData();
         if (myCenter != null) {
             myCenter.update();
         }
     }
     
     /**
      * Updates the objects state for the game loop. States are updated separately
      * because all states must be updated together, either before or after other
      * update logic that depends on current states.
      */
     public void updateState() {
         if (myCurrentState != null) {
             myCurrentState.update();
         }
     }    
     
     /**
      * Returns true if this object is colliding with another.
      */
     public boolean checkCollision(GameObject other) {
         Rectangle thisRect = getCurrentState().getCurrentRectangle(); 
         Rectangle otherRect = other.getCurrentState().getCurrentRectangle();
         return thisRect.intersects(otherRect);
     }
     
     /**
      * Returns the map of states for this object.
      */
     public Map<String,State> getStates(){
         return myStates;
     }
  
     /**
      * Second dispatch for collision management. Key part of the visitor pattern.
      */
     public abstract void dispatchCollision(GameObject other);
     
     /**
      * Handles a collision with another game object. Key part of the visitor pattern.
      */
     public abstract void handleCollision(CharacterObject other);
 
     /**
      * Handles a collision with another game object. Key part of the visitor pattern.
      */
     public abstract void handleCollision(AttackObject other);
     
     /**
      * Handles a collision with another game object. Key part of the visitor pattern.
      */
     public abstract void handleCollision(EnvironmentObject other);
     
     /**
      * Indicates whether or not the object is ready to be removed.
      */
     public abstract boolean shouldBeRemoved();
     
 }
