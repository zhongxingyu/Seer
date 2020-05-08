 package com.ubicomp.iseesomethingyoudont;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONArray;
 
 import android.speech.tts.TextToSpeech;
 
 import com.h5n1.eventsys.EventSystem;
 import com.h5n1.eventsys.JsonRequester;
 import com.h5n1.eventsys.events.ApplicationEvent;
 import com.h5n1.eventsys.events.ApplicationEvent.ApplicationEventType;
 import com.h5n1.eventsys.events.CompanionEvent;
 import com.h5n1.eventsys.events.Event;
 import com.h5n1.eventsys.events.EventState;
 import com.h5n1.eventsys.events.GPSEvent;
 import com.h5n1.eventsys.events.MotionEvent;
 import com.h5n1.eventsys.events.NavigationEvent;
 import com.h5n1.eventsys.events.NavigationEvent.NavigationEventType;
 import com.h5n1.eventsys.events.RFIDEvent;
 import com.h5n1.eventsys.listener.ApplicationEventListener;
 import com.h5n1.eventsys.listener.CompanionEventListener;
 import com.h5n1.eventsys.listener.GPSEventListener;
 import com.h5n1.eventsys.listener.MotionEventListener;
 import com.h5n1.eventsys.listener.NavigationEventListener;
 import com.h5n1.eventsys.listener.RFIDEventListener;
 
 public class EventHandler {
 
 	private GPSEventListener gpsEventListener;
 	private RFIDEventListener rfidEventListener;
 	private MotionEventListener motionEventListener;
 	private CompanionEventListener companionEventListener;
 	private ApplicationEventListener applicationEventListener;
 	private NavigationEventListener navigationEventListener;
 
 	private ArrayList<Integer> focusedEventIds = new ArrayList<Integer>();
 
 	private Timer timer = null;
 	private TimerTask eventsystemTask = null;
 
 	private ApplicationEvent getAllEvents;
 
 	public EventHandler(final EventSystem system, final EventToSpeechSynthesis eventToSpeechSynthesis) {
 		//register device 
 		ApplicationEvent registerDevice = new ApplicationEvent(
 				JsonRequester.getDeviceID(),
				ApplicationEventType.REGISTER_DEVICE);
		registerDevice.setState(EventState.REGISTER_DEVICE);
 		EventSystem.pushEvent(registerDevice);
 		//create device table
 		ApplicationEvent createTable = new ApplicationEvent(
 				JsonRequester.getDeviceID(),
 				ApplicationEventType.CREATE_DEVICE_TABLE);
 		createTable.setState(EventState.CREATE_DEVICE_TABLE);
 		EventSystem.pushEvent(createTable);
 		// delete all previous events
 		ApplicationEvent deleteAllEvents = new ApplicationEvent(
 				JsonRequester.getDeviceID(),
 				ApplicationEventType.DELETE_ALL_EVENTS);
 		deleteAllEvents.setState(EventState.DELETE_ALL_EVENTS);
 		EventSystem.pushEvent(deleteAllEvents);
 		// test event
 		double[] val = { 1,2,3 };
 		NavigationEvent nav = new NavigationEvent(JsonRequester.getDeviceID(), NavigationEventType.OBSTACLE_HUMAN, val);
 		nav.setReceiverId(JsonRequester.getDeviceID());
 		EventSystem.pushEvent(nav);
 		// get all events for this device
 		getAllEvents = new ApplicationEvent("0",
 				ApplicationEventType.GET_ALL_EVENTS);
 		getAllEvents.setState(EventState.GET_ALL_EVENTS);
 		getAllEvents.setReceiverId(JsonRequester.getDeviceID());
 
 		gpsEventListener = new GPSEventListener() {
 
 			@Override
 			public void fired(GPSEvent event) {
 
 			}
 		};
 		rfidEventListener = new RFIDEventListener() {
 
 			@Override
 			public void fired(RFIDEvent event) {
 
 			}
 		};
 		motionEventListener = new MotionEventListener() {
 
 			@Override
 			public void fired(MotionEvent event) {
 
 			}
 		};
 		companionEventListener = new CompanionEventListener() {
 
 			@Override
 			public void fired(CompanionEvent event) {
 
 			}
 		};
 
 		applicationEventListener = new ApplicationEventListener() {
 
 			@Override
 			public void fired(ApplicationEvent event) {
 
 			}
 		};
 
 		navigationEventListener = new NavigationEventListener() {
 			
 			@Override
 			public void fired(NavigationEvent event) {
 				event.setState(EventState.DELETE_EVENT);
 				eventToSpeechSynthesis.getTtsEngine().speak("Achtung, Hinderniss vorraus! " + event.getData()[0] + " meter. Typ " + event.getType().toString(), TextToSpeech.QUEUE_FLUSH, null);
 				EventSystem.pushEvent(event);
 			}
 		};
 
 		system.addCompanionListener(companionEventListener);
 		system.addGPSListener(gpsEventListener);
 		system.addMotionListener(motionEventListener);
 		system.addRFIDListener(rfidEventListener);
 		system.addApplicationListener(applicationEventListener);
 		system.addNavigationListener(navigationEventListener);
 
 		timer = new Timer("eventTimer", false);
 		final float timeGap = 1;
 		eventsystemTask = new TimerTask() {
 
 			@Override
 			public void run() {
 				EventSystem.pushEvent(getAllEvents);
 				system.update(timeGap);
 				// LinkedList<Event> in = system.updateInEvents(timeGap);
 				// HashMap<Event, JSONArray> out =
 				// system.updateOutEvents(timeGap);
 			}
 		};
 		timer.schedule(eventsystemTask, 2000, (long) (timeGap * 1000));
 
 	}
 
 	public ArrayList<Integer> getFocusedEventIds() {
 		return focusedEventIds;
 	}
 
 	public void addFocusedEvent(Integer id) {
 		focusedEventIds.add(id);
 	}
 
 }
