 package com.sjsu.mobilebikelet;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import com.sjsu.mobilebikelet.dto.Transaction;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 
 public class ViewStatsFormActivity extends Activity {
 	//http://www.bart.gov/guide/carbon.aspx
 	private final double CO2_PER_GALLON_IN_POUNDS = 19.592;
 	private final double AVG_MILES_PER_GALLON = 21.0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_statistics_form);
 		Spinner spinner = (Spinner) findViewById(R.id.tripTypeId);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.tripType, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         spinner.setAdapter(adapter);
 				
 		
 //		
 //		TextView checkinto = (TextView) findViewById(R.id.checkinto);
 //		checkinto.setText(transaction.getTransaction().getToStation());
 //		
 //		TextView checkintime = (TextView) findViewById(R.id.checkinat);
 //		checkintime.setText(transaction.getTransaction().getRentEndDate());
 		
 //		TextView totalcharges = (TextView) findViewById(R.id.bikeinfo);
 //		bikeinfo.setText(transaction.getTransaction().get);
 		Button button = (Button) findViewById(R.id.submitStatInfoButtonId);
 		button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 
 				TextView dcLabelId = (TextView) findViewById(R.id.dcInputId);
 				TextView weightInputId = (TextView) findViewById(R.id.weightInputId);
 				TextView minutesInputId = (TextView) findViewById(R.id.minutesInputId);
 				Spinner tripType = (Spinner) findViewById(R.id.tripTypeId);
 				String tripTypeValue = tripType.getSelectedItem().toString();
 				
 				String distanceCoveredStr = dcLabelId.getText().toString();
 				String weight = weightInputId.getText().toString();
 				String minutes = minutesInputId.getText().toString();
 				if(distanceCoveredStr == null || distanceCoveredStr.trim().length() ==0){
 					dcLabelId.setError("Invalid Distance");
 					return;
 				} else if (weight == null || weight.trim().length() == 0){
 					weightInputId.setError("Invalid Weight");
 					return;
 				} else if (minutes == null || minutes.trim().length() == 0){
 					minutesInputId.setError("Invalid Minutes");
 					return;
 				}
				
 				double calorieBurned = calculateCaloriesBurned(Integer.parseInt(weight)
 						, Integer.parseInt(minutes), Double.parseDouble(distanceCoveredStr), tripTypeValue);
 				
 				double co2Saved = calculateCo2Saved(Integer.parseInt(minutes), Double.parseDouble(distanceCoveredStr));
 				
 				Intent intent = new Intent(ViewStatsFormActivity.this, ViewStatsActivity.class);
                 
                 DecimalFormat df = new DecimalFormat("#.00");
                 intent.putExtra("caloriesBurned", df.format(calorieBurned));
                 intent.putExtra("co2Saved", df.format(co2Saved));
                 startActivity(intent);
 				
 			}
 
 			
 		});
 			
 	}
 	
 	private double calculateCaloriesBurned(int weight, int minutes, double miles, String tripType){
		
 		double mph = miles*60.0/minutes;
 		int gradiance = 0;
 		if ("Leisurely".equalsIgnoreCase(tripType)){
 			gradiance = 4;
 		} else if ("Moderate".equalsIgnoreCase(tripType)){
 			gradiance = 8;
 		} else {
			gradiance = 12;
 		}
 		//Formula from the internet resource http://www.ehow.com/about_5417938_calories-burned-riding-bicycle.html
		double caloriesBurned = (mph*weight*(.0053 + gradiance/100) + .0083*(Math.pow(mph, 3)))*7.2;
 		
 		return caloriesBurned;
 	}
 	
 	private double calculateCo2Saved(int minutes, double miles){
 		double mph = miles*60.0/minutes;
 		double co2Saved = mph/AVG_MILES_PER_GALLON * CO2_PER_GALLON_IN_POUNDS;
 		return co2Saved;
 	}
 }
