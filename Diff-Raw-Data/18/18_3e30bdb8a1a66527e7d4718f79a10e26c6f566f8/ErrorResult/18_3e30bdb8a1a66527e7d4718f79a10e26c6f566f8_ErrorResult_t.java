 package org.kari.call.event;
 
 import org.kari.call.CallType;
 
 /**
  * Error from server side
  *
  * @author kari
  */
 public final class ErrorResult extends BufferResult {
     /**
      * For error reading
      */
     public ErrorResult() {
         super();
     }
     
     public ErrorResult(Throwable pError, int pStackSkip) {
         super(pError);
 
         // skip until "proxy" method; seeing proxy method name in stack trace
         // is usefull
        // => Ignore stack skip; it causes extra gc() in server side
        setupServerStack(pError, 0);
     }
     
     @Override
     public CallType getType() {
         return CallType.ERROR;
     }
 
     @Override
     public Object getResult() 
         throws Throwable
     {
         Throwable error = getError();
         
         // skip until "proxy"; seeing proxy method name in stack trace is useful
         setupClientStack(error, 4);
         
         throw error;
     }
 
     /**
      * @return Reference to error, without throwing it
      */
     public Throwable getError() {
         Throwable error = (Throwable)mResult;
         return error;
     }
     
     /**
      * Append client side stack into exception thrown from server. Skip 
      * internals of framework, since application logic shouldn't care about them.
      */
     public static void setupClientStack(Throwable pError, int pStackSkip) {
         
         final StackTraceElement[] server = pError.getStackTrace();
         final StackTraceElement[] client = new Throwable().getStackTrace();
         final StackTraceElement[] stack = new StackTraceElement[server.length + client.length - pStackSkip + 1];
 
         System.arraycopy(server, 0, stack, 0, server.length);
         // divider to allow easily see client vs. server cutoff point
         stack[server.length] = new StackTraceElement("--------------------", "", null, -1);
         System.arraycopy(client, pStackSkip, stack, server.length + 1, client.length - pStackSkip);
         
         pError.setStackTrace(stack);
     }
 
     /**
      * In server side, strip away framework internals from stack trace, since
      * application logic shouldn't care about them
      */
     public static void setupServerStack(Throwable pError, int pStackSkip) {
        if (pStackSkip > 0) {
            final StackTraceElement[] server = pError.getStackTrace();
            final StackTraceElement[] stack = new StackTraceElement[server.length - pStackSkip];
    
            System.arraycopy(server, 0, stack, 0, server.length - pStackSkip);
            
            pError.setStackTrace(stack);
        }
     }
 
     @Override
     public void traceDebug() {
         LOG.debug("Failed to send error back to client", (Throwable)mResult);
     }    
 }
