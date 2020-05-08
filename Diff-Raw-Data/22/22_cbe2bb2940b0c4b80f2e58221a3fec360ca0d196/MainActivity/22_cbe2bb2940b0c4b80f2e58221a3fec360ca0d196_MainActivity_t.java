 package com.exmple.drags;
 
 import android.app.Activity;
 import android.content.ClipData;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	Button b1, b2, r1, r2;
 	LinearLayout drpHere;
 	View gobx;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setupViews();
 		setListeners();
 	}
 
 	private void setListeners() {
 		// TODO Auto-generated method stub
 		b1.setOnTouchListener(new MyTouchListener());
 		b2.setOnTouchListener(new MyTouchListener());
 		r1.setOnTouchListener(new MyTouchListener());
 		r2.setOnTouchListener(new MyTouchListener());
 		drpHere.setOnDragListener(new MyDragListener());
 	}
 
 	private void setupViews() {
 		// TODO Auto-generated method stub
 		b1 = (Button) findViewById(R.id.blkFirst);
 		b2 = (Button) findViewById(R.id.blkSecond);
 		r1 = (Button) findViewById(R.id.redFirst);
 		r2 = (Button) findViewById(R.id.redSecond);
 		drpHere = (LinearLayout) findViewById(R.id.drpHere);
 
 	}
 
 	// This defines your touch listener Currently works only for API 11
 	// have to port some methods I guess
 	// njoy
 	private final class MyTouchListener implements OnTouchListener {
 		public boolean onTouch(View view, MotionEvent motionEvent) {
 			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
 				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = (DragShadowBuilder) new View.DragShadowBuilder(
 						view);
				view.startDrag(data, (android.view.View.DragShadowBuilder) shadowBuilder, view, 0);
 				view.setVisibility(View.INVISIBLE);
 				gobx = view;
 				return true;
 			}
			/*if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
 				view.setVisibility(View.VISIBLE);
 				return true;
			}*/
 			return false;
 		}

 	}
 
 	class MyDragListener implements OnDragListener {
 		Drawable enterShape = getResources().getDrawable(R.drawable.white);
 
 		// Drawable normalShape = getResources().getDrawable(R.drawable.black);
 
 		@Override
 		public boolean onDrag(View v, DragEvent event) {
 			int action = event.getAction();
 			switch (event.getAction()) {
 			case DragEvent.ACTION_DRAG_STARTED:
 				// Do nothing
 				break;
 			case DragEvent.ACTION_DRAG_ENTERED:
 				v.setBackgroundDrawable(enterShape);
 				break;
 			case DragEvent.ACTION_DRAG_EXITED:
 				// v.setBackgroundDrawable(normalShape);
				
 				break;
 			case DragEvent.ACTION_DROP:
 				// Dropped, reassign View to ViewGroup
 				View view = (View) event.getLocalState();
 				ViewGroup owner = (ViewGroup) view.getParent();
 				owner.removeView(view);
 				LinearLayout container = (LinearLayout) v;
 				container.addView(view);
 				view.setVisibility(View.VISIBLE);
 				gobx.setEnabled(false);
 				break;
 			case DragEvent.ACTION_DRAG_ENDED:
 				// v.setBackgroundDrawable(normalShape);
				if(gobx.getParent()!=drpHere)
				gobx.setVisibility(View.VISIBLE);
 			default:
 				break;
 			}
 			return true;
 		}
 	}
 
 	public void TostMsg(String msg) {
 		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
 	}
 
 }
