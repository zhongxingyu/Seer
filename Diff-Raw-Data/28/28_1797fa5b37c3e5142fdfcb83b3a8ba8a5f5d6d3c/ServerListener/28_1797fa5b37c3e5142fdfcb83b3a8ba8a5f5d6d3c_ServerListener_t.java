 package main.game.server;
 
 import main.game.net.GameMessage;
import main.game.net.utils.*;
 
 import com.jme3.network.HostedConnection;
 import com.jme3.network.Message;
 import com.jme3.network.MessageListener;
 import com.jme3.network.Server;
 
 /**
  * this class is used by the server to listen
  * for incoming messages
  * @author simon
  */
 
 public class ServerListener implements MessageListener<HostedConnection> {
 	
 	private Server myServer;
 	
 	public ServerListener(Server myServerts){
 		myServer = myServerts;
 	}
 	
 	@Override
 	public void messageReceived(HostedConnection source, Message messageInput) {
 		if (messageInput instanceof GameMessage) {
 			GameMessage Input = (GameMessage) messageInput;
 			System.out.println("Client #" + source.getId() + ":");
 	    	if (Input.getInformation() instanceof Movement){
 	    		Movement newPos = (Movement) Input.getInformation();
 	    		System.out.println("Position: " + newPos.getPosition());
 	    		System.out.println("Rotation: " + newPos.getRotation());
 	    		System.out.println("Speed:    " + newPos.getSpeed());
 	    		
 		    	//broadcasting position messages to all clients
 		    	myServer.broadcast(messageInput);
 	    	}
 	    	else if (Input.getInformation() instanceof Ping){
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof ObjectArray){
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof SpawnEvent){
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof GameInformation){
 	    		
 	    	}
 		}
 		else
 			System.out.println("unknown Message instance!");
 			
 	}
 }
