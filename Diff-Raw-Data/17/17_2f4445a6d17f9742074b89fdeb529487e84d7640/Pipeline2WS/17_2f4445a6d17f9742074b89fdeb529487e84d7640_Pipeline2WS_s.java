 package pipeline2;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.security.SignatureException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.management.RuntimeErrorException;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 
 import play.Logger;
 
 public class Pipeline2WS {
 	
 	public static final Map<String, String> ns; 
 	static {
 		// Useful as the last parameter with the Play 2.0 XPath library
     	Map<String, String> nsMap = new HashMap<String, String>();
     	nsMap.put("d", "http://www.daisy.org/ns/pipeline/data");
     	ns = Collections.unmodifiableMap(nsMap);
 	}
 	
 	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
 	
 	public static DateFormat iso8601;
 	static {
 		iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 		iso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 	
 	/**
 	 * Send a GET request.
 	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
 	 * @param path Path to resource, for instance "/scripts".
 	 * @param username Robot username.
 	 * @param secret Robot secret.
 	 * @param parameters URL query string parameters
 	 * @return The return body.
 	 */
 	public static Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) {
 		String url = url(endpoint, path, username, secret, parameters);
 		if (endpoint == null) {
 			return new Pipeline2WSResponse(503, "Pipeline 2 Web Service endpoint is not configured.", "Please configure Pipeline 2 in the administrator settings.", null);
 		}
 		
 		ClientResource resource = new ClientResource(url);
 		Representation representation;
 		InputStream in = null;
 		try {
 			representation = resource.get();
 			if (representation != null)
 				in = representation.getStream();
 			
 		} catch (ResourceException e) {
			// Unauthorized etc. Silently ignore...
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Status status = resource.getStatus();
 		
 		return new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), in);
 	}
 	
 	/**
 	 * POST an XML document.
 	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
 	 * @param path Path to resource, for instance "/scripts".
 	 * @param username Robot username.
 	 * @param secret Robot secret.
 	 * @param xml The XML document to post.
 	 * @return The return body.
 	 */
 	public static Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) {
 		String url = url(endpoint, path, username, secret, null);
 		
 		Logger.debug("URL: ["+url+"]");
 		Logger.debug(utils.XML.toString(xml));
 		
 		ClientResource resource = new ClientResource(url);
 		Representation representation = null;
 		try {
 			representation = resource.post(xml);
 		} catch (org.restlet.resource.ResourceException e) {
 			Logger.error(e.getMessage(), e);
 		}
 		
 		InputStream in = null;
 		if (representation != null) {
 			try {
 				in = representation.getStream();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		Status status = resource.getStatus();
 		
 		return new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), in);
 	}
 	
 	/**
 	 * POST a multipart request.
 	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
 	 * @param path Path to resource, for instance "/scripts".
 	 * @param username Robot username.
 	 * @param secret Robot secret.
 	 * @param parts A map of all the parts.
 	 * @return The return body.
 	 */
 	public static Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) {
 		String url = url(endpoint, path, username, secret, null);
 		
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(url);
 		
 		MultipartEntity reqEntity = new MultipartEntity();
 		for (String partName : parts.keySet()) { 
 			reqEntity.addPart(partName, new FileBody(parts.get(partName)));
 		}
 		httppost.setEntity(reqEntity);
 		
 		HttpResponse response = null;
 		try {
 			response = httpclient.execute(httppost);
 		} catch (ClientProtocolException e) {
 			Logger.error("Error while POSTing: "+e.getMessage());
 			throw new RuntimeErrorException(new Error(e), "Error while POSTing.");
 		} catch (IOException e) {
 			Logger.error("Error while POSTing: "+e.getMessage());
 			throw new RuntimeErrorException(new Error(e), "Error while POSTing.");
 		}
 		HttpEntity resEntity = response.getEntity();
 		
 		InputStream bodyStream = null;
 		try {
 			bodyStream = resEntity.getContent();
 		} catch (IOException e) {
 			Logger.error("Error while reading response body");
 			throw new RuntimeErrorException(new Error(e), "Error while reading response body"); 
 		}
 		
 		Status status = Status.valueOf(response.getStatusLine().getStatusCode());
 		
 		return new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), bodyStream);
 	}
 	
 	public static String url(String endpoint, String path, String username, String secret, Map<String,String> parameters) {
 		String time = iso8601.format(new Date());
 		
 		String nonce = "";
 		while (nonce.length() < 30)
 			nonce += (Math.random()+"").substring(2);
 		nonce = nonce.substring(0, 30);
 		
 		String url = endpoint + path + "?";
 		if (parameters != null) {
 			for (String name : parameters.keySet()) {
 				try { url += URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(parameters.get(name), "UTF-8") + "&"; }
 				catch (UnsupportedEncodingException e) { Logger.error("Unsupported encoding: UTF-8", e); }
 			}
 		}
 		url += "authid="+username + "&time="+time + "&nonce="+nonce;
 		
 		String hash = "";
 		try {
 			hash = calculateRFC2104HMAC(url, secret);
 			String hashEscaped = "";
 			char c;
 			for (int i = 0; i < hash.length(); i++) {
 				// Base64 encoding uses + which we have to encode in URL parameters.
 				// Hoping this for loop is more efficient than the equivalent replace("\\+","%2B") regex.
 				c = hash.charAt(i);
 				if (c == '+') hashEscaped += "%2B";
 				else hashEscaped += c;
 			} 
 			url += "&sign="+hashEscaped;
 			
 		} catch (SignatureException e) {
 			Logger.error("Could not sign request.");
 			e.printStackTrace();
 		}
 		
 		return url;
 	}
 	
 	// adapted slightly from
     // http://docs.amazonwebservices.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/index.html?AuthJavaSampleHMACSignature.html
 	// (copied from Pipeline 2 fwk code)
     /**
     * Computes RFC 2104-compliant HMAC signature.
     * * @param data
     * The data to be signed.
     * @param secret
     * The signing secret.
     * @return
     * The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws
     * java.security.SignatureException when signature generation fails
     */
     private static String calculateRFC2104HMAC(String data, String secret) throws java.security.SignatureException {
         byte[] result;
         try {
             // get an hmac_sha1 key from the raw key bytes
             SecretKeySpec signingSecret = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
 
             // get an hmac_sha1 Mac instance and initialize with the signing key
             Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
             mac.init(signingSecret);
 
             // compute the hmac on input data bytes
             byte[] rawHmac = mac.doFinal(data.getBytes());
 
             // base64-encode the hmac
             result = Base64.encodeBase64(rawHmac);
 
         } catch (Exception e) {
             throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
         }
         return new String(result);
     }
 
 	
 }
