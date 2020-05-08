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
		timer.startTimeTracking();
 	}
 
 	@Override
 	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
 		//timer.stopTimeTracking();
 	}
 
 	@Override
 	public Object invoke(MethodInvocation methodArg) throws Throwable {
		System.out.println("Check if the timer works.");
		methodArg.proceed();
		System.out.println("Congratulations. Hope you beat your earlier record");
		return null;
 	}
 	
 	public void afterThrowing(Throwable throwable) {
 		//timer.resetTimeTracking();
 	}
 }
