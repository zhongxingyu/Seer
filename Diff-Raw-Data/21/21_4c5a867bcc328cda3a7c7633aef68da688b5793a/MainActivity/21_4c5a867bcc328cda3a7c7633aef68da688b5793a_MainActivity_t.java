 package mil.spawar.npe;
 
 import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MainActivity extends Activity {
 
     private static String TAG = "perimeterWatch";
    private WifiP2pManager manager;
    private Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
 
     /**
      * Called when the activity is first created.
      * @param savedInstanceState If the activity is being re-initialized after 
      * previously being shut down then this Bundle contains the data it most 
      * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Log.i(TAG, "onCreate");
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WifiBroadcastReciever(manager, channel, this);
         setContentView(R.layout.main);
     }
 
 }
 
