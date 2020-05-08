 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009-2010  Blinz <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.world;
 
 import org.blinz.util.User;
 import org.blinz.graphics.Graphics;
 import org.blinz.util.Bounds;
 import org.blinz.input.MouseListener;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import org.blinz.input.ClickEvent;
 import org.blinz.input.KeyEvent;
 import org.blinz.input.KeyListener;
 import org.blinz.input.MouseEvent;
 import org.blinz.input.MouseWheelEvent;
 import org.blinz.input.MouseWheelListener;
 import org.blinz.world.UserListenerCatalog.UserListenerList;
 
 /**
  * Camera acts as an interface between a the user, and the Zone.
  * It delivers all necessary images to the screena and allows input to travel to
  * the Zone.
  * @author Blinz
  */
 public class Camera extends ZoneObject {
 
     /**
      * Determines whether or not this Camera represents a local user. True by default.
      */
     private final Bounds bounds = new Bounds();
     private final Hashtable<BaseSprite, CameraSprite> sprites =
             new Hashtable<BaseSprite, CameraSprite>();
     private final Vector<CameraSprite> selectableSprites = new Vector<CameraSprite>();
     private final Vector<BaseSprite> spritesToRemove = new Vector<BaseSprite>();
     private Zone zone;
     private User user;
     private Scene scene = new Scene();
     private Scene swap1 = new Scene();
     private Scene swap2 = new Scene();
     private InputListener inputListener;
 
     /**
      * Constructer for Camera.
      */
     public Camera() {
         this(new User());
     }
 
     /**
      * Creates a new Camera with the given User as this Camera's User.
      * @param user
      */
     public Camera(User user) {
         this.user = user;
     }
 
     /**
      *
      * @return the User that this Camera represents.
      */
     public final User getUser() {
         return user;
     }
 
     /**
      * Returns an input listener Used to direct input by the User owning this 
      * Camera to sprites.
      * @return an Mouse, MouseWheel, Key listener object
      */
     public final synchronized Object getInputListener() {
         return inputListener == null ? inputListener = new InputListener() : inputListener;
     }
 
     /**
      * Sets the zone of this Camera. In addition to setting the Zone it also
      * removes the old Zone.
      * @param zone
      */
     public final synchronized void setZone(Zone zone) {
         dropZone();
         this.zone = zone;
         zone.addCamera(this);
     }
 
     /**
      * Drops the current zone, the Camera will have no Zone to moniter after
      * this method is called.
      */
     public final void dropZone() {
         if (zone != null) {
             zone.removeCamera(this);
             selectableSprites.clear();
             sprites.clear();
             spritesToRemove.clear();
             zone = null;
             inputListener = null;
         }
     }
 
     /**
      * 
      * @return the x location of this Camera
      */
     public final int getX() {
         return bounds.x;
     }
 
     /**
      * 
      * @return the y location of this Camera
      */
     public final int getY() {
         return bounds.y;
     }
 
     /**
      * 
      * @return the width of this Camera
      */
     public final int getWidth() {
         return bounds.width;
     }
 
     /**
      * 
      * @return the height of this Camera
      */
     public final int getHeight() {
         return bounds.height;
     }
 
     public final void setSize(final int width, final int height) {
         setWidth(width);
         setHeight(height);
     }
 
     /**
      * Sets the width of this Camera to that given.
      * @param width
      */
     public final void setWidth(final int width) {
         if (zone != null) {
             if (getData().getSectorOfSafe(bounds.x2(), bounds.y)
                     != getData().getSectorOfSafe(bounds.x + width, bounds.y)) {
                 if (width > bounds.width) {
                     //Growing
                     joinSectors(bounds.x2() + getData().sectorWidth, bounds.y, bounds.x + width, bounds.y2());
                 } else {
                     //Shrinking
                     leaveSectors(bounds.x + width + getData().sectorWidth, bounds.y, bounds.x2(), bounds.y2());
                 }
             }
         }
         bounds.width = width;
     }
 
     /**
      * Sets the height of this Camera to that given.
      * @param height
      */
     public final void setHeight(final int height) {
         if (zone != null) {
             if (getData().getSectorOfSafe(bounds.x, bounds.y2())
                     != getData().getSectorOfSafe(bounds.x, bounds.y + height)) {
                 if (height > bounds.height) {
                     //Growing
                     joinSectors(bounds.x, bounds.y + getData().sectorHeight, bounds.x2(), bounds.y + height);
                 } else {
                     //Shrinking
                     leaveSectors(bounds.x, bounds.y + height + getData().sectorHeight, bounds.x2(), bounds.y2());
                 }
             }
         }
         bounds.height = height;
     }
 
     /**
      * Sets the x coordinate of this Camera to the given value.
      * @param x
      */
     public final void setX(final int x) {
         if (x < bounds.x) {
             moveLeft(bounds.x - x);
         } else {
             moveRight(x - bounds.x);
         }
     }
 
     /**
      * Sets the y coordinate of this Camera to the given value.
      * @param y
      */
     public final void setY(final int y) {
         if (y < bounds.y) {
             moveUp(bounds.y - y);
         } else {
             moveDown(y - bounds.y);
         }
     }
 
     /**
      * Sets the location of this Camera.
      * @param x
      * @param y
      */
     public final void setPosition(final int x, final int y) {
         setX(x);
         setY(y);
     }
 
     /**
      * Moves this Camera up the specified distance.
      * @param distance
      */
     public final void moveUp(final int distance) {
         if (distance < 0) {
             moveDown(-distance);
             return;
         }
 
         final int newY = bounds.y - distance;
 
         if (zone != null) {
             //Leave old Sectors
             if (sector2() != getData().getSectorOfSafe(bounds.x2(), bounds.height + newY)) {
                 final int y1 = newY + bounds.height < bounds.y ? bounds.y : newY + getData().sectorHeight();
                 leaveSectors(bounds.x, y1, bounds.x2(), bounds.y2());
             }
 
             //Join new Sectors
             if (sector1() != getData().getSectorOfSafe(bounds.x, newY)) {
                 final int y2 = newY + bounds.height < bounds.y ? bounds.y - getData().sectorHeight()
                         : newY + bounds.height;
                 joinSectors(bounds.x, newY, bounds.x2(), y2);
             }
         }
         bounds.y = newY;
     }
 
     /**
      * Moves this Camera down the specified distance.
      * @param distance
      */
     public final void moveDown(final int distance) {
         if (distance < 0) {
             moveUp(-distance);
             return;
         }
 
         final int newY = bounds.y + distance;
 
         if (zone != null) {
             //Join new Sectors
            if (sector2() != getData().getSectorOfSafe(bounds.x2(), bounds.height + newY)) {
                 final int y1 = newY > bounds.y2()
                         ? newY : bounds.y + getData().sectorHeight();
                 joinSectors(bounds.x, y1, bounds.x2(), newY + bounds.height);
             }
 
             //Leave old Sectors
             if (sector1() != getData().getSectorOfSafe(bounds.x, newY)) {
                 final int y2 = newY < bounds.y2() ? newY - getData().sectorHeight() : bounds.y2();
                 leaveSectors(bounds.x, newY, bounds.x2(), y2);
             }
         }
 
         bounds.y = newY;
     }
 
     /**
      * Moves this Camera right the specified distance.
      * @param distance
      */
     public final void moveRight(final int distance) {
         if (distance < 0) {
             moveLeft(-distance);
             return;
         }
 
         final int newX = bounds.x + distance;
 
         if (zone != null) {
             //Join new Sectors
            if (sector2() != getData().getSectorOfSafe(bounds.width + newX, bounds.y + bounds.height)) {
                 final int x1 = newX > bounds.x2()
                         ? newX : bounds.x + getData().sectorWidth();
                 joinSectors(x1, bounds.y, newX + bounds.width, bounds.y2());
             }
 
             //Leave old Sectors
            if (sector1() != getData().getSectorOfSafe(bounds.x, newX)) {
                 final int x2 = newX < bounds.x2() ? newX - getData().sectorWidth() : bounds.x2();
                 leaveSectors(newX, bounds.y, x2, bounds.y2());
             }
         }
 
         bounds.x = newX;
     }
 
     /**
      * Moves this Camera left the specified distance.
      * @param distance
      */
     public final void moveLeft(final int distance) {
         if (distance < 0) {
             moveRight(-distance);
             return;
         }
 
         final int newX = bounds.x - distance;
 
         if (zone != null) {
             //Leave old Sectors
            if (sector2() != getData().getSectorOfSafe(newX + bounds.width, bounds.y + bounds.height)) {
                 final int x1 = newX + bounds.width < bounds.x ? bounds.x : newX + getData().sectorWidth();
                 leaveSectors(x1, bounds.y, bounds.x2(), bounds.y2());
             }
 
             //Join new Sectors
             if (sector1() != getData().getSectorOfSafe(bounds.x, newX)) {
                 final int x2 = newX + bounds.width < bounds.x ? bounds.x - getData().sectorWidth()
                         : newX + bounds.width;
                 joinSectors(newX, bounds.y, x2, bounds.y2());
             }
         }
 
         bounds.x = newX;
     }
 
     /**
      * Draws this Camera. A single given Camera should only be drawn by one
      * thread at a time, and thus by only one Screen.
      * @param graphics
      */
     public synchronized final void draw(final Graphics graphics) {
         Scene s = scene;
         while (!s.lock()) {
             s = scene;
         }
         s.draw(graphics);
         s.unLock();
     }
 
     @Override
     final void init() {
         for (int i = 0; i < getData().sectors.length; i++) {
             for (int n = 0; n < getData().sectors[i].length; n++) {
                 if (getData().sectors[i][n].intersects(bounds)) {
                     getData().sectors[i][n].addCamera(this);
                 }
             }
         }
         initCamera();
     }
 
     /**
      * Adds the sprites on this list to the specified Sector's representation in
      * this Camera.
      * @param sprites
      */
     final void addSprites(Vector<BaseSprite> sprites) {
         for (int i = 0; i < sprites.size(); i++) {
             addSprite(sprites.get(i));
         }
     }
 
     /**
      * Adds a Sprite to the Camera. The Sprite is stored in a CameraSprite.
      * @param sprite
      */
     final void addSprite(BaseSprite sprite) {
         CameraSprite s = sprites.get(sprite);
         if (s != null) {
             s.incrementUseCount();
         } else {
             s = new CameraSprite(sprite);
             sprites.put(sprite, s);
             if (sprite instanceof SelectibleSprite) {
                 selectableSprites.add(s);
                 sortByLayer(selectableSprites, 0, selectableSprites.size());
             }
         }
     }
 
     /**
      * Decrements the usage count for the Sprite's CameraSprite.
      * @param sprite
      */
     final void decrementSpriteUsage(BaseSprite sprite) {
         CameraSprite w = sprites.get(sprite);
         if (w != null) {
             w.decrementUseCount();
             if (w.getUsageCount() < 1) {
                 spritesToRemove.add(sprite);
             }
         }
     }
 
     /**
      * Updates the Camera.
      */
     final void internalUpdate() {
         removeStaleSprites();
         update();
         generateCurrentScene();
     }
 
     /**
      * Called after each cycle of this Camera's Zone. Does nothing, for implementing
      * as needed.
      */
     protected void update() {
     }
 
     /**
      * Called after the Camera recieves a Zone. Does nothing, for implementing
      * as needed.
      */
     protected void initCamera() {
     }
 
     /**
      * Finds the sprite currently on screen, gathers them, orders them by layer
      * and sets it to the current scene.
      */
     private final void generateCurrentScene() {
         final Scene upcoming = getScene();
         upcoming.manageContainers();
 
         upcoming.translation.setPosition(getX(), getY());
 
         Bounds b = new Bounds();
         b.setPosition(upcoming.translation);
         b.setSize(upcoming.size);
         Enumeration<CameraSprite> spriteList = sprites.elements();
         while (spriteList.hasMoreElements()) {
             CameraSprite s = spriteList.nextElement();
             if (b.intersects(s.getX(), s.getY(), s.getWidth(), s.getHeight())) {
                 upcoming.add(s);
             }
         }
 
         upcoming.sortLayers();
 
         b = null;
 
         upcoming.unLock();
         scene = upcoming;
     }
 
     /**
      * Sorts the given layer's sprites according the sub-layer data using quick
      * sort.
      * NOTE: Should be rewritten with insertion sort.
      * @param low the point on the list where the sorting will begin
      * @param high the point on the list where the sorting will end
      */
     private final void sortByLayer(Vector<CameraSprite> layer, int low, int high) {
         if (low >= high) {
             return;
         }
 
         final CameraSprite pivot = layer.get(high);
         int pivotIndex = high;
         for (int i = low; i <= pivotIndex;) {
             if (layer.get(i).getLayer() > pivot.getLayer()) {
                 final CameraSprite current = layer.get(i);
                 layer.set(pivotIndex, current);
                 layer.set(i, layer.get(pivotIndex - 1));
                 layer.set(pivotIndex - 1, pivot);
                 pivotIndex--;
             } else {
                 i++;
             }
         }
 
         sortByLayer(layer, low, pivotIndex - 1);
         sortByLayer(layer, pivotIndex + 1, high);
     }
 
     private final Scene getScene() {
         Scene retval = null;
         while (retval == null) {
             if (swap1.lock()) {
                 retval = swap1;
             } else if (swap2.lock()) {
                 retval = swap2;
             }
         }
         retval.size.setSize(getWidth(), getHeight());
         return retval;
     }
 
     /**
      * Removes sprite that no longer have a Sector representing them.
      */
     private final void removeStaleSprites() {
         for (int i = 0; i < spritesToRemove.size(); i++) {
             //Make sure the sprite didn't re-enter the observer between the
             //decrement to 0 and now.
             if (sprites.get(spritesToRemove.get(i)).getUsageCount() < 1) {
                 sprites.remove(spritesToRemove.get(i));
                 for (int n = 0; n < selectableSprites.size(); n++) {
                     if (spritesToRemove.get(i) instanceof SelectibleSprite) {
                         selectableSprites.remove(n);
                     }
                 }
             }
         }
         spritesToRemove.clear();
     }
 
     /**
      * Returns Sector 1 of this Camera, makes sure the indices are safe.
      * @return Sector of the upper left hand corner of this Camera.
      */
     private final Sector sector1() {
         return getData().getSectorOfSafe(bounds.x, bounds.y);
     }
 
     /**
      * Returns Sector 2 of this Camera, makes sure the indices are safe.
      * @return Sector of the lower right hand corner of this Camera.
      */
     private final Sector sector2() {
         return getData().getSectorOfSafe(bounds.x + bounds.width, bounds.y + bounds.height);
     }
 
     /**
      * Joins the Sectors of the given coordinates and all Sectors in between, inclusive.
      * @param x1
      * @param y1
      * @param x2
      * @param y2
      */
     private final void joinSectors(int x1, int y1, int x2, int y2) {
         x1 /= getData().sectorWidth();
         x2 /= getData().sectorWidth();
         y1 /= getData().sectorHeight();
         y2 /= getData().sectorHeight();
         for (int x = x1; x <= x2; x++) {
             for (int y = y1; y <= y2; y++) {
                 getData().sectors[x][y].addCamera(this);
             }
         }
     }
 
     /**
      * Joins the Sectors of the given coordinates and all Sectors in between, inclusive.
      * @param x1
      * @param y1
      * @param x2
      * @param y2
      */
     private final void leaveSectors(int x1, int y1, int x2, int y2) {
         x1 /= getData().sectorWidth();
         x2 /= getData().sectorWidth();
         y1 /= getData().sectorHeight();
         y2 /= getData().sectorHeight();
         for (int x = x1; x <= x2; x++) {
             for (int y = y1; y <= y2; y++) {
                 getData().sectors[x][y].removeCamera(this);
             }
         }
     }
 
     private class InputListener implements MouseListener, MouseWheelListener, KeyListener {
 
         private UserListenerList list;
         private CameraSprite selected;
 
         private InputListener() {
             this.list = getData().userListeners.checkOut(user);
         }
 
         @Override
         public void buttonClick(int buttonNumber, int clickCount, int cursorX, int cursorY) {
             ClickEvent e = new ClickEvent(user, buttonNumber,
                     cursorX + getX(), cursorY + getY(), clickCount);
             list.buttonClick(e);
 
             CameraSprite oldSelected = selected;
             if (selected != null) {
                 for (int i = 0; i < selectableSprites.size(); i++) {
                     if (selectableSprites.get(i).getSprite() instanceof SelectibleSprite) {
                         BaseSprite s = selectableSprites.get(i).getSprite();
                         if (Bounds.intersects(s.getX(), cursorX - getX(), s.getY(),
                                 cursorY - getY(), s.getWidth(), 1, s.getHeight(), 1)) {
                             if (selectableSprites.get(i) != oldSelected) {
                                 if (oldSelected != null) {
                                     oldSelected.deselect(user);
                                 }
                                 selectableSprites.get(i).select(user);
                             }
                         }
                     }
                 }
             }
         }
 
         @Override
         public void buttonPress(int buttonNumber, int cursorX, int cursorY) {
             MouseEvent e = new MouseEvent(user, buttonNumber, cursorX + getX(), cursorY + getY());
             list.buttonPress(e);
         }
 
         @Override
         public void buttonRelease(int buttonNumber, int cursorX, int cursorY) {
             MouseEvent e = new MouseEvent(user, buttonNumber, cursorX + getX(), cursorY + getY());
             list.buttonRelease(e);
         }
 
         @Override
         public void wheelScroll(int number, int cursorX, int cursorY) {
             MouseWheelEvent e = new MouseWheelEvent(user, number, cursorX + getX(), cursorY + getY());
             list.wheelScroll(e);
         }
 
         @Override
         public void keyPressed(int key) {
             KeyEvent e = new KeyEvent(user, key);
             list.keyPressed(e);
         }
 
         @Override
         public void keyReleased(int key) {
             KeyEvent e = new KeyEvent(user, key);
             list.keyReleased(e);
         }
 
         @Override
         public void keyTyped(int key) {
             KeyEvent e = new KeyEvent(user, key);
             list.keyTyped(e);
         }
 
         @Override
         protected void finalize() {
             getData().userListeners.checkIn(user);
         }
     }
 }
