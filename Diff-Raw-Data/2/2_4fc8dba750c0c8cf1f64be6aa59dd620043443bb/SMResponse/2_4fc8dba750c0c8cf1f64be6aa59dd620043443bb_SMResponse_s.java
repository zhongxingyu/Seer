 package com.github.jkschoen.jsma.response;
 
 import java.util.Map;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.jkschoen.jsma.SmugMugAPI;
 import com.github.jkschoen.jsma.misc.JsmaLoggingFilter;
 import com.github.jkschoen.jsma.misc.SmugMugException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import com.sun.jersey.oauth.client.OAuthClientFilter;
 import com.sun.jersey.oauth.signature.OAuthParameters;
 import com.sun.jersey.oauth.signature.OAuthSecrets;
 
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class SMResponse {
 	static final Logger logger = LoggerFactory.getLogger(SMResponse.class);
 	
 	private String stat;
 	private String method;
 	private int code;
 	private String message;
 	
 	public String getStat() {
 		return stat;
 	}
 
 	public SMResponse(){	
 	}
 	
 	public SMResponse(String stat, String method) {
 		super();
 		this.stat = stat;
 		this.method = method;
 	}
 
 	public void setStat(String stat) {
 		this.stat = stat;
 	}
 
 	public String getMethod() {
 		return method;
 	}
 
 	public void setMethod(String method) {
 		this.method = method;
 	}
 	
 	public int getCode() {
 		return code;
 	}
 
 	public void setCode(int code) {
 		this.code = code;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((method == null) ? 0 : method.hashCode());
 		result = prime * result + ((stat == null) ? 0 : stat.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SMResponse other = (SMResponse) obj;
 		if (method == null) {
 			if (other.method != null)
 				return false;
 		} else if (!method.equals(other.method))
 			return false;
 		if (stat == null) {
 			if (other.stat != null)
 				return false;
 		} else if (!stat.equals(other.stat))
 			return false;
 		return true;
 	}
 
 	public String toStringSuper() {
         return "stat=" + stat + ", method=" + method;
     }
 	
     @Override
 	public String toString() {
 		return "SMResponse [stat=" + stat + ", method=" + method + ", code="
 				+ code + ", message=" + message + "]";
 	}
 
 
	private static final String URL_API = "http://api.smugmug.com/services/api/json/1.3.0/";
 
     /**
      * Makes the actual API request against the SmugMug API. It will return an instance
      * of the <code>type</code passed in if successful.
      * 
      * @param type Required, the class type to be returned
      * @param method Required, the api method to be called
      * @param params parameters to be passed in the request
      * @param token the access token that proves we are authorized to make the call
      * @return instance of the type containing the results of the response
      */
     public static <T extends SMResponse> T callMethod(SmugMugAPI smugmug, Class<T> type, String method, Map<String, String> params) throws SmugMugException {
     	return SMResponse.callMethod(smugmug,type, method, params, false);
     }
     
     /**
      * Makes the actual API request against the SmugMug API. It will return an instance
      * of the <code>type</code passed in if successful.
      * 
      * @param type Required, the class type to be returned
      * @param method Required, the api method to be called
      * @param params parameters to be passed in the request
      * @param token the access token that proves we are authorized to make the call
      * @param heavy Returns a heavy response for this method.
      * @return instance of the type containing the results of the response
      */
     public static <T extends SMResponse> T callMethod(SmugMugAPI smugmug, Class<T> type, String method, Map<String, String> params, boolean heavy) throws SmugMugException {
     	return SMResponse.callMethod(smugmug,type, method, params, null, false, false, false, heavy);
     }
     
     /**
      * Makes the actual API request against the SmugMug API. It will return an instance
      * of the <code>type</code passed in if successful.
      * 
      * @param type Required, the class type to be returned
      * @param method Required, the api method to be called
      * @param params parameters to be passed in the request
      * @param token the access token that proves we are authorized to make the call
      * @param extras additional attributes to return in the response
      * @param pretty a more human friendly response.
      * @param sandboxed URLs to a location with a crossdomain.xml file.
      * @param strict Enable strict error handling.
      * @param heavy Returns a heavy response for this method.
      * @return instance of the type containing the results of the response
      */
     public static <T extends SMResponse> T callMethod(SmugMugAPI smugmug,
     		Class<T> type, String method, Map<String, String> params, 
     		String[] extras, boolean pretty, boolean sandboxed, boolean strict,
     		boolean heavy) throws SmugMugException {
     	WebResource resource = SmugMugAPI.CLIENT.resource(URL_API).queryParam("method", method);
     	
     	MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
     	
     	if (extras != null && extras.length > 0){
     		String extraString = null;
     		for (String extra : extras){
     			if (extraString == null){
     				extraString = extra;
     			} else {
     				extraString = extraString+","+extra;
     			}
     		}
     		queryParams.add("Extras", extraString);
     	}
     	if (pretty){
     		queryParams.add("Pretty", "true");
     	}
     	if (sandboxed){
     		queryParams.add("Sandboxed", "true");
     	}
     	if (strict){
     		queryParams.add("Strict", "true");
     	}
     	if (heavy){
     		queryParams.add("Heavy", "true");
     	}
     	if (params != null){
     		for (String key : params.keySet()){
     			logger.debug("Adding Query Param: '"+key+"' with Value: '"+params.get(key)+"'");
     			queryParams.add(key, params.get(key));
     		}
     	}
     	
     	resource = resource.queryParams(queryParams);
         
     	JsmaLoggingFilter logFilter = new JsmaLoggingFilter();
         resource.addFilter(logFilter);
     	
     	OAuthSecrets secrets = new OAuthSecrets().consumerSecret(smugmug.getConsumerSecret());
         OAuthParameters oauthParams = new OAuthParameters().consumerKey(smugmug.getCosumerKey()).
                 signatureMethod("HMAC-SHA1").version("1.0");
         // Create the OAuth client filter
         OAuthClientFilter filter = new OAuthClientFilter(SmugMugAPI.CLIENT.getProviders(), oauthParams, secrets);
         // Add the filter to the resource
         if (smugmug.getToken() != null){
             secrets.setTokenSecret(smugmug.getToken().getSecret());
             oauthParams.token(smugmug.getToken().getId());
         }
         resource.addFilter(filter);
 
     	
     	WebResource.Builder builder = resource.getRequestBuilder();
     	//User agent
 		builder = builder.header("User-Agent", smugmug.getAppName());
 		
         T response = builder.get(type);
         if (!"ok".equals(response.getStat())) {
             throw new SmugMugException(response);
         }
     	return response;
     }
 }
