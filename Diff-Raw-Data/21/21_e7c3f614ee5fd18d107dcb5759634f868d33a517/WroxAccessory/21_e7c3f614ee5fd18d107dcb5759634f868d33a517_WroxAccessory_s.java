 package com.wiley.wroxaccessories;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 
 public class WroxAccessory {
 	private static final int USB_ACCESSORY_10 = 0;
 	private static final int USB_ACCESSORY_12 = 1;
 	private static final int BT_ACCESSORY = 3;
 	private Context mContext;
 	private MonitoringThread mMonitoringThread;
 	public WroxAccessory(Context context) {
 		mContext = context;
 	}
 	public void connect(int mode, Connection connection) throws IOException {
 		mMonitoringThread = new MonitoringThread(mode, connection);
 		mMonitoringThread.mConnection.getOutputStream().write(MQTT.connect());
 	}
 	public void publish(String topic, byte[] message) throws IOException {
 		new WriteHelper().execute(MQTT.publish(topic, message));
 	}
 	public void subscribe(BroadcastReceiver receiver, String topic, int id) throws 
 			IOException {
 		this.receiver = receiver;
 		new WriteHelper().execute(MQTT.subscribe(id, topic, MQTT.AT_MOST_ONCE));
 		String sub = WroxAccessory.SUBSCRIBE + "." + topic;
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(sub);
 		mContext.registerReceiver(receiver, filter);
		return sub;
 	}
 	public void unsubscribe(String topic, int id) throws IOException {
 		new WriteHelper().execute(MQTT.unsubscribe(id, topic));
 		mContext.unregisterReceiver(receiver);
 	}
 	public void pingreq() throws IOException {
 		new WriteHelper().execute(MQTT.ping());
 	}
 	public void disconnect() throws IOException {
 		if (mMonitoringThread.mConnection != null) {
 			mMonitoringThread.mConnection.close();
 		}
 	}
 	private class MonitoringThread implements Runnable {
 		Connection mConnection;
 		private ArrayList<String> subscriptions;
 		public MonitoringThread(int mode, Connection connection) {
 			mConnection = connection;
 			subscriptions = new ArrayList<String>();
 		}
 		public void run() {
 			int ret = 0;
 			byte[] buffer = new byte[16384];
 			while (ret >= 0) {
 				try {
 					ret = mConnection.getInputStream().read(buffer);
 				} catch (IOException e) {
 					break;
 				}
 				if (ret > 0) {
 					MQTTMessage msg = MQTT.decode(buffer);
 					if (msg.type == MQTT.PUBLISH) {
 						Intent broadcast = new Intent();
 						broadcast.setAction(SUBSCRIBE + "." +
 								msg.variableHeader.get("topic_name"));
 						broadcast.putExtra("payload", msg.payload);
 						mContext.sendBroadcast(broadcast);
 					} else if (msg.type == MQTT.SUBSCRIBE) {
 						String topic = new String(msg.payload);
						if (!subscriptions.contain(topic))
 							subscriptions.add(topic);
 					} else if (msg.type == MQTT.UNSUBSCRIBE) {
 						String topic = new String(msg.payload);
 						boolean unsubscribed = subscriptions.remove(topic);
 					}
 				}
 			}
 		}
 	}
 	
 	private class WriteHelper extends AsyncTask<byte[], Void, Void> {
 		@Override
 		protected Void doInBackground(byte[]... params) {
 			try {
 				mMonitoringThread.mConnection.getOutputStream().write(params[0]);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 }
