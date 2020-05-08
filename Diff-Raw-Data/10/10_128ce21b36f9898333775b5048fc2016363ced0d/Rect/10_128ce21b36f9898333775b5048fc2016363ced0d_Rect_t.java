 package com.spacemangames.math;
 
 public class Rect {
     public int left;
     public int top;
    public int right;
     public int bottom;
 
     public Rect() {
         this(0, 0, 0, 0);
     }
 
     public Rect(int left, int top, int right, int bottom) {
         set(left, top, right, bottom);
     }
 
     public void set(int left, int top, int right, int bottom) {
         this.left = left;
         this.top = top;
         this.right = right;
         this.bottom = bottom;
     }
 
     public void set(Rect other) {
         set(other.left, other.top, other.right, other.bottom);
     }
 
     public PointF center() {
         return new PointF(exactCenterX(), exactCenterY());
     }
 
     private int center(int small, int big) {
         return small + ((big - small) / 2);
     }
 
     private float exactCenter(int small, int big) {
         return (float) small + (((float) big - (float) small) / 2f);
     }
 
     public int centerX() {
         return center(left, right);
     }
 
     public int centerY() {
         return center(top, bottom);
     }
 
     public float exactCenterX() {
         return exactCenter(left, right);
     }
 
     public float exactCenterY() {
         return exactCenter(top, bottom);
     }
 
     public int height() {
         return bottom - top;
     }
 
     public int width() {
         return right - left;
     }
 
     public void offset(int x, int y) {
         left = left + x;
         right = right + x;
         top = top + y;
         bottom = bottom + y;
     }
 
     public void offset(PointF offset) {
         offset((int) offset.x, (int) offset.y);
     }
 
     public void scale(float scale) {
         float verticalZoom = scale * height();
         float horizontalZoom = scale * width();
         bottom += verticalZoom / 2.0f;
         top -= verticalZoom / 2.0f;
         left -= horizontalZoom / 2.0f;
         right += horizontalZoom / 2.0f;
     }
 
     public boolean isEmpty() {
         return left >= right || top >= bottom;
     }
 
     public static boolean intersects(Rect a, Rect b) {
         return (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom);
     }
 }
