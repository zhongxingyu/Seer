 package com.higomo.media.imageresizer.web;
 
 import sun.misc.BASE64Decoder;
 
 import java.io.IOException;
 import java.net.URI;
 
 public class URIConverter {
     public static URI fromBase64Encoded(String encoded) throws InvalidURIException {
         BASE64Decoder decoder = new BASE64Decoder();
         String uriString;
 
         try {
             uriString = new String(decoder.decodeBuffer(encoded));
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
