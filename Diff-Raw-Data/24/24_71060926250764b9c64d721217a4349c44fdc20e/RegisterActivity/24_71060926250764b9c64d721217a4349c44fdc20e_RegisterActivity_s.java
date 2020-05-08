 package edu.gatech.TriggerHappyCoders;
 
 import edu.gatech.User.User;
 import edu.gatech.User.UserCollection;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class RegisterActivity extends Activity {
 	
	private User user;
 	UserCollection uc = new UserCollection();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Set View to register.xml
         setContentView(R.layout.register);
         
         TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
          
         // Listening to Login Screen link
         loginScreen.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View arg0) {
 				// Switching to Login Screen/closing register screen
 				finish();
 			}
 		});
         
         Button registered = (Button) findViewById(R.id.btnRegister);
         registered.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
				EditText name = (EditText)findViewById(R.id.reg_email);
 				EditText password = (EditText) findViewById(R.id.reg_password);
 				EditText email = (EditText) findViewById(R.id.reg_email);
 				showDialog(name.getText().toString(), email.getText().toString(), 
 						password.getText().toString());
 			}
 		});
     }
     
     public void showDialog(String name, String email, String password){
     	    	
 		 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 		 if(uc.getUser().getEmail() == email){
 				alertDialog.setMessage("Email already exists");
 				alertDialog.setButton(-3,"OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				// here you can add functions
 				}
 			});
        }
 		 else{
 				alertDialog.setMessage("Registration successful.");
 				alertDialog.setButton(-3,"OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				// here you can add functions
 			    	User newUser = new User(uc.getUser().getEmail(), 
 			    			uc.getUser().getName(), uc.getUser().getPassword());
 			    	uc.addUser(newUser);
 				}
 			});
 		}	
 		alertDialog.show();
 	
 }
 }
