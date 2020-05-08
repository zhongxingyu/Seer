 package com.b50.overheard;
 
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 import com.b50.gesticulate.SwipeDetector;
 
 
 public class OverheardWord extends Activity {
 
 	private GestureDetector gestureDetector;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_overheard_word);
 
 		gestureDetector = initGestureDetector();
 
 		View view = findViewById(R.id.LinearLayout1);
 
 		view.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 			}
 		});
 
 		view.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return gestureDetector.onTouchEvent(event);
 			}
 		});
 	}
 

 	private GestureDetector initGestureDetector() {
 		return new GestureDetector(new SimpleOnGestureListener() {
 									
 			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 				try {
 					final SwipeDetector detector = new SwipeDetector(e1, e2, velocityX, velocityY);
 					if (detector.isDownSwipe()) {
 						return false;
 					} else if (detector.isUpSwipe()) {
 						Toast.makeText(getApplicationContext(), "up Swipe", Toast.LENGTH_SHORT).show();
 					}else if (detector.isLeftSwipe()) {
 						Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
 					} else if (detector.isRightSwipe()) {
 						Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
 					}
 				} catch (Exception e) {
 					// nothing
 				}
 				return false;
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.overheard_word, menu);
 		return true;
 	}
 }
