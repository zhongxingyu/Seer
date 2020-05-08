 package com.redshape.servlet.actions.exceptions.handling;
 
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.core.IHttpResponse;
 import com.redshape.servlet.core.controllers.ProcessingException;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @package com.redshape.servlet.actions.exceptions.handling
  * @user cyril
  * @date 6/20/11 10:49 PM
  */
 public abstract class AbstractPageExceptionHandler implements IPageExceptionHandler {
     private Map<Class<? extends ProcessingException>, Method> interceptors
                     = new HashMap< Class<? extends ProcessingException>, Method>();
     private boolean initialized;
     protected static String HANDLER_NAME = "handleException";
 
     public AbstractPageExceptionHandler() {
         super();
 
         this.init();
     }
 
     protected void init() {
         if ( this.initialized ) {
             return;
         }
 
        for ( Method method : this.getClass().getMethods() ) {
             if ( this.isHandlerMethod(method) ) {
                 this.interceptors.put(
                         method.getParameterTypes()[0].asSubclass(ProcessingException.class),
                         method );
             }
         }
 
         this.initialized = true;
     }
 
     protected boolean isHandlerMethod( Method method ) {
         Class<?>[] params = method.getParameterTypes();
 
         return method.getName().equals( HANDLER_NAME )
                    && params.length == 3
                        && params[0].equals(ProcessingException.class)
                             && ProcessingException.class.isAssignableFrom( params[0] );
     }
 
     protected abstract void unknownExceptionHandler( ProcessingException e,
                                                      IHttpRequest request, IHttpResponse response );
 
     @Override
     public void handleException(ProcessingException e, IHttpRequest request, IHttpResponse response) {
        Method method = this.interceptors.get(e);
         if ( method == null ) {
             this.unknownExceptionHandler(e, request, response);
         }
 
         try {
             method.invoke( this, e, request, response );
         } catch ( InvocationTargetException ex ) {
             throw new IllegalStateException( e.getCause().getMessage(), e.getCause() );
         } catch ( Throwable ex ) {
             throw new IllegalStateException( e.getMessage(), e );
         }
     }
 }
