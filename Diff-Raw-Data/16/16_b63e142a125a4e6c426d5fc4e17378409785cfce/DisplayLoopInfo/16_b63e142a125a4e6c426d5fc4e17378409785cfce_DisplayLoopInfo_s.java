 package vooga.fighter.controller.displayinformation;
 
 import java.util.List;
 
 import vooga.fighter.controller.interfaces.ViewDataSource;
 import vooga.fighter.model.Mode;
 import vooga.fighter.model.utils.ImageDataObject;
 import vooga.fighter.util.HUDFactory;
import vooga.fighter.util.Paintable;
 
 import vooga.fighter.view.HUDElement;
 
 /**
  * Display loop info for menus
  * Extends DisplayInfo and implements
  * ViewDataSource
  * 
  * @author Jack Matteucci
  * @author Wayne You
  * @author Jerry Li
  *
  */
 
 public class DisplayLoopInfo extends DisplayInfo implements ViewDataSource{
     private Mode myMode;
     private List<ImageDataObject> myImageData;
     
     /**
      * Constructor
      */
     public DisplayLoopInfo() {
         super();
     }
     
     /**
      * Constructor with mode, gets imagedata, from mode
      * @param mode
      */
     public DisplayLoopInfo(Mode mode) {
         myMode = mode;
     	myImageData = mode.getImageData();
         updateInfo();
     }
     
     /**
      * Adds HUDElements by checking the annotation and creating
      * the HUDElements
      */
     protected void addHUDElements () {
         try {
             for (HUDElement e : HUDFactory.getHUDElements(this)) {
                 addHUDDisplay(e);
             }
         }
         catch (InstantiationException e) {
             throw new NullPointerException("Could not instantiate HUDElement: " + e.getMessage());
         }
         catch (IllegalAccessException e) {
             throw new NullPointerException("Could not access member variable: " + e.getMessage());
         }
         catch (ClassNotFoundException e) {
             throw new NullPointerException("Could not find class: " + e.getMessage());
         }
     }
     
     /**
      * Updates by retrieving information from mode
      */
     public void updateInfo(){
     	getLocations().clear();
     	getGameObjects().clear();
     	getImageSizes().clear();
     	getImageEffects().clear();
     	myImageData = myMode.getImageData();
     	for(ImageDataObject data : myImageData){
     		getGameObjects().add((Paintable) data.getImage());
     		getLocations().add(data.getLocation());
     		getImageSizes().add(data.getSize());
     		getImageEffects().add(data.getImageEffect());
     	}
     }
     
     /**
      * Updates information
      */
     @Override
 	public void update() {
         updateInfo();
     }
     
     /**
      * clear imagedata
      */
     @Override
 	public void clear(){
     	super.clear();
         myImageData.clear();
     }
     
     /**
      * Returns mode
      * @return
      */
     public Mode getMode() {
         return myMode;
     }
 
 }
