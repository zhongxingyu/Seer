 package com.twoseasgames.rivet.common;
 
 
 public class Rect {
 
   private int x = 0;
   private int y = 0;
   private int width = 0;
   private int height = 0;
   
   public Rect() {}
   
   public Rect(int x, int y, int width, int height) {
     this.x = x;
     this.y = y;
     this.width = width;
     this.height = height;
   }
   
   public Rect(Point pos, Size size) {
 	  this.x = pos.x();
 	  this.y = pos().y();
 	  this.width = size.width();
 	  this.height = size.height();
   }
   
   public boolean contains(Rect other) {
       return (this.x <= other.x()) 
         && (this.y <= other.y()) 
         && (this.x + this.width >= other.x() + other.width()) 
         && (this.y + this.height >= other.y() + other.height()) 
         && (this.x + this.width > other.x()) 
         && (this.y + this.height > other.y());
   }
   
   public boolean contains(Point point) {
     return (this.x <= point.x()) 
       && (this.y <= point.y()) 
       && (this.x + this.width > point.x()) 
       && (this.y + this.height > point.y()); 
   }
   
   static public boolean intersect(Rect biggest, Rect smallest) {
 	  boolean result = false;
 	  if (biggest.contains(smallest.topLeftCorner())) {
 		 result = true;
 	  } else if (biggest.contains(smallest.topRightCorner())) {
 		  result = true;
 	  } else if (biggest.contains(smallest.bottomLeftCorner())) {
 		  result = true;
 	  } else if (biggest.contains(smallest.bottomRightCorner())) {
 		  result = true;
 	  }
 	  return result;
   }
   
   public boolean intersectTop(Rect other) {
	  return (
			  	other.contains(topLeftCorner())
			  	&& other.contains(topRightCorner())
			 ) || (
				contains(other.bottomLeftCorner())
				&& contains(other.bottomRightCorner())
			 );
   }
 
   public boolean intersectBottom(Rect other) {
 	  return other.intersectTop(this);
   }
 
   public boolean intersectLeft(Rect other) {
	  return (
			  	other.contains(topLeftCorner())
			  	&& other.contains(bottomLeftCorner())
			 ) || (
				contains(other.topRightCorner())
				&& contains(other.bottomRightCorner())
			 );
   }
 
   public boolean intersectRight(Rect other) {
 	  return other.intersectLeft(this);
   }
 
   public boolean intersect(Rect other) {
 	  return intersect(this, other) || intersect(other, this);
   }
   
   public void reset(int x, int y, int width, int height) {
 	  this.x = x;
 	  this.y = y;
 	  this.width = width;
 	  this.height = height;
   }
   
   public int x() {
     return this.x;
   }
 
   public int y() {
     return this.y;
   }
 
   public int width() {
     return this.width;
   }
 
   public int height() {
     return this.height;
   }
 
   public void setX(int x) {
     this.x = x;
   }
 
   public int right() {
       return this.x + this.width - 1;
   }
 
   public int bottom() {
       return this.y + this.height - 1;
   }
   
   public void setY(int y) {
     this.y = y;
   }
 
   public Size size() {
       return new Size(this.width, this.height);
   }
 
   public Point pos() {
       return new Point(this.x, this.y);
   }
   
   public Point center() {
       return new Point(this.x + this.width / 2, this.y + this.height / 2);
   }
   
   public Point topLeftCorner() {
 	  return new Point(x, y);
   }
 
   public Point topRightCorner() {
 	  return new Point(right(), y);
   }
 
   public Point bottomLeftCorner() {
 	  return new Point(x, bottom());
   }
 
   public Point bottomRightCorner() {
 	  return new Point(right(), bottom());
   }
   
   public String toString() {
       return "(" + String.valueOf(this.x) + ", "
               + String.valueOf(this.y) + ", "
               + String.valueOf(this.width) + ", "
               + String.valueOf(this.height) + ")";
   }
   
   public Rect copy() {
       return new Rect(this.x, this.y, this.width, this.height);
   }
 }
