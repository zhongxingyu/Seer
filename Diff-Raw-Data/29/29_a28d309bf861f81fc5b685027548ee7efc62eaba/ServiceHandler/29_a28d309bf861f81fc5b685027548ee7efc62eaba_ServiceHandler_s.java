 /**
  *
  */
 package org.nexyu.nexyuAndroid.service;
 
 import java.lang.ref.WeakReference;
 
 import org.nexyu.nexyuAndroid.R;
 
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Message handler that call NexyuService's functions depending on the message
  * received.
  * 
  * @author Paul Ecoffet
  */
 class ServiceHandler extends Handler
 {
 	private static final String					TAG	= "NexYuServiceHandler";
 	private final WeakReference<NexyuService>	mService;
 
 	/**
 	 * Unique constructor, create a reference to the service that must be
 	 * manipulated.
 	 * 
 	 * @author Paul Ecoffet
 	 */
 	public ServiceHandler(NexyuService service)
 	{
 		mService = new WeakReference<NexyuService>(service);
 	}
 
 	/**
 	 * Callback called when a message is received. It manages which function of
 	 * the service is called depending of the type of message received.
 	 * 
 	 * @author Paul Ecoffet
 	 * @see android.os.Handler#handleMessage(android.os.Message)
 	 */
 	@Override
 	public void handleMessage(Message msg)
 	{
 		NexyuService service = mService.get();
 		Bundle data;
 		JsonObject json;
 
 		switch (msg.what)
 		{
 		case NexyuService.MSG_CONNECT:
 			data = msg.getData();
 			service.connect(data.getString("ip"), msg.getData().getInt("port"));
 			break;
 		case NexyuService.MSG_CONNECTED:
 			Log.i(TAG, "Connected message received");
 			Toast.makeText(service, "Connected", Toast.LENGTH_SHORT).show();
 			break;
 		case NexyuService.MSG_IMPOSSIBLE_CONNECT:
 			Toast.makeText(service, R.string.impossible_to_connect, Toast.LENGTH_LONG).show();
 			break;
 		case NexyuService.MSG_SEND_SMS:
 			SmsManager smsManager = SmsManager.getDefault();
 			json = ((JsonElement) msg.obj).getAsJsonObject();
			smsManager.sendTextMessage(json.get("recipient").getAsString(), null,
					json.get("body").getAsString(), null, null);
 			break;
 		default:
 			super.handleMessage(msg);
 		}
 	}
 }
