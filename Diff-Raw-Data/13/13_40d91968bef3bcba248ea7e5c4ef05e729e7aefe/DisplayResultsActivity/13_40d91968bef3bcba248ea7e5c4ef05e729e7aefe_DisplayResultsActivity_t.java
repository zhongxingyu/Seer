 /* Scott Caruso
  * Java II 1308
  * Week 3 Assignment
  */
 package com.scottcaruso.mygov;
 
 import org.json.JSONObject;
 
 import com.scottcaruso.datafunctions.TurnStringIntoJSONObject;
 import com.scottcaruso.interfacefunctions.DisplayPoliticianResults;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Spinner;
 
 public class DisplayResultsActivity extends Activity {
 	
 	String pols;
 	Boolean favorites;
 	static Context displayContext;
 	Boolean orientationState;
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		Log.i("Info","State Change Detected");
 		savedInstanceState.putBoolean("orientationchanged", true);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
		Log.i("Info","Created display activity.");
 		orientationState = false;
 		if (savedInstanceState != null)
 		{
 			if (savedInstanceState.getBoolean("orientationchanged") == true)
 			{
 				orientationState = true;
 			}
 		}
 		setDisplayContext(DisplayResultsActivity.this);
 		this.setContentView(R.layout.politician_display);
 		Bundle passedData = getIntent().getExtras();
 		pols = (String) passedData.get("response");
 		favorites = (Boolean) passedData.getBoolean("favorites");
 		JSONObject responseObject = TurnStringIntoJSONObject.createMasterObject(pols,favorites);
 		DisplayPoliticianResults.showPoliticiansInDisplay(responseObject, favorites);
 	}
 	
 	@Override
 	public void finish() {
 		Log.i("Info","Display Activity Finishing");
 		final Spinner polName = (Spinner) findViewById(com.scottcaruso.mygov.R.id.politicianName);
 		String name = polName.getSelectedItem().toString();
 	    Intent data = new Intent();
 	    data.putExtra("lastpolname", name);
 	    data.putExtra("orientationchanged", orientationState);
 	    setResult(RESULT_OK, data);
 	    super.finish();
 	}
 	
 	public static Context getDisplayContext() {
 		return displayContext;
 	}
 
 	public static void setDisplayContext(Context displayContext) {
 		DisplayResultsActivity.displayContext = displayContext;
 	}
 }
