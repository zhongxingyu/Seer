 package de.uniulm.bagception.minimeusbconnectionservicecontrol;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import de.philipphock.android.lib.BroadcastActor;
 import de.uniulm.bagception.broadcastconstants.BagceptionBroadcastContants;
 
 /**
  * 
  * @author phil
  *
  */
 public class USBConnectionActor extends BroadcastActor<USBConnectionReactor>{
 
 
 	public USBConnectionActor(USBConnectionReactor reactor) {
 		super(reactor);
 	}
 
 	@Override
 	public void register(Context context) {
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_CONNECTED);
 		filter.addAction(BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_DISCONNECTED);
 		context.registerReceiver(this,filter);
 		
 	}
 
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_CONNECTED.equals(intent.getAction())){
 			reactor.onUSBConnected();
 		} else if (BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_DISCONNECTED.equals(intent.getAction())){
 			reactor.onUSBDisconnected();
 		}
 	}
 	
 	
 }
