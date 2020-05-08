 package component1;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.logging.*;
 import org.springframework.stereotype.Component;
 
 
 @Component
 public final class SomeBean {
 
     private final Log logger = LogFactory.getLog(getClass());
 
 
     public SomeBean() {
         logger.warn("\nSomeBean-ctor\n");
     }
 
 
     @PostConstruct // TODO not working in JBoss?? //         <module name="javax.annotation.api"/>????
     public void init() {
         logger.warn("\nI'm alive\n");
         // throw new RuntimeException("kvakkel");
     }
 
 
     public String utf8String() {
         return "æøåÆØÅ";
     }
 }

