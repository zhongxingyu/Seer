 package com.studentersamfundet.app.ui;
 
 import android.os.Bundle;
 import android.text.util.Linkify;
 import android.widget.TextView;
 
 import com.studentersamfundet.app.R;
 
 public class JoinUsActivity extends BaseDnsActivity {
 	   public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.join_us);
 	        
 	        TextView tv = (TextView)findViewById(R.id.join_us_textview);
	        Linkify.addLinks(tv, Linkify.ALL);
 	    }
 }
