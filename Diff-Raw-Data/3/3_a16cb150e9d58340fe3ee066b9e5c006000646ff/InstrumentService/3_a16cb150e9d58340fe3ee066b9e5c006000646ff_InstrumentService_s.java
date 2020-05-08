 package com.davidjennes.ElectroJam;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceEvent;
 import javax.jmdns.ServiceListener;
 
 import android.app.Service;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class InstrumentService extends Service {
 	private static final String TAG = "InstrumentService";
 	private static final String LOCK_NAME = "ElectroJamInstrument-BonjourLock";
 	private MulticastLock m_lock;
 	
 	@Override
     public void onCreate() {
         super.onCreate();
         
         // Acquire lock to be able to process multicast
         WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
         m_lock = wifi.createMulticastLock(LOCK_NAME);
         m_lock.setReferenceCounted(true);
         m_lock.acquire();
     }
 	
 	@Override
     public IBinder onBind(Intent intent) {
         return m_binder;
     }
 	
 	public void onDestroy() {
         if (m_lock != null)
         	m_lock.release();
    }
 	
 	private final IInstrumentService.Stub m_binder = new IInstrumentService.Stub() {
		@Override
 		public void loadSamples(Map samples) throws RemoteException {
 			try {
 				final JmDNS jmdns = JmDNS.create();
 				String type = "_workstation._tcp.local.";
 				
 				ServiceListener listener = new ServiceListener() {
 			        public void serviceResolved(ServiceEvent ev) {
 			            Log.d(TAG, "Service resolved: "
 			                     + ev.getInfo().getQualifiedName()
 			                     + " port:" + ev.getInfo().getPort());
 			        }
 			        public void serviceRemoved(ServiceEvent ev) {
 			        	Log.d(TAG, "Service removed: " + ev.getName());
 			        }
 			        public void serviceAdded(ServiceEvent event) {
 			            // Required to force serviceResolved to be called again
 			            // (after the first search)
 			            jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
 			        }
 			    };
 			    jmdns.addServiceListener(type, listener);
 			    
 			    // ...
 			    
 			    jmdns.removeServiceListener(type, listener);
 			    jmdns.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
		@Override
 		public void sendEvent(String sample, int mode) throws RemoteException {
 			Log.d(TAG, "playing sample: " + sample + " mode: " + mode);
 		}
 	};
 }
