 package com.github.sfleiter.cdi_interceptors.impl;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import javax.inject.Inject;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptor;
 import javax.interceptor.InvocationContext;
 
 import uk.org.lidalia.slf4jext.Level;
 import uk.org.lidalia.slf4jext.Logger;
 import uk.org.lidalia.slf4jext.LoggerFactory;
 
 import com.github.sfleiter.cdi_interceptors.api.Logging;
 import com.thoughtworks.paranamer.BytecodeReadingParanamer;
 import com.thoughtworks.paranamer.CachingParanamer;
 import com.thoughtworks.paranamer.Paranamer;
 
 /**
  * The Class LoggingInterceptor add a special kind of logging advice to classes / methods.
  * Calls with parameter names, results and exceptions are logged in a single line.
  * A {@link BytecodeReadingParanamer} is use to detect parameter names.
  * If paranamer does not deliver names, a default of arg0...n is used.
  */
 @Interceptor
 @Logging
 public class LoggingInterceptor {
 
     /** The paranamer used to get parameter names. */
     private Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
     
     @Inject
     private StringTransformer stringTransformer;
 
     /**
      * Logging method invoke.
      *
      * @param ctx the invocation context
      * @return the result object
      */
     @AroundInvoke
     public Object loggingMethodInvoke(InvocationContext ctx) throws Exception {
         Logger logger = LoggerFactory.getLogger(ctx.getTarget().getClass().getName());
         Logging annotation = getAnnotation(ctx.getMethod(), Logging.class);
         Level level = annotation.level();
         int maximumCount = annotation.maximumIterableLoggingCount();
         boolean measureDuration = annotation.measureDuration();
 
         StringBuilder sb;
         long start = currentTimeMillis(measureDuration);
         long duration;
         Object result;
         try {
             result = ctx.proceed();
             if (logger.isEnabled(level)) {
                 duration = currentTimeMillis(measureDuration) - start;
                 sb = getCallString(ctx, maximumCount);
                 sb.append(" returns ");
                 stringTransformer.transform(sb, result, maximumCount);
                 appendDuration(sb, measureDuration, duration);
                 logger.log(level, sb.toString());
             }
         } catch (Exception e) {
             if (e instanceof RuntimeException) {
                 level = annotation.exceptionLevel(); 
             }
             if (logger.isEnabled(level)) {
                 duration = currentTimeMillis(measureDuration) - start;
                 sb = getCallString(ctx, maximumCount);
                 sb.append(" caused ");
                 sb.append(e.getMessage());
                 appendDuration(sb, measureDuration, duration);
                 logger.log(level, sb.toString(), e);
             }
             throw e;
         }
         return result;
     }
 
     private long currentTimeMillis(boolean measureDuration) {
         return measureDuration ? System.currentTimeMillis() : 0;
     }
 
     private <T extends Annotation> T getAnnotation(Method m, Class<T> clz) {
         // first look at method-level annotations, since they take priority
         for (Annotation a : m.getAnnotations()) {
             if (clz.isInstance(a)) {
                 return clz.cast(a);
             }
         }
         for (Annotation a : m.getDeclaringClass().getAnnotations()) {
             if (clz.isInstance(a)) {
                 return clz.cast(a);
             }
         }
         throw new RuntimeException("@" + clz.getName() + " not found on method " + m.getName() + " or its class "
                 + m.getClass().getName());
     }
 
     private StringBuilder getCallString(InvocationContext ctx, int maximumCount) {
         String method = ctx.getMethod().getName();
         String[] parameterNames = paranamer.lookupParameterNames(ctx.getMethod(), false);
         StringBuilder sb = new StringBuilder();
         sb.append("call ");
         sb.append(method);
         sb.append("(");
         Object[] parameters = ctx.getParameters();
         for (int i = 0; i < parameters.length; i++) {
             // if paranamer has found a name, then use it
             if (parameterNames != null && parameterNames.length > i) {
                 sb.append(parameterNames[i]);
             } else {
                 // render a generic name
                 sb.append("arg");
                 sb.append(i);
             }
             sb.append("=");
             if (parameters[i] != null) {
                 stringTransformer.transform(sb, parameters[i], maximumCount);
             }
             if (i < parameterNames.length - 1)
                 sb.append(", ");
         }
         sb.append(")");
         return sb;
     }
 
     private void appendDuration(StringBuilder sb, boolean measureDuration, long duration) {
         if (measureDuration) {
             sb.append(" [duration=");
             sb.append(duration);
             sb.append("ms]");
         }
     }
 
 }
