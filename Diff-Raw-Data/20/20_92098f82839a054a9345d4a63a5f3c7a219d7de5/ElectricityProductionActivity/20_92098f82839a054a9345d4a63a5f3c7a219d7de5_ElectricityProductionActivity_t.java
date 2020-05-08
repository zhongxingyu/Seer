 package com.qut.spc;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.qut.spc.service.LocationService;
 import com.qut.spc.task.AddressTask;
 
 /**
  * Activity for Calculation of Electricity Production  
  * @author QuocViet
  */
 public class ElectricityProductionActivity extends Activity {
 	private EditText etSystemCost, etPanelOutput, etPanelEfficiency,
 		etInverterEfficiency, etPostcode, etEnergyUsage;
 	
 	private TextView tvAddress;
 	
 	private LocationService locationService;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_electricity_production);
 		
 		etPostcode = (EditText) findViewById(R.id.postcode);
 		etSystemCost = (EditText) findViewById(R.id.system_cost);
 		etEnergyUsage = (EditText) findViewById(R.id.energy_usage);
 		etPanelOutput = (EditText) findViewById(R.id.panel_output);
 		etPanelEfficiency = (EditText) findViewById(R.id.panel_efficiency);
 		etInverterEfficiency = (EditText) findViewById(R.id.inverter_efficiency);
 
 		tvAddress = (TextView) findViewById(R.id.address);
 
 		locationService = new LocationService(this) {
 			@Override
 			public void onLocationChanged(Location location) {
 				super.onLocationChanged(location);
 				if (location != null) {
 					new AddressTask(tvAddress, etPostcode)
 							.execute(getLatitude(), getLongitude());
 				} else {
 					MainActivity.showError(ElectricityProductionActivity.this,
 							"Could not get location");
 				}
 			}
 		};
 		if (locationService.getLatitude() != 0 && locationService.getLongitude() != 0) {
 			new AddressTask(tvAddress, etPostcode)
 					.execute(locationService.getLatitude(), locationService.getLongitude());
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_electricity_production, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.update_location:
 			updatePostcode();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case MainActivity.REQUEST_CODE_MAP:
 			if (resultCode == Activity.RESULT_OK && data.getExtras() != null) {
 				double latitude = data.getExtras().getDouble("latitude");
 				double longitude = data.getExtras().getDouble("longitude");
 				if (latitude != 0 && longitude != 0) {
 					new AddressTask(tvAddress, etPostcode)
 							.execute(latitude, longitude);
 				}
 			}
 			break;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		locationService.cancelUpdateLocation();
		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		locationService.cancelUpdateLocation();
 		super.onStop();
 	}
 
 	public void onButtonClick(View v) {
 		switch(v.getId()) {
 		case R.id.btCalculate:
 			calculate();
 			break;
 		case R.id.btMap:
 			MainActivity.getLocationFromMap(this);
 			break;
 		}
 	}
 	
 	private void calculate() {
 		if (etSystemCost.getText().length() == 0) {
 			MainActivity.showError(this, "System cost must not be empty");
 			return;
 		}
 		if (etEnergyUsage.getText().length() == 0) {
 			MainActivity.showError(this, "Energy usage must not be empty");
 			return;
 		}
 		if (etPanelOutput.getText().length() == 0) {
 			MainActivity.showError(this, "Panel output must not be empty");
 			return;
 		}
 		if (etPanelEfficiency.getText().length() == 0) {
 			MainActivity.showError(this, "Panel efficiency must not be empty");
 			return;
 		}
 		if (etInverterEfficiency.getText().length() == 0) {
 			MainActivity.showError(this, "Inverter efficiency must not be empty");
 			return;
 		}
 		
 		String query = "";
 		query += "systemCost=" + etSystemCost.getText().toString() + "&";
 		query += "energyConsumption=" + etEnergyUsage.getText().toString() + "&";
 		query += "panelOutput=" + etPanelOutput.getText().toString() + "&";
 		query += "panelEfficiency=" + etPanelEfficiency.getText().toString() + "&";
 		query += "inverterEfficiency=" + etInverterEfficiency.getText().toString() + "&";
 
 		if (etPostcode.getText().length() > 0) {
 			query += "postcode=" + etPostcode.getText().toString() + "&";
 		}
 		
 		showCalculationResult(getString(R.string.app_url) + "/calculate?" + query);
 	}
 	
 	private void updatePostcode() {
 		if (!locationService.updateLocation()) {
 			MainActivity.showError(this, "Could not get location. Please enable your GPS");
 		}
 	}
 	
 	private void showCalculationResult(String url) {
 		Intent i = new Intent(this, CalculationResultActivity.class);
 		i.putExtra("url", url);
 		startActivity(i);
 	}
 }
