 package sk.folki.aspectlogger;
 
 import org.apache.log4j.Logger;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.springframework.stereotype.Component;
 
 // TODO Split sk.folki.aspectlogger package to subpackages
 @Component
 @Aspect
 public class LogAspect {
 	private LoggerGetter loggerGetter = new LoggerGetter();
 	private LoggableProceedor loggableProceeder = new LoggableProceedor(); 
 	private AspectLogger aspectLogger = new AspectLogger();
 	
	@Around(value = "@annotation(loggable)", argNames = "loggableMethodJoinPoint, loggableAnnotation")
 	public void invokedAndLogLoggableMethod(ProceedingJoinPoint loggableMethod, Loggable loggableAnnotation) throws Throwable {
 		Logger loggableMethodLogger = getLoggerFor(loggableMethod);		
 		LoggableMethodDescription loggableMethodDescription = createLoggableMethodDescription(loggableMethod, loggableAnnotation);
 		LoggableMethodInvocation loggableMethodInvocation = proccedToLoggableMethod(loggableMethod);
 		aspectLogger.log(loggableMethodLogger, loggableMethodDescription, loggableMethodInvocation);	// TODO Hide getting logger into aspectLogger; Note: add getLoggableMethodParentClass() method loggableMethodDescription
 		if (loggableMethodInvocation.isErrorOccured()) {
 			Throwable occuredError = loggableMethodInvocation.getOccuredError();
 			throw occuredError;
 		}
 	}
 	
 	private Logger getLoggerFor(ProceedingJoinPoint loggableMethod) {
 		return loggerGetter.getLoggerFor(loggableMethod);		
 	}
 	
 	private LoggableMethodDescription createLoggableMethodDescription(ProceedingJoinPoint loggableMethod, Loggable loggableAnnotation) {
 		return ProceedingJoinPointAdapter.adapt(loggableMethod, loggableAnnotation).toLoggableMethod();
 	}
 	
 	private LoggableMethodInvocation proccedToLoggableMethod(ProceedingJoinPoint loggableMethod) {
 		return loggableProceeder.proceedToJoinPoint(loggableMethod);		
 	}
 }
