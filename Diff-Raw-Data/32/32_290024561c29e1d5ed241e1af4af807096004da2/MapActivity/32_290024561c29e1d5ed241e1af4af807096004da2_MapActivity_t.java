/*//<<<<<<< HEAD
 package ca.skule.froshapplication;
 
 import ca.skule.froshapplication.PanAndZoomListener;
 import ca.skule.froshapplication.PanAndZoomListener.Anchor;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 
 public class MapActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 
 	    FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams (FrameLayout.LayoutParams.MATCH_PARENT, 
 	                                                                FrameLayout.LayoutParams.MATCH_PARENT, 
 	                                                                Gravity.TOP | Gravity.LEFT);
 	    FrameLayout view = new FrameLayout (this);
 	    setContentView (view);
 
 	    ImageView imageView = new ImageView(this);
 	    //Use line below for large images if you have hardware rendering turned on
 	    //imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 	    // Line below is optional, depending on what scaling method you want
 	    view.addView(imageView, fp);
 
 	    imageView.setScaleType(ImageView.ScaleType.MATRIX);
 	    view.setOnTouchListener(new PanAndZoomListener(view, imageView, Anchor.TOPLEFT));
 
 	    imageView.setImageResource (R.drawable.u_of_t_map);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map, menu);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		
 		return true;
 	}
 
 	public void onMenuClick (MenuItem Item){
 		String id = Item.getTitle().toString();
 		if (id.equalsIgnoreCase("schedule")){
 			Intent intent = new Intent (this, DayListActivity.class);
 			startActivity (intent);
 		}
 		else if (id.equalsIgnoreCase("map")){
 			// Point to Map
 		}
 	}
 }
 =======
*/
 package ca.skule.froshapplication;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MapActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map, menu);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		
 		return true;
 	}
 
 	public void onMenuClick (MenuItem Item){
 		String id = Item.getTitle().toString();
 		if (id.equalsIgnoreCase("schedule")){
			Intent intent = new Intent (this, DayListActivity.class);
			startActivity (intent);
 		}
 		else if (id.equalsIgnoreCase("map")){
 			// Point to Map
 		}
 		else if (id.equalsIgnoreCase("locations")){
 			// Point to Building List Activity
 			Intent intent = new Intent (this,ListsActivity.class);
 			startActivity(intent);
 		}
 	}
 }

