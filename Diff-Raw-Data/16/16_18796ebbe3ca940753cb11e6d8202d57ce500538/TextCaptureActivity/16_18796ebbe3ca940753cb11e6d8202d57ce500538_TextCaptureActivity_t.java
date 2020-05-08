 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
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
  * by {@link FulfillmentIntentFactory}.
  */
 public class TextCaptureActivity extends FulfillmentActivity implements OnClickListener {
 
     /**
      * Handles initialization of the activity.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_text_capture);
         
         //setup our listeners
         ((Button)findViewById(R.id.text_save_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.text_cancel_button)).setOnClickListener(this); 
     }
 
     /**
      * Constructs menu options.
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_text_capture, menu);
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
