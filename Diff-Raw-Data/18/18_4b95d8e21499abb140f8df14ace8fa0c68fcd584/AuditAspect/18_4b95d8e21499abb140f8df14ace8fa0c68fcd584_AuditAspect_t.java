 package se.su.it.svc.server.aspect;
 
 import org.apache.cxf.phase.PhaseInterceptorChain;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.AfterThrowing;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.slf4j.LoggerFactory;
 import se.su.it.svc.server.annotations.AuditHideReturnValue;
 import se.su.it.svc.server.annotations.AuditMethodDetails;
 import se.su.it.svc.server.audit.AuditEntity;
 
 import javax.servlet.http.HttpServletRequest;
 import java.lang.reflect.Method;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Aspect
 public class AuditAspect {
   private static final String STATE_INPROGRESS = "IN PROGRESS";
   private static final String STATE_SUCCESS = "SUCCESS";
   private static final String STATE_EXCEPTION = "EXCEPTION";
   private static final String UNKNOWN = "<unknown>";
   private static final String HIDDEN_VALUE = "******";
 
   private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AuditAspect.class);
 
   @Before("execution(* (@javax.jws.WebService *).*(..))")
   public void auditBefore(JoinPoint joinPoint) throws Throwable {
     String id = getId();
     Class targetClass = joinPoint.getTarget().getClass();
     String methodName = joinPoint.getSignature().getName();
     Object[] args = joinPoint.getArgs();
 
     LOG.info("[" + id + "] Before: " + targetClass.getName() + "." + methodName + " with " + args.length + "params");
 
     Method method = null;
     try {
        method = getMethod(targetClass, methodName, args);
     } catch (NoSuchMethodException e) {
       LOG.warn("[" + id + "] Could not get method for " + targetClass.getName() + "." + methodName);
     }
 
     // Create an AuditEntity based on the gathered information
     AuditEntity ae = AuditEntity.getInstance(
             new Timestamp(new Date().getTime()).toString(),
             methodName,
             objectsToString(args),
             UNKNOWN,
             STATE_INPROGRESS,
             getMethodDetails(method)
     );
 
     LOG.info("[" + id + "] Received: " + ae);
   }
 
   @AfterReturning(
           pointcut = "execution(* (@javax.jws.WebService *).*(..))",
           returning = "result")
   public void auditAfterReturning(JoinPoint joinPoint, Object result) throws Throwable {
     String id = getId();
     Class targetClass = joinPoint.getTarget().getClass();
     String methodName = joinPoint.getSignature().getName();
     Object[] args = joinPoint.getArgs();
 
     LOG.info("[" + id + "] After: " + targetClass.getName() + "." + methodName + " with " + args.length + "params");
 
     Method method = null;
     try {
       method = getMethod(targetClass, methodName, args);
     } catch (NoSuchMethodException e) {
       LOG.warn("[" + id + "] Could not get method for " + targetClass.getName() + "." + methodName);
     }
 
     Object printedResult = result;
     if (method != null && method.isAnnotationPresent(AuditHideReturnValue.class)) {
       printedResult = HIDDEN_VALUE;
     }
 
     AuditEntity ae = AuditEntity.getInstance(
             new Timestamp(new Date().getTime()).toString(),
             methodName,
             objectsToString(args),
             printedResult != null ? printedResult.toString() : null,
             STATE_SUCCESS,
             getMethodDetails(method)
     );
 
     LOG.info("[" + id + "] Returned: " + ae);
   }
 
   @AfterThrowing(
           pointcut = "execution(* (@javax.jws.WebService *).*(..))",
           throwing = "throwable")
   public void auditAfterThrowing(JoinPoint joinPoint, Throwable throwable) throws Throwable {
     String id = getId();
     Class targetClass = joinPoint.getTarget().getClass();
     String methodName = joinPoint.getSignature().getName();
     Object[] args = joinPoint.getArgs();
 
    LOG.info("[" + id + "] After exception: " + targetClass.getName() + "." + methodName + " with " + args.length + " params");
 
     Method method = null;
     try {
       method = getMethod(targetClass, methodName, args);
     } catch (NoSuchMethodException e) {
       LOG.warn("[" + id + "] Could not get method for " + targetClass.getName() + "." + methodName);
     }
 
     AuditEntity ae = AuditEntity.getInstance(
             new Timestamp(new Date().getTime()).toString(),
             methodName,
             objectsToString(args),
             throwable != null ? throwable.toString() : null,
             STATE_EXCEPTION,
             getMethodDetails(method)
     );
 
     LOG.info("[" + id + "] Exception: " + ae);
   }
 
   protected String objectsToString(Object[] objects) {
     if (objects == null) {
       return "null";
     }
 
     StringBuilder sb = new StringBuilder();
 
     sb.append("[");
     for (Object object : objects) {
       sb.append(object).append(", ");
     }
     if (sb.lastIndexOf(",") > 0) {
       sb.replace(sb.lastIndexOf(","), sb.length(), "");
     }
     sb.append("]");
 
     return sb.toString();
   }
 
   protected String getId() {
     String id = "";
 
     try {
       HttpServletRequest request = (HttpServletRequest) PhaseInterceptorChain.getCurrentMessage().get("HTTP.REQUEST");
       id = request.getSession().getId();
     } catch (Exception ex) {
       LOG.debug("Failed to get id from session", ex);
     }
 
     return id;
   }
 
   private Method getMethod(Class target, String name, Object[] args) throws NoSuchMethodException {
     Class[] parameterTypes = new Class[args.length];
     for(int i = 0; i < parameterTypes.length; i++) {
       parameterTypes[i] = args[i].getClass();
     }
 
     return target.getMethod(name, parameterTypes);
   }
 
   /**
    * Generate MethodDetails from annotation on method to be able to describe
    * functions that will be invoked by this method
    *
    * @return the method details
    */
   private List<String> getMethodDetails(Method method) {
     List<String> methodDetails = new ArrayList<String>();
 
     if (method != null) {
       if (method.isAnnotationPresent(AuditMethodDetails.class)) {
         AuditMethodDetails annotation = method.getAnnotation(AuditMethodDetails.class);
         String details = annotation.details();
 
         for(String s : details.split(",")) {
           methodDetails.add(s.trim());
         }
       }
     }
 
     return methodDetails;
   }
 }
