 package no.uio.inf5750.assignment3.dashboard;
 
 import no.uio.inf5750.assignment3.R;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.os.Bundle;
 import android.util.FloatMath;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 
 public class DashboardChartActivity extends Activity {
 
 	private ProgressBar mProgressBar;	
 	private ImageView mImageView;
 	private Button mButtonShare, mButtonInsert, mButtonClear;
 	private Bundle extras;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dashboardchart);
 
 		mImageView = (ImageView) findViewById(R.id.dashboardchart_imageview);
 		mProgressBar = (ProgressBar) findViewById(R.id.dashboardchart_progress);
 		mButtonShare = (Button) findViewById(R.id.dashboardchart_btnShare);
 		mButtonInsert = (Button) findViewById(R.id.dashboardchart_btnInsert);
 		mButtonClear = (Button) findViewById(R.id.dashboardchart_btnClear);
 
 		setButtons();
 		setImage();
 	}
 	
 	Bitmap bmp;
 	// For OnTouchListener:
 	Matrix mtx = new Matrix();
 	Matrix tempMtx = new Matrix();
 	private PointF contact = new PointF(), midpoint = new PointF();
 	private float dist, tempDist;
 	static final int PASSIVE = 0;
 	static final int DRAGGING = 1;
 	static final int ZOOMING = 2;
 	private int state = PASSIVE;
 	private void setImage()
 	{ 
 		final Thread setImageThread = new Thread(){
 			public void run()
			{ //Separate thread to be run on UI
 				if(bmp != null)
 				{
 		        	mImageView.setImageBitmap(bmp);
 				}
 				else
 				{
 					mImageView.setImageDrawable(getResources().getDrawable(R.drawable.notfound));
 				}
 				mImageView.setVisibility(View.VISIBLE);
 	        	mProgressBar.setVisibility(View.INVISIBLE);
 			}
 		};
 
 		//Fetching images in separate threads
 		new Thread()
 		{
 			public void run()
 			{
 				extras = getIntent().getExtras();
 				try {
 					byte [] barr = extras.getByteArray("dashboard_chart");
 					bmp = BitmapFactory.decodeByteArray(barr, 0, barr.length);
 				} catch (NullPointerException npe) {
 					npe.printStackTrace();
 					bmp = null;
 				}
 				runOnUiThread(setImageThread);
 			}
 		}.start();
 		
 		mImageView.setOnTouchListener(new OnTouchListener()
 		{
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				ImageView view = (ImageView) v;
 				
 				switch(event.getAction() & MotionEvent.ACTION_MASK) {
 				case MotionEvent.ACTION_DOWN:
 					tempMtx.set(mtx);
 					contact.set(event.getX(), event.getY());
 					state = DRAGGING;
 					break;
 				case MotionEvent.ACTION_UP:
 				case MotionEvent.ACTION_POINTER_UP:
 					state = PASSIVE;
 					break;
 				case MotionEvent.ACTION_MOVE:
 					if (state == DRAGGING) {
 						mtx.set(tempMtx);
 						mtx.postTranslate(event.getX() - contact.x, event.getY() - contact.y);
 					}
 					else if (state == ZOOMING) {
 						dist = getDist(event);
 						if (dist > 10f) {
 							mtx.set(tempMtx);
 							float scale = dist / tempDist;
 							mtx.postScale(scale, scale, midpoint.x, midpoint.y);
 						}
 					}
 					break;
 				case MotionEvent.ACTION_POINTER_DOWN:
 					tempDist = getDist(event);
 					if (tempDist > 10f) {
 						tempMtx.set(mtx);
 						setMid(midpoint, event);
 						state = ZOOMING;
 					}
 					break;
 				}
 				
 				view.setImageMatrix(mtx);
 				return true;
 			}
 			
 			private float getDist(MotionEvent event) {
 				float x = event.getX(0) - event.getX(1);
 				float y = event.getY(0) - event.getY(1);
 				return FloatMath.sqrt(x*x + y*y);
 			}
 			
 			private void setMid(PointF p, MotionEvent event) {
 				float x = event.getX(0) + event.getX(1);
 				float y = event.getY(0) + event.getY(1);
 				p.set(x/2, y/2);
 			}
 		});
 	}
 	
 	private void setButtons() {
 		mButtonShare.setOnClickListener(new OnClickListener() 
 		{	
 			public void onClick(View v) 
 			{
 				// TODO Auto-generated method stub
 			}
 		});
 
 		mButtonInsert.setOnClickListener(new OnClickListener() 
 		{	
 			public void onClick(View v) 
 			{
 				// TODO Auto-generated method stub
 			}
 		});
 		
 		mButtonClear.setOnClickListener(new OnClickListener() 
 		{	
 			public void onClick(View v) 
 			{
 				// TODO Auto-generated method stub
 			}
 		});
 	}
 }
