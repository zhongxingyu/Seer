 package org.omo.ltw;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.springframework.util.StopWatch;
 
 @Aspect
 public class ProfilingAspect {
 
     private static final Log LOG = LogFactory.getLog(ProfilingAspect.class);
 
 	@Around("methodsToBeProfiled()")
     public Object profile(ProceedingJoinPoint pjp) throws Throwable {
         StopWatch sw = new StopWatch(getClass().getSimpleName());
         try {
             sw.start(pjp.getSignature().getName());
             return pjp.proceed();
         } finally {
             sw.stop();
             LOG.info(sw.prettyPrint());
         }
     }
 
    @Pointcut("execution(public * org.omo.ltw..*.*(..))")
     public void methodsToBeProfiled(){}
 }
