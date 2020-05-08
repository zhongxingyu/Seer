 package com.deeep.sod2.graphics;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.deeep.sod2.utility.Logger;
 
 import java.util.Random;
 
 /**
  * Name: Particle
  * Pack: com.deeep.sod2.graphics
  * User: andreaskruhlmann
  * Date: 10/1/13
  */
 
 public class Particle {
 
     /** Red green blue color*/
     private float r = 255.f, g = 255.f, b = 255.f;
     private float width, height;
 
     /** Ticks before ceasing to exist*/
     public double lifespan;
 
     /** Location vector (x,y)*/
     public PVector location;
 
     /** Velocity to add to position vector*/
     public PVector velocity;
 
     /** Velocity to add to velocity vector*/
     public PVector acceleration;
 
     private Random random = new Random();
 
     public Particle(PVector v, float lifetime, float w, float h){
         location = v.get();
         acceleration = new PVector(0.0F,0.0F);
         velocity = new PVector(0.0F,0.0F);
         lifespan = lifetime;
         width = w;
         height = h;
     }
 
     /** Constructor when specified a particle color */
     public Particle(PVector v, Color c, float lifetime, float w, float h){
         location = v.get();
         acceleration = new PVector(0.0F,0.00F);
         velocity = new PVector(0.0F,0.0F);
         lifespan = lifetime;
         width = w;
         height = h;
         setColor(c);
     }
 
     public void update(float delta){
         location.add(velocity);
         velocity.add(new PVector(acceleration.x*delta, acceleration.y*delta));
         if(lifespan!=-1)lifespan -= delta;
     }
 
     public void draw(SpriteBatch graphics){
        if(lifespan<0 && lifespan!=-1f) return;
         double t = lifespan;
         if(lifespan!=-1) t=255;
         ShapeRenderer.setColor(new Color(r, g, b, (float)t));
         ShapeRenderer.drawRectangle(graphics, location.x, location.y, width, height, true);
     }
 
     public boolean isDead(){
         return lifespan<0.0 && lifespan!=-1 ? true : false;
     }
 
     public void setHeight(float h){
         height = h;
     }
 
     public void setWidth(float w){
         width = w;
     }
 
     public float getHeight(){
         return height;
     }
 
     public float getWidth(){
         return width;
     }
 
     public Color getColor(){
         return new Color(r, g, b, lifespan<0.0 ? 255 : (float)lifespan);
     }
 
     public void setColor(Color c) {
         r = c.r;
         g = c.g;
         b = c.b;
     }
 
 }
