 package com.hackathon.fshow;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.KeyEvent;
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
 
 public class ObjectDraggingActivity extends RajawaliExampleActivity implements
 		OnTouchListener {
 	private static final String TAG = "ObjectDraggingActivity";
 	private ObjectDraggingRenderer mRenderer;
 	private int mScreenWidth = 0;
 	private int mScreenHeight = 0;
 	private LinearLayout dropArea = null;
 	private float objx, objy, objz;
 	private boolean isMoveFirst = false;
 	private boolean isInDropArea = false;
 	public static String sUserId;
 	private int mScreenType = 1; //1=all; 2=mine
 	
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
 
 		// TextView label = new TextView(this);
 		// label.setText("Touch & drag");
 		// label.setTextSize(20);
 		// label.setGravity(Gravity.CENTER_HORIZONTAL);
 		// label.setHeight(100);
 		// ll.addView(label);
 
 		LinearLayout dragArea = (LinearLayout) LayoutInflater.from(this)
 				.inflate(R.layout.drag_area, null);
 		dragArea.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				130));
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
 
 	public boolean onTouch(View v, MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 		switch (event.getAction()) {
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
 			if (y > (mScreenHeight - 200)) {
 				if (isInDropArea == false) {
 					vibrate();
 					isInDropArea = true;
 				}
 				String name = ((MyPlane) mRenderer.getSelectedObject())
 						.getName();
 				Log.v(TAG, "@@@selected " + name);
 			} else {
 				isInDropArea = false;
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 			if (isInDropArea && mRenderer.getSelectedObject() != null) {
 				Bitmap bm = ((MyPlane) mRenderer.getSelectedObject())
 						.getBitmap();
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
 		Toast.makeText(this, "Selected Item: " + item.getTitle(),
 				Toast.LENGTH_SHORT).show();
 		int id = item.getItemId();
 		if (id == R.id.action_user) {
 			Intent intent = new Intent(this, UserLoginActivity.class);
 			startActivityForResult(intent, 1);
 		} else if (id == R.id.action_new) {
 			Intent intent = new Intent(this, ItemResgisterActivity.class);
 			startActivityForResult(intent, 2);
 		} else if (id == R.id.action_myitem) {
 			new DownloadMapAsyncTask(this, mRenderer).execute(sUserId);
 			mScreenType = 2;
 		} else if (id == R.id.action_allitem) {
 			mScreenType = 1;
 			new DownloadMapAsyncTask(this, mRenderer).execute();
 		} else if (id == R.id.action_update) {
 			if (mScreenType == 1) {
 				new DownloadMapAsyncTask(this, mRenderer).execute();
 			} else if (mScreenType == 2) {
 				new DownloadMapAsyncTask(this, mRenderer).execute(sUserId);
 			}
 		}
 		return true;
 	}
 
 	private void vibrate() {
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		// Vibrate for 50 milliseconds
 		v.vibrate(50);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == 2) {
 			if (mScreenType == 1) {
 				new DownloadMapAsyncTask(this, mRenderer).execute();
 			} else if (mScreenType == 2) {
 				new DownloadMapAsyncTask(this, mRenderer).execute(sUserId);
 			}
 		}
 	}
 	
 	long backcount = 0;
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (backcount == 0) {
 				Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
 				backcount = System.currentTimeMillis();
 				return true;
			} else if (System.currentTimeMillis() - backcount > 2000) {
				backcount = System.currentTimeMillis();
				Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
