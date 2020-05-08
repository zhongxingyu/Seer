 package com.zeyomir.ocfun.gui;
 
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.ImageView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.MenuItem;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.controller.DisplayImage;
 import com.zeyomir.ocfun.model.Image;
 import org.holoeverywhere.app.Activity;
 import org.holoeverywhere.widget.TextView;
 
 public class SingleImage extends Activity {
 	private Image image;
 	private ActionBar actionBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.image);
 
 		image = DisplayImage.getImage(getIntent());
 		actionBar = getSupportActionBar();
 		setActionBarOptions();
 		fillData();
 	}
 
 	private void setActionBarOptions() {
 		actionBar.hide();
 	}
 
 	private void fillData() {
 		String path = Environment.getExternalStorageDirectory().toString()
 				+ "/OCFun/" + image.path;
 		Log.i("Image", path);
 		((ImageView) findViewById(R.id.imageView1))
 				.setImageBitmap(BitmapFactory.decodeFile(path));
		((TextView) findViewById(R.id.image_title)).setText(image.name);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				Intent intent = new Intent(this, OCFun.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
