 // Copyright 2010 Google Inc. All Rights Reserved.
 package com.google.jstestdriver.requesthandlers;
 
 /**
  * An enumeration of supported HTTP methods.
  *
  * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9
  *
 * @author rdionne@google.com (Robert Dionne)
 */
 public enum HttpMethod {
 
   /**
    * ANY is a wildcard that matches any other method.
    */
   ANY,
   DELETE,
   GET,
  HEAD,
   OPTIONS,
   POST,
   PUT,
   TRACE
 }
