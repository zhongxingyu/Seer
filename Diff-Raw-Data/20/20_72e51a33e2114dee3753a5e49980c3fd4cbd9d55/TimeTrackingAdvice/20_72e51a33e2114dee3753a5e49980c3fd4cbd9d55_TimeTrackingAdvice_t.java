 package com.san.redis.aop;
 
 import java.lang.reflect.Method;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.springframework.aop.AfterReturningAdvice;
 import org.springframework.aop.MethodBeforeAdvice;
 import org.springframework.aop.ThrowsAdvice;
 
 public class TimeTrackingAdvice implements MethodBeforeAdvice, AfterReturningAdvice, ThrowsAdvice, MethodInterceptor {
 	private Timer timer;
 
 	public Timer getTimer() {
 		return timer;
 	}
 
 	public void setTimer(Timer timer) {
 		this.timer = timer;
 	}
 
 	@Override
 	public void before(Method method, Object[] args, Object target) throws Throwable {
		//timer.startTimeTracking();
 	}
 
 	@Override
 	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
 		//timer.stopTimeTracking();
 	}
 
 	@Override
 	public Object invoke(MethodInvocation methodArg) throws Throwable {
		long start = System.nanoTime();
		Object retVal = methodArg.proceed();
		long end = System.nanoTime();
		System.out.println("**It took " + (end-start) + " nanoseconds.");
		return retVal;
 	}
 	
 	public void afterThrowing(Throwable throwable) {
 		//timer.resetTimeTracking();
 	}
 }
