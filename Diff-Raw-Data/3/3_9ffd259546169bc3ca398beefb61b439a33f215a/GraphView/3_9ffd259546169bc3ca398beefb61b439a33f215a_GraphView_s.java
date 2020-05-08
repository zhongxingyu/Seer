 package com.mmio.lab;
 
 import static android.graphics.Color.BLACK;
 import static android.graphics.Color.BLUE;
 import static android.graphics.Color.GRAY;
 import static android.graphics.Color.RED;
 import static android.graphics.Color.WHITE;
 
 import android.content.Context;
 import android.graphics.*;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 import java.util.ArrayList;
 import edu.function.IFunction;
 
 public class GraphView extends View
 {
 	private double from = -1, to = 1;
 	private ArrayList<IFunction> func = new ArrayList<IFunction> ();  //  @jve:decl-index=0:
 	private int step = 21;
 	private double scale = 1;
 	private float xAbsShift=4, yAbsShift=8;
 	private int color=1;
     private VersionedGestureDetector mDetector;
     private Paint mPaint;
     
     public GraphView(Context context) {
         this(context, null, 0);
     }
     
     public GraphView(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
     
     public GraphView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         
         mPaint = new Paint();
         mDetector = VersionedGestureDetector.newInstance(context, new GestureCallback());
     }
     
     private class GestureCallback implements VersionedGestureDetector.OnGestureListener {
     	
         public void onScale(float scaleFactor) {
             //scale *= scaleFactor;
             step *= scaleFactor;
             if (step < 10 ) step=10;
             else if (step > 100) step=100;
             // Don't let the object get too small or too large.
             //scale = Math.max(0.1f, Math.min(scale, 5.0f));
 
             invalidate();
         }
 
 		@Override
 		public void onDrag(float dx, float dy)
 		{
 			xAbsShift += dx;
 			yAbsShift += dy;
 			invalidate();
 		}
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent ev) {
         mDetector.onTouchEvent(ev);
         return true;
     }
     
 	public void setColor(String color)
 	{
 		if (color.equals("Red"))
 			this.color = 1;
 		if (color.equals("Blue"))
 			this.color = 2;
 		if (color.equals("Green"))
 			this.color = 3;
 	}
 	
 	public ArrayList<IFunction> getFunc()
 	{
 		return func;
 	}
 
 	public void setFunc(ArrayList<IFunction> func)
 	{
 		this.func = func;
 	}
 	
 	public void addFunc(IFunction func)
 	{
 		this.func.add(func);
 	}
 
 	public double getFrom()
 	{
 		return from;
 	}
 
 	public int getStep()
 	{
 		return step;
 	}
 
 	public void setStep(int step)
 	{
 		this.step = step;
 	}
 
 	public double getScale()
 	{
 		return scale;
 	}
 
 	public void setScale(double scale)
 	{
 		this.scale = scale;
 	}
 
 	public float getxAbsShift()
 	{
 		return xAbsShift;
 	}
 
 	public void setxAbsShift(int xAbsShift)
 	{
 		this.xAbsShift = xAbsShift;
 	}
 
 	public float getyAbsShift()
 	{
 		return yAbsShift;
 	}
 
 	public void setyAbsShift(int yAbsShift)
 	{
 		this.yAbsShift = yAbsShift;
 	}
 
 	public void setFrom(double from)
 	{
 		this.from = from;
 	}
 
 	public double getTo()
 	{
 		return to;
 	}
 
 	public void setTo(double to)
 	{
 		this.to = to;
 	}
 	
 	@Override
 	protected void onDraw(Canvas g)
 	{
 		mPaint.setColor(WHITE);
 	    g.drawRect(0.0f, 0.0f, (float)getWidth(), (float)getHeight(), mPaint);
 	    
 	    int xShift = (int)xAbsShift%step;
 	    int yShift = (int)yAbsShift%step;
 	    
 	    int zeroX = getWidth()/step/2*step + (int)xAbsShift;
 	    int zeroY = getHeight()/step/2*step + (int)yAbsShift;
 	    
 	    float k = (float)scale/(float)step;
 	    
 	    for (int i=xShift; i<getWidth();i+=step)
 	    	{
 		      
 		      if (i!=zeroX)
 		    	  {
 			    	  mPaint.setColor(GRAY);
 				      g.drawLine(i, 0, i, getHeight(), mPaint);
 				      mPaint.setColor(BLACK);
 			    	  g.drawText((int)((i-zeroX)/step*scale) + "", i + 2, 10, mPaint);
 		    	  }
 		      else if (i==zeroX)
 		    	  {
 		    		  mPaint.setColor(RED);
 		    		  g.drawLine(i, 0, i, getHeight(),mPaint);
 		    		  mPaint.setColor(BLACK);
 		    		  g.drawText("Y", i + 2, 10, mPaint);	   
 		    	  }
 		    }
 	    	
 	    for (int i=yShift; i<getHeight();i+=step)
 	    	{
 		      
 		      if (i!=zeroY)
 		    	  {
 			    	  mPaint.setColor(GRAY);
 				      g.drawLine(0, i, getWidth(), i, mPaint);
 				      mPaint.setColor(BLACK);
 			    	  g.drawText((int)((zeroY-i)/step*scale) + "", 2, i - 2, mPaint);
 		    	  }
 		      else if (i==zeroY)
 		    	  {
 		    		  mPaint.setColor(RED);
 		    		  g.drawLine(0, i, getWidth(), i, mPaint);
 		    		  mPaint.setColor(BLACK);
 		    		  g.drawText("0.0", 2, i - 2, mPaint);
 		    		  g.drawText("X", getWidth() - 10, i - 2, mPaint);
 		    	  }
 		    }
 
 	    //  ,   :
 	    if (func.size() != 0)
 		    {
 			    for (int i=0; i<func.size(); i++)
 				    {
 					    if (func.get(i) != null) 
 						    { 
 						    	if (color==1)
 						    		mPaint.setColor(RED);
 						    	if (color==2)
 						    		mPaint.setColor(BLUE);
 						    	if (color==3)
 						    		mPaint.setColor(Color.GREEN);
 						      int yOld = (int) (func.get(i).substitute((1.0 - zeroX)*k)/k);
 						      int y=0;
 						      for (int x = 2; x < getWidth(); x++) 
 						    	  {
 						    		  y = (int) (func.get(i).substitute((x - zeroX)*k)/k);
 						    		  g.drawLine(x-1, zeroY - yOld, x, zeroY - y, mPaint);
 						    		  yOld=y;
 						          }
 						    }
 				    }
 		    }
 	    
 	    mPaint.setColor(Color.GREEN);
 	    g.drawLine((int)(zeroX + from/k), 0 , (int)(zeroX + from/k), getHeight(), mPaint);
 	    g.drawLine((int)(zeroX + to/k), 0 , (int)(zeroX + to/k), getHeight(), mPaint);
 	}
 }
