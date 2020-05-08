 package com.googlecode.qualitas.engines.api.invocation.webservice;
 
 import java.net.SocketTimeoutException;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axis2.client.async.AxisCallback;
 import org.apache.axis2.context.MessageContext;
 
 /**
  * The Class AsynchronousAxis2Callback.
  */
 public class AsynchronousAxis2Callback implements AxisCallback {
 
     /** The completed. */
     private boolean completed;
 
     /** The error. */
     private boolean error;
 
     /** The timeout. */
     private boolean timeout;
 
     /** The result. */
     private OMElement result;
 
     /** The error message. */
     private String errorMessage;
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.axis2.client.async.AxisCallback#onComplete()
      */
     @Override
     public void onComplete() {
         doNotify();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.axis2.client.async.AxisCallback#onError(java.lang.Exception)
      */
     @Override
     public void onError(Exception ex) {
         error = true;
         if (ex.getCause() instanceof SocketTimeoutException) {
             timeout = true;
         }
         errorMessage = ex.toString();
         doNotify();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.axis2.client.async.AxisCallback#onFault(org.apache.axis2.context
      * .MessageContext)
      */
     @Override
     public void onFault(MessageContext ctx) {
         onError(ctx.getFailureReason());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.axis2.client.async.AxisCallback#onMessage(org.apache.axis2
      * .context.MessageContext)
      */
     @Override
     public void onMessage(MessageContext ctx) {
         result = ctx.getEnvelope().getFirstElement().getFirstElement();
         completed = true;
         doNotify();
     }
 
     /**
      * Do notify.
      */
     private void doNotify() {
         synchronized (this) {
            this.notify();
         }
     }
 
     /**
      * Checks if is completed.
      * 
      * @return true, if checks if is completed
      */
     public boolean isCompleted() {
         return completed;
     }
 
     /**
      * Checks if is timeout.
      * 
      * @return true, if checks if is timeout
      */
     public boolean isTimeout() {
         return timeout;
     }
 
     /**
      * Checks if is error.
      * 
      * @return true, if checks if is error
      */
     public boolean isError() {
         return error;
     }
 
     /**
      * Gets the result.
      * 
      * @return the result
      */
     public OMElement getResult() {
         return result;
     }
 
     /**
      * Gets the error message.
      * 
      * @return the error message
      */
     public String getErrorMessage() {
         return errorMessage;
     }
 
 }
