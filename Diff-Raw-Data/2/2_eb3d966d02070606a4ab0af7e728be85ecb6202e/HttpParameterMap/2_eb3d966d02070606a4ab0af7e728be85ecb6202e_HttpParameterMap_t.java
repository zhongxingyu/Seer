 package yucatan.communication;
 
 import java.util.HashMap;
 
 /**
  * First draft of a http parameter map.
  */
 public final class HttpParameterMap implements ReadOnlyMemberAccessor {
 
 	/**
 	 * The {@link HashMap}&lt;String, String&gt which contains the key value pairs.
 	 */
 	private HashMap<String, HttpParameterValue> parameters;
 
 	/**
 	 * Create empty HttpParameterMap.
 	 */
 	public HttpParameterMap() {
 		parameters = new HashMap<String, HttpParameterValue>();
 	}
 
 	/**
 	 * Create HttpParameterMap with inital values.
 	 * 
 	 * @param initalData initial parameters
 	 */
 	public HttpParameterMap(HashMap<String, HttpParameterValue> initalData) {
 		// create empty parameters Hashmap
 		parameters = new HashMap<String, HttpParameterValue>();
 
 		// null check
 		if (initalData == null) {
 			return;
 		}
 
 		// put dynamic data first
 		parameters.putAll(initalData);
 	}
 
 	/**
 	 * Associates the specified value with the specified key in this map.
 	 * 
 	 * @param key key with which the specified value is to be associated. (null is not allowed)
 	 * @param stringValue value to be associated with the specified key
 	 * @return the old value that had been associated with the specified key
 	 */
 	public HttpParameterValue put(String key, String stringValue) {
 		if (key == null) {
 			return null;
 		}
 		HttpParameterValue oldValue = parameters.get(key);
 		parameters.put(key, new HttpParameterValue(stringValue));
 		return oldValue;
 	}
 
 	/**
 	 * Associates the specified value with the specified key in this map.
 	 * 
 	 * @param key key with which the specified value is to be associated. (null is not allowed)
 	 * @param value value to be associated with the specified key
 	 * @return the old value that had been associated with the specified key
 	 */
 	public HttpParameterValue put(String key, HttpParameterValue value) {
 		if (key == null) {
 			return null;
 		}
 		HttpParameterValue oldValue = parameters.get(key);
 		parameters.put(key, value);
 		return oldValue;
 	}
 
 
 	/**
 	 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
 	 * 
 	 * @param key the key whose associated value is to be returned
 	 * @return the requested value or null
 	 */
 	@Override
	public Object get(String key) {
 		return parameters.get(key);
 	}
 }
