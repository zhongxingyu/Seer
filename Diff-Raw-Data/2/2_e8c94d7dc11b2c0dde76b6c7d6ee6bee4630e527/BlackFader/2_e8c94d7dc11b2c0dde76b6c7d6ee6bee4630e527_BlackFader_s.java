 /*
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
 package com.gyver.matrixmover.fader;
 
 /**
  * The BlackFader.
  * 
  * @author Gyver
  */
 public class BlackFader extends Fader {
 
     /**
      * Instantiates a new crossfader.
      */
     public BlackFader() {
         super(FaderName.CROSSFADE);
     }
 
     @Override
     public int[] fade(int[] left, int[] right, int position) {
         
         short leftBlue, leftGreen, leftRed, rightBlue, rightGreen, rightRed;
         
         int[] mix = new int[left.length];
         
         float ratio = (position / 1000f);
         
         int col;
         
         for(int i = 0; i < left.length; i++){
             col = left[i];
             leftRed = (short) ((col >> 16) & 255);
             leftGreen = (short) ((col >> 8) & 255);
             leftBlue = (short) (col & 255);
 
             col = right[i];
             rightRed = (short) ((col >> 16) & 255);
             rightGreen = (short) ((col >> 8) & 255);
             rightBlue = (short) (col & 255);    
             
             if(ratio < 0.5){
                 float newratio = 1 - (ratio * 2);
                 rightRed = (short) (Math.round(leftRed * newratio));
                 rightGreen = (short) (Math.round(leftGreen * newratio));
                 rightBlue = (short) (Math.round(leftBlue * newratio));
            } else if (ratio > 0.5) {
                 float newratio = 1 - ((1 - ratio ) * 2);
                 rightRed = (short) (Math.round(rightRed * newratio));
                 rightGreen = (short) (Math.round(rightGreen * newratio));
                 rightBlue = (short) (Math.round(rightBlue * newratio));
             }
             
 //            rightRed += leftRed;
 //            rightGreen += leftGreen;
 //            rightBlue += leftBlue;
 //            
             if (rightRed > 255) {
                 rightRed = 255;
             }
             if (rightGreen > 255) {
                 rightGreen = 255;
             }
             if (rightBlue > 255) {
                 rightBlue = 255;
             }
             
             mix[i] = (rightRed << 16) | (rightGreen << 8) | rightBlue;
         }
         return mix;
     }
 }
