 package org.omo.ltw;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 
 @Aspect
 public class RelationalAspect {
 	
 	public static Map<String, Map<Integer, Integer>> model = new HashMap<String, Map<Integer,Integer>>();
 	
 	protected Map<Integer, Integer> ensureTable(String fieldName) {
 		Map<Integer, Integer> table = model.get(fieldName);
 		if (table==null) {
 			model.put(fieldName, table = new HashMap<Integer, Integer>());
 		}
 		return table;
 	}
 	
     private static final Log LOG = LogFactory.getLog(RelationalAspect.class);
 
 	@Around("getters()")
     public Object adviseGetter(ProceedingJoinPoint pjp) throws Throwable {
 		String fieldName = fieldName(pjp);
 		Map<Integer, Integer> table = ensureTable(fieldName);
 		Identifiable target = (Identifiable)pjp.getTarget();
         try {
         	Identifiable value = (Identifiable)pjp.proceed();
             LOG.info("get: " + target + "." + fieldName + " = " + value);
             return value;
         } finally {
         }
     }
 
 	private String fieldName(ProceedingJoinPoint pjp) {
 		return pjp.getSignature().getName().substring(3);
 	}
 
 	@Around("setters()")
     public Object adviseSetter(ProceedingJoinPoint pjp) throws Throwable {
 		Identifiable target = (Identifiable)pjp.getTarget();
 		Identifiable value = (Identifiable)pjp.getArgs()[0];
         try {
     		String fieldName = fieldName(pjp);
     		Map<Integer, Integer> table = ensureTable(fieldName);
 			table.put(target.getId(), value.getId());
             return pjp.proceed();
         } finally {
             LOG.info("set: " + target + "." + fieldName(pjp)+" = "+value);
         }
     }
 
    @Pointcut("execution(public com.nomura.ltw.Identifiable+ com.nomura.ltw.Identifiable+.get*())")
     public void getters(){
     }
 
    @Pointcut("execution(public void com.nomura.ltw.Identifiable+.set*(com.nomura.ltw.Identifiable+))")
     public void setters(){
     }
 }
