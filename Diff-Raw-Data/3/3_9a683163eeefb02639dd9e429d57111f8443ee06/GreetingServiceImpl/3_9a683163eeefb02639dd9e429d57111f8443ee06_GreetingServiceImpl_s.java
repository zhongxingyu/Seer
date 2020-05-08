 package za.co.cmsoftware.random.server;
 
 import za.co.cmsoftware.random.client.GreetingService;
 import za.co.cmsoftware.random.shared.FieldVerifier;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class GreetingServiceImpl extends RemoteServiceServlet implements
         GreetingService {
 
     public String generateNumber(String maximum) throws IllegalArgumentException {
         // Verify that the input is valid.
         if (!FieldVerifier.isValidNumber(maximum)) {
             // If the input is not valid, throw an IllegalArgumentException back to
             // the client.
             throw new IllegalArgumentException("Please enter a valid number");
         }
 
        return String.valueOf(Math.round(Integer.parseInt(maximum) * Math.random()));
     }
 
 }
