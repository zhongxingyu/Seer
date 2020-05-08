 package com.example.funwithforms;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CrazyName extends Activity {
     private Handler myHandler = new Handler();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_crazy_name);
 		
 		Toast.makeText(this, "All your base are belong to us!!", Toast.LENGTH_SHORT).show();
 		
		myHandler.postDelayed(new Runnable() {
            public void run() {
                streamText();
            }
        }, 3000);
 	    
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_crazy_name, menu);
 		return true;
 	}
 	
 	public void streamText(){
 		// Get the text view
 	    TextView textView = (TextView)findViewById(R.id.crazy_name);
 	    
 		// Get the crazyString from the intent, turn it into an array
 	    Intent intent = getIntent();
 	    String crazyString = intent.getStringExtra(MainActivity.CRAZY_STRING);
 	    String[] split = crazyString.split("");
 	    
 	    //Start rendering text
 	    int i = 0;
 	    while (true) {
 	    	textView.append(split[i]);
 	    	if (i>=99){
 	    		i = 0;
 	    		break;
 	    	}
 	    }
 	}
 
 }
