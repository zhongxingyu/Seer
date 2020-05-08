 package edu.jhuapl.sages.mobile.lib.receiver;
 
 import java.io.File;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 
 import javax.crypto.NoSuchPaddingException;
 
 import org.spongycastle.util.encoders.Base64;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 import android.widget.Toast;
 import edu.jhuapl.sages.mobile.lib.SagesConstants;
 //import edu.jhuapl.sages.mobile.lib.app.crypto.ShowDecryptedMessageActivity;
 //import edu.jhuapl.sages.mobile.lib.app.tests.crypto.SagesKeyTest;
 import edu.jhuapl.sages.mobile.lib.crypto.engines.CryptoEngine;
 import edu.jhuapl.sages.mobile.lib.crypto.persisted.KeyStoreI;
 import edu.jhuapl.sages.mobile.lib.crypto.persisted.SagesKey.KeyEnum;
 import edu.jhuapl.sages.mobile.lib.crypto.persisted.SagesKeyException;
 import edu.jhuapl.sages.mobile.lib.crypto.persisted.SagesKeyStore;
 import edu.jhuapl.sages.mobile.lib.crypto.persisted.SagesPrivateKey;
 import edu.jhuapl.sages.mobile.lib.crypto.persisted.SagesPublicKey;
 import edu.jhuapl.sages.mobile.lib.message.DataMessage;
 import edu.jhuapl.sages.mobile.lib.message.KeyExchangeMessage;
 import edu.jhuapl.sages.mobile.lib.message.MessageBuilderUtil;
 import edu.jhuapl.sages.mobile.lib.message.SagesMessage;
 import edu.jhuapl.sages.mobile.lib.message.SagesMessage.MsgTypeEnum;
 
 /**
  * TODO:
  * - if exception occurs during key-exch, need to send back NACK
  * - need to keep track of state of exchange model to ensure all steps occurred and in sync both sides
  * 
  * @author pokuam1
  * @date May, 5 2013
  */
 public class SagesSmsReceiver extends BroadcastReceiver {
 
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Bundle extras = intent.getExtras();
 		
 		if (extras == null) {
 			return;
 		}
 
 		Toast.makeText(context, "SMS Received (SagesSmsReceiver)", Toast.LENGTH_SHORT).show();
 		
 		Object[] pdus = (Object[])extras.get("pdus");
 		for (int i = 0; i < pdus.length; i++){
 			SmsMessage sms = SmsMessage.createFromPdu((byte[])pdus[i]);
 			String sender = sms.getOriginatingAddress();
 			String body = sms.getMessageBody();
 			
 			/**
 			 * if sms body contains the text for "key_exch", then process as key exchange message.
 			 *  -> extracts the public key
 			 *  -> saves public key for this sender to the keystore
 			 *  
 			 * if sms body contains the text for "data", and also "enc aes" then process as an encrypted msg
 			 */
 			boolean keyExchangeSuccess = false;
 			/**
 			 * 1. determine message type -> msg type
 			 * 	  1) KEY_EXCHANGE (AES, RSA)
 			 *    2) DATA
 			 *    3) STATUS
 			 * 2. dispatch on msg type
 			 * 
 			 */
 			try {
 				determineAndHandleMessageType(context, intent, sms);
 			} catch (SagesKeyException e) {
 				e.printStackTrace();
 				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
 
 			}
 		}
 	}
 	
 	protected void determineAndHandleMessageType(Context context, Intent intent, SmsMessage sms) throws SagesKeyException{
 		
 		String sender = sms.getOriginatingAddress();
 		String body = sms.getMessageBody();
 		
 		/**
 		 * 1. determine message type -> msg type
 		 * 	  1) KEY_EXCHANGE (AES, RSA)
 		 *    2) DATA
 		 *    3) STATUS
 		 * 2. dispatch on msg type
 		 * 
 		 */
 		if (body.contains(SagesMessage.data)){
 			handleDataMessage(context, intent, sender, body);
 		} else if (body.contains(SagesMessage.key_exch)){
 			handleKeyExchangeMessage(context, sender, body);
 		} else if (body.contains(SagesMessage.reply)){ 
 			handleReplyMessage(context, null, sender, body);
 		}
 	
 	}
 
 	
 	/**
 	 * @param sender
 	 * @param body
 	 * @throws SagesKeyException 
 	 */
 	protected void handleDataMessage(Context context, Intent intent, String sender, String body) {
 		if (!body.contains(SagesMessage.data)){ //TODO Handle this case.
 			return;
 		}
 		
 		Intent broadcast = new Intent("edu.jhuapl.sages.mobile.lib.android.provider.Telephony.SMS_RECEIVED");
 		broadcast.putExtras(intent.getExtras());
 		context.sendBroadcast(broadcast);
 	}
 	
 
 
 	/**
 	 * @param sender
 	 * @param body
 	 * @throws SagesKeyException 
 	 */
 	protected void handleKeyExchangeMessage(Context context, String sender, String body) throws SagesKeyException {
 		//TODO: when production key store should live on internal device storage. during testing allows to have visibility.
 		File file = SagesConstants.KEYSTORE_FILE;
 		SagesKeyStore keystore = new SagesKeyStore(file);
 
 		
 		if(body.contains(SagesMessage.init)){ 
 			Toast.makeText(context, "Key exchange initiated by " + sender, Toast.LENGTH_SHORT).show();
 			/* Just recieved a KeyExchange[init] from sender, so I will need:
 			 * 1) extract sender's key and save key to my keystore
 			 * 2) return my own keys back to the sender by sending a KeyExchange[reply] as SMS
 			 *    2a) also, if i don't have my keys stored, save them (intend to only encounter this in testing)
 			 */
 			
 			if (body.contains("aes")){ //TODO this is for quick test only
 				KeyExchangeMessage keyExch = new KeyExchangeMessage();
 				keyExch.setSender(sender);
 				keyExch.processRawMessage(body);
 				SagesPrivateKey privKey = (SagesPrivateKey) keyExch.getKey();
 				
 				keystore.addKey(privKey);
 				//TODO patch for real use
 //				Intent intentDecrypt = new Intent(context, ShowDecryptedMessageActivity.class);
 //				intentDecrypt.putExtra("msg_body", body);
 //				intentDecrypt.putExtra("msg_sender", sender);
 //				intentDecrypt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //				context.startActivity(intentDecrypt);
 			} else {
 			//1)
 			KeyExchangeMessage keyExch = new KeyExchangeMessage();
 			keyExch.setSender(sender);
 			keyExch.processRawMessage(body);
 			SagesPublicKey pubKey = (SagesPublicKey) keyExch.getKey();
 			
 			keystore.addKey(pubKey);
 			}
 			//2)
 			
 			String msgHeader = MessageBuilderUtil.genMetaDataHeader(MsgTypeEnum.KEY_EXCH, "reply");
 			//TODO patch for real use
 			String msgBody = SagesMessage.my_key ;//+ SagesKeyTest.testData + "- for key:" + MainActivity.NUMBER_THIS_DEVICE + " - " + Calendar.MILLISECOND;
 //			String msgBody = SagesMessage.my_key + SagesKeyTest.testData + "- for key:" + MainActivity.NUMBER_THIS_DEVICE + " - " + Calendar.MILLISECOND;
 //			try {
 //				MainActivity.saveOwnKeyPriorToSMS(context, "+" + MainActivity.NUMBER_THIS_DEVICE, (msgHeader + msgBody));
 //			} catch (SagesKeyException e1){
 //				Toast.makeText(context, "Unable to save own key. Key exchange reply will not be sent to " + sender, Toast.LENGTH_SHORT).show();
 //			}
 			
 			SmsManager smsManager = SmsManager.getDefault();
 			smsManager.sendTextMessage(sender, null, msgHeader + msgBody, null, null);
 		}
 		
 		Toast.makeText(context, "Public key stored for sender " + sender, Toast.LENGTH_SHORT).show();
 		
 		/**
 		 * Now time to respond by sending own(aka 'remote' to remove conufsion) public key back to the initiator
 		 * TOOD: break this into another process?
 		 */
 	}
 
 
 	/**
 	 * @param sender
 	 * @param body
 	 * @throws SagesKeyException 
 	 */
 	protected void handleReplyMessage(Context context, KeyStoreI keystore, String sender, String body) throws SagesKeyException {
 		Toast.makeText(context, "Key exchange reply from " + sender, Toast.LENGTH_SHORT).show();
 		/* This has the remote key from the device I requested key for, and it needs to be saved to my keystore 
 		 * 1) save key for the remote sender
 		 * 
 		 */
 		KeyExchangeMessage keyExch = new KeyExchangeMessage();
 		keyExch.setSender(sender);
 		keyExch.processRawMessage(body);
 		SagesPublicKey pubKey = (SagesPublicKey) keyExch.getKey();
 		
 		keystore.addKey(pubKey);
 	}
 	
 }
