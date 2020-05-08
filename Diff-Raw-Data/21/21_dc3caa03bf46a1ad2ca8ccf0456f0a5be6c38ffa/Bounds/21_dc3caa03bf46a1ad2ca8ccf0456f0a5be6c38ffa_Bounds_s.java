 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2009  BlinzProject <gtalent2@gmail.com>
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
 package org.blinz.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Blinz Project
  */
 public class Bounds {
 
     int width = 0;
     int height = 0;
     int x = 0;
     int y = 0;
 
     /**
      * Default constructor with no parameters, does nothing.
      */
     public Bounds() {
     }
 
     /**
      * Creates a new Bounds with the specified coordinates and dimensions.
      * @param x
      * @param y
      * @param width
      * @param height
      */
     public Bounds(int x, int y, int width, int height) {
         setBounds(x, y, width, height);
     }
 
     /**
      * Creates a new Bounds with attributes of the given Bounds.
      * @param bounds
      */
     public Bounds(Bounds bounds) {
         setBounds(bounds);
     }
 
     /**
      * Gets the x of this Bounds.
      * @return x
      */
     public final int getX() {
         return x;
     }
 
     /**
      * Gets the y of this Bounds.
      * @return y
      */
     public final int getY() {
         return y;
     }
 
     /**
      * Gets the width of this Bounds.
      * @return width
      */
     public final int getWidth() {
         return width;
     }
 
     /**
      * Gets the height of this Bounds.
      * @return height
      */
     public final int getHeight() {
         return height;
     }
 
     /**
      * Sets the x of this Bounds.
      * @param x
      */
     public final void setX(int x) {
         this.x = x;
     }
 
     /**
      * Sets the y of this Bounds.
      * @param y
      */
     public final void setY(int y) {
         this.y = y;
     }
 
     /**
      * Sets the coordinates of this Bounds to that of the given Location object.
      * @param location
      */
     public final void setPosition(Position location) {
         x = location.x;
         y = location.y;
     }
 
     /**
      * Sets the coordinates of this Bounds to the given coordinates.
      * @param x
      * @param y
      */
     public final void setPosition(int x, int y) {
         this.x = x;
         this.y = y;
     }
 
     /**
      * Sets the width of this Bounds.
      * @param width
      */
     public final void setWidth(int width) {
         this.width = width;
     }
 
     /**
      * Sets the height of this Bounds.
      * @param height
      */
     public final void setHeight(int height) {
         this.height = height;
     }
 
     /**
      * Sets the size attributes of this Bounds to the given width and height.
      * @param width
      * @param height
      */
     public final void setSize(int width, int height) {
         this.width = width;
         this.height = height;
     }
 
     /**
      * Sets the size attributes of this Bounds that of the give Size object.
      * @param size
      */
     public final void setSize(Size size) {
         setSize(size.width, size.height);
     }
 
     /**
      * Addes xMod to the current x.
      * @param xMod
      */
     public final void modX(int xMod) {
         x += xMod;
     }
 
     /**
      * Adds yMod to the current y.
      * @param yMod
      */
     public final void modY(int yMod) {
         y += yMod;
     }
 
     /**
      * Adds widthMod to the current width.
      * @param widthMod
      */
     public final void modWidth(int widthMod) {
         width += widthMod;
     }
 
     /**
      * Adds heightMod to the current height.
      * @param heightMod
      */
     public final void modHeight(int heightMod) {
         height += heightMod;
     }
 
     /**
      * Sets the Bounds object to the given parameters.
      * @param x
      * @param y
      * @param width
      * @param height
      */
     public final void setBounds(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
     }
 
     /**
      *
      * @param bounds
      */
     public final void setBounds(Bounds bounds) {
         setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
     }
 
     /**
      * Returns true if the Bounds object intersects the given line.
      * Note: broken
      * @param x1
      * @param y1
      * @param x2
      * @param y2
      * @return true if given line intersects this Bounds
      */
     public final boolean intersectsLine(int x1, int y1, int x2, int y2) {
         return contains(x1, y1) || contains(x2, y2);
     }
 
     /**
      * Returns true if it intersects the given Bounds.
      * @param bounds
     * @return
      */
     public final boolean intersects(Bounds bounds) {
         return bounds.width > 0 && bounds.height > 0 && width > 0 && height > 0 &&
                 bounds.x < x + width && bounds.x + bounds.width > x && bounds.y < y + height &&
                 bounds.y + bounds.height > y;
     }
 
     /**
      * Static method that takes two bounds and checks to see if they intersect.
      * @param x1
      * @param x2
      * @param y1
      * @param y2
      * @param width1
      * @param width2
      * @param height1
      * @param height2
      * @return true if the given bounds intersect
      */
     public static final boolean intersects(int x1, int x2, int y1, int y2, int width1, int width2, int height1, int height2) {
         return width1 > 0 && height1 > 0 && width2 > 0 && height2 > 0 &&
                 x1 < x2 + width2 && x1 + width1 > x2 &&
                 y1 < y2 + height2 && y1 + height1 > y2;
     }
 
     /**
      * Checks to see if the given bounds intersects with this.
      * @param x
      * @param y
      * @param width
      * @param height
      * @return true if the given bounds intersects with this, false otherwise
      */
     public final boolean intersects(int x, int y, int width, int height) {
         return width > 0 && height > 0 && this.width > 0 && this.height > 0 &&
                 x < this.x + this.width && x + width > this.x &&
                 y < this.y + this.height && y + height > this.y;
     }
 
     /**
      * Checks to see if the given point is within this Bounds.
      * @param x
      * @param y
      * @return true if this Bounds contains the given point, false otherwise
      */
     public final boolean contains(int x, int y) {
         return width > 0 && height > 0 && x >= this.x && x < this.x + width &&
                 y >= this.y && y < this.y + height;
     }
 
     /**
      * Checks to see if the given Position is within this Bounds.
      * @param loc
      * @return true if this Bounds contains the given Position, false otherwise
      */
     public final boolean contains(Position loc) {
         return contains(loc.x, loc.y);
     }
 }
