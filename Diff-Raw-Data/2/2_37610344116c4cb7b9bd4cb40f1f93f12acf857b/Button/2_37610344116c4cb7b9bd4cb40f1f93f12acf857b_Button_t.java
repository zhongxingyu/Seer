 package vooga.rts.gui;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.Observable;
 import vooga.rts.IGameLoop;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Location;
 
 
 public abstract class Button extends Observable implements IGameLoop {
 
     private BufferedImage myImage;
     protected Dimension mySize;
     protected Location myPos;
     protected boolean isFocused;
 
     /*
      * TODO: Add onFocus behavior for each button.
      */
 
     public Button (String image, Dimension size, Location pos) {
         if (image != null) {
            myImage = ResourceManager.getInstance().<BufferedImage> getFile(image, BufferedImage.class);
         }
         mySize = size;
         myPos = pos;
         isFocused = false;
     }
 
     @Override
     public abstract void update (double elapsedTime);
 
     @Override
     public void paint (Graphics2D pen) {
         if (myImage != null) {
             pen.drawImage(myImage, (int) myPos.x, (int) myPos.y, mySize.width, mySize.height, null);
         }
     }
 
     public abstract void processClick ();
 
     public abstract void processHover ();
 
     public boolean checkWithinBounds (int x, int y) {
         return (x > myPos.x && y > myPos.y && x < (myPos.x + mySize.width) && y < (myPos.y + mySize.height));
     }
 
     public Dimension getSize () {
         return mySize;
     }
 
     public void setSize (Dimension s) {
         mySize = s;
     }
 
     public Location getPos () {
         return myPos;
     }
 
     public void setPos (Location l) {
         myPos = l;
     }
 
     public void setFocused (boolean b) {
         isFocused = b;
     }
 
 }
