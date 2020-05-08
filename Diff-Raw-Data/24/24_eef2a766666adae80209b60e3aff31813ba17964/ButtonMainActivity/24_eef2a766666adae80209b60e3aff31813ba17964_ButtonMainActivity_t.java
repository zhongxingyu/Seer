 package com.choiboi.apiexamples.button;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.choiboi.apiexamples.R;
 
 public class ButtonMainActivity extends Activity implements OnClickListener {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_button);
         
         // Button listener done by implementing this class with OnClickListener.
         // When button is pressed it will invoke the overriden onClick method below.
         Button classListenerButton = (Button) findViewById(R.id.button_class_implement_onclicklistener);
         classListenerButton.setOnClickListener(this);
         
         // Button listener implemented by not implmenting this class with
         // OnClickListener. When button is pressed, it will invoke the onClick method
         // inside the OnClickListener object created when the listener has been set.
         Button implementListenerButton = (Button) findViewById(R.id.button_object_onclicklistener);
         implementListenerButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 Toast.makeText(ButtonMainActivity.this, 
                         "Button listener created while setting the button listener pressed!", Toast.LENGTH_LONG).show();
             }
         });
     }
    
    // Overriden onClick method. This method will invoke for buttons that has been set with the on click listener
    // with this class.
     @Override
     public void onClick(View v) {
         Toast.makeText(this, "Button listener implemented through this class pressed!", Toast.LENGTH_LONG).show();
     }
     
    // This method will invoke with the button that has the xml attribute onClick set with this method name.
     public void onButtonPressed(View v) {
         Toast.makeText(this, "Button listener implemented xml onClick attribute!", Toast.LENGTH_LONG).show();
     }
 }
