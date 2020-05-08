 package DroidSocial.Network;
 
 // necessary packages for using streams
 import java.io.*;
 
 // necessary for CGI encoding
 import java.net.URLEncoder;
 
 // necessary for 
 import java.util.*;
 
 // necessary for communicating via http
 import org.apache.http.*;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 
 // necessary for generating and parsing xml 
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 
 public abstract class SocialNetworkRequest {
 	
 	// The base URI that all services of this network reside at
 	private String ServiceURI = "";
 	
 	// Objects that are critical to http interaction
 	private HttpClient Client = null;
 	private HttpResponse Response = null;
 	
 	protected SocialNetwork NetworkObj = null;
 	
 	
 	
 	/**
 	 * Constructor: 
 	 * @param service_uri : the base uri of all services on this network
 	 */
 	public SocialNetworkRequest(String service_uri) {
 
 		this.ServiceURI = service_uri;
 		// Create the http client object
 		this.Client = new DefaultHttpClient();
 		
 	}
 	
 	protected String SendGetRequest(Map<String, String> get_params, Map<String, String> headers) throws SocialNetworkAPIException {
 		String output = "none";
 		String get_param_string = "";
 		Set<String> header_keys = null;
 		
 		// Build the get param string
 		if (get_params != null) get_param_string = ToURIStr(get_params);
 		
 		try {
 			// Create the get request object with any parameters
			HttpGet getReq = new HttpGet(this.ServiceURI + get_param_string);
 			
 			if (headers != null) {
 				header_keys = headers.keySet();
 				for (String key : header_keys) {
 					getReq.addHeader(new BasicHeader(key, headers.get(key)));
 				}
 			}
 			
 			this.Response = this.Client.execute(getReq);
 			
 			// Get the HttpEntity that contains the response data
 			HttpEntity en = this.Response.getEntity();
 			InputStream instream = en.getContent();
 			output = StreamToString(instream);
 		}
 		catch (Exception e) {
 			throw new SocialNetworkAPIException(e.getMessage());
 		}
 		
 		return output;
 	}
 	
 	protected String SendPostRequest(Map<String, String> post_data, Map<String, String> get_params, Map<String, String> headers) {
 		return this.SendPostRequest(this.ServiceURI, post_data, get_params, headers);
 	}
 	
 	protected String SendPostRequest(String URI, Map<String, String> post_data, Map<String, String> get_params, Map<String, String> headers) {
 	
 		List<NameValuePair> post_fields = new ArrayList<NameValuePair>();
 		Set<String> post_keys = null;
 		Set<String> header_keys = null;
 		
 		String output = "";		
 		String getStr = "";		
 		
 		try {
 			
 			// GET params
 			if (get_params != null) {
 				getStr = ToURIStr(get_params);
 			}
 			
 			// POST params
 			if (post_data != null) {
 				post_keys = post_data.keySet();
 				for (String key : post_keys) {
 					post_fields.add(new BasicNameValuePair(key, post_data.get(key)));
 				}
 			}
 
 			HttpPost p = new HttpPost(URI + "?" + getStr);
 			
 			// Headers
 			if (headers != null) {
 				header_keys = headers.keySet();
 				for (String key : header_keys) {
 					p.addHeader(new BasicHeader(key, headers.get(key)));
 				}
 			}
 			
 			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post_fields);
 			p.setEntity(entity);
 		
 			// execute the request
 			this.Response = this.Client.execute(p);
 
 			HttpEntity en = this.Response.getEntity();
 			InputStream in = en.getContent();
 			output = StreamToString(in);
 			
 		}
 		catch (Exception e) {
 			String msg = e.getMessage();
 		}
 		finally {
 			this.Response = null;
 		}
 		
 		return output;	
 	}
 	
 	/**
 	 * TODO: ADD other methods for performing HTTP Post Requests
 	 * 		 that carry data in the message by using File and FileEntity objects
 	 * 		 http://www.mail-archive.com/android-developers@googlegroups.com/msg72456.html
 	 */
 
 
 	/**
 	 * Convert a String indexed Map of Strings to a URI encoded UTF-8 string
 	 * @param params Map of String indexed Strings
 	 * @return A URI encoded representation of the key value pairs represented by params
 	 */
 	public static String ToURIStr(Map<String, String> params) {
 		Set<String> keys = params.keySet();
 		StringBuilder sb = new StringBuilder();
 		
 		try {
 			int count = 1;
 			for (String key : keys) {
 				sb.append(key + "=" + params.get(key));
 				count++;
 				if (count <= keys.size()) sb.append("&");
 			}
 		}
 		catch (Exception e) {}
 		return sb.toString();
 	}
 	
     protected static String StreamToString(InputStream is) {
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
  
         String line = "";
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         finally {
             try {
                 is.	close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return sb.toString();
     }
 }
