 package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.services;
 
 import java.util.Date;
 import java.util.List;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.*;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.ClientAccessRequestNotFoundException;
 import org.jetbrains.annotations.Nullable;
 
 
 /**
  * Interface to the authorization requests made by clients ({@code ClientAccess}). Implement this (for example using JPA) to provide a
  * store
  * for client authorization requests.
  * <p/>
  * <p/>
  * Date: 20/03/12
  * Time: 15:19
  *
  * @author sgdesmet
  */
 public interface ClientAccessRequestService {
 
     @Nullable
    String create(ClientConfiguration clientConfiguration, ClientConfiguration.FlowType flowType, List<String> requestedScope, String state,
                  String validatedRedirectionURI);
 
     /**
      * Get the request with the specified id
      */
     ClientAccessRequest findClientAccessRequest(String clientAccessId);
 
     /**
      * Find the request with the specified request token, access token or authorization code
      */
     ClientAccessRequest findClientAccessRequestByToken(Token token);
 
     /**
      * Get the request with the specified id
      */
     ClientAccessRequest getClientAccessRequest(String clientAccessId)
             throws ClientAccessRequestNotFoundException;
 
     /**
      * Sets the resource owner's idenity for the client authorization request.
      */
     void setUser(String clientAccessId, String userId)
             throws ClientAccessRequestNotFoundException;
 
     /**
      * Sets the resource owner's authorization (or lack thereof) for the given authorization request.
      */
     void setAuthorizationResult(String clientAccessId, boolean authorized, List<String> approvedScope, Date expireTime)
             throws ClientAccessRequestNotFoundException;
 
     /**
      * Update the authorization request with the generated authorization code (in case of the authorization grant flow)
      */
     void setAuthorizationCode(String clientAccessId, CodeToken authorizationCode)
             throws ClientAccessRequestNotFoundException;
 
     void addToken(String clientAccessId, Token token)
             throws ClientAccessRequestNotFoundException;
 
     void invalidateToken(String clientAccessId, Token token)
             throws ClientAccessRequestNotFoundException;
 }
