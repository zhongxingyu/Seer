 package net.analogyc.wordiary;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.Menu;
 import net.analogyc.wordiary.models.DBAdapter;
 
 import java.io.InputStream;
 
 public class ImageActivity extends Activity {
 	private int dayId;
 	private DBAdapter dataBase;
 	private ImageWebView imageWebView;
 	private float scale = 1.f;
 	private float relativeX;
 	private float relativeY;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_image);
 
 		if (dayId == 0) {
 			Intent intent = getIntent();
 			dayId = intent.getIntExtra("dayId", -1);
 		}
 
 		dataBase = new DBAdapter(this);
 		imageWebView = (ImageWebView) findViewById(R.id.imageWebView);
 	}
 
 
 	public void setView() {
 		InputStream imageStream;
 		Bitmap image;
 		String location;
 		if (dayId == -1) {
 			location = "file://android_asset/default-avatar.jpg";
 		} else {
 			Cursor c = dataBase.getDayById(dayId);
 			c.moveToFirst();
 			location = "file://" + c.getString(1);
 			c.close();
 		}
 
 		imageWebView.setImage(location);
 	}
 
 	@Override
 	public void onBackPressed() {
 
		if (imageWebView.getScale() != 1f) {
			imageWebView.setScaleX(1f);
			imageWebView.setScaleY(1f);
		} else {
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.entry, menu);
 		return true;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
 		super.onSaveInstanceState(outState);
 		outState.putInt("dayId", dayId);
 		outState.putFloat("scale", scale);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState){
 		super.onRestoreInstanceState(savedInstanceState);
 		if(savedInstanceState.containsKey("dayId")){
 			dayId = savedInstanceState.getInt("dayId");
 			scale = savedInstanceState.getFloat("scale");
 		}
 	}
 
 	@Override
 	protected void onPause(){
 		dataBase.close();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume(){
 		super.onResume();
 		setView();
 	}
 }
