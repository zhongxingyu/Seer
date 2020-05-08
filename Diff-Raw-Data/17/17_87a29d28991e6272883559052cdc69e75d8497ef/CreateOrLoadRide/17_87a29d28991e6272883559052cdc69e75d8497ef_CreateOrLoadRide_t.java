 package no.ntnu.idi.socialhitchhiking.journey;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.map.MapActivityCreateOrEditRoute;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 /**
  * This is a menu where the driver can choose to create new drive or to reuse previous drive.
  * The GUI is defined in create_or_load_ride.xml
  * @author Made Ziius
  *
  */
 public class CreateOrLoadRide extends SocialHitchhikingActivity {
 
	public void onBackPressed() {
		finish();
	}
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.create_or_load_ride);
 	}
 	
 	public void onReuseRideClick(View v) {
 		Intent intent = new Intent(CreateOrLoadRide.this, ScheduleDrive.class);
 		startActivity(intent);
 	}
 	public void onScheduleNewRideClick(View v){
 		Intent intent = new Intent(CreateOrLoadRide.this, MapActivityCreateOrEditRoute.class);
 		startActivity(intent);
 	}
 	
 }
