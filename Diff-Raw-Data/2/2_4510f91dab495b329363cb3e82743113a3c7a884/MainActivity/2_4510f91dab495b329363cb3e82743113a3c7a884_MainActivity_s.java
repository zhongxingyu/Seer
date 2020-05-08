 //Coder:Octavio Gutierrez
 package com.example.addressbook;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableRow;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	Button expand;
 	Button ok;
 	Button clear;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         expand = (Button) findViewById(R.id.button1);
         
         expand.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				EditText name = (EditText) findViewById(R.id.fname);
 				name.setVisibility(View.VISIBLE);
 				name = (EditText) findViewById(R.id.mname);
 				name.setVisibility(View.VISIBLE);
 				name = (EditText) findViewById(R.id.lname);
 				name.setVisibility(View.VISIBLE);
 				name = (EditText) findViewById(R.id.nameprefix);
 				name.setText("Name prefix");
 				
 			}
         	      	
         });
         
         ok = (Button) findViewById(R.id.okbutton);
         
         ok.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				EditText fname = (EditText) findViewById(R.id.fname);
 				EditText address = (EditText) findViewById(R.id.address);
 				EditText city = (EditText) findViewById(R.id.city);
 				EditText state = (EditText) findViewById(R.id.state);
 				EditText zip = (EditText) findViewById(R.id.zip);	
 				
				Toast.makeText(getBaseContext(), "You entered: " + fname + " ," + address + " ," +  city + " ," + state + " ," + zip, Toast.LENGTH_SHORT).show();
 			}
         	      	
         });
         
         clear = (Button) findViewById(R.id.clearbutton);
         
         clear.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				EditText fname = (EditText) findViewById(R.id.nameprefix);
 				EditText address = (EditText) findViewById(R.id.address);
 				EditText city = (EditText) findViewById(R.id.city);
 				EditText state = (EditText) findViewById(R.id.state);
 				EditText zip = (EditText) findViewById(R.id.zip);
 				fname.setText("First Name");
 				address.setText("Address");
 				city.setText("City");
 				state.setText("State");
 				zip.setText("Zip");
 			}
         	      	
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 
     
 }
