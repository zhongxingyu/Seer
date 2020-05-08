 package factory;
 
 import java.util.ArrayList;
 
 import Networking.Request;
 import Networking.Server;
 import Utils.Constants;
 import agent.FCSAgent;
 
 /**
  * Factory Control System. Hub for GUI controls to the factory operations. 
  * 
  * @author Peter Zhang
  */
 public class FCS {
 	private ArrayList<Order> queue = new ArrayList<Order>();
 	private ArrayList<KitConfig> kitConfigs = new ArrayList<KitConfig>();
 	private ArrayList<PartType> partTypes = (ArrayList<PartType>) Constants.DEFAULT_PARTTYPES.clone();
 	
 	private FCSAgent agent;
 	private Server server;
 	
 	public FCS(FCSAgent a, Server server) {
 		agent = a;
 		this.server = server;
 	}
 	
 	public void updateParts() {
 		server.sendData(new Request(Constants.FCS_UPDATE_PARTS, Constants.ALL_TARGET, partTypes));
 	}
 	
 	public void updateKits() {
 		server.sendData(new Request(Constants.FCS_UPDATE_KITS, Constants.ALL_TARGET, kitConfigs));
 	}
 	
	// to clients
	public void updateQueue() {
		server.sendData(new Request(Constants.FCS_UPDATE_ORDERS, Constants.ALL_TARGET, queue));
	}
 	
	// from agent
 	public void updateQueue(ArrayList<Order> q) {
 		queue = q;
 	} 
 	
 	public void newPart(PartType pt) {
 		partTypes.add(pt);
		updateParts();
 	}
 	
 	public void newKit(KitConfig kc) {
 		kitConfigs.add(kc);
		updateKits();
 	}
 	
 	public void addOrder(Order o) {
 		queue.add(o);
		updateQueue();
 		
 		//TODO: put in the right agent message
 	}
 	
 	public void startProduction() {
 		agent.msgStartProduction();
 	}
 
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.FCS_NEW_PART)) {
 			PartType pt = (PartType) req.getData();
 			newPart(pt);
 		} else if (req.getCommand().equals(Constants.FCS_NEW_KIT)) {
 			KitConfig kc = (KitConfig) req.getData();
 			newKit(kc);
 		} else if (req.getCommand().equals(Constants.FCS_ADD_ORDER)) {
 			Order o = (Order) req.getData();
 			addOrder(o);
 		} else if (req.getCommand().equals(Constants.FCS_START_PRODUCTION)) {
 			startProduction();
 		} else if (req.getCommand().equals(Constants.FCS_STOP_KIT)) {
 			// agents aren't ready
 		} else if (req.getCommand().equals(Constants.FCS_STOP_PRODUCTION)) {
 			// agents aren't ready
 		}
 	}
 	
 }
