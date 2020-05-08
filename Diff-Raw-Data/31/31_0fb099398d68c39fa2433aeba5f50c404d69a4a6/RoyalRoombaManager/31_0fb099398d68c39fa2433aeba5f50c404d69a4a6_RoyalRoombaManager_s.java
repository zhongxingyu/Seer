 package mainpackage;
 import com.ibm.mqtt.*;
 
 public class RoyalRoombaManager implements MqttSimpleCallback {
 	
 	public static IMqttClient mqttclient;
 	public static RoombaControl roomba1;
 	//public static RoombaControl roomba2;
 	public static String port1 = "COM40";
 	public static String port2 = "COM41";
 	
 	public static void main(String args[]){
 		
 		//Start MQTT client
 		RoyalRoombaManager rrm = new RoyalRoombaManager();
 		rrm.startMQTT();
 		
 		//Instantiate roombas
 		roomba1 = new RoombaControl(port1);
 		//roomba2 = new RoombaControl(port2);		
 	}
 	
 	public void startMQTT(){
 		
 		try{
 			mqttclient = MqttClient.createMqttClient("tcp://localhost@1883", null);
 			mqttclient.connect("1", true, (short)5);
 			mqttclient.registerSimpleHandler(this);
 		}catch(Exception e){
 			e.printStackTrace();
 		}		
 		
		String[] stringArray = {"roomba1", "roomba2"};
 		int[] numArray = {1,1};
 		
 		try{
 			mqttclient.subscribe(stringArray, numArray);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void connectionLost(){
 		
 	}
 	
 	public void publishArrived(String topicName, byte[] payload, int qos, boolean retain){
 		
 		if(topicName.equals("toroomba1")){
 			roomba1.roombaAction(new String(payload));
 		}else{
 			
 			if(topicName.equals("toroomba2")){
 				System.out.println(topicName);
 				//roomba2.roombaAction(new String(payload));
 			}
 		}
 	}
 	
 	//Pushes roomba messages to the MQTT broker
 	public static void publish(String portname,String thismessage){
 		
 		System.out.println(thismessage);
 		
 		try{
 			//Publish to roomba1
 			if(portname.equals(port1)){
 				mqttclient.publish("byroomba1", thismessage.getBytes(), 1, false);
 			}
 			
 			//Publish to roomba2
 			if(portname.equals(port2)){
 				mqttclient.publish("byroomba2", thismessage.getBytes(), 1, false);
 			}
 			
 			//Publish to server
 			if(portname.equals("server")){
 				
 			}
 		}catch(Exception e){
 	
 		}
 	}
 }
