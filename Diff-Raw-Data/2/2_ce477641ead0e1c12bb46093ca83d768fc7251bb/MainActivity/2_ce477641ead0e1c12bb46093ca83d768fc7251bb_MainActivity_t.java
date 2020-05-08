 package me.richardking.hmap7;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity
 {
 
 	private Button _actionButton;
 	private EditText _input;
 	
 	/** Called when the activity is first created. */
     @Override public void onCreate(Bundle savedInstanceState)
     {
     	
     	// We don't need no title bar. (Hey! Coder! Leave that status bar alone!) 
     	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
     	
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         _actionButton = (Button) findViewById(R.id.button1);
         _input = (EditText) findViewById(R.id.editText1);
         
         _actionButton.setOnClickListener(
         	new View.OnClickListener()
         	{
 	            public void onClick(View view)
 	            {
 	            	String s = _input.getText().toString(); // Grab the input.
 	            	
	            	if(s.equals(""))
 	            	{
 	            		// Make sure they enter _SOME_ data.
 	            		Toast toast;
 	            		toast = Toast.makeText(getApplicationContext(), "Hold on! You have to enter your age first!", Toast.LENGTH_SHORT);
 	            		toast.show();
 	            	}
 	            	else
 	            	{
 		            	int age = Integer.parseInt(s); // Parse input to an int (we know we're safe as they can only input 0123456789).
 		            	
 		            	if( age < 18 )
 		            	{
 		            		// Generate a warning if the user is under 18.
 		            		Toast toast;
 		            		toast = Toast.makeText(getApplicationContext(), "You're a little young for this...", Toast.LENGTH_SHORT);
 		            		toast.show();
 		            	}
 		            	else
 		            	{
 		            		// Proof of concept more than anything else.
 		            		Toast toast;
 		            		toast = Toast.makeText(getApplicationContext(), calculateLower(age) + "-" + calculateUpper(age), Toast.LENGTH_SHORT);
 		            		toast.show();
 		            	}
 	            	}
 	            }
         	}
         );
     	
     }
     
     /** Calculate the lower limit based on an integer age */
     private int calculateLower(int myAge)
     {
     	if( myAge < 23 )
     		return 18; // We don't want THE LAW a-comin' after us...
     	
     	return (int) ( Math.ceil(myAge/2d) + 7 );
     }
     
     /** Calculate the upper limit based on an integer age */
     private int calculateUpper(int myAge)
     {
     	return 2 * ( myAge - 7 );
     }
 	
 }
