 package pt.lsts.accu.state;
 
 import pt.lsts.accu.msg.IMCManager;
 import pt.lsts.accu.msg.IMCSubscriber;
 import pt.lsts.imc.IMCMessage;
 import pt.lsts.imc.Sms;
import android.content.Context;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class AccuSmsHandler implements IMCSubscriber {
 
 	private IMCManager mManager;
	private Context mContext;
 	
 	@Override
 	public void onReceive(IMCMessage msg) {
		
 		if (msg.getMgid() == Sms.ID_STATIC && msg.getDst() == mManager.getLocalId()) {
 			Log.i("SmsManager", "Sending an SMS to "+msg.getString("number"));
 			sendSms(msg.getString("number"), msg.getString("contents"), msg.getInteger("timeout"));
 		}
 		else {
 			Log.w("SmsManager", "Ignoring Sms request");
			System.out.println(mManager.getLocalId());
			System.out.println(msg.toString());
 		}
 	}
 	
	public AccuSmsHandler(Context context, IMCManager imcComms) {
		mContext = context;
 		mManager = imcComms;
 		mManager.addSubscriber(this, "Sms");
 	}
 	
 	private void sendSms(String destination, String text, int timeout) {
 		SmsManager sms = SmsManager.getDefault();
 		sms.sendTextMessage(destination, null, text, null, null);
		Toast.makeText(mContext, "SMS sent to "+destination, Toast.LENGTH_LONG).show();
 	}
 	
 	public void stop() {
 		
 	}
 }
