 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.first.tribes.core;
 
 import com.first.tribes.core.Tile.Accessory;
 import java.util.Arrays;
 import java.util.HashSet;
 import playn.core.Color;
 import playn.core.Surface;
 import pythagoras.f.Dimension;
 import pythagoras.f.Rectangle;
 
 /**
  *
  * @author taylor
  */
 public class IrrigationPipe extends DrawnObject implements Accessory {
 
     private float xPos;
     private float yPos;
     private final Dimension IRRIGATION_PIPE_SIZE = new Dimension(40, 40);
     public static final int HIT_RADIUS = 4;
    public static final float FOOD_CHANGE = 0.007f;
     public static final float MAX_AGE = 60000;
     private TribesWorld world;
     private float age;
     private Tile myTile;
 
     public IrrigationPipe(float x, float y, TribesWorld world) {
         this.world = world;
         this.xPos = x - IRRIGATION_PIPE_SIZE.width / 2f;
         this.yPos = y - IRRIGATION_PIPE_SIZE.height / 2f;
         myTile = world.tileAt(bounds().centerX(), bounds().centerY());
     }
 
     @Override
     protected Rectangle bounds() {
         return new Rectangle(xPos, yPos, IRRIGATION_PIPE_SIZE.width, IRRIGATION_PIPE_SIZE.height);
     }
 
     @Override
     public void paintToRect(Rectangle rect, Surface surface) {
         surface.setFillColor(Color.rgb((int) (age / (MAX_AGE / 150)), (int) (age / (MAX_AGE / 100)), 0));
         surface.fillRect(rect.x, rect.y, rect.width, rect.height);
     }
 
     @Override
     public void update(float delta) {
         age += delta;
         if (age >= MAX_AGE) {
             world.removeExtraDrawnObject(this);
             myTile.accessories().remove(this);
             return;
         }
 
         HashSet<Tile> tiles = new HashSet<Tile>();
         tiles.add(myTile);
         for (int i = 0; i < HIT_RADIUS; i++) {
             HashSet<Tile> newTiles = new HashSet<Tile>();
             for (Tile tile : tiles) {
                 newTiles.addAll(Arrays.asList(tile.neighbors()));
                 tile.numFood += FOOD_CHANGE * (((MAX_AGE - age) / MAX_AGE) / 2.0 + 0.5);
             }
             tiles.addAll(newTiles);
         }
     }
 
     public void addToWorld() {
         world.addExtraDrawnObject(this);
         myTile.accessories().add(this);
     }
 }
