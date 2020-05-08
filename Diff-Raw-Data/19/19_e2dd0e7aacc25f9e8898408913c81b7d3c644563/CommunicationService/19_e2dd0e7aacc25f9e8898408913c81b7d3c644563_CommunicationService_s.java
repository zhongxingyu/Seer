 package de.unidue.wiwi.tdr.kn3.rasp_home;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.util.Log;
 
 public class CommunicationService extends IntentService implements Observer<CommunicationClass.RequestMessage> {
 
 	public CommunicationService() {
 		super("CommunicationService");
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d(MainApplication.RH_TAG, "Start CommunicationService");
 		MainApplication.com.zeroconf.observer.addObserver(this);
 		MainApplication.com.server.observer.addObserver(this);
 		MainApplication.com.client.SetUserPass(MainApplication.pref.getString("pref_communication_user", "") + ":"
 				+ MainApplication.pref.getString("pref_communication_pass", ""));
 		MainApplication.com.client.SetTimeout(MainApplication.pref.getInt("pref_communication_timeout", 5000));
 		MainApplication.com.zeroconf.Start(MainApplication.pref.getInt("pref_communication_zeroconfport", 1234));
 		MainApplication.com.server.Start(MainApplication.pref.getInt("pref_communication_serverport", 8888));
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		synchronized (this) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(MainApplication.RH_TAG, "Stop CommunicationService");
 		MainApplication.com.zeroconf.observer.deleteObserver(this);
 		MainApplication.com.server.observer.deleteObserver(this);
 		MainApplication.com.zeroconf.Stop();
 		MainApplication.com.server.Stop();
 	}
 
 	@Override
 	public void update(Observable<CommunicationClass.RequestMessage> o, CommunicationClass.RequestMessage arg) {
 		Log.d(MainApplication.RH_TAG, "Get message: " + arg.method + " " + arg.type + " " + arg.name + " " + arg.attrib
 				+ " " + arg.value);
 		if (arg.method.equals("ZERO") && arg.type.equals("Backend")) {
 			MainApplication.com.client.SetServerIpPort(arg.value);
 		}
 		// MainApplication.com.server.SetAuthorizedUserPass("ich:123");
 		// MainApplication.com.client.SendRequestPost("/nodes", "hallo node");
 	}
 }
