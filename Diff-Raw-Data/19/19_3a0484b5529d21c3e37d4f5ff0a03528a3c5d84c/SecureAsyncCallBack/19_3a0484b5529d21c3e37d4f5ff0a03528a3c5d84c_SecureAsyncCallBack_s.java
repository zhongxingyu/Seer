 package com.murex.ccportal.client;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.murex.ccportal.client.exceptions.AuthenticationException;
 import com.murex.ccportal.client.exceptions.AuthorizationException;
 
 /**
  * @author hsuleiman
  *         Date: 12/13/11
  *         Time: 4:18 PM
  */
 public abstract class SecureAsyncCallBack<T> implements AsyncCallback<T> {
   public void onFailure(Throwable caught) {
     if(caught instanceof AuthenticationException) {
      alert("Auth exception");
       //redirect to login page
       return;
     }
     else if(caught instanceof AuthorizationException) {
      alert("Auth exception");
       return;
     }
     try {
       //allow your application to handle the exception
       handleFailure(caught);
     }
     catch(Throwable e) {
       alert("Exception " + e.getMessage());
     }
   }
 
   public void handleFailure(Throwable t) throws Throwable {
     throw t;
   }
 
   public static native void alert(String msg) /*-{
    console.info("msg is " + $wnd.jQuery('modal-from-dom').modal());
    $wnd.jQuery('modal-from-dom').modal(true);
   }-*/;
 }
