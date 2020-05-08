 package com.omc.demo;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Random;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.protocol.HttpContext;
 
 public class AuthInterceptor implements HttpRequestInterceptor {
 	Random random = new Random();
 	private String websolrSecret;
 
 	public AuthInterceptor(String websolrSecret) {
 		random.setSeed(System.currentTimeMillis());
 		this.websolrSecret = websolrSecret;
 	}
 
 	@Override
 	public void process(HttpRequest request, HttpContext context)
 			throws HttpException, IOException {
 		Long time = System.currentTimeMillis() / 1000;
 		Long nonce = random.nextLong();
		String value = time + "" + nonce;
 		Mac mac;
 		try {
 			mac = Mac.getInstance("HmacSHA1");
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
		SecretKeySpec secret = new SecretKeySpec(websolrSecret.getBytes(),
				"HmacSHA1");
 		try {
 			mac.init(secret);
 		} catch (InvalidKeyException e) {
 			throw new RuntimeException(e);
 		}
		byte[] bytes = mac.doFinal(value.getBytes());
 
 		// Bytes to hex-string
 		StringBuffer result = new StringBuffer();
 		for (byte b : bytes) {
			result.append(String.format("%02x", b));
 		}
 		String digest = result.toString();
 
 		request.addHeader("X-Websolr-Time", time.toString());
 		request.addHeader("X-Websolr-Nonce", nonce.toString());
 		request.addHeader("X-Websolr-Auth", digest);
 	}
 }
