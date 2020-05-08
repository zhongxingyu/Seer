 package org.linesofcode.antfarm.sceneObjects;
 
 import java.awt.Color;
 
 import org.linesofcode.antfarm.AntFarm;
 
 import processing.core.PVector;
 
 public class Food implements SceneObject {
 	
 	private static final int MAX_COUNT = 1000;
 	private static final int SIZE = 10; 
 
     private int count;
     
     private PVector position;
 
     private final AntFarm antFarm;
 
 	private final int outlineColor = Color.BLACK.getRGB();
 	private final int color = Color.GREEN.getRGB();
 
     public Food(final AntFarm antFarm) {
         this.antFarm = antFarm;
         count = (int) antFarm.random(MAX_COUNT * 0.90f, MAX_COUNT);
     }
 
     public void draw() {
     	antFarm.stroke(outlineColor);
         antFarm.fill(color);
         // make the size of the food source shrink while it depletes
         final float relativeSize = ((count * 100) / MAX_COUNT) * SIZE;
         antFarm.rect(position.x, position.y, relativeSize, relativeSize);
     }
 
     @Override
     public void update(final float delta) {
     }
 
     @Override
     public BoundingBox getBoundingBox() {
         throw new UnsupportedOperationException();
     }
 
     public void pickUp() {
         count--;
         if (count == 0) {
             deplete();
         }
     }
 
     public void deplete() {
     	antFarm.removeFood(this);
     }
 }
