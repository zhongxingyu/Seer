 package com.xerox.amazonws.fps;
 
 import java.io.Serializable;
 
 /**
  * @author J. Bernard
  * @author Elastic Grid, LLC.
  * @author jerome.bernard@elastic-grid.com
  */
 public class TemporaryDeclinePolicy implements Serializable {
     private final TemporaryDeclinePolicyType temporaryDeclinePolicyType;
     private final int implicitRetryTimeoutInMins;
 
     public TemporaryDeclinePolicy(TemporaryDeclinePolicyType temporaryDeclinePolicyType, int implicitRetryTimeoutInMins) {
         this.temporaryDeclinePolicyType = temporaryDeclinePolicyType;
         this.implicitRetryTimeoutInMins = implicitRetryTimeoutInMins;
     }
 
     public TemporaryDeclinePolicyType getTemporaryDeclinePolicyType() {
         return temporaryDeclinePolicyType;
     }
 
     public int getImplicitRetryTimeoutInMins() {
         return implicitRetryTimeoutInMins;
     }
 
    enum TemporaryDeclinePolicyType implements Serializable {
         EXPLICIT_RETRY("ExplicitRetry"),
         IMPLICIT_RETRY("ImplicitRetry"),
         FAILURE("Failure");
 
         private final String value;
 
         TemporaryDeclinePolicyType(String v) {
             value = v;
         }
 
         public String value() {
             return value;
         }
 
         public static TemporaryDeclinePolicyType fromValue(String v) {
             for (TemporaryDeclinePolicyType c : TemporaryDeclinePolicyType.values()) {
                 if (c.value.equals(v)) {
                     return c;
                 }
             }
             throw new IllegalArgumentException(v);
         }
     }
     
 }
