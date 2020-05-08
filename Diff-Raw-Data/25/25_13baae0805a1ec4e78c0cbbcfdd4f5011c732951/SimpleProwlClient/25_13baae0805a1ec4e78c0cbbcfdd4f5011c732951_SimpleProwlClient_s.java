 package org.antbear.jprowl;
 
 import org.antbear.jprowl.model.ProwlResponse;
 import org.antbear.jprowl.model.ProwlSuccess;
 
 import javax.validation.constraints.NotNull;
 
 public class SimpleProwlClient implements ProwlClient {
 
     private final ProwlClient client;
 
    public SimpleProwlClient() {
        this.client = new RawProwlClient();
    }

     public SimpleProwlClient(final ProwlClient prowlClient) {
         this.client = prowlClient;
     }
 
     @Override
     public ProwlResponse verifyApiKey(final String apiKey) {
         final ProwlResponse response = client.verifyApiKey(apiKey);
         final ProwlSuccess success = response.getSuccess();
         if (null == success || 200 != success.getStatusCode()) {
             throw new ProwlException("API Key could not be verified", response);
         }
         return response;
     }
 
     @Override
     public ProwlResponse postNotification(@NotNull final ProwlNotification notification) {
         final ProwlResponse response = client.postNotification(notification);
         final ProwlSuccess success = response.getSuccess();
         if (null == success || 200 != success.getStatusCode()) {
             throw new ProwlException("Notification could not be posted", response);
         }
         return response;
     }
 }
