 package edu.vub.at.nfcpoker.smartwatch;
 
 import android.content.Intent;
 import android.util.Log;
 
 import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
 import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
 import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;
 
 public class WePokerSWService extends ExtensionService {
 
 	static final String SMARTWATCH_KEY = "edu.vub.at.nfcpoker.smartwatch.key";
 	
 	public WePokerSWService() {
 		super(SMARTWATCH_KEY);
 	}
 
 	@Override
 	protected RegistrationInformation getRegistrationInformation() {
 		return new WePokerSWRegistrationInformation(this);
 	}
 
 	@Override
 	protected boolean keepRunningWhenConnected() {
 		return false;
 	}
 
 	@Override
 	public WidgetExtension createWidgetExtension(String hostAppPackageName) {
 		return new WePokerWidgetExtension(this, hostAppPackageName);
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
		Log.d("wePoker service", "start");
 	}
 }
