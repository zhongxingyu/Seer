 /*
  * Copyright (c) 2007 Zauber S.A.  -- All rights reserved
  */
 package ar.com.zauber.commons.exception.interceptors;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * Interceptor de Exceptions
  * 
  * Es el interceptor encargado de manejar Exceptions. Tiene un handler que es
  * el encargado de tratar la excepciones.
  * 
  * @author Martin A. Marquez
  * @since Jul 24, 2007
  */
public class ExceptionInterceptor implements MethodInterceptor {
 
     /** logger */
     private static Log log = LogFactory.getLog(ExceptionInterceptor.class);
     
     /** el handler de excepciones **/
     private MethodInvocationExceptionHandler handler;
         
     /**
 	 * @param handler El/La handler a setear.
 	 */
 	public void setHandler(MethodInvocationExceptionHandler handler) {
 		this.handler = handler;
 	}
 
 	/**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(
     * org.aopalliance.intercept.MethodInvocation)
      */
     public Object invoke(MethodInvocation aMethod) throws Throwable {
        Object ret = null;
         
         try {
             ret = aMethod.proceed();
         } catch (Exception e) {
         	this.handler.handle(e, aMethod);
            throw e;
         }
         
         return ret;
     }
 	
 }
