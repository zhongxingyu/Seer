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
      alert("You must be logged in to perform this action.");
       //redirect to login page
       return;
     }
     else if(caught instanceof AuthorizationException) {
      alert("You do not have permission to perform this action.");
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
    $wnd.$('#modal-body-text').text(msg);
    $wnd.$('#error-ok').modal({
      keyboard: true,
      show: true
    });
   }-*/;
 }
