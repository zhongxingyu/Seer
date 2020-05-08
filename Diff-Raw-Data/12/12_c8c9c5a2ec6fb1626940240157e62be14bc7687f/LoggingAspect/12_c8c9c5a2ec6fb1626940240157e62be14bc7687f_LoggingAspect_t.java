 /**
  * 
  */
 package ch.krizi.utility.logging.aspect;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.reflect.MethodSignature;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.krizi.utility.logging.annotation.Log;
 import ch.krizi.utility.logging.logger.LevelLogger;
 import ch.krizi.utility.logging.logger.factory.LevelLoggerFactory;
 
 /**
  * @author krizi
  * 
  */
 @Aspect
 public class LoggingAspect {
 
 	private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
 
 	@Around("execution(* *(..)) && @annotation(log)")
	public Object aroundMethod(ProceedingJoinPoint joinPoint, Log log) throws Throwable {
 		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
 
 		String methodName = signature.getName();
 		if (logger.isTraceEnabled()) {
 			logger.trace("before Method [{}]", methodName);
 		}
 		Class<?> type = joinPoint.getTarget().getClass();
 		LevelLogger classLogger = LevelLoggerFactory.createLogger(log.level(), type);
 		Object returnValue = null;
 		boolean exceptionThrown = false;
 		try {
 			if (log.logBeginEnd()) {
 				List<MethodParameter> methodParameter = createMethodParameter(signature, joinPoint.getArgs());
 				if (log.logArguments() && !methodParameter.isEmpty()) {
 					classLogger.log("begin Method [{}] with Parameter={}", methodName, methodParameter);
 				} else {
 					classLogger.log("begin Method [{}]", methodName);
 				}
 			}
 			returnValue = joinPoint.proceed();
 			return returnValue;
 		} catch (Throwable t) {
 			exceptionThrown = true;
 			if (logger.isTraceEnabled()) {
 				logger.trace("Method [{}] throws Exception [{}]", methodName, t);
 			}
 			if (log.logError()) {
 				LevelLogger errorLogger = LevelLoggerFactory.createLogger(log.errorLevel(), type);
 				errorLogger.log("Method [{}] throws unexpected error", methodName, t);
 			}
			throw t;
 		} finally {
 			if (log.logBeginEnd()) {
 				if (!isVoid(signature) && !exceptionThrown) {
 					classLogger.log("end Method [{}] return=[{}]", methodName, returnValue);
 				} else {
 					classLogger.log("end [{}]", methodName);
 				}
 			}
 			if (logger.isTraceEnabled()) {
 				logger.trace("after Method [{}]", methodName);
 			}
 		}
 	}
 
 	private boolean isVoid(MethodSignature signature) {
 		return "void".equals(signature.getReturnType().toString());
 	}
 
 	/**
 	 * create MethodParameter Objects representing the Method-Parameter (name, value, class)
 	 * 
 	 * @param signatur
 	 * @param objects
 	 * @return
 	 */
 	private List<MethodParameter> createMethodParameter(MethodSignature signatur, Object[] objects) {
 		List<MethodParameter> params = new ArrayList<MethodParameter>();
 		for (int i = 0; i < objects.length; i++) {
 			params.add(new MethodParameter(objects[i], signatur.getParameterTypes()[i], signatur.getParameterNames()[i]));
 		}
 		return params;
 	}
 }
