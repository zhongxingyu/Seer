 /**
  * 
  */
 package ltg.commons.ltg_handler;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import ltg.commons.MessageListener;
 import ltg.commons.SimpleXMPPClient;
 
 import org.jivesoftware.smack.packet.Message;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 /**
  * @author tebemis
  *
  */
 public class LTGEventHandler {
 
 	private SimpleXMPPClient sc = null;
 	private Map<String, LTGEventListener> listeners = new HashMap<String, LTGEventListener>();
 
 
 	public LTGEventHandler(String fullJid, String password) {
 		sc = new SimpleXMPPClient(fullJid, password);
 	}
 
 
 	public LTGEventHandler(String fullJid, String password, String chatRoom)  {
 		sc = new SimpleXMPPClient(fullJid, password, chatRoom);
 	}
 
 
 	public void registerHandler(String eventType, LTGEventListener listener) {
 		try {
 			Pattern.compile(eventType);
 		} catch (PatternSyntaxException e) {
 			System.out.println("Invalid event type. If you are writing a regular expression check your syntax. Terminating... ");
 			System.exit(-1);
 		}
 		listeners.put(eventType, listener);
 	}
 
 
 	public void runSynchronously() {
 		printRegisteredListeners();
 		// We are now connected and in the group chat room. If we don't do something
 		// the main thread will terminate. Let's go ahead and 
 		// wait for a message to arrive...
 		while (!Thread.currentThread().isInterrupted()) {
 			// ... and process it ...
 			processEvent(sc.nextMessage());
 		}
 		// ... and finally disconnect
 		sc.disconnect();
 	}
 
 
 	public void runAsynchronously() {
 		printRegisteredListeners();
 		sc.registerEventListener(new MessageListener() {
 			public void processMessage(Message m) {
 				processEvent(m);
 			}
 		});
 	}
 
 
 	public void close() {
 		sc.disconnect();
 	}
 
 	
 	/**
 	 * Generates a public event that is either broadcasted to the whole group chat
 	 * or addressd to a specific agent or client.
 	 * 
 	 * @param e the event
 	 */
 	public void generateEvent(LTGEvent e) {
 		sc.sendMessage(serializeEvent(e));
 	}
 	
 	
 	/**
 	 * Generates a public event that is broadcasted to the whole group chat.
 	 * 
 	 * @param event
 	 * @param payload
 	 */
 	public void generateEvent(String event, JsonNode payload) {
 		generateEvent(new LTGEvent(event, sc.getUsername(), null, payload));
 	}
 	
 	
 	/**
 	 * Generates a public event that is addressed to a particular agent or client.
 	 * 
 	 * @param event
 	 * @param destination
 	 * @param payload
 	 */
 	public void generateEvent(String event, String destination, JsonNode payload) {
 		generateEvent(new LTGEvent(event, sc.getUsername(), destination, payload));
 	}
 
 
 	/**
 	 * Generates a point-to-point event that is sent to a particular client
 	 * off the public group chat.
 	 * 
 	 * @param destination
 	 * @param e
 	 */
 	public void generatePrivateEvent(String destination, LTGEvent e) {
 		sc.sendMessage(destination, serializeEvent(e));
 	}
 
 	
 	/**
 	 * Generates a point-to-point event that is sent to a particular client
 	 * off the public group chat.
 	 * 
 	 * @param event
 	 * @param destination
 	 * @param payload
 	 */
 	public void generatePrivateEvent(String event, String destination, JsonNode payload) {
 		generatePrivateEvent(destination, new LTGEvent(event, sc.getUsername(), destination, payload));
 	}
 
 	
 	/**
 	 * Serializes a <code>LTGEvent</code> object into JSON.
 	 * 
 	 * @param e
 	 * @return
 	 */
 	public static String serializeEvent(LTGEvent e) {
 		ObjectNode json = new ObjectMapper().createObjectNode();
 		json.put("event", e.getType());
 		if (e.getOrigin()!=null)
 			json.put("origin", e.getOrigin());
 		if (e.getDestination()!=null)
 			json.put("destination", e.getDestination());
 		json.put("payload", e.getPayload());
 		return json.toString();
 	}
 
 
 	/**
 	 * De-serializes JSON into a <code>LTGEvent</code> object.
 	 * 
 	 * @param json
 	 * @return
 	 * @throws IOException
 	 * @throws NotAnLTGEventException
 	 */
 	public static LTGEvent deserializeEvent(String json) throws IOException, NotAnLTGEventException {
 		// Parse JSON
 		ObjectMapper jsonParser = new ObjectMapper();
 		JsonNode jn = jsonParser.readTree(json);
 		String event = jn.path("event").textValue();
 		String origin = jn.path("origin").textValue();
 		String destination = jn.path("destination").textValue();
 		JsonNode payload = jn.path("payload");
 		// Validate fields
 		if(event==null || event.isEmpty()) 
 			throw new NotAnLTGEventException();
 		if (payload==null)
 			throw new NotAnLTGEventException();
 		// Create and return event
 		return new LTGEvent(event, origin, destination, payload);
 	}
 	
 	
 	private void processEvent(Message m) {
 		// Parse JSON
 		LTGEvent event = null;
 		try {
 			event = deserializeEvent(m.getBody());
 		} catch (Exception e) {
 			// Not JSON or wrong format, ignore and return
 			return;
 		}
 		// Process event
 		List<LTGEventListener> els = new ArrayList<LTGEventListener>();
 		if (event!=null) {
 			for (String eventSelector : listeners.keySet())
 				if (event.getType().matches(eventSelector))
 					els.add(listeners.get(eventSelector));
 			for (LTGEventListener el : els)
 				el.processEvent(event);
 		}
 	}
 	
 	
 	private void printRegisteredListeners() {
 		String registeredListeners = " ";
 		for (String s: listeners.keySet())
 			registeredListeners = registeredListeners + s + ", ";
 		if (registeredListeners.length()>3) {
 		System.out.print("Listening for events of type [");
 		System.out.print(registeredListeners.substring(0, registeredListeners.length()-2)+" ]\n");
 		} else {
			System.out.print("Listening for events of type [ ]\n");
 		}
 	}
 
 }
