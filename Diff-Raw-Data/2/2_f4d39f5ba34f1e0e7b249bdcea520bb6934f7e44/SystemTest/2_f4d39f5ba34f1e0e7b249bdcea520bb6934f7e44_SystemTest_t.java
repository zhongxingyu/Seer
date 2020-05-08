 package vivo;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.Date;
 import java.util.Iterator;
 import vivo.shared.services.VivoQueryService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"test-vivo.xml"})
 public class SystemTest extends AbstractDependencyInjectionSpringContextTests {
 
     @Autowired
     private VivoQueryService vivoQueryServiceImpl;
 
     @After
     public void cleanUp() throws Throwable {
     }
 
     @Test
     public void testDbMethods() throws Throwable {
         String userId = "99999";
         String sampleHistory = "test|sample|query";
         assertTrue("There should be nothing coming back from DB before history is created", vivoQueryServiceImpl.findVivoQuery(userId) == null);
         vivoQueryServiceImpl.saveOrUpdateVivoQuery(userId, sampleHistory);
        assertEquals("Didn't return history expected.", sampleHistory, vivoQueryServiceImpl.findVivoQuery(userId));
         vivoQueryServiceImpl.deleteVivoQuery(userId);
         assertTrue("After deletion, there should be nothing coming back from DB", vivoQueryServiceImpl.findVivoQuery(userId) == null);
     }
 }
