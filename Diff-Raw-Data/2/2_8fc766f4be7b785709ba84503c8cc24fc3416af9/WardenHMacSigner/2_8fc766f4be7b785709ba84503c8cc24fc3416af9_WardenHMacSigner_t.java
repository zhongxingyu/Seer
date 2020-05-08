 package com.asquera.hmac;
 
 import java.net.URISyntaxException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 /**
  * A helper class that provides functions to create signatures for HMac. It is easy to use.
  * 
  */
public class WardenHMacSigner {
     
     private final Mac algorithm;
     
     /**
      * Creates a signer with 'SHA1' as default HMac hash algorithm.
      * 
      * @throws NoSuchAlgorithmException thrown if SHA1 is not supported!
      */
     public WardenHMacSigner() throws NoSuchAlgorithmException {
         this(Mac.getInstance("HmacSHA1"));
     }
     
     /**
      * Creates a signer with a specific hash algorithm.
      * 
      * @param algorithm The instance of a hash algorithm this signer shall use.
      */
     public WardenHMacSigner(final Mac algorithm) {
         this.algorithm = algorithm;
     }
     
     /**
      * Signs the given request. Url, secret and request parameters are used to generate the signature
      * and the resulting request object.
      * 
      * @param url     The url of the request
      * @param secret  The shared secret for the signature
      * @param options Options for the signature generation
      * 
      * @return A Request object with the signature and filled values.
      * 
      * @throws URISyntaxException Thrown when the given url is not not a valid URI.
      * @throws InvalidKeyException Thrown when the shared secret is rejected by the used hash algorithm.
      * @throws NoSuchAlgorithmException Thrown when the provided Mac algorithm is not supported or cannot be applied.
      */
     public Request signRequest(final String url, final String secret, final RequestParams options)
             throws URISyntaxException, InvalidKeyException, NoSuchAlgorithmException {
         
         Request request = new Request(url, options);
         String signature = generateSignature(secret, request);
         
         if (options.isQueryBased()) {
             
             List<NameValuePair> auth_params = new ArrayList<NameValuePair>(options.extraAuthParams());
             auth_params.add(new BasicNameValuePair("date", request.dateAsString()));
             auth_params.add(new BasicNameValuePair("signature", signature));
             
             if (!options.nonce().isEmpty()) {
                 auth_params.add(new BasicNameValuePair("nonce", options.nonce()));
             }
             
             request.query().add(options.authParam(), auth_params);
         
         } else {
             
             List<NameValuePair> headers = new ArrayList<NameValuePair>();
             
             Map<String, String> map = new HashMap<String, String>();
             for (NameValuePair pair : options.extraAuthParams()) {
                 map.put(pair.getName(), pair.getValue());
             }
             map.put("signature", signature);
             
             headers.add(new BasicNameValuePair("auth_header", options.authHeaderFormat(map)));
             if (!options.nonce().isEmpty()) {
                 headers.add(new BasicNameValuePair("nonce", options.nonce()));
             }
             
             if (options.useAlternateDateHeader()) {
                 headers.add(new BasicNameValuePair(options.alternateDateHeader(), request.dateAsString()));
             } else {
                 headers.add(new BasicNameValuePair("Date", request.dateAsString()));
             }
             
             options.addHeaders(headers);
         }
         
         return request;
     }
     
     /**
      * Signs a given url for use with query-based authentication. The signature is part of the
      * url's query.
      * 
      * @param url     The url to sign
      * @param secret  The shared secret used to sign the url
      * @param options Request parameters controlling the signature generation
      * @return A signed url with the signature as part of the query. 
      * 
      * @throws URISyntaxException Thrown when the given url is not not a valid URI.
      * @throws InvalidKeyException      Thrown when the provided secret key is inappropriate for the hash algorithm
      * @throws NoSuchAlgorithmException Thrown when the provided Mac algorithm is not supported or cannot be applied.
      */
     public String signUrl(final String url, final String secret, final RequestParams options)
             throws InvalidKeyException, NoSuchAlgorithmException, URISyntaxException {
         options.setQueryBased(true);
         Request request = signRequest(url, secret, options);
         return request.url();
     }
     
     /**
      * Generates a signature of the shared secret and the canonical representation of the given Request.
      * 
      * @param secret  The shared secret
      * @param request A Request object that is used to generate the signature for.
      * @return Returns a hash string (the signature) in hexadecimal format.
      * 
      * @throws NoSuchAlgorithmException Thrown when used hash algorithm is not available
      * @throws InvalidKeyException      Thrown when the provided secret key is inappropriate for the hash algorithm
      */
     protected String generateSignature(final String secret, final Request request) throws InvalidKeyException, NoSuchAlgorithmException {
         return hash_hmac(secret, request.canonicalRepresentation());
     }
     
     /**
      * Applies the Mac algorithm to the pair of message and shared secret and returns the resulting hash value (Mac).
      * 
      * @param secret  The shared secret
      * @param message The message to sign
      * @return Returns a hash string (the signature) in hexadecimal format.
      * 
      * @throws NoSuchAlgorithmException Thrown when used hash algorithm is not available
      * @throws InvalidKeyException      Thrown when the provided secret key is inappropriate for the hash algorithm
      */
     private String hash_hmac(final String secret, final String message) throws NoSuchAlgorithmException, InvalidKeyException {
         Mac mac = Mac.getInstance(this.algorithm.getAlgorithm());
         byte[] keyBytes = secret.getBytes();
         SecretKeySpec keySpec = new SecretKeySpec(keyBytes, mac.getAlgorithm());
         mac.init(keySpec);
         
         byte[] rawHmac = mac.doFinal(message.getBytes()); // the same as OpenSSL::HMAC.hexDigest
         return Hex.encodeHexString(rawHmac);
     }
 }
 
