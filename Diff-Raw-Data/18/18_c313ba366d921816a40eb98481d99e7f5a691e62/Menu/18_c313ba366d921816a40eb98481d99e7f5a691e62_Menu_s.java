 package vooga.rts.gui;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import vooga.rts.IGameLoop;
 import vooga.rts.util.Location;
 
 
 public abstract class Menu extends Observable implements IGameLoop, Observer {
 
     protected List<Button> myButtons;
     protected Image myImage;
     protected AffineTransform myTransform;
 
     public Menu () {
         myButtons = new ArrayList<Button>();
     }
 
     @Override
     public void update (double elapsedTime) {
 
     }
 
     protected void paintBG (Graphics2D pen) {
         if (myImage != null) {
             if (myTransform == null) {
                myTransform = new AffineTransform();
                double sx = pen.getDeviceConfiguration().getBounds().getWidth();
                sx /= myImage.getWidth(null);
                double sy = pen.getDeviceConfiguration().getBounds().getHeight();
                sy /= myImage.getHeight(null);
                myTransform.scale(sx, sy);
             }
             pen.drawImage(myImage, myTransform, null);
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         paintBG(pen);
         
 
         for (Button b : myButtons) {
             b.paint(pen);
         }
     }
 
     public void setBGImage (Image i) {
         myImage = i;
         myTransform = null;
     }
 
     public void addButton (Button b) {
         myButtons.add(b);
         b.addObserver(this);
     }
 
     public void handleMouseDown (int x, int y) {
         for (Button b : myButtons) {
             if (b.checkWithinBounds(x, y)) {
                 b.processClick();
             }
         }
     }
     
     public void handleMouseDown (Location l) {
         handleMouseDown((int) l.getX(), (int) l.getY());
     }
 
     public void handleMouseMovement (int x, int y) {
         for (Button b : myButtons) {
             if (b.checkWithinBounds(x, y)) {
                 b.setFocused(true);
             }
             else {
                 b.setFocused(false);
             }
         }
     }
     
     public void handleMouseMovement (Location l) {
         handleMouseMovement((int) l.getX(), (int) l.getY());
     }
 
     @Override
     public void update (Observable o, Object arg) {
         setChanged();
         notifyObservers(arg);
     }
 
 }
