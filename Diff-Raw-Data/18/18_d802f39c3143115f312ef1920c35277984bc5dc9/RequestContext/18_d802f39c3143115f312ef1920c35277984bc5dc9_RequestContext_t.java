 /* Copyright 2004, Aetrion LLC.  All Rights Reserved. */
 
 package com.aetrion.flickr;
 
 /**
  * A thread local variable used to hold contextual information used in requests.  To get an instance of this class use
  * RequestContext.getRequestContext().  The method will return a RequestContext object which is only usable within the
  * current thread.
  *
  * @author Anthony Eden
  */
 public class RequestContext {
 
     private static RequestContextThreadLocal threadLocal =
             new RequestContextThreadLocal();
 
     private Authentication authentication;
 
     /**
      * Get the RequestContext instance for the current Thread.
      *
      * @return The RequestContext
      */
     public static RequestContext getRequestContext() {
         return (RequestContext) threadLocal.get();
     }
 
    /**
     * Get the authentication object.
     *
     * @return The Authentication object
     */
     public Authentication getAuthentication() {
         return authentication;
     }
 
    /**
     * Set the authentication object.
     *
     * @param authentication The Authentication object
     */
     public void setAuthentication(Authentication authentication) {
         this.authentication = authentication;
     }
 
     private static class RequestContextThreadLocal extends ThreadLocal {
 
         protected Object initialValue() {
             return new RequestContext();
         }
     }
 
 }
