 package com.supinfo.notetonsta.android;
 
 import java.util.ArrayList;
 
 import com.supinfo.notetonsta.android.entity.Campus;
 import com.supinfo.notetonsta.android.entity.SimpleIntervention;
 import com.supinfo.notetonsta.android.handler.GetCampusesHandler;
 import com.supinfo.notetonsta.android.handler.GetInterverventionsHandler;
 import com.supinfo.notetonsta.android.resource.GetCampusesResource;
 import com.supinfo.notetonsta.android.resource.GetInterventionsResource;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 
 public class ChooseCampusActivity extends Activity implements View.OnClickListener {
	Spinner campusSpinner;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.choosecampus);
         
        campusSpinner = (Spinner) findViewById(R.id.campuslist);
         Button campusButton = (Button) findViewById(R.id.campusbutton);
         
         campusButton.setOnClickListener(this);
         
        ArrayList<Campus> campusList = new ArrayList<Campus>();
        ArrayAdapter<Campus> campusAdapter = new ArrayAdapter<Campus>(this, android.R.layout.simple_spinner_item, campusList);
         
        campusSpinner.setAdapter(campusAdapter);
         
         campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         GetCampusesHandler handler = new GetCampusesHandler(this, getResources(), campusAdapter);
        GetCampusesResource resource = new GetCampusesResource(handler, campusList);
         Thread thread = new Thread(resource);
         thread.start();
     }
 
 	public void onClick(View v) {
 		ArrayList<SimpleIntervention> interventions = new ArrayList<SimpleIntervention>();
		Campus campus = (Campus) campusSpinner.getSelectedItem();
 		
 		Intent intent = new Intent(this, ChooseInterventionActivity.class);
 		intent.putExtra("campus", campus);
 		intent.putExtra("interventions", interventions);
 		
 		GetInterverventionsHandler handler = new GetInterverventionsHandler(this, getResources(), intent);
 		GetInterventionsResource resource = new GetInterventionsResource(handler, interventions, campus.getId());
 		Thread thread = new Thread(resource);
 		thread.start();
 	}
 }
