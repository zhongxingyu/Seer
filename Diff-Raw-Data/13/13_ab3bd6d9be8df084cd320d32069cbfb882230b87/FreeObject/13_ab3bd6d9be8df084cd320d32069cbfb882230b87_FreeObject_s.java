 package com.hack.elements.model;
 
 import org.newdawn.slick.Image;
 
 import com.hack.elements.model.impl.Direction;
 
 public interface FreeObject {
   public float getX();
   public void setX(float x);
 
   public float getY();
   public void setY(float y);
 
   public int getWidth();
   public void setWidth(int width);
 
   public int getHeight();
   public void setHeight(int height);
 
   public boolean usesDirection();
   public Direction getFacing();
   public void setFacing(Direction d);
   
   public Image getImage();
   public void setImage(Image image);
}
