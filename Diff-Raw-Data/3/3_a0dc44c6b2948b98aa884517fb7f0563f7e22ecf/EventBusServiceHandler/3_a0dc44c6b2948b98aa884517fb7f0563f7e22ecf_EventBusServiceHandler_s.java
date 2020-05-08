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
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.atmosphere.gwt.poll.AtmospherePollService;
 
 import ch.cern.atlas.apvs.eventbus.client.EventBusService;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
 
 @SuppressWarnings("serial")
 public class EventBusServiceHandler extends AtmospherePollService implements
 		EventBusService {
 
 	private ServerEventBus eventBus;
 	private Map<Long, ClientInfo> clients = new HashMap<Long, EventBusServiceHandler.ClientInfo>();
 
 	class ClientInfo {
 		SuspendInfo suspendInfo;
 		BlockingQueue<RemoteEvent<?>> eventQueue = new LinkedBlockingQueue<RemoteEvent<?>>();
 	}
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		System.out.println("Starting EventBusService...");
 
 		eventBus = ServerEventBus.getInstance();
 		eventBus.setEventBusServiceHandler(this);
 	}
 
 	/**
 	 * Incoming event from client
 	 * Broadcast it to all other clients
 	 * Forward it to server event bus
 	 */
 	@Override
 	public void fireEvent(RemoteEvent<?> event) {
 		System.err.println("Server: Received event..." + event+" "+event.getEventBusUUID());
 		sendToRemote(event);
 
 		eventBus.forwardEvent(event);
 	}
 
 	/**
 	 * Provide available events for the eventbus of the client
 	 */
 	@Override
 	public synchronized List<RemoteEvent<?>> getQueuedEvents(Long eventBusUUID) {
 		BlockingQueue<RemoteEvent<?>> eventQueue = getEventQueue(eventBusUUID);
 		List<RemoteEvent<?>> events = new ArrayList<RemoteEvent<?>>();
 		if (eventQueue.drainTo(events) > 0) {
 			System.err.println("Server: getting next events..." + events.size());
 			for (Iterator<RemoteEvent<?>> i = events.iterator(); i.hasNext(); ) {
 				System.err.println("  "+i.next());
 			}
 			return events;
 		}
 		System.err.println("Server: getting next event...");
 		clients.get(eventBusUUID).suspendInfo = suspend();
 		return null;
 	}
 
 	/**
 	 * Incoming event from server bus, broadcast to all clients
 	 * @param event
 	 */
     void forwardEvent(RemoteEvent<?> event) {
 		// System.err.println("Server: Forward event..."+event);
 		sendToRemote(event);
 	}
 
 	private synchronized void sendToRemote(RemoteEvent<?> event) {
 		if (event == null) {
 			System.err.println("*S**S*S* event is null");
 			return;
 		}
 		
 		// add event to all the queues (except its own)
 		for (Iterator<Entry<Long, ClientInfo>> i = clients.entrySet().iterator(); i.hasNext(); ) {
 			Entry<Long, ClientInfo> entry = i.next();
 			if (event.getEventBusUUID() != entry.getKey()) {
 				entry.getValue().eventQueue.add(event);
 			}
 		}
 
 		purgeQueues();
 	}
 
 	private void purgeQueues() {
 		
 		for (Iterator<ClientInfo> i = clients.values().iterator(); i.hasNext(); ) {
 			ClientInfo client = i.next();
 			if (client.suspendInfo != null) {
 				List<RemoteEvent<?>> events = new ArrayList<RemoteEvent<?>>();
 				if (client.eventQueue.drainTo(events) > 0) {
 					try {
 						client.suspendInfo.writeAndResume(events);
 						System.err.println("Sending events..." + events.size());
 						for (Iterator<RemoteEvent<?>> j = events.iterator(); j.hasNext(); ) {
 							System.err.println("  "+j.next());
 						}
 						client.suspendInfo = null;
 					} catch (IOException e) {
 						System.err
 								.println("Server: Could not write and resume event "
 										+ e);
 					}
 				}
 			}
 		}
 		
 
 	}
 
 	private BlockingQueue<RemoteEvent<?>> getEventQueue(Long uuid) {
 		ClientInfo info = clients.get(uuid);
 		if (info == null) {
 			info = new ClientInfo();
 			clients.put(uuid, info);
 		}
 		return info.eventQueue;
 	}
 }
