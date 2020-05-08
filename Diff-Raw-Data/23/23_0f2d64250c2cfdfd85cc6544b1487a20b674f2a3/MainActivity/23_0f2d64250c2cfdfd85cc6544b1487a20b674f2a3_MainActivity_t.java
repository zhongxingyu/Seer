 package com.example.raagaaroha;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		
 		ListView myraagalist = (ListView)(findViewById(R.id.listView1));
 		
 		String[] myraaga = {"Adana","AhirBhairav","Baahar","Basant"};
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,myraaga);
 		
 		myraagalist.setAdapter(adapter);			//myraagalist is the listview
 		
 		
 		myraagalist.setOnItemClickListener(new OnItemClickListener(){
 			
 			@Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             //get value for listItems using position    
 			
 			//String raagaclicked = (String) parent.getItemAtPosition(position).toString();
 			
 			
 			
 			Intent intent = new Intent(MainActivity.this,RaagaDisplay.class);
 			
			intent.putExtra("position",position);
 			
 			startActivity(intent);
 			
 
 			
 			}
 		});
 	}
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
