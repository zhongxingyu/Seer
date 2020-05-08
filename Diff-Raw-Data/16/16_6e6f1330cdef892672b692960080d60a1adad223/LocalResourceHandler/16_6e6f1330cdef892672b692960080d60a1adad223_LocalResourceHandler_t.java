 package com.amee.base.resource;
 
 import com.amee.base.domain.VersionBeanFinder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import java.util.concurrent.*;
 
 /**
  * A ResourceHandler which proxies handling of the RequestWrapper to the Spring bean identified in the
  * target property. A target ResourceHandler will be found in the Spring context which matches the Version
  * supported for the current request. The target ResourceHandler will be invoked with a Future
  * with the timeout value (if this is greater than zero).
  */
 @Service
 @Scope("prototype")
 public class LocalResourceHandler implements ResourceHandler {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private static final ExecutorService executor = Executors.newCachedThreadPool();
 
     @Autowired
     private VersionBeanFinder versionBeanFinder;
 
     private String target = "";
 
     private int timeout = 0;
 
     public Object handle(RequestWrapper requestWrapper) {
         // Lookup target bean.
         Object target = versionBeanFinder.getBeanForVersion(getTarget(), requestWrapper.getVersion());
         if (target != null) {
             // Target bean found, send request there and get result object.
             // Only ResourceHandler derived implementations are supported.
             if (ResourceHandler.class.isAssignableFrom(target.getClass())) {
                 if (getTimeout() > 0) {
                     return handleWithTimeout(requestWrapper, (ResourceHandler) target);
                 } else {
                     return ((ResourceHandler) target).handle(requestWrapper);
                 }
             } else {
                 // Target bean type not supported.
                 log.warn("handle() Target bean type not supported: " + target.getClass());
                 throw new NotFoundException();
             }
         } else {
             // Target bean not found.
             log.warn("handle() Target bean not found:  " + requestWrapper.getTarget());
             throw new NotFoundException();
         }
     }
 
     /**
      *
      * @param requestWrapper
      * @param handler
      * @return
      */
     protected Object handleWithTimeout(final RequestWrapper requestWrapper, final ResourceHandler handler) {
         Object response = null;
         Callable<Object> task = new Callable<Object>() {
             public Object call() throws Exception {
                 return handler.handle(requestWrapper);
             }
         };
         log.debug("handleWithTimeout() Submitting the task.");
         Future<Object> future = executor.submit(task);
         try {
             response = future.get(getTimeout(), TimeUnit.SECONDS);
         } catch (TimeoutException e) {
             log.warn("handleWithTimeout() Caught TimeoutException (aborting).");
             throw new TimedOutException();
         } catch (InterruptedException e) {
             log.error("handleWithTimeout() Caught InterruptedException (aborting): " + e.getMessage(), e);
         } catch (ExecutionException e) {
             // We expect ResourceExceptions sometimes.
             if ((e.getCause() != null) && ResourceException.class.isAssignableFrom(e.getCause().getClass())) {
                 throw (ResourceException) e.getCause();
             } else {
                log.error("handleWithTimeout() Caught unexpected ExecutionException: " + e.getMessage(), e);
                 throw new RuntimeException("Caught ExecutionException whilst handling: " + e.getMessage(), e);
             }
         } finally {
             log.debug("handleWithTimeout() Canceling the task via its Future.");
             // TODO: One day we should switch this to true.
             // TODO: This can be true if we trust all tasks to be killed cleanly.
             future.cancel(false);
         }
         return response;
     }
 
     public String getTarget() {
         return target;
     }
 
     public void setTarget(String target) {
         if (target == null) {
             target = "";
         }
         this.target = target;
     }
 
     public int getTimeout() {
         return timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 }
