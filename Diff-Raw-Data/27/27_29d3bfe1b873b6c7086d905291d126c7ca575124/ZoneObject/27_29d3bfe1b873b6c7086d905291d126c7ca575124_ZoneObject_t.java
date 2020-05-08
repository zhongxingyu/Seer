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
 
 /**
  * The root class for many objects relating to Zone.
  * @author Blinz
  */
 public abstract class ZoneObject {
 
     private final static ZoneData[] dataList = new ZoneData[32767];
 
     static {
         dataList[0] = new ZoneData();
         dataList[0].zoneSize.setSize(dataList[0].sectorWidth(), dataList[0].sectorHeight());
         dataList[0].init((short) 0);
     }
     short zoneID = 0;
 
     /**
      * Runs the given CollidableSprite against other CollidableSprites to check
      * for collisions.
      * @param sprite
      */
     public final void checkCollisions(final CollidableSprite sprite) {
         final Sector tl = getData().getSectorOf(((BaseSprite) sprite).getX(), ((BaseSprite) sprite).getY());
         final Sector br = getData().getSectorOf(((BaseSprite) sprite).getX() + ((BaseSprite) sprite).getWidth(),
                 ((BaseSprite) sprite).getY() + ((BaseSprite) sprite).getHeight());
         tl.checkCollisionsFor(sprite);
 
         if (tl.leftNeighbor != null) {
             tl.leftNeighbor.checkCollisionsFor(sprite);
             if (tl.topNeighbor != null) {
                 tl.topNeighbor.leftNeighbor.checkCollisionsFor(sprite);
             }
         }
         if (tl.topNeighbor != null) {
             tl.topNeighbor.checkCollisionsFor(sprite);
         }
 
         if (tl != br) {
             if (tl.bottomNeighbor.rightNeighbor == br) {
                 //check them all
                 br.checkCollisionsFor(sprite);
                 if (br.leftNeighbor != null) {
                     br.leftNeighbor.checkCollisionsFor(sprite);
                 }
                 if (tl.rightNeighbor != null) {
                     tl.rightNeighbor.checkCollisionsFor(sprite);
                     if (tl.rightNeighbor.topNeighbor != null) {
                         tl.rightNeighbor.topNeighbor.checkCollisionsFor(sprite);
                     }
                 }
             } else if (br == tl.bottomNeighbor) {
                 if (br.leftNeighbor != null) {
                     br.leftNeighbor.checkCollisionsFor(sprite);
                 }
             } else {
                 if (br.topNeighbor != null) {
                     br.topNeighbor.checkCollisionsFor(sprite);
                 }
             }
         }
     }
 
     /**
      * Gets a long representing the number of cycles the zone has gone through.
      * @return a long representing the number of cycles the zone has gone through
      */
     protected final long zoneCycles() {
         return getData().zoneCycles;
     }
 
     /**
      * Gets the shared data object of the Zone that this is a part of.
      * @return the shared data object of the Zone that this is a part of.
      */
     protected final Object getSharedZoneData() {
         return getData().data;
     }
 
     /**
     * Gets the data for the Zone of this ZoneObject.
      *
      * ZoneData contains such items as a master list of Sectors, Sector size, zoneTime,
      * and numerous other pieces of useful data.
      *
      * @return ZoneData
      */
     final ZoneData getData() {
         return dataList[zoneID];
     }
 
    /**
     * Sets the ZoneData object for all ZoneObjects in the Zone to access without any further
     * memory penalties from additional pointers.
     * @param zoneID the ID for the Zone
     * @param zoneData the ZoneData object
     */
     final static void setZoneData(final int zoneID, final ZoneData zoneData) {
         dataList[zoneID] = zoneData;
     }
 
    /**
     * Initializes this ZoneObject for use.
     */
     void internalInit() {
         init();
     }
 
    /**
     * An abstract initialization method allowing users of this API to specify
     * what should be done when the object joins its Zone.
     */
     abstract void init();
 }
