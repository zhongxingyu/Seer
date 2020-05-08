 package nl.rug.peerbox.middleware;
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 public class RemoteHostManager {
 	
 	private static final int TIMEOUT = 1000 * 30; //should be smaller then timeout
 	private static final int FREQUENCY = 1000 * 15; //should be smaller then timeout
 	private static final Logger logger = Logger.getLogger(RemoteHostManager.class);
 	
	private final ConcurrentHashMap<Integer,RemoteHost> hosts = new ConcurrentHashMap<Integer, RemoteHost>();
 	
 		public RemoteHostManager() {
 		
 			Timer conntrol = new Timer("RemoteHostManager", true);
 			conntrol.scheduleAtFixedRate(new TimerTask() {
 				
 				@Override
 				public void run() {
 					long current = new Date().getTime();
 					logger.debug("Check hosts");
 					for (RemoteHost h : hosts.values()) {
 						if (h.getLastLifeSign()+TIMEOUT < current) {
 							logger.info(h.getHostID() + "is dead");
 							hosts.remove(h.getHostID());
 						}
 					}
 					
 				}
 			}, 0, FREQUENCY);
 		}
 		
 		
 		
 		RemoteHost getRemoteHost(int id) {
 			return hosts.get(id);
 		}
 
 
 
 		public void addRemoteHost(int peerID, RemoteHost p) {
 			hosts.put(peerID, p);
 		}
 		
 		
 	
 }
