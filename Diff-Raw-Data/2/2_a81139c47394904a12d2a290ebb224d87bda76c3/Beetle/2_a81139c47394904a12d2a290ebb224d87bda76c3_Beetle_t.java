 package com.servegame.abendstern.tunnelblick.game;
 
 public class Beetle extends Enemy {
   private static final float DIM = 0.1f;
   private static final float[] MODEL = {
     1, 0, 0, 1,
 
     0, -DIM, 0, -DIM,
     0, +DIM, 0, -DIM,
     1, 0, 0.5f, 1,
     0, 0, DIM, 0,
 
     0, 0, DIM, 0,
     1, 0, 0, 1,
     0, -DIM, 0, -DIM,
     0, -DIM, 0, +DIM,
 
     0, -DIM, 0, +DIM,
     0, +DIM, 0, +DIM,
     1, 0, 0.5f, 1,
     0, 0, DIM, 0,
 
     0, 0, DIM, 0,
     1, 0, 0, 1,
     0, +DIM, 0, -DIM,
     0, +DIM, 0, +DIM,
   };
 
  static { normalise(MODEL); }

   private static final float SPEED = 0.5f;
   private float vx;
 
   public Beetle(Tunnelblick tb) {
     super(tb, MODEL, 3);
 
     if (Math.random() < 0.5f)
       vx = SPEED;
     else
       vx = -SPEED;
   }
 
   public void update(float et) {
     boolean moved = moveTo(x + vx*et, y, z, false);
     if (x-w/2 == 0 && vx < 0) moved = false;
     if (x+w/2 == 1 && vx > 0) moved = false;
     if (!moved)
       vx = -vx;
   }
 
   protected float getColourR() { return 0; }
   protected float getColourG() { return 0; }
   protected float getColourB() { return 1; }
   protected float getPulseSpeed() { return 18; }
   protected int getAward() { return 500; }
 }
