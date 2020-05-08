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
 package org.blinz.graphics;
 
 import com.sun.opengl.util.j2d.TextRenderer;
 
 /**
  * Instances of this class representing various desired fonts are given to the Graphics
  * class to determine how to draw text.
  * @author Blinz Project
  */
 public final class Font {
 
     FontStub stub;
     private String name;
    private int size;
 
     Font() {
     }
 
     /**
      * Returns a String representing the name of this font.
      * @return String representing the name of this font
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns an int representing the size of this font.
      * @return in representing the size of this font.
      */
     public int getSize() {
        return size;
     }
 
     /**
      * Returns the width of the given character.
      * @param character
      * @return int width of the specified char, rounded from floating point value
      */
     public int getCharWidth(char character) {
         return Math.round(stub.getRenderer().getCharWidth(character));
     }
 
     /**
      * Returns this Font's shared font stub.
      * @return
      * @throws NullPointerException - likely means this font was not found.
      */
     FontStub getStub() throws NullPointerException {
         return stub;
     }
 
     @Override
     protected void finalize() throws Throwable {
         stub.dependentCount--;
         super.finalize();
     }
 }
 
 /**
  * Contains internal implementation of the font.
  * @author Blinz Project
  */
 final class FontStub {
 
     int dependentCount = 1;
     private TextRenderer renderer;
     private String name;
     private int size;
 
     /**
      *
      * @param name - name of font
      * @param size - font size
      * @param font - awt form of this font
      */
     FontStub(String name, int size, java.awt.Font font) {
         this.name = name;
         this.size = size;
         renderer = new TextRenderer(font);
     }
 
     /**
      * 
      * @return a String represting the name of the font this FontStub represents.
      */
     final String getName() {
         return name;
     }
 
     /**
      *
      * @return an int representing the size of the font this FontStub represents.
      */
     final int getSize() {
         return size;
     }
 
     /**
      *
      * @return the JOGL TextRender for the font this FontStub represents.
      */
     final TextRenderer getRenderer() {
         return renderer;
     }
 }
