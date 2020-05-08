 /* Copyright (c) 2013, HotDocs Limited
    Use, modification and redistribution of this source is subject
    to the New BSD License as set out in LICENSE.TXT. */
 
 package com.hotdocs.cloud;
 
 /**
  * A catch-all exception for the extremely unlikely
 * errors that could during HMAC calculation.
  */
 @SuppressWarnings("serial")
 public class HmacException extends Exception {
     public HmacException(String message) {
         super(message);
     }
 }
