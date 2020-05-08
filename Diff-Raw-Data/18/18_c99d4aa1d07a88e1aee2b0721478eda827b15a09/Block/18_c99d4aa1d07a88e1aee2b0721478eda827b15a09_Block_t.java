 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.me.qpad;
 
 /**
  *
  * @author burke
  */
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 
 public class Block {
 
     private int xPos;
     private int yPos;
     private int width;
     private int height;
     private int blockColor;
     private Paint paint;
 
     public Block(int x, int y)
     {
             paint = new Paint();
             
             blockColor = Color.BLUE;
 
             xPos = x;
             yPos = y;
            width = 12;
            height = 12;
     }
 
     public int getWidth()
     {
         return width;
     }
 
     public int getHeight()
     {
         return height;
     }
 
     public void draw(Canvas c)
     {
         paint.setColor(blockColor);
         c.drawRect(this.getRect(), paint);
     }
 
     public RectF getRect()
     {
         return new RectF(xPos, yPos, xPos + width, yPos + height);
     }
 
 }
