 package com.sk.web;
 
 import java.net.MalformedURLException;
 import java.net.URLConnection;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Objects;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 public class OAuthRequest extends Request {
 
 	private static final String DIGEST_ALGORITHM = "HmacSHA1";
 
 	private final Map<String, String> oauth = new HashMap<>();
 
 	public OAuthRequest(String url, String method) throws MalformedURLException {
 		super(url, method);
 	}
 
 	public void addOAuthParam(String key, String value) {
 		oauth.put("oauth_" + key, value);
 	}
 
 	public void signOAuth(OAuthToken consumer, OAuthToken token) {
 		addBasicOAuthParams();
 		addKeyParams(consumer, token);
 		addOAuthSignature(consumer, token);
 	}
 
 	private void addBasicOAuthParams() {
 		addOAuthParam("signature_method", "HMAC-SHA1");
 		addOAuthParam("version", "1.0");
 		addOAuthParam("nonce", IOUtil.generateNonce());
 		addOAuthParam("timestamp", Objects.toString(System.currentTimeMillis() / 1000));
 	}
 
 	private void addKeyParams(OAuthToken consumer, OAuthToken token) {
 		addOAuthParam("consumer_key", consumer.getKey());
 		if (token != null)
 			addOAuthParam("token", token.getKey());
 	}
 
 	private void addOAuthSignature(OAuthToken consumer, OAuthToken token) {
 		addOAuthParam("signature", getOAuthSignature(consumer, token));
 	}
 
 	private String getOAuthSignature(OAuthToken consumer, OAuthToken token) {
 		String signature = getSignatureString();
 		String secret = getEncodedSecret(consumer, token);
 		byte[] digest = getDigest(signature.getBytes(), secret.getBytes());
		return Base64Util.encode(digest);
 	}
 
 	private String getEncodedSecret(OAuthToken consumer, OAuthToken token) {
 		StringBuilder secret = new StringBuilder();
 		secret.append(IOUtil.urlEncode(new String(consumer.getSecret())));
 		secret.append("&");
 		if (token != null)
 			secret.append(IOUtil.urlEncode(new String(token.getSecret())));
 		return secret.toString();
 	}
 
 	private byte[] getDigest(byte[] data, byte[] secret) {
 		try {
 			Mac mac = Mac.getInstance(DIGEST_ALGORITHM);
 			mac.init(new SecretKeySpec(secret, DIGEST_ALGORITHM));
 			return mac.doFinal(data);
 		} catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
 			throw new RuntimeException("Failed OAuth Digest");
 		}
 	}
 
 	private String getSignatureString() {
 		StringBuilder signature = new StringBuilder();
 		signature.append(getRequestMethod());
 		signature.append("&");
 		signature.append(IOUtil.urlEncode(getBaseURL().toExternalForm()));
 		signature.append("&");
 		signature.append(getAllParameterString());
 		return signature.toString();
 	}
 
 	private String getAllParameterString() {
 		TreeMap<String, String> allParams = getAllParamsSorted();
 		StringBuilder paramStringBuilder = new StringBuilder();
 		for (Entry<String, String> param : allParams.entrySet()) {
 			paramStringBuilder.append("&");
 			paramStringBuilder.append(IOUtil.urlEncode(param.getKey()));
 			paramStringBuilder.append("=");
 			paramStringBuilder.append(IOUtil.urlEncode(param.getValue()));
 		}
 		String paramString = paramStringBuilder.length() > 0 ? paramStringBuilder.substring(1) : "";
 		return IOUtil.urlEncode(paramString);
 	}
 
 	private TreeMap<String, String> getAllParamsSorted() {
 		TreeMap<String, String> allParams = new TreeMap<>();
 		allParams.putAll(getQueryAndUrlParams());
 		allParams.putAll(oauth);
 		return allParams;
 	}
 
 	@Override
 	public void addRequestHeaders(URLConnection conn) {
		super.addRequestHeaders(conn);
 		conn.addRequestProperty("Authorization", joinOAuthProperties());
 	}
 
 	private String joinOAuthProperties() {
 		boolean firstIteration = true;
 		StringBuilder oauthProp = new StringBuilder("OAuth ");
 		for (Entry<String, String> prop : oauth.entrySet()) {
 			if (!firstIteration)
 				oauthProp.append(",");
 			else
 				firstIteration = false;
			oauthProp.append(IOUtil.urlEncode(prop.getKey()));
 			oauthProp.append("=\"");
			oauthProp.append(IOUtil.urlEncode(prop.getValue()));
 			oauthProp.append("\"");
 		}
 		return oauthProp.toString();
 	}
 }
