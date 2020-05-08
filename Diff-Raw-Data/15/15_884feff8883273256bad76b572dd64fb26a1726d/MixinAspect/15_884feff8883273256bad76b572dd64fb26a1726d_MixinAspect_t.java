 package ua.kiev.naiv.drinkit.cocktail.common;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
 import org.springframework.stereotype.Component;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Pavel Kolmykov
  * Date: 28.07.13
  * Time: 16:14
  */
 @Component
 @Aspect
 public class MixinAspect {
     @Around("@annotation(annotation)")
     public Object test(final ProceedingJoinPoint joinPoint, final JsonMixin annotation) {
         Class<?> mixin = annotation.value();
         ObjectMapper mapper = new ObjectMapper();
        MethodSignature msig = (MethodSignature) joinPoint.getSignature();
        mapper.addMixInAnnotations(msig.getMethod().getReturnType(), mixin);
         System.out.println("added mixin: " + mixin.getName());
         try {
             mapper.writeValue(WebContext.getInstance().getResponse().getOutputStream(), joinPoint.proceed());
         } catch (Throwable ex) {
             throw new RuntimeException(ex);
         }
         return null;
     }
 }
