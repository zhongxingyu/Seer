 package com.macbury.unamed.level;
 
 import java.util.Random;
 
 import org.newdawn.slick.Color;
 
 import com.macbury.unamed.PerlinGen;
 
 public class WorldBuilder {
   public float world[][];
   public int size;
   private Random rand_;
   
   public WorldBuilder(int size) {
     this.rand_ = new Random();
     this.size = size;
     
     PerlinGen pg = new PerlinGen(0, 0);
     this.world   = pg.generate(size, 8, 30);
     smoothResources();
   }
   
   private void smoothResources() {
     float resourceCount = 1.0f / 5.0f;
     
     for (int x = 0; x < this.size; x++) {
       for (int y = 0; y < this.size; y++) {
         float val        = this.world[x][y];
        this.world[x][y] = Math.round(Math.round(100.0f * val) / resourceCount) ;
       }
     }
   }
 }
