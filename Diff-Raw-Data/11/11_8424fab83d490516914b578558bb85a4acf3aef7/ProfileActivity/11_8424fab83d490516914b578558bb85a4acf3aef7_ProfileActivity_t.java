 package com.example.zonedhobbitsportfolio;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.graphics.Typeface;
 import android.view.Menu;
import android.widget.TextView;
 
 public class ProfileActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile);
		
		//Test fonts
        
        TextView myTextView=(TextView)findViewById(R.id.text_header_main);
        Typeface typeFace=Typeface.createFromAsset(getAssets(),"fonts/Edmondsans-Bold.otf");
        myTextView.setTypeface(typeFace);
        
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.profile, menu);
 		return true;
 	}
 
 }
