 /**
  * 
  */
 package calabash.java;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import static calabash.java.Utils.*;
 
 /**
  * Represents an UI element.
  * 
  */
 public final class UIElement {
 
 	private final JSONObject data;
 	private final String query;
 	private final Http http;
 
 	public UIElement(JSONObject data, String query) {
 		this.data = data;
 		this.query = query;
 		this.http = new Http(Config.endPoint());
 	}
 
 	/**
 	 * Get element's class
 	 * 
 	 * @return
 	 */
 	public String getElementClass() {
 		return getStringFromJSON(data, "class");
 	}
 
 	/**
 	 * Gets the element id
 	 * 
 	 * @return
 	 */
 	public String getId() {
 		return getStringFromJSON(data, "id");
 	}
 
 	/**
 	 * Gets the label
 	 * 
 	 * @return
 	 */
 	public String getLabel() {
 		return getStringFromJSON(data, "label");
 	}
 
 	/**
 	 * Get description about this element
 	 * 
 	 * @return
 	 */
 	public String getDescription() {
 		return getStringFromJSON(data, "description");
 	}
 
 	/**
 	 * Gets the rectangle
 	 * 
 	 * @return
 	 */
 	public Rect getRect() {
 		JSONObject rect;
 		try {
 			rect = data.getJSONObject("rect");
 		} catch (JSONException e) {
 			return null;
 		}
 
 		return new Rect(getIntFromJSON(rect, "x"), getIntFromJSON(rect, "y"),
 				getIntFromJSON(rect, "width"), getIntFromJSON(rect, "height"),
 				getIntFromJSON(rect, "center_x"), getIntFromJSON(rect,
 						"center_y"));
 	}
 
 	/**
 	 * Get the rectangle representing frame
 	 * 
 	 * @return
 	 */
 	public Rect getFrame() {
 		JSONObject rect;
 		try {
 			rect = data.getJSONObject("frame");
 		} catch (JSONException e) {
 			return null;
 		}
 
 		return new Rect(getIntFromJSON(rect, "x"), getIntFromJSON(rect, "y"),
 				getIntFromJSON(rect, "width"), getIntFromJSON(rect, "height"),
 				null, null);
 	}
 
 	/**
 	 * Touches this element
 	 * 
 	 * @throws CalabashException
 	 */
 	public void touch() throws CalabashException {
 		Utils.playback("touch", query);
 	}
 
 	/**
 	 * Flashes this UIElement
 	 * 
 	 * @throws CalabashException
 	 */
 	public void flash() throws CalabashException {
 		JSONObject operation = new JSONObject();
 		operation.put("method_name", "flash");
 		operation.put("arguments", new JSONArray());
 		JSONObject postData = new JSONObject();
 		postData.put("query", query);
 		postData.put("operation", operation);
 		http.post("map", postData.toString());
 	}
 
 	/**
 	 * Gets the text from this UI element if it supports text
 	 * 
 	 * @return
 	 * @throws CalabashException
 	 */
 	public String getText() throws CalabashException {
 		JSONArray result = Utils.query(query, "text");
 		if (result.length() > 0) {
 			return result.getString(0);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets the property from this UIElement. This method can be used to fetch
 	 * any properties which are present in the UIElement
 	 * 
 	 * @param properties
 	 *            list of required properties
 	 * @return
 	 * @throws CalabashException
 	 */
 	public JSONArray getProperyValuesAsJSON(String... properties)
 			throws CalabashException {
 		return Utils.query(query, properties);
 	}
 
 	/**
 	 * Scrolls the element to the direction
 	 * 
 	 * @param direction
 	 *            Direction to scroll
 	 * @throws CalabashException
 	 */
 	public void scroll(ScrollDirection direction) throws CalabashException {
		Utils.query(query, direction.getDirection());
 	}
 
 	@Override
 	public String toString() {
 		return String.format(
 				"class: %s, label: %s, description: %s, rect: %s, frame: %s",
 				getElementClass(), getLabel(), getDescription(), getRect(),
 				getFrame());
 	}
 
 }
