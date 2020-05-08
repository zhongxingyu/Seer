 package verify.aop.aspectj;
 
import java.util.Date;

 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 
 @Aspect
 public class TracedAspect {
 
	@Pointcut("execution(* *..*Account*.debit(..))")
 	public void traced() {
 	};
 	
 	@Before("traced()")
	public void traceLogging() {
		System.out.println(new Date());
 	}
 
 }
