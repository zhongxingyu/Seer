 package edu.mit.rerun.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.mit.rerun.R;
 import edu.mit.rerun.client.Client;
 import edu.mit.rerun.client.ClientException;
 import edu.mit.rerun.model.ReuseItem;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 
 /**
  * This class is the Activity for posting an item. User should input title,
  * description, location. Post will be emailed to reuse when user presses "Post"
  * button.
  * 
  * Menu Item:
  * 
  * @param <ReuseItem>
  * 
  */
 public class PostItemActivity<ReuseItem> extends Activity implements
 LocationListener {
 
 	private int lat = 00000;
 	private int lon = 00000;
 	private LocationManager mlocationManager;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.postitem);
 
 		mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		if (mlocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			mlocationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 20 * 1000, 5, this);
 		}
 		if (mlocationManager
 				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			mlocationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 0, 0, this);
 		}
 
 		Button postButton = (Button) findViewById(R.id.post_button);
 		final EditText itemTitle = (EditText) findViewById(R.id.item_title);
 		final EditText itemDescription = (EditText) findViewById(R.id.edit_description);
 		final EditText itemLocation = (EditText) findViewById(R.id.item_location);
 		//        ReuseItem item = new ReuseItem("stubid", "stubsender",
 				//                itemTitle.getText().toString(), itemDescription
 		//                        .getText().toString(), itemLocation.getText()
 		//                        .toString(), "stubtime", 0, 0);
 		postButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO
 				// Toast.makeText(v.getContext(), "test",
 				// Toast.LENGTH_SHORT).show();
 				// Toast.makeText(getApplicationContext(),
 				// "Item Posted \n " + "Lat: " + lat + " Lon:" + lon,
 				// Toast.LENGTH_SHORT).show();
 
 				String itemTitleStr = itemTitle.getText().toString();
 				String itemLocationStr = itemLocation.getText().toString();
 
				if (itemTitleStr.matches("")) {
					//error- empty string
					Toast.makeText(v.getContext(), "Please enter an item name", Toast.LENGTH_SHORT).show();
 
 				}
 
				else if (itemLocationStr.matches("")) {
					//error- empty string
					Toast.makeText(v.getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
 
 				} 
 
 				else {
 
 					int latitude = 0;
 					int longitude = 0;
 
 					Location loc = mlocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
 					if (loc == null) {
 						loc = mlocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 					}
 					if (((int) (loc.getLatitude() * 1E6)) != 0) {
 						latitude = (int)(loc.getLatitude() * 1E6);
 					}
 					if ((int) (loc.getLongitude() * 1E6) != 0) {
 						longitude = (int) (loc.getLongitude() * 1E6);
 					}
 					PostTask post = new PostTask();
 					post.execute(new edu.mit.rerun.model.ReuseItem("stubid", "stubsender",
 							itemTitle.getText().toString(), itemDescription
 							.getText().toString(), itemLocation.getText()
 							.toString(), "stubtime", latitude, longitude));
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20*1000, 10, this);
 		mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mlocationManager.removeUpdates(this); //<8>
 	}
 
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		Toast.makeText(getApplicationContext(), "provider DISABLED",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		Toast.makeText(getApplicationContext(), "provider ENABLED",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public class PostTask extends AsyncTask<edu.mit.rerun.model.ReuseItem, byte[], Boolean> {
 		private ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			dialog = ProgressDialog.show(PostItemActivity.this, "",
 					"Posting Item to ReRun", true);
 		}
 
 		@Override
 		protected Boolean doInBackground(edu.mit.rerun.model.ReuseItem... params) {
 			boolean success = false;
 			try {
 				success = Client.postItem("", params[0]);
 			} catch (ClientException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return success;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean success) { // happens in
 			// UI thread
 			// Bad practice, but meh, it'd be better if java had tuples
 			if (!success) {
 				Toast.makeText(getApplicationContext(),
 						"Error posting, please try again later",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(), "Post Success",
 						Toast.LENGTH_SHORT).show();
 			}
 			dialog.dismiss();
 			finish();
 		}
 
 
 	}
 }
