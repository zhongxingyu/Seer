 package com.example.foundit;
 
 import android.os.Bundle;
import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 
 public class FullscreenImage extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.foundit_final_small);
		actionBar.setTitle("");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(117, 150, 194)));
		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_fullscreen_image);
 		Intent info = getIntent();
 		Bitmap pic = (Bitmap) this.getIntent().getExtras().getParcelable("IMAGE");
 		//ImageView image = new ImageView()
 		ImageView img = (ImageView) findViewById(R.id.imageView1);
 		img.setImageBitmap(pic);
 		//img.setImageResource(R.drawable.clouds);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.///
 		getMenuInflater().inflate(R.menu.activity_fullscreen_image, menu);
 		return true;
 	}
 	public void close(View view){
 		finish();
 	}
 
 }
