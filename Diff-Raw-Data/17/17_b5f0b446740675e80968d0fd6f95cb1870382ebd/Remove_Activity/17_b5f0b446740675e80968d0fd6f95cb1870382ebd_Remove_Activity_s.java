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
 /**
  * activity on remove button. 
  * @author Suttanan
  *
  */
 public class Remove_Activity extends Activity{
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.remove_layout);
 	    
 	    // view matching
 	    Button okButton = (Button)findViewById(R.id.OK_button);
 		Button cancelButton = (Button)findViewById(R.id.cancel_button);
 		
         Database myDb = new Database(this);
         myDb.getWritableDatabase();
 		final StoreController sCT = new StoreController(myDb);
 		
 		// add function on click at OK button.
 		okButton.setOnClickListener(new OnClickListener(){
 		
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				EditText pc = (EditText)findViewById(R.id.pc_text);
 				String product_code = pc.getText().toString();
 				boolean check = sCT.removeProduct(product_code);
 				String output = "";
				if (check) output = "<" + product_code  + "> have been removed";
				else output = "can't find <" +product_code + "> in inventory";
		        Toast.makeText(Remove_Activity.this,output,
		        		Toast.LENGTH_LONG).show();
 		        if (check) {
 					setContentView(R.layout.activity_main);
 					startActivity(new Intent(Remove_Activity.this, main_activity.class));
 		        }
 
 			}
 			
 		});
 		
 		// add function at cancel button.
 		cancelButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setContentView(R.layout.activity_main);
 				startActivity(new Intent(Remove_Activity.this, main_activity.class));
 			}
 			
 		});
 	}
 }
