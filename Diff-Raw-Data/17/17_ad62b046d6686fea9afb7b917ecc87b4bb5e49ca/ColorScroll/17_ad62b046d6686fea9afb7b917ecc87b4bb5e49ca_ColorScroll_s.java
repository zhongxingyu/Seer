 /*
  * Copyright (C) 2011 Michael Vogt <michu@neophob.com>
  * Copyright (C) 2012 Gyver
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.gyver.matrixmover.generator;
 
 import com.gyver.matrixmover.core.Controller;
 import com.gyver.matrixmover.core.MatrixData;
 import java.awt.Color;
 import java.util.List;
 
 /**
  * The Class ColorScroll
  * 
  * Code-parts copied from http://github.com/neophob/PixelController
  *
  * @author Gyver
  */
 public class ColorScroll extends ColorMapAwareGenerator {
 
     /** The fade. */
     private int fade;
     /** The scroll mode. */
     private ScrollMode scrollMode;
     /** The frame count. */
     private float frameCount;
     /** The internal buffer x size2. */
     private int internalBufferHalfWidth;
     /** The internal buffer y size2. */
     private int internalBufferHalfHeight;
     /** The speed given in bpm */
     private int speed;
     private double forward;
 
     /**
      * The Enum ScrollMode.
      */
     public enum ScrollMode {
 
         LEFT_TO_RIGHT(0),
         RIGHT_TO_LEFT(1),
         TOP_TO_BOTTOM(2),
         BOTTOM_TO_TOP(3),
         RIGHT_BOTTOM_TO_LEFT_TOP(4),
         LEFT_BOTTOM_TO_RIGHT_TOP(5),
         RIGHT_TOP_TO_LEFT_BOTTOM(6),
         LEFT_TOP_TO_RIGHT_BOTTOM(7),
         MIDDLE_TO_SIDES_VERTICAL(8),
         SIDES_TO_MIDDLE_VERTICAL(9),
         MIDDLE_TO_SIDES_HORIZONTAL(10),
         SIDES_TO_MIDDLE_HORIZONTAL(11),
         EXPLODE_CIRCLE(12),
         IMPLODE_CIRCLE(13);
         /** The mode. */
         private int mode;
 
         /**
          * Instantiates a new scroll mode.
          *
          * @param mode the mode
          */
         private ScrollMode(int mode) {
             this.mode = mode;
         }
 
         /**
          * Gets the mode.
          *
          * @return the mode
          */
         public int getMode() {
             return mode;
         }
 
         /**
          * Gets the scroll mode.
          *
          * @param nr the nr
          * @return the scroll mode
          */
         public static ScrollMode getScrollMode(int nr) {
             for (ScrollMode s : ScrollMode.values()) {
                 if (s.getMode() == nr) {
                     return s;
                 }
             }
             return null;
         }
 
         @Override
         public String toString() {
             switch (this) {
                 case LEFT_TO_RIGHT:
                     return "Left to Right";
                 case RIGHT_TO_LEFT:
                     return "Right to Left";
                 case TOP_TO_BOTTOM:
                     return "Top to Bottom";
                 case BOTTOM_TO_TOP:
                     return "Bottom to Top";
                 case RIGHT_BOTTOM_TO_LEFT_TOP:
                     return "Right-Bottom to Left-Top";
                 case LEFT_BOTTOM_TO_RIGHT_TOP:
                     return "Left-Bottom to Right-Top";
                 case RIGHT_TOP_TO_LEFT_BOTTOM:
                     return "Right-Top to Left-Bottom";
                 case LEFT_TOP_TO_RIGHT_BOTTOM:
                     return "Left-Top to Right-Bottom";
                 case MIDDLE_TO_SIDES_VERTICAL:
                     return "Middle to Sides Vertical";
                 case SIDES_TO_MIDDLE_VERTICAL:
                     return "Sides to Middle Vertical";
                 case MIDDLE_TO_SIDES_HORIZONTAL:
                     return "Middle to Sides Horizontal";
                 case SIDES_TO_MIDDLE_HORIZONTAL:
                     return "Sides to Biddle Horizontal";
                 case EXPLODE_CIRCLE:
                     return "Explode Circle";
                 case IMPLODE_CIRCLE:
                     return "Implode Circle";
                 default:
                     super.toString();
             }
             // if it has no string, return the enum-string
             return super.toString();
         }
     }
 
     /**
      * Instantiates a new colorscroll.
      *
      * @param controller the controller
      * @param colorMap the color list
      */
     public ColorScroll(MatrixData matrix, List<Color> colorMap) {
         super(GeneratorName.COLOR_SCROLL, matrix, colorMap);
 
         this.fade = 15;
 
         this.speed = 0x78;
 
         scrollMode = ScrollMode.EXPLODE_CIRCLE;
 
         internalBufferHalfWidth = internalBufferWidth / 2;
         internalBufferHalfHeight = internalBufferHeight / 2;
     }
 
     @Override
     public void update() {
 
         int referenceScreenSizeForSpeed = 0;
 
         // scroll colors on x axis
         switch (scrollMode) {
             case LEFT_TO_RIGHT:
                 leftToRight();
                 referenceScreenSizeForSpeed = internalBufferWidth;
                 break;
             case RIGHT_TO_LEFT:
                 rightToLeft();
                 referenceScreenSizeForSpeed = internalBufferWidth;
                 break;
             case TOP_TO_BOTTOM:
                 topToBottom();
                 referenceScreenSizeForSpeed = internalBufferHeight;
                 break;
             case BOTTOM_TO_TOP:
                 referenceScreenSizeForSpeed = internalBufferHeight;
                 bottomToTop();
                 break;
             case RIGHT_BOTTOM_TO_LEFT_TOP:
                 referenceScreenSizeForSpeed = internalBufferWidth + internalBufferWidth;
                 rightBottomToLeftTop();
                 break;
             case LEFT_BOTTOM_TO_RIGHT_TOP:
                 referenceScreenSizeForSpeed = internalBufferWidth + internalBufferWidth;
                 leftBottomToRightTop();
                 break;
             case RIGHT_TOP_TO_LEFT_BOTTOM:
                 referenceScreenSizeForSpeed = internalBufferWidth + internalBufferWidth;
                 rightTopToLeftBottom();
                 break;
             case LEFT_TOP_TO_RIGHT_BOTTOM:
                 referenceScreenSizeForSpeed = internalBufferWidth + internalBufferWidth;
                 leftTopToRightBottom();
                 break;
             case MIDDLE_TO_SIDES_VERTICAL:
                 referenceScreenSizeForSpeed = internalBufferHalfWidth;
                 middleToSidesVertical();
                 break;
             case SIDES_TO_MIDDLE_VERTICAL:
                 referenceScreenSizeForSpeed = internalBufferHalfWidth;
                 sidesToMiddleVertical();
                 break;
             case MIDDLE_TO_SIDES_HORIZONTAL:
                 referenceScreenSizeForSpeed = internalBufferHalfHeight;
                 middleToSidesHorizontal();
                 break;
             case SIDES_TO_MIDDLE_HORIZONTAL:
                 referenceScreenSizeForSpeed = internalBufferHalfHeight;
                 sidesToMiddleHorizontal();
                 break;
             case EXPLODE_CIRCLE:
                 referenceScreenSizeForSpeed = Math.max(internalBufferHalfHeight, internalBufferHalfWidth);
                 explodeCircle();
                 break;
             case IMPLODE_CIRCLE:
                 referenceScreenSizeForSpeed = Math.max(internalBufferHalfHeight, internalBufferHalfWidth);
                 implodeCircle();
                 break;
         }
 
         int fps = Controller.getControllerInstance().getFps();
         forward = (speed / 60F);
         forward = forward * (fade / (double) fps);
 
         frameCount = (float) (frameCount + forward);
     }
 
     public void setSpeed(int speed) {
         this.speed = speed;
     }
 
     public int getSpeed() {
         return speed;
     }
 
     /**
      * Sets the scroll mode.
      *
      * @param scrollMode the new scroll mode
      */
     void setScrollMode(int scrollMode) {
         this.scrollMode = ScrollMode.getScrollMode(scrollMode);
     }
 
     /**
      * Sets the fade length.
      *
      * @param fadeLength the new fade length
      */
     public void setFadeLength(int fadeLength) {
         this.fade = fadeLength;
     }
 
     public int getFadeLength() {
         return fade;
     }
 
     /**
      * Gets the color.
      *
      * @param val the val
      * @return the color
      */
     private int getColor(double val) {
         int saveFade = this.fade;
         if (saveFade == 0) {
             saveFade = 1;
         }
 
         int colornumber = (int) (Math.round(Math.floor((val + frameCount) / saveFade)));
         colornumber = colornumber % colorMap.size();
         int nextcolornumber = (colornumber + 1) % colorMap.size();
         float ratio = (float) (((val + frameCount) % saveFade) / (float) saveFade);
         return super.getColor(colornumber, nextcolornumber, ratio);
     }
 
     /**
      * Left to right.
      */
     private void leftToRight() {
         for (int x = 0; x < internalBufferWidth; x++) {
             int xRev = internalBufferWidth - x - 1;
 
             int col = getColor(x);
             for (int y = 0; y < internalBufferHeight; y++) {
                 this.internalBuffer[y * internalBufferWidth + xRev] = col;
             }
         }
     }
 
     /**
      * Right to left.
      */
     private void rightToLeft() {
         for (int x = 0; x < internalBufferWidth; x++) {
             int col = getColor(x);
             for (int y = 0; y < internalBufferHeight; y++) {
                 this.internalBuffer[y * internalBufferWidth + x] = col;
             }
         }
     }
 
     /**
      * Top to bottom.
      */
     private void topToBottom() {
         for (int y = 0; y < internalBufferHeight; y++) {
             int yRev = internalBufferHeight - y - 1;
             int col = getColor(y);
             for (int x = 0; x < internalBufferWidth; x++) {
                 this.internalBuffer[yRev * internalBufferWidth + x] = col;
             }
         }
     }
 
     /**
      * Bottom to top.
      */
     private void bottomToTop() {
         for (int y = 0; y < internalBufferHeight; y++) {
             int col = getColor(y);
             for (int x = 0; x < internalBufferWidth; x++) {
                 this.internalBuffer[y * internalBufferWidth + x] = col;
             }
         }
     }
 
     /**
      * Right bottom to left top.
      */
     private void rightBottomToLeftTop() {
         int bigSide = Math.max(internalBufferWidth, internalBufferHeight);
         for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
 
             int col = getColor(diagStep);
 
             int diagPixelCount = diagStep;
             int diagOffset = 0;
             if (diagStep >= bigSide) {
                 diagPixelCount = (2 * bigSide) - diagStep;
                 diagOffset = diagStep - bigSide;
             }
 
             for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                 int x = diagOffset + diagCounter;
                 int y = diagPixelCount - diagCounter - 1 + diagOffset;
                 setPixel(x, y, col);
             }
         }
     }
 
     /**
      * Left bottom to right top.
      */
     private void leftBottomToRightTop() {
         int bigSide = Math.max(internalBufferWidth, internalBufferHeight);
         for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
             int col = getColor(diagStep);
 
             int diagPixelCount = diagStep;
             int diagOffset = 0;
             if (diagStep >= bigSide) {
                 diagPixelCount = (2 * bigSide) - diagStep;
                 diagOffset = diagStep - bigSide;
             }
 
             for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                 int x = internalBufferWidth - 1 - (diagOffset + diagCounter);
                 int y = diagPixelCount - diagCounter - 1 + diagOffset;
                 setPixel(x, y, col);
             }
         }
     }
 
     /**
      * Right top to left bottom.
      */
     private void rightTopToLeftBottom() {
         int bigSide = Math.max(internalBufferWidth, internalBufferHeight);
         for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
             int col = getColor(diagStep);
 
             int diagPixelCount = diagStep;
             int diagOffset = 0;
             if (diagStep >= bigSide) {
                 diagPixelCount = (2 * bigSide) - diagStep;
                 diagOffset = diagStep - bigSide;
             }
 
             for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                 int x = diagOffset + diagCounter;
                 int y = internalBufferHeight - 1 - (diagPixelCount - diagCounter - 1 + diagOffset);
                 setPixel(x, y, col);
             }
         }
     }
 
     /**
      * Left top to right bottom.
      */
     private void leftTopToRightBottom() {
         int bigSide = Math.max(internalBufferWidth, internalBufferHeight);
         for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
             int col = getColor(diagStep);
 
             int diagPixelCount = diagStep;
             int diagOffset = 0;
             if (diagStep >= bigSide) {
                 diagPixelCount = (2 * bigSide) - diagStep;
                 diagOffset = diagStep - bigSide;
             }
 
             for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                 int x = internalBufferWidth - 1 - (diagOffset + diagCounter);
                 int y = internalBufferHeight - 1 - (diagPixelCount - diagCounter - 1 + diagOffset);
                 setPixel(x, y, col);
             }
         }
     }
 
     /**
      * Middle to sides vertical.
      */
     private void middleToSidesVertical() {
         int ySize = internalBufferHeight;
 
        for (int x = 0; x < internalBufferHalfWidth; x++) {
             int col = getColor(x);
 
             for (int y = 0; y < ySize; y++) {
                 this.internalBuffer[y * internalBufferWidth + x] = col;
                 this.internalBuffer[y * internalBufferWidth + internalBufferWidth - x - 1] = col;
             }
         }
     }
 
     /**
      * Sides to middle vertical.
      */
     private void sidesToMiddleVertical() {
         int ySize = internalBufferHeight;
 
        for (int x = 0; x < internalBufferHalfWidth; x++) {
 
            int xRev = (internalBufferHalfWidth) - x - 1;
             int col = getColor(x);
             for (int y = 0; y < ySize; y++) {
                 this.internalBuffer[y * internalBufferWidth + xRev] = col;
                 this.internalBuffer[y * internalBufferWidth + internalBufferWidth - xRev - 1] = col;
             }
         }
     }
 
     /**
      * Middle to sides horizontal.
      */
     private void middleToSidesHorizontal() {
         int xSize = internalBufferWidth;
 
        for (int y = 0; y < internalBufferHalfHeight; y++) {
 
             int col = getColor(y);
 
             for (int x = 0; x < xSize; x++) {
                 this.internalBuffer[y * internalBufferWidth + x] = col;
                 this.internalBuffer[(internalBufferHeight - y - 1) * internalBufferWidth + x] = col;
             }
         }
     }
 
     /**
      * Sides to middle horizontal.
      */
     private void sidesToMiddleHorizontal() {
         int xSize = internalBufferWidth;
 
        for (int y = 0; y < internalBufferHalfHeight; y++) {
 
            int yRev = internalBufferHalfHeight - y - 1;
             int col = getColor(y);
 
             for (int x = 0; x < xSize; x++) {
                 this.internalBuffer[yRev * internalBufferWidth + x] = col;
                 this.internalBuffer[(internalBufferHeight - yRev - 1) * internalBufferWidth + x] = col;
             }
         }
     }
 
     /**
      * Implode circle.
      */
     private void implodeCircle() {
 
         int upToValue = (int) (Math.max(internalBufferWidth, internalBufferHeight) * 1.42f);
         for (double r = 0; r < upToValue; r = r + 0.1) {
             int col = getColor(r);
 
             double f = 1 - r;
             int ddFx = 1;
             double ddFy = -2 * r;
             int x = 0;
             double y = r;
 
             setPixel(internalBufferHalfWidth, (int) Math.round(internalBufferHalfHeight + r), col);
             setPixel(internalBufferHalfWidth, (int) Math.round(internalBufferHalfHeight - r), col);
             setPixel((int) Math.round(internalBufferHalfWidth + r), internalBufferHalfHeight, col);
             setPixel((int) Math.round(internalBufferHalfWidth - r), internalBufferHalfHeight, col);
 
             while (x < y) {
                 if (f >= 0) {
                     y--;
                     ddFy += 2;
                     f += ddFy;
                 }
                 x++;
                 ddFx += 2;
                 f += ddFx;
                 setPixel(internalBufferHalfWidth + x, (int) Math.round(internalBufferHalfHeight + y), col);
                 setPixel(internalBufferHalfWidth - x, (int) Math.round(internalBufferHalfHeight + y), col);
                 setPixel(internalBufferHalfWidth + x, (int) Math.round(internalBufferHalfHeight - y), col);
                 setPixel(internalBufferHalfWidth - x, (int) Math.round(internalBufferHalfHeight - y), col);
                 setPixel((int) Math.round(internalBufferHalfWidth + y), internalBufferHalfHeight + x, col);
                 setPixel((int) Math.round(internalBufferHalfWidth - y), internalBufferHalfHeight + x, col);
                 setPixel((int) Math.round(internalBufferHalfWidth + y), internalBufferHalfHeight - x, col);
                 setPixel((int) Math.round(internalBufferHalfWidth - y), internalBufferHalfHeight - x, col);
 
             }
         }
     }
 
     /**
      * Explode circle.
      */
     private void explodeCircle() {
 
         int upToValue = (int) (Math.max(internalBufferWidth, internalBufferHeight) * 1.42f);
         for (double r = 0; r < upToValue; r = r + 0.1) {
             double rRev = upToValue - r;
             int col = getColor(rRev);
 
             double f = 1 - r;
             int ddFx = 1;
             double ddFy = -2 * r;
             int x = 0;
             double y = r;
 
             setPixel(internalBufferHalfWidth, (int) Math.round(internalBufferHalfHeight + r), col);
             setPixel(internalBufferHalfWidth, (int) Math.round(internalBufferHalfHeight - r), col);
             setPixel((int) Math.round(internalBufferHalfWidth + r), internalBufferHalfHeight, col);
             setPixel((int) Math.round(internalBufferHalfWidth - r), internalBufferHalfHeight, col);
 
             while (x < y) {
                 if (f >= 0) {
                     y--;
                     ddFy += 2;
                     f += ddFy;
                 }
                 x++;
                 ddFx += 2;
                 f += ddFx;
                 setPixel(internalBufferHalfWidth + x, (int) Math.round(internalBufferHalfHeight + y), col);
                 setPixel(internalBufferHalfWidth - x, (int) Math.round(internalBufferHalfHeight + y), col);
                 setPixel(internalBufferHalfWidth + x, (int) Math.round(internalBufferHalfHeight - y), col);
                 setPixel(internalBufferHalfWidth - x, (int) Math.round(internalBufferHalfHeight - y), col);
                 setPixel((int) Math.round(internalBufferHalfWidth + y), internalBufferHalfHeight + x, col);
                 setPixel((int) Math.round(internalBufferHalfWidth - y), internalBufferHalfHeight + x, col);
                 setPixel((int) Math.round(internalBufferHalfWidth + y), internalBufferHalfHeight - x, col);
                 setPixel((int) Math.round(internalBufferHalfWidth - y), internalBufferHalfHeight - x, col);
 
             }
         }
     }
 
     /**
      * Sets the pixel.
      *
      * @param x the x
      * @param y the y
      * @param col the col
      */
     private void setPixel(int x, int y, int col) {
         if (y >= 0 && y < internalBufferHeight && x >= 0 && x < internalBufferWidth) {
             this.internalBuffer[y * internalBufferWidth + x] = col;
         }
     }
 
     /**
      * @return the scrollMode
      */
     public ScrollMode getScrollMode() {
         return scrollMode;
     }
 
     /**
      * @param scrollMode the scrollMode to set
      */
     public void setScrollMode(ScrollMode scrollMode) {
         this.scrollMode = scrollMode;
     }
 }
