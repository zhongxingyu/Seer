 package com.axprint.official;
 
 import android.app.Activity;
import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.axprint.official.backgroundUtilites.HttpImageDownloader;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.FailReason;
 import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
 
 public class MainPageActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_page);
 		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
 				this.getApplicationContext()).imageDownloader(
 				new HttpImageDownloader()).build();
 		ImageLoader.getInstance().init(config);
 		ImageView profilePic = (ImageView) findViewById(R.id.main_profile_picture);
 		DisplayImageOptions options = new DisplayImageOptions.Builder()
 				.showStubImage(R.drawable.ic_launcher)
 				.showImageForEmptyUri(R.drawable.ic_launcher).build();
 		ImageLoader.getInstance().displayImage(
 				this.getIntent().getStringExtra("Pic"), profilePic, options,new ImageLoadingListener() {
 
 					@Override
 					public void onLoadingStarted(String imageUri, View view) {
 					}
 
 					@Override
 					public void onLoadingFailed(String imageUri, View view,
 							FailReason failReason) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public void onLoadingComplete(String imageUri, View view,
 							Bitmap loadedImage) {
 					}
 
 					@Override
 					public void onLoadingCancelled(String imageUri, View view) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 				});
 		TextView name = (TextView) findViewById(R.id.main_name);
 		name.setText(this.getIntent().getStringExtra("Name"));
 		TextView cartAmount = (TextView) findViewById(R.id.main_cart_amount);
 		cartAmount.setText(this.getIntent().getStringExtra("Cart"));
 		Button gallery = (Button) findViewById(R.id.main_gallery);
 		Button discount = (Button) findViewById(R.id.main_discount);
 		Button report = (Button) findViewById(R.id.main_report);
 		Button exit = (Button) findViewById(R.id.main_exit);
 
 		gallery.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 			}
 		});
 		discount.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 			}
 		});
 		report.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 			}
 		});
 		exit.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
				MainPageActivity.this.logOut();
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main_page, menu);
 		return true;
 	}
	
	private void logOut() {
		Intent nextAcitivity = new Intent(this,LoginActivity.class);
		startActivity(nextAcitivity);
		finish();
	}
 
 }
