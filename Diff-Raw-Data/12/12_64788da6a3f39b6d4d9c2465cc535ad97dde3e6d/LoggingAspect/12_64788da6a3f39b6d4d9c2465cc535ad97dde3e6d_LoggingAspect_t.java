 package com.example.samplewebapp.aspect.logging;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
 
 @Aspect
@Component
 public class LoggingAspect {
 
	@Before("execution(* fetch*())")
 	public void logBefore(JoinPoint joinPoint) {
 
		System.out.println("****************************************************************************************");
 		System.out.println("logBefore() is running!");
 		System.out.println("hijacked : " + joinPoint.getSignature().getName());
		System.out.println("****************************************************************************************");
 	}
 }
