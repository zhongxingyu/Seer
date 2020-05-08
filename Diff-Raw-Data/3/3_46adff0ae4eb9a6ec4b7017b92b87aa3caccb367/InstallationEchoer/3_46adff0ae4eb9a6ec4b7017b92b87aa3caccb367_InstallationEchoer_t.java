 package edu.ucla.cens.Updater;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import edu.ucla.cens.systemlog.Log;
 
 /**
  * Catches installation events and rebroadcasts them so newly installed
  * packages may catch their own installation event.
  * 
  * @author John Jenkins
  */
 public class InstallationEchoer extends BroadcastReceiver
 {
 	private static final String TAG = "CENS.Updater.InstallationEchoer";
 
 	private static final String INSTALL_ACTION = "edu.ucla.cens.Updater.Installer.AppInstalled";
 	
 	/**
 	 * Catches the broadcast and echoes it by replacing only the Action.
 	 */
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.i(TAG, "Echoing new package added Intent: " + intent.getData().toString());
 		
 		Intent newIntent = new Intent(INSTALL_ACTION);
		newIntent.putExtras(intent);
		newIntent.setPackage(intent.getData().getSchemeSpecificPart());
 		newIntent.setData((new Uri.Builder()).scheme(intent.getData().getScheme()).authority(intent.getData().getSchemeSpecificPart()).build());
 		
 		context.sendBroadcast(newIntent);
 	}
 }
