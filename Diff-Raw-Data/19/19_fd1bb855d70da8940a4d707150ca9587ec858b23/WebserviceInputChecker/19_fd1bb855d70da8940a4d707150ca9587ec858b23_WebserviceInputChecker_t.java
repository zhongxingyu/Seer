 package org.bitrepository.webservice;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class WebserviceInputChecker {
 
     public static void checkFileIDParameter(String fileID) throws WebserviceInputCheckException {
         if(fileID == null || fileID.isEmpty()) {
             throw new WebserviceInputCheckException("Failure: missing fileID parameter."); 
         }
     }
     
     public static void checkURLParameter(String URL) throws WebserviceInputCheckException {
         if(URL == null || URL.isEmpty()) {
             throw new  WebserviceInputCheckException("Failure: missing url parameter.");
         }
         @SuppressWarnings("unused")
         URL url;
         try {
             url = new URL(URL);
         } catch (MalformedURLException e) {
             throw new  WebserviceInputCheckException("Failure: malformed url parameter.");
         }
     }
     
     public static void checkChecksumTypeParameter(String checksumType) throws WebserviceInputCheckException {
         if(checksumType == null || checksumType.isEmpty()) {
             throw new WebserviceInputCheckException("Failure: missing checksumType parameter."); 
         }
         // check for supported types?
     }
     
     public static void checkPillarIDParameter(String pillarID) throws WebserviceInputCheckException {
         if(pillarID == null || pillarID.isEmpty()) {
             throw new WebserviceInputCheckException("Failure: missing pillarID parameter."); 
         }
     }
    
     public static void checkChecksumParameter(String checksum) throws WebserviceInputCheckException {
         if(checksum != null && !checksum.isEmpty()) {
             if((checksum.length() % 2) != 0) {
                 throw new WebserviceInputCheckException("Failure: checksum parameter contains an uneven number of characters.");
             }
            if(!checksum.matches("^\\p{XDigit}*$")) {
                 throw new WebserviceInputCheckException("Failure: checksum parameter contains a non hexadecimal value.");
             }
         }    
     }
     
     public static void checkSaltParameter(String salt) throws WebserviceInputCheckException {
         if(salt != null && !salt.isEmpty()) {
             if((salt.length() % 2) != 0) {
                 throw new WebserviceInputCheckException("Failure: salt parameter contains an uneven number of characters.");
             }
            if(!salt.matches("^\\p{XDigit}*$")) {
                 throw new WebserviceInputCheckException("Failure: salt parameter contains a non hexadecimal value.");
             }
         }    
     }
 }
