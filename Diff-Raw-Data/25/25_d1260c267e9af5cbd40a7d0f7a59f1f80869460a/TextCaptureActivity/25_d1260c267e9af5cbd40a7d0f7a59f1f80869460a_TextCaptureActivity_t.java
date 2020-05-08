 package ca.cmput301.team13.taskman;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
/**
 * TextCaptureActivity is the activity that allows the user
 * to submit the text to fulfill a textual requirement.
 * This activity should be launched with an intent created
 * by FulfillmentActivityFactory.
 */
 public class TextCaptureActivity extends FulfillmentActivity implements OnClickListener {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.text_fulfillment);
         
         //setup our listeners
         ((Button)findViewById(R.id.text_save_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.text_cancel_button)).setOnClickListener(this); 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.text_fulfillment, menu);
         return true;
     }
 
     /**
      * Delegates action based on which listener has been clicked
      * @param source 
      */    
     public void onClick(View source) {
         if (source.equals(findViewById(R.id.text_save_button))) {
             save();
         }
         else if (source.equals(findViewById(R.id.text_cancel_button))) {
             cancel();
         }
     }
 
     /**
      * Send the entered text to our parent and exit the Activity
      */
     public void save() {
        //get the text
         String text = ((EditText) findViewById(R.id.text_fulfillment)).getText().toString();
        //return the text
         fulfillment.setText(text);
         successful = true;
         finish();
     }
     
     /**
      * Cancel the Activity
      */
     public void cancel() {
         successful = false;
         finish();
     }
     
 }
