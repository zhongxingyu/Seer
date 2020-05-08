 package com.cateye.ui.android;
 
 import java.util.ArrayList;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.MotionEvent.PointerCoords;
 import android.view.View;
 
 import com.cateye.core.IPreciseBitmap;
 import com.cateye.core.PointD;
 import com.cateye.core.RestrictedImageCoordinatesTransformer;
 
 public class PreciseBitmapView extends View
 {
 	private IPreciseBitmap preciseBitmap;
 	private RestrictedImageCoordinatesTransformer imageTransformer = new RestrictedImageCoordinatesTransformer();
     private PointF fingerStartPosition = new PointF(0, 0);
     private ArrayList<PointF> oldFingers = new ArrayList<PointF>();
     private ArrayList<PointF> currentFingers = new ArrayList<PointF>();
     private PreciseBitmapViewCache[] cache = new PreciseBitmapViewCache[] 
     {
     	new PreciseBitmapViewCache(4, imageTransformer),
     	new PreciseBitmapViewCache(1, imageTransformer)
     };
     private static final int LQ = 0; 
     private static final int HQ = 1; 
 
     private volatile int activeImageIndex = LQ;
 	
 	private Thread polishingDrawingThread = null;
 	
 	private final Runnable polishingDrawingRunnable = new Runnable()
 	{
		@Override
 		public void run() 
 		{
 			if (cache[HQ].update())
 			{
 				Log.i("PreciseBitmapView", "polished");
 	        	activeImageIndex = HQ;
 		        PreciseBitmapView.this.postInvalidate();
 	        }
 		}
 	};
 	
 	void changeFingersCount(ArrayList<PointF> newFingers)
 	{
 		Log.i("PreciseBitmapView", "There were " + oldFingers.size() + " fingers, but now there are " + newFingers.size());
 		if (newFingers.size() > 0)
 		{
 			float oldX = 0, oldY = 0;
 			if (oldFingers.size() > 0)
 			{			
 				for (int i = 0; i < oldFingers.size(); i++)
 				{
 					oldX += oldFingers.get(i).x;
 					oldY += oldFingers.get(i).y;
 				}
 				oldX /= oldFingers.size();
 				oldY /= oldFingers.size();
 			}
 			
 			float newX = 0, newY = 0;
 			for (int i = 0; i < newFingers.size(); i++)
 			{
 				newX += newFingers.get(i).x;
 				newY += newFingers.get(i).y;
 			}
 			newX /= newFingers.size();
 			newY /= newFingers.size();
 			
 			if (oldFingers.size() > 0)
 			{			
 				float dX = newX - oldX;
 				float dY = newX - oldY;
 				fingerStartPosition = new PointF(fingerStartPosition.x + dX,
 				                                 fingerStartPosition.y + dY);
 			}
 			else
 			{
 				fingerStartPosition = new PointF(newX, newY);
 			}
 		}
 	}
 	
 	ArrayList<PointF> extractFingers(MotionEvent event)
 	{
     	ArrayList<PointF> curFingers = new ArrayList<PointF>();
     	for (int i = 0; i < event.getPointerCount(); i++)
     	{
     		PointerCoords pc = new PointerCoords();
     		event.getPointerCoords(i, pc);
     		
     		if ((event.getActionMasked() != MotionEvent.ACTION_UP &&
    		        event.getActionMasked() != MotionEvent.ACTION_POINTER_UP) ||
    		        event.getActionIndex() != i)		// to remove fingers which are not here already
     		{
     			curFingers.add(new PointF(pc.x, pc.y));
     		}
     	}
     	return curFingers;
 	}
 	
 	PointD getCenter(ArrayList<PointF> points)
 	{
     	float cX = 0, cY = 0;
     	for (int i = 0; i < points.size(); i++)
     	{
     		cX += points.get(i).x; 
     		cY += points.get(i).y;
     	}
     	cX /= points.size();
     	cY /= points.size();
     	return new PointD(cX, cY);
 	}
 	
 	float getDispersion(ArrayList<PointF> points)
 	{
 		PointD center = getCenter(points);
 		
     	float d = 0;
     	for (int i = 0; i < points.size(); i++)
     	{
     		double dx = points.get(i).x - center.getX(); 
     		double dy = points.get(i).y - center.getY();
     		d += Math.sqrt(dx * dx + dy * dy);	// distance
     	}
     	d /= points.size();		// average distance
     	
 		return d;
 	}
 	
 	public PreciseBitmapView(Context context, AttributeSet attrs, int defStyle)
 	{
 		super(context, attrs, defStyle);
 	}
 
 	public PreciseBitmapView(Context context)
 	{
 		super(context);
 	}
 
 	public PreciseBitmapView(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 	}
 
 
 	public void setPreciseBitmap(IPreciseBitmap value) 
 	{
 		preciseBitmap = value;
 		Log.i("PreciseBitmapView", "Precise bitmap changed. Updating");
 		
 		// Setting imageTransformer's imageSize
 		PointD imageSize = new PointD(preciseBitmap.getWidth(), preciseBitmap.getHeight());
 		imageTransformer.setImageSize(imageSize);
 		
 		for (int i = 0; i < 2; i++)
 		{
 			cache[i].setPreciseBitmap(value);
 		}
 
 		cache[LQ].update();
 		activeImageIndex = LQ;
 		invalidate();
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
 	{
 		super.onSizeChanged(w, h, oldw, oldh);
 		Log.i("PreciseBitmapView", "Size changed. Updating");
 		
 		// Setting imageTransformer's screenSize
 		PointD screenSize = new PointD(getWidth(), getHeight());
 		imageTransformer.setScreenSize(screenSize);
 		
 		for (int i = 0; i < 2; i++)
 		{
 			cache[i].setViewSize(screenSize);
 		}
 	}
 	
 	
 	@Override
 	protected void onDraw(Canvas canvas) 
 	{
 		cache[activeImageIndex].draw(canvas);
 		if (activeImageIndex == 0) polish();
 	}
 	
 	
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) 
 	{
 		if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
 		    event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
 		{
 			currentFingers = extractFingers(event);
 			for (int i = 0; i < 2; i++)
 			{
 				cache[i].setCenter(getCenter(currentFingers));
 				cache[i].setDispersion(getDispersion(currentFingers));
 			}
 			return true;
 		}
 		else if (event.getActionMasked() == MotionEvent.ACTION_UP ||
 		         event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
 		{
 			currentFingers = extractFingers(event);
 			if (currentFingers.size() > 0)
 			{
 				for (int i = 0; i < 2; i++)
 				{
 					cache[i].setCenter(getCenter(currentFingers));
 					cache[i].setDispersion(getDispersion(currentFingers));
 				}
 			}
 			//invalidate();
 			return true;
 		}
 		else if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
 		{
 			currentFingers = extractFingers(event);
 			
 			PointD newCenter = getCenter(currentFingers);
 			double newDispersion = getDispersion(currentFingers);
 
 			if (currentFingers.size() > 1)
 			{		
 				// More than one finger -- zooming
 				imageTransformer.addPan(new PointD(newCenter.getX() - cache[activeImageIndex].getCenter().getX(), 
 													newCenter.getY() - cache[activeImageIndex].getCenter().getY()));
 				imageTransformer.zoomUponScreenPoint(newCenter, newDispersion / cache[activeImageIndex].getDispersion());
 			}
 			else
 			{
 				// Only one finger -- just panning
 				imageTransformer.addPan(new PointD(newCenter.getX() - cache[activeImageIndex].getCenter().getX(), 
 						newCenter.getY() - cache[activeImageIndex].getCenter().getY()));
 			}
 			
 			for (int i = 0; i < 2; i++)
 			{
 				cache[i].setCenter(newCenter);
 				cache[i].setDispersion(newDispersion);
 			}
 			
 			cache[LQ].update();
 			activeImageIndex = LQ;
 			invalidate();
 			
 			return true;
 		}
 		
 		return super.onTouchEvent(event);
 	}
 	
 	private void polish()
 	{
 		if (polishingDrawingThread != null && polishingDrawingThread.isAlive())
 		{
 			polishingDrawingThread.interrupt();
 			try 
 			{
 				polishingDrawingThread.join();
 			} 
 			catch (InterruptedException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		polishingDrawingThread = new Thread(this.polishingDrawingRunnable);
 		polishingDrawingThread.start();
 	}
 
 }
