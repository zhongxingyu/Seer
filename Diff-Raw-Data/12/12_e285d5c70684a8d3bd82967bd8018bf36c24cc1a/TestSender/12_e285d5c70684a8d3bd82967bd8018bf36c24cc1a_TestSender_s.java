 package com.wl.rabbits;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.GetResponse;
 
 public class TestSender {
 	private final static String QUEUE_NAME = "process-queue";
 	private final static String HOST_NAME = "localhost";
 	// private final static String HOST_NAME = "192.168.101.112";
 	private final static int PORT = 5672;
 
	private final static String SAMPLE_FILE = "/json/sample.json";
 
 	private StringBuilder readJSONFileToString() {
 		StringBuilder sBuilder = new StringBuilder();
 		try {
			System.out.println("PATH of the json file is :" + getClass().getResource(SAMPLE_FILE).getPath());
			BufferedReader in = new BufferedReader(new FileReader(
					getClass().getResource(SAMPLE_FILE).getPath()));
			
 			String s = null;
 			while ((s = in.readLine()) != null) {
 				sBuilder = sBuilder.append(s);
 			}
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return sBuilder;
 	}
 
 	private JSONObject parseJSON() {
 		JSONParser parser = new JSONParser();
 		JSONObject jsonObject = null;
 		try {
 			StringBuilder jsonString = readJSONFileToString();
 			Object obj = parser.parse(jsonString.toString());
 			jsonObject = (JSONObject) obj;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return jsonObject;
 	}
 
 	public String getDocumentId(JSONObject jsonObject) {
 		String rootID = null;
 		try {
 			JSONObject greenCCD = (JSONObject) jsonObject.get("greenCCD");
 			JSONObject header = (JSONObject) greenCCD.get("header");
 			JSONObject docID = (JSONObject) header.get("documentID");
 			rootID = (String) docID.get("root");
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return rootID;
 	}
 
 	public String getJSONMessage(JSONObject jsonObject) {
 		StringBuffer jsonString = new StringBuffer();
 		try {
 			jsonString = new StringBuffer(jsonObject.toJSONString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return jsonString.toString();
 	}
 
 	public void pushToQueue(String message) {
 		try {
 			ConnectionFactory factory = new ConnectionFactory();
 			factory.setHost(HOST_NAME);
 			factory.setPort(PORT);
 			Connection connection = factory.newConnection();
 			Channel channel = connection.createChannel();
 
 			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
 			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
 
 			channel.close();
 			connection.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void repeatTelecast(String message, int times) {
 
 		for (int k = 1; k <= times; k++) {
 			System.out.println("Take " + k);
 			pushToQueue(message);
 		}
 	}
 
 	public static void main(String[] args) {
 		TestSender pm = new TestSender();
 		JSONObject jsonObject = pm.parseJSON();
 		String jsonMessage = pm.getJSONMessage(jsonObject);
 		System.out.println("Pushing document ~ ["
 				+ pm.getDocumentId(jsonObject) + "] to queue ...");
 		// pm.pushToQueue(jsonMessage);
 		pm.repeatTelecast(jsonMessage, 30);
 
 	}
 
 }
