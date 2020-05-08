 package drexel.dragonmap;
 /*
 	updated from Mark's week 6 code
  */
 
 
 
 /* This is Mark's code, and as such, I am
  * full unqualified to comment on it :-) 
  */
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.widget.ImageView;
 
 public class MapView extends ImageView {
 
     Matrix matrix = new Matrix();
 
     // We can be in one of these 3 states
     static final int NONE = 0;
     static final int DRAG = 1;
     static final int ZOOM = 2;
     int mode = NONE;
 
     // Remember some things for zooming
     PointF last = new PointF();
     PointF start = new PointF();
     float minScale = 1f;
     float maxScale = 3f;
     float[] m;
     
     float redundantXSpace, redundantYSpace;
     
     float width, height;
     static final int CLICK = 5;
     float saveScale = 1f;
     float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
     
     ScaleGestureDetector mScaleDetector;
     
     Context context;
 
     public MapView(Context context) {
         super(context);
         sharedConstructing(context);
     }
     
     public MapView(Context context, AttributeSet attrs) {
     	super(context, attrs);
     	sharedConstructing(context);
     }
     
     private void sharedConstructing(Context context) {
     	super.setClickable(true);
         this.context = context;
         mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
         matrix.setTranslate(1f, 1f);
         m = new float[9];
         setImageMatrix(matrix);
         setScaleType(ScaleType.MATRIX);
         
 
         setOnTouchListener(new OnTouchListener() {
 
             public boolean onTouch(View v, MotionEvent event) {
             	mScaleDetector.onTouchEvent(event);
 
             	matrix.getValues(m);
             	float x = m[Matrix.MTRANS_X];  //x = scaled x coordinate of the upper-left corner
             	float y = m[Matrix.MTRANS_Y];  //y = scaled y coordinate of the upper-left corner
             	PointF curr = new PointF(event.getX(), event.getY());
             	
             	switch (event.getAction()) {
 	            	case MotionEvent.ACTION_DOWN:
 	                    last.set(event.getX(), event.getY());
 	                    start.set(last);                  
 	                    mode = DRAG;
 	                    break;
 	            	case MotionEvent.ACTION_MOVE:
 	            		if (mode == DRAG) {
 	            			float deltaX = curr.x - last.x;
 	            			float deltaY = curr.y - last.y;
 	            			float scaleWidth = Math.round(origWidth * saveScale);
 	            			float scaleHeight = Math.round(origHeight * saveScale);
             				if (scaleWidth < width) {
 	            				deltaX = 0;
 	            				if (y + deltaY > 0)
 		            				deltaY = -y;
 	            				else if (y + deltaY < -bottom)
 		            				deltaY = -(y + bottom); 
             				} else if (scaleHeight < height) {
 	            				deltaY = 0;
 	            				if (x + deltaX > 0)
 		            				deltaX = -x;
 		            			else if (x + deltaX < -right)
 		            				deltaX = -(x + right);
             				} else {
 	            				if (x + deltaX > 0)
 		            				deltaX = -x;
 		            			else if (x + deltaX < -right)
 		            				deltaX = -(x + right);
 		            			
 	            				if (y + deltaY > 0)
 		            				deltaY = -y;
 		            			else if (y + deltaY < -bottom)
 		            				deltaY = -(y + bottom);
 	            			}
                         	matrix.postTranslate(deltaX, deltaY);
                         	last.set(curr.x, curr.y);
 	                    }
 	            		break;
 	            		
 	            	case MotionEvent.ACTION_UP:
 	            		mode = NONE;
 	            		int xDiff = (int) Math.abs(curr.x - start.x);
 	                    int yDiff = (int) Math.abs(curr.y - start.y);
 	                    //If there is no movement, consider the event a click
 	                    if (xDiff < CLICK && yDiff < CLICK)
 	                    {
 	                    	//Find the unscaled left-hand corner coordinate by dividing the translated position by the scaling
 	                    	float xLeftCorner = m[Matrix.MTRANS_X]/m[Matrix.MSCALE_X];
 	                    	float yLeftCorner = m[Matrix.MTRANS_Y]/m[Matrix.MSCALE_Y];
 	                    	
 	                    	//Figure out the absolute position of the touch by dividing taking the touch screen position
 	                    	//divided by the scaling and multiple by (-1)
 	                    	float unscaledTouchX = xLeftCorner + ((-1) * event.getX() / m[Matrix.MSCALE_X]);
 	                    	float unscaledTouchY = yLeftCorner + ((-1) * event.getY() / m[Matrix.MSCALE_Y]);
 	                    	
 	                        //To find percentage, divide scaledTouchX (or Y) by bmWidth (or height)
 	                    	//THIS IS REQUIRED FOR A UNIVERSAL COORDINATE SYSTEM
 	                    	float xPercent = (unscaledTouchX/bmWidth)*(float)-1;
 	                    	float yPercent = (unscaledTouchY/bmHeight)*(float)-1;
 	                    	
 	                    	//Call the performClick function to see what building was clicked
 	                    	performClick(xPercent,yPercent);
 	                    }	                    
 	            		break;
 	            		
 	            	case MotionEvent.ACTION_POINTER_UP:
 	            		mode = NONE;
 	            		break;
             	}
                 setImageMatrix(matrix);
                 invalidate();
                 return true; // indicate event was handled
             }
 
         });
     }
     
     /*
      * Modified by Drew, 5/22
      * Now the Alert prompts the user to see the details of the thing they clicked
      * or to ignore their click.
      */
     public void performClick(float x, float y)
     {
     	//must be final so it can be accessed inside of the onClick methods below
     	// Drew, what is this line?  Is this what's stopping the click from working?
     	final POI clicked = DBAccessor.getInstance().getData().getFirstContained(x, y);
     	if (clicked == null)
     	{
     		//didn't click a POI
     		return;
     	}
     	else
     	{
             AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
             alertDialog.setTitle(clicked.getName());
            alertDialog.setIcon(R.drawable.icon_small);
             alertDialog.setMessage("What would you like to do?");
             alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "View Info", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
        				Intent myIntent = new Intent(getContext(), DetailedViewActivity.class);
        				//send over the POI name
                     myIntent.putExtra("POI", clicked.getName());
                     getContext().startActivity(myIntent);
     			}
     		});
             alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
     				//do nothing
     				//i wonder if i can make the onClickListener null
     				//NullPointerException? who knows
     			}
     		});
             alertDialog.show();	
     	}
     }
 
     @Override
     public void setImageBitmap(Bitmap bm) { 
         super.setImageBitmap(bm);
         if(bm != null) {
         	bmWidth = bm.getWidth();
         	bmHeight = bm.getHeight();
         }
     }
     
     public void setMaxZoom(float x)
     {
     	maxScale = x;
     }
     
     private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
     	@Override
     	public boolean onScaleBegin(ScaleGestureDetector detector) {
     		mode = ZOOM;
     		return true;
     	}
     	
 		@Override
 	    public boolean onScale(ScaleGestureDetector detector) {
 			float mScaleFactor = detector.getScaleFactor();
 		 	float origScale = saveScale;
 	        saveScale *= mScaleFactor;
 	        if (saveScale > maxScale) {
 	        	saveScale = maxScale;
 	        	mScaleFactor = maxScale / origScale;
 	        } else if (saveScale < minScale) {
 	        	saveScale = minScale;
 	        	mScaleFactor = minScale / origScale;
 	        }
         	right = width * saveScale - width - (2 * redundantXSpace * saveScale);
             bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
         	if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
         		matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
             	if (mScaleFactor < 1) {
             		matrix.getValues(m);
             		float x = m[Matrix.MTRANS_X];
                 	float y = m[Matrix.MTRANS_Y];
                 	if (mScaleFactor < 1) {
         	        	if (Math.round(origWidth * saveScale) < width) {
         	        		if (y < -bottom)
             	        		matrix.postTranslate(0, -(y + bottom));
         	        		else if (y > 0)
             	        		matrix.postTranslate(0, -y);
         	        	} else {
 	                		if (x < -right) 
 	        	        		matrix.postTranslate(-(x + right), 0);
 	                		else if (x > 0) 
 	        	        		matrix.postTranslate(-x, 0);
         	        	}
                 	}
             	}
         	} else {
             	matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
             	matrix.getValues(m);
             	float x = m[Matrix.MTRANS_X];
             	float y = m[Matrix.MTRANS_Y];
             	if (mScaleFactor < 1) {
     	        	if (x < -right) 
     	        		matrix.postTranslate(-(x + right), 0);
     	        	else if (x > 0) 
     	        		matrix.postTranslate(-x, 0);
     	        	if (y < -bottom)
     	        		matrix.postTranslate(0, -(y + bottom));
     	        	else if (y > 0)
     	        		matrix.postTranslate(0, -y);
             	}
         	}
 	        return true;
 	        
 	    }
 	}
     
     @Override
     protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
     {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         width = MeasureSpec.getSize(widthMeasureSpec);
         height = MeasureSpec.getSize(heightMeasureSpec);
         //Fit to screen.
         //float scale;
         float scaleX =  (float)width / (float)bmWidth;
         float scaleY = (float)height / (float)bmHeight;
         float scale = Math.max(scaleX, scaleY);
         matrix.setScale(scale, scale);
         setImageMatrix(matrix);
         saveScale = 1f;
 
         // Center the image
         redundantYSpace = (float)height - (scale * (float)bmHeight) ;
         redundantXSpace = (float)width - (scale * (float)bmWidth);
         redundantYSpace /= (float)2;
         redundantXSpace /= (float)2;
 
         matrix.postTranslate(redundantXSpace, redundantYSpace);
         
         //Set the orginal scaled width and height of the image
         origWidth = width - 2 * redundantXSpace;
         origHeight = height - 2 * redundantYSpace;
         right = width * saveScale - width - (2 * redundantXSpace * saveScale);
         bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
         setImageMatrix(matrix);
     }
 }
