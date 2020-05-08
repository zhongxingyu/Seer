 package vooga.fighter.controller.displayinformation;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import util.Location;
 import vooga.fighter.controller.interfaces.ViewDataSource;
import vooga.fighter.util.Paintable;
 
 
 /**
  * Displays game updatable information
  * Extends Observable and implements
  * ViewDataSource
  * 
  * @author Jack Matteucci
  * @author Jerry Li
  * @author Wayne You
  *
  */
 public class DisplayInfo extends Observable implements ViewDataSource{
 
 
 
     private List<Location> myLocations;
     private List<Paintable> myGamePaintables;
     private List<Paintable> myHUDPaintables;
     private List<Dimension> myImageSizes;
     private List<List<Integer>> myImageEffects;
 
     /**
      * Constructer, constructs lists. 
      */
     public DisplayInfo() {
         myLocations = new ArrayList<Location>();
         myGamePaintables = new ArrayList<Paintable>();
         myHUDPaintables = new ArrayList<Paintable>();
         myImageSizes = new ArrayList<Dimension>();
         myImageEffects = new ArrayList<List<Integer>>();
     }
 
     /**
      * @return object location at list index
      */
     @Override
 	public Paintable getPaintable (int index) {
         if (index >= myGamePaintables.size()) {
             return myHUDPaintables.get(index - myGamePaintables.size());
         }
         return myGamePaintables.get(index);
     }
 
     /**
      * Returns location
      */
     @Override
 	public Location getLocation(int index) {
         if (index >= myGamePaintables.size()) {
             return new Location(0,0);
         }
         return myLocations.get(index);
     }
 
     /**
      * @return size of sprite at list index
      */
     @Override
 	public Dimension getSize (int index) {
         if (index >= myGamePaintables.size()) {
             return new Dimension(0,0);
         }
         return myImageSizes.get(index);
     }
 
     /**
      * @param myLocations the mySpriteLocations to set
      */
     public void setGameObjectLocations(List<Location> spriteLocations) {
         myLocations = spriteLocations;
     }
 
     /**
      * 
      * @param index
      */
     public void setGameObjectLocation(int index, Location loc) {
         myLocations.set(index, loc);
     }
 
     /**
      * @return the mySprites
      */
     public List<Paintable> getGameObjects() {
         return myGamePaintables;
     }
 
     /**
      * @param myGamePaintables the mySprites to set
      */
     public void setGamePaintables(List<Paintable> gamePaintables) {
         myGamePaintables = gamePaintables;
     }
 
     /**
      * Set a paintable
      * @param index
      * @param p
      */
     public void setGamePaintable(int index, Paintable p) {
         myGamePaintables.set(index, p);
     }
 
     /**
      * Get List of Image sizes dimensions
      * @return
      */
     public List<Dimension> getImageSizes() {
         return myImageSizes;
     }
 
     /**
      * @param myImageSizes the myImageSizes to set
      */
     public void setSizes(List<Dimension> sizes) {
         myImageSizes = sizes;
     }
 
     /**
      * Sets the size
      */
     public void setSize(int index, Dimension dim) {
         myImageSizes.set(index, dim);
     }
 
     /**
      * @return the mySpriteLocations
      */
     protected List<Location> getLocations() {
         return myLocations;
     }
 
     /**
      * Clear lists
      */
     public void clear(){
         myGamePaintables.clear();
         myImageSizes.clear();
         myLocations.clear();
     }
 
     /**
      * Returns numbre of objects
      */
     @Override
 	public int ObjectNumber (){
         return myGamePaintables.size() + myHUDPaintables.size();
     }
 
     
     /**Empty update for 
      * heirarchy purposes. 
      */
     public void update(){
 
     }
 
 
     /**
      * Add to the HUD an element
      * @param element
      */
     public void addHUDDisplay (Paintable element) {
         myHUDPaintables.add(element);
     }
 
     /**
      * Clears hud
      */
     public void clearHUD () {
         myHUDPaintables.clear();
     }
 
     /**
      * Add effects
      * @param effects
      */
     public void addImageEffect(List<Integer> effects) {
         myImageEffects.add(effects);
     }
 
     /**
      * Return effects
      * @return myImageEffects
      */
     public List<List<Integer>> getImageEffects(){
         return myImageEffects;
     }
 
     /**
      * Returns effect at index
      */
     @Override
 	public List<Integer> getImageEffects(int index) {
         if (index >= myGamePaintables.size()) {
             return new ArrayList<Integer>();
         }
         return myImageEffects.get(index);
     }
 
 
 
 }
