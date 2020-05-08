 package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server;
 
 import java.io.Serializable;
 import java.util.*;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.generators.SimpleUUIDCodeGenerator;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators.*;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.*;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.services.ClientAccessRequestService;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.services.ClientConfigurationStore;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.*;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.Nullable;
 
 
 public class DefaultAuthorizationServer implements Serializable {
 
     protected ClientConfigurationStore   clientConfigurationStore;
     protected ClientAccessRequestService clientAccessRequestService;
 
     protected List<Validator> requestValidators;
 
     protected TokenGenerator codeGenerator;
 
     static final Log LOG = LogFactory.getLog( DefaultAuthorizationServer.class );
 
     public DefaultAuthorizationServer(final ClientConfigurationStore clientConfigurationStore,
                                       final ClientAccessRequestService clientAccessRequestService) {
 
         this.clientConfigurationStore = clientConfigurationStore;
         this.clientAccessRequestService = clientAccessRequestService;
 
         requestValidators = new LinkedList<Validator>();
         requestValidators.add( new ClientValidator() );
         requestValidators.add( new RequiredFieldsValidator() );
         requestValidators.add( new RedirectionURIValidator() );
         requestValidators.add( new ScopeValidator() );
         requestValidators.add( new FlowValidator() );
         requestValidators.add( new CredentialsValidator() );
         requestValidators.add( new TokenRequestValidator() );
 
         this.codeGenerator = new SimpleUUIDCodeGenerator();
     }
 
     public DefaultAuthorizationServer() {
 
     }
 
     public List<Validator> getRequestValidators() {
 
         return requestValidators;
     }
 
     public void setRequestValidators(final List<Validator> requestValidators) {
 
         this.requestValidators = requestValidators;
     }
 
     public ClientAccessRequestService getClientAccessRequestService() {
 
         return clientAccessRequestService;
     }
 
     public void setClientAccessRequestService(final ClientAccessRequestService clientAccessRequestService) {
 
         this.clientAccessRequestService = clientAccessRequestService;
     }
 
     public ClientConfigurationStore getClientConfigurationStore() {
 
         return clientConfigurationStore;
     }
 
     public void setClientConfigurationStore(final ClientConfigurationStore clientConfigurationStore) {
 
         this.clientConfigurationStore = clientConfigurationStore;
     }
 
     /**
      * Init an authorization request flow. Performs validation on the request, and if valid, stores the request
      * and returns requests id.
      *
      * @return ClientAccess id
      */
     public String initFlow(AuthorizationRequest authRequest)
             throws OAuthException {
 
         LOG.debug( "init a authorization code or implicit flow" );
         ClientConfiguration client;
         if (authRequest.getClientId() == null)
             throw new OAuthInvalidMessageException( "Missing client_id" );
         try {
             client = clientConfigurationStore.getClient( authRequest.getClientId() );
         }
         catch (ClientNotFoundException e) {
             throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST, "Client application configuration not found", e );
         }
 
         // validations (check redirect URI, scope, flow type,...)
         for (Validator validator : requestValidators) {
             validator.validate( authRequest, client );
         }
 
         // store the client access request
         ClientConfiguration.FlowType requestFlowType = null;
         switch (authRequest.getResponseType()) {
             case CODE:
                 requestFlowType = ClientConfiguration.FlowType.AUTHORIZATION;
                 break;
             case TOKEN:
                 requestFlowType = ClientConfiguration.FlowType.IMPLICIT;
                 break;
         }
         String validatedURI = MessageUtils.stringEmpty( authRequest.getRedirectUri() )? null
                 : authRequest.getRedirectUri(); //don't store empty string as redirection uri
         return clientAccessRequestService.create( client, requestFlowType, authRequest.getScope(), authRequest.getState(), validatedURI );
     }
 
     /**
      * Sets the resource owner's identity for the client authorization request.
      */
     public void setUserIdentity(String clientAccessId, String userId)
             throws ClientAccessRequestNotFoundException {
 
         LOG.debug( "set user identity for flow instance " + clientAccessId );
         clientAccessRequestService.setUser( clientAccessId, userId );
     }
 
     /**
      * Sets the user's confirmation or rejection of the authorization request.
      *
      * @param clientAccessId the authorization request
      * @param authorized     is authorization granted
      * @param approvedScope  the approved scope
      * @param expireTime     expiration time of the authorization
      */
     public void setAuthorizationResult(String clientAccessId, boolean authorized, List<String> approvedScope, @Nullable Date expireTime)
            throws ClientAccessRequestNotFoundException, OAuthException {
 
         LOG.debug( "set user authorization result for flow instance " + clientAccessId );
         clientAccessRequestService.setAuthorizationResult( clientAccessId, authorized, approvedScope, expireTime );
 
         if (authorized) {
             ClientAccessRequest clientAccessRequest = clientAccessRequestService.getClientAccessRequest( clientAccessId );
             switch (clientAccessRequest.getFlowType()) {
                 case AUTHORIZATION:
                     if (clientAccessRequest.getAuthorizationCode() == null || MessageUtils.stringEmpty(
                             clientAccessRequest.getAuthorizationCode().getTokenData() )) {
                         clientAccessRequestService.setAuthorizationCode( clientAccessId, codeGenerator.createCode( clientAccessRequest ) );
                     } else {
                         throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "trying to set authorization code twice" );
                     }
                     break;
                 case IMPLICIT:
                     if (MessageUtils.collectionEmpty( clientAccessRequest.getAccessTokens() ))
                         clientAccessRequestService.addToken( clientAccessRequest.getId(),
                                 codeGenerator.createAccessToken( clientAccessRequest ) );
                     else
                         throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "more than one access token for implicit flow" );
                     break;
                 case RESOURCE_CREDENTIALS:
                 case CLIENT_CREDENTIALS:
                     throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR,
                             "Invalid server state: authorization endpoint cannot be used for resource credentials or client credentials flow" );
             }
         }
     }
 
     /**
      * Get the response message for the authorization request. Responses can be an authorization code grant, an access token
      * (depending on the original flow type) or an error message.
      */
     public ResponseMessage getAuthorizationResponseMessage(String clientAccessId)
             throws OAuthException {
 
         LOG.debug( "get authorization response message for flow instance " + clientAccessId );
         ClientAccessRequest clientAccessRequest = clientAccessRequestService.getClientAccessRequest( clientAccessId );
 
         if (!clientAccessRequest.isGranted()) {
             return new ErrorResponse( OAuth2Message.ErrorType.ACCESS_DENIED, "Resource owner refused authorization", null,
                     clientAccessRequest.getState() );
         }
 
         switch (clientAccessRequest.getFlowType()) {
             case AUTHORIZATION: {
                 AuthorizationCodeResponse response = new AuthorizationCodeResponse();
                 response.setCode( clientAccessRequest.getAuthorizationCode().getTokenData() );
                 response.setState( clientAccessRequest.getState() );
                 return response;
             }
             case IMPLICIT: {
                 AccessTokenResponse response = new AccessTokenResponse();
                 AccessToken accessToken = clientAccessRequest.getAccessTokens().get( clientAccessRequest.getAccessTokens().size() - 1 );
                 response.setAccessToken( accessToken.getTokenData() );
                 long expiresIn = (accessToken.getExpirationDate().getTime() - new Date().getTime()) / 1000;
                 response.setExpiresIn( expiresIn );
                 response.setTokenType( accessToken.getAccessTokenType() );
                 return response;
             }
             case RESOURCE_CREDENTIALS:
             case CLIENT_CREDENTIALS:
                 throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR,
                         "Invalid server state: authorization endpoint cannot be used for resource credentials or client credentials flow" );
         }
         return null;
     }
 
     public ErrorResponse getErrorMessage(Exception exception) {
 
         if (exception instanceof OAuthAuthorizationException) {
             return new CredentialsRequiredResponse( ((OAuthAuthorizationException) exception).getErrorType(), exception.getMessage(), null,
                     null );
         } else if (exception instanceof OAuthException) {
             return new ErrorResponse( ((OAuthException) exception).getErrorType(), exception.getMessage(), null, null );
         } else {
             return new ErrorResponse( OAuth2Message.ErrorType.SERVER_ERROR, "unknown error", null, null );
         }
     }
 
     /**
      * For the given token request, resume the oauth flow (in case the request message contains an authorization code
      * or refresh token), or create a new one in case the request message is part of a client credentials or
      * resource owner credentials flow. Returns the associated id.
      */
     public String initOrResumeFlow(AccessTokenRequest accessTokenRequest)
             throws OAuthException {
 
         LOG.debug( "resume or init flow instance" );
 
         // can't continue without knowing grant type
         if (accessTokenRequest.getGrantType() == null)
             throw new OAuthInvalidMessageException( "Missing fields in message" );
 
         // get the client config
         ClientConfiguration client;
         try {
             client = clientConfigurationStore.getClient( accessTokenRequest.getClientId() );
         }
         catch (ClientNotFoundException e) {
             throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST, "Client application configuration not found", e );
         }
 
         //find existing flow instance or create a new one, depening on requested flow type
         ClientAccessRequest clientAccessRequest = null;
         switch (accessTokenRequest.getGrantType()) {
             case AUTHORIZATION_CODE:
                 clientAccessRequest = clientAccessRequestService.findClientAccessRequestByToken(
                         new CodeToken( accessTokenRequest.getCode(), null ) );
                 break;
             case REFRESH_TOKEN:
                 clientAccessRequest = clientAccessRequestService.findClientAccessRequestByToken(
                         new RefreshToken( accessTokenRequest.getRefreshToken(), null ) );
                 break;
             case CLIENT_CREDENTIALS: {
                 String clientAccessId = clientAccessRequestService.create( client, ClientConfiguration.FlowType.CLIENT_CREDENTIALS,
                         accessTokenRequest.getScope(), null, null );
                 clientAccessRequest = clientAccessRequestService.findClientAccessRequest( clientAccessId );
                 break;
             }
             case PASSWORD: {
                 String clientAccessId = clientAccessRequestService.create( client, ClientConfiguration.FlowType.RESOURCE_CREDENTIALS,
                         accessTokenRequest.getScope(), null, null );
                 clientAccessRequest = clientAccessRequestService.findClientAccessRequest( clientAccessId );
                 break;
             }
         }
 
         // validations (check redirect URI, scope, flow type,...)
         for (Validator validator : requestValidators) {
             validator.validate( accessTokenRequest, clientAccessRequest, client );
         }
 
         // set access tokens, and invalidate tokens if needed
         switch (accessTokenRequest.getGrantType()) {
             case AUTHORIZATION_CODE:
                 try {
                     CodeToken code = clientAccessRequest.getAuthorizationCode();
                     code.setInvalid( true );
                     clientAccessRequestService.setAuthorizationCode( clientAccessRequest.getId(), code );
                     clientAccessRequestService.addToken( clientAccessRequest.getId(),
                             codeGenerator.createAccessToken( clientAccessRequest ) );
                     if (client.isIncludeRefreshToken())
                         clientAccessRequestService.addToken( clientAccessRequest.getId(),
                                 codeGenerator.createRefreshToken( clientAccessRequest ) );
                 }
                 catch (ClientAccessRequestNotFoundException e) {
                     LOG.error( e );
                     throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "internal server error" );
                 }
                 break;
             case REFRESH_TOKEN:
                 try {
                     for (RefreshToken refreshToken : clientAccessRequest.getRefreshTokens()) {
                         clientAccessRequestService.invalidateToken( clientAccessRequest.getId(), refreshToken );
                     }
                     for (AccessToken accessToken : clientAccessRequest.getAccessTokens()) {
                         clientAccessRequestService.invalidateToken( clientAccessRequest.getId(), accessToken );
                     }
 
                     clientAccessRequestService.addToken( clientAccessRequest.getId(),
                             codeGenerator.createAccessToken( clientAccessRequest ) );
                     if (client.isIncludeRefreshToken())
                         clientAccessRequestService.addToken( clientAccessRequest.getId(),
                                 codeGenerator.createRefreshToken( clientAccessRequest ) );
                 }
                 catch (ClientAccessRequestNotFoundException e) {
                     LOG.error( e );
                     throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "internal server error" );
                 }
 
                 break;
             case CLIENT_CREDENTIALS:
                 //credentials have been validated by validators
                 try {
                     for (AccessToken accessToken : clientAccessRequest.getAccessTokens()) {
                         clientAccessRequestService.invalidateToken( clientAccessRequest.getId(), accessToken );
                     }
 
                     clientAccessRequestService.addToken( clientAccessRequest.getId(),
                             codeGenerator.createAccessToken( clientAccessRequest ) );
                 }
                 catch (ClientAccessRequestNotFoundException e) {
                     LOG.error( e );
                     throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "internal server error" );
                 }
                 break;
             case PASSWORD:
                 throw new UnsupportedOperationException( "not yet implemented" ); //TODO (will also require a resource owner class)
         }
 
         return clientAccessRequest.getId();
     }
 
     /**
      * Builds the response message containing a valid access token (if present) for the specified flow instance.
      * ONLY call this method when all everything else is done.
      */
     public ResponseMessage getAccessToken(String clientAccessId)
             throws OAuthException {
 
         ClientAccessRequest clientAccessRequest = clientAccessRequestService.getClientAccessRequest( clientAccessId );
 
         if (!clientAccessRequest.isGranted()) {
             return new ErrorResponse( OAuth2Message.ErrorType.ACCESS_DENIED, "Resource owner refused authorization", null,
                     clientAccessRequest.getState() );
         }
 
         if (MessageUtils.collectionEmpty( clientAccessRequest.getAccessTokens() )) {
             LOG.error( "No access tokens found at this stage, but they should be here. Did you call this methods at the correct time?" );
             throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "no access token available" );
         }
 
         AccessTokenResponse response = new AccessTokenResponse();
         AccessToken accessToken = clientAccessRequest.getAccessTokens().get( clientAccessRequest.getAccessTokens().size() - 1 );
         RefreshToken refreshToken = MessageUtils.collectionEmpty( clientAccessRequest.getRefreshTokens() )? null
                 : clientAccessRequest.getRefreshTokens().get( clientAccessRequest.getRefreshTokens().size() - 1 );
         if (!accessToken.isInvalid() && accessToken.getExpirationDate().after( new Date() )) {
             response.setAccessToken(
                     clientAccessRequest.getAccessTokens().get( clientAccessRequest.getAccessTokens().size() - 1 ).getTokenData() );
             long expiresIn = clientAccessRequest.getAccessTokens()
                                                 .get( clientAccessRequest.getAccessTokens().size() - 1 )
                                                 .getExpirationDate()
                                                 .getTime() - new Date().getTime();
             expiresIn /= 1000; //need seconds
             response.setExpiresIn( expiresIn );
             response.setTokenType(
                     clientAccessRequest.getAccessTokens().get( clientAccessRequest.getAccessTokens().size() - 1 ).getAccessTokenType() );
         } else {
             LOG.error(
                     "No valid access tokens found at this stage, but they should be here. Did you call this methods at the correct time?" );
             throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR, "no access token available" );
         }
         if (null != refreshToken && !refreshToken.isInvalid() && refreshToken.getExpirationDate().after( new Date() ))
             response.setRefreshToken( refreshToken.getTokenData() );
 
         return response;
     }
 
     /**
      * Validate an access token. Returns the client access id if the message is valid, and the token
      * is still valid (i.e. not expired, revoked, ...)
      *
      * @return clientAccessId
      */
     public ResponseMessage validateAccessToken(ValidationRequest request)
             throws OAuthException {
 
         if (MessageUtils.stringEmpty( request.getAccessToken() )) {
             throw new OAuthInvalidMessageException( "Missing access token" );
         }
 
         ClientAccessRequest clientAccessRequest = clientAccessRequestService.findClientAccessRequestByToken(
                 new AccessToken( request.getAccessToken(), null ) );
 
         // validations (check redirect URI, scope, flow type,...)
         for (Validator validator : requestValidators) {
             validator.validate( request, clientAccessRequest, clientAccessRequest.getClient() );
         }
 
         Date expirationDate = null;
         for (AccessToken token : clientAccessRequest.getAccessTokens()) {
             if (request.getAccessToken().equals( token.getTokenData() )) {
                 expirationDate = token.getExpirationDate();
             }
         }
 
         ValidationResponse response = new ValidationResponse();
         response.setAudience( clientAccessRequest.getClient().getClientId() );
         long expiresIn = (expirationDate.getTime() - new Date().getTime()) / 1000;
         response.setExpiresIn( expiresIn );
         response.setScope( clientAccessRequest.getApprovedScope() );
         response.setUserId( clientAccessRequest.getUserId() );
 
         return response;
     }
 }
