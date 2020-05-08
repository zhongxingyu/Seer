 package com.totalchange.discodj.web.client.error;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.totalchange.discodj.web.client.views.ErrorView;
 
 @Singleton
 public class ErrorHandler {
     private ErrorView errorView;
     private ErrorConstants errorConstants;
 
     @Inject
     public ErrorHandler(ErrorView errorView, ErrorConstants errorConstants) {
         this.errorView = errorView;
         this.errorConstants = errorConstants;
     }
 
     private String stackTraceToString(Throwable th) {
         StringBuilder str = new StringBuilder();
         String nl = "\n";
 
         if (th.getMessage() != null) {
             str.append(th.getMessage());
         }
 
         if (th.getStackTrace() != null) {
             for (StackTraceElement st : th.getStackTrace()) {
                 str.append(nl);
                 str.append(st.toString());
             }
         }
 
         if (th.getCause() != null) {
             str.append(nl + nl);
             str.append("Caused by: ");
             str.append(stackTraceToString(th.getCause()));
         }
 
         return str.toString();
     }
 
     public void argh(String msg) {
         errorView.setMessage(msg);
         errorView.setErrorMessage(null);
         errorView.setStackTrace(null);
         errorView.show();
     }
 
     public void argh(String msg, Throwable th) {
         errorView.setMessage(msg);
         errorView.setErrorMessage(th.getMessage());
         errorView.setStackTrace(stackTraceToString(th));
         errorView.show();
     }
 
     public void loadingError(Throwable th) {
         argh(errorConstants.loadingErrorMessage(), th);
     }
 }
