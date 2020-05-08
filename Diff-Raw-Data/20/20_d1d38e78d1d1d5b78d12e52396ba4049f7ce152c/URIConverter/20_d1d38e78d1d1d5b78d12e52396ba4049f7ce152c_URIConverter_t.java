 package com.higomo.media.imageresizer.web;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import sun.misc.BASE64Decoder;
 
 import java.io.IOException;
 import java.net.URI;
 
 public class URIConverter {
    private final static Logger log = LoggerFactory.getLogger(URIConverter.class);

     public static URI fromBase64Encoded(String encoded) throws InvalidURIException {
         BASE64Decoder decoder = new BASE64Decoder();
         String uriString;
 
         try {
            log.debug("Decoding Base64 encoded URI: {}", encoded);

             uriString = new String(decoder.decodeBuffer(encoded));

            log.debug("Decoded URI: {}", uriString);
         } catch (IOException e) {
             throw new InvalidURIException("Unable to decode Base64 encoded URI");
         }
 
         try {
             return URI.create(uriString);
         } catch (IllegalArgumentException e) {
             throw new InvalidURIException(String.format("Invalid URI '%s'", uriString));
         }
     }
 }
