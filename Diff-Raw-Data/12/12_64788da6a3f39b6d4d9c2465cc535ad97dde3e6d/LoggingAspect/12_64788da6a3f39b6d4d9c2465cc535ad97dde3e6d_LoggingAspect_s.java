 package com.example.samplewebapp.aspect.logging;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 
 @Aspect
 public class LoggingAspect {
 
	@Before("execution(* com.hm.online.accountingmanager.domain.service.AccountingManagementServiceImpl.fetchTransactionTypeCount(..))")
 	public void logBefore(JoinPoint joinPoint) {
 
		System.out.println("******");
 		System.out.println("logBefore() is running!");
 		System.out.println("hijacked : " + joinPoint.getSignature().getName());
		System.out.println("******");
 	}
 }
