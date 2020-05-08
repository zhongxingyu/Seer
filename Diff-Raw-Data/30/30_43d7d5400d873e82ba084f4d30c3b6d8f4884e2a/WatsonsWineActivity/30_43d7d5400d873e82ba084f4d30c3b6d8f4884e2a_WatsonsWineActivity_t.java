 package watsons.wine;
 
 import watsons.wine.utilities.WatsonWineDB;
 
 import watsons.wine.utilities.Registration;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
import android.graphics.Point;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.ViewGroup;
 import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
 
 public class WatsonsWineActivity extends Activity implements OnClickListener {
 	/** Called when the activity is first created. */
 
 	AsyncTask<Void, Void, Void> mRegisterTask;
 	ImageView icon_wine, icon_cellar, icon_food, icon_events, icon_location,
 			icon_inbox;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		if (width == 720)
		{
			LinearLayout ll = (LinearLayout) findViewById(R.id.main_ll1);
	        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)ll.getLayoutParams(); 
	        lp.setMargins(0, 300, 0, 0);
	        ll.setLayoutParams(lp);
	        
	        ll = (LinearLayout) findViewById(R.id.main_ll2);
	        lp = (RelativeLayout.LayoutParams)ll.getLayoutParams();
	        lp.setMargins(0, 80, 0, 0);
	        ll.setLayoutParams(lp);
	        
	        ll = (LinearLayout) findViewById(R.id.main_ll3);
	        lp = (RelativeLayout.LayoutParams)ll.getLayoutParams();
	        lp.setMargins(0, 80, 0, 0);
	        ll.setLayoutParams(lp);
		}
 
 		// **********Create My Cellars Table*************
 		WatsonWineDB temp = new WatsonWineDB();
 		temp.createOrOpenMyCellarTable(this);
 		// **********Create My Cellars Table*************
 
 		// **********Create Notification and notification count
 		// Table*************
 		Registration reg = new Registration();
 		reg.openDatabase(this);
 		// **********Create Notification and notification count
 		// Table*************
 		// **********Reqister GCM*************
 		mRegisterTask = reg.register_GCM(this);
 		// **********Reqister GCM*************
 
 		icon_wine = (ImageView) findViewById(R.id.icon_wine);
 		icon_cellar = (ImageView) findViewById(R.id.icon_cellar);
 		icon_food = (ImageView) findViewById(R.id.icon_food);
 		icon_events = (ImageView) findViewById(R.id.icon_events);
 		icon_location = (ImageView) findViewById(R.id.icon_location);
 		icon_inbox = (ImageView) findViewById(R.id.icon_inbox);
 
 		icon_wine.setOnClickListener(this);
 		icon_cellar.setOnClickListener(this);
 		icon_food.setOnClickListener(this);
 		icon_events.setOnClickListener(this);
 		icon_location.setOnClickListener(this);
 		icon_inbox.setOnClickListener(this);
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.v("Osmands", "onDestroy()");
 		if (mRegisterTask != null) {
 			boolean temp = mRegisterTask.cancel(true);
 			Log.v("Osmands", "mRegisterTask.cancel = " + temp);
 
 		}
 		// GCMRegistrar.onDestroy(this);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent = new Intent(v.getContext(), TabGroupView.class);
 		switch (v.getId()) {
 		case R.id.icon_wine:
 			intent.putExtra("index", 0);
 			break;
 		case R.id.icon_cellar:
 			intent.putExtra("index", 1);
 			break;
 		case R.id.icon_food:
 			intent.putExtra("index", 2);
 			break;
 		case R.id.icon_events:
 			intent.putExtra("index", 3);
 			break;
 		case R.id.icon_location:
 			intent.putExtra("index", 4);
 			break;
 		case R.id.icon_inbox:
 		    intent.putExtra("index", 5);
 		    break;
 		}
 		startActivity(intent);
 
 	}
 }
