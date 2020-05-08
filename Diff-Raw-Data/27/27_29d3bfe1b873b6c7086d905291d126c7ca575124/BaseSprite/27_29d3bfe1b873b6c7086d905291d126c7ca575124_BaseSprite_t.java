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
 
 import org.blinz.input.KeyEvent;
 import org.blinz.graphics.Graphics;
 import org.blinz.input.ClickEvent;
 import org.blinz.input.MouseEvent;
 import org.blinz.input.MouseWheelEvent;
 import org.blinz.util.Bounds;
 import org.blinz.util.Position3D;
 import org.blinz.util.Size;
 
 /**
 * The base of all sprite classes. This class should only be used when optimization
 * or extreme customization is needed. The Sprite class is much easier to use and
 * will probably fit most situations.
  * @author Blinz
  */
 public abstract class BaseSprite extends ZoneObject {
 
     /**
      * Gets the maximum width for a sprite.
      * return the maximum width for a sprite
      */
     public final int maximumSpriteWidth() {
         return getData().sectorWidth;
     }
 
     /**
      * Gets the maximum height for a sprite.
      * return the maximum height for a sprite
      */
     public final int maximumSpriteHeight() {
         return getData().sectorHeight;
     }
 
     /**
      * Marks the Sprite for removal from its Zone. After removal from its Zone
      * it will be unmarked for removal. If no other reference to it exists it
      * will be deleted by the garbage colletor.
      */
     public final void delete() {
         getData().spritesToDelete.add(this);
     }
 
     /**
      * Stub method called when this sprite is deleted.
      */
     protected void onDelete() {
     }
 
     /**
      *
      * @return a Size object representing the sprites size.
      */
     public abstract Size getSize();
 
     /**
      * Gets this sprite's width.
      * @return this sprite's width
      */
     public abstract int getWidth();
 
     /**
      * Gets this sprite's height.
      * @return this sprite's height
      */
     public abstract int getHeight();
 
     /**
      * Gets a new Position3D object representing the position of this Sprite.
      * @return a new Position3D object representing the position of this Sprite.
      */
     public abstract Position3D getPosition();
 
     /**
      * Gets this sprite's x coordinate.
      * @return this sprite's x coordinate
      */
     public abstract int getX();
 
     /**
      * Gets this sprite's y coordinate.
      * @return this sprite's y coordinate
      */
     public abstract int getY();
 
     /**
      * Gets this sprite's layer.
      * @return this sprite's z coordinate
      */
     public abstract float getLayer();
 
     /**
      * Gets an instance of Bounds representing the location and size of this
      * sprite.
      * @return an instance of Bounds representing the location and size of this
      * sprite
      */
     public abstract Bounds getBounds();
 
     /**
      * Sets the width of this sprite to the given value.
      * @param width the new width of this sprite
      */
     public final void setWidth(int width) {
         if (width < 1) {
             width = 1;
         } else {
             if (width > getData().sectorWidth()) {
                 width = getData().sectorWidth();
             }
             if (width + getX() > getData().getZoneWidth()) {
                 width = getData().getZoneWidth() - getX();
             }
         }
         updateWidth((short) width);
     }
 
     /**
      * Sets the height of this sprite to the given value.
      * @param height the new height of this sprite
      */
     public final void setHeight(int height) {
         if (height < 1) {
             height = 1;
         } else {
             if (getY() + height > getData().getZoneHeight()) {
                 height = getData().getZoneHeight() - getY();
             }
             if (height > getData().sectorHeight()) {
                 height = getData().sectorHeight() - getY();
             }
         }
         updateHeight((short) height);
     }
 
     /**
      * Sets the size of this sprite to the given values.
      * @param width the new width of this sprite
      * @param height the new height of this sprite
      */
     public final void setSize(int width, int height) {
         //Method excessively large because of frequency of call and need for efficiency
         if (width > getData().sectorWidth()) {
             width = getData().sectorWidth();
         }
         if (height > getData().sectorHeight()) {
             height = getData().sectorHeight();
         }
         updateWidth((short) width);
         updateHeight((short) height);
     }
 
     /**
      * Sets the x location of this sprite to the given value.
      * @param x the new x coordinate of this sprite    
      */
     public final void setX(int x) {
         //Method excessively large because of frequency of call and need for efficiency
         final ZoneData zoneData = getData();
 
         //ensure the new location is within bounds
         if (x < 0) {
             x = 0;
         } else if (x + getWidth() > zoneData.getZoneWidth()) {
             x = zoneData.getZoneWidth() - getWidth();
         }
         final Sector otl = zoneData.getSectorOf(getX(), getY());
         final Sector ntl = zoneData.getSectorOf(x, getY());
         if (otl != ntl) {
             otl.removeSprite(this, ntl);
             ntl.addSprite(this);
         }
         updateX(x);
     }
 
     /**
      * Sets the y location of this sprite to the given value.
      * @param y the new y coordinate of this sprite
      */
     public final void setY(int y) {
         //Method excessively large because of frequency of call and need for efficiency
         final ZoneData zoneData = getData();
 
         //ensure the new location is within bounds
         if (y < 0) {
             y = 0;
         } else if (y + getHeight() > zoneData.getZoneHeight()) {
             y = zoneData.getZoneHeight() - getHeight();
         }
         final Sector otl = getData().getSectorOf(getX(), getY());
         final Sector ntl = getData().getSectorOf(getX(), y);
         if (otl != ntl) {
             otl.removeSprite(this, ntl);
             ntl.addSprite(this);
         }
         updateY(y);
     }
 
     /**
      * Sets the location of this sprite to the given coordinates.
      * @param x the new x coordinate of this sprite
      * @param y the new y coordinate of this sprite
      */
     public final void setPosition(final int x, final int y) {
         setX(x);
         setY(y);
     }
 
     /**
      * Sets the layer of this sprite to the given value.
      * The layer of this sprite determines the other sprites it can collide with
      * and the order in which they will be drawn. The lower the number the deeper
      * into the Zone the layer is.
      * @param layer
      */
     public final void setLayer(float layer) {
         if (layer < 0) {
             layer = 0;
         } else if (layer > 49) {
             layer = 49;
         }
         updateLayer(layer);
     }
 
     /**
      * Gets the amount of time the Zone has been executing, not including the
      * time before it started executing for time it was paused.
      * @return the amount of time the Zone has been executing, not including the
      * time before it started executing for time it was paused
      */
     protected final long zoneTime() {
         return getData().zoneTime;
     }
 
     /**
      * A stub method for listening to clicks. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonClicked(final ClickEvent event) {
     }
 
     /**
      * A stub method for listening to mouse button presses. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonPressed(final MouseEvent event) {
     }
 
     /**
      * A stub method for listening to mouse button releases. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonReleased(final MouseEvent event) {
     }
 
     /**
      * A stub method for listening to the mouse wheel. Implement as needed.
      * @param event contains data about the input
      */
     protected void mouseWheelScroll(final MouseWheelEvent event) {
     }
 
     /**
      * A stub method for listening to the keys pressed. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyPressed(final KeyEvent event) {
     }
 
     /**
      * A stub method for listening to the keys released. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyReleased(final KeyEvent event) {
     }
 
     /**
      * A stub method for listening to the key typed. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyTyped(final KeyEvent event) {
     }
 
     /**
      * Adds the given sprite to this sprites zone.
      * @param sprite the sprite tot be added to the Zone
      */
     protected final void addSpriteToZone(final BaseSprite sprite) {
         getData().addSprite(sprite);
     }
 
     /**
      * Gets the name assigned to this sprite as a String.
      * @return the name assigned to this sprite as a String
      */
     protected String getName() {
         return "BaseSprite";
     }
 
     protected abstract void draw(final Graphics g, final Bounds bounds);
 
     @Override
     protected abstract void init();
 
     /**
      * Updates the width of this sprite to that given.
      * @param width the new width of this sprite
      */
     protected abstract void updateWidth(int width);
 
     /**
      * Updates the height of this sprite to that given.
      * @param height the new height of this sprite
      */
     protected abstract void updateHeight(int height);
 
     /**
      * Updates the x of the sprite to that given.
      * @param x the new x coordinate of this sprite
      */
     protected abstract void updateX(int x);
 
     /**
      * Updates the y of the sprite to that given.
      * @param y the new y coordinate of this sprite
      */
     protected abstract void updateY(int y);
 
     /**
      * Updates the layer of the sprite of that given.
      * @param layer the new layer of this sprite
      */
     protected abstract void updateLayer(float layer);
 }
