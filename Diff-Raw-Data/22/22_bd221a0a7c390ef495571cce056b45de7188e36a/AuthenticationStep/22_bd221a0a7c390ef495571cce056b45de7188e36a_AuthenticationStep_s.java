 /*
  * SafeOnline project.
  *
  * Copyright 2006-2008 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 package net.link.safeonline.auth.ws.soap;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * <h2>{@link AuthenticationStep}</h2>
 *
  * <p>
  * [description / usage].
  * </p>
 *
  * <p>
  * <i>Dec 18, 2008</i>
  * </p>
  *
  * @author wvdhaute
  */
 public enum AuthenticationStep {
 
     AUTHENTICATE( "Authenticate" ),
     INSUFFICIENT_DEVICE( "Insufficient device" ),
     GLOBAL_USAGE_AGREEMENT( "Global Usage Agreement" ),
     USAGE_AGREEMENT( "Usage Agreement" ),
     SUBSCRIPTION_DENIED( "Subscription denied" ),
     IDENTITY_CONFIRMATION( "Identity Confirmation" ),
    SUCCESS( "Success" );
 
     private final String value;
 
    private final static Map<String, AuthenticationStep> authenticationStepMap = new HashMap<String, AuthenticationStep>();
 
     static {
         AuthenticationStep[] authenticationSteps = AuthenticationStep.values();
         for (AuthenticationStep authenticationStep : authenticationSteps) {
             authenticationStepMap.put( authenticationStep.getValue(), authenticationStep );
         }
     }
 

    private AuthenticationStep(String value) {
 
         this.value = value;
     }
 
     public String getValue() {
 
         return value;
     }
 
     @Override
     public String toString() {
 
         return value;
     }
 
     public static AuthenticationStep getAuthenticationStep(String authenticationStepValue) {
 
         AuthenticationStep authenticationStep = authenticationStepMap.get( authenticationStepValue );
         if (null == authenticationStep)
             throw new IllegalArgumentException( "unknown authentication step: " + authenticationStepValue );
         return authenticationStep;
     }
 }
