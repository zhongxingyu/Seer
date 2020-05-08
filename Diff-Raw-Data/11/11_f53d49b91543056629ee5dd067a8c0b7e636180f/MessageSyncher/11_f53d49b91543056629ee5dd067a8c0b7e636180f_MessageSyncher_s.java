 /**
  * 
  */
 package net.frontlinesms.plugins.sync;
 
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.data.domain.FrontlineMessage;
 import net.frontlinesms.messaging.MessageFormatter;
 
 /**
  * @author ekala
  *
  */
 public class MessageSyncher {
 	private static final String POST = "POST";
 	private static final String GET = "GET";
 	/** HTTP request method name */
 	private String requestMethod ;
 	/**Base URL */
 	private String baseURL;
 	
 	/** Stores the key=>value mapping of the URL variables */
 	private Map<String, String> templateParams;
 	/** Loggger */
 	private static final Logger LOG = FrontlineUtils.getLogger(MessageSyncher.class);
 	
 //> CONSTRUCTORS 
 	/**
 	 * Constructor for this class
 	 * 
 	 * @param synchronisationURL URL to be synchronised to
 	 */
 	public MessageSyncher(String synchronisationURL, Map<String ,String> paramMap) {
 //		setSynchronisationURL(synchronisationURL);
 		this.baseURL = synchronisationURL;
 		this.templateParams = paramMap;
 	}
 	
 	/** Empty constructor */
 	public MessageSyncher() { }
 	
 	
 	boolean syncMessage(FrontlineMessage message) {
 		Map<String, String> paramMap = createRequestParam(message);
 		return doHttpRequest(paramMap);
 	}
 
 	/** Sets the request method for the syncher */
 	public void setRequestMethod(String requestMethod) {
 		this.requestMethod = requestMethod;
 	}
 	
 	/** Gets the request method for the syncher */
 	public String getRequestMethod() {
 		return requestMethod;
 	}
 	
 	/** Performs a HTTP request; GET or POST */
 	private boolean doHttpRequest(Map<String, String> paramMap) {
 		String data = buildRequestString(paramMap);
 
 		String url = this.baseURL;
 		if(requestMethod.equals(GET)) {
 			url += '?' + data;
 		}
 		
 		// Modify the url depending on the request method
 		LOG.debug("Sending synchronisation request: " + url);
 		
 		HttpURLConnection conn = null;
 		OutputStreamWriter out = null;
 		try {
 			conn = (HttpURLConnection) new URL(url).openConnection();
 			
 			if (requestMethod.equals(POST)) {
 				conn.setDoOutput(true);
 				conn.setRequestMethod(requestMethod);
 
 				out = new OutputStreamWriter(conn.getOutputStream());
 				out.write(data);
 				out.flush();
 			}
 			
 			return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
 			
		} catch (Exception e) {
 			return false;
 		} finally {
 			try { out.close(); } catch(Exception ex) { /* ignore */ }
 			try { conn.disconnect(); } catch(Exception ex) { /* ignore */ }
 		}
 	}
 
 	private static String buildRequestString(Map<String, String> paramMap) {
 		StringBuilder bob = new StringBuilder();
 		for(Entry<String, String> e : paramMap.entrySet()) {
 			bob.append('&');
 			assert(!e.getKey().contains("="));
 			bob.append(e.getKey());
 			bob.append('=');
 			bob.append(FrontlineUtils.urlEncode(e.getValue()));
 		}
 		return bob.length() > 0 ? bob.substring(1) : "";
 	}
 
 	/** Creates a map of the parameters to be passed to the base url */
 	private Map<String, String> createRequestParam(FrontlineMessage message) {
 		HashMap<String, String> params = new LinkedHashMap<String, String>();
 		
 		for (Entry<String, String> entry: templateParams.entrySet()) {
 			String paramName = entry.getKey();
 			String val = MessageFormatter.formatMessage(entry.getValue(),
 					MessageFormatter.MARKER_SENDER_NUMBER,		/*->*/ message.getSenderMsisdn(),
 					// N.B. message content should always be substituted last to prevent injection attacks
 					MessageFormatter.MARKER_MESSAGE_CONTENT,	/*->*/ message.getTextContent() 
 					);
 			
 			params.put(paramName, val);
 		}
 		
 		return params;
 	}
 	
 	/** Gets the HttpRequest URL used for synchronisation */
 	public String getHttpRequestURL(FrontlineMessage message) {
 		return requestMethod.equals(GET)
 				? this.baseURL + "?" + buildRequestString(createRequestParam(message))
 				: this.baseURL;
 	}
 }
