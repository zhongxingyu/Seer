 package javapns.data;
 
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class is the JSON representation of the notification
  * See page 19 of Apple Push Notification Service Programming Guide
  * @author Maxime Peron
  *
  */
 public class PayLoad {
 
 	/* The root Payload */
 	private JSONObject payload;
 	/* The application Dictionnary */
 	private JSONObject apsDictionary;
 
 	/**
 	 * Constructor, instantiate the two JSONObjects
 	 */
 	public PayLoad(){
 		super();
 		this.payload = new JSONObject();
 		this.apsDictionary = new JSONObject();
 		try {
 			this.payload.put("aps", this.apsDictionary);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public PayLoad(String alert, int badge, String sound)  throws JSONException {
 		this();
 		addAlert(alert);
 		addBadge(badge);
 		if(sound != null){
 			addSound(sound);
 		}
 	}
 	
 	/**
 	 * Add a badge
 	 * @param badge
 	 * @throws JSONException
 	 */
 	public void addBadge (int badge) throws JSONException{
 		this.apsDictionary.putOpt("badge", badge);
 	}
 
 	/**
 	 * Add a sound
 	 * @param sound
 	 * @throws JSONException
 	 */
 	public void addSound (String sound) throws JSONException{
 		this.apsDictionary.putOpt("sound", sound);
 	}
 
 	/**
 	 * Add an alert message
 	 * @param alert
 	 * @throws JSONException
 	 */
 	public void addAlert (String alert) throws JSONException{
 		this.apsDictionary.put("alert", alert);
 	}
 
 	/**
 	 * Add a custom alert message
 	 * @param alert
 	 * @throws JSONException
 	 */
 	public void addCustomAlert (PayLoadCustomAlert alert) throws JSONException{
 		this.apsDictionary.put("alert", alert);
 	}
 
 	/**
 	 * Add a custom dictionnary with a string value
 	 * @param name
 	 * @param value
 	 * @throws JSONException
 	 */
 	public void addCustomDictionary (String name, String value) throws JSONException{
 		this.payload.put(name,value);
 	}
 
 	/**
 	 * Add a custom dictionnary with a int value
 	 * @param name
 	 * @param value
 	 * @throws JSONException
 	 */
 	public void addCustomDictionary (String name, int value) throws JSONException{
 		this.payload.put(name, value);
 	}
 
 	/**
 	 * Add a custom dictionnary with multiple values
 	 * @param name
 	 * @param values
 	 * @throws JSONException
 	 */
 	@SuppressWarnings("unchecked")
 	public void addCustomDictionary (String name, List values) throws JSONException{
 		this.payload.put(name, values);
 	}
 
 	/**
 	 * Get the string representation
 	 */
 	public String toString(){
 		return this.payload.toString();
 	}
 
 	/**
 	 * Get this payload as a byte array
 	 * @return
 	 */
	public byte[] getPayloadAsBytes(){
 		try {
			return toString().getBytes("UTF-8");
 		} catch (Exception ex) {
			return toString().getBytes()	;
 		}
 	}
 
 }
