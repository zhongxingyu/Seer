 //
 // Copyright 2011 MERS Technologies.
 //
 
 /* Java client library for the SUBUNO API.
 
 This client library is designed to support the SUBUNO API. Read more
 about the SUBUNO API at subuno.com. You can download this API at
 http://github.com/subuno/api/
 
 */
 
 package com.subuno;
 
 import javax.net.ssl.HttpsURLConnection;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.Map;
 
 import java.io.UnsupportedEncodingException;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.json.JSONException;
 
 
 public class SUBUNOAPI {
 
 /*
 A client for the SUBUNO API.
 
 See subuno.com for complete API documentation.
 */
 
	public static final String SUBUNO_SERVER_URI = "https://api.subuno.com/v1/";
 
 	private String _apikey = null;
 	private String _server_uri = null;
 
 	public JSONObject run(String apikey, HashMap data) throws SUBUNOAPIError {
 		return this.run(apikey, data, this.SUBUNO_SERVER_URI);
 	}
 
 	public JSONObject run(String apikey, HashMap data, String server_uri) throws SUBUNOAPIError {
 		this._set_authentication_info(apikey, server_uri);
 		return this._call_server(data);
 	}
 	
 	private void _set_authentication_info(String apikey, String server_uri) {
 		this._apikey = apikey;
 		this._server_uri = server_uri;
 	}
 	
 	private JSONObject _call_server(HashMap args) throws SUBUNOAPIError {
 		if (this._apikey != null && this._apikey != "") {
 
 			Iterator i = null;
 
 			//create data packet
 			HashMap<String, String> data = new HashMap<String, String>();
 
 			i = args.entrySet().iterator();
 			while (i.hasNext()) {
 				Map.Entry pair = (Map.Entry)i.next();
 				data.put(pair.getKey().toString(), pair.getValue().toString());
 			}
 
 			//add apikey to the data packet.
 			data.put("apikey", this._apikey);
 			
 			//serialize data.
 			String urlencoding = "";
 			
 			try {
 				i = data.entrySet().iterator();
 				while (i.hasNext()) {
 					Map.Entry pair = (Map.Entry)i.next();
 					urlencoding = urlencoding + "&" +
 						URLEncoder.encode(
 							pair.getKey().toString(), "UTF-8"
 						) + "=" + 
 						URLEncoder.encode(
 							pair.getValue().toString(), "UTF-8"
 						);
 				}
 				urlencoding = urlencoding.substring(1); // remove first &				
 				
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}						
 			
 			URL url = null;
 			try {
 				url = new URL(this._server_uri + "?" + urlencoding);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 
 			System.out.println(url);
 
 			//perform request
 			HttpsURLConnection connection = null;
 			JSONObject json = null;
 			String line = null;
 
 			try {
 				connection = (HttpsURLConnection)url.openConnection();
 				connection.setRequestMethod("GET");
 				connection.connect();				
 
 				json = new JSONObject(new JSONTokener(new InputStreamReader(connection.getInputStream())));
 
 			} catch (IOException e) {
 				throw new SUBUNOAPIError("Server error or access denied. '"+e.toString()+"'");
 			} catch (JSONException e) {
 				throw new SUBUNOAPIError("Value doesn't convert to json object. '"+e.toString()+"'");
 			}
 
 			return json;
 		} else {
 			throw new SUBUNOAPIError("API key not set.");
 		}
 	}
 }
