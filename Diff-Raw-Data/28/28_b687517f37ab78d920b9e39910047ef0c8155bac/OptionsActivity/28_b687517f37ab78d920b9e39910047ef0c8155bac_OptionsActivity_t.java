 package com.projectdroid.app;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
 import android.widget.TextView;
 
 public class OptionsActivity extends Activity {
   
	private Spinner spinner1;
	private Button btnSubmit;
	
	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settinglayout);
        
        addListenerOnSpineerItemSelection();
     }

	private void addListenerOnSpineerItemSelection() {
		// TODO Auto-generated method stub
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectionListener());
	}

	public void addListenerOnButton(){
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		
		
	}

 }
