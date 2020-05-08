 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package math;
 
 public class Rectangle {
   protected final Vector2 min, max, center;
   protected final Vector2 size;
 
  public Rectangle(Rectangle rect) {
     this.min    = new Vector2(rect.min);
     this.max    = new Vector2(rect.max);
     this.center = new Vector2(rect.center);
     this.size   = new Vector2(rect.size);
   }
 
  public Rectangle(float x1, float y1, float width, float height) {
     float x2 = x1 + width;
     float y2 = y1 + height;
 
     min    = new Vector2(x1, y1);
     max    = new Vector2(x2, y2);
     size   = new Vector2(width, height);
     center = min.add(size.divide(2.0f));
   }
 
   public Rectangle(Vector2 upperLeft, Vector2 bottomRight) {
     min  = new Vector2(upperLeft);
     max  = new Vector2(bottomRight);
     size = new Vector2(bottomRight.x - upperLeft.x,
                        bottomRight.y - upperLeft.x);
     center = min.add(size.divide(2.0f));
 
   }
 
   public boolean isIntersecting(Rectangle other) {
     return !((min.x > other.max.x) || (min.y > other.max.y) ||
              (max.x < other.min.x) || (max.y < other.min.y));
   }
 
   public boolean isContaining(Rectangle other) {
     return ((other.min.x > min.x && other.max.x < max.x) &&
             (other.min.y > min.y && other.max.y < max.y));
   }
 
   public void setPosition(Vector2 v) {
     min.set(v);
     max.set(v.add(size));
   }
 
   public void addPosition(Vector2 v) {
     min.addSelf(v);
     max.addSelf(v);
   }
 
   /**
    * Top left corner of the rectangle.
    * @return a vector2
    */
   public Vector2 getMin() {
     return new Vector2(min);
   }
 
   /**
    * Bottom right corner of the rectangle.
    * @return a vector2
    */
   public Vector2 getMax() {
     return new Vector2(max);
   }
 
   public Vector2 getCenter() {
     return new Vector2(center);
   }
 
   public Vector2 getSize() {
     return new Vector2(size);
   }
 
   public float getWidth() {
     return size.x;
   }
 
   public float getHeight() {
     return size.y;
   }
 
   public float getX1() {
     return min.x;
   }
 
   public float getY1() {
     return min.y;
   }
 
   public float getX2() {
     return max.x;
   }
 
   public float getY2() {
     return max.y;
   }
 }
