 package Networking;
 
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 /**
  * Contains connection to server. Sends data to the server at request.
  * 
  * @author Peter Zhang
  */
 public class StreamWriter {
 	private Socket socket;
 	private ObjectOutputStream oos;	
 	
 	public StreamWriter(Socket s){
 		socket = s;
 		try {
 			// Create the output stream for reading from the server
 			oos = new ObjectOutputStream(socket.getOutputStream());
 			System.out.println("StreamWriter: got stream");
 		} catch (Exception e) {
 			System.out.println("StreamWriter: Stream init fail");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Sends the Request variable to the Server. For example:
	 * <code>writer.sendData(new Request("receiveBin", "feeder1", null));</code>
 	 * 
 	 * @param req - Request variable to be sent.
 	 */
 	public void sendData(Request req) {
 		try {
 			System.out.println("StreamWriter: requesting for \" " + req + " \"");
 			oos.writeObject(req);
 			oos.flush();
 			oos.reset();
 		} catch (Exception e) {
 			System.out.println("StreamWriter: request fail");
 			e.printStackTrace();
 		}
 	}
 }
