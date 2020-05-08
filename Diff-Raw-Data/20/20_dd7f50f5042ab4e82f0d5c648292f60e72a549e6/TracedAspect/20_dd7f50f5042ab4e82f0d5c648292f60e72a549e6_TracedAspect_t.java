 package verify.aop.aspectj;
 
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 @Aspect
 public class TracedAspect {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
 
	@Pointcut("(execution(* *.*(..)) || call(* *..*.*(..)) || initialization(*..*.new(..)))  && !within(TracedAspect)")
 	public void traced() {
 	};
 	
 	@Before("traced()")
	public void traceLogging(JoinPoint pjp) {	
		Signature sig = pjp.getSignature();
		logger.info(
		"Entering [" + sig.toShortString() + "]");
 	}
 
 }
