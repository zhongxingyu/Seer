 package com.allplayers.android;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class FindGroups extends Activity
 {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.findgroups);
 		
 		final Button logOnButton = (Button) findViewById(R.id.searchGroupsButton);
         logOnButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
             	EditText searchEditText = (EditText)findViewById(R.id.searchGroupsField);
             	String search = searchEditText.getText().toString();
             	
             	String result = APCI_RestServices.searchGroups(search);
             	
             	String groups = "";
 				try
 				{
					JSONObject jsonResult = new JSONObject(result);
					groups += jsonResult.getJSONObject("group").getString("title");
 				}
 				catch(JSONException ex)
 				{
 					groups += ex.toString();
 				}
            	
            	TextView tv = new TextView(FindGroups.this);
        		tv.setText(result);
        		setContentView(tv);
             }
         });
 	}
 }
