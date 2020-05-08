 package com.example.foundit;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 
 public class FullscreenImage extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
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
