 package com.dracosoftrnd.waitingPatientSorter;
 
 import java.util.Collections;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 public class PatientListActivity extends ListActivity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.patient_list);
         setAdapter();
         if(PatientList.GlobalPatients.size() == 0)
         {
         	Intent mainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
             startActivity(mainMenu);
         }
         
     }
 
     @Override
     public void onResume(){
     	super.onResume();
          setAdapter();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.patient_list, menu);
         return true;
     }
     public boolean onOptionsItemSelected(MenuItem item)
     {
  
         switch (item.getItemId())
         {
         case R.id.add_patient:
             Intent addPatient = new Intent(getApplicationContext(), AddPatientActivity.class);
             startActivity(addPatient);
             return true;
         case R.id.load_testData:
         	PatientList.loadTestData();
             setAdapter();
         case R.id.mainMenu:
         	Intent mainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
             startActivity(mainMenu);
         }
 		return false;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
         ContextMenuInfo menuInfo) {
       if (v.getId()==android.R.id.list) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
         menu.add(Menu.NONE, 1, 1, "Remove Patient");
         menu.add(Menu.NONE, 2, 2, "Patient Info");
       }
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
       AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
       int menuItemIndex = item.getItemId();
       if(menuItemIndex == 1)
       {
     	  PatientList.GlobalPatients.remove(info.position);
     	  setAdapter();
       }
       if(menuItemIndex == 2)
       {
    	  
       }
         return true;
     }
     
 	private void setAdapter() {
 		ListView patientList = (ListView) findViewById(android.R.id.list);
         Collections.sort(PatientList.GlobalPatients);
         PatientAdapter adapter = new PatientAdapter(this,
         		  PatientList.GlobalPatients);
         
         patientList.setAdapter(adapter);
         registerForContextMenu(patientList);
 	}
 	
 }
