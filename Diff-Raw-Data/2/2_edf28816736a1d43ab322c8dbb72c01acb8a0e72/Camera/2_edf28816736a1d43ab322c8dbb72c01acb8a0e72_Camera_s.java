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
 import org.blinz.graphics.ScreenManager;
 import org.blinz.graphics.Graphics;
 import org.blinz.graphics.Screen;
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
 public abstract class Camera extends ZoneObject {
 
     /**
      * Determines whether or not this Camera represents a local user. True by default.
      */
     private boolean local = true;
     private boolean centeredOnFocusSprite = false;
     private ZoneScreen screen;
     private final Bounds bounds = new Bounds();
     private final Hashtable<BaseSprite, CameraSprite> sprites =
             new Hashtable<BaseSprite, CameraSprite>();
     private final Vector<CameraSprite> selectableSprites = new Vector<CameraSprite>();
     private final Vector<BaseSprite> spritesToRemove = new Vector<BaseSprite>();
     private final SpriteSelecter spriteSelecter = new SpriteSelecter();
     private Sector sector1, sector2;
     private BaseSprite focusSprite;
     private Zone zone;
     private User user;
 
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
         this(user, true);
     }
 
     /**
      * Creates a new Camera.
      * @param user User associated with this Camera.
      * @param local Indicates whether or not this Camera represents a local user, true by default
      */
     Camera(User user, boolean local) {
         this.user = user;
         this.local = local;
     }
 
     /**
      *
      * @return the User that this Camera represents.
      */
     public final User getUser() {
         return user;
     }
 
     /**
      * Sets whether or not this Camera's contents are drawn.
      * @param display if true this Camera will be drawn, otherwise it will not.
      */
     public final synchronized void display(boolean display) {
         if (display) {
             if (screen == null) {
                 screen = new ZoneScreen();
                 ScreenManager.addScreen(screen);
                 Zone z = zone;
                 if (z != null) {
                     screen.joinZone();
                     //to prevent concurrency issues
                     while (zone != z) {
                         screen.dropZone();
                         if ((z = zone) != null) {
                             screen.joinZone();
                         }
                     }
                 }
             }
         } else {
             ScreenManager.removeScreen(screen);
             screen = null;
         }
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
         ZoneScreen s;
         if ((s = screen) != null) {
             s.joinZone();
         }
     }
 
     /**
      * Drops the current zone, the Camera will have no Zone to moniter after
      * this method is called.
      */
     public final void dropZone() {
         if (zone != null) {
             ZoneScreen s = screen;
             if (s != null) {
                 screen.dropZone();
             }
             zone.removeCamera(this);
             selectableSprites.clear();
             sprites.clear();
             focusSprite = null;
             spritesToRemove.clear();
             zone = null;
         }
     }
 
     /**
      * If set to true this Camera will remain centered on the focus sprite
      * if there is one, if false it will not move unless instructed to.
      * @param centerOnFocusSprite
      */
     public final void centerOnFocusSprite(boolean centerOnFocusSprite) {
         this.centeredOnFocusSprite = centerOnFocusSprite;
     }
 
     /**
      * 
      * @return the x location of this Camera
      */
     public final int getX() {
         return bounds.getX();
     }
 
     /**
      * 
      * @return the y location of this Camera
      */
     public final int getY() {
         return bounds.getY();
     }
 
     /**
      * 
      * @return the width of this Camera
      */
     public final int getWidth() {
         return bounds.getWidth();
     }
 
     /**
      * 
      * @return the height of this Camera
      */
     public final int getHeight() {
         return bounds.getHeight();
     }
 
     public final void setSize(int width, int height) {
         if (zone != null) {
             updateSize(width, height);
         }
         bounds.setSize(width, height);
     }
 
     /**
      * Sets the x coordinate of this Camera to the given value.
      * @param x
      */
     public final void setX(int x) {
         updateX(x);
         bounds.setX(x);
     }
 
     /**
      * Sets the y coordinate of this Camera to the given value.
      * @param y
      */
     public final void setY(int y) {
         updateY(y);
         bounds.setX(y);
     }
 
     /**
      * Sets the location of this Camera.
      * @param x
      * @param y
      */
     public final void setPosition(int x, int y) {
         updatePosition(x, y);
         bounds.setPosition(x, y);
     }
 
     /**
      * Moves this Camera up the specified distance.
      * @param distance
      */
     public final void moveUp(int distance) {
         if (distance < 0) {
             return;
         }
         int newY = bounds.getY() - distance;
         updateY(newY);
 
         bounds.setY(newY);
     }
 
     /**
      * Moves this Camera down the specified distance.
      * @param distance
      */
     public final void moveDown(int distance) {
         if (distance > zone.getHeight()) {
             return;
         }
         int newY = bounds.getY() - distance;
         updateY(newY);
 
         bounds.setY(newY);
     }
 
     /**
      * Moves this Camera right the specified distance.
      * @param distance
      */
     public final void moveRight(int distance) {
         if (distance > zone.getWidth()) {
             return;
         }
         int newX = bounds.getX() - distance;
         updateX(newX);
 
         bounds.setX(newX);
     }
 
     /**
      * Moves this Camera left the specified distance.
      * @param distance
      */
     public final void moveLeft(int distance) {
         if (distance < 0) {
             return;
         }
         int newX = bounds.getX() - distance;
         updateX(newX);
 
         bounds.setX(newX);
     }
 
     /**
      * Sets the focus sprite to the given BaseSprite, that is the sprite that
      * the Camera will follow.
      * @param sprite
      */
     public final void setFocusSprite(BaseSprite sprite) {
         focusSprite = sprite;
     }
 
     /**
      * Informs the Zone of this BaseSprites new width. Use this or one of the other
      * two size update methods prior manually editting the size of a BaseSprite.
      * @param width
      */
     protected final void updateWidth(final int width) {
         if (getData() == null) {
             return;
         }
         if (((int) ((getX() + width) / getData().sectorSize.width))
                 == (((int) (getX() + getWidth()) / getData().sectorWidth()))) {
             return;
         }
 
         if (width < getWidth()) {
             final Sector goal = getData().getSectorOfSafe(getX() + width, getY() + getHeight());
             this.sector2 = goal;
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.removeCamera(this);
 
                 Sector s = s2.topNeighbor;
                 Sector ts = getData().getSectorOf(s2.getX(), getY());
 
                 while (s != ts) {
                     s.removeCamera(this);
                     s = s.topNeighbor;
                 }
 
                 s2 = s2.leftNeighbor;
             }
         } else {
             final Sector goal = getData().getSectorOfSafe(getX() + width, getY() + getHeight());
             this.sector2 = goal;
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.addCamera(this);
 
                 Sector s = s2.topNeighbor;
                 Sector ts = getData().getSectorOf(s2.getX(), getY());
 
                 while (s != ts) {
                     s.addCamera(this);
                     s = s.topNeighbor;
                 }
 
                 s2 = s2.rightNeighbor;
             }
         }
 
     }
 
     /**
      * Informs the Zone of this BaseSprites new height. Use this or one of the other
      * two size update methods prior manually editting the size of a BaseSprite.
      * @param height
      */
     protected final void updateHeight(final int height) {
         if (getData() == null) {
             return;
         }
         if (((int) ((getY() + height) / getData().sectorHeight()))
                 == (((int) (getY() + getHeight()) / getData().sectorHeight()))) {
             return;
         }
 
         if (height < getHeight()) {
             final Sector goal = getData().getSectorOfSafe(getX() + getWidth(), getY() + height);
             this.sector2 = goal;
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.removeCamera(this);
 
                 Sector s = s2.rightNeighbor;
                 Sector ts = getData().getSectorOf(getX(), s2.getY());
 
                 while (s != ts) {
                     s.removeCamera(this);
                     s = s.rightNeighbor;
                 }
 
                 s2 = s2.leftNeighbor;
             }
         } else {
             final Sector goal = getData().getSectorOfSafe(getX() + getWidth(), getY() + height);
             this.sector2 = goal;
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.addCamera(this);
 
                 Sector s = s2.bottomNeighbor;
                 Sector ts = getData().getSectorOf(getX(), s2.getY());
 
                 while (s != ts) {
                     s.addCamera(this);
                     s = s.bottomNeighbor;
                 }
 
                 s2 = s2.rightNeighbor;
             }
         }
 
     }
 
     /**
      * Informs the Zone of this BaseSprites new size. Use this or one of the other
      * two size update methods prior manually editting the size of a BaseSprite.
      * @param width
      * @param height
      */
     protected final void updateSize(final int width, final int height) {
         if (getData() == null) {
             return;
         }
         if (sector2().contains(getX() + width, getY() + height)) {
             return;
         }
 
         final int oldX2 = getX() + getWidth() >= getData().sectors.length ? getX() + getWidth() : getData().sectors.length - 1;
         final int oldX2i = oldX2 / getData().sectorSize.width;
         final int newX2 = getX() + width >= getData().sectors.length ? getX() + width : getData().sectors.length - 1;
         final int newX2i = newX2 / getData().sectorSize.width;
         final int oldY2 = getY() + getHeight() >= getData().sectors.length ? getY() + getHeight() : getData().sectors[0].length - 1;
         final int oldY2i = oldY2 / getData().sectorSize.height;
         final int newY2 = getY() + height >= getData().sectors.length ? getY() + height : getData().sectors[0].length - 1;
         final int newY2i = newY2 / getData().sectorSize.height;
 
         if (newX2i != oldX2i) {
             updateWidth(width);
         }
         if (newY2i != oldY2i) {
             updateHeight(height);
         }
     }
 
     /**
      * Informs the Zone that this sprite is moving. This or one of the other two
      * coordinate modification methods should be called BEFORE manually editting
      * the position of this BaseSprite.
      *
      * If you are using the position mutators of the Sprite class it will not be
      * necessary to call this method.
      *
      * @param x the new x coordinate of this sprite
      */
     final void updateX(final int x) {
         if (getData() == null) {
             return;
         }
         if (sector1.contains(x, getY()) && sector2.contains(x + getWidth(), getY() + getHeight())) {
             return;
         }
 
         sector1 = getData().getSectorOf(x, getY());
         sector2 = getData().getSectorOf(x + getWidth(), getY());
 
         int fx1, fx2;
 
         if (x < getX()) {
             fx1 = x / getData().sectorSize.width;
             fx2 = (getX() + getWidth()) / getData().sectorSize.width;
         } else {
             fx1 = getX() / getData().sectorSize.width;
             fx2 = (x + getWidth()) / getData().sectorSize.width;
         }
 
         int ox1 = getX() / getData().sectorSize.width;
         int ox2 = (getX() + getWidth()) / getData().sectorSize.width;
         int nx1 = x / getData().sectorSize.width;
         int nx2 = (x + getWidth()) / getData().sectorSize.width;
         int iy = getY() / getData().sectorSize.height;
         int targetY = (getY() + getHeight()) / getData().sectorHeight();
 
         for (int i = fx1; i <= fx2; i++) {
             final boolean inOldRange = (ox1 <= i && ox2 > i);
             final boolean inNewRange = (nx1 <= i && nx2 > i);
             for (int n = iy; n < targetY; n++) {
                 if (inNewRange && !inOldRange) {
                     getData().sectors[i][iy].addCamera(this);
                 } else if (inOldRange && !inNewRange) {
                     getData().sectors[i][iy].removeCamera(this);
                 }
             }
         }
 
     }
 
     /**
      * Informs the Zone that this sprite is moving. This or one of the other two
      * coordinate modification methods should be called BEFORE manually editting
      * the position of this BaseSprite.
      *
      * If you are using the position mutators of the Sprite class it will not be
      * necessary to call this method.
      *
      * @param y the new y coordinate of this sprite
      */
     protected final void updateY(final int y) {
         if (getData() == null) {
             return;
         }
         if (sector1().contains(getX(), y) && sector2().contains(getX() + getWidth(), y + getHeight())) {
             return;
         }
 
         sector1 = getData().getSectorOf(getX(), y);
         sector2 = getData().getSectorOf(getX(), y + getHeight());
 
         int fy1, fy2;
 
         if (y < getX()) {
             fy1 = y / getData().sectorSize.height;
             fy2 = (getY() + getHeight()) / getData().sectorSize.height;
         } else {
             fy1 = getY() / getData().sectorSize.width;
             fy2 = (y + getHeight()) / getData().sectorSize.width;
         }
 
         final int oy1 = getY() / getData().sectorSize.height;
         final int oy2 = (getY() + getHeight()) / getData().sectorSize.height;
         final int ny1 = y / getData().sectorSize.height;
         final int ny2 = (y + getHeight()) / getData().sectorSize.height;
         final int ix = getX() / getData().sectorSize.height;
 
         for (int i = fy1; i <= fy2; i++) {
             final boolean inOldRange = (oy1 <= i && oy2 > i);
             final boolean inNewRange = (ny1 <= i && ny2 > i);
 
             if (inNewRange && !inOldRange) {
                 getData().sectors[ix][i].addCamera(this);
             } else if (inOldRange && !inNewRange) {
                 getData().sectors[ix][i].removeCamera(this);
             }
         }
 
     }
 
     /**
      * Informs the Zone that this sprite is moving. This or one of the other two
      * coordinate modification methods should be called BEFORE manually editting
      * the position of this BaseSprite.
      *
      * If you are using the position mutators of the Sprite class it will not be
      * necessary to call this method.
      *
      * @param x the new x coordinate of this sprite
      * @param y the new y coordinate of this sprite
      */
     protected final void updatePosition(final int x, final int y) {
         if (getData() == null) {
             return;
         }
         if (sector1.contains(x, y) && sector2.contains(x + getWidth(), y + getHeight())) {
             return;
         }
 
         sector1 = getData().getSectorOfSafe(x, y);
         sector2 = getData().getSectorOfSafe(x + getHeight(), y + getHeight());
 
         //get the full range of all relevent Sectors
         int fx1, fy1, fx2, fy2;
 
         if (x < getX()) {
             fx1 = x / getData().sectorSize.width;
             fx2 = (getX() + getWidth()) / getData().sectorSize.width;
             if (fx2 >= getData().sectors.length) {
                 fx2 = getData().sectors.length - 1;
             }
         } else {
             fx1 = getX() / getData().sectorSize.width;
             fx2 = (x + getWidth()) / getData().sectorSize.width;
             if (fx2 >= getData().sectors.length) {
                 fx2 = getData().sectors.length - 1;
             }
         }
         if (y < getY()) {
             fy1 = y / getData().sectorSize.height;
             fy2 = (getY() + getHeight()) / getData().sectorSize.height;
             if (fy2 >= getData().sectors[0].length) {
                 fy2 = getData().sectors[0].length - 1;
             }
         } else {
             fy1 = getY() / getData().sectorSize.height;
             fy2 = (y + getHeight()) / getData().sectorSize.height;
             if (fy2 >= getData().sectors[0].length) {
                 fy2 = getData().sectors[0].length - 1;
             }
         }
 
         int ox1 = getX() / getData().sectorSize.width;
         int ox2 = (getX() + getWidth()) / getData().sectorSize.width;
         int oy1 = getY() / getData().sectorSize.height;
         int oy2 = (getY() + getHeight()) / getData().sectorSize.height;
 
         int nx1 = x / getData().sectorSize.width;
         int nx2 = (x + getWidth()) / getData().sectorSize.width;
         int ny1 = y / getData().sectorSize.height;
         int ny2 = (y + getHeight()) / getData().sectorSize.height;
 
         for (int i = fx1; i <= fx2; i++) {
             for (int n = fy1; n <= fy2; n++) {
                 final boolean inOldRange = (ox1 <= i && ox2 >= i && oy1 <= n && oy2 >= n);
                 final boolean inNewRange = (nx1 <= i && nx2 >= i && ny1 <= n && ny2 >= n);
 
                 if (inNewRange && !inOldRange) {
                     getData().sectors[i][n].addCamera(this);
                 } else if (inOldRange && !inNewRange) {
                     getData().sectors[i][n].removeCamera(this);
                 }
             }
         }
     }
 
     @Override
     final void init() {
         sector1 = sector1();
         sector2 = sector2();
         for (int i = 0; i < getData().sectors.length; i++) {
             for (int n = 0; n < getData().sectors[i].length; n++) {
                 if (getData().sectors[i][n].intersects(bounds)) {
                     getData().sectors[i][n].addCamera(this);
                 }
             }
         }
     }
 
     /**
      * Finds the sprite currently on screen, gathers them, orders them by layer
      * and sets it to the current scene.
      */
     final void generateCurrentScene() {
         Scene scene = screen.getScene();
         scene.manageContainers();
 
         BaseSprite sprite = focusSprite;
         if (sprite != null) {
             scene.translation.setPosition((sprite.getX() - scene.size.getWidth() / 2) + sprite.getWidth() / 2,
                     (sprite.getY() - scene.size.getHeight() / 2) + sprite.getHeight() / 2);
         } else {
             scene.translation.setPosition(0, 0);
         }
 
         Bounds b = new Bounds();
         b.setPosition(scene.translation);
         b.setSize(scene.size);
         Enumeration<CameraSprite> spriteList = sprites.elements();
         while (spriteList.hasMoreElements()) {
             CameraSprite s = spriteList.nextElement();
             if (b.intersects(s.getX(), s.getY(), s.getWidth(), s.getHeight())) {
                 scene.add(s);
             }
         }
 
         scene.sortLayers();
 
         b = null;
 
         scene.unLock();
         screen.scene = scene;
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
         if (sprites.containsKey(sprite)) {
             sprites.get(sprite).incrementUseCount();
         } else {
             CameraSprite zs = new CameraSprite(sprite);
             sprites.put(sprite, zs);
             if (sprite instanceof SelectibleSprite) {
                 selectableSprites.add(zs);
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
         w.decrementUseCount();
         if (w.getUsageCount() < 1) {
             spritesToRemove.add(sprite);
         }
     }
 
     /**
      * Updates the Camera.
      */
     void internalUpdate() {
         removeStaleSprites();
         if (screen != null) {
             setSize(screen.getWidth(), screen.getHeight());
         }
 
         if (focusSprite != null && centeredOnFocusSprite) {
             setPosition((focusSprite.getX() - getWidth() / 2) + focusSprite.getWidth() / 2,
                     (focusSprite.getY() - getHeight() / 2) + focusSprite.getHeight() / 2);
         }
 
         if (screen != null) {
             generateCurrentScene();
         }
 
         update();
     }
 
     protected abstract void update();
 
     protected abstract void initCamera();
 
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
         int ix, iy;
         if (bounds.getX() > zone.getWidth()) {
             ix = getData().sectors.length - 1;
         } else if (bounds.getX() < 0) {
             ix = 0;
         } else {
             ix = getX() / getData().sectorSize.width;
         }
         if (bounds.getY() > zone.getHeight()) {
             iy = getData().sectors[ix].length - 1;
         } else if (bounds.getY() < 0) {
             iy = 0;
         } else {
             iy = getY() / getData().sectorSize.height;
         }
 
         return getData().sectors[ix][iy];
     }
 
     /**
      * Returns Sector 2 of this Camera, makes sure the indices are safe.
      * @return Sector of the lower right hand corner of this Camera.
      */
     private final Sector sector2() {
         int ix, iy;
         if (bounds.getX() + bounds.getWidth() > zone.getWidth()) {
             ix = getData().sectors.length - 1;
         } else if (bounds.getX() + bounds.getWidth() < 0) {
             ix = 0;
         } else {
             ix = (getX() + getWidth()) / getData().sectorSize.width;
         }
         if (bounds.getY() + bounds.getHeight() > zone.getHeight()) {
             iy = getData().sectors[ix].length - 1;
         } else if (bounds.getY() + bounds.getHeight() < 0) {
             iy = 0;
         } else {
             iy = (getY() + getHeight()) / getData().sectorSize.height;
         }
 
         return getData().sectors[ix][iy];
     }
 
     private class SpriteSelecter implements MouseListener {
 
         private CameraSprite selected;
 
         @Override
         public synchronized void buttonClick(int buttonNumber, int clickCount, int cursorX, int cursorY) {
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
         }
 
         @Override
         public void buttonRelease(int buttonNumber, int cursorX, int cursorY) {
         }
     }
 
     private class ZoneScreen extends Screen {
 
         private class InputListener implements MouseListener, MouseWheelListener, KeyListener {
 
             private UserListenerList list;
             private Zone zone;
 
             public InputListener(UserListenerList list, Zone zone) {
                 this.list = list;
                 this.zone = zone;
             }
 
             @Override
             public void buttonClick(int buttonNumber, int clickCount, int cursorX, int cursorY) {
                 ClickEvent e = new ClickEvent(user, buttonNumber,
                         cursorX + getX(), cursorY + getY(), clickCount);
                 list.buttonClick(e);
             }
 
             @Override
             public void buttonPress(int buttonNumber, int cursorX, int cursorY) {
                 MouseEvent e = new MouseEvent(user, buttonNumber, cursorX + getX(), cursorY + getY());
                list.buttonRelease(e);
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
         }
         private Scene scene = new Scene();
         private Scene swap1 = new Scene();
         private Scene swap2 = new Scene();
         private InputListener listener;
 
         private ZoneScreen() {
             addMouseListener(spriteSelecter);
         }
 
         @Override
         protected void draw(Graphics graphics) {
             Scene s = scene;
             while (!s.lock()) {
                 s = scene;
             }
             s.draw(graphics);
             s.unLock();
         }
 
         private final void dropZone() {
             Zone z = zone;
             if (z != null) {
                 UserListenerList l = getData().userListeners.checkOut(user);
                 removeMouseListener(listener);
                 removeMouseWheelListener(listener);
                 removeKeyListener(listener);
                 listener = null;
             }
             getData().userListeners.checkIn(user);
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
 
         private final void joinZone() {
             dropZone();
             UserListenerList l = getData().userListeners.checkOut(user);
             listener = new InputListener(l, zone);
             addMouseListener(listener);
             addMouseWheelListener(listener);
             addKeyListener(listener);
         }
     }
 }
