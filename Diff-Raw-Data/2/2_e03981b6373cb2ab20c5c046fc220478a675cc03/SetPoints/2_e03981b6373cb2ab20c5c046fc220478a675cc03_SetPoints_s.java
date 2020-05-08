 package amazenite.lockit;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.drawable.Drawable;
 import android.view.View; 
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.support.v4.view.GestureDetectorCompat;
 
 
 public class SetPoints extends Activity { 
 	private static final String DEBUG_TAG = "Gestures"; 	
 	private static float x = -50;
  	private static float y = -50;
  	private GraphicView graphView; 
  	private GestureDetectorCompat mDetector; 
  	private float[] coordinates = {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f};
 
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		graphView = new GraphicView(this);
 		//setContentView(R.layout.activity_set_points);
 		
 		
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		//Set As Background Image
 	    
 	    File file = getBaseContext().getFileStreamPath("lockimg");
 	    String internalPath = "data/data/files/lockimg";
 	    if (file.exists()) {
 	    	 internalPath = file.getAbsolutePath();
 	    }
         Drawable d = Drawable.createFromPath(internalPath);
         if(d!=null)
         {
        	 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
         		 setContentView(graphView);
         		 graphView.setBackground(d);
         	 }
         	 else
         	 {
         		 setContentView(graphView);
         		 graphView.setBackgroundDrawable(d);
         	 }
          }    
 	    mDetector = new GestureDetectorCompat(this, new MyGestureListener());
 	}
 	
     @Override 
     public boolean onTouchEvent(MotionEvent event){ 
         this.mDetector.onTouchEvent(event);        
         return super.onTouchEvent(event);
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_set_points, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 		//	NavUtils.navigateUpFromSameTask(this);
 			finish();
 			return true;
 		}
 		return false;//return super.onOptionsItemSelected(item);
 	}
 	
 	public void storeCoordinates(float x, float y)
 	{
 		for(int i = 0; i<coordinates.length; i++)
 		{
 			if(coordinates[i] == -1)
 			{
 				coordinates[i] = x;
 				coordinates[i+1] = y;
 				break;
 			}
 		}
 		checkFull();
 	}
 	
 	public void checkFull()
 	{
 		/*
 		if(coordinates[coordinates.length-1] != -1)
 		{
 			//Full, save the array
 			try {
 				String space = " ";
 	        	FileOutputStream fos = openFileOutput("coordinates", Context.MODE_PRIVATE);
 		        for(int i = 0; i<coordinates.length; i++)
 		        {
 		        	fos.write((space + Float.toString(coordinates[i])).getBytes());
 		        }
 	        	try {
 		        		fos.close();
 		        		fos = null;
 		        	} 
 		        	catch (IOException e) {
 		        		e.printStackTrace();
 		        	}
 		        	chosenImage.recycle();
 	        	} 
 	        catch (FileNotFoundException e1) {
 	        	e1.printStackTrace();
 	        	}
 			
 			finish();
 		}
 		*/
 	}
 	
 	public void clearCoordiantes()
 	{
 		for(int i = 0; i<coordinates.length; i++)
 		{
 			coordinates[i] = -1;
 		}
 	}
 	
 	/* Gesture Dectector Class To Only listen On The Ones We Want */	
 	public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
 		  @Override
 		    public boolean onSingleTapConfirmed(MotionEvent event) {
 		        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
 		        x = event.getRawX();
 		        y = event.getRawY()-75.0f;
 		        graphView.invalidate();
 		        Log.d(DEBUG_TAG, "X is: " + x);
 			    Log.d(DEBUG_TAG, "Y is: " + y);
 			    
 			    storeCoordinates(x, y);
 			    
 		        return true;
 		    }
 		  
 		  @Override
 		  public boolean onDoubleTap(MotionEvent event)
 		  {
 			  clearCoordiantes();
 			  Toast.makeText(SetPoints.this, "Gestures reset, please make 3 gestures again", Toast.LENGTH_SHORT).show();
 			  x = -100;
 			  y = -100;
 			  graphView.invalidate();
 			  return true;
 		  }
 	 }
 	  
 	 public class GraphicView extends View{		  
 		  Paint dotColor = new Paint(Paint.ANTI_ALIAS_FLAG);
 		  
 	        public GraphicView(Context context){
 	            super(context);
 	            setFocusable(true);
 	        }
 
 	        @Override
 	        public void onDraw(Canvas canvas){
 	        	dotColor.setColor(0xff33CCCC);
 	        	dotColor.setAlpha(80);
 	        	super.onDraw(canvas);
 	        	dotColor.setStyle(Paint.Style.FILL);
 	        	canvas.drawCircle(x, y, 20, dotColor);
 	        	
 	        	for(int i = 0; i<coordinates.length; i++)
 	    		{
 	    			if(coordinates[i] != -1)
 	    			{
 	    				Log.d("coordinates", Float.toString(coordinates[i]));
 	    			}
 	    		}
 	        }	          
 	   }
 }
 
 
