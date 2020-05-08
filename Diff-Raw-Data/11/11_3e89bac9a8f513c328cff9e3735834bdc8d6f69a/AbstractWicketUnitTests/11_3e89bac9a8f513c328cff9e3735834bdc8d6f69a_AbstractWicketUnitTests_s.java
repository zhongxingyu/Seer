 package de.flower.common.test.wicket;
 
 import de.flower.common.spring.SpringApplicationContextBridge;
 import org.apache.wicket.Component;
 import org.apache.wicket.resource.loader.IStringResourceLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.BeforeMethod;
 
 import java.util.Locale;
 
 /**
  * Base class for pure wicket unit tests.
  * Adds a dummy string resource locator so that all resource keys can be resolved.
  *
  * @author flowerrrr
  */
 public class AbstractWicketUnitTests {
 
     public final static Logger log = LoggerFactory.getLogger(AbstractWicketUnitTests.class);
 
     protected WicketTester wicketTester;
 
     @BeforeMethod
     public void setUp() {
         wicketTester = new WicketTester();
         wicketTester.getApplication().getResourceSettings().getStringResourceLoaders().add(new IStringResourceLoader() {
             @Override
             public String loadStringResource(final Class<?> clazz, final String key, final Locale locale, final String style, final String variation) {
                 return "resource-for(" + key + ")";
             }
 
             @Override
             public String loadStringResource(final Component component, final String key, final Locale locale, final String style, final String variation) {
                 return "resource-for(" + key + ")";
             }
         });
 
         // for usage of validation needed. not nice but i found no easy way to have
         // two different validation.xml on the test-classpath (one w/o ApplicationContextAwareValidationFactory and one with).
         SpringApplicationContextBridge.getInstance().setApplicationContext(new MockitoFactoryApplicationContext());
     }
 
 }
