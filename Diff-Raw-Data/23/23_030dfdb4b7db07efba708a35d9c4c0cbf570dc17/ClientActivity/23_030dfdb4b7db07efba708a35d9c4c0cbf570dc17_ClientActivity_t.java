 /*
 * Strat-game Android-Client :
  * 
  * Code : Frédéric Meslin, Florent Touchard, Benjamin Blois
  * Released on 29/01/12 at 15h00 for the 4th GGJ
  * 
  * This program and all its resources included is
  * published under the Creative common license By-NC 3.0.
  * 
  * For more informations :
  * http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package org.globalgamejam.strat;
 
 import java.net.Socket;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ClientActivity extends Activity {
 
 	private EditText etIpDest, etPortDest;
 	private Button bConnect;
 
 	protected Socket sock;
 
 	public void onCreate(Bundle savedInstanceState) {
 		// Create the interface
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.client_layout);
 		Log.d("Strat", "Application opened");
 
 
 		// Retrieving of the UI handlers
 		etIpDest = (EditText) findViewById(R.id.etIpDest);
 		etPortDest = (EditText) findViewById(R.id.etPortDest);
 		bConnect = (Button) findViewById(R.id.bConnect);
 
 		// Connect the interface listeners
 		bConnect.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				launchUI();
 				finish();
 			}
 		});  
     }
     
     private void launchUI() {
     	Intent intent = new Intent(this, GameUIActivity.class);
     	intent.putExtra("host", etIpDest.getText().toString());
     	intent.putExtra("port", Integer.parseInt(etPortDest.getText().toString()));
     	startActivity(intent);
     }
 }
