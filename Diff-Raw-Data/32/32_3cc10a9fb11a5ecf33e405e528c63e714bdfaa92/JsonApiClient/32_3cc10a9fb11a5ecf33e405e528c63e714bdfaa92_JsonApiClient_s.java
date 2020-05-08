 package fr.utc.assos.payutc.api;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class JsonApiClient {
 	public static final String LOG_TAG = "JsonApiClient";
 	public String api_url;
 	HashMap<String, String> cookies = new HashMap<String, String>();
 	
 	public class Arg {
 		public String key, value;
 		Arg(String _key, Object _value) {
 			key = _key; value = String.valueOf(_value);
 		}
 	}
 	
 	public JsonApiClient(String url) {
 		api_url = url;
 	}
 	
 	protected String arglist2string(Arg[] args) throws UnsupportedEncodingException {
 		StringBuilder builder = new StringBuilder();
 		for (Arg arg : args) {
 			builder.append(URLEncoder.encode(arg.key, "UTF-8")+"="+URLEncoder.encode(arg.value, "UTF-8")+"&");
 		}
 		return builder.toString();
 	}
 	
 	protected String post(String uri, Arg[] args) 
 			throws IOException {
 		String data = arglist2string(args);
 		Log.d(LOG_TAG, "post "+uri+", data : "+data);
 		URL url = new URL(uri);
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setDoOutput(true);
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
         conn.setRequestProperty("Cookie", getCookiesHeader());
         conn.setUseCaches(false);
         conn.setDoInput(true);
         conn.setDoOutput(true);
         
         DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
         wr.writeBytes(data);
         wr.flush();
         
 
 		StringBuilder builder = new StringBuilder();
 		InputStreamReader is;
 		if (conn.getResponseCode() == 200) {
 			is = new InputStreamReader(conn.getInputStream(), "UTF-8");
 		}
 		else {
 			is = new InputStreamReader(conn.getErrorStream(), "UTF-8");
 		}
         BufferedReader in = new BufferedReader(is);
 	    String inputLine;
 	    while ((inputLine = in.readLine()) != null) {
 	    	builder.append(inputLine);
 	    }
 		in.close();
	    update_cookies(conn.getHeaderField("Set-Cookie"));
 		
         return builder.toString();
 	}
 
 	protected Object call(String method) 
 			throws IOException, JSONException, ApiException {
 		Arg[] args = {};
 		return call(method, args);
 	}
 	protected Object call(String method, Arg[] args)
 			throws IOException, JSONException, ApiException {
 		String post_result = post(api_url+"/"+URLEncoder.encode(method, "UTF-8"), args);
 		//Log.d(LOG_TAG, "post_result : "+post_result);
 		
 		JSONObject result = null;
 		try {
 			result = new JSONObject(post_result);
 		}
 		catch (JSONException e) {
 			try {
 				return Integer.valueOf(post_result);
 			}
 			catch (NumberFormatException e2) {
 				try {
 					return Float.valueOf(post_result);
 				}
 				catch (NumberFormatException e3) {
 					if (post_result.length() > 1 && post_result.startsWith("\"") && post_result.endsWith("\"")) {
 						return post_result.substring(1, post_result.length()-1)
 												.replaceAll("\\\\([^\\\\])", "$1");
 					}
 					else if (post_result == "true") {
 						return true;
 					}
 					else if (post_result == "false") {
 						return false;
 					}
 					else {
 						throw new ApiException("LocalParseError", "42", e.getMessage()+". Impossible de parser : "+post_result);
 					}
 				}
 			}
 		}
 
 		if (result.opt("error") != null) {
 			String err_code = "42";
 			String err_msg = null;
 			String err_type = null;
 			try {
 				JSONObject error = result.getJSONObject("error");
 				err_code = error.getString("code");
 				err_msg = error.getString("message");
 				err_type = error.getString("type");
 			} catch (Exception e) {
 				Log.e(LOG_TAG, "parse error failed", e);
 			}
 			throw new ApiException(err_type, err_code, err_msg);
 		}
 		
 		return result;
 	}
 	
 
 	synchronized String getCookiesHeader() {
 		StringBuilder sb = new StringBuilder();
         for (String cookie : cookies.keySet()) {
         	sb.append(cookie + "=" + cookies.get(cookie) + "; ");
         }
         return sb.toString();
 	}
 	
	synchronized void update_cookies(String cookiesHeader) {
 		if (cookiesHeader!=null) {
 		    Log.d(LOG_TAG, "cookies : "+cookiesHeader);
	    	String[] cookieSplit = cookiesHeader.replace("; ", ";").split(";");
	    	for (String cookieString : cookieSplit) {
	    		try {
		    		String cookieKey = cookieString.substring(0, cookieString.indexOf("="));
		    		String cookieValue = cookieString.substring(cookieString.indexOf("=")+1);
		    		cookies.put(cookieKey, cookieValue);
		    		Log.d(LOG_TAG, cookieKey + " = " + cookieValue);
	    		}
	    		catch (Exception ex) {
	    			Log.w(LOG_TAG, "error parsing cookie : '"+cookieString+"'", ex);
	    		}
	    	}
 		}
 	}
 }
