 package sim7.speedscanadvanced;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class MainActivity extends Activity{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
	
 		setupMessageButton();
 	}
 
 	
 	
 	private void setupMessageButton(){
 	// get a reference to the button
 		Button messageButton = (Button)findViewById(R.id.buttonMain);
 	
 		// set the click listener to run my code
 		View.OnClickListener myListener = new View.OnClickListener(){
 			
 			@Override
 			public void onClick(View v){
 			//Log.i("MainButtonApp", "You Clicked the button");	
 			//Toast.makeText(MainActivity.this, "You clicked it!", 	Toast.LENGTH_LONG).show();
 			
 			startActivity(new Intent(MainActivity.this, CalculateActivity.class));
 			}	
 		};	
 		
 		messageButton.setOnClickListener(myListener);
 	}
 		
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 
 }
