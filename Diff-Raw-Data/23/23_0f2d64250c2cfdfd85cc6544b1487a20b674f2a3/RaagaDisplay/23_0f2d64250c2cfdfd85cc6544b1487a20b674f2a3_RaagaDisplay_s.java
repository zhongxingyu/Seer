 package com.example.raagaaroha;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RaagaDisplay extends Activity{
 	
 	public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.displayraaga);
 	
 	Intent i = getIntent();
 	
	//int indexofraaga;
 	
	Bundle extras = i.getExtras();
 	
	int indexofraaga = extras.getInt("position");
 	
 	//String myraaga = i.getIntExtra("position");
 	
 	Toast.makeText(getApplicationContext(), Integer.toString(indexofraaga) , Toast.LENGTH_SHORT).show();
 	
 	String raagas[] = {"Adana","Ahir Bhairav","Baahar","Basant"};
 	
	String description[] = {"Adana","Ahir Bhairav","Thaat: Kafi\nTime: 12 am - 3 am\nAaroha: 'n-S-m,m-P-g-m,n-D-N-S'\nAvaroha: S'-n-P-m-P-g-m-R-S","Basant"};
 	
 	TextView nameofraaga = (TextView)(findViewById(R.id.nameofraaga));
 	
 	nameofraaga.setText(raagas[indexofraaga]);
 	
 	//nameofraaga.setText(myraaga);
 	
 	TextView describeraaga = (TextView)(findViewById(R.id.describeraaga));
 	
 	describeraaga.setText(description[indexofraaga]);
 	
 	
 	}
 
 }
