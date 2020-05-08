 package com.mitfahr;
 
 import com.mitfahr.database.Ride;
 import com.mitfahr.database.RideModel;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class ActivityOfferRide extends Activity {
 
 	
 	private String from = "";
 	private String to = "";
 	private String time = "";
 	private Integer seats = 0;
 	private String desc = "";
 	private String date = "";
 	private int costs;
 	private String phone = "";
 	private String name = "";
 	private String email = "";
 	private int hour;
 	private int minute;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_offerride);
 
 		Button button_weiter = (Button) findViewById(R.id.btn_activityOfferride_next);
 
 		button_weiter.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 
 				Intent inte = new Intent(ActivityOfferRide.this,
 						ActivityCheckTrip.class);
 				try {
 
 					final EditText m_edit_from = (EditText) findViewById(R.id.txt_activityOfferride_from);
 					from = m_edit_from.getText().toString();
 
 					final EditText m_edit_to = (EditText) findViewById(R.id.txt_activityOfferride_to);
 					to = m_edit_to.getText().toString();
 
 					final EditText m_edit_date = (EditText) findViewById(R.id.txt_activityOfferride_date);
 					date = m_edit_date.getText().toString();
 
 					final EditText m_edit_time = (EditText) findViewById(R.id.txt_activityOfferride_time);
 					time = m_edit_time.getText().toString();
 
 					final EditText m_edit_seats = (EditText) findViewById(R.id.txt_activityOfferride_size);
 					seats = Integer.parseInt(m_edit_seats.getText().toString());
 
 					final EditText m_edit_desc = (EditText) findViewById(R.id.txt_activityOfferride_desc);
 					desc = m_edit_desc.getText().toString();
 
 				} catch (Exception E) {
 					Toast.makeText(v.getContext(),
 							"Bitte alle Felder ausfüllen",
 							Toast.LENGTH_SHORT).show();
 				}
 
 				Log.d("DEBUG ActivityTrip: from", from);
 				Log.d("DEBUG ActivityTrip: to", to);
 				Log.d("DEBUG ActivityTrip: date", date);
 				Log.d("DEBUG ActivityTrip: time", time);
 				Log.d("DEBUG ActivityTrip: seats", seats.toString());
 				Log.d("DEBUG ActivityTrip: desc", desc);
 
 				DateValidator val = new DateValidator();
 
 				if(!date.equalsIgnoreCase("") && !time.equalsIgnoreCase(""))
 				{
 
 					if (val.isThisDateValid(date, "dd.MM.yyyy") == false) 
 					{
 						Log.d("Date not valid........", "bla");
 						Toast.makeText(v.getContext(),
 								"Bitte Datum im Format dd.MM.yyyy eingeben!",
 								Toast.LENGTH_SHORT).show();
 					}
 	
					else if (val.isThisDateValid(time, "hh:mm") == false) 
 					{
 						Log.d("Time not valid........", "bla");
 						Toast.makeText(v.getContext(),
 								"Bitte Zeit im Format hh:mm eingeben!",
 								Toast.LENGTH_SHORT).show();
 					}
	
 					else 
 					{
 						inte.putExtra("from", from);
 						inte.putExtra("to", to);
 						inte.putExtra("date", date);
 						inte.putExtra("time", time);
 						inte.putExtra("seats", seats.toString());
 						inte.putExtra("desc", desc);
 	
 						
 						//int dateParts[] = Utils.getInstance().splitDate(date);
 						
 
 						ActivityOfferRide.this.startActivity(inte);
 					}
 
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 
 }
