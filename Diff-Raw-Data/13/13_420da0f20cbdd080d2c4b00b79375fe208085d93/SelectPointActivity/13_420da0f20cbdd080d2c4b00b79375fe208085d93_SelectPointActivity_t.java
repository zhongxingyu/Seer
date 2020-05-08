 package edu.virginia.cs.sgd.gpsgames;
 
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 
 public class SelectPointActivity extends MapActivity implements PointRequestor {
 
 	private String name;
 	private LatLng point;
 	private Button select;
 	
 	@Override
 	public int getLayout() {
 		return R.layout.activity_select;
 	}
 	
 	@Override
 	public void setUpUI() {
 		select = (Button) findViewById(R.id.point_select_button);
 		
 		select.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				moveToRaceSetupActivity();
 			}
 			
 		});
 		
 		String ex = getIntent().getExtras().getString("Name");
 		
 		name = (ex == null ? "" : ex);
 	}
 
 	@Override
 	public void onClick(LatLng point) {
 		PointPicker picker = new PointPicker(this);
 		
 		picker.pickPoint(point);
 	}
 	
 	public void moveToRaceSetupActivity() {
		
 		//REPLACE MenuActivity.clss BELOW WITH CLASS YOU WANT TO GO TO
 		Intent mainActivity = new Intent(this, RaceSetupActivity.class);
 		
 		mainActivity.putExtra("Name", name);
 		double[] pointArr = {point.latitude, point.longitude};
 		mainActivity.putExtra("Point", pointArr);
 		
		map.clear();
		point = null;
		
 		startActivity(mainActivity);
 	}
 
 	@Override
 	public void pointCheck(LatLng point, boolean valid) {
 		
 		TextView text = (TextView) findViewById(R.id.info_text);
 		if(valid) {
 			map.clear();
 			
 			this.point = point;
 			addMarker(point, "Current Point", BitmapDescriptorFactory.HUE_RED);
 
 			text.setText("Point Selected!");
 		}
 		else {
 			
 			text.setText("Point Invalid.");
 			
 		}
 		
 	}
 	
 }
