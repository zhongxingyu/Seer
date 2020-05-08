 package AP2DX;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 
 import AP2DX.specializedMessages.*;
 
 /**
  * Specialized bufferedReader to directly parse messages from a (in our case)
  * sockets.
  * 
  * @author Maarten Inja
  */
 public class AP2DXMessageReader extends BufferedReader implements
 		IMessageReader {
 	/**
 	 * source of messages of the stream
 	 */
 	private Module source = null;
 
 	/**
 	 * destinatnion of messages of the stream, should be component self.
 	 */
 	private Module destination = null;
 
 	/**
 	 * 
 	 * @param in
 	 */
 	public AP2DXMessageReader(InputStream in, Module source) {
 		super(new InputStreamReader(in));
 		this.source = source;
 	}
 
 	/**
 	 * 
 	 * @param in
 	 */
 	public AP2DXMessageReader(InputStream in, Module source, Module destination) {
 		super(new InputStreamReader(in));
 		this.source = source;
 		this.destination = destination;
 	}
 
 	/**
 	 * Reads a message and parses it. Messag
 	 */
 	public Message readMessage() throws IOException {
 		String line = null;
 		boolean succeeded = false;
 		
 		while (!succeeded) {
 				line = readLine();
 				succeeded = true;
 		}
 		
 		AP2DXMessage message = new AP2DXMessage(line, source);
 		switch (message.getMsgType()) {
 		case AP2DX_SENSOR_SONAR:
 			return new SonarSensorMessage(message);
         case AP2DX_SENSOR_RANGESCANNER:
             return new RangeScannerSensorMessage(message);
 		case AP2DX_MOTOR_ACTION:
 			return new ActionMotorMessage(message);
         case HELLO:
             return new HelloMessage(message);
 		default:
 			System.out
 					.println("AP2DX.readMessage(), no specialized message could be parsed!");
 			return message;
 		}
 	}
 }
