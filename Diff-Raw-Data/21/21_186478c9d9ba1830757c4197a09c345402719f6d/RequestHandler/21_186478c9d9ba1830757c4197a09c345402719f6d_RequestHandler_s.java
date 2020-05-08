 package core.protocols.transport.socket.request;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import core.protocols.transport.IRequestHandler;
 
 /**
  * RequestHandler interface
  */
 public class RequestHandler {
 
 	private IRequestHandler overlay = null;
 
 	public void setOverlay(IRequestHandler overlay) {
 		this.overlay = overlay;
 	}
 
 	public void handleRequest(Socket socket) {
 		try {
 			BufferedReader pin = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 
 			PrintWriter pout = new PrintWriter(new BufferedWriter(
 					new OutputStreamWriter(socket.getOutputStream())), true);
 			String message = pin.readLine(); // receive a message
 			String response = "";
 			if (message != null)
 				response = overlay.handleRequest(message);
 			pout.println(response);// sending a response <IP>,<ID>,<Port>
 			pout.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
