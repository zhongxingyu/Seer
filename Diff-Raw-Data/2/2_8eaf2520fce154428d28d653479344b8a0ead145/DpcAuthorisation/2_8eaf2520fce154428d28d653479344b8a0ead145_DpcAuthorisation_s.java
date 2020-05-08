 package com.mmakowski.tutorial.literatespecs;
 
 /**
  * A plug-in into our authorisation system that authorises with DPC.
  */
 public class DpcAuthorisation {
     
     /**
      * @param userId the id of the user who attempts the access
      * @param documentId the id of the document user tries to access
      * @return {@code true} if the authorisation has been granted
      * @throws RuntimeException if any error is encountered during authorisation
      */
     public boolean request(String userId, String documentId) {
        throw new RuntimeException("TODO");
     }
 }
