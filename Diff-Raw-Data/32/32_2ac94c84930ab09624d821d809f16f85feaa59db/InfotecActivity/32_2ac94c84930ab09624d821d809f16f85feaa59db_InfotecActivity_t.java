 package mwhs.ap.bkat.app;
 
 import mwhs.ap.doan.app.R;
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class InfotecActivity extends Activity implements android.view.View.OnClickListener {
 	private Button search;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
        Spinner spinnersport = (Spinner) findViewById(R.id.spinnerSports);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.sports_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

   
         search = (Button) findViewById(R.id.search);
         search.setOnClickListener(this);

        spinnersport.setAdapter(adapter);
        spinnersport.setOnItemSelectedListener(new MyOnItemSelectedListener());
        Object s = spinnersport.getSelectedItem();
        System.out.println(s);
         
        Spinner spinnertuition = (Spinner) findViewById(R.id.spinnerTuition);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this, R.array.tuition_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnertuition.setAdapter(adapter1);
        spinnertuition.setOnItemSelectedListener(new MyOnItemSelectedListener());
        
        Spinner spinnerregion = (Spinner) findViewById(R.id.spinnerRegion);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.region_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerregion.setAdapter(adapter2);
        spinnerregion.setOnItemSelectedListener(new MyOnItemSelectedListener());
         
     }
     
     public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent,
             View view, int pos, long id) {
 
           Toast.makeText(parent.getContext(), parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
         }
 
         public void onNothingSelected(AdapterView parent) {
           // Do nothing.
         }
     }
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.search:{
 			Intent i = new Intent().setClass(this, SearchResults.class);
 			startActivity(i);
 		}
 		}
 		
 	}
 
 }
