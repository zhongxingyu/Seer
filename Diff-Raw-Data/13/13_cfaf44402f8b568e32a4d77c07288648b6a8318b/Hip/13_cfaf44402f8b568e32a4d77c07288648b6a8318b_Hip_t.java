 package com.wobuddy.activity;
 
import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 
 import com.wobuddy.AppContext;
 import com.wobuddy.R;
 
public class Hip extends Activity {
 	public final static String URL = "url";
 	private String url = "http://www.youtube.com/watch?v=rck_vRkMhp8";
	
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.repmachine);
 		
 		// Set spinners
		Spinner weightSpinner = (Spinner) findViewById(R.id.weightSpinner);
		Spinner repSpinner = (Spinner) findViewById(R.id.repSpinner);
 		
 		
 		//Tutorial button
 		Button tutorial = (Button) findViewById(R.id.videoTutorial);
 	
 		tutorial.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent i = new Intent(Intent.ACTION_VIEW,
 						  Uri.parse(url));
 				startActivity(i);				
 			}
 		});
 		
 		// Add button
 		ImageButton addButton = (ImageButton) findViewById(R.id.addRowButton);
 		addButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				AppContext appContext = (AppContext)v.getContext().getApplicationContext();
 				appContext.appendHipData("TEST");
 			}
 		});
 		
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	
 	}
 
 }
