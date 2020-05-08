 package data.contents;
 
 import data.Content;
 
 import java.security.PublicKey;
 
 /**
  * @author Gary Ye
  * @version 12/1/13
  */
 public class PublicKeyResponse extends Content {
     private String requestId;
    private PublicKey publicKey;
 
     /**
      * Constructs a public key response, which will be sent back
      * from the server as a response of the PublicKeyRequest
      * @param requestId the id of the requested user
      * @param publicKey the public key of the requested user
      */
     public PublicKeyResponse(String requestId, PublicKey publicKey) {
         this.type = Type.PUBLIC_KEY_RESPONSE;
         this.requestId = requestId;
        this.publicKey = publicKey;
     }
 
     /**
      * @return the string
      */
     @Override
     public String toString() {
         return "Public key of " + requestId + " is inside";
     }
 
     /**
      * Returns the request id
      * @return the request id
      */
     public String getRequestId() {
         return requestId;
     }
 
     /**
      * Return the public key
      * @return the public key
      */
     public PublicKey getPublicKey() {
        return publicKey;
     }
 
     /**
      * Return if the user exists
      * @return whether the user exists or not
      */
    boolean userNotExisting(){
         return publicKey == null;
     }
 }
