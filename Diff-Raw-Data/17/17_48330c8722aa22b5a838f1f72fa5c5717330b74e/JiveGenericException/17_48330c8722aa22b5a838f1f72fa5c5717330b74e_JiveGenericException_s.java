/*
 * $Id$
 * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.jive.api;
 
 public class JiveGenericException extends RuntimeException
 {
     private static final long serialVersionUID = -1766006781130688521L;
     
     public JiveGenericException (Throwable error)
     {
         super(error);
     }
     
     public JiveGenericException (String message) 
     {
         super(message);
     }
     
     public JiveGenericException (String message, Throwable error)
     {
         super(message, error);
     }
 
     
 }
 
 
