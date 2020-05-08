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
 	
	int indexofraaga;
 	
	//Bundle extras = i.getExtras();
 	
	//int indexofraaga = extras.getInt("position");
	
	indexofraaga = i.getIntExtra("position",100);
 	
 	//String myraaga = i.getIntExtra("position");
 	
 	Toast.makeText(getApplicationContext(), Integer.toString(indexofraaga) , Toast.LENGTH_SHORT).show();
 	
 	String raagas[] = {"Adana","Ahir Bhairav","Baahar","Basant"};
 	
	String description[] = {"Thaat: Asavari\nTime: 12 am - 3 am\nAaroha: S-R_m-P-nmP-S'\nAvaroha: S'-dnP-mP-gmRS\nVaadi-Samvaadi: /P",
			"Thaat: Bhairav\nTime: 6 am - 9 am\nAaroha: S-r-G-m-P-GmD-n-S'\nAvaroha: S'-n-D-P-m-G-r-S-'n-r-S\nVaadi-Samvaadi: m/S",
			"Thaat: Kafi\nTime: 12 am - 3 am\nAaroha: 'n-S-m,m-P-g-m,n-D-N-S'\nAvaroha: S'-n-P-m-P-g-m-R-S\nVaadi-Samvaadi: m/S",
			"Thaat: Poorvi\nTime: 3am - 6am\nAaroha: S-G-M-d-r'-S'\nAvaroha: S'-N-d-P-M-G-M-G-r-S\nVaadi-Samvaadi:S/P\nSimilar:Poorvi,Sohini"};
 	
 	TextView nameofraaga = (TextView)(findViewById(R.id.nameofraaga));
 	
 	nameofraaga.setText(raagas[indexofraaga]);
 	
 	//nameofraaga.setText(myraaga);
 	
 	TextView describeraaga = (TextView)(findViewById(R.id.describeraaga));
 	
 	describeraaga.setText(description[indexofraaga]);
 	
 	
 	}
 
 }
