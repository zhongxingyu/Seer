 package dk.itu.frza.smartspb;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Parse a byte stream using the given protocol, buffer and handler
  * @author frza
  *
  */
 public class StreamProtocol {
 	static final Logger log = Logger.getLogger(StreamProtocol.class.getSimpleName());
 	
 	/**
 	 * When a state-method in the protocol return the END value,
 	 * the handler will be called with the current buffer and
 	 * its extras will be reset.
 	 */
 	public static final int END = -1;
 	
 	/**
	 * When a state-method encounter an error, it can throws an exception or 
	 * return the ERROR identifier. In any case, an exception is returned
	 * to the <code>start</code> caller.
	 */
	public static final int ERROR = -2;
	
	/**
 	 * The smart buffer used by this stream protocol
 	 */
 	Buffer buffer;
 	
 	/**
 	 * The object to be called when an iteration of the protocol ends
 	 */
 	Handler handler;
 	
 	/**
 	 * The current state
 	 */
 	int current;
 	
 	/**
 	 * The first state to be called. By convention, this is the
 	 * state-method identified by the lowest id
 	 */
 	int firstState;
 	
 	/**
 	 * Set it to true to stop the parse cycle
 	 */
 	boolean terminate = false;
 	
 	/**
 	 * The protocol-object
 	 */
 	Object protocol;
 	
 	/**
 	 * Maps of the protocol states
 	 */
 	Map<Integer,Method> states = new HashMap<Integer,Method>();
 	
 	/**
 	 * Set the smart buffer
 	 * @param b
 	 */
 	public void setBuffer(Buffer b) {
 		this.buffer = b;
 	}
 	/**
 	 * Set the handler
 	 * @param h
 	 */
 	public void setHandler(Handler h) {
 		this.handler = h;
 	}
 	/**
 	 * Set the protocol object
 	 * @param p
 	 */
 	public void setProtocol(Object p) {
 		this.protocol = p;
 	}
 	
 	/**
 	 * Analyze the protocol object to get information about the different states
 	 */
 	public void setup() {
 		Class<?> c = protocol.getClass();
 		ProtocolState state;
 		int min;
 		min = c.getMethods().length+1;
 		for(Method m : c.getMethods()) {
 			state = null;
 			state = m.getAnnotation(ProtocolState.class);
 			if(state != null) {
 				int s = state.value();
 				if(!states.containsKey(s)) {
 					checkMethod(m);
 					log.info("State "+s+" set to method " + m.getName());
 					states.put(s, m);
 					if(min > s){min=s;}
 				} else {
 					throw new RuntimeException("State " + s + " already defined! (" + states.get(s).getName() + "/" + m.getName());
 				}
 			}
 		}
 		current = min;
 		firstState = min;
 	}
 	private void checkMethod(Method m) {
 		String mn = m.getName();
 		if(m.getReturnType() != Integer.TYPE) {
 			throw new RuntimeException(mn + " must return int!");
 		}
 		Class<?>[] ptypes = m.getParameterTypes();
 		if(ptypes.length != 1) {
 			throw new RuntimeException(mn + " must accept a dk.itu.frza.smartspb.Buffer parameter!");
 		}
 		if(ptypes[0] != Buffer.class) {
 			throw new RuntimeException(mn + " must accept a dk.itu.frza.smartspb.Buffer as first parameter!");
 		}
 	}
 	
 	/**
 	 * The name of the method associated with the current state
 	 * @return
 	 */
 	public String currentState() {
 		return states.get(current).getName();
 	}
 	
 	/**
 	 * Cause the protocol to stop the parse cycle
 	 */
 	public void terminate() {
 		terminate = true;
 	}
 
 	/**
 	 * Start the parse cycle.
 	 * @throws Exception
 	 */
 	public void start() throws Exception {
 		Method m;
 		do {
 			m = states.get(current);
 			log.info("state: "+currentState());
 			current = (Integer)m.invoke(protocol, buffer);
 			if(current == END) {
 				handler.onProtocolEnd(buffer);
 				buffer.resetExtras();
 				current = firstState;
			} else if(current == ERROR) {
				throw new Exception("Protocol error");
 			}
 		} while(!terminate);
 	}
 }
