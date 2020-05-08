 package edu.channel4.mobilemetrics.sample.android;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import edu.channel4.mobilemetrics.sdk.android.LocalyticsSession;
 
 public class SampleApplication extends Activity {
 
 	public static final String LOCALYTICS_KEY = "sample_key";
 	private LocalyticsSession localyticsSession;
 	private EditText editTextName, editTextAge;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		localyticsSession = new LocalyticsSession(getApplicationContext(),
 				LOCALYTICS_KEY);
 		localyticsSession.open();
 
 		editTextName = (EditText) findViewById(R.id.editTextName);
 		editTextAge = (EditText) findViewById(R.id.editTextAge);
 	}
 
 	public void tag(View v) {
 		Map<String, String> stringMap = new HashMap<String, String>();
 
 		stringMap.put("name", editTextName.getText().toString());
 		stringMap.put("age", editTextAge.getText().toString());
 
		localyticsSession.tagEvent("Tag user name and age", stringMap);
 	}
 
 	public void upload(View v) {
 		localyticsSession.upload();
 	}
 
 }
