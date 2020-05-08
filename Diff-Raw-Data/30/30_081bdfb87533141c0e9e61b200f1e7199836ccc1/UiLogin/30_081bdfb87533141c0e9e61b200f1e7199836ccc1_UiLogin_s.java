 package uk.ac.sussex.asegr3.tracker.client.ui;
 
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 
 import uk.ac.sussex.asegr3.tracker.client.service.login.LoginGrantedListener;
 import uk.ac.sussex.asegr3.tracker.client.service.login.LoginService;
 import uk.ac.sussex.asegr3.tracker.client.transport.HttpTransportClientApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 
 public class UiLogin extends Activity implements LoginGrantedListener{
 
 	// Initializing variables
 		private EditText inputName;
 		private EditText inputPassword;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
         
        final ProgressBar progress = (ProgressBar)findViewById(R.id.progressBar1);
        
         
 		try {
 			final LoginService loginService = new LoginServiceFactory().create(this, this, AsyncTask.SERIAL_EXECUTOR, AndroidLogger.INSTANCE);
 			Button next = (Button) findViewById(R.id.button1);      
 			next.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	                loginService.login(inputName.getText().toString(), inputPassword.getText().toString());
	                
	              //  progress.setVisibility(ProgressBar.VISIBLE);
 	                Toast.makeText(getBaseContext(),
 							"Loading.....",
 							Toast.LENGTH_SHORT).show();
 	            }
 
 	        });
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
         
         //getting the connection of the objects
         inputName = (EditText) findViewById(R.id.editText1);
 		inputPassword = (EditText) findViewById(R.id.editText2);  
        
         //register button
         
         Button register = (Button) findViewById(R.id.button2);
         register.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 Intent myIntent = new Intent(view.getContext(), UiRegister.class);
                 startActivity(myIntent);
             }
 
         });
         
         Button exit = (Button) findViewById(R.id.button3);
         exit.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
              
                 System.exit(0);
                  
             }
 
         });
     }
 
 	@Override
 	public void processLogin(HttpTransportClientApi api) {
 		// remove loading message from above 
 		//finish();
 		
 		Intent myIntent = new Intent(this.getBaseContext(), TrackingActivity.class);
 		 startActivity(myIntent);
 	}
 
 	@Override
 	public void processFailedLogin(Exception e) {
 		// TODO remove loading message and show error message.
 		//finish();
 		
 		Intent myIntent = new Intent(this.getBaseContext(), UiError.class);
     	 startActivity(myIntent);
 		
 	}
 	
 }
