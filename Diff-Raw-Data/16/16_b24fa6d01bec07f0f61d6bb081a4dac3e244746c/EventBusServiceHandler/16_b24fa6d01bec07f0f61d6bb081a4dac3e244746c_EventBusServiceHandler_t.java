 package ch.cern.atlas.apvs.eventbus.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.atmosphere.gwt.poll.AtmospherePollService;
 
 import ch.cern.atlas.apvs.eventbus.client.EventBusService;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBusIdsChangedEvent;
 
 @SuppressWarnings("serial")
 public class EventBusServiceHandler extends AtmospherePollService implements
 		EventBusService {
 
 	private static Logger log = Logger.getLogger(EventBusServiceHandler.class
 			.getName());
 	private ServerEventBus eventBus;
 	private Map<Long, ClientInfo> clients = new HashMap<Long, EventBusServiceHandler.ClientInfo>();
 
 	class ClientInfo {
 		long uuid;
 		SuspendInfo suspendInfo;
 		BlockingQueue<RemoteEvent<?>> eventQueue;
 		
 		ClientInfo(long uuid) {
 			this.uuid = uuid;
 			suspendInfo = null;
 			eventQueue = new LinkedBlockingQueue<RemoteEvent<?>>();
 		}
 		
 		public String toString() {
 			return "ClientInfo: uuid=0x"+Long.toHexString(uuid).toUpperCase()+" event queue size="+eventQueue.size()+" suspend info="+suspendInfo;
 		}
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		System.out.println("Starting EventBusService...");
 
 		eventBus = ServerEventBus.getInstance();
 		eventBus.setEventBusServiceHandler(this);
 	}
 
 	/**
 	 * Incoming event from client Broadcast it to all other clients Forward it
 	 * to server event bus
 	 */
 	@Override
 	public void fireEvent(RemoteEvent<?> event) {
 		System.err.println("Server: Received event..." + event + " "
 				+ Long.toHexString(event.getEventBusUUID()).toUpperCase());
 		// add to queues
 		getClientInfo(event.getEventBusUUID());
 
 		sendToRemote(event);
 
 		eventBus.forwardEvent(event);
 	}
 
 	/**
 	 * Provide available events for the eventbus of the client
 	 */
 	@Override
 	public List<RemoteEvent<?>> getQueuedEvents(Long eventBusUUID) {
		ClientInfo info = getClientInfo(eventBusUUID);
		
		List<RemoteEvent<?>> events = new ArrayList<RemoteEvent<?>>();
		int d = info.eventQueue.drainTo(events);
		if (d > 0) {
			log.info("Returning "+events.size()+" for uuid "+Long.toHexString(eventBusUUID).toUpperCase());
			return events;
		} else {		

			log.info("Suspend "+Long.toHexString(eventBusUUID).toUpperCase());
			info.suspendInfo = suspend();
			return null;
	    }
 	}
 
 	/**
 	 * Incoming event from server bus, broadcast to all clients
 	 * 
 	 * @param event
 	 */
 	void forwardEvent(RemoteEvent<?> event) {
 		log.info("Forward event " + event.getClass());
 		sendToRemote(event);
 	}
 
 	private synchronized void sendToRemote(RemoteEvent<?> event) {
 		if (event == null) {
 			log.warning("EBSH: sentToRemote event is null");
 			return;
 		}
 
 		// add event to all the queues (except its own, unless EventBusUUID is
 		// null)
 		int n = 0;
 		int m = 0;
 		for (Iterator<Entry<Long, ClientInfo>> i = clients.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Long, ClientInfo> entry = i.next();
 			if (event.getEventBusUUID() != entry.getKey()) {
 				n++;
 				entry.getValue().eventQueue.add(event);
 			}
 			m++;
 		}
 		log.info("Added event to " + n + " of " + m + " queues: " + event);
 
 		purgeQueues();
 	}
 
 	private synchronized void purgeQueues() {
 		int n = 0;
 		int m = 0;
 		for (Iterator<ClientInfo> i = clients.values().iterator(); i.hasNext(); m++) {
 			ClientInfo client = i.next();
 			if (client.suspendInfo == null) {
 				continue;
 			}
 
 			List<RemoteEvent<?>> events = new ArrayList<RemoteEvent<?>>();
 			int d = client.eventQueue.drainTo(events);
 			log.info("Drained "+d+" "+events.size()+" from queue "+m);
 			if (d > 0) {
 				try {
 					log.info("Server: Sending " + events.size()
 							+ " events to uuid " + Long.toHexString(client.uuid).toUpperCase());
 
 					// Debug print
 					for (Iterator<RemoteEvent<?>> j = events.iterator(); j
 							.hasNext();) {
 						RemoteEvent<?> event = j.next();
 						log.info("  "
 								+ (event != null ? event.toString() : "null"));
 					}
 					client.suspendInfo.writeAndResume(events);
 					client.suspendInfo = null;
 				} catch (IOException e) {
 					log.warning("Server: Could not write and resume event on queue "
 							+ n + e);
 					client.suspendInfo = null;
 				}
 				n++;
 			}
 		}
 		log.info("Purged " + n + " of " + m + " queues");
 	}
 
 	private ClientInfo getClientInfo(Long uuid) {
 		ClientInfo info = clients.get(uuid);
 		if (info == null) {
 			// new event bus client...
 			info = new ClientInfo(uuid);
 			clients.put(uuid, info);
 
 			// event without eventBusUUID
 			RemoteEventBusIdsChangedEvent event = new RemoteEventBusIdsChangedEvent(
 					new ArrayList<Long>(clients.keySet()));
 			// broadcast to all
 			sendToRemote(event);
 			eventBus.forwardEvent(event);
 		}
 		
 		// Debug only
 		log.info("Clients: ");
 		for (Iterator<ClientInfo> i = clients.values().iterator(); i.hasNext();) {
 			log.info("  "+i.next());
 		}
 		
 		return info;
 	}
 }
