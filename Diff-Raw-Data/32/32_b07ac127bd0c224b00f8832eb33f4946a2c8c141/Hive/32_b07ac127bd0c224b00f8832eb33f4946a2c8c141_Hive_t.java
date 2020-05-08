 package org.linesofcode.antfarm.sceneObjects;
 
 import org.linesofcode.antfarm.AntFarm;
 
 import processing.core.PVector;
 
 public class Hive implements SceneObject {
 
     public static float SIZE = 10;
     public static final float SPAWN_NOISE = 0.5f;
    public static final int INITIAL_FOOD_COUNT = 50;
 
     private long foodCount = INITIAL_FOOD_COUNT;
     private float spawnSpeed = 5f;
     private int ants = 0;
     private int antsAllive = 0;
 
     private PVector position;
     private int color;
     private int outlineColor = 0;
 
     private float lastSpawn;
 
     private final AntFarm antFarm;
 
     public Hive(final AntFarm antFarm, final int color) {
         this.antFarm = antFarm;
         this.color = color;
         position = antFarm.calcStaticSpawnPosition(this, SIZE);
         lastSpawn = Float.MAX_VALUE;
     }
 
     public void update(final float delta) {
         lastSpawn += delta;
         if (lastSpawn > spawnSpeed) {
         	if(foodCount > 0) {
         		antFarm.spawnAnt(this);
         		decreaseFood();
         		antsAllive++;
         		lastSpawn = - antFarm.random(1, SIZE * SPAWN_NOISE);
         	}
         }
     }
 
 	public void draw() {
         antFarm.stroke(outlineColor);
         antFarm.fill(color);
         antFarm.rect(position.x, position.y, SIZE, SIZE);
     }
 
     public int getColor() {
         return color;
     }
 
     public PVector getCenter() {
         return new PVector(position.x + SIZE / 2, position.y + SIZE /2);
     }
     
     public void putFood() {
     	increaseFood();
     }
     
     private void increaseFood() {
 		foodCount++;
 	}
 
 	private void decreaseFood() {
 		foodCount--;
 	}
     
     public void pickUpAnt() {
     	ants++;
     }
 
 	public PVector getSpawnPosition() {
 		final PVector pos = getCenter();
         final float dx = ((antFarm.random(-1, 1) < 0) ? -1 : 1 ) * antFarm.random(3, 10);
         final float dy = ((antFarm.random(-1, 1) < 0) ? -1 : 1 ) * antFarm.random(3, 10);
         pos.add(dx, dy, 0);
         return pos;
     }
 
     public PVector getPosition() {
         return position;
     }
 
 	public void decreaseAnts() {
 		antsAllive--;
 		if(antsAllive == 0 && foodCount == 0) {
 			antFarm.removeHive(this);
 		}
 	}
 }
