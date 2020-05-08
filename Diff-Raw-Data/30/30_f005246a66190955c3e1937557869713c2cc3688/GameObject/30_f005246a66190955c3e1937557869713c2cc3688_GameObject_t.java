package edu.mines.csci598.recycler.entejagd;
 
 public abstract class GameObject {
     protected final GameField field;
     protected float x, y;
     protected boolean alive = true;
 
     protected GameObject(GameField field, float x, float y) {
         this.field = field;
         this.x = x;
         this.y = y;
     }
 
     public final float getX() { return x; }
     public final float getY() { return y; }
     public final boolean isAlive() { return alive; }
 
     public abstract void update(float et);
     public abstract void draw(java.awt.Graphics2D g);
     public void shoot(float x, float y) {}
 }
