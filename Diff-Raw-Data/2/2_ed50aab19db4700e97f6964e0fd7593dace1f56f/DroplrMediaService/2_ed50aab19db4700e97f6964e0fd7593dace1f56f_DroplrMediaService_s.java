 package com.teamboid.twitterapi.media;
 
 import java.io.BufferedOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.SignatureException;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import android.util.Base64;
 
 import com.teamboid.twitterapi.client.RequestHandler;
 import com.teamboid.twitterapi.client.Twitter;
 import com.teamboid.twitterapi.client.TwitterException;
 import com.teamboid.twitterapi.status.StatusUpdate;
 import com.teamboid.twitterapi.status.entity.media.MediaEntity;
 import com.teamboid.twitterapi.status.entity.url.UrlEntity;
 import com.teamboid.twitterapi.utilities.Utils;
 
 /**
  * Drop.lr
  * 
  * To get access you must contact Drop.lr yourself.
  * ApiKey = Public Key
  * ApiSecret = Private Key
  * @author kennydude
  *
  */
 public class DroplrMediaService extends ExternalMediaService {
 	public static final String ENDPOINT = "http://dev.droplr.com:8069/";
 
 	@Override
 	public String getServiceName() {
 		return "Droplr";
 	}
 
 	@Override
 	public String[] getSupportedUrls() {
 		return new String[]{};
 	}
 	
 	public String getUserName(){ // email in this case
 		HttpURLConnection client = null;
 		try{
 			URL get = new URL(ENDPOINT + "account");
 			client = (HttpURLConnection) get.openConnection();
 			client.addRequestProperty("Content-Length", "0");
 			authorizeRequest(client);
 			
 			InputStream ir = client.getInputStream();
 			if(client.getResponseCode() == 200){
 				return client.getHeaderField("x-droplr-email");
 			} else{
 				throw new Exception("Droplr did not return 200: "+ client.getHeaderField("x-droplr-errordetails"));
 			}
 		} catch(Exception e){
 			showHTTPError(client);
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	void showHTTPError(HttpURLConnection client){
 		if(client == null) return;
 		System.out.println(client.getHeaderField("x-droplr-errordetails"));
 	}
 	
 	void authorizeRequest(HttpURLConnection request){
 		request.addRequestProperty("Date", System.currentTimeMillis()+"");
 		request.addRequestProperty("User-Agent", "Droplr+Boid+Lib/1.0");
 		request.addRequestProperty("Authorization", getDroplrSignature(request));
 	}
 	
 	/**
 	 * This is a lot of work!
 	 * @param request
 	 * @return
 	 */
 	String getDroplrSignature(HttpURLConnection request){
 		try{
 			String password = Utils.sha1(authPassword + "");
 			
 			// Base64
 			String accessKey = Base64.encodeToString(
 					(apiKey + ":" + authMail).getBytes()
 			, Base64.NO_WRAP);
 			
 			// Signature
 			String accessSecret = apiSecret + ":" + password;
 			String contentType = "";
 			
 	        if (request.getRequestProperty("Content-Type") != null) {
 	            contentType = request.getRequestProperty("Content-Type");
 	        }
 	        
 	        String date = request.getRequestProperty("Date");
 			String stringToSign =
 					request.getRequestMethod() + ' ' + // GET 
 					request.getURL().getPath() + ' ' + // /account
 					"HTTP/1.1" + '\n' +
 					contentType + '\n' +
 					date;
 			
 			String signature = calculateRfc2104Hmac(stringToSign, accessSecret);
 			
 			// Return
 			return ("droplr " + accessKey + ":" + signature).replaceAll("\n", "");
 		} catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
     /**
      * Computes RFC 2104-compliant HMAC signature.
      *
      * @param data The data to be signed.
      * @param key The signing key.
      *
      * @return The Base64-encoded RFC 2104-compliant HMAC signature.
      *
      * @throws java.security.SignatureException when signature generation fails
      */
     public static String calculateRfc2104Hmac(String data, String key) throws SignatureException {
         String result;
         try {
             // get an hmac_sha1 key from the raw key bytes
             SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
             // get an hmac_sha1 Mac instance and initialize with the signing key
             Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
             mac.init(signingKey);
             // compute the hmac on input data bytes
             byte[] rawHmac = mac.doFinal(data.getBytes());
             // base64-encode the hmac
             result = Base64.encodeToString(rawHmac, Base64.DEFAULT);
         } catch (Exception e) {
             throw new SignatureException("Failed to generate HMAC: " + e.getMessage());
         }
 
         return result;
     }
 	
 	public AuthorizationNeeded getNeededAuthorization(){
 		return AuthorizationNeeded.MAIL_AND_PASSWORD;
 	}
 
 	@Override
 	public MediaEntity uploadFile(StatusUpdate tweet, Twitter tw,
 			InputStream file, long length) throws TwitterException {
 		HttpURLConnection client = null;
 		try{
 			URL post = new URL(ENDPOINT + "files");
 			client = (HttpURLConnection) post.openConnection();
 			client.setRequestMethod("POST");
 			client.addRequestProperty("Content-Type", "image/jpg");
 			client.addRequestProperty("x-droplr-privacy", "PUBLIC");
 			client.addRequestProperty("x-droplr-filename", "boid-upload.jpg");
 			
 			authorizeRequest(client);
 			
 			client.setDoOutput(true);
			client.setFixedLengthStreamingMode(length);
 			OutputStream out = new BufferedOutputStream(client.getOutputStream());
 			
 			byte[] b = new byte[1024];
             int noOfBytes = 0;
 			while( (noOfBytes = file.read(b)) != -1 ) {
 				out.write(b, 0, noOfBytes);
 			}
 			out.close();
 			
 			InputStream ir = client.getInputStream();
 			if(client.getResponseCode() == 201){
 				ExternalMediaEntity ema = new ExternalMediaEntity(client.getHeaderField("x-droplr-shortlink"));
 				
 				return ema;
 			} else{
 				throw new Exception("Droplr did not return 201: "+ client.getHeaderField("x-droplr-errordetails"));
 			}
 		} catch(Exception e){
 			showHTTPError(client);
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public MediaEntity getEntity(UrlEntity in) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public MediaEntity uploadFile(StatusUpdate tweet, Twitter tw,
 			RequestHandler client, InputStream file) throws TwitterException {
 		return null;
 	}
 	
 }
