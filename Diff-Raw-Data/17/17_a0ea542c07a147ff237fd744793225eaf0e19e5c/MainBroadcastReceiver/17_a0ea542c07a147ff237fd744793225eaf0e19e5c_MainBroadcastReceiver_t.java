 package com.codingspezis.android.metalonly.player.stream;
 
 import android.content.*;
import android.util.Log;
 
 import com.codingspezis.android.metalonly.player.*;
 
 /**
  * 
  * broadcast receiver class for communication between other activities or
  * services
  * 
  */
 public class MainBroadcastReceiver extends BroadcastReceiver {
 
 	/**
 	 * 
 	 */
 	private final MainActivity mainActivity;
 
 	/**
 	 * @param mainActivity
 	 */
 	public MainBroadcastReceiver(MainActivity mainActivity) {
 		this.mainActivity = mainActivity;
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
		
		Log.d("MainBCReceiver", "received intent: "+intent.getAction());
		
 		// is playing?
 		if (intent.getAction().equals(PlayerService.INTENT_STATUS)) {
 			this.mainActivity
 					.setSupportProgressBarIndeterminateVisibility(false);
 			if (intent.getBooleanExtra(PlayerService.EXTRA_CONNECTED, false)) {
 				this.mainActivity.setShouldPlay(true);
 				this.mainActivity.toggleStreamButton(true);
 				this.mainActivity.setMetadataParser(new MetadataParser(intent
 						.getStringExtra(PlayerService.EXTRA_META)));
 				this.mainActivity.displayMetadata();
 			} else {
 				this.mainActivity.toggleStreamButton(false);
 			}
 			// meta data
 		} else if (intent.getAction().equals(PlayerService.INTENT_METADATA)) {
 			String metadata = intent.getStringExtra(PlayerService.EXTRA_META);
 			this.mainActivity.setMetadataParser(new MetadataParser(metadata));
 			this.mainActivity.displayMetadata();
 			this.mainActivity.displaySongs();
 		}
 	}
 }
