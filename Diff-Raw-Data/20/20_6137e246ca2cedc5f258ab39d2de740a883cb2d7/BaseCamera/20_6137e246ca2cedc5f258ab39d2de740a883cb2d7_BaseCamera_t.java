 /*
  * Dog - A project for making highly scalable non-clustered game and simulation environments.
  * Copyright (C) 2010 BlinzProject <gtalent2@gmail.com>
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
 import java.util.HashMap;
 import java.util.Vector;
 import net.blinz.core.util.Bounds;
 import net.blinz.dog.util.User;
 
 /**
  * A base class for Camera and ServerCamera.
  * @author Blinz
  */
 public abstract class BaseCamera extends ZoneObject {
 
     /**
      * Tracks the Sprites in a given Sector that the Camera occupies.
      */
     private final class CameraSector {
 
         private final UnorderedList<CameraSprite> sprites = new UnorderedList<CameraSprite>();
         private Sector sector;
 
         /**
          * Constructor
          * @param sector the associated Sector
          */
         CameraSector(final Sector sector) {
             this.sector = sector;
         }
 
         /**
          * Adds sprites new to this Sector that this CameraSector represents to
          * this Camera.
          */
         private final void addNewSprites() {
             final Vector<BaseSprite> list = sector.getAddedSprites();
             for (int n = 0; n < list.size(); n++) {
                 addSprite(list.get(n));
             }
         }
 
         /**
          * Declares all of the sprites in this CameraSector orphans.
          */
         private final void orphanSprites() {
             while (!sprites.isEmpty()) {
                 orphanMap.put(sprites.get(0).getSprite(), sprites.get(0));
                 sprites.remove(0).setOrphaned(true);
             }
         }
 
         /**
          * Finds, removes, and declares the sprites that should no longer represent
          * this sector.
          */
         private final void orphanRemovedSprites() {
             final Vector<BaseSprite> list = sector.getRemovedSprites();
             for (int n = 0; n < list.size(); n++) {
                 final BaseSprite sprite = list.get(n);
                 for (int i = 0; i < sprites.size(); i++) {
                     if (sprites.get(i).getSprite() == sprite) {
                         orphanMap.put(sprites.get(i).getSprite(), sprites.get(i));
                         sprites.remove(i).setOrphaned(true);
                         break;
                     }
                 }
             }
         }
 
         /**
          * Adds the given sprite to this CameraSector and to the Camera if necessary.
          * @param sprite the sprite to be added
          */
         private final void addSprite(final BaseSprite sprite) {
             CameraSprite cs = null;
             //recover the sprite's orphaned representation if it exists
             cs = orphanMap.remove(sprite);
             if (cs != null) {
                 cs.setOrphaned(false);
             } else {
                 cs = new CameraSprite(sprite);
                 me.addSprite(cs);
             }
             //add the sprite to the list representing its Sector
             sprites.add(cs);
         }
 
         /**
          * Adds the sprites already existing within the Sector.
          */
         private final void addSprites() {
             final UnorderedList<BaseSprite> list = sector.getSprites();
             for (int n = 0; n < list.size(); n++) {
                 addSprite(list.get(n));
             }
         }
     }
     /**
      * Needed because CameraSector confuses its addSprite method with BaseCamera's addSprite method.
      */
     private final BaseCamera me = this;
     /**
      * Represents the bounds of this Camera during the last round.
      */
     private final Bounds oldBounds = new Bounds();
     private final Vector<CameraSector> sectors = new Vector<CameraSector>();
     private final HashMap<BaseSprite, CameraSprite> orphanMap = new HashMap<BaseSprite, CameraSprite>();
     private final ArrayList<CameraSprite> orphanList = new ArrayList<CameraSprite>();
     private final Bounds bounds = new Bounds();
    private User user = new User("Default");
 
     /**
      * Constructor for Camera.
      */
     public BaseCamera() {
         this(new User());
     }
 
     /**
      * Constructor
      * @param user the User associated with this Camera
      */
     public BaseCamera(final User user) {
         this.user = user;
     }
 
     /**
      * Gets the User associated with this Camera.
      * @return the User that this Camera represents.
      */
     public final User getUser() {
         return user;
     }
 
     /**
      * Sets the zone of this Camera. In addition to setting the Zone it also
      * removes the old Zone.
      * @param zone the Zone for this to start following
      */
     public final synchronized void setZone(final Zone zone) {
         dropZone(getZone());
         zone.addCamera(this);
     }
 
     /**
      * Gets the x coordinate of this Camera
      * @return the x location of this Camera
      */
     public final int getX() {
         return bounds.x;
     }
 
     /**
      * Gets the y coordinate of this Camera
      * @return the y location of this Camera
      */
     public final int getY() {
         return bounds.y;
     }
 
     /**
      * Gets the width of this Camera.
      * @return the width of this Camera
      */
     public final int getWidth() {
         return bounds.width;
     }
 
     /**
      * Gets the height of this Camera.
      * @return the height of this Camera
      */
     public final int getHeight() {
         return bounds.height;
     }
 
     /**
      * Sets the size of this Camera in the Zone.
      * @param width the new width of this Camera
      * @param height the new height of this Camera
      */
     public final void setSize(final int width, final int height) {
         setWidth(width);
         setHeight(height);
     }
 
     /**
      * Sets the width of this Camera to that given.
      * @param width the new width of this Camera
      */
     public final void setWidth(final int width) {
         bounds.width = width;
     }
 
     /**
      * Sets the height of this Camera to that given.
      * @param height the new height of this Camera
      */
     public final void setHeight(final int height) {
         bounds.height = height;
     }
 
     /**
      * Sets the x coordinate of this Camera to the given value.
      * @param x the new x coordinate of this Camera
      */
     public final void setX(final int x) {
         bounds.x = x;
     }
 
     /**
      * Sets the y coordinate of this Camera to the given value.
      * @param y the new y coordinate of this Camera
      */
     public final void setY(final int y) {
         bounds.y = y;
     }
 
     /**
      * Sets the location of this Camera.
      * @param x the new x coordinate of this Camera
      * @param y the new y coordinate of this Camera
      */
     public final void setPosition(final int x, final int y) {
         setX(x);
         setY(y);
     }
 
     /**
      * Moves this Camera up the specified distance.
      * @param distance the distance that this Camera is to move
      */
     public final void moveUp(final int distance) {
         bounds.y = -distance;
     }
 
     /**
      * Moves this Camera down the specified distance.
      * @param distance the distance that this Camera is to move
      */
     public final void moveDown(final int distance) {
         bounds.y += distance;
     }
 
     /**
      * Moves this Camera right the specified distance.
      * @param distance the distance that this Camera is to move
      */
     public final void moveRight(final int distance) {
         bounds.x += distance;
     }
 
     /**
      * Moves this Camera left the specified distance.
      * @param distance the distance that this Camera is to move
      */
     public final void moveLeft(final int distance) {
         bounds.x -= distance;
     }
 
     /**
      * Drops the current zone, the Camera will have no Zone to moniter after
      * this method is called.
      */
     public final void dropZone() {
         dropZone(getZone());
     }
 
     /**
      * Called after each cycle of this Camera's Zone. Does nothing, for implementing
      * as needed.
      */
     protected void update() {
     }
 
     /**
      * Called after the Camera receives a Zone. Does nothing, for implementing
      * as needed.
      */
     @Override
     protected void init() {
     }
 
     /**
      * Drops the current zone, the Camera will have no Zone to moniter after
      * this method is called.
      * @param zone the Zone that this currently follows and you intend to drop
      */
     @Override
     synchronized void dropZone(final Zone zone) {
        if (getZone() == zone && zone != null) {
             getZone().removeCamera(this);
             super.dropZone(zone);
         }
     }
 
     @Override
     void internalInit() {
         init();
     }
 
     /**
      * Updates the Camera.
      */
     void internalUpdate() {
         update();
         updateSprites();
     }
 
     /**
      * Passes in a CameraSprite to add as the implementation needs.
      * @param sprite the sprite to add
      */
     abstract void addSprite(final CameraSprite sprite);
 
     /**
      * Method indicating the need to remove Sectorless sprites.
      */
     abstract void removeOrphanedSprites(final ArrayList<CameraSprite> orphans);
 
     /**
      * Adds the given Sector to the necessary structures.
      * @param sector the Sector to be added
      */
     private final void addSector(final Sector sector) {
         final CameraSector cs = new CameraSector(sector);
         cs.addSprites();
         sectors.add(cs);
     }
 
     /**
      * Removes the given Sector from the necessary structures.
      * @param sectorIndex the location of the Sector in the sectors list
      */
     private final void removeSector(final int sectorIndex) {
         sectors.remove(sectorIndex).orphanSprites();
     }
 
     /**
      * Updates the sprites in this Camera.
      */
     private final void updateSprites() {
         //manage sprites for current sectors
         //find and declare orphaned sprites
         for (int i = 0; i < sectors.size(); i++) {
             sectors.get(i).orphanRemovedSprites();
         }
 
         //add new sprites
         for (int i = 0; i < sectors.size(); i++) {
             sectors.get(i).addNewSprites();
         }
 
         //update Sectors
         if (bounds.x != oldBounds.x || bounds.y != oldBounds.y
                 || bounds.width != oldBounds.width || bounds.height != oldBounds.height) {
             //update the Sectors
             //remove old Sectors
             for (int i = 0; i < sectors.size(); i++) {
                 if (!sectors.get(i).sector.withinSpriteRange(bounds)) {
                     removeSector(i);
                     i--;
                 }
             }
             if (bounds.width > 0 && bounds.height > 0) {
                 //add new Sectors
                 final int x1 = sector1().leftNeighbor != null ? sector1().leftNeighbor.getX() : 0;
                 final int y1 = sector1().topNeighbor != null ? sector1().topNeighbor.getY() : 0;
                 final int x2 = sector2().getX();
                 final int y2 = sector2().getY();
 
                 for (int x = x1; x <= x2; x += getData().sectorWidth()) {
                     for (int y = y1; y <= y2; y += getData().sectorHeight()) {
                         final Sector s = getData().getSectorOf(x, y);
                         if (!s.withinSpriteRange(oldBounds)) {
                             addSector(s);
                         }
                     }
                 }
             }
             //update oldBounds
             oldBounds.setBounds(bounds);
         }
 
         removeOrphanedSprites(orphanList);
         orphanList.clear();
         orphanMap.clear();
     }
 
     /**
      * Gets Sector 1 of this Camera, makes sure the indices are safe.
      * @return Sector of the upper left hand corner of this Camera.
      */
     private final Sector sector1() {
         return getData().getSectorOfSafe(bounds.x, bounds.y);
     }
 
     /**
      * Gets Sector 2 of this Camera, makes sure the indices are safe.
      * @return Sector of the lower right hand corner of this Camera.
      */
     private final Sector sector2() {
         return getData().getSectorOfSafe(bounds.x2(), bounds.y2());
     }
 }
