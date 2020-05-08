 /**
  *
  * (c) 2012 MuleSoft, Inc. This software is protected under international copyright
  * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
  * (or other master license agreement) separately entered into in writing between you and
  * MuleSoft. If such an agreement is not in place, you may not use the software.
  */
 package org.mule.debugger.response;
 
 import org.mule.debugger.client.IDebuggerResponseCallback;
 import org.mule.debugger.exception.RemoteDebugException;
 
 public class ExceptionResponse extends DebuggerResponse {
 
     private RemoteDebugException exception;
 
 
     public ExceptionResponse(RemoteDebugException exception) {
         super();
         this.exception = exception;
     }
 
     public RemoteDebugException getException() {
         return exception;
     }
 
 


     public void callCallback(IDebuggerResponseCallback callback) {
         callback.onException(getException());
     }
 }
