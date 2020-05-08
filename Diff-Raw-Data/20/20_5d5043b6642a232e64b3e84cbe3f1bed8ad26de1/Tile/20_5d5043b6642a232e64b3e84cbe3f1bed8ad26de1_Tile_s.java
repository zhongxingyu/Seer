 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.first.tribes.core;
 
 import com.first.tribes.core.util.Updatee;
 import playn.core.Color;
 import playn.core.Surface;
 import pythagoras.f.Rectangle;
 import static playn.core.PlayN.*;
 import pythagoras.f.Point;
 
 /**
  *
  * @author taylor
  */
 public class Tile extends DrawnObject implements Updatee {
 
     public static final float TILE_SIZE = 100f;
     private int xIndex;
     private int yIndex;
 //    int color = Color.rgb((int)(random()/2*128), (int)(random()/2*128), (int)(random()/2*128));
     private float height = 2;
     private static final float WATER_FOOD_INCREMENT = 0.0002f;
     private static final float FOOD_SPREAD_MULTIPLIER = 0.00004f;
     private static final int normalWaterLevel=0;
    private static final double waterLevelDelta=2.5;
     private static double waterLevel=0;
     public float numFood = 0;
     private Tile[][] tileMap;
     private Tile[] neighbors;
 
     public Tile(int xIndex, int yIndex, Tile[][] tileMap) {
         this.xIndex = xIndex;
         this.yIndex = yIndex;
         this.tileMap = tileMap;
     }
 
     public Tile[] neighbors() {
         if (neighbors == null) {
             int neighborCount = 0;
             if (xIndex - 1 >= 0) {
                 neighborCount++;
             }
             if (xIndex + 1 < tileMap.length) {
                 neighborCount++;
             }
             if (yIndex - 1 >= 0) {
                 neighborCount++;
             }
             if (yIndex + 1 < tileMap[0].length) {
                 neighborCount++;
             }
             neighbors = new Tile[neighborCount];
             neighborCount = 0;
 
             if (xIndex - 1 >= 0) {
                 neighbors[neighborCount++] = tileMap[xIndex - 1][yIndex];
             }
             if (xIndex + 1 < tileMap.length) {
                 neighbors[neighborCount++] = tileMap[xIndex + 1][yIndex];
             }
             if (yIndex - 1 >= 0) {
                 neighbors[neighborCount++] = tileMap[xIndex][yIndex - 1];
             }
             if (yIndex + 1 < tileMap[0].length) {
                 neighbors[neighborCount++] = tileMap[xIndex][yIndex + 1];
             }
         }
         return neighbors;
     }
 
     public void setHeight(float height) {
         if (Math.abs(this.height - height) > 0.6) {
             this.height = height;
 
             for (Tile neighbor : neighbors()) {
                 neighbor.smoothHeight();
             }
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 61 * hash + this.xIndex;
         hash = 61 * hash + this.yIndex;
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         final Tile other = (Tile) obj;
         if (this.xIndex != other.xIndex)
             return false;
         if (this.yIndex != other.yIndex)
             return false;
         return true;
     }
 
     public int color() {
         if (height >= waterLevel) {
             if (height <= 40) {
                 // flat plains up to mountains
                 return Color.rgb((int) (255 - height * 4 - this.numFood * 2 < 0 ? 0 : 255 - height * 4 - this.numFood * 2), (int) (200 - height * 5 + (this.numFood) > 200 ? 200 : 200 - height * 5 + (this.numFood)), (int) (190 - height * 8 - this.numFood * 2 < 0 ? 0 : 190 - height * 8 - this.numFood * 2));
            } else {
                 // snow capped mountains
                 if (height <= 65) {
                     return Color.rgb((int) (230 + (height - 40)), (int) (200 + (height * 2 - 80)), (int) (200 + (height * 2 - 80)));
                 } else {
                     return Color.rgb(255, 255, 255);
                 }
             }
         } else {
             // Water
         	float heightSub=(float) Math.max(height-waterLevel,-100);
             return Color.rgb((int) (200 + heightSub * 2), (int) (200 + heightSub * 2), (int) (255 + heightSub));
        }
    }
 
     public static void raiseWaterLevel(){
     	waterLevel+=waterLevelDelta;
     }
     
     public static void lowerWaterLevel(){
     	waterLevel-=waterLevelDelta;
     }
     
     public static void restoreWaterLevel(){
     	waterLevel=normalWaterLevel;
     }
     
     
     public float height() {
         return height;
     }
 
     public void smoothHeight() {
         int count = 5;
 
         float newHeight = (height * height * height) * 5;
 
         for (Tile neighbor : neighbors()) {
             newHeight += neighbor.height * neighbor.height * neighbor.height;
             count++;
         }
 
         newHeight /= count;
 
 
         float mult = newHeight < 0 ? -1 : 1;
 
         this.setHeight((float) Math.pow(newHeight * mult, 0.333333) * mult);
     }
 
     public void paintToRect(Rectangle rect, Surface surface) {
         surface.setFillColor(color());
         surface.fillRect(rect.x, rect.y, rect.width, rect.height);
     }
 
     public Rectangle bounds() {
         return new Rectangle(xIndex * TILE_SIZE, yIndex * TILE_SIZE, TILE_SIZE, TILE_SIZE);
     }
 
     public Point centerPoint() {
         return bounds().center();
     }
 
     @Override
     public void update(float delta) {
         for (Tile neighbor : neighbors()) {
             if (neighbor.height() < waterLevel) {
                 this.numFood += WATER_FOOD_INCREMENT * delta / (this.numFood + 1);
             } else if (neighbor.numFood > 0) {
                 this.numFood += FOOD_SPREAD_MULTIPLIER * neighbor.numFood * delta / (this.numFood + 1);
             }
         }
     }
 
     public boolean isSafe(float minFood) {
         return height() > waterLevel-4 && numFood >= minFood;
     }
 }
