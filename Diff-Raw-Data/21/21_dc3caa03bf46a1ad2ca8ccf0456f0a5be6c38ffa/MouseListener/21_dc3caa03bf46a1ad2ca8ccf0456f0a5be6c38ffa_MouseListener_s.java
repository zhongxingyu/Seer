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
 package org.blinz.input;
 
 /**
  * Interface with the methods for mouse events pressed, released, and clicked.
  * @author Blinz Project
  */
 public interface MouseListener {
 
     /**
      * Called when a key is clicked.
      * @param buttonNumber id to be run against relevant possiblities from the Key class
      * @param clickCount number of times the button was clicked
     * @param x x coordinate of the location the mouse was clicked at
     * @param y y coordinate of the location the mouse was clicked at
      */
     public void buttonClick(int buttonNumber, int clickCount, int cursorX, int cursorY);
 
     /**
      * Called when a key is pressed.
      * @param buttonNumber id to be run against relevant possiblities from the Key class
     * @param x x coordinate of the location the mouse was clicked at
     * @param y y coordinate of the location the mouse was clicked at
      */
     public void buttonPress(int buttonNumber, int cursorX, int cursorY);
 
     /**
      * Called when a key is released.
      * @param buttonNumber id to be run against relevant possiblities from the Key class
     * @param x x coordinate of the location the mouse was clicked at
     * @param y y coordinate of the location the mouse was clicked at
      */
     public void buttonRelease(int buttonNumber, int cursorX, int cursorY);
 }
