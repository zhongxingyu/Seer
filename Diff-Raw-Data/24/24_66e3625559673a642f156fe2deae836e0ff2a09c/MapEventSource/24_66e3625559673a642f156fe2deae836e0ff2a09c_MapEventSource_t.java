 package models;
 
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import play.Logger;
 import play.libs.Akka;
 import play.libs.EventSource;
 import play.libs.F.Callback0;
 import play.libs.Json;
 import akka.actor.ActorRef;
 import akka.actor.Props;
 import akka.actor.UntypedActor;
 import org.traccar.model.Position;
 
 public class MapEventSource extends UntypedActor {
 
 	public static ActorRef instance = Akka.system().actorOf(
 			Props.create(MapEventSource.class), "map");
 
 	ConcurrentMap<Long, Set<EventSource>> deviceListeners = new ConcurrentHashMap<Long, Set<EventSource>>();
 
 	public void onReceive(Object message) {
 
 		// Handle connections
 		if (message instanceof Position) {
 			final Position position = (Position) message;
			Logger.info("Messaje Postion recived."
					+ Json.toJson(position).toString());
 			notifyPosition(position);
 
 		} else if (message instanceof Listener) {
 			Logger.info("Messaje Listener recived.");
 			final Listener listener = (Listener) message;
 			// Register disconnected callback
 			listener.getEventSource().onDisconnected(new Callback0() {
 				public void invoke() {
 					getContext().self().tell(
 							new Disconnect(listener.getEventSource()), null);
 				}
 			});
 			// Add EventSource
 			for (Long deviceId : listener.getDevicesIds()) {
 				addListenerToDevice(deviceId, listener.getEventSource());
 			}
 
 		} else if (message instanceof Disconnect) {
 			Logger.info("Messaje Disconnect recived.");
 			final Disconnect disconnect = (Disconnect) message;
 			removeListener(disconnect.eventSource);
 
 		} else {
 			Logger.info("Unhandled messaje recived.");
 			unhandled(message);
 
 		}
 	}
 
 	private void addListenerToDevice(Long deviceId, EventSource eventSource) {
 		deviceListeners.putIfAbsent(deviceId, new HashSet<EventSource>());
 		deviceListeners.get(deviceId).add(eventSource);
 		logDeviceListenersStatus();
 	}
 
 	private void removeListener(EventSource eventSource) {
 		for (Entry<Long, Set<EventSource>> entry : deviceListeners.entrySet()) {
 			Logger.info("Remove " + eventSource.toString());
 			entry.getValue().remove(eventSource);
 			if (entry.getValue().isEmpty()) {
 				deviceListeners.remove(entry.getKey()); // TODO avoid possible
 														// ConcurrentModificationException
 			}
 		}
 		logDeviceListenersStatus();
 	}
 
 	private void logDeviceListenersStatus() {
 		Logger.info("Clients listening "
 				+ Integer.toString(deviceListeners.size()) + " devices.");
 		for (Entry<Long, Set<EventSource>> entry : deviceListeners.entrySet()) {
 			Logger.info("   Device " + entry.getKey() + " have : "
 					+ Integer.toString(entry.getValue().size()) + " listeners.");
 		}
 	}
 
 	private void notifyPosition(Position position) {
 		Long deviceID = position.getDeviceId();
		if (deviceID != null) {
			if (deviceListeners.containsKey(deviceID)) {
				for (EventSource es : deviceListeners.get(deviceID)) {
					Logger.info(Json.stringify(Json.toJson(position)));
					es.sendDataByName(Long.toString(deviceID),
							Json.stringify(Json.toJson(position)));
				}
			} else {
				Logger.info("No listener to device "
						+ position.getDeviceId().toString());
 			}
 		} else {
			Logger.info("Position with null device.");
 		}
 	}
 
 	public static class Disconnect {
 		final EventSource eventSource;
 
 		public Disconnect(EventSource eventSource) {
 			this.eventSource = eventSource;
 		}
 	}
 
 }
