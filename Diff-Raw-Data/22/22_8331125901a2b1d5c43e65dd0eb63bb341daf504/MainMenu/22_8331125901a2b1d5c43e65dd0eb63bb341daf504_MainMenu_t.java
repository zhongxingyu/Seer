 package vooga.rts.gui.menus;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.Observable;
 import vooga.rts.gui.Menu;
 import vooga.rts.gui.Window;
 import vooga.rts.gui.buttons.MainMenuButton;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Location;
import vooga.rts.util.Scale;
 
 
 public class MainMenu extends Menu {
 
     public static final String DEFAULT_BGIMAGE_LOCATION = "images/backgrounds/menu_bg.png";
     public BufferedImage myBGImage;
     private Dimension myDefaultButtonDimension = new Dimension(500, 122);
 
     private Location myDefaultButtonLocation = new Location(678, 500);
 
     private int ySpacing = 160;
 
     private double myX;
     private double myY;
 
     private MainMenuButton mySingleButton;
     private MainMenuButton myMultiButton;
 
     public MainMenu () {
         super();
 
         myBGImage =
                 ResourceManager.getInstance().<BufferedImage> getFile(DEFAULT_BGIMAGE_LOCATION,
                                                                       BufferedImage.class);
         setBGImage(myBGImage);
 
         mySingleButton =
                 new MainMenuButton("Single Player", myDefaultButtonDimension,
                                    myDefaultButtonLocation, 0);
         setScaledButton(mySingleButton);
 
         addButton(mySingleButton);
 
         myMultiButton =
                 new MainMenuButton("Multi Player", myDefaultButtonDimension,
                                    new Location(myDefaultButtonLocation.getX(),
                                                 (myDefaultButtonLocation.getY() + ySpacing)), 1);
 
         setScaledButton(myMultiButton);
 
         addButton(myMultiButton);
     }
 
     public void setScaledButton (MainMenuButton b) {
         myX = Window.D_X;
         myY = Window.D_Y;
         double xFactor = myX / myBGImage.getWidth();
         double yFactor = myY / myBGImage.getHeight();
         b.setSize(new Dimension((int) (b.getSize().getWidth() * xFactor),
                                 (int) (b.getSize().getHeight() * yFactor)));
 
         b.setPos(new Location((int) (b.getPos().getX() * xFactor),
                               (int) (b.getPos().getY() * yFactor)));
     }
 
     @Override
     public void paint (Graphics2D pen) {
 
         super.paint(pen);
         pen.setFont(new Font("Helvetica", Font.BOLD, 100));
         pen.setColor(Color.white);
         pen.drawString("The Vooga RTS Game", 150, 150);
 
     }
     
     @Override
     public void update(Observable o, Object a) {
         if (o instanceof MainMenuButton) {
             setChanged();
             int s = (Integer) a;
             notifyObservers(s);
         }
     }
 }
