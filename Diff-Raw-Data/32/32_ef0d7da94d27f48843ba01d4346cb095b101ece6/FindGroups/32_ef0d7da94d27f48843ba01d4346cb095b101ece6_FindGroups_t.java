 package com.allplayers.android;
 
import org.json.JSONArray;
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
					JSONArray jsonResult = new JSONArray(result);
					
					if(jsonResult.length() > 0)
					{
						for(int i = 0; i < jsonResult.length(); i++)
						{
							groups += jsonResult.getJSONObject(i).getString("title") + "\n\n";
						}
					}
					else
					{
						groups += "There were no matches for your search terms.";
					}
 				}
 				catch(JSONException ex)
 				{
 					groups += ex.toString();
 				}
				
				TextView resultView = (TextView)findViewById(R.id.searchGroupsResults);
				resultView.setText(groups);
             }
         });
 	}
 }
