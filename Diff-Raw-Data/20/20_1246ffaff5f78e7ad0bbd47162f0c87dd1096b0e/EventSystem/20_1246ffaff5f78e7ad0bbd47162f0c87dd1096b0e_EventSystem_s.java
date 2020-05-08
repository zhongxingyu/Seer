 package com.h5n1.eventsys;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.h5n1.eventsys.events.ApplicationEvent;
 import com.h5n1.eventsys.events.CompanionEvent;
 import com.h5n1.eventsys.events.Event;
 import com.h5n1.eventsys.events.EventState;
 import com.h5n1.eventsys.events.GPSEvent;
 import com.h5n1.eventsys.events.MotionEvent;
 import com.h5n1.eventsys.events.NavigationEvent;
 import com.h5n1.eventsys.events.RFIDEvent;
 import com.h5n1.eventsys.listener.EventListener;
 import com.h5n1.eventsys.JsonRequester;
 
 @SuppressWarnings("rawtypes")
 public class EventSystem {
 	private static EventSystem instance;
 
 	private EventSystem() {
 
 	}
 
 	public static EventSystem getInstance() {
 		if (instance != null) {
 			return instance;
 		}
 		return instance = new EventSystem();
 	}
 
 	// =================================================
 
 	private static Queue<Event> incomingEvents = new LinkedList<Event>();
 	private static Queue<Event> outgoingEvents = new LinkedList<Event>();
 
 	private ArrayList<EventListener<RFIDEvent>> rfidEventListeners = new ArrayList<EventListener<RFIDEvent>>();;
 	private ArrayList<EventListener<GPSEvent>> gpsEventListeners = new ArrayList<EventListener<GPSEvent>>();
 	private ArrayList<EventListener<MotionEvent>> motionEventListeners = new ArrayList<EventListener<MotionEvent>>();
 	private ArrayList<EventListener<CompanionEvent>> companionEventListeners = new ArrayList<EventListener<CompanionEvent>>();
 	private ArrayList<EventListener<ApplicationEvent>> applicationEventListeners = new ArrayList<EventListener<ApplicationEvent>>();
 	private ArrayList<EventListener<NavigationEvent>> navigationEventListeners = new ArrayList<EventListener<NavigationEvent>>();
 
 	public static void pushEvent(Event event) {
 		if (!outgoingEvents.contains(event))
 			outgoingEvents.offer(event);
 	}
 
 	public void update(float timeGap) {
 		HashMap<Event, JSONObject> result = updateOutEvents(timeGap);
 		JSONObject eventContent = null;
 		Event evt = null;
 		for (Map.Entry<Event, JSONObject> entry : result.entrySet()) {
 			eventContent = entry.getValue();
 			evt = entry.getKey();
 			if (eventContent == null)
 				continue;
 			try {
 				if (eventContent == null)
 					continue;
 
 				switch (evt.getState()) {
 				case REGISTER_DEVICE:
 
 					break;
 				case CREATE_DEVICE_TABLE:
 
 					break;
 				case NEW_EVENT:
 
 					break;
 				case NEW_UPDATE_EVENT:
 
 					break;
 				case NEW_DELETE_EVENT:
 
 					break;
 				case DELETE_EVENT:
 
 					break;
 				case DELETE_ALL_EVENTS:
 
 					break;
 				case GET_ALL_EVENTS:
 					JSONArray events = eventContent.getJSONArray("events");
 					for (int j = 0; j < events.length(); j++) {
 						JSONObject event = (JSONObject) events.get(j);
 						int id = event.getInt(JsonRequester.TAG_ID);
 						String deviceid = event
 								.getString(JsonRequester.TAG_DEVICEID);
 						String time = event.getString(JsonRequester.TAG_TIME);
 						String type = event.getString(JsonRequester.TAG_TYPE);
 						String[] split = type.split("[-]");
 						
 						Event incomingEvent = null;
 						if (split[0].equals(GPSEvent.class.getSimpleName())) {
 							incomingEvent = new GPSEvent(deviceid, event);
 						} else if (split[0].equals(RFIDEvent.class.getSimpleName())) {
 							incomingEvent = new RFIDEvent(deviceid, event);
 						} else if (split[0].equals(CompanionEvent.class.getSimpleName())) {
 							incomingEvent = new CompanionEvent(deviceid, event);
 						} else if (split[0].equals(MotionEvent.class.getSimpleName())) {
 							incomingEvent = new MotionEvent(deviceid, event);
 						} else if (split[0].equals(ApplicationEvent.class.getSimpleName())) {
 							incomingEvent = new ApplicationEvent(deviceid, event);
 						} else if (split[0].equals(NavigationEvent.class.getSimpleName())) {
 							incomingEvent = new NavigationEvent(deviceid, event);
 						}
 						incomingEvents.offer(incomingEvent);
 //						System.out.println(id + " " + eventid + " " + deviceid
 //								+ " " + receivcerid + " " + time);
 					}
 					break;
 				case UPDATE_EVENT:
 
 					break;
 				case GET_EVENT:
 
 					break;
 				default:
 					break;
 				}
 
 				System.out.println("Response to event#"
 						+ entry.getKey().getEventId() + ": "
 						+ eventContent.toString());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		updateInEvents(timeGap);
 	}
 
 	public void updateInEvents(float timeGap) {
 		Event temp;
 		while (!incomingEvents.isEmpty()) {
 			temp = incomingEvents.poll();
 			raiseCheckedEvent(temp);
 		}
 	}
 
 	public HashMap<Event, JSONObject> updateOutEvents(float timeGap) {
 		Event temp = null;
 		JSONObject obj = null;
 		HashMap<Event, JSONObject> events = new HashMap<Event, JSONObject>(
 				outgoingEvents.size());
 		while (!outgoingEvents.isEmpty()) {
 			temp = outgoingEvents.poll();
 
 			switch (temp.getState()) {
 			case NEW_EVENT:
 				obj = JsonRequester.newEvent(temp);
 				break;
 			case NEW_UPDATE_EVENT:
 				obj = JsonRequester.newEvent(temp);
 				temp.setState(EventState.UPDATE_EVENT);
 				break;
 			case NEW_DELETE_EVENT:
 				obj = JsonRequester.newEvent(temp);
 				temp.setState(EventState.DELETE_EVENT);
 				break;
 			case DELETE_EVENT:
 
 				obj = JsonRequester.deleteEvent(temp.getDeviceId(),
 						temp.getEventId());
 				break;
 			case REGISTER_DEVICE:
 				if (temp instanceof ApplicationEvent) {
					obj = JsonRequester.registerDevice(temp.getDeviceId());
 				}
 				break;
 			case CREATE_DEVICE_TABLE:
 				if (temp instanceof ApplicationEvent) {
 					obj = JsonRequester.createDeviceTable(temp.getDeviceId());
 				}
 				break;
 			case DELETE_ALL_EVENTS:
 				if (temp instanceof ApplicationEvent) {
 					obj = JsonRequester.deleteEvents(temp.getDeviceId());
 				}
 				break;
 			case GET_ALL_EVENTS:
 				if (temp instanceof ApplicationEvent) {
 					obj = JsonRequester.getAllEvents(temp.getDeviceId(),
 							temp.getReceiverId());
 				}
 				break;
 			case UPDATE_EVENT:
 				obj = JsonRequester.updateEvent(temp);
 				break;
 			case GET_EVENT:
 				obj = JsonRequester.getEvent(temp.getDeviceId(),
 						temp.getReceiverId(), temp.getEventId());
 				break;
 			default:
 				break;
 			}
 
 			events.put(temp, obj);
 		}
 		return events;
 	}
 
 	public void addRFIDListener(EventListener<RFIDEvent> listener) {
 		rfidEventListeners.add(listener);
 	}
 
 	public void addGPSListener(EventListener<GPSEvent> listener) {
 		gpsEventListeners.add(listener);
 	}
 
 	public void addMotionListener(EventListener<MotionEvent> listener) {
 		motionEventListeners.add(listener);
 	}
 
 	public void addCompanionListener(EventListener<CompanionEvent> listener) {
 		companionEventListeners.add(listener);
 	}
 
 	public void addApplicationListener(EventListener<ApplicationEvent> listener) {
 		applicationEventListeners.add(listener);
 	}
 
 	public void addNavigationListener(EventListener<NavigationEvent> listener) {
 		navigationEventListeners.add(listener);
 	}
 
 	public void removeRFIDListener(EventListener<RFIDEvent> listener) {
 		rfidEventListeners.remove(listener);
 	}
 
 	public void removeGPSListener(EventListener<GPSEvent> listener) {
 		gpsEventListeners.remove(listener);
 	}
 
 	public void removeMotionListener(EventListener<MotionEvent> listener) {
 		motionEventListeners.remove(listener);
 	}
 
 	public void removeCompanionListener(EventListener<CompanionEvent> listener) {
 		companionEventListeners.remove(listener);
 	}
 
 	public void removeApplicationListener(
 			EventListener<ApplicationEvent> listener) {
 		applicationEventListeners.remove(listener);
 	}
 
 	public void removeNavigationListener(EventListener<NavigationEvent> listener) {
 		navigationEventListeners.remove(listener);
 	}
 
 	private void raiseCheckedEvent(Event event) {
 		if (event instanceof RFIDEvent) {
 			raiseRFIDEvent((RFIDEvent) event);
 		} else if (event instanceof GPSEvent) {
 			raiseGPSEvent((GPSEvent) event);
 		} else if (event instanceof MotionEvent) {
 			raiseMotionEvent((MotionEvent) event);
 		} else if (event instanceof CompanionEvent) {
 			raiseCompanionEvent((CompanionEvent) event);
 		} else if (event instanceof ApplicationEvent) {
 			raiseApplicationEvent((ApplicationEvent) event);
 		} else if (event instanceof NavigationEvent) {
 			raiseNavigationEvent((NavigationEvent) event);
 		} else {
 			System.err.println("The event-type is not supported!");
 		}
 	}
 
 	private void raiseRFIDEvent(RFIDEvent event) {
 		for (EventListener<RFIDEvent> lst : rfidEventListeners) {
 			lst.fired(event);
 		}
 	}
 
 	private void raiseGPSEvent(GPSEvent event) {
 		for (EventListener<GPSEvent> lst : gpsEventListeners) {
 			lst.fired(event);
 		}
 	}
 
 	private void raiseMotionEvent(MotionEvent event) {
 		for (EventListener<MotionEvent> lst : motionEventListeners) {
 			lst.fired(event);
 		}
 	}
 
 	private void raiseCompanionEvent(CompanionEvent event) {
 		for (EventListener<CompanionEvent> lst : companionEventListeners) {
 			lst.fired(event);
 		}
 	}
 
 	private void raiseApplicationEvent(ApplicationEvent event) {
 		for (EventListener<ApplicationEvent> lst : applicationEventListeners) {
 			lst.fired(event);
 		}
 	}
 
 	private void raiseNavigationEvent(NavigationEvent event) {
 		for (EventListener<NavigationEvent> lst : navigationEventListeners) {
 			lst.fired(event);
 		}
 	}
 }
