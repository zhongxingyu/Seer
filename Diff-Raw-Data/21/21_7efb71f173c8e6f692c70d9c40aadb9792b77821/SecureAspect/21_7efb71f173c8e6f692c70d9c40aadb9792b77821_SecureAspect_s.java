 package no.sag.activitymanager.service;
 
 import java.lang.reflect.Method;
 
 import org.apache.log4j.Logger;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.reflect.MethodSignature;
 
 @Aspect
 public class SecureAspect {
 
 	private final static Logger log = Logger.getLogger(SecureAspect.class);
 
 	@Around("@annotation( Secure )")
	public void doSomething(final ProceedingJoinPoint pjp) throws Throwable {
 		
 
 		final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
 		final Method targetMethod = methodSignature.getMethod();
 		//final Secure secure = targetMethod.getAnnotation(Secure.class);
 		final Role role = targetMethod.getAnnotation(Secure.class).role();
 		
 		
 		log.info("\n\n\nROLLE: " + role);
 
		pjp.proceed();
 		
 	}
 
 }
