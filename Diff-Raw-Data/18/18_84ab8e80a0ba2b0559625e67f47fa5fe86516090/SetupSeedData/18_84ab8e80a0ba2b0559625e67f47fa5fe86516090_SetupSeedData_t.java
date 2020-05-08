 package org.motechproject.ghana.mtn.tools.seed;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class SetupSeedData {
     public static final String APPLICATION_CONTEXT_XML = "seed/applicationContext-tools.xml";
 
     public static void main(String[] args) {
         ApplicationContext context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML);
         SeedLoader seedLoader = (SeedLoader) context.getBean("seedLoader");
         seedLoader.load();
        ((ClassPathXmlApplicationContext) context).close();
     }
 }
