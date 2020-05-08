 package vooga.rts.leveleditor.components;
 
 import java.awt.Image;
 import vooga.rts.gamedesign.sprite.gamesprites.Resource;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 /**
  * This class represents the available resource types the designer can choose from
  * 
  * @author Yang Yang
  *
  */
 
 public class EditableResource extends Resource {
     
     private int myID;
     private String myImageName;
     private int myAmount;   
     
     
     /**
      * the constructor for editable resource
      * @param image of the resource
      * @param center of the resource
      * @param id of the resource
      * @param name of the resource
      * @param imageName of the resource
      * @param amount of the resource
      */
     public EditableResource(Pixmap image, Location3D center , int id, 
                             String name , String imageName, int amount) {
         super(image, center, image.getMyDimension(), 0, amount, name);
         myID = id;
         myImageName = imageName;
     }
     /**
      * construtor for editableResource
      * @param image image for this resource
      * @param x x position 
      * @param y y position
      * @param z z position
      * @param id id of the resource
      * @param name of the resource
      * @param imageName of the resource
      * @param amount of the resource
      */
     
     public EditableResource(Pixmap image, int x , int y , int z , 
                             int id, String name , String imageName, int amount) {
        this(image, new Location3D(x, y, z), id, name, imageName, amount);
     }
     
     /**
      * constructor
      * @param image image of the resource
      * @param x x position
      * @param y y position
      * @param layerCount layer number
      * @param layerHeight height of the layer
      * @param id id of the resource
      * @param name name of the resource
      * @param imageName image name of the resource
       * @param amount amount of the resource
      */
     public EditableResource(Pixmap image, int x , int y , 
                             int layerCount , int layerHeight, int id, String name , String imageName, int amount) {
         this(image, x, y, layerCount * layerHeight, id, name, imageName, amount);
     }
     /**
      * return the id 
      * @return int
      */
 
     public int getMyID () {
         return myID;
     }   
    
     /**
      * get the image name
      * @return String
      */
     public String getMyImageName () {
         return myImageName;
     }
 
     /**
      * get the amount of resource
      * @return int 
      */
     public int getMyAmount () {
         return myAmount;
     }
 
     /**
      * set the amount of resource
      * @param amount resource amount
      */
     public void setAmount(int amount) {
         myAmount = amount;
         
     }  
     /**
      * return the image of the resource
      * @return
      */
     public Image getMyImage() {
         return super.getImage().getMyImage();
     }
 }
