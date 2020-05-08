 package jumpignon;
 
 import org.newdawn.slick.*;
 
 public class Rectangle extends RenderItem{
     private Image image;
     private int height;
     private int width;
     private int rotation;
    
    //
 //rotation muss nochmal gedacht werden ;) wegen collisoin
 public Rectangle(int x, int y, int height ,int  width ,int rotation ){
     this.pos_x = x;
     this.pos_y = y;
     this.height = height;
     this.width = width;
     this.rotation = rotation;
 }
 }
