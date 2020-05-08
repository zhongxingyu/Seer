 package com.worthwhilegames.cardgames.shared.connection;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Common methods for Bluetooth Client and Server
  */
 public abstract class ConnectionCommon {
 
 	/**
 	 * Perform a write operation
 	 * 
 	 * @param service the BluetoothConnectionService to write to
 	 * @param obj the object to write
 	 * @return whether the message was written or not
 	 */
	protected boolean performWrite(final ConnectionService service, int messageType, final Object obj) {
 		// If we aren't connected, return false
 		if (service.getState() != ConnectionConstants.STATE_CONNECTED) {
 			return false;
 		}
 
 		try {
 			// Wrap the data into a JSON object with a messageType
 			//
 			// {
 			// 		"MSG_TYPE"	: messageType,
 			//		"DATA"		: obj.toString()
 			// }
 
			final JSONObject json = new JSONObject();
 			if (obj != null) {
 				json.put(ConnectionConstants.KEY_MSG_DATA, obj.toString());
 			}
 			json.put(ConnectionConstants.KEY_MESSAGE_TYPE, messageType);
 
			// Perform the networking stuff on a separate thread
			// due to some devices throwing a NetworkOnMainThreadException
			new Thread(new Runnable() {
				@Override
				public void run() {
					service.write(json.toString().getBytes());
				}
			}).start();
 		} catch (JSONException e) {
 			// If we encounter a JSON error, just send the message without the
 			// message type parameter
 			e.printStackTrace();
 
			// Perform the networking stuff on a separate thread
			// due to some devices throwing a NetworkOnMainThreadException
			new Thread(new Runnable() {
				@Override
				public void run() {
					service.write(obj.toString().getBytes());
				}
			}).start();
 		}
 
 		return true;
 	}
 
 }
