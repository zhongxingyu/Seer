 package com.junj.imagerssreader;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ItemDetailActivity extends Activity {
 	private TextView mTitle;
 	private TextView mDescription;
 	private ImageView mImage;
 	
 	@Override
 	public void onCreate(Bundle saveInstanceState) {
 		super.onCreate(saveInstanceState);
 		setContentView(R.layout.item_detail);
 		
 		Intent intent = getIntent();
 		
 		String title = intent.getStringExtra("TITLE");
 		String description = intent.getStringExtra("DESCRIPTION");
		String imageURL = intent.getStringExtra("IMAGEURL");
 		
 		mTitle = (TextView) findViewById(R.id.item_detail_title);
 		mDescription = (TextView) findViewById(R.id.item_detail_descr);
 		
 		mImage = (ImageView) findViewById(R.id.item_detail_image);
 		
 		mTitle.setText(title);
 		mDescription.setText(description);
 		
		WebImageLoaderTask task = new WebImageLoaderTask(mImage, imageURL);
		task.execute(imageURL);
 	}
 
 }
