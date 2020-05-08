 package ch.hsr.hsrbuddy.activity.map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import ch.hsr.hsrbuddy.R;
 import ch.hsr.hsrbuddy.activity.SettingsActivity;
 import ch.hsr.hsrbuddy.view.ImageMap;
 import ch.hsr.hsrbuddy.view.ImageMap.OnImageMapClickedHandler;
 
 public class MapActivity extends Activity {
 	ImageMap mImageMap;
 	Drawable hsrmap;
 	
 	OnImageMapClickedHandler handler = new ImageMap.OnImageMapClickedHandler() {
 		public void onImageMapClicked(int id) { changeMap(id); }
 		public void onBubbleClicked(int id) { }
 	};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		mImageMap = (ImageMap) findViewById(R.id.map);
 		mImageMap.addOnImageMapClickedHandler(handler);
 	}
 	
 	private void changeMap(int id){
 		Intent intent = new Intent(this, MapActivityDetail.class);
 		Integer buildingId = 0;
     	switch (id)
     	{
     		case R.id.building1:
    			buildingId = R.drawable.geb1;
     	        break;
     		case R.id.building3:
    			buildingId = R.drawable.geb3;
     	        break;
     		case R.id.building5:
    			buildingId = R.drawable.geb5;
     	        break;
    		case R.id.building6:
    			buildingId = R.drawable.geb6;
    			break;
     	}
 		intent.putExtra("imageId", buildingId);
         startActivity(intent);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_map, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
