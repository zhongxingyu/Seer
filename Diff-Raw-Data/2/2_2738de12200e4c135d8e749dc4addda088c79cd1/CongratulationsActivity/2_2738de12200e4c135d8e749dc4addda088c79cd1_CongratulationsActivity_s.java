 package com.numbers;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.numbers.domain.ImageService;
 
 public class CongratulationsActivity extends Activity {
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_congratulations);
 		
		String congratulationsImageAddress = "https://dl.dropbox.com/u/4294426/baloons.jpg";
 		Log.d("Numbers", "porra A");
 		ImageView congratulationImage = (ImageView) findViewById(R.id.imageView1);
 		ImageService.newInstance(congratulationsImageAddress, congratulationImage).loadImage();
 		Log.d("Numbers", "porra B");
 	}
 	
 	public void newExercise(View view) {
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 
 	
 }
