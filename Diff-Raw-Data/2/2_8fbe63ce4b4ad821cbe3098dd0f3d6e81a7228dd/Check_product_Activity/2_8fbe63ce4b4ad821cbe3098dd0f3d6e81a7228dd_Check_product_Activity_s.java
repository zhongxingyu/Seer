 package com.rtt_ku.pos;
 
 import com.database.pos.Database;
 import com.rtt_store.pos.StoreController;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Check_product_Activity extends Activity {
 
 	StoreController sCT;
 
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.product_id_layout);
 
 		Database myDb = new Database(this);
 		myDb.getWritableDatabase();
 		sCT = new StoreController(myDb);
 		;
 
 		// view matching
 		Button okButton = (Button) findViewById(R.id.button1);
 		Button cancelButton = (Button) findViewById(R.id.button2);
 
 		// add function on click at OK button.
 		okButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				EditText pc = (EditText) findViewById(R.id.pc_text);
 				
 				String product_code = pc.getText().toString();
				if(sCT.isHasYet(product_code))
 				{
 					//has
 					Toast.makeText(Check_product_Activity.this,"Put your fucking hand up", Toast.LENGTH_SHORT).show();
 					Intent intent = new Intent(Check_product_Activity.this, Add_Activity.class);
 					intent.putExtra("pc", product_code);
 					startActivity(intent);
 				}
 				else
 				{
 					// not has
 					Toast.makeText(Check_product_Activity.this,"This product id has already", Toast.LENGTH_SHORT).show();
 				}
 			}
 
 		});
 
 		// add function on click at cancel button.
 		cancelButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				// setContentView(R.layout.activity_main);
 				startActivity(new Intent(Check_product_Activity.this,
 						main_activity.class));
 			}
 
 		});
 	}
 }
