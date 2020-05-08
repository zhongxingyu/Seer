 package com.wixpress.utils.sqlMonitor;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
 import org.springframework.core.ParameterNameDiscoverer;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
 * @author yoav
 * @since 3/1/11
 */
 abstract class LoggingInvocationHandler implements InvocationHandler {
 
     protected Object actualTarget;
     private static ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
     private static final Map<Method, String> logTemplateMethods = new ConcurrentHashMap<Method, String>();
     protected static Logger log = LoggerFactory.getLogger(SqlMonitorDriver.class);
 
     public LoggingInvocationHandler(Object actualTarget) {
         this.actualTarget = actualTarget;
     }
 
     protected Pair<Object, Long> invokeLogAndReturnTime(Object proxy, Method method, Object[] args) throws Throwable {
         return invokeLogAndReturnTime(proxy, method, args, Level.DEBUG);
     }
 
     /**
      * invokes the target method, logging the method execution at debug level and counting the method time
      * @param proxy - the proxy object
      * @param method - the method to call
      * @param args - the arguments to the method
      * @param level - log level
      * @return - the result of the method
      * @throws Throwable - any exception thrown from the method
      */
     protected Pair<Object, Long> invokeLogAndReturnTime(Object proxy, Method method, Object[] args, Level level) throws Throwable {
         long start = System.nanoTime();
         try {
             Object actualResult = method.invoke(actualTarget, args);
             long time = System.nanoTime() - start;
             SqlMonitorDriver.addDBTime(time);
             writeSuccessLogs(method, args, time, actualResult, level);
             return new Pair<Object, Long>(actualResult, time);
         }
         catch (Throwable e)
         {
             long time = System.nanoTime() - start;
             SqlMonitorDriver.addDBTime(time);
             writeExceptionLogs(method, args, time, e, level);
             if (e instanceof InvocationTargetException)
                 throw ((InvocationTargetException) e).getTargetException();
             else
                 throw e;
         }
     }
 
     protected Object invokeAndLog(Object proxy, Method method, Object[] args) throws Throwable {
         return invokeAndLog(proxy, method, args, Level.DEBUG);
     }
     /**
      * invokes the target method and logs the method execution at debug level
      * @param proxy - the proxy object
      * @param method - the method to call
      * @param args - the arguments to the method
      * @param level - log level
      * @return - the result of the method
      * @throws Throwable - any exception thrown from the method
      */
     protected Object invokeAndLog(Object proxy, Method method, Object[] args, Level level) throws Throwable {
         long start = System.nanoTime();
         try {
             Object actualResult = method.invoke(actualTarget, args);
             long time = System.nanoTime() - start;
             SqlMonitorDriver.addDBTime(time);
             writeSuccessLogs(method, args, time, actualResult, level);
             return actualResult;
         }
         catch (Throwable e)
         {
             long time = System.nanoTime() - start;
             SqlMonitorDriver.addDBTime(time);
             writeExceptionLogs(method, args, time, e, level);
             if (e instanceof InvocationTargetException)
                 throw ((InvocationTargetException) e).getTargetException();
             else
                 throw e;
         }
     }
 
     protected boolean isThisMethod(Method method, String name, Class<?> returnType, Class<?> ... argsTypes) {
        if (!(name.equals(method.getName()) && returnType.equals(method.getReturnType()) && method.getParameterTypes().length == argsTypes.length))
             return false;
         else {
             for (int i=0; i < argsTypes.length; i++) {
                 if (!method.getParameterTypes()[i].equals(argsTypes[i]))
                     return false;
             }
             return true;
         }
     }
 
     private void writeExceptionLogs(Method method, Object[] args, long time, Throwable e, Level level) {
         String template = getLogTemplate(method);
         Object[] allArgs = concatArray(args, time, e);
         doWriteLog(template, allArgs, level);
     }
 
     private void writeSuccessLogs(Method method, Object[] args, long time, Object actualResult, Level level) {
         String template = getLogTemplate(method);
         Object[] allArgs = concatArray(args, time, actualResult);
         doWriteLog(template, allArgs, level);
     }
 
     private void doWriteLog(String template, Object[] allArgs, Level level) {
         if (level == Level.DEBUG)
             log.debug(template, allArgs);
         else if (level == Level.INFO)
             log.info(template, allArgs);
         else if (level == Level.TRACE)
             log.trace(template, allArgs);
         else if (level == Level.WARN)
             log.warn(template, allArgs);
         else if (level == Level.ERROR)
             log.error(template, allArgs);
     }
 
     private Object[] concatArray(Object[] args, long time, Object actualResult) {
         Object[] allArgs;
         if (args == null) {
             allArgs = new Object[2];
             allArgs[0] = time;
             allArgs[1] = actualResult;
         }
         else {
             allArgs = new Object[args.length+2];
             System.arraycopy(args, 0, allArgs, 0, args.length);
             allArgs[args.length] = time;
             allArgs[args.length+1] = actualResult;
         }
         return allArgs;
     }
 
     private String createTemplate(Method method)
     {
         Class[] paramTypes = method.getParameterTypes();
         String[] paramNames = getParameterNames(method);
         if (paramNames == null) {
             paramNames = new String[paramTypes.length];
             for (int i=0; i < paramTypes.length; i++)
                 paramNames[i] = "arg_"+i;
         }
         StringBuilder sb = new StringBuilder();
         sb.append("method=[").append(actualTarget.getClass().getName()).append(".").append(method.getName()).append("]");
         for (int j = 0; j < paramTypes.length; j++)
         {
             sb.append(" ").append(paramNames[j]).append("=[{}]");
         }
         sb.append(" time=[{}] result=[{}]");
         return sb.toString();
     }
 
     private String[] getParameterNames(Method method) {
         return parameterNameDiscoverer.getParameterNames(method);
     }
 
     String getLogTemplate(Method method)
     {
         if (!logTemplateMethods.containsKey(method))
         {
             synchronized (logTemplateMethods)
             {
                 if (!logTemplateMethods.containsKey(method))
                 {
                     String template = createTemplate(method);
                     logTemplateMethods.put(method, template);
                     return template;
                 }
 
             }
         }
         return logTemplateMethods.get(method);
     }
 
 }
