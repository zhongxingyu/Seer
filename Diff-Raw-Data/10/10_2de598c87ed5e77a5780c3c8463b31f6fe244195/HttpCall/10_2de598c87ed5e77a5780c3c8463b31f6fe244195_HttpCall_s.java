 package ja.tools.common;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * 
  * @author jobert
  * 
  */
 public class HttpCall {
 	public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
 	public static final String CONTENT_TYPE = "Content-type";
 	private URLConnection conn;
 	private Map<String, String> body;
 
 	private HttpCall(String url) {
 		body = new HashMap<String, String>();
 		try {
 			conn = new URL(url).openConnection();
 		} catch (Exception e) {
 		}
 	}
 
 	/**
 	 * Create a new object.
 	 * 
 	 * @param url
 	 * @return request object wrapper
 	 */
 	public static HttpCall create(String url) {
 		return new HttpCall(url);
 	}
 
 	/**
 	 * Add request property
 	 * 
 	 * @param key
 	 * @param value
 	 * @return self
 	 */
 	public HttpCall addRequestProperty(String key, String value) {
 		conn.setRequestProperty(key, value);
 		return this;
 	}
 
 	/**
 	 * Append a value to body.
 	 * 
 	 * @param key
 	 * @param value
 	 * @return self
 	 */
 	public HttpCall addRequestBody(String key, String value) {
 		body.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Execute a get request.
 	 * 
 	 * @return the data
 	 * @throws Exception
 	 */
 	public byte[] get() throws Exception {
 		InputStream in = null;
 		byte[] data = null;
 		try {
 			in = conn.getInputStream();
			data = new byte[in.available()];
			in.read(data);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Execute a post request.
 	 * 
 	 * @return the data
 	 * @throws Exception
 	 */
 	public byte[] post() throws Exception {
 		conn.setDoOutput(true);
 		addRequestProperty(CONTENT_TYPE, FORM_URL_ENCODED);
 		conn.connect();
 		String body = buildRequestBody();
 		if (body != null && body.length() > 0) {
 			OutputStream output = null;
 			try {
 				output = conn.getOutputStream();
 				output.write(body.getBytes());
 			} finally {
 				if (output != null)
 					try {
 						output.close();
 					} catch (IOException e) {
 					}
 			}
 		}
 		return get();
 	}
 
 	/**
 	 * Build the request body.
 	 * 
 	 * @return serialized form of request body.
 	 * @throws Exception
 	 */
 	private String buildRequestBody() throws Exception {
 		StringBuffer sb = new StringBuffer();
 		Set<String> keys = body.keySet();
 		int count = 0;
 		for (String key : keys) {
 			String value = body.get(key);
 			sb.append(key).append('=')
 					.append(URLEncoder.encode(value, "utf-8"));
 			if ((count + 1) < keys.size()) {
 				sb.append('&');
 			}
 		}
 		return sb.toString();
 	}
 }
