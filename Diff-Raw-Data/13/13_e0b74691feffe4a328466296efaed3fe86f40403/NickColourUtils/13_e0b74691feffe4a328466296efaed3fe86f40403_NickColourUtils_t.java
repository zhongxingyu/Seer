 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.nickcolours;
 
 import com.dmdirc.ui.messages.ColourManager;
 import com.dmdirc.util.colours.Colour;
 import com.dmdirc.util.colours.ColourUtils;
 
 import java.awt.Color;
 
 final class NickColourUtils {
 
     private NickColourUtils() {
         //Utility class
     }
 
     static Color getColorFromString(final ColourManager colourManager, final String value) {
         final Colour colour = colourManager.getColourFromString(value, null);
         return new Color(colour.getRed(), colour.getGreen(), colour.getBlue());
     }
 
     static String getStringFromColor(final Color color) {
        if (color == null) {
            return "";
        }
         return ColourUtils.getHex(new Colour(color.getRed(), color.getGreen(), color.getBlue()));
     }
 
     static Colour getColourfromColor(final Color color) {
         return new Colour(color.getRed(), color.getGreen(), color.getBlue());
     }
 }
