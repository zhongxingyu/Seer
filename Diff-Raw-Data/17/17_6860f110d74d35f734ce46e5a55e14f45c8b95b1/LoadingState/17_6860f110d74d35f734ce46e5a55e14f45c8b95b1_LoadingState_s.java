 package vooga.rts.state;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.util.Observer;
 import javax.swing.ImageIcon;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.resourcemanager.ResourceManager;
 
 /**
  * The state of the game while its loading. Once it is done, a simple click will 
  * advance to the menu state.
  * @author challenherzberg-brovold
  *
  */
 
 public class LoadingState extends SubState {
 
     AffineTransform myTransform;
     BufferedImage myBGImage;
     ImageIcon myLoadingImage;
 
     public static final String DEFAULT_BGIMAGE_LOCATION = "images/backgrounds/loading_bg.png";
     public static final String DEFAULT_LOADING_LOCATION = "images/ajax_loader_gray_300.gif";
 
     private MainState myMain;
 
     public LoadingState (Observer observer) {
         super(observer);
         if (observer instanceof MainState) {
             myMain = (MainState) observer;
         }
         myBGImage =
                 ResourceManager.getInstance().<BufferedImage> getFile(DEFAULT_BGIMAGE_LOCATION,
                                                                       BufferedImage.class);
         myLoadingImage =
                 new ImageIcon(ResourceManager.getInstance()
                         .<BufferedImage> getFile(DEFAULT_LOADING_LOCATION, BufferedImage.class));
     }
 
     @Override
     public void update (double elapsedTime) {
         if (!isLoading()) {
             setChanged();
             notifyObservers();
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         myTransform = new AffineTransform();
        double sx = pen.getDeviceConfiguration().getBounds().getWidth();
         sx /= myBGImage.getWidth();
        double sy = pen.getDeviceConfiguration().getBounds().getHeight();
         sy /= myBGImage.getHeight();
         myTransform.scale(sx, sy);
         pen.drawImage(myBGImage, myTransform, null);
         Rectangle screen = pen.getDeviceConfiguration().getBounds();
         pen.setColor(Color.white);
         pen.setFont(new Font("Georgia", Font.PLAIN, 72));
         pen.drawString("Game is Loading...", 200, 300);
         pen.setFont(new Font("Georgia", Font.PLAIN, 30));
         pen.drawString("Please Wait...", 200, 380);
         if (!isLoading()) {
             pen.setFont(new Font("Georgia", Font.PLAIN, 30));
             pen.drawString("Click to start.", 200, 380);
         }        
     }
 
     @Override
     public void receiveCommand (Command command) {
         if (command.getMethodName().equals(ClickCommand.LEFT_CLICK)) {
             update(0);
         }
     }
 
     /**
      * 
      * @return whether or not the game is finished loadings
      */
     private boolean isLoading () {
         return ResourceManager.getInstance().isLoading() || !myMain.isReady();
     }
 }
