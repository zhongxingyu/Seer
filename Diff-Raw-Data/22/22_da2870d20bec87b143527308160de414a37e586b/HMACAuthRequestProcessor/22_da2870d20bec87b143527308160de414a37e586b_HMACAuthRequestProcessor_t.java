 package ch.hsr.bieridee.test.http.requestprocessors;
 
 import ch.hsr.bieridee.test.http.IRequestProcessor;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.http.HttpEntity;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpRequestBase;
 
 import javax.crypto.Mac;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 
 public class HMACAuthRequestProcessor implements IRequestProcessor {
 
 	private final static String LOG_TAG = "HMACAuth";
 	private String username;
 	private String password;
 
 	public HMACAuthRequestProcessor(final String username, final String password) {
 		this.username = username;
 		this.password = password;
 	}
 
 	public HttpRequestBase processRequest(HttpRequestBase request) {
 		final String uri = request.getURI().toString();
 		final String method = request.getMethod();
 		final String accept = request.getFirstHeader("Accept").getValue();
 
 		String body = "";
 		String contentLength = "0";
 
 		if (request instanceof HttpEntityEnclosingRequestBase) {
 			final HttpEntity entity = ((HttpEntityEnclosingRequestBase) request).getEntity();
 			if (entity != null) {
 				contentLength = String.valueOf(entity.getContentLength());
//				final byte[] content = new byte[(int)entity.getContentLength()];
//				try {
//					((HttpEntityEnclosingRequestBase) request).getEntity().getContent().read(content);
//				} catch (IOException e) {
//					throw new RuntimeException("Could not get body from " + request.getMethod().toUpperCase() + " request.");
//				}
//				body = new String(content);
 			}
 		}
 
 		final String hmacInputData = method + uri + accept + contentLength + body;
 		String macString = "";
 		try {
 			final SecretKey signingKey = new SecretKeySpec(this.password.getBytes(), "HmacSHA256");
 
 			final Mac m = Mac.getInstance("HmacSHA256");
 			m.init(signingKey);
 			m.update(hmacInputData.getBytes());
 			byte[] macBytes = m.doFinal();
 
 			macString = new String(Hex.encodeHex(macBytes));
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException("HmacSHA256 algorithm missing", e);
 		} catch (InvalidKeyException e) {
 			throw new RuntimeException("Invalid key for HmacSHA256 signing", e);
 		}
 
 		if (macString.isEmpty()) {
 			throw new RuntimeException("Empty Authorization-HMAC");
 		}
 		final String authorizationHeader = this.username + ":" + macString;
 		request.setHeader("Authorization", authorizationHeader);
 
 		return request;
 	}
 }
