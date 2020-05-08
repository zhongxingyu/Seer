 package de.Psychologie.socialintelligence;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.Editable;
import android.text.InputFilter;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
 	Button btnWeiter;
 	EditText userCode;

    //TODO: sind umlaute erlaubt?
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		userCode = (EditText)findViewById(R.id.userCode);
		userCode.setFilters(new InputFilter[]{
				new InputFilter.AllCaps(), new InputFilter.LengthFilter(5)});
 		userCode.addTextChangedListener(new TextWatcher() {
 			@Override  
			public void afterTextChanged(Editable arg0) {       
		        enableSubmitIfReady();
 		      }
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {			
 			}
 			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
 			}
		    });		
 		
 		// Weiter Button geklickt?
 		btnWeiter = (Button)findViewById(R.id.btnWeiter);
 		btnWeiter.setOnClickListener(new OnClickListener() {		
 			@Override
 			public void onClick(View v) {
 				// Usereingabe aus Textfeld holen und alles gro� machen
 				String code = userCode.getText().toString().toUpperCase();
 				// SQL Handler fuer Datenbankimport
 				SQLHandler db = new SQLHandler(MainActivity.this);
 				db.addUserCode(code);
 				// zur naechsten Activity
 				startActivity(new Intent(MainActivity.this,Week.class));
 			}
 		});
 	}
  
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//Aktiviert weiter button
	public void enableSubmitIfReady() {	
        
 	    boolean isReady =userCode.getText().toString().length()==5;
 	    if (isReady) {
 	    	btnWeiter.setEnabled(true);
 	   } else {
 		   btnWeiter.setEnabled(false);
 	    }
 	}	
 
 }
