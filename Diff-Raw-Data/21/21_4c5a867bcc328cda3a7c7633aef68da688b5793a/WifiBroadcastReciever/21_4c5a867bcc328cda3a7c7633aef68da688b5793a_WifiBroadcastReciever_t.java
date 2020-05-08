 package mil.spawar.npe;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 
 public class WifiBroadcastReciever extends BroadcastReceiver {
 
     private WifiP2pManager manager;
     private Channel channel;
     private MainActivity activity;
     
    public WifiBroadcastReciever(WifiP2pManager manager, Channel channel,MainActivity activity) 
     {
         super();
         this.manager = manager;
         this.channel = channel;
         this.activity = activity;
     }
 	@Override
 	public void onReceive(Context context, Intent intent) 
 	{
 
 		
 	}
 
 }
