 package csci422.final_project;
 
 import java.io.*;
 
 import csci422.final_project.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class HvZ_Activity extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		//The code below is used to create the popup window for the player code
 		String path = getInternalCacheDirectory();
 		//final String FILENAME = path + "/PlayerCodeFile" ;
 		//System.out.println("BALLS" + FILENAME);
 		final String FILENAME = "PlayerCodeFile" ;
 		
 		int len = 1024;
 		byte[] buffer = new byte[len];
 		try {
 			FileInputStream fis = openFileInput(FILENAME);
 			int nrb = fis.read(buffer, 0, len);
 			while (nrb != -1) {
 				nrb = fis.read(buffer, 0, len);
 			}
 			System.out.println(buffer.toString());
 			fis.close();
 		} catch (FileNotFoundException e) {
 			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 			alert.setTitle("Enter Player Code");
 			alert.setMessage("Please enter your player code.");
 
 			// Set an EditText view to get user input
 			final EditText input = new EditText(this);
 			alert.setView(input);
 			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,int whichButton) {
 							int value = Integer.parseInt(input.getText().toString());
 							if (value == 0 || value < 9999 || value > 100000) {
 								removeDialog(1);
 								Toast.makeText(getApplicationContext(), "Invalid Player Code.\nPlease go to your Profile to update.", Toast.LENGTH_LONG).show();
 								Handler handler = new Handler();
 								handler.post(new Runnable() { 
 									public void run() { showDialog(1);
 									}
 								});
 							}
 							else
 
 							System.out.println(value);
 						}
 					});
 			alert.show();
 
 		} catch (IOException e) {
 			System.out.println("FAILED");
 		}
 
 		// code used to create the activity for retrieving player info
 		final Button playerButton = (Button) findViewById(R.id.player_list);
 		playerButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, players.class);
 				startActivity(i);
 			}
 		});
 
 		//code used to create the activity for opening screen to report a kill
 		final Button killButton = (Button) findViewById(R.id.report_kill);
 		killButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, report.class);
 				startActivity(i);
 			}
 		});
 		
 		//code used to create the activity for flare gun 
 		//Rory, uncomment this for the flare gun, copy/modify for minimap.
 		//Also, you might have to add the activity in AndroidManifest.xml,
 		//the other examples in the file should be sufficient 
 		final Button miniMapButton = (Button) findViewById(R.id.mini_map);
 		miniMapButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, MiniMap.class);
 				startActivity(i);
 			}
 		});
 		
 		// Create Flare Activity
 		final Button flareButton = (Button) findViewById(R.id.shoot_flare);
 		flareButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				unimplemented();
 			}
 		});
 		
 		// Creates Strategy Map Activity
 		final Button strategyButton = (Button) findViewById(R.id.strategy_map);
 		strategyButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				unimplemented();
 			}
 		});
 		
 		// Creates Profile Activity
 		final Button profileButton = (Button) findViewById(R.id.profile);
 		profileButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
				Intent i = new Intent(HvZ_Activity.this, ProfileActivty.class);
 				startActivity(i);
 			}
 		});
 		
 	}
 	
 	public void unimplemented() {
 		System.out.println("Currently unimplemented.");
 		Toast.makeText(getApplicationContext(), "Not currently implemented.", Toast.LENGTH_SHORT).show();
 	}
 	public String getInternalCacheDirectory() {
 	    String cacheDirPath = null;
 	    File cacheDir = getCacheDir();
 	    if (cacheDir != null) {
 	        cacheDirPath = cacheDir.getPath();
 	    }
 	    return cacheDirPath;        
 	}
 }
