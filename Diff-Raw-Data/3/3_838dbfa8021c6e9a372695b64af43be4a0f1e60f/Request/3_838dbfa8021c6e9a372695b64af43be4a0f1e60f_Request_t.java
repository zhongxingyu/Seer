 package hemera.ext.batch.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.jayway.jsonpath.JsonPath;
 
 import hemera.core.structure.enumn.EHttpMethod;
 
 /**
  * <code>Request</code> defines the immutable data
  * structure that presents a request to be sent to
  * the API as one of the batch requests.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public class Request {
 	/**
 	 * The <code>EHttpMethod</code> method.
 	 */
 	final EHttpMethod method;
 	/**
 	 * The <code>String</code> URI.
 	 */
 	final String uri;
 	/**
 	 * The optional <code>Map</code> of arguments.
 	 */
 	final Map<String, String> args;
 	/**
 	 * The <code>List</code> of children requests. It
 	 * may be empty if there are no children requests.
 	 */
 	final List<ChildRequest> children;
 	
 	/**
 	 * Constructor of <code>Request</code>.
 	 * @param data The <code>JSONObject</code> data of
 	 * the request to parse.
 	 * @throws JSONException If parsing failed.
 	 */
 	public Request(final JSONObject data) throws JSONException {
 		this.children = new ArrayList<ChildRequest>();
 		this.method = EHttpMethod.parse(data.getString("method"));
 		this.uri = data.getString("uri");
 		if (data.has("args")) {
 			this.args = new HashMap<String, String>();
 			final JSONArray argsArray = data.getJSONArray("args");
 			final int count = argsArray.length();
 			for (int i = 0; i < count; i++) {
 				final JSONObject arg = argsArray.getJSONObject(i);
 				final String key = arg.getString("key");
 				// Value may be a JSON object indicating a dependent request.
 				final JSONObject value = arg.optJSONObject("value");
 				if (value == null) {
 					final String stringValue = arg.getString("value");
 					this.args.put(key, stringValue);
 				}
 				// Add as child request, whose response value will be later
 				// put into the arguments map when the child request is done.
 				else {
 					final ChildRequest child = new ChildRequest(value, key);
 					this.children.add(child);
 				}
 			}
 		} else {
 			this.args = null;
 		}
 	}
 	
 	/**
 	 * Complete all the children dependency requests.
 	 * @param sender The <code>RequestSender</code>.
 	 * @return <code>null</code> if all dependencies
 	 * completed successfully. Otherwise return the
 	 * <code>JSONObject</code> of the first failed
 	 * child request.
 	 * @throws Exception If any processing failed.
 	 */
 	public JSONObject completeDependencies(final RequestSender sender) throws Exception {
 		final int count = this.children.size();
 		for (int i = 0; i < count; i++) {
 			final ChildRequest child = this.children.get(i);
 			final JSONObject failedChildResponse = child.completeDependencies(sender);
 			// If one of the dependencies failed, just return failed response.
 			if (failedChildResponse != null) return failedChildResponse;
 			// Send child request and wait for response.
 			final JSONObject response = sender.sendRequest(child);
 			// Set argument using child response.
 			this.args.put(child.parentArgKey, (String)JsonPath.read(response.toString(), child.path));
 		}
 		return null;
 	}
 	
 	/**
 	 * Check if this request requires write permission.
 	 * @return <code>true</code> if the request needs
 	 * write permission. <code>false</code> otherwise.
 	 */
 	public boolean requiresWritePermission() {
		if (this.method == EHttpMethod.Options || this.method == EHttpMethod.Get || this.method == EHttpMethod.Head
				|| this.method == EHttpMethod.Trace) return false;
 		else return true;
 	}
 	
 	/**
 	 * <code>ChildRequest</code> defines the data structure
 	 * of a request whose response value is used by its
 	 * parent request as an argument.
 	 *
 	 * @author Yi Wang (Neakor)
 	 * @version 1.0.0
 	 */
 	private class ChildRequest extends Request {
 		/**
 		 * The <code>String</code> parent argument key.
 		 */
 		private final String parentArgKey;
 		/**
 		 * The <code>String</code> JSON path.
 		 */
 		private final String path;
 		
 		/**
 		 * Constructor of <code>ChildRequest</code>.
 		 * @param data The <code>JSONObject</code> data of
 		 * the child request to parse.
 		 * @param parentArgKey The <code>String</code>
 		 * parent argument key.
 		 * @throws JSONException If data parsing failed.
 		 */
 		private ChildRequest(final JSONObject data, final String parentArgKey) throws JSONException {
 			super(data);
 			this.parentArgKey = parentArgKey;
 			this.path = data.getString("dependency_path");
 		}
 	}
 }
