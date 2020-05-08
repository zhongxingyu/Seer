 package entropia.clubmonitor;
 
 import java.net.URL;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.paho.client.mqttv3.MqttClient;
 import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
 import org.eclipse.paho.client.mqttv3.MqttException;
 import org.eclipse.paho.client.mqttv3.MqttMessage;
 import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import entropia.clubmonitor.TernaryStatusRegister.RegisterState;
 
 public class MQTTTrigger extends PublicOnlyTrigger implements Runnable {
     private static final Logger logger =
 	    LoggerFactory.getLogger(MQTTTrigger.class);
     
     enum Status {
 	OPEN,
 	CLOSED;
     }
     
     private static final LinkedBlockingDeque<Status> queue =
 	    new LinkedBlockingDeque<>();
 
     @Override
     public void run() {
 	while (!Thread.interrupted()) {
 	    try {
 		_run();
 	    } catch (InterruptedException e) {
 		Thread.currentThread().interrupt();
 	    }
 	}
     }
 
     private static MqttClient initConnection() throws MqttException {
 	    final MemoryPersistence persistence = new MemoryPersistence();
 	    final MqttClient client = new MqttClient(
 			    Config.getMQTTURL(),
 			    "clubmonitor",
 			    persistence);
 
 	    MqttConnectOptions connOpts = new MqttConnectOptions();
 	    connOpts.setCleanSession(true);
 	    connOpts.setUserName(Config.getMQTTUser());
 	    connOpts.setPassword(Config.getMQTTPassword().toCharArray());
 
 	    client.connect(connOpts);
 
 	    return client;
     }
 
     private static final long RESTART_WAIT_SECONDS =
 	    TimeUnit.MINUTES.toMillis(1);
 
     private void _run() throws InterruptedException {
 	    try {
 		    final MqttClient client = initConnection();
 		    try {
 			    process(client);
 		    } finally {
 			    client.disconnect();
 		    }
 	    } catch(MqttException e) {
 		    logger.warn("MQTT error", e);
 		    Thread.sleep(RESTART_WAIT_SECONDS);
 	    }
     }
 
     private void process(MqttClient client) throws MqttException, InterruptedException {
 	    while(true) {
 		    final Status poll = queue.take();
 		    byte[] payload = new byte[1];
 
 		    if(poll == Status.OPEN) {
 			    payload[0] = '1';
 		    } else {
			    payload[1] = '0';
 		    }
 
 		    final MqttMessage message = new MqttMessage(payload);
 		    message.setQos(1);
 		    message.setRetained(true);
 
 		    client.publish("/public/eden/clubstatus", message);
 	    }
     }
 
     @Override
     public void trigger(TernaryStatusRegister register) {
 	if (!Config.isMQTTEnabled()) {
 	    return;
 	}
 	
 	if (register == TernaryStatusRegister.CLUB_OFFEN
 		&& register.status() == RegisterState.LOW) {
 	    logger.info("send club closed status to MQTT");
 	    queue.offer(Status.CLOSED);
 	}
 	
 	if (register == TernaryStatusRegister.CLUB_OFFEN
 		&& register.status() == RegisterState.HIGH) {
 	    logger.info("send club open status to MQTT");
 	    queue.offer(Status.OPEN);
 	}
     }
     
     public static Thread startMQTTTrigger() {
 	final Thread t = new Thread(new MQTTTrigger());
 	t.setName(MQTTTrigger.class.getCanonicalName());
 	t.start();
 	logger.info("MQTTTriggerThread started");
 	return t;
     }
 }
