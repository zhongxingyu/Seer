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
 	
 	/**
 	 * Called to send clients the updated queue. 
 	 */
 	public void updateQueue() {
 		server.sendData(new Request(Constants.FCS_UPDATE_ORDERS, Constants.ALL_TARGET, queue));
 	}
 	
 	/**
 	 * Called only by FCSAgent to synchronize order queues from them.
 	 * @param q updated order queue
 	 */
 	public void updateQueue(ArrayList<Order> q) {
 		queue = q;
 	} 
 	
 	public void newPart(PartType pt) {
 		partTypes.add(pt);
 		updateParts();
 		
 		agent.msgAddNewPartType(pt);
 	}
 	
 	public void editPart(PartType pt) {
 		// loops through ArrayList to find based on id, then replaces 
 		for(int i = 0; i < partTypes.size(); i++) {
 			if(partTypes.get(i).equals(pt)) {
 				partTypes.set(i, pt);
 			}
 		}
 	}
 	
 	public void deletePart(PartType pt) {
 		// loops through ArrayList to find based on id, then deletes 
 		for(int i = 0; i < partTypes.size(); i++) {
 			if(partTypes.get(i).equals(pt)) {
 				partTypes.remove(i);
 			}
 		}
 		
 		//TODO: add in agent call so to stop drawing bin
 	}
 	
 	public void newKit(KitConfig kc) {
 		kitConfigs.add(kc);
 		updateKits();
 	}
 	
 	public void editKit(KitConfig kc) {
 		// loops through ArrayList to find based on id, then replaces 
 		for(int i = 0; i < kitConfigs.size(); i++) {
 			if(kitConfigs.get(i).equals(kc)) {
 				kitConfigs.set(i, kc);
 			}
 		}
 	}
 	
 	public void deleteKit(KitConfig kc) {
 		// loops through ArrayList to find based on id, then deletes 
 		for(int i = 0; i < kitConfigs.size(); i++) {
 			if(kitConfigs.get(i).equals(kc)) {
 				kitConfigs.remove(i);
 			}
 		}
 	}
 	
 	public void addOrder(Order o) {
 		//TODO: might not be necessary, since agent will update ours anyway.
 		queue.add(o);
 		updateQueue();
 		
 		agent.msgAddKitsToQueue(o);
 	}
 	
 	public void startProduction() {
 		agent.msgStartProduction();
 	}
 
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.FCS_NEW_PART)) {
 			PartType pt = (PartType) req.getData();
 			newPart(pt);
 		} else if (req.getCommand().equals(Constants.FCS_EDIT_PART)) {
 			PartType pt = (PartType) req.getData();
 			editPart(pt);
 		} else if (req.getCommand().equals(Constants.FCS_DELETE_PART)) {
 			PartType pt = (PartType) req.getData();
 			deletePart(pt);
 		} else if (req.getCommand().equals(Constants.FCS_NEW_KIT)) {
 			KitConfig kc = (KitConfig) req.getData();
 			newKit(kc);
 		} else if (req.getCommand().equals(Constants.FCS_EDIT_KIT)) {
 			KitConfig kc = (KitConfig) req.getData();
 			editKit(kc);
 		} else if (req.getCommand().equals(Constants.FCS_DELETE_KIT)) {
 			KitConfig kc = (KitConfig) req.getData();
 			deleteKit(kc);
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
