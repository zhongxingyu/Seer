 package com.cs371m.notesync;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.Toast;
 
 public class MyImageView extends ImageView {
 
 	ArrayList<Point> points = new ArrayList<Point>();
 	Paint paint = new Paint();
 	MainActivity activity;
 	MediaController myMediaController;
 	PlaybackController myPController;
 	private static final String DEBUG_TAG = "MyImageView";
 	private static final float THRESHOLD=5;
 	public MyImageView(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 		Log.v(DEBUG_TAG,"myImageView Constructor");
 		activity = (MainActivity) context;
 	}
 	public MyImageView(Context context, AttributeSet attrs, MediaController myMedia)
 	{
 		super(context, attrs);
 		Log.v(DEBUG_TAG,"myImageView Constructor");
 		activity = (MainActivity) context;
 	}
 	public void setControllers(MediaController mediaController, PlaybackController playbackController)
 	{
 		myMediaController=mediaController;
 		myPController=playbackController;
 	}
 
 	@Override
 	public void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		Log.v(DEBUG_TAG,"onDraw()");
 		if (activity != null && activity.mCurrentNote != null && activity.mCurrentNote.bookmarks != null) {
 			for (Point point : activity.mCurrentNote.bookmarks) {
 				canvas.drawCircle(point.x, point.y, 20, paint);
 				// Log.d(TAG, "Painting: "+point);
 			}
 		}
 	}
 
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		return gestureDetector.onTouchEvent(event);
 	}
 	final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureListener(getContext()));
 
 	public final class GestureListener extends SimpleOnGestureListener
 	{
 		final Context myContext;
 		public GestureListener(Context context)
 		{
 			myContext = context;
 		}
 		@Override
 		public boolean onDown(MotionEvent e)
 		{
 			Log.v(DEBUG_TAG, "Down tapped");
 			return true;
 		}
 		@Override
 		public boolean onSingleTapUp(MotionEvent e)
 		{
 			super.onSingleTapUp(e);
 			Log.v(DEBUG_TAG, "Single Tap up");
 
 			//If around visual mark
 			float eventX=e.getX();
 			float eventY=e.getY();
 			Point tP1=new Point(eventX, eventY);
 			//Call distance formula
 
 			if (activity != null && activity.mCurrentNote != null 
 					&& myPController!=null && activity.mCurrentNote.bookmarks != null)
 				
 			{
 
 				for (int i=0; i< activity.mCurrentNote.bookmarks.size();i++)
 				{
 					if (Point.getDistance(tP1, activity.mCurrentNote.bookmarks.get(i)) < 20+THRESHOLD)
 					{	
 						long eachTime= activity.mCurrentNote.timestamps.get(i);
 						myPController.seekTo((int) eachTime);
 						myMediaController.show();
 						
 					}		
 				}
 			}			
 
 			//Seek to that point
 			//Call 
 
 			myMediaController.show();
 			return true;
 		}
 
 
 
 		@Override
 		public void onLongPress(MotionEvent event)
 		{
 			//If we're here onDown() has been called
 
 			Log.v(DEBUG_TAG, "Longpressed");
 			if (activity != null && activity.mCurrentNote != null)
 			{
 				if (activity.mCurrentNote.bookmarks == null)
 					activity.mCurrentNote.bookmarks = new ArrayList<Point>();
 				if (activity.mCurrentNote.bookmarks.size() < activity.mCurrentNote.timestamps.size()) 
 				{	
 					//Are you sure to make this mark?
 					//Create confirmation dialog
 					confirmDialog dialog=new confirmDialog();
 		
 					Point point = new Point(event.getX(), event.getY());
 					activity.mCurrentNote.bookmarks.add(point);
 					invalidate();
 					Log.d(DEBUG_TAG, "marked point: " + point);
 				}
 
 			}
 		}
 		/*
 		public void drawCircle(Point p)
 		{
 			
 		}
 		*/
 		public class confirmDialog extends DialogFragment 
 		{
 			
 			//Source:http://developer.android.com/guide/topics/ui/dialogs.html
 			@Override
 		    public Dialog onCreateDialog(Bundle savedInstanceState) 
 			{
 		        // Use the Builder class for convenient dialog construction
 		        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		        builder.setMessage(R.string.confirmAddNote)
 		               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 		                   public void onClick(DialogInterface dialog, int id) 
 		                   {
 		                	  
 		                	 /*  
 		                	 drawCircle();
 		   					activity.mCurrentNote.bookmarks.add(point);
 		   					invalidate();
 		   					Log.d(DEBUG_TAG, "marked point: " + point);	
 		   					*/
 		                	   Log.v(DEBUG_TAG, "OK!!");
 		                	   
 		                	  
 		                   }
 		               })
 		               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 		                   public void onClick(DialogInterface dialog, int id) {
 		                       // User cancelled the dialog
 		                	   Log.v(DEBUG_TAG, "CANCEL!!");
 		                   }
 		               });
 		        // Create the AlertDialog object and return it
 		        return builder.create();
 		    }
 
 		}
 
 	}
 
 }
 
