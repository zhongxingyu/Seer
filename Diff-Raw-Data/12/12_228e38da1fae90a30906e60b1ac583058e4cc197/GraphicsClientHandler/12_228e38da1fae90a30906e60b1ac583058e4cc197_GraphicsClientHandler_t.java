 import java.util.concurrent.ConcurrentLinkedQueue;
 import org.json.*;
 import java.io.IOException;
 
 public class GraphicsClientHandler implements Runnable {
     GraphicsConnection connection;
     public GraphicsClientHandler(GraphicsConnection gConnection) {
 	connection = gConnection;
 	System.out.println("[GRAPHICS] Created new reader thread.");
     }
     @Override
     public void run(){
 	while(true){
 	    try {
 		JSONObject o = read();
 		if(o == null){
 		    throw new IOException("Graphics disconnected");
 		}
 		connection.input(o);
 	    }
 	    catch (IOException e){
 		System.out.println("[GRAPHICS] Graphics engine disconnected!");
 		connection.isAlive = false;
		System.exit(1);
 	    }
 	    catch (ProtocolException e){
 		try {
 		    JSONObject errorMessage = new JSONObject().put("error", e.getMessage());
 		    connection.sendMessage(errorMessage);
 		}
 		catch (JSONException f){}
 		catch (IOException g){
 		    System.out.println("urgh!");
 		}
 	    }
 	}
     }
     
     public synchronized JSONObject read() throws IOException, ProtocolException {
 	String line = connection.readLine();
 	JSONObject obj = null;
 	if(line == null){
 	    return null;
 	}
 	try {
 	    obj = new JSONObject(line);
 	    return obj;
 	}
 	catch (JSONException e){
 	    throw new ProtocolException("Invalid packet received: " + e.getMessage());
 	}
     }
 }
