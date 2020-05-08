 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.HashMap;
import com.abhinav.rajan.ChangeCoordinates;
import com.abhinav.rajan.Notify;
import com.abhinav.rajan.RmiStarter;
 
 
 public class Player extends RmiStarter implements ChangeCoordinates{
 	
 	public Player(){
 		super(ChangeCoordinates.class);
 	}
 	
 	@Override
 	public void doCustomRmiHandling() {
 		try {
 			ChangeCoordinates engine = this;
 			ChangeCoordinates engineStub = (ChangeCoordinates)UnicastRemoteObject.exportObject(engine, 0);
             Registry registry = LocateRegistry.createRegistry(9000);
             registry.rebind(ChangeCoordinates.SERVICE_NAME, engineStub);
             System.out.println("Move player server started");
         }
         catch(Exception e) {
             e.printStackTrace();
         }		
 	}
 
 	@Override
 	public HashMap<String, Object> connectToServer(String clientKey)
 			throws RemoteException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public HashMap<String, Object> moveToLocation(String keyPressed,
 			String playerId) throws RemoteException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void heartBeat(String myKey, Notify notify) throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
