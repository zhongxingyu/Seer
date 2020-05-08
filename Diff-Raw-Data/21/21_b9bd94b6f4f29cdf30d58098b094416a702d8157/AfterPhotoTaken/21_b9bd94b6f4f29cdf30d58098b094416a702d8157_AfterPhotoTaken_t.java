 package com.bubblebot;
 
import java.io.File;

 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AfterPhotoTaken extends Activity {
 
 	ImageView image;
 	TextView text;
 	Button retake;
 	Button process;
 	Bitmap bm;
 	String dir = "/sdcard/BubbleBot/capturedImages/";
 	String filename = "";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.after_photo_taken);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			filename = extras.getString("file");
 		}
 
 		text = (TextView) findViewById(R.id.text);
 		text.setText("Displaying " + dir + filename);
 
 		image = (ImageView) findViewById(R.id.image);
 		bm = BitmapFactory.decodeFile(dir + filename);
 		image.setImageBitmap(bm);
 
 		retake = (Button) findViewById(R.id.retake_button);
 		retake.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
				// Delete photo
				File photoFile = new File(dir + filename);
				photoFile.delete();
 				// Start process form algorithm
 				Intent intent = new Intent(getApplication(),
 						BubbleCollect.class);
 				startActivity(intent);
 				finish();
 			}
 		});
 
 		process = (Button) findViewById(R.id.process_button);
 		process.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Start process form algorithm
 				Intent intent = new Intent(getApplication(),
 						BubbleProcess.class);
 				intent.putExtra("file", filename);
 				startActivity(intent);
 				finish();
 			}
 		});
 	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// Delete photo
		File photoFile = new File(dir + filename);
		photoFile.delete();
	}
 }
