 package main.game.net;
 
import main.game.net.utils.MessageType;
import main.game.net.utils.Movement;
 
 import com.jme3.network.Client;
 import com.jme3.network.Message;
 import com.jme3.network.MessageListener;
 import com.jme3.network.serializing.Serializer;
 
 /**
  * this class is made for the client
  * to listen for incoming messages
  * @author simon
  *
  */
 
 public class NetListener implements MessageListener<Client> {
 	
 	public NetListener() {
 		Serializer.registerClass(GameMessage.class);
 		System.out.println("netlistener gestartet!");
 	}
 	
 	@Override
 	public void messageReceived(Client source, Message messageInput) {
 	    if (messageInput instanceof GameMessage) {
 			GameMessage Input = (GameMessage) messageInput;
 			System.out.println("Client #" + source.getId() + ":");
 	    	if (Input.getInformation() instanceof Movement){
 	    		Movement newPos = (Movement) Input.getInformation();
 	    		System.out.println("Position: " + newPos.getPosition());
 	    		System.out.println("Rotation: " + newPos.getRotation());
 	    		System.out.println("Speed:    " + newPos.getSpeed());
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof Ping){
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof ObjectArray){
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof SpawnEvent){
 	    		
 	    	}
 	    	else if (Input.getInformation() instanceof GameInformation){
 	    		
 	    	}
 	    	else
 	    		System.out.println("unknown messagetype!");
 	      }
 	    else
 	    	System.out.println("unknown Message instance!");
 	}
 
 }
