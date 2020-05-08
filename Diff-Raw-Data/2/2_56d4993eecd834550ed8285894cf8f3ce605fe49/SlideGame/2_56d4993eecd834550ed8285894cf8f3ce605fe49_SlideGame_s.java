 package com.jdrago.slide;
 
 import android.content.Context;
 
 import javax.microedition.khronos.opengles.GL10;
 
 public class SlideGame extends QuadRenderer
 {
     private final float[][] COLORS = {
     { 0.5f,    0, 0.5f, 1.0f },
     {    0, 0.5f, 0.5f, 1.0f },
     { 0.5f, 0.5f,    0, 1.0f },
     };
     private static final int COLOR_COUNT = 3;
 
     public SlideGame(Context context)
     {
         super(context);
 
         grid_ = new int[100]; // I AM THE LAZIEST PROGRAMMER EVER
         shuffle(4);
     }
 
     public boolean isSolved()
     {
         if(grid_[(size_ * size_) - 1] != 0)
         {
             return false;
         }
 
         for(int i = 0; i < ((size_ * size_) - 1); i++)
         {
             if(grid_[i] != (i+1))
             {
                 return false;
             }
         }
         return true;
     }
 
     public void update()
     {
         if(!solved_)
         {
             solved_ = isSolved();
             if(solved_)
             {
                 endTime_ = System.nanoTime();
             }
         }
     }
 
     public void onDrawFrame(GL10 glUnused)
     {
         update();
 
         int smallestDim = Math.min(width(), height());
         boxDim_ = (smallestDim - (WINDOW_MARGIN * 2) - (BOX_SPACING * (size_ - 1))) / size_;
         timerHeight_ = height() / 10;
 
         renderBegin(0.1f, 0.1f, 0.1f);
 
         // render boxes
         int boxCount = size_ * size_;
         for(int i = 0; i < boxCount; i++)
         {
             int value = grid_[i];
             if(value > 0)
             {
                 int x = i % size_;
                 int y = i / size_;
                 int pixelX = WINDOW_MARGIN + (x * boxDim_) + (x * BOX_SPACING);
                 int pixelY = WINDOW_MARGIN + (y * boxDim_) + (y * BOX_SPACING);
                int color = i % COLOR_COUNT;
                 float r = COLORS[color][0];
                 float g = COLORS[color][1];
                 float b = COLORS[color][2];
                 float a = COLORS[color][3];
 
                 renderQuad(pixelX, pixelY, boxDim_, boxDim_, r, g, b, a);
                 renderInt(value, pixelX + (boxDim_ / 2), pixelY + (boxDim_ / 4), 0, boxDim_ / 2, 1, 1, 1, 1);
             }
         }
 
         // render timer
         long end = endTime_;
         float tr = 0.0f;
         float tg = 1.0f;
         float tb = 0.0f;
         if(end == 0)
         {
             end = System.nanoTime();
             tr = 1.0f;
             tg = 0.0f;
             tb = 0.0f;
         }
         float t = (end - startTime_) / 1000000000.0f;
         renderFloat(t, width(), height() - timerHeight_, 0, timerHeight_, tr, tg, tb, 1);
         renderEnd();
     }
 
     public void click(int x, int y)
     {
         if(!solved_)
         {
             x -= WINDOW_MARGIN;
             y -= WINDOW_MARGIN;
             x /= boxDim_;
             y /= boxDim_;
             moveBlankTo(x, y);
         }
     }
 
     int sign(int x)
     {
         if(x < 0)
         {
             return -1;
         }
         return 1;
     }
 
     void gridSwap(int x1, int y1, int x2, int y2)
     {
         int i1 = x1 + (y1 * size_);
         int i2 = x2 + (y2 * size_);
         int temp = grid_[i1];
         grid_[i1] = grid_[i2];
         grid_[i2] = temp;
     }
 
     void moveBlankTo(int x, int y)
     {
         int dx = blankX_ - x;
         int dy = blankY_ - y;
         int dirX = sign(dx) * -1;
         int dirY = sign(dy) * -1;
         int distX = Math.abs(dx);
         int distY = Math.abs(dy);
 
         for(int i = 0; i < distX; i++)
         {
             int newX = blankX_ + dirX;
             if((newX >= 0) && (newX < size_))
             {
                 gridSwap(blankX_, blankY_, newX, blankY_);
                 blankX_ = newX;
             }
         }
 
         for(int i = 0; i < distY; i++)
         {
             int newY = blankY_ + dirY;
             if((newY >= 0) && (newY < size_))
             {
                 gridSwap(blankX_, blankY_, blankX_, newY);
                 blankY_ = newY;
             }
         }
     }
 
     public void moveRandomly()
     {
         int dx = -1;
         if(Math.random() > 0.5)
         {
             dx = 1;
         }
         int dy = -1;
         if(Math.random() > 0.5)
         {
             dy = 1;
         }
         moveBlankTo(blankX_ + dx, blankY_ + dy);
     }
 
     public void shuffle(int size)
     {
         if(size > 0)
         {
             size_ = size;
         }
 
         for(int i = 0; i < size_ * size_; i++)
         {
             grid_[i] = i + 1;
         }
         grid_[(size_ * size_) - 1] = 0;
 
         blankX_ = size_ - 1;
         blankY_ = size_ - 1;
 
         for(int i = 0; i < 100 * size_; i++)
         {
             moveRandomly();
         }
 
         solved_ = false;
         startTime_ = System.nanoTime();
         endTime_ = 0;
     }
 
     private boolean solved_;
     private int size_;
     private int blankX_;
     private int blankY_;
     private int boxDim_;
     private int timerHeight_;
     private long startTime_;
     private long endTime_;
     private int[] grid_;
 
     private static final int WINDOW_MARGIN = 10;
     private static final int BOX_SPACING = 4;
 }
