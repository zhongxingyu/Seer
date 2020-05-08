 package se.chalmers.project14.activities;
 
 /*
  * Copyright (c) 2012 Henrik Andersson, Anton Palmqvist, Tomas Selldn and Marcus Tyrn
  * See the file license.txt for copying permission.
  */
 
 import java.io.IOException;
 
 import se.chalmers.project14.activities.R;
 import se.chalmers.project14.model.storage.DatabaseInput;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Splash extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 		DatabaseInput di = new DatabaseInput(this);
 		try {
 			di.readCoordinatesFile();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		Thread timer = new Thread() {
 			public void run() {
 				try {
 					sleep(3000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
 					Intent openMain = new Intent(
							"se.chalmers.project14.activities.ChooseLocationActivity");
 					startActivity(openMain);
 				}
 			}
 		};
 		timer.start();
 	}
 
 }
