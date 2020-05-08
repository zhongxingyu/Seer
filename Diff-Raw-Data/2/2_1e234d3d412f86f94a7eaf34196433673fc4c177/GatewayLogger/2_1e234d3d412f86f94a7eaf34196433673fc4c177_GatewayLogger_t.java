 package org.motechproject.ananya.kilkari.web;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 @Aspect
 @Component
 public class GatewayLogger {
     private final Logger logger = LoggerFactory.getLogger("RequestResponseLogger");
 
     @Pointcut("execution(public * org.springframework.web.client.RestTemplate.*(..))")
     public void allExternalHttpCalls() {
     }
 
     @Before("allExternalHttpCalls()")
     public void beforeRESTCall(JoinPoint joinPoint) {
         if (logger.isDebugEnabled()) {
             Object[] args = joinPoint.getArgs();
             if (args.length > 0) {
                 logger.debug("Accessing external url: {}", getPrintableArgsList(args));
             }
         }
     }
 
     private List<Object> getPrintableArgsList(Object[] args) {
         List<Object> listOfArguments = new ArrayList<>();
         for (int i = 0, len = args.length; i < len; i++) {
             Object arg = args[i];
             String string = arg instanceof Object[] ? Arrays.toString((Object[]) arg) : arg.toString();
             listOfArguments.add(string);
         }
         return listOfArguments;
     }
 
     @AfterReturning(pointcut = "allExternalHttpCalls()", returning = "result")
     public void afterRESTCall(JoinPoint joinPoint, Object result) {
         if (logger.isDebugEnabled()) {
             Object[] args = joinPoint.getArgs();
             String arguments = "";
             if (args.length > 0) {
                arguments = getPrintableArgsList(args).toString();
             }
             logger.debug("After accessing external url: {} got response: {}", arguments, result);
         }
     }
 }
