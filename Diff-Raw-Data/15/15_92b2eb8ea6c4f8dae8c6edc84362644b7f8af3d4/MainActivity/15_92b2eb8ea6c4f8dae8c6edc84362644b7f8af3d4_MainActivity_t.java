 package com.example.cardio_v1;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class MainActivity extends Activity implements OnItemSelectedListener 
 {
 	//Doesn't do much
 	 public final static String SEND_MESSAGE = "com.example.cardio_v1.MESSAGE";
 	 public final static String SEND_MESSAGE2 = "com.example.myfirstapp.MESSAGE1";
 	 public final static String minSpeedr = "com.example.myfirstapp.MESSAGE3";
 	 // container to hold the selection of the first spinner
 	 public String message1 = "Test";
 	 //container to hold the selection of the second spinner
 	 public String message2 = "Test";
 	 
 	 public int spinnerCount = 0;
 	 
 	 public String message3 = "test";
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         spinnerCount+=0;
  //       createSpinner1();
         createSpinner2();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
     	
     	getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void sendMessage(View view)
     {
     	Intent intent = new Intent(this, DisplayMessageActivity.class);
     	//EditText editText = (EditText) findViewById(R.id.edit_message);
     	//String message = editText.getText().toString();
     	intent.putExtra(SEND_MESSAGE, message1);
     	intent.putExtra(SEND_MESSAGE2, message2);
     	intent.putExtra(minSpeedr, message3);
     	startActivity(intent);
 
     }
     
     //method to create the first spinner using the zombieTracks array in spinnerResources
     /*
     public void createSpinner1()
     {
     	Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
     	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.zombieTracks, android.R.layout.simple_spinner_item);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     	spinner1.setOnItemSelectedListener(this);
     	spinner1.setAdapter(adapter);
     
     }
     */
     //method to create the second spinner using the minSpeed array in spinnerResources
     public void createSpinner2()
     {
     	Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
     	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.minSpeed, android.R.layout.simple_spinner_item);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     	spinner2.setOnItemSelectedListener(this);
     	spinner2.setAdapter(adapter);
     }
     public void onItemSelected(AdapterView<?> parent,View view,int pos, long id)
     {
 //    	spinnerCount++;
 //    	
 //    	if(spinnerCount == 3)
 //    	{
 //    		message1 = parent.getItemAtPosition(pos).toString();
 //    		spinnerCount++;
 //    	}
 //    	
 //    	
 //    	else if (spinnerCount > 3) 
 //    	{
 //    		message2 = parent.getItemAtPosition(pos).toString();
 //    	}
 //    	
     	
     	message1 = parent.getItemAtPosition(pos).toString();
     	
     }
     public void onNothingSelected(AdapterView<?> parent)
     {
     
     }
     	 
 }
     
 
