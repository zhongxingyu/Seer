 package T07;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import communication.Message;
 
 import lejos.nxt.Sound;
 import lejos.nxt.comm.NXTConnection;
 import lejos.nxt.comm.RS485;
 import lejos.nxt.comm.RS485Connection;
 
 public class CommunicationServer {
 	
 	private RS485Connection connection;
 	private DataOutputStream dataOut;
 	private DataInputStream dataIn;
 	
	public static int MID_LIGHT_SENSOR_VALUE = 1;
	public static int MID_LIGHT_SENSOR_HEIGHT = 2;
	public static int MID_LIGHT_SENSOR_THETA = 3;
 	
 	public CommunicationServer() {
 		connection = RS485.waitForConnection(0, NXTConnection.PACKET);
 		
 		if (connection == null) {
 			return;
 		}
 		Sound.twoBeeps();
 		
 		dataOut = connection.openDataOutputStream();
 		dataIn = connection.openDataInputStream();
 		
 	}
 	
 	public void sent(Message message) {
 		try {
 			dataOut.writeUTF(message.getString());
 			dataOut.flush();
 		} catch (IOException e) {
 			//
 		}
 	}
 	
 	public Message receive() {
 		String messageString = null;
 		Message receivedMessage = null;
 		try {
 			messageString = dataIn.readUTF();
 			receivedMessage = new Message(messageString);
 		} catch (IOException e) {
 		}		
 		return receivedMessage;
 	}
 	
 	
 	
 
 }
