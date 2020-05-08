 package edu.sjsu.cs.davsync;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.EditText;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.util.Log;
 
 public class davsync extends Activity {
 
     private DSDatabase db;
     private EditText[] field = new EditText[4]; // hostname, resource, username, password
     private Button[] button = new Button[3]; // save, clear, test
 
     private enum ButtonType {
         SAVE, CLEAR, TEST
     }
 
     private class ButtonListener implements OnClickListener {
         ButtonType type;
 
         public ButtonListener(ButtonType type) {
             this.type = type;
         }
         @Override
         public void onClick(View v) {
             switch( type ) {
             case SAVE:
                 db.addProfile(getCurrentProfile());
                 break;
             case CLEAR:
                 db.delProfile(getCurrentProfile());
                 clearTextFields();
                 break;
             case TEST:
                 //test();
                 break;
             }
         }
     }
 
     // read the state of all fields from memory and return a Profile object
     private Profile getCurrentProfile() {
         Profile p;
         p = new Profile(
             field[0].getText().toString(),
             field[1].getText().toString(),
             field[2].getText().toString(),
             field[3].getText().toString());
         return p;
     }
 
     // removes any text from all fields
     private void clearTextFields() {
         for(int i = 0; i < 4; i++)
             field[i].setText("");
     }
 
     /** Called when the activity is first created. */
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.info);
 	db = new DSDatabase(this);
 
         // handle button events
         button[0] = (Button)this.findViewById(R.id.btnSave);
         button[1] = (Button)this.findViewById(R.id.btnClear);
         button[2] = (Button)this.findViewById(R.id.btnTest);
         button[0].setOnClickListener(new ButtonListener(ButtonType.SAVE));
         button[1].setOnClickListener(new ButtonListener(ButtonType.CLEAR));
         button[2].setOnClickListener(new ButtonListener(ButtonType.TEST));
 
         // access to text fields
         field[0] = (EditText)findViewById(R.id.hostname);
         field[1] = (EditText)findViewById(R.id.resource);
         field[2] = (EditText)findViewById(R.id.username);
         field[3] = (EditText)findViewById(R.id.password);
 
 	Profile p = db.getProfile();
 	field[0].setText(p.getHostname());
 	field[1].setText(p.getResource());
 	field[2].setText(p.getUsername());
 	field[3].setText(p.getPassword());
     }
 }
