 package de.flower.rmt.test;
 
 import de.flower.common.test.wicket.WicketTester;
 import de.flower.common.ui.serialize.Filter;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
import org.wicketstuff.jsr303.JSR303Validation;
 
 /**
  * Base class for ui-test that require full application stack down to database.
  *
  * @author flowerrrr
  */
 public abstract class AbstractRMTWicketIntegrationTests extends AbstractRMTIntegrationTests {
 
     protected WicketTester wicketTester = null;
 
     @Autowired
     private WebApplication webApp;
 
     @Autowired
     private Filter filter;
 
     protected WebApplication createWebApp(ApplicationContext ctx) {
 //        return new TestApplication(ctx);
         return webApp;
     }
 
     @BeforeMethod
     public void init() {
         if (wicketTester == null) {
             createTester(applicationContext);
         }
        JSR303Validation.getInstance().reset();
     }
 
     @AfterMethod
     public void cleanup() {
         Mockito.validateMockitoUsage();
     }
 
     private void createTester(ApplicationContext ctx) {
         WebApplication webApp = createWebApp(ctx);
         wicketTester = new WicketTester(webApp);
         wicketTester.setPageSerializationValidatorFilter(filter);
     }
 
 }
