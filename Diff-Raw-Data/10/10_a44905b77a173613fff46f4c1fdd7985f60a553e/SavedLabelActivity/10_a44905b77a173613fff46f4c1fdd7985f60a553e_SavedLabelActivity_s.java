 package com.example;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 public class SavedLabelActivity extends Activity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.savelabel);
 	    
         Button back = (Button) findViewById(R.id.back);
         back.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 Intent intent = new Intent();
                 setResult(RESULT_OK, intent);
                 finish();
             }
         });
 	
 	    // TODO Auto-generated method stub
 	}
 
 }
