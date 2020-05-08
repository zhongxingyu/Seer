 package ru.kradecky.flat_payments;
 
 import ru.kradecky.flat_payments.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 
public class NewCounter extends Activity implements OnItemSelectedListener
 {
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_counter);
 		
 		Spinner spinner = (Spinner) findViewById(R.id.counter_type);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.counter_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		spinner.setOnItemSelectedListener(this);
 	}
 
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
 	{
 		//Toast.makeText(parent.getContext(), "The counter type is " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();		
 	}
 
 	public void onNothingSelected(AdapterView parent) 
 	{
 		// nothing to do
 		
 	}
 
 }
