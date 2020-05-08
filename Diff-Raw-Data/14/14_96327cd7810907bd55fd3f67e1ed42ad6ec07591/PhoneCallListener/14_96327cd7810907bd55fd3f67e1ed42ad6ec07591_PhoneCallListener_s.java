 package ch.unibe.scg.team3.wordfinder;
 
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 
 public class PhoneCallListener extends PhoneStateListener {
 	//code from: http://stackoverflow.com/questions/9955016/handling-android-application-pause-on-incoming-call-and-resume-after-call-end
 	
 	private boolean isPhoneCalling;
 
 	public PhoneCallListener() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	
     @Override
     public void onCallStateChanged(int state, String incomingNumber) {
 
         if (TelephonyManager.CALL_STATE_RINGING == state) {
             // phone ringing
         	isPhoneCalling = true;
         }
 
         if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
             // active
             isPhoneCalling = true;
         }
 
     }
     
     public boolean isCalling(){
 		return isPhoneCalling;
     }
 
 }
