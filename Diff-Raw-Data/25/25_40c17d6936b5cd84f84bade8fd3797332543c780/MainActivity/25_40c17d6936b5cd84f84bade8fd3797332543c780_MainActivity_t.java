 package com.mray.veganbrews;
 
 import java.util.ArrayList;
 
 import com.mray.brewerys.Brewery;
 import com.mray.brewerys.Company;
import com.mray.brewerys.States; 
 import com.mray.lib.FormThings;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		LinearLayout linLay = new LinearLayout(this);
 		
 		LinearLayout entryBox = FormThings.singleEntryWithButton(this, "Enter a Vegan Beer Company", "Find It");
 		Button findBtn = (Button) entryBox.findViewById(2);
 		
 		findBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// LOGIC 
 				// Get the tag
 				EditText companyName = (EditText) v.getTag();
 				if(companyName.getText().toString().length() > 0){
 					Log.i("Find It: ", companyName.getText().toString());
 				}else{
 					String userSearch = "Search by State";
 					Log.i("Find It: ", userSearch);
 				}
 				
 				// Is it working?
 				Log.i("BUTTON CLICKED: ", companyName.getText().toString());
 				
 			}
 		});
		/* Example on pg. 132 in the book (Listing 3-39)  
 		 */
		ArrayList<Company>brewerys = new ArrayList<Company>();
 		brewerys.add(new Brewery("10 Barrel Brewery", "Ben, Oregon"));
 		brewerys.add(new Brewery("4 Hands Brewery", "St. Louis, Missouri"));
 		brewerys.add(new Brewery("5 Rabbit Cervecer", "Chicago, Illinois"));
 		brewerys.add(new Brewery("512 Brewing Company", "Austin, Texas"));
 		brewerys.add(new Brewery("Abita Brewing Company", "Abita Springs, Louisiana"));
 		
 		//This should bring up the brewery by state once input (not sure though, need to do more research)
 		for(Company Company : brewerys){
 			Log.i ("Brewerys by state ", Company.toString());
 		}
 		linLay.addView(entryBox);
 		
 		setContentView(linLay);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
