 /*
  * Dog - A project for making highly scalable non-clustered game and simulation environments.
  * Copyright (C) 2009-2010 BlinzProject <gtalent2@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as
  * published by the Free Software Foundation.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.blinz.dog.zone;
 
 import java.util.ArrayList;
 import java.util.Vector;
 import net.blinz.core.graphics.Graphics;
 import net.blinz.core.input.KeyListener;
 import net.blinz.core.input.MouseListener;
 import net.blinz.core.input.MouseWheelListener;
 import net.blinz.core.util.Bounds;
 import net.blinz.core.util.Position;
 import net.blinz.dog.input.ClickEvent;
 import net.blinz.dog.input.KeyEvent;
 import net.blinz.dog.input.MouseEvent;
 import net.blinz.dog.input.MouseWheelEvent;
 import net.blinz.dog.util.User;
 import net.blinz.dog.zone.SelectableSprite.Selection;
 import net.blinz.dog.zone.UserListenerCatalog.UserListenerList;
 
 /**
  * Camera acts as an interface between a the user, and the Zone.
  * It delivers all necessary images to the screen and allows input to travel to
  * the Zone and appropriate sprites.
  * @author Blinz
  */
 public class Camera extends BaseCamera {
 
     /**
      * Class used to read all input for the Camera.
      */
     private final class InputListener implements MouseListener, MouseWheelListener, KeyListener {
 
         /**
          * Constructor
          */
         private InputListener() {
         }
 
         @Override
         public void buttonClick(int buttonNumber, int clickCount, int cursorX, int cursorY) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final ClickEvent e = new ClickEvent(getUser(), buttonNumber,
                         cursorX + getX(), cursorY + getY(), clickCount);
                 ul.buttonClick(e);
                 selections.add(new Position(cursorX, cursorY));
             }
         }
 
         @Override
         public void buttonPress(int buttonNumber, int cursorX, int cursorY) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final MouseEvent e = new MouseEvent(getUser(), buttonNumber, cursorX + getX(), cursorY + getY());
                 ul.buttonPress(e);
             }
         }
 
         @Override
         public void buttonRelease(int buttonNumber, int cursorX, int cursorY) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final MouseEvent e = new MouseEvent(getUser(), buttonNumber, cursorX + getX(), cursorY + getY());
                 ul.buttonRelease(e);
             }
         }
 
         @Override
         public void wheelScroll(int number, int cursorX, int cursorY) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final MouseWheelEvent e = new MouseWheelEvent(getUser(), number, cursorX + getX(), cursorY + getY());
                 ul.wheelScroll(e);
             }
         }
 
         @Override
         public void keyPressed(int key) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final KeyEvent e = new KeyEvent(getUser(), key);
                 ul.keyPressed(e);
             }
         }
 
         @Override
         public void keyReleased(int key) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final KeyEvent e = new KeyEvent(getUser(), key);
                 ul.keyReleased(e);
             }
         }
 
         @Override
         public void keyTyped(int key) {
             final UserListenerList ul = userListeners;
             if (ul != null) {
                 final KeyEvent e = new KeyEvent(getUser(), key);
                 ul.keyTyped(e);
             }
         }
 
         @Override
         protected void finalize() throws Throwable {
             getData().userListeners.checkIn(getUser());
             super.finalize();
         }
     }
     private final Vector<CameraSprite> spriteList = new Vector<CameraSprite>();
     private CameraSprite selected;
     /**
      * Used to keep track of how click inputs affect the selected sprite.
      */
     private final Vector<Position> selections = new Vector<Position>();
     private Scene scene = new Scene();
     private Scene swap1 = new Scene();
     private Scene swap2 = new Scene();
     private InputListener inputListener;
     private UserListenerList userListeners;
 
     /**
      * Constructor for Camera.
      */
     public Camera() {
         this(new User());
     }
 
     /**
      * Constructor
      * @param user the User associated with this Camera
      */
     public Camera(final User user) {
         super(user);
     }
 
     /**
      * Draws this Camera. A single given Camera should only be drawn by one
      * thread at a time, and thus by only one Screen.
      * @param graphics the Graphics object with which this is to be drawn
      */
     public synchronized final void draw(final Graphics graphics) {
         Scene s = scene;
         while (!s.lock()) {
             s = scene;
         }
         s.draw(graphics);
         s.unLock();
     }
 
     /**
      * Gets the input listener Used to direct input by the User owning this
      * Camera to sprites. Add this to the input context that the Camera will
      * listen to.
      * @return an Mouse, MouseWheel, Key listener object
      */
     public final synchronized Object getInputListener() {
         return inputListener == null ? inputListener = new InputListener() : inputListener;
     }
 
     /**
      * Drops the current zone, the Camera will have no Zone to moniter after
      * this method is called.
      * @param zone the Zone that this currently follows and you intend to drop
      */
     @Override
     final synchronized void dropZone(final Zone zone) {
        if (getZone() == zone) {
             getData().userListeners.checkIn(getUser());
             inputListener = null;
             super.dropZone(zone);
         }
     }
 
     @Override
     final void addSprite(final CameraSprite cs) {
         //add the sprite to sprite list at the proper location
         synchronized (spriteList) {
             if (spriteList.isEmpty()) {
                 spriteList.add(cs);
                 return;
             }
             for (int i = spriteList.size() - 1; i > -1; i--) {
                 if (spriteList.get(i).getLayer() <= cs.getLayer()) {
                     spriteList.insertElementAt(cs, i + 1);
                     break;
                 }
             }
         }
     }
 
     @Override
     final void removeOrphanedSprites(final ArrayList<CameraSprite> orphans) {
         //remove remaining orphans from spriteList
         for (int i = 0; i < spriteList.size(); i++) {
             if (spriteList.get(i).isOrphaned()) {
                 spriteList.remove(i);
                 i--;
             }
         }
     }
 
     @Override
     final void internalInit() {
         super.internalInit();
         userListeners = getData().userListeners.checkOut(getUser());
     }
 
     /**
      * Updates the Camera.
      */
     @Override
     final void internalUpdate() {
         super.internalUpdate();
         processSpriteSelection();
         generateCurrentScene();
     }
 
     /**
      * Gets a Scene that is safe to write to.
      * @return a Scene that is safe to write to
      */
     private final Scene getScene() {
         Scene retval = null;
         while (retval == null) {
             if (swap1.lock()) {
                 retval = swap1;
             } else if (swap2.lock()) {
                 retval = swap2;
             }
         }
         retval.width = getWidth();
         retval.height = getHeight();
         return retval;
     }
 
     /**
      * Processes all sprite selection from this Camera within the last round.
      */
     private final void processSpriteSelection() {
         while (!selections.isEmpty()) {
 
             final Position selection = selections.remove(selections.size() - 1);
             final int x = selection.x;
             final int y = selection.y;
             final CameraSprite oldSelected = selected;
 
 
             CameraSprite newSelected = null;
             for (int i = spriteList.size() - 1; i > -1; i--) {
                 final BaseSprite s = spriteList.get(i).getSprite();
                 if (Bounds.intersects(s.getX(), s.getY(), s.getWidth(), s.getHeight(), x + getX(), y + getY(), 1, 1)) {
                     newSelected = spriteList.get(i);
                     if (newSelected.select(getUser()) == Selection.ACCEPT) {
                         selected = newSelected;
                         break;
                     } else if (newSelected.select(getUser()) == Selection.REJECT_STOP) {
                         break;
                     }
                 }
             }
             if (oldSelected != null && oldSelected != newSelected) {
                 oldSelected.deselect(getUser());
             }
         }
     }
 
     /**
      * Finds the sprite currently on screen, gathers them, orders them by layer
      * and sets it to the current scene.
      */
     private final void generateCurrentScene() {
         final Scene upcoming = getScene();
         upcoming.manageSceneSprites();
 
         upcoming.setTranslation(getX(), getY());
         upcoming.setSize(getWidth(), getHeight());
         for (int i = 0; i < spriteList.size(); i++) {
             final CameraSprite s = spriteList.get(i);
             if (Bounds.intersects(s.getX(), s.getY(), s.getWidth(), s.getHeight(),
                     upcoming.translationX, upcoming.translationY, upcoming.width, upcoming.height)) {
                 upcoming.add(s);
             }
         }
 
         upcoming.sortLayers();
         upcoming.unLock();
         scene = upcoming;
     }
 }
