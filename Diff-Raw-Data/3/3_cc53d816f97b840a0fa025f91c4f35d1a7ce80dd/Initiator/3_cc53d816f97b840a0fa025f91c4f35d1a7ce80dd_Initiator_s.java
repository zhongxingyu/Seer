 package dk.sst.snomedcave.dbgenerate;
 
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextStoppedEvent;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Initiator {
     public static void main(String[] args) {
         ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:dbgenerateContext.xml");
         SnomedParser snomedParser = context.getBean(SnomedParser.class);
 
         for (String name : context.getBeanDefinitionNames()) {
             System.out.println("name = " + name);
         }
 
         if (snomedParser == null) {
             throw new RuntimeException("Could not start");
         }
 
         if (snomedParser.conceptRepository == null) {
             throw new RuntimeException("Needs repo");
         }
 
         snomedParser.startImport();
 
         context.destroy();
     }
 }
