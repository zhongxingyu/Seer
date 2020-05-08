 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009  Blinz <gtalent2@gmail.com>
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
 
 import org.blinz.util.Bounds;
 import org.blinz.util.Position;
 import org.blinz.util.Size;
 
 /**
  *
  * @author Blinz
  */
 public abstract class Sprite extends BaseSprite {
 
     private int x = 0, y = 0;
     private float layer = 0;
     private short width = 1, height = 1;
 
     //PUBLIC METHODS------------------------------------------------------------
     /**
      * Marks the Sprite for removal from its Zone. After removal from its Zone
      * it will be unmarked for removal. If no other reference to it exists it
      * will be deleted by the garbage colletor.
      */
     @Override
     public void delete() {
         super.delete();
     }
 
     /**
      *
      * @return Size object representing the Sprite's size.
      */
     @Override
     public Size getSize() {
         return new Size(width, height);
     }
 
     /**
      * 
      * @return Width of Sprite.
      */
     @Override
     public int getWidth() {
         return width;
     }
 
     /**
      * 
      * @return Height of Sprite
      */
     @Override
     public int getHeight() {
         return height;
     }
 
     /**
      * 
      * @return Location object representing the Sprite's location.
      */
     @Override
     public Position getPosition() {
         return new Position(x, y);
     }
 
     /**
      * 
      * @return The x coordinate of the Sprite.
      */
     @Override
     public int getX() {
         return x;
     }
 
     /**
      * 
      * @return The y coordinate of the Sprite.
      */
     @Override
     public int getY() {
         return y;
     }
 
     /**
      *
      * @return The z coordinate of the Sprite.
      */
     @Override
     public float getLayer() {
         return layer;
     }
 
     @Override
     public Bounds getBounds() {
         return new Bounds(x, y, width, height);
     }
 
     public void moveDown(int distance) {
        if (y + height + distance > getData().zoneSize.height) {
            distance = getData().zoneSize.height - y;
        }
        updateY(y + distance);
        y += distance;
     }
 
     public void moveUp(int distance) {
        if (y - distance < 0) {
            distance -= y - distance;
        }

        updateY(y - distance);
        y -= distance;
     }
 
     public void moveRight(int distance) {
        if (x + width + distance > getData().zoneSize.width) {
            distance = getData().zoneSize.width - x;
        }

        updateX(x + distance);
        x += distance;
     }
 
     public void moveLeft(int distance) {
        if (x - distance < 0) {
            distance -= x - distance;
        }

        updateX(x - distance);
        x -= distance;
     }
 
     //END OF PUBLIC METHODS/////////////////////////////////////////////////////
     //PROTECTED METHODS---------------------------------------------------------
     /**
      *
      * @return the amount of time the Zone has been executing, not including the
      * time before it started executing for time it was paused.
      */
     protected long zoneTime() {
         return getData().zoneTime;
     }
 
     @Override
     protected void updateWidth(int width) {
         if (width < 1) {
             width = 1;
         }
         setWidth(width);
         this.width = (short) width;
     }
 
     @Override
     protected void updateHeight(int height) {
         if (height < 1) {
             height = 1;
         }
         updateHeight(height);
         this.height = (short) height;
     }
 
     @Override
     protected void updateX(int x) {
         this.x = x;
     }
 
     @Override
     protected void updateY(int y) {
         this.y = y;
     }
 
     @Override
     protected void updateLayer(float layer) {
         this.layer = layer;
     }
 
     @Override
     protected void init() {
         init();
     }
 
     //END OF PROTECTED METHODS//////////////////////////////////////////////////
 //DEFAULT ACCESS METHODS////////////////////////////////////////////////////
     void detectCollision(Sprite sprite) {
         boolean wasHit = false;
         if (this instanceof CollidableSprite && sprite instanceof CollidableSprite) {
             CollidableSprite me = (CollidableSprite) this, him = (CollidableSprite) sprite;
             if (me.frontBumper().intersects(him.backBumper())) {
                 me.topBumperCollisionReaction(sprite);
                 him.bottumBumperCollisionReaction(this);
                 wasHit = true;
             }
 
             if (me.backBumper().intersects(him.frontBumper())) {
                 me.bottumBumperCollisionReaction(sprite);
                 him.topBumperCollisionReaction(this);
                 wasHit = true;
             }
 
             if (me.leftBumper().intersects(him.rightBumper())) {
                 me.leftBumperCollisionReaction(sprite);
                 him.rightBumperCollisionReaction(this);
                 wasHit = true;
             }
 
             if (me.rightBumper().intersects(him.leftBumper())) {
                 me.rightBumperCollisionReaction(sprite);
                 him.leftBumperCollisionReaction(this);
                 wasHit = true;
             }
 
             if (wasHit) {
                 me.collisionReaction(sprite);
                 him.collisionReaction(this);
             }
 
         }
     }
 
     //END OF DEFAULT ACCESS METHODS/////////////////////////////////////////////
     //PRIVATE METHODS-----------------------------------------------------------
     //END OF PRIVATE METHODS////////////////////////////////////////////////////
     //ABSTRACT METHODS----------------------------------------------------------
 }
