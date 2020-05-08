 package org.racenet.racesow;
 
 import java.io.InputStream;
 import org.racenet.framework.XMLParser;
 import org.racenet.racesow.models.OnlinePlayersAdapter;
 import org.racenet.racesow.models.PlayerItem;
 import org.racenet.racesow.threads.XMLLoaderTask;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 
 /**
 * Obtain and display the online players
  * 
  * @author soh#zolex
  *
  */
 public class OnlinePlayers extends XMLListActivity {
 
 	WakeLock wakeLock;
 	OnlinePlayersAdapter adapter;
 	boolean isLoading = false;
 	int chunkLimit = 50;
 	int chunkOffset = 0;
 	ProgressDialog pd;
 	
     @Override
     /**
     * Load the players and initialize the pager and adapter
      * 
      * @param Bundle savedInstanceState
      */
     public void onCreate(Bundle savedInstanceState) {
         
     	super.onCreate(savedInstanceState);
     	
     	PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
     	this.wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "racesow");
 		
     	adapter = new OnlinePlayersAdapter(this);
 		setListAdapter(adapter);
 		this.loadData();
 		
 		setContentView(R.layout.listview);
 		getListView().setOnScrollListener(new OnScrollListener() {
 			
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				
 			}
 			
 			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 
 				if (totalItemCount > 0 && visibleItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
 					
 					if (!isLoading && adapter.getCount() % chunkLimit == 0) {
 					
 						loadData();
 					}
 				}
 			}
 		});
     }
     
     /**
      * Show a loading indication and trigger the xml request
      */
     public void loadData() {
     	
     	pd = new ProgressDialog(OnlinePlayers.this);
 		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		pd.setMessage("Obtaining player ranking...");
 		pd.setCancelable(false);
 		pd.show();
     	
     	isLoading = true;
 		String url = "http://racesow2d.warsow-race.net/players.php?offset=" + this.chunkOffset + "&limit=" + this.chunkLimit;
 		new XMLLoaderTask(this).execute(url);
     }
     
     /**
      * Called by XMLLoaderTask when loading has finished
      * 
      * @param InputStream xmlStream
      */
     public void xmlCallback(InputStream xmlStream) {
     	
     	pd.dismiss();
 		
 		if (xmlStream == null) {
 			
 			new AlertDialog.Builder(this)
 	            .setMessage("Could not load the player ranking.\nCheck your network connection and try again.")
 	            .setNeutralButton("OK", new OnClickListener() {
 								
 					public void onClick(DialogInterface arg0, int arg1) {
 						
 						if (chunkOffset == 0) {
 							
 							finish();
 							overridePendingTransition(0, 0);
 						}
 					}
 				})
 	            .show();
 			
 		} else {
 		
 			XMLParser parser = new XMLParser();
 			parser.read(xmlStream);
 			NodeList positions = parser.doc.getElementsByTagName("player");
 			int numPositions = positions.getLength();
 			for (int i = 0; i < numPositions; i++) {
 
 				Element position = (Element)positions.item(i);
 		
 				PlayerItem player = new PlayerItem();
 				player.position = Integer.parseInt(parser.getValue(position, "no"));
 				player.name = parser.getValue(position, "name");
 				player.points = Integer.parseInt(parser.getValue(position, "points"));
 				
 				adapter.addItem(player);
 			}
 			
 			// adapter.notifyDataSetChanged(); // WHY THE FUCK DOESN'T THIS WORK????
 			setListAdapter(adapter);
 			getListView().setSelection(chunkOffset - 6);
 			
 			chunkOffset += chunkLimit;
 		}
 		
 		isLoading = false;
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
