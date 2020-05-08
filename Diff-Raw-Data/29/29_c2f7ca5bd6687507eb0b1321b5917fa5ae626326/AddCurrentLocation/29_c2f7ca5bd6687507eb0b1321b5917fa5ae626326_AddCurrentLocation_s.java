 package com.jeremyhaberman.playgrounds;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 public class AddCurrentLocation extends Activity implements OnClickListener, Runnable {
 
 	private EditText nameText;
 	private EditText descriptionText;
 	private View addButton;
 	private ProgressDialog progressDialog;
 
 	private Location myLocation;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.addcurrentlocation);
 
 		nameText = (EditText) findViewById(R.id.name);
 		descriptionText = (EditText) findViewById(R.id.description);
 
 		LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		String provider = manager.getBestProvider(criteria, true);
 		myLocation = manager.getLastKnownLocation(provider);
 
 		addButton = findViewById(R.id.add_button);
 		addButton.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		progressDialog = ProgressDialog.show(AddCurrentLocation.this, "",
 				"Adding playground...", true);
 		progressDialog.show();
 		Thread thread = new Thread(AddCurrentLocation.this);
 		thread.start();
 	}
 
 	private Handler handler = new Handler() {
 		public void handleMessage(Message message) {
 			progressDialog.dismiss();
 			
 			String result;
 			if(message.what == 0) {
 				result = "Playground added.";
 				Playgrounds.setNewPlaygrounds(true);
 			} else {
 				result = "Failed to add playground.";
 			}
 			showResult(result);
 			
 		}
 	};
 	
 	final Runnable goHome = new Runnable() {
 		public void run() {
 			goHome();
 		}
 	};
 
 	@Override
 	public void run() {
 		addCurrentLocation();
 	}
 
 	protected void goHome() {
 		Intent goHomeIntent = new Intent(this, Playgrounds.class);
 		goHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(goHomeIntent);
 	}
 
 	protected void showResult(String result) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(result);
 		builder.setCancelable(false);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				AddCurrentLocation.this.finish();
 			}
 		});
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private void addCurrentLocation() {
 		String name = nameText.getText().toString().trim();
 		String description = descriptionText.getText().toString().trim();
 		int latitude = (int) (myLocation.getLatitude() * 1E6);
 		int longitude = (int) (myLocation.getLongitude() * 1E6);
 
 		PlaygroundDAO playgroundDAO = new WebPlaygroundDAO(getParent());
 		int result = playgroundDAO.createPlayground(name, description,
 				latitude, longitude);
 		handler.sendEmptyMessage(result);
 //		handler.post(goHome);
 	}
 }
