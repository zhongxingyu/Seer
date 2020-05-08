 package com.UPCLookup.com;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class upcSearchActivity extends Activity {
 
 	Button   mButton;
 	EditText mEdit;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.upcsearch);
         
         mButton = (Button)findViewById(R.id.findItem);
         mEdit   = (EditText)findViewById(R.id.edittext);
 
         mButton.setOnClickListener(
         		
             new View.OnClickListener()
             {
                 public void onClick(View view)
                 {
                 	//get the value from the input
                 	String upc = mEdit.getText().toString();
                 	//set up the intent, params include the current activity and the next activity
                	Intent intent = new Intent(view.getContext(), UpcLookupActivity.class);
                	//startActivity(intent);
                 	//in order toe send data form the input field we add it to the intent
                 	//Intent.putExtra(String Name,String Value)
                 	intent.putExtra("upc", upc);
                 	//an integer identifier that is used as a correlation id to identify which sub activity has finished its work
                 	final int result = 1;
                 	startActivityForResult(intent, result);
                     //Log.v("EditText", mEdit.getText().toString());
                	setResult(RESULT_OK, intent);
                     //finish();
                 }
             });
     }
 }
