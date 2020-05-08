 package org.jaynestown.tragic7ball;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 import java.util.Random;
 
 
public class DisplayMessageActivity extends Activity {	
 	private String[] msgs;
 	private TextView tv;
 	private Random rnd;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Resources res = getResources();
         setContentView(R.layout.activity_display_message);
         
        tv = (TextView)findViewById(R.id.messageTextView);
        msgs = res.getStringArray(R.array.messages);
         rnd = new Random();        
         
        changeText();        
     }
     
     public void buttonClicked(final View view) {
     	changeText();
     }
 	
 	public void changeText() {
 		int msgIdx = rnd.nextInt(msgs.length);
         tv.setText(msgs[msgIdx]);
 	}
 	
 	
 	/*
 	@Override    	
 	protected void onResume() {
 		super.onResume();    		
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 	*/
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.display_message, menu);
         return true;
     }
 
 
 
     
 }
