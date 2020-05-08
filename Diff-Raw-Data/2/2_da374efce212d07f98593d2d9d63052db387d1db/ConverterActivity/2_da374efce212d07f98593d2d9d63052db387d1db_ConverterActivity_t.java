 package com.example.dojo1;
 
 import java.security.InvalidParameterException;
 
 import android.app.Activity;
 import android.net.MailTo;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class ConverterActivity extends Activity {
 	EditText ed1;
 	EditText ed2;
 	EditText ed3;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         ed1 = (EditText) findViewById(R.id.editText1);
     	ed2 = (EditText) findViewById(R.id.editText2);
     	ed3 = (EditText) findViewById(R.id.editText3);
     	
         ed1.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				process();				
 			}
 		});
         
         ed2.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				process();				
 			}
 		});
         
     }
 
     private void process(){
     	Sum sum = new Sum();
 
 		try {
 			if (ed1.getText().toString().isEmpty() || ed2.getText().toString().isEmpty()) {
 				return;
 			}
 			
 			int bin = sum.binarySum(ed1.getText().toString(), ed2.getText().toString());
 			String d3 = Integer.toString(bin);
 			ed3.setText(d3);	
 		} catch (InvalidParameterException e) {
 			Toast.makeText(ConverterActivity.this, "U CANNOT DO THIS", Toast.LENGTH_SHORT).show();
			ed1.setText("");
			ed2.setText("");
 			ed3.setText("");
 		}
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 }
