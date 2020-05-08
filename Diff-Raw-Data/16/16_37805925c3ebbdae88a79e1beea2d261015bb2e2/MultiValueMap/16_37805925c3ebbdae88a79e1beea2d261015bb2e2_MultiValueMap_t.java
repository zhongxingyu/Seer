 package fedora.server;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class MultiValueMap {
 	
 	private boolean locked = false;
 
 	private Map attributes = new HashMap();
 
     /**
      * Creates and initializes the <code>WritableContext</code>.
      * <p></p>
     * A pre-loaded Map of name-value pairs
      *        comprising the context.
      */
     public MultiValueMap() {
     }
 
     public String setReturn(String name, Object value) throws Exception {
     	set(name, value);
     	return name;
     }
     
     public void set(String name, Object value) throws Exception {
     	if (name == null) {
     		String msg = here + ": set() has null name, value=" + value;
     		log(msg);
     		throw new Exception(msg);
     	}
     	if (locked) {
     		String msg = here + ": set() has object locked";
     		log(msg);
     		throw new Exception(msg);    		
     	}
     	if (value instanceof String) {
     	} else if (value instanceof String[]) {
     		if (((String[])value).length == 1) {
         		value = ((String[])value)[0];
     		}
     	} else if (value == null) {
         		value = "";
     	} else {
     		String msg = here + ": set() has unhandled type";
     		log(msg);
     		throw new Exception(msg);    		
     	}
     	attributes.put(name, value);    	
     }
     
     public void lock() {
     	locked = true;
     }
     
     public Iterator names() {
         return attributes.keySet().iterator();
     }
 
     public int length(String name) {
     	if (attributes.get(name) instanceof String) {
     		return 1;
     	} else if (attributes.get(name) instanceof String[]) {
     		return ((String[])attributes.get(name)).length;
     	} else {
     		return 0;
     	}
     }
     
     public String getString(String name) {
         return (String) attributes.get(name);
     }
 
     public String[] getStringArray(String name) {
         return (String[]) attributes.get(name);
     }
     
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         Iterator it = attributes.keySet().iterator();
         while (it.hasNext()) {
         	String key = (String) it.next();
         	String value = (String) attributes.get(key);
         	buffer.append(key + "=" + value + "\n");
         }
         return buffer.toString();
     }
     
     protected static final String here;
 	static {
     	here = "MultiValueMap";
     }
     
 	public static boolean log = false; 
 	
 	protected static final void log(String msg) {
 		if (! log) return;
 		System.err.println(here + msg);
 	}
 
 }
