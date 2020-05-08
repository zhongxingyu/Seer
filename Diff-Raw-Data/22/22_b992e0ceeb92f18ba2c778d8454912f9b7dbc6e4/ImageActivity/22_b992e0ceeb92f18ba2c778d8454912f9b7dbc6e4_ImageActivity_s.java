 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ImageView;
 
 public class ImageActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_image);
 		Intent intent = getIntent();
 		String path = intent.getStringExtra(GalleryActivity.EXTRA_MESSAGE);
 		ImageView img = (ImageView)findViewById(R.id.img);
 		img.setImageURI(Uri.parse(path));
 	}
 
 }
