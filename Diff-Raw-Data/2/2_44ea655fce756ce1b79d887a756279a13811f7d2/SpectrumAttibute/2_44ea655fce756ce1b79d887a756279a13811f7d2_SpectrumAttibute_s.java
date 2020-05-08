 package ch.almana.spectrum.rest.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class SpectrumAttibute {
 
 	private static final String NO_VALUE = "";
 	
 	public static final Map<String, String> attributeNames = new HashMap<String, String>();
 	
 	public static final String MODEL_TYPE_NAME = "0x10000";
 	public static final String CONDITION = "0x10009";
 	public static final String MODEL_NAME = "0x1006e";
 	public static final String MODEL_CLASS = "0x11ee8";
 	public static final String ACKNOWLEDGED = "0x11f4d";
 	public static final String CREATION_DATE = "0x11f4e";
 	public static final String ALARM_STATUS = "0x11f4f";
 	public static final String SEVERITY = "0x11f56";
 	public static final String OCCURENCES = "0x11fc5";
 	public static final String NETWORK_ADDRESS = "0x12d7f";
 	public static final String ALARM_TITLE = "0x12b4c";
 	public static final String ALARM_ID = "0x11f9c";
 	public static final String MODEL_HANDLE = "0x129fa";
 
 	
 	//	public static final String  = "0x";
 	//	public static final String  = "0x";
 	//	public static final String  = "0x";
 	//	public static final String  = "0x";
 
	public static String get(String key) {
 		if (key == null) {
 			return NO_VALUE;
 		}
 		for (String id : attributeNames.keySet()) {
 			String name = attributeNames.get(id);
 			if (key.equals(name)) {
 				return id;
 			}
 		}
 		return NO_VALUE;
 	}
 
 	@Override
 	public String toString() {
 		String ret = "[";
 		for (String id : attributeNames.keySet()) {
 			String name = attributeNames.get(id);
 			ret = ret + name + " -> " + id + " ";
 		}
 		ret += "]";
 		return ret;
 	}
 
 	static {
 		attributeNames.put(MODEL_TYPE_NAME, "MODELTYPE NAME");
 		attributeNames.put(CONDITION, "CONDITION");
 		attributeNames.put(MODEL_NAME, "MODEL NAME");
 		attributeNames.put(MODEL_CLASS, "MODEL CLASS");
 		attributeNames.put(ACKNOWLEDGED, "ACKNOWLEDGED");
 		attributeNames.put(CREATION_DATE, "CREATION DATE");
 		attributeNames.put(ALARM_STATUS, "ALARM STATUS");
 		attributeNames.put(SEVERITY, "SEVERITY");
 		attributeNames.put(OCCURENCES, "OCCURENCES");
 		attributeNames.put(NETWORK_ADDRESS, "NETWORK ADDRESS");
 		attributeNames.put(ALARM_TITLE, "ALARM TITLE");
 	}
 }
