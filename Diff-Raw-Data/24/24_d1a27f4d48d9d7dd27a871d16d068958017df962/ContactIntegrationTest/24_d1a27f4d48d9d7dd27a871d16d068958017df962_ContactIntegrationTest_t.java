 package nl.enovation.addressbook.cqrs.webui.contacts;
 
 import nl.enovation.addressbook.cqrs.query.repositories.ContactQueryRepositoryImpl;
 
 import org.axonframework.eventstore.EventStore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.junit.Assert.assertNotNull;
 
 /**
 * Basic test to tell if the autowiring is working properly. 
  * @author Allard Buijze Modified by Yorick
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/persistence-infrastructure-context.xml",
                                    "classpath:/META-INF/spring/cqrs-infrastructure-context.xml", "classpath:/META-INF/spring/contacts-context.xml" })
 public class ContactIntegrationTest {
 
     @Autowired
     private EventStore eventStore;
 
     @Autowired
     private ThreadPoolTaskExecutor taskExecutor;
 
     @Autowired
     private ContactQueryRepositoryImpl contactQueryRepositoryImpl;
 
     @Test(timeout = 10000)
     public void testApplicationContext() throws InterruptedException {
         assertNotNull(eventStore);
         assertNotNull(taskExecutor);
         assertNotNull(contactQueryRepositoryImpl);
     }
 
 }
