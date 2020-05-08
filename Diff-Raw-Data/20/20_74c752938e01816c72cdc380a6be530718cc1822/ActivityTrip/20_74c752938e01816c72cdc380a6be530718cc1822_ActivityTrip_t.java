 package com.mitfahr;
 
 import com.mitfahr.R.layout;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.mitfahr.database.Fahrt;
 import com.mitfahr.database.FahrtenModel;
 
 
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
 
 public class ActivityTrip extends Activity {
 	
 	private String from;
 	private String to;
 	private String date;
 	private String time;
 	private Integer seats;
 	private String desc;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_trip);
 		
 		
		
 		Button button_weiter = (Button) findViewById(R.id.btn_weiter);
 		
 		button_weiter.setOnClickListener(new OnClickListener()
 	    {
 	      public void onClick(View v)
 	      {
 
 	    	  Intent inte = new Intent(ActivityTrip.this, ActivityCheckTrip.class);
 	    	 try{
	    		 
 	    	  final EditText m_edit_from = (EditText) findViewById(R.id.txt_trip_von);
 	    	  from = m_edit_from.getText().toString();
 	    	 
 	    	  final EditText m_edit_to = (EditText) findViewById(R.id.txt_bis);
 	    	  to = m_edit_to.getText().toString();
 	    	 
 	    	  final EditText m_edit_date = (EditText) findViewById(R.id.txt_date);
 	    	  date = m_edit_date.getText().toString();
 	    	  
 	    	  final EditText m_edit_time = (EditText) findViewById(R.id.txt_time);
 	    	  time = m_edit_time.getText().toString();
 	    	  
 	    	  final EditText m_edit_seats = (EditText) findViewById(R.id.txt_size);
 	    	  seats = Integer.parseInt(m_edit_seats.getText().toString());
 	    	  
 	    	  final EditText m_edit_desc = (EditText) findViewById(R.id.txt_beschreibung);
 	    	  desc = m_edit_desc.getText().toString();
	    	  
	    	  
 	    	 }
 	    	 catch(Exception E){
 	              Toast.makeText(v.getContext(),"Sitzplätze dürfen nicht leer sein", Toast.LENGTH_SHORT).show();
 	          }
	    	 
	    	
	    	 
 	    	Log.d("DEBUG ActivityTrip: from", from);
 	    	Log.d("DEBUG ActivityTrip: to", to);
 	    	Log.d("DEBUG ActivityTrip: date", date);
 	    	Log.d("DEBUG ActivityTrip: time", time);
 	    	Log.d("DEBUG ActivityTrip: seats", seats.toString());
 	    	Log.d("DEBUG ActivityTrip: desc", desc);
 	    	
	    	DateValidator val = new DateValidator();
	    	
 	    	if(from.length() < 1|| to.length() < 1 || date.length() < 1 ||time.length() < 1  || seats.equals(null)  )
 	    	{
 	    		Toast.makeText(v.getContext(),"Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
 	    	}
	    	
	    	else if( val.isThisDateValid(date, "dd.mm.yyyy") == false){
	    		Log.d("Date not valid........", "bla");
	    		Toast.makeText(v.getContext(),"Bitte Datum im Format dd.mm.yyyy eingeben!", Toast.LENGTH_SHORT).show();
	    	}

 	    	else
 	    	{
 	    		
 	    		inte.putExtra("from", from);
 	    		inte.putExtra("to", to);
 	    		inte.putExtra("date", date);
 	    		inte.putExtra("time", time);
 	    		inte.putExtra("seats", seats.toString());
 	    		inte.putExtra("desc", desc);
 	    		
 	    		ActivityTrip.this.startActivity(inte);
 	    	}
 	    	  
 	    	
 	    	
 
 	      }
 	    });
 		
 	 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_trip, menu);
 		return true;
 	}
 
 	
 	
 
 }
