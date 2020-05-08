 package name.felixbecker.brokenaop.aspects;
 
import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 
 @Aspect
 public class BrokenAspect {
 	
 	@Pointcut(value="execution(* *(..))")
 	public void allMethods() {}
 	
 	@Before("allMethods()")
	public void logAllMethodEnters(JoinPoint joinPoint) {
		System.out.println("BrokenAspect: Method enter!" + joinPoint.toShortString());
 	}
 
 }
