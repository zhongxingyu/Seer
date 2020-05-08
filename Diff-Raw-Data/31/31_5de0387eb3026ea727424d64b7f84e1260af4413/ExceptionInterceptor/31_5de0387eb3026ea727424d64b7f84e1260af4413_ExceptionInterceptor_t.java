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
 * Se le puede setear un valor de retorno en caso de error de invocacin al
 * metodo y en dicho caso no arrojara el Trowable nuevamente sin que
 * devovera dicho valor.
 * 
  * @author Martin A. Marquez
  * @since Jul 24, 2007
  */
public class ExceptionInterceptor<T> implements MethodInterceptor {
 
     /** logger */
     private static Log log = LogFactory.getLog(ExceptionInterceptor.class);
     
     /** el handler de excepciones **/
     private MethodInvocationExceptionHandler handler;
         
    private T exceptionReturnValue;
    
     /**
 	 * @param handler El/La handler a setear.
 	 */
 	public void setHandler(MethodInvocationExceptionHandler handler) {
 		this.handler = handler;
 	}
 
	
 	/**
	 * @param exceptionReturnValue El/La exceptionReturnValue a devolver.
	 */
	public void setExceptionReturnValue(T exceptionReturnValue) {
        this.exceptionReturnValue = exceptionReturnValue;
    }

    /**
     * @see MethodInterceptor#invoke(MethodInvocation)
      */
     public Object invoke(MethodInvocation aMethod) throws Throwable {
        Object ret = exceptionReturnValue;
         
         try {
             ret = aMethod.proceed();
         } catch (Exception e) {
         	this.handler.handle(e, aMethod);
            if(ret == null) {
                throw e;
            }
         }
         
         return ret;
     }
 	
 }
