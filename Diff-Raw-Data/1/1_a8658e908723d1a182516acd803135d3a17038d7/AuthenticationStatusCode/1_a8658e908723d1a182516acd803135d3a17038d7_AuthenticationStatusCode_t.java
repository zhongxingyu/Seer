 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.api.ws.auth;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Error codes used by the authentication protocols.
  *
  * @author wvdhaute
  */
 public enum AuthenticationStatusCode {
 
     ALREADY_AUTHENTICATED( "urn:net:lin-k:safe-online:ws:auth:status:AlreadyAuthenticated" ),
     ALREADY_SUBSCRIBED( "urn:net:lin-k:safe-online:ws:auth:status:AlreadySubscribed" ),
     APPLICATION_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:ApplicationNotFound" ),
     ATTRIBUTE_TYPE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:AttributeTypeNotFound" ),
     AUTHENTICATION_FAILED( "urn:net:lin-k:safe-online:ws:auth:status:AuthenticationFailed" ),
     DEVICE_DISABLED( "urn:net:lin-k:safe-online:ws:auth:status:DeviceDisabled" ),
     DEVICE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:DeviceNotFound" ),
     EMPTY_DEVICE_POLICY( "urn:net:lin-k:safe-online:ws:auth:status:EmptyDevicePolicy" ),
     IDENTITY_UNAVAILABLE( "urn:net:lin-k:safe-online:ws:auth:status:IdentityUnavilable" ),
     INSUFFICIENT_IDENTITY( "urn:net:lin-k:safe-online:ws:auth:status:InsufficientIdentity" ),
     INSUFFICIENT_CREDENTIALS( "urn:net:lin-k:safe-online:ws:auth:status:InsufficientCredentials" ),
     INSUFFICIENT_DEVICE( "urn:net:lin-k:safe-online:ws:auth:status:InsufficientDevice" ),
     REQUIRED_DEVICE_MISSING( "urn:net:lin-k:safe-online:ws:auth:status:RequiredDeviceMissing" ),
     PUSH_REGISTER_ANOTHER( "urn:net:lin-k:safe-online:ws:auth:status:PushRegisterAnother" ),
     INVALID_CREDENTIALS( "urn:net:lin-k:safe-online:ws:auth:status:InvalidCredentials" ),
     INTERNAL_ERROR( "urn:net:lin-k:safe-online:ws:auth:status:InternalError" ),
     NOT_AUTHENTICATED( "urn:net:lin-k:safe-online:ws:auth:status:NotAuthenticated" ),
     PERMISSION_DENIED( "urn:net:lin-k:safe-online:ws:auth:status:PermissionDenied" ),
     PKI_REVOKED( "urn:net:lin-k:safe-online:ws:auth:status:PkiRevoked" ),
     PKI_SUSPENDED( "urn:net:lin-k:safe-online:ws:auth:status:PkiSuspended" ),
     PKI_EXPIRED( "urn:net:lin-k:safe-online:ws:auth:status:PkiExpired" ),
     PKI_NOT_YET_VALID( "urn:net:lin-k:safe-online:ws:auth:status:PkiNotYetValid" ),
     PKI_INVALID( "urn:net:lin-k:safe-online:ws:auth:status:PkiInvalid" ),
     REQUEST_DENIED( "urn:net:lin-k:safe-online:ws:auth:status:RequestDenied" ),
     REQUEST_FAILED( "urn:net:lin-k:safe-online:ws:auth:status:RequestFailed" ),
     SESSION_EXPIRED( "urn:net:lin-k:safe-online:ws:auth:status:SessionExpired" ),
     SUBJECT_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:SubjectNotFound" ),
     SUBJECT_ALREADY_EXISTS( "urn:net:lin-k:safe-online:ws:auth:status:SubjectAlreadyExists" ),
     SUBSCRIPTION_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:SubscriptionNotFound" ),
     LANGUAGE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:LanguageNotFound" ),
     CANCELLED( "urn:net:lin-k:safe-online:ws:auth:status:Cancelled" ),
    FAILED( "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed" ),
     SUCCESS( "urn:oasis:names:tc:SAML:2.0:status:Success" );
 
     private final String urn;
 
     private static final Map<String, AuthenticationStatusCode> urnMap = new HashMap<String, AuthenticationStatusCode>();
 
     static {
         AuthenticationStatusCode[] statusCodes = AuthenticationStatusCode.values();
         for (AuthenticationStatusCode statusCode : statusCodes)
             urnMap.put( statusCode.getURN(), statusCode );
     }
 
     AuthenticationStatusCode(String urn) {
 
         this.urn = urn;
     }
 
     public String getURN() {
 
         return urn;
     }
 
     @Override
     public String toString() {
 
         return urn;
     }
 
     public static AuthenticationStatusCode ofURN(String urn) {
 
         AuthenticationStatusCode authenticationStatusCode = urnMap.get( urn );
         if (null == authenticationStatusCode)
             throw new IllegalArgumentException( "unknown ws authentication error code: " + urn );
         return authenticationStatusCode;
     }
 }
