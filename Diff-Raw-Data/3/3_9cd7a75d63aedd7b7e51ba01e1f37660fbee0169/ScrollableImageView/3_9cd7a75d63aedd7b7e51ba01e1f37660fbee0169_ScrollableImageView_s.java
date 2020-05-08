 package com.xmedic.troll.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
import android.R;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.text.TextPaint;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 
 import com.xmedic.troll.service.MapMath;
 import com.xmedic.troll.service.MapMath.MapType;
 import com.xmedic.troll.service.model.City;
 
 public class ScrollableImageView extends ImageView {
 
 	float downX, downY;
     int totalX, totalY;
     int scrollByX, scrollByY;
     int maxLeft;
     int maxRight;
     int maxTop;
     int maxBottom;
     
     int maxX;
     int maxY;
     
     Paint blackPaint;
     Paint labelPaint;
     Paint bluePaint;
     Paint linePaint;
     Paint goalPaint;
 	Paint initialCityPaint;
     
     int canvasOffsetX;
     int canvasOffsetY;
 
     private City city;
     private City goalCity;
 	private City initialCity;
 	
     private List<City> nearestCities;
 	private int screenHeight;
 	private int screenWidth;
 	
 	List<Point> history = new ArrayList<Point>();
 	
 	final Handler h = new Handler();
     
     public ScrollableImageView(Context context, AttributeSet set) {
     	super(context, set);
     	setupLines();
     }
     
     
     
 	public ScrollableImageView(Context context) {
 		super(context);
 		setupLines();
 	}
 	
 	private void setupLines() {
 		 blackPaint = new Paint();
 		 blackPaint.setColor(Color.BLACK);
 		 blackPaint.setTextSize(30);
 
 
 		 labelPaint = new TextPaint();
 		 labelPaint.setColor(Color.BLACK);
 		 labelPaint.setTextSize(16);
 		 labelPaint.setAntiAlias(true);
 		 labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
 		 
 		 bluePaint = new Paint();
 		 bluePaint.setColor(Color.BLUE);
 		 
 		 linePaint = new Paint();
 		 linePaint.setColor(Color.GRAY);
 		 linePaint.setStrokeWidth(5);
 		 linePaint.setAntiAlias(true);
 		 linePaint.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));
 		 
 		 goalPaint = new Paint();
 		 goalPaint.setColor(Color.RED);
 		 goalPaint.setStrokeWidth(5);
 
 		 initialCityPaint  = new Paint();
 		 initialCityPaint.setColor(Color.CYAN);
 		 initialCityPaint.setStrokeWidth(5);
 	}
 
 
 
 	@Override
 	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 		super.onScrollChanged(l, t, oldl, oldt);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		super.onDraw(canvas);
 		
 		if(history.size() > 1) {
 			Point previous = null;
 			for(Point point : history) {
 				if(previous != null) {
 					canvas.drawLine(previous.x, previous.y, point.x, point.y, linePaint);
 				}
 				previous = point;
 			}
 		}
 
 		if(city != null) {
 			Point coordinates = MapMath.toDrawPoint(city.getPoint(), maxX, maxY);
 
 			canvas.drawText(city.getName(), coordinates.x - 20, coordinates.y - 10, labelPaint);
 			canvas.drawBitmap(
 					BitmapFactory.decodeResource(getResources(), R.drawable.active_pin), 
 					coordinates.x - 15, 
 					coordinates.y - 12, 
 					labelPaint);
 
 		}
 		if(nearestCities != null) {
 			for(City nearestCity : nearestCities) {
 				Point coordinates = MapMath.toDrawPoint(nearestCity.getPoint(), maxX, maxY);
 				canvas.drawBitmap(
 						BitmapFactory.decodeResource(getResources(), R.drawable.normal_pin), 
 						coordinates.x - 15, 
 						coordinates.y - 12, 
 						labelPaint);
 			}
 		}
 		if(goalCity != null) {
 			Point coordinates = MapMath.toDrawPoint(goalCity.getPoint(), maxX, maxY);
 			canvas.drawBitmap(
 					BitmapFactory.decodeResource(getResources(), R.drawable.finish_ping), 
 					coordinates.x - 15, 
 					coordinates.y - 12, 
 					labelPaint);
 		}
 		if(goalCity != null) {
 			Point coordinates = MapMath.toDrawPoint(initialCity.getPoint(), maxX, maxY);
 			canvas.drawBitmap(
 					BitmapFactory.decodeResource(getResources(), R.drawable.start_pin), 
 					coordinates.x - 15, 
 					coordinates.y - 12, 
 					labelPaint);
 		}
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
          return true;
 	}
 
 	public void setCurrent(int x, int y) {
 		totalX = x;
 		totalY = y;
 		
 	}
 
 	public void setScreenSize(int width, int height) {
 		this.screenWidth = width;
 		this.screenHeight = height;
 		maxX = (int) ((MapMath.MAP_WIDTH / 2) - (width / 2));
 		maxY = (int) ((MapMath.MAP_HEIGHT / 2) - (height / 2));
 
 		// set scroll limits
 		maxLeft = (maxX * -1);
 		maxRight = maxX;
 		maxTop = (maxY * -1);
 		maxBottom = maxY;
 		
 		
 	}
 
 	public void moveTo(final Point point, Activity activity) {
 		//scrollTo(point.x, point.y);
 		
         final int yCoeficient = (getScrollY() > point.y) ? -1 : 1;
         final int xCoeficient = (getScrollX() > point.x) ? -1 : 1;
         Thread t = new Thread(){
             public void run(){
                 int y = getScrollY();
                 int x = getScrollX();
                 while(y != point.y || x != point.x){
                     final int X = x;
                     final int Y = y;
                     h.post(new Runnable() {
                         public void run() {
                             scrollTo(X, Y);
                         }
                     });
                     if(y != point.y) {
                     y = y + yCoeficient;
                     }
                     if(x != point.x) {
                     x = x + xCoeficient;
                     }
                     try {
                         sleep(1000/350);
                     } catch (InterruptedException e) {
                     }
                 }
             }
         };
         t.start();
 		
 		setCurrent(point.x, point.y);
 	}
 
 	public void setCenter(City city, Activity activity) {
 		history.add(MapMath.toDrawPoint(city.getPoint(), maxX, maxY));
 
 		moveTo(MapMath.toScrollPoint(city.getPoint(), MapType.NORMAL), activity);
 
 		if(this.city == null) {
 			this.initialCity = city;
 		}
 		
 		this.city = city;
 	}
 
 	public void setNearest(List<City> nearestCities) {
 		this.nearestCities = nearestCities;
 	}
 
 	public void setGoalCity(City goalCity) {
 		this.goalCity = goalCity;
 	}
 }
