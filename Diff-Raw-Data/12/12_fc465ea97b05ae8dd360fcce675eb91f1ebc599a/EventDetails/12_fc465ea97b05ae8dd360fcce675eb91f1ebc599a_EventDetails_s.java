 package com.appathon.letsgo;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class EventDetails extends Activity {
 
 	int eventID;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event_details);
 		Button button = (Button)findViewById(R.id.btn_sel_ride);
         Button b = (Button)findViewById(R.id.button2);
         int i = getIntent().getExtras().getInt("extra");
         //int i = savedInstanceState.getInt("extra");
 
         Event event = HTTPHelper.es[i];
 		
        eventID = savedInstanceState.getInt("event");
         
        Event e = HTTPHelper.GetEvent(eventID);
 
        ((TextView)findViewById(R.id.txtDate)).setText(e.getTime().toString());
        ((TextView)findViewById(R.id.txtLoc)).setText(e.getLocation());
        ((TextView)findViewById(R.id.txtCost)).setText(e.getCost());
        ((TextView)findViewById(R.id.txtPOC)).setText(e.getPOC());
        
         button.setOnClickListener(new View.OnClickListener() {
  
             public void onClick(View arg0) {
                 Intent myIntent = new Intent(EventDetails.this, AttendanceSelect.class);
                 Bundle ex = new Bundle();
                 ex.putInt("event", eventID);
                 myIntent.putExtras(ex);
                 EventDetails.this.startActivity(myIntent);
                 // TODO Auto-generated method stub
             }
         });
         
         b.setOnClickListener(new View.OnClickListener() {
         	 
             public void onClick(View arg0) {
                 Intent myIntent = new Intent(EventDetails.this, AttendanceList.class);
                 Bundle ex = new Bundle();
                 ex.putInt("event", eventID);
                 myIntent.putExtras(ex);
                 EventDetails.this.startActivity(myIntent);
                 // TODO Auto-generated method stub
             }
         });
 
         ((TextView)findViewById(R.id.date_event_details)).setText(event.getStartTime().toString());
         ((TextView)findViewById(R.id.cost_event_view)).setText(event.getCost().toString());
         ((TextView)findViewById(R.id.event_location_view)).setText(event.getLocation().toString());
         ((TextView)findViewById(R.id.time_event_view)).setText(event.getTime().toString());
         ((TextView)findViewById(R.id.poc_event_view)).setText(event.getPOC().toString());
 
 
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.event_details, menu);
 		return true;
 	}
 
 }
