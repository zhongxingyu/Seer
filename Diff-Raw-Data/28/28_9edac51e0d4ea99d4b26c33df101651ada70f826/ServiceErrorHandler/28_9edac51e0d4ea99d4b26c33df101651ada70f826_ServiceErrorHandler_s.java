 package org.scriptbox.util.spring.aop;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.scriptbox.util.common.error.ExceptionHelper;
 
 public class ServiceErrorHandler implements MethodInterceptor {
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger( ServiceErrorHandler.class );
 	
 	@Override
 	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
 		try {
 			return methodInvocation.proceed();
 		} 
 		catch (Exception ex ) {
			LOGGER.error( "Error from service: " + methodInvocation.getMethod() + ", arguments: " + methodInvocation.getArguments(), ex );
			throw ExceptionHelper.makeSerializable( ex );
 		}
 	}
 }
