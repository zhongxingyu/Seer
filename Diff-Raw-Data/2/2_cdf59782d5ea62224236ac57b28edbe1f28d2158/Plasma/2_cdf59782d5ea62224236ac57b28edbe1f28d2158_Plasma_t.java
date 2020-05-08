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
 
 import com.gyver.matrixmover.core.MatrixData;
 import java.awt.Color;
 import java.util.List;
 
 /**
  * Plasma Generator, ripped form openprocessing
  * 
  * Code-parts copied from http://github.com/neophob/PixelController
  *
  * @author Gyver
  */
 public class Plasma extends ColorMapAwareGenerator {
 
     /** The frame count. */
     private float timeDisplacement;
     
     private float offset = 20;
     private float zoom = 2;
     private int distance = 1024;
     private float speed = 1;
 
     /**
      * Instantiates a new plasma2.
      *
      * @param controller the controller
      */
     public Plasma(MatrixData matrix, List<Color> colorMap) {
        super(GeneratorName.PLASMA, matrix, colorMap);
         timeDisplacement = 1;
     }
 
     @Override
     public void update() {
         float xc = offset;
 
         timeDisplacement = timeDisplacement + speed;
 
         // No need to do this math for every pixel
         float calculation1 = (float) Math.sin(Math.toRadians(timeDisplacement * 0.61655617f));
         float calculation2 = (float) Math.sin(Math.toRadians(timeDisplacement * -3.6352262f));
 
         int aaa = this.distance;
         int ySize = internalBufferHeight;
         // Plasma algorithm
         for (int x = 0; x < internalBufferWidth; x++) {
             xc = xc + zoom;
             float yc = offset;
             float s1 = aaa + aaa * (float) Math.sin(Math.toRadians(xc) * calculation1);
 
             for (int y = 0; y < ySize; y++) {
                 yc = yc + zoom;
                 float s2 = aaa + aaa * (float) Math.sin(Math.toRadians(yc) * calculation2);
                 float s3 = aaa + aaa * (float) Math.sin(Math.toRadians((xc + yc + timeDisplacement * 5) / 2));
                 float s = (s1 + s2 + s3) / (6f * 255f);
                 this.internalBuffer[y * internalBufferWidth + x] = getColor(s);
             }
         }
     }
     
     public void setOffset(int offset){
         this.offset = (float) (offset / 10.0);
     }
     
     public int getOffset(){
         return (int) (this.offset * 10);
     }
     
     public void setZoom(int zoom){
         this.zoom = (float) (zoom / 100.0);
     }
     
     public int getZoom(){
         return (int) this.zoom*100;
     }
     
     public void setDistance(int distance){
         this.distance = (int) Math.round(Math.pow(1.1, (100-(double)distance)));
     }
 
     public int getDistance(){
         return (int) Math.round(100 - (Math.log(distance) / Math.log(1.1)));
     }
     
     public int getSpeed(){
         return (int) (speed * 50);
     }
     
     public void setSpeed(int speed){
         this.speed = (speed / 50f);
     }
     
     /**
      *
      * @param s
      * @return
      */
     private int getColor(float s) {
         //reduce s to [0-1]
         s = (s - (float) Math.floor(s)) * colorMap.size();
 
         int colornumber = (int) Math.floor(s);
         int nextcolornumber = (colornumber + 1) % colorMap.size();
 
         //use sinus as cross over function for much smoother transitions
         float ratio = (float) (Math.cos((s - colornumber) * Math.PI + Math.PI) + 1) / 2;
 
         return super.getColor(colornumber, nextcolornumber, ratio);
     }
 }
