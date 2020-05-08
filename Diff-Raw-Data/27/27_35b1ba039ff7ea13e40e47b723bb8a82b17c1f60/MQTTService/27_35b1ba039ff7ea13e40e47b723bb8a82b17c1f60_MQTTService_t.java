 package se.chalmers.pd.dashboard;
 
 import org.eclipse.paho.client.mqttv3.MqttCallback;
 import org.eclipse.paho.client.mqttv3.MqttClient;
 import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
 import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
 import org.eclipse.paho.client.mqttv3.MqttException;
 import org.eclipse.paho.client.mqttv3.MqttMessage;
 import org.eclipse.paho.client.mqttv3.MqttTopic;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 
 /**
  * This service launches an MQTT client in a separate thread and subscribes to the
  * systems basic topics. When a message is received, it broadcasts the message using
  * custom intent filters.
  *
  */
 public class MQTTService extends Service {
 
 	public static final String MQTT_STATUS_INTENT = "se.chalmers.pd.dashboard.mqtt.STATUS";
 	public static final String MQTT_STATUS_MESSAGE = "se.chalmers.pd.dashboard.mqtt.STATUS_MESSAGE";
 	public static final String MQTT_MESSAGE_RECEIVED_INTENT = "se.chalmers.pd.dashboard.mqtt.MESSAGE_RECEIVED";
    public static final String MQTT_MESSAGE_RECEIVED_TOPIC = "se.chalmers.pd.dashboard.mqtt.MESSAGE_RECEIVED_TOPIC";
    public static final String MQTT_MESSAGE_RECEIVED_PAYLOAD = "se.chalmers.pd.dashboard.mqtt.MESSAGE_RECEIVED_PAYLOAD";
 
 	private MqttClient mqttClient;
 
 	@Override
 	public void onCreate() {
 
 	}
 
 	@Override
 	public void onStart(final Intent intent, final int startId) {
 		connect();
 	}
 
 	@Override
 	public int onStartCommand(final Intent intent, int flags, final int startId) {
 		connect();
 		return START_STICKY;
 	}
 
 	private void connect() {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					String tmpDir = Environment.getExternalStorageDirectory() + "/infotainment/";
 					MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
					mqttClient = new MqttClient("tcp://192.168.43.147:1883", "dashboard", dataStore);
 					mqttClient.setCallback(new CustomMqttCallback());
 					mqttClient.connect();
 					mqttClient.subscribe("/app/webapp");
 				} catch (MqttException e) {
 					e.printStackTrace();
 				}
 			}
 
 			class CustomMqttCallback implements MqttCallback {
 
 				@Override
 				public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
					Log.d("MQTTService", "messageArrived " + "topic:" + topic.toString() + ", message:" + message.toString());
 					broadcastServiceStatus("messageArrived");
 					broadcastReceivedMessage(topic.toString(), message.toString());
 				}
 
 				@Override
 				public void deliveryComplete(MqttDeliveryToken token) {
					Log.d("MQTTService", "deliveryComplete " + "token:" + token);
 				}
 
 				@Override
 				public void connectionLost(Throwable cause) {
					Log.d("MQTTService", "connectionLost " + "cause:" + cause.toString());
 				}
 			}
 		}, "MQTTService").start();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		disconnect();
 		broadcastServiceStatus("Disconnected");
 	}
 
 	private void disconnect() {
 		try {
 			mqttClient.disconnect();
 		} catch (MqttException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void broadcastServiceStatus(String statusDescription) {
 		Intent broadcastIntent = new Intent();
 		broadcastIntent.setAction(MQTT_STATUS_INTENT);
 		broadcastIntent.putExtra(MQTT_STATUS_MESSAGE, statusDescription);
 		sendBroadcast(broadcastIntent);
 	}
 
 	private void broadcastReceivedMessage(String topic, String message) {
 		Intent broadcastIntent = new Intent();
 		broadcastIntent.setAction(MQTT_MESSAGE_RECEIVED_INTENT);
 		broadcastIntent.putExtra(MQTT_MESSAGE_RECEIVED_TOPIC, topic);
 		broadcastIntent.putExtra(MQTT_MESSAGE_RECEIVED_PAYLOAD, message);
 		sendBroadcast(broadcastIntent);
 	}
 
 }
