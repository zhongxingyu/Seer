 package com.shopzilla.perf.aspect;
 
 /**
  * Created by IntelliJ IDEA.
  * User: spradhan
  * Date: 5/24/11
  * Time: 7:30 PM
  * To change this template use File | Settings | File Templates.
  */
 import com.shopzilla.perf.logger.Logger;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.Signature;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.Aspect;
 import org.springframework.util.Assert;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 
 @Aspect
 @Component
 public class TimingAspect {
 
     @Resource
 	private Logger logger;
 
     @Around(value = "@annotation(trace)", argNames = "pjp, trace")
    static Object timeMethod(final ProceedingJoinPoint pjp, final PerfTimed timed) throws Throwable {
        return time(pjp, timed);
     }
 
     //@Around(value = "within(@a.PerfTimed *) && @target(timed)", argNames = "pjp, timed")
     //static Object timeClass(final ProceedingJoinPoint pjp, final PerfTimed timed) throws Throwable {
     //    return time(pjp, timed);
     //}
 
    private static Object time(final ProceedingJoinPoint pjp, final PerfTimed timed) throws Throwable {
 
         Assert.notNull(pjp);
         Assert.notNull(timed);
 
         final Signature signature = pjp.getSignature();
         final String longString = "[" + signature.toLongString() + "]";
 
         System.out.println();
         System.out.println(">>>> started " + longString);
 
         final long startTimeMs = System.currentTimeMillis();
 
         try {
            return pjp.proceed();
         } finally {
 
             final long timeTakenMs = System.currentTimeMillis() - startTimeMs;
             System.out.println("<<<< completed " + longString + " (took " + timeTakenMs + "ms)");
             System.out.println();
 
         }
     }
 
 }
