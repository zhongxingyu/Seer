 package com.morgan.sparklewombat;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Scroller;
 import android.widget.TextView;
 //test
 public class MyFirstAppActivity extends Activity {
 	
 	private int count;
 	String responseDialog = "";
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final EditText et;
 		final Button b;
 
 
 		et = (EditText) findViewById(R.id.edittext);
 
 		
 		b = (Button) findViewById(R.id.button);
 		final TextView rtv = (TextView) findViewById(R.id.responseTextView);
 		rtv.setScroller(new Scroller(getBaseContext()));
		rtv.setMaxLines(25);
 		rtv.setVerticalScrollBarEnabled(true);
 		rtv.setMovementMethod(new ScrollingMovementMethod());
 		final String responsearray[] = new String[10];
		
 			responsearray[0] = getResources().getString(R.string.response1);
 			responsearray[1] = getResources().getString(R.string.response2);
 			responsearray[2] = getResources().getString(R.string.response3);
 			responsearray[3] = getResources().getString(R.string.response4);
 			responsearray[4] = getResources().getString(R.string.response5);
 			responsearray[5] = getResources().getString(R.string.response6);
 			responsearray[6] = getResources().getString(R.string.response7);
 			responsearray[7] = getResources().getString(R.string.response8);
 			responsearray[8] = getResources().getString(R.string.response9);
 			responsearray[9] = getResources().getString(R.string.response10);
 		
 		
 		b.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String userText = et.getText().toString();
 				responseDialog = responseDialog + "You: " + userText;
 				et.setText("");
 				if (count==2) {
 					responseDialog = responseDialog + "\n" + responsearray[count] + " " + userText + "\n\n";
					rtv.setText(responseDialog);	
 				}
 				else
 				{	responseDialog = responseDialog + "\n" + responsearray[count] + "\n\n";
 				rtv.setText(responseDialog);
 				}
 				
 

				if (count==10) {
					responseDialog = "";
					count=0;
				}
 				
 				
 				/*Toast msg = Toast.makeText(getBaseContext(), userText,
 						Toast.LENGTH_SHORT);
 				msg.show();*/
 				
 				
 				
 			}
 		});
 	}
 }
