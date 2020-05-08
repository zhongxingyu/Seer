 package com.xmedic.troll.components;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
import android.util.DisplayMetrics;
 import android.util.Log;
import android.view.Display;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 
 public class ScrollableImageView extends ImageView {
 
 	float downX, downY;
     int totalX, totalY;
     int scrollByX, scrollByY;
     int maxLeft;
     int maxRight;
     int maxTop;
     int maxBottom;
     Paint paint;
 
     public ScrollableImageView(Context context, AttributeSet set) {
     	super(context, set);
 		 paint = new Paint();
     }
     
     
     
 	public ScrollableImageView(Context context) {
 		super(context);
 		 paint = new Paint();
 		 paint.setColor(Color.BLACK);
 
 	}
 	
 	@Override
 	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 		super.onScrollChanged(l, t, oldl, oldt);
 		Log.d("TAG", "Scrolled to l" + l + " Scrolled to l" + t);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		super.onDraw(canvas);
 		Log.d("TAG", "onDraw");
 		 canvas.drawLine(0, 0, 200, 200, paint);
          canvas.drawLine(200, 0, 0, 200, paint);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		 float currentX = 0, currentY = 0;
 		 
          switch (event.getAction())
          {
              case MotionEvent.ACTION_DOWN:
                  downX = event.getX();
                  downY = event.getY();
                  break;
 
              case MotionEvent.ACTION_MOVE:
                  currentX = event.getX();
                  currentY = event.getY();
                  scrollByX = (int)(downX - currentX);
                  scrollByY = (int)(downY - currentY);
 
                  // scrolling to left side of image (pic moving to the right)
                  if (currentX > downX)
                  {
                      if (totalX == maxLeft)
                      {
                          scrollByX = 0;
                      }
                      if (totalX > maxLeft)
                      {
                          totalX = totalX + scrollByX;
                      }
                      if (totalX < maxLeft)
                      {
                          scrollByX = maxLeft - (totalX - scrollByX);
                          totalX = maxLeft;
                      }
                  }
 
                  // scrolling to right side of image (pic moving to the left)
                  if (currentX < downX)
                  {
                      if (totalX == maxRight)
                      {
                          scrollByX = 0;
                      }
                      if (totalX < maxRight)
                      {
                          totalX = totalX + scrollByX;
                      }
                      if (totalX > maxRight)
                      {
                          scrollByX = maxRight - (totalX - scrollByX);
                          totalX = maxRight;
                      }
                  }
 
                  // scrolling to top of image (pic moving to the bottom)
                  if (currentY > downY)
                  {
                      if (totalY == maxTop)
                      {
                          scrollByY = 0;
                      }
                      if (totalY > maxTop)
                      {
                          totalY = totalY + scrollByY;
                      }
                      if (totalY < maxTop)
                      {
                          scrollByY = maxTop - (totalY - scrollByY);
                          totalY = maxTop;
                      }
                  }
 
                  // scrolling to bottom of image (pic moving to the top)
                  if (currentY < downY)
                  {
                      if (totalY == maxBottom)
                      {
                          scrollByY = 0;
                      }
                      if (totalY < maxBottom)
                      {
                          totalY = totalY + scrollByY;
                      }
                      if (totalY > maxBottom)
                      {
                          scrollByY = maxBottom - (totalY - scrollByY);
                          totalY = maxBottom;
                      }
                  }
 
                  scrollBy(scrollByX, scrollByY);
                  downX = currentX;
                  downY = currentY;
                  break;
 
                  
          }
        
          Log.d("Scroll", "CurrentX " + currentX + " CurrentY " + currentY);
          Log.d("Scroll", "TotalX " + totalX + " TotalY " + totalY);
          return true;
 	}
 
 	public void setCurrent(int x, int y) {
 		totalX = x;
 		totalY = y;
 		
 	}
 

 	public void setScreenSize(int width, int height) {
 		int maxX = (int) ((2400 / 2) - (width / 2));
 		int maxY = (int) ((1600 / 2) - (height / 2));
 
 		// set scroll limits
 		maxLeft = (maxX * -1);
 		maxRight = maxX;
 		maxTop = (maxY * -1);
 		maxBottom = maxY;
 	}
 }
