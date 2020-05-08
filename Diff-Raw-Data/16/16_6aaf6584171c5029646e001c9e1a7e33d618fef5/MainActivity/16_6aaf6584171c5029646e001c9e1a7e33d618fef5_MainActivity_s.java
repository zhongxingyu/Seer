 package com.mray.veganbrews;
 
 import java.util.ArrayList;
 
 import com.mray.brewerys.Brewery;
 import com.mray.brewerys.Company;
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
 				// Get the tag
 				EditText companyName = (EditText) v.getTag();
 				
 				// Is it working?
 				Log.i("BUTTON CLICKED: ", companyName.getText().toString());
 				
 			}
 		});
 		
		ArrayList<Company>brewery;
		brewery.add(new Brewery("10 Barrel Brewery", "Bend Oregon"));
 		
 		
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
