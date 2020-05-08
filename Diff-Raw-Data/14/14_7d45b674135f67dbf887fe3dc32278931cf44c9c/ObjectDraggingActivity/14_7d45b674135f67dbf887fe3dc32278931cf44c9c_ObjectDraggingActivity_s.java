 package com.hackathon.fshow;
 
 import org.json.JSONArray;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.hackathon.fshow.module.DownloadMapAsyncTask;
 import com.hackathon.fshow.module.MyPlane;
 
 public class ObjectDraggingActivity extends RajawaliExampleActivity implements OnTouchListener {
 	private static final String TAG = "ObjectDraggingActivity";
 	private ObjectDraggingRenderer mRenderer;
 	private int mScreenWidth = 0;
 	private int mScreenHeight = 0;
	LinearLayout dropArea = null;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		mScreenWidth = size.x;
 		mScreenHeight = size.y;
 		mRenderer = new ObjectDraggingRenderer(this);
 		mRenderer.setSurfaceView(mSurfaceView);
 		super.setRenderer(mRenderer);
 		mSurfaceView.setOnTouchListener(this);
 		
 		LinearLayout ll = new LinearLayout(this);
 		ll.setOrientation(LinearLayout.VERTICAL);
         ll.setGravity(Gravity.BOTTOM);
 
 //        TextView label = new TextView(this);
 //        label.setText("Touch & drag");
 //        label.setTextSize(20);
 //        label.setGravity(Gravity.CENTER_HORIZONTAL);
 //        label.setHeight(100);
 //        ll.addView(label);
         
 //        LinearLayout dropArea = new LinearLayout(this);
 //        dropArea.setBackgroundColor(Color.TRANSPARENT);
 //        dropArea.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 130));
 //        dropArea.setGravity(Gravity.CENTER_HORIZONTAL);
        
         LinearLayout dragArea = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.drag_area, null);
       dragArea.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 130));
       dragArea.setGravity(Gravity.CENTER_HORIZONTAL);
         ll.addView(dragArea);
         dropArea = (LinearLayout) dragArea.findViewById(R.id.dropArea);
         mLayout.addView(ll);
 		
         new DownloadMapAsyncTask(this, mRenderer).execute();
 		initLoader();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 	}
	float objx, objy, objz;
	boolean isMoveFirst =false;
	boolean isInDropArea = false;
 	public boolean onTouch(View v, MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 		switch(event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				mRenderer.getObjectAt(x, y);
 				break;
 			case MotionEvent.ACTION_MOVE:
 				mRenderer.moveSelectedObject(x, y);
 				if (mRenderer.getSelectedObject() == null) {
 					return true;
 				} else if (isMoveFirst == false) {
 					objx = mRenderer.getSelectedObject().getX();
 					objy = mRenderer.getSelectedObject().getY();
 					objz = mRenderer.getSelectedObject().getZ();
 					isMoveFirst = true;
 				}
 				if (y > (mScreenHeight -200)) {
 					if(isInDropArea == false) {
 						vibrate();
 						isInDropArea = true;
 					}
 					String name = ((MyPlane)mRenderer.getSelectedObject()).getName();
 					Log.v(TAG, "@@@selected "+name);
 				} else {
 					isInDropArea = false;
 				}
 				break;
 			case MotionEvent.ACTION_UP:
				if (isInDropArea) {
 					Bitmap bm = ((MyPlane)mRenderer.getSelectedObject()).getBitmap();
 					ImageView imageView = new ImageView(this);
 					imageView.setImageBitmap(bm);
 					dropArea.addView(imageView);
 					mRenderer.getSelectedObject().setX(objx);
 					mRenderer.getSelectedObject().setY(objy);
 					mRenderer.getSelectedObject().setZ(objz);
 					isMoveFirst = false;
 				}
 				mRenderer.stopMovingSelectedObject();
 				break;
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.action_menu, menu);
 	    return true;
 	}
 	 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
 	    int id = item.getItemId();
 	    if (id == R.id.action_user) {
 	    	Intent intent = new Intent(this, UserLoginActivity.class);
 	    	startActivity(intent);
 	    } else if (id == R.id.action_new) {
 	    	Intent intent = new Intent(this, ItemResgisterActivity.class);
 	    	startActivity(intent);
 	    }
 	    return true;
 	}
 
 	public void duy(JSONArray items) {
 //		mRenderer = new ObjectDraggingRenderer(this, items);
 //		mRenderer.setSurfaceView(mSurfaceView);
 //		super.setRenderer(mRenderer);
 //		mSurfaceView.setOnTouchListener(this);
 		
 	}
 	
 	private void vibrate() {
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		// Vibrate for 500 milliseconds
 		v.vibrate(100);
 	}
 }
