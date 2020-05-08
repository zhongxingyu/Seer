 package org.amplafi.flow.utils;
 
 import static org.amplafi.flow.utils.GeneralFlowRequest.APPLICATION_ZIP;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.amplafi.json.JSONArray;
 import org.amplafi.json.JSONObject;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.util.EntityUtils;
 
 public class FlowResponse {
 
 	private static Map<Integer, String> responseExplanations = new HashMap<Integer, String>();
 	private static final String EXPLANATION_200 = "A call finished successfully. A call result (an object, an array, or a string might be returned as response body";
 	private static final String EXPLANATION_400 = "An error (usually user related) happened on server. Usually means bad request parameters.";
 	private static final String EXPLANATION_401 = "Authorization problem. Usually means you're using invalid API key.";
 	private static final String EXPLANATION_404 = "Flow not found. Means that the request tried to access a non existent API entry point.";
 	private static final String EXPLANATION_500 = "Server has problems. Contact server developers.";
 	private static final String EXPLANATION_302 = "Redirect. Usually not handled by client, as redirects happen automatically.";
 
 	static {
 		responseExplanations.put(200, EXPLANATION_200);
 		responseExplanations.put(400, EXPLANATION_400);
 		responseExplanations.put(404, EXPLANATION_404);
 		responseExplanations.put(401, EXPLANATION_401);
 		responseExplanations.put(500, EXPLANATION_500);
 		responseExplanations.put(302, EXPLANATION_302);
 	}
 
 	private final String responseText;
 	private final int httpStatusCode;
 
 	public FlowResponse() {
 		responseText = null;
 		httpStatusCode = 0;
 	}
 
 	public FlowResponse(HttpResponse response) {
 		httpStatusCode = response.getStatusLine().getStatusCode();
 		try {
 			Header contentTypeHeader = response.getFirstHeader("Content-Type");
 			if (contentTypeHeader != null
 					&& contentTypeHeader.getValue() != null
 					&& contentTypeHeader.getValue().equals(APPLICATION_ZIP)) {
 				// calling classes should check for this.
 				responseText = APPLICATION_ZIP;
 			} else {
 				responseText = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
 			}
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public FlowResponse(HttpServletRequest serverCallbackRequest) {
 		httpStatusCode = 200;
 		Enumeration<String> parameterNames = serverCallbackRequest
 				.getParameterNames();
 		JSONObject response = new JSONObject();
 		while (parameterNames.hasMoreElements()) {
 			String next = parameterNames.nextElement();
 			String parameter = serverCallbackRequest.getParameter(next);
 			if (parameter.startsWith("{")) {
 				response.put(next, JSONObject.toJsonObject(parameter));
 			} else if (parameter.startsWith("[")) {
 				response.put(next, JSONArray.toJsonArray(parameter));
 			} else {
 				response.put(next, parameter);
 			}
 		}
 		responseText = response.toString();
 	}
 
 	public int getHttpStatusCode() {
 		return httpStatusCode;
 	}
 
 	public boolean hasError() {
 		return httpStatusCode != 200;
 	}
 
 	@Override
     public String toString() {
 		return hasError() ? handleError() : toJSONObject().toString(2);
 	}
 	
     private String handleError() {
         StringBuilder error = new StringBuilder();
         if (getErrorMessage().contains("Callback with lookupKey")) {
             error.append("Your current key is invalid. This will happen if the farreach.es server restarts. Ask Pat for a new key");
         } else {
             error.append("response string:");
             error.append(responseText + "\n");
             error.append("response error:");
             error.append(getErrorMessage() + "\n");
         }
         return error.toString();
     }
 
 	public JSONObject toJSONObject() {
 		if (hasError()) {
 			return null;
 		}
		return new JSONObject(responseText);
 	}
 
 	public JSONArray toJSONArray() {
 		if (hasError()) {
 			return null;
 		}
 		return new JSONArray(toString());
 	}
 
 	public String getErrorMessage() {
 		// Construct error message, connsisting of explanation of code (for Pat
 		// at 3am) and
 		// error message returned from server.
 		return "Http Status Code " + this.getHttpStatusCode() + "\n"
 				+ responseExplanations.get(this.getHttpStatusCode()) + "\n"
 				+ this.toString();
 	}
 
 	public String get(String key) {
 		JSONObject jsonObject = toJSONObject();
 		jsonObject = jsonObject.flatten();
 		return jsonObject.optString(key);
 	}
 }
