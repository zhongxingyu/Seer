 /**
  *
  */
 package org.nexyu.nexyuAndroid.client.protocol;
 
 import java.util.ArrayList;
 
 import android.telephony.SmsMessage;
 
 import com.google.gson.JsonObject;
 
 /**
  * SMS messages to send formatted so as to be sent over the network.
  * 
  * @author Paul Ecoffet
  * 
  */
 public class SMSToSendNetworkMessage extends NetworkMessage
 {
 	/**
 	 * @param messages
 	 *            The list of received messages.
 	 */
 	public SMSToSendNetworkMessage(ArrayList<SmsMessage> messages)
 	{
		super("messages", null);
 		JsonObject data = new JsonObject();
 		SmsMessage sms = messages.get(0);
 		StringBuilder body = new StringBuilder();
 		for (SmsMessage message : messages)
 		{
 			body.append(message.getMessageBody());
 		}
 		data.addProperty("sender", sms.getOriginatingAddress());
 		data.addProperty("timestamp", sms.getTimestampMillis());
 		data.addProperty("body", body.toString());
 
 		super.setData(data);
 	}
 }
