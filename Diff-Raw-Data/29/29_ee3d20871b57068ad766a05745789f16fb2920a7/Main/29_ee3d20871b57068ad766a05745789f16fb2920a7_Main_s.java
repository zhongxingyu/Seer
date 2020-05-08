 package cache;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Main {
 
     public static void main(String[] args) throws Exception {
 
         ApplicationContext appContext = new ClassPathXmlApplicationContext(
                 "cache/cache-beans.xml");
 
         MessageRepository repo = appContext.getBean(MessageRepository.class);
         repo.save(new Message("key1", "Hello"));
 
         System.out.println(repo.getMessage("key1"));
         System.out.println(repo.getMessage("key1"));
 
         repo.save(new Message("key1", "World"));
 
         System.out.println(repo.getMessage("key1"));
         System.out.println(repo.getMessage("key1"));
 
         ((ConfigurableApplicationContext) appContext).close();
     }
 }
