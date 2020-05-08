 package org.racenet.racesow;
 
 import org.racenet.racesow.models.ScoresMenuAdapter;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
 * Show the online scores menu
  * 
  * @author soh#zolex
  *
  */
 public class OnlineScores extends ListActivity {
 
 	WakeLock wakeLock;
 	boolean isLoading = false;
 	int chunkLimit = 50;
 	int chunkOffset = 0;
 	ProgressDialog pd;
 	
     @Override
     /**
     * Initialite the listview
      * 
      * @param Bundle savedInstanceState
      */
     public void onCreate(Bundle savedInstanceState) {
         
     	super.onCreate(savedInstanceState);
     	
     	PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
     	this.wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "racesow");
 		
 		setListAdapter(new ScoresMenuAdapter(this));
 		
 		setContentView(R.layout.listview);
 		
 		getListView().setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 				
 				switch (pos) {
 				
 					case 0:
 						Intent i = new Intent(OnlineScores.this, OnlinePlayers.class);
 						i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 						startActivityForResult(i, 0);
 						break;
 						
 					case 1:
 						Intent i2 = new Intent(OnlineScores.this, OnlineMaps.class);
 						i2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 						startActivityForResult(i2, 0);
 						break;
 				}
 			}
         });
     }
     
     /**
 	 * Acquire the wakelock on resume
 	 */
     public void onResume() {
     	
     	super.onResume();
     	this.wakeLock.acquire();
     }
     
     /**
      * Release the wakelock when leaving the activity
      */
     public void onDestroy() {
     	
     	super.onDestroy();
     	this.wakeLock.release();
     }
 
     /**
      * Disable animations when leaving the activity
      */
 	public void onBackPressed() {
     	
     	this.finish();
     	this.overridePendingTransition(0, 0);
     }
 }
