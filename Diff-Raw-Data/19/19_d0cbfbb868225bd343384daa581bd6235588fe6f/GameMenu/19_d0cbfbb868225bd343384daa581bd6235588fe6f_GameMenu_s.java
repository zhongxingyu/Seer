 package vooga.rts.gui.menus;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import javax.imageio.ImageIO;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.InformationCommand;
 import vooga.rts.commands.PositionCommand;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gui.Button;
 import vooga.rts.gui.Menu;
 import vooga.rts.gui.Window;
 import vooga.rts.gui.buttons.ActionButton;
 import vooga.rts.gui.buttons.ImageButton;
 import vooga.rts.gui.buttons.MainMenuButton;
 import vooga.rts.gui.buttons.TextButton;
 import vooga.rts.manager.Manager;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location;
 
 
 public class GameMenu extends Menu {
 
     private static final String BG_IMAGE_LOCATION = "images/gamemenu/menu_bg.png";
     private static final String EXIT_IMAGE_LOCATION = "images/gamemenu/menu_button.png";
     private static final String ACTION_IMAGE_LOCATION = "images/gamemenu/action_button.png";
     private BufferedImage myBGImage;
     private static final Dimension ACTION_BUTTON_DIMENSION = new Dimension(50, 50);
     private static final Dimension EXIT_BUTTON_DIMENSION = new Dimension(200, 40);
 
     private static final Location EXIT_BUTTON_LOCATION =
             new Location(Window.SCREEN_SIZE.getWidth() - EXIT_BUTTON_DIMENSION.getWidth(), 0);
     private static final int ACTION_MENU_WIDTH = 360;
     private static final int ACTION_MENU_HEIGHT = 175;
     private static final int ACTION_X_PADDING = 30;
     private static final int ACTION_Y_PADDING = 30;
     private static final int MAX_ACTION_BUTTONS = 8;
     private static final int S_X = (int) Window.SCREEN_SIZE.getWidth();
     private static final int S_Y = (int) Window.SCREEN_SIZE.getHeight();
 
     private InteractiveEntity mySelectedEntity;
 
     private Button myExitButton;
     private ArrayList<Button> myActionButtons;
     Location[] myActionButtonLocations;
 
     public GameMenu () {
 
         myBGImage =
                 ResourceManager.getInstance().<BufferedImage> getFile(BG_IMAGE_LOCATION,
                                                                       BufferedImage.class);
         setBGImage(myBGImage);
 
         myExitButton =
                 new ImageButton(EXIT_IMAGE_LOCATION, EXIT_BUTTON_DIMENSION, EXIT_BUTTON_LOCATION);
         addButton(myExitButton);
 
         myActionButtons = new ArrayList<Button>();
         createActionButtonLocations();
     }
 
     public void createActionButtonLocations () {
         myActionButtonLocations = new Location[MAX_ACTION_BUTTONS];
         for (int i = 0; i < 4; i++) {
             myActionButtonLocations[i] =
                     new Location((S_X - ACTION_MENU_WIDTH) +
                                  (ACTION_BUTTON_DIMENSION.getWidth() * i) +
                                  (ACTION_X_PADDING * (i + 1)),
                                  (S_Y - ACTION_MENU_HEIGHT) + (ACTION_Y_PADDING));
             System.out.println("Location: " + myActionButtonLocations[i].getX() + " " +
                                myActionButtonLocations[i].getY());
         }
 
         for (int i = 0; i < 4; i++) {
             int l = 4 + i;
             myActionButtonLocations[l] =
                     new Location((S_X - ACTION_MENU_WIDTH) +
                                  (ACTION_BUTTON_DIMENSION.getWidth() * i) +
                                  (ACTION_X_PADDING * (i + 1)),
                                  (S_Y - ACTION_MENU_HEIGHT) + ((ACTION_Y_PADDING) * 2) +
                                          ACTION_BUTTON_DIMENSION.getHeight());
             System.out.println("Location: " + myActionButtonLocations[i].getX() + " " +
                                myActionButtonLocations[l].getY());
         }
     }
 
     private void updateActionButtons () {
         myActionButtons.clear();
         myButtons.clear();
        if (mySelectedEntity == null || mySelectedEntity.getCommands().isEmpty()) return;
 
         Iterator<InformationCommand> i = mySelectedEntity.getCommands().iterator();
         int k = 0;
         while (i.hasNext()) {
             Information info = i.next().getInfo();
             BufferedImage image = info.getButtonImage();
             Button b;
             if (image == null) {
                 b =
                         new ImageButton(ACTION_IMAGE_LOCATION, ACTION_BUTTON_DIMENSION,
                                         myActionButtonLocations[k]);
             } else {
                 b =
                     new ImageButton(info.getButtonImage(), ACTION_BUTTON_DIMENSION,
                                     myActionButtonLocations[k]);
             }
             myActionButtons.add(b);
             addButton(b);
             k++;
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
 
         int screenX = (int) Window.SCREEN_SIZE.getWidth();
         int screenY = (int) Window.SCREEN_SIZE.getHeight();
 
         int bgImgHeight = myBGImage.getHeight();
         int bgImgWidth = myBGImage.getWidth();
 
         int x = 0;
 
         double xFactor = (double) screenX / (double) bgImgWidth;
         int newHeight = (int) (xFactor * bgImgHeight);
         int y = screenY - newHeight;
 
         pen.drawImage(myImage, x, y, screenX, newHeight, null);
 
         pen.setFont(new Font("Arial", Font.PLAIN, 24));
         if (mySelectedEntity != null) {
             if (mySelectedEntity.getInfo() != null) {
                 pen.setColor(Color.WHITE);
 
                 pen.drawString(mySelectedEntity.getInfo().getName(),
                                (int) 280,
                                (int) Window.SCREEN_SIZE.getHeight() - 90);
                 pen.setFont(new Font("Arial", Font.PLAIN, 20));
                 pen.setColor(Color.GRAY);
                 pen.drawString(mySelectedEntity.getInfo().getDescription(),
                                (int) 280,
                                (int) Window.SCREEN_SIZE.getHeight() - 65);
                 if (mySelectedEntity.getInfo().getButtonImage() != null) {
                     pen.drawImage(mySelectedEntity.getInfo().getButtonImage(),
                                   (int) 220,
                                   (int) Window.SCREEN_SIZE.getHeight() - 90, null);
                 }
             }
         }
         updateActionButtons();
 
         for (Button b : myButtons) {
             b.paint(pen);
         }
 
     }
 
     @Override
     public void update (Observable o, Object arg) {
         if (o instanceof Manager) {
             Boolean b = (Boolean) arg;
             Manager m = (Manager) o;
             if (b) {
                 setSelected(m.getSelected());
             }
             else {
                 setDeselected();
             }
         }
 
         if (o.equals(myExitButton)) {
             System.exit(0);
         }
         if (o instanceof ActionButton) {
             ActionButton a = (ActionButton) o;
             Integer id = a.getID();
 
             setChanged();
             notifyObservers(id);
         }
     }
 
     private void setDeselected () {
         mySelectedEntity = null;
     }
 
     private void setSelected (List<InteractiveEntity> selected) {
         mySelectedEntity = selected.get(0); // The current select method if there is more than
                                             // one is just to choose the first one
         updateActionButtons();
     }
 
 }
