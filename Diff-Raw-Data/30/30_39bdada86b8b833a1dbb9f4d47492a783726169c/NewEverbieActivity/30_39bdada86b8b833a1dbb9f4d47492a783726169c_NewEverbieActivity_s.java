 package com.cheesymountain.woe;
 
 
 import com.cheesymountain.woe.SimpleGestureFilter.SimpleGestureListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class NewEverbieActivity extends Activity implements SimpleGestureListener {
 
 	private int[] imageId = {
 		R.drawable.wbe,
 		R.drawable.ic_launcher,
 		R.drawable.ic_action_search,
 	};
 	private String[] imageDescription = {
			"Mogno:\nThe Mogno race are known for their sharp calws and short temper.",
			"Mogno:\nThe Mogno race are known for their sharp calws and short temper.",
			"Mogno:\nThe Mogno race are known for their sharp calws and short temper.",
 	};
 	private int selectedImage = 0;
 	
 	private ImageView pictures;
	private TextView name;
 	private TextView description;
 	private SimpleGestureFilter detector; 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		if(Everbie.exists()){
 			Intent main = new Intent("com.cheesymountain.woe.MAINACTIVITY");
 			startActivity(main);
 		}
 		setContentView(R.layout.activity_new_everbie);
 		description = ((TextView)findViewById(R.id.everbieLongText));
		description.setText(imageDescription[0]);
 		pictures = (ImageView)findViewById(R.id.everbiePicsImageView);
 		pictures.setImageResource(imageId[0]);
		name = (TextView)findViewById(R.id.everbieNameText);
 
         detector = new SimpleGestureFilter(this,this);
 	}
 	
 	public void create(View view){
 		String name = this.name.getText().toString();
 		Everbie.createEverbie(name, getResources().getResourceEntryName(imageId[selectedImage]));
 		Intent main = new Intent("com.cheesymountain.woe.MAINACTIVITY");
 		startActivity(main);
 	}
 
 
 	public void onSwipe(int direction){
 		switch(direction){
 			case SimpleGestureFilter.SWIPE_RIGHT:
 				if(selectedImage > 0){
 					pictures.setImageResource(imageId[--selectedImage]);
 				}
 				break;
 			case SimpleGestureFilter.SWIPE_LEFT:
				if(selectedImage < imageId.length){
 					pictures.setImageResource(imageId[++selectedImage]);
 				}
 				break;
 		}
		description.setText(imageDescription[selectedImage]);
 	}
 	
 	public void exit(View view){
 		System.exit(0);
 	}
 
 	public void onDoubleTap() {
 		/* Do nothing */
 	}
 	
 	public boolean dispatchTouchEvent(MotionEvent me){ 
 		this.detector.onTouchEvent(me);
 		return super.dispatchTouchEvent(me); 
 	}
 	
 }
