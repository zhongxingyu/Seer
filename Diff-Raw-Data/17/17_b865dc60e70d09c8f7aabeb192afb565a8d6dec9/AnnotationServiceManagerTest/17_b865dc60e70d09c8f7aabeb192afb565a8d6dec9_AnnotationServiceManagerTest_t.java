import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * @author Thomas Duncan
  */
 public class AnnotationServiceManagerTest {
     private final Injector injector = Guice.createInjector(new TestModule());
     private Service service;
     
     public AnnotationServiceManagerTest() {
     }
 
     @Before
     public void setup() {
         service = injector.getInstance(Service.class);
     }
     
     @Test
     public void testStartup() {
         ServiceManager manager = injector.getInstance(ServiceManager.class);
         manager.startup();
         Assert.assertTrue(service.wasCalled());
     }
     
     @Test
     public void testShutdown() {
         ServiceManager manager = injector.getInstance(ServiceManager.class);
         manager.startup();
         Assert.assertTrue(service.wasCalled());
     }
 
     @After
     public void shutdown() {
         service = null;
     }
     
     private class TestModule extends AbstractModule {
         @Override
         protected void configure() {
             bind(Service.class).in(Singleton.class);
             bind(ServiceManager.class).to(AnnotationServiceManager.class);
         }
         
         @Provides
         @SuppressWarnings("unchecked")
         protected Collection<Class<?>> providesServiceClassCollection() {
             return Arrays.<Class<?>>asList(Service.class);
         }
     }
     
     private static class Service {
         private boolean called = false;
         
         @PostConstruct
         @PreDestroy
         public void invoke() {
             called = true;
         }
         
         public boolean wasCalled() {
             return called;
         }
     }
 }
