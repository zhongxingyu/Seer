 package ch.good2go;
 
 
 import ch.good2go.objects.Device.Devices;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class DeviceEdit extends Activity {
 	
 	private EditText nameText;
 	private EditText typeText;
 	private Long mRowId;
 	private Spinner spinner;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.device_edit);
 
	    nameText = (EditText) findViewById(R.id.device_name);
 	    typeText = (EditText) findViewById(R.id.type);
 	    
 
 	    Button confirmButton = (Button) findViewById(R.id.confirm);
 
 	    mRowId = null;
         spinner = (Spinner) findViewById(R.id.location);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.location_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
 
 	    
 
 	    confirmButton.setOnClickListener(new View.OnClickListener() {
 
 	        public void onClick(View view) {
	        	String s = nameText.getText().toString();
	        	String ss= spinner.getSelectedItem().toString();
	        	String sss=typeText.getText().toString();
	        	s=s;
	        	insertRecord(nameText.getText().toString(), spinner.getSelectedItem().toString(), typeText.getText().toString());
 
 	            Intent mIntent = new Intent();
 	            setResult(RESULT_OK, mIntent);
 	            finish();
 	        }
 	        
 	    });
 
 	}
 	
 	private void insertRecord(String name, String location, String type) {
         ContentValues values = new ContentValues();
         values.put(Devices.NAME, name);
         values.put(Devices.LOCATION, location);
         values.put(Devices.DEVICE_TYPE, type);
         getContentResolver().insert(Devices.CONTENT_URI, values);
 	}
 
 }
