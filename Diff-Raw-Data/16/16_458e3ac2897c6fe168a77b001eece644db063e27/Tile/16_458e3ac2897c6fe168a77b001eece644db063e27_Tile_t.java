 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.first.tribes.core;
 
 import com.first.tribes.core.util.Updatee;
 import java.util.ArrayList;
 import java.util.List;
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
 
     public static final float SNOW_PEAK_HEIGHT = 40;
     public static final float TILE_SIZE = 100f;
     private static final float WATER_FOOD_INCREMENT = 0.0002f;
     private static final float FOOD_SPREAD_MULTIPLIER = 0.00004f;
     private int xIndex;
     private int yIndex;
     private float height = 2;
     public float numFood = 0;
     private TribesWorld world;
     static boolean debug = true;
     public static float proportion = 1f;
     private List<Accessory> accessories;
 
     public Tile(int xIndex, int yIndex, TribesWorld world) {
         this.xIndex = xIndex;
         this.yIndex = yIndex;
         this.world = world;
         this.accessories = new ArrayList<Accessory>();
     }
     private Tile[] neighbors; // Cache for neighbors. No need to recalculate.
 
     public Tile[] neighbors() {
         if (neighbors == null) {
             int neighborCount = 0;
             if (xIndex - 1 >= 0) {
                 neighborCount++;
             }
             if (xIndex + 1 < world.tiles().length) {
                 neighborCount++;
             }
             if (yIndex - 1 >= 0) {
                 neighborCount++;
             }
             if (yIndex + 1 < world.tiles()[0].length) {
                 neighborCount++;
             }
             neighbors = new Tile[neighborCount];
             neighborCount = 0;
 
             if (xIndex - 1 >= 0) {
                 neighbors[neighborCount++] = world.tiles()[xIndex - 1][yIndex];
             }
             if (xIndex + 1 < world.tiles().length) {
                 neighbors[neighborCount++] = world.tiles()[xIndex + 1][yIndex];
             }
             if (yIndex - 1 >= 0) {
                 neighbors[neighborCount++] = world.tiles()[xIndex][yIndex - 1];
             }
             if (yIndex + 1 < world.tiles()[0].length) {
                 neighbors[neighborCount++] = world.tiles()[xIndex][yIndex + 1];
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
 
     	float red=0;
     	float blue=0;
     	float green =0;
     	int c = 0;
     	
 	    if (height >= world.waterLevel) {
             if (height <= SNOW_PEAK_HEIGHT) {
                 // flat plains up to mountains
                 if (height < TribesWorld.NORMAL_WATER_LEVEL) {
                     c= Color.rgb((int) (237 + height), (int) (216 + height), (int) (151 + height));
                 } else {
                     c= Color.rgb((int) (255 - height * 4 - this.numFood * 2 < 0 ? 0 : 255 - height * 4 - this.numFood * 2), (int) (200 - height * 5 + (this.numFood) > 200 ? 200 : 200 - height * 5 + (this.numFood)), (int) (190 - height * 8 - this.numFood * 2 < 0 ? 0 : 190 - height * 8 - this.numFood * 2));
                 }
             } else {
                 // snow capped mountains
                 if (height <= 65) {
                     c= Color.rgb((int) (230 + (height - 40)), (int) (200 + (height * 2 - 80)), (int) (200 + (height * 2 - 80)));
                 } else {
                     c= Color.rgb(255, 255, 255);
                 }
             }
         } else {
             // Water
             float heightSub = (float) Math.max(height - world.waterLevel, -100);
 
             c= Color.rgb((int) (200 + heightSub * 2), (int) (200 + heightSub * 2), (int) (255 + heightSub));
         }
 	    
 	    if(debug){
 	    	float p = 1-proportion;
     		red =  (255f*world.villages().get(0).getDensityAt(this));
 			blue =  (255f*world.villages().get(1).getDensityAt(this));
 			green =  (255f*world.caves().get(0).getDensityAt(this));
 			//green = (255f*world.monsters().get(0).calculateScore(this));
 			
 			
 			return Color.rgb(  (int)Math.min(Math.max( (Color.red(c)*proportion+red*p)-1,0),255),(int)Math.min(Math.max( (Color.green(c)*proportion)-1+green*p,0),255),(int)Math.min(Math.max((Color.blue(c)*proportion+blue*p)-1,0),255));
 	    
 	    }
 	    return c;
     	
     }
 
    public boolean equals(Tile t){
    	return t.getXIndex()==getXIndex() && t.getYIndex() == getYIndex();
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
 
     @Override
     public void update(float delta) {
         for (Tile neighbor : neighbors()) {
             if (neighbor.height() < world.waterLevel) {
                 this.numFood += WATER_FOOD_INCREMENT * delta / (this.numFood + 1);
             } else if (neighbor.numFood > 0) {
                 this.numFood += FOOD_SPREAD_MULTIPLIER * neighbor.numFood * delta / (this.numFood + 1);
             }
         }
 
         // Snow caps on mountains don't have any food
         if (this.height > SNOW_PEAK_HEIGHT) {
             this.numFood = 0;
         }
     }
 
     public boolean isSafe(float minFood) {
         return height() > world.waterLevel - 4 && numFood >= minFood;
     }
 
     public int getXIndex() {
         return xIndex;
     }
 
     public int getYIndex() {
         return yIndex;
     }
     
     public static void changeProportion(float f){
     	proportion += f;
     	if(proportion<0){
     		proportion=0;
     	}
     	if(proportion>1){
     		proportion=1;
     	}
     }
 
     public List<Accessory> accessories() {
         return accessories;
     }
     
     interface Accessory {
         
     }
 }
