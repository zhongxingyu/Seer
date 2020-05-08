 package org.linesofcode.antfarm.sceneObjects;
 
 import org.linesofcode.antfarm.AntFarm;
 import processing.core.PVector;
 
 import java.awt.Color;
 
 public class Food implements SceneObject {
 	
 	public static int MAX_COUNT = 20;
 	public static int SIZE = 8;
 	public static int OUTLINE_COLOR = new Color(0x0, 0x55, 0x0).getRGB();
 	public static int COLOR = new Color(0x99, 0xff, 0x99).getRGB();
 
     private int count;
     private PVector position;
     private final AntFarm antFarm;
 
     public Food(final AntFarm antFarm) {
         this.antFarm = antFarm;
         count = (int) antFarm.random(MAX_COUNT * 0.1f, MAX_COUNT);
         position = antFarm.calcStaticSpawnPosition(this, 20);
     }
 
     public void draw() {
     	antFarm.stroke(OUTLINE_COLOR);
         antFarm.fill(COLOR);
         antFarm.strokeWeight(1f);
         
        for(int i=count; i>0; i--) {
         	float dx = (float)Math.sin(i*10);
         	float dy = (float)Math.cos(i*10);
         	antFarm.ellipse(position.x + dx*(float)Math.log(i*15*SIZE), position.y + dy*(float)Math.log(i*15*SIZE), SIZE, SIZE);
         }
     }
 
     @Override
     public void update(final float delta) {
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
 
     public PVector getPosition() {
         return position;
     }
 }
