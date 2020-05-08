 /*
  * Created on 22 Jun 2007
  */
 package uk.org.ponder.rsac.test;
 
 import java.net.URL;
import java.util.Properties;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
 
 import uk.org.ponder.arrayutil.ArrayUtil;
 import uk.org.ponder.rsac.RSACBeanLocator;
 
 /** A base class for deriving test fixtures which interact with an RSAC request cycle **/
 
 public abstract class AbstractRSACTests extends
     AbstractDependencyInjectionSpringContextTests {
    
   private RSACBeanLocator rsacbl;
   protected String[] configLocations = new String[] {};
   protected String[] requestConfigLocations = new String[] {};
   
   protected String[] getRequestConfigLocations() {
     return requestConfigLocations;
   }
   
   protected String[] getConfigLocations() {
     return configLocations;
   }
 
   public void contributeConfigLocations(String[] configLocations) {
     this.configLocations = (String[]) ArrayUtil.concat(this.configLocations, configLocations);
   }
   
   public void contributeRequestConfigLocations(String[] requestConfigLocations) {
     this.requestConfigLocations = (String[]) ArrayUtil.concat(this.requestConfigLocations, requestConfigLocations);    
   }
   
   public void contributeConfigLocation(String configLocation) {
     this.configLocations = (String[]) ArrayUtil.append(this.configLocations, configLocation);
   }
 
   public void contributeRequestConfigLocation(String requestConfigLocation) {
     this.requestConfigLocations = (String[]) ArrayUtil.append(this.requestConfigLocations, requestConfigLocation);
   }
   
   public AbstractRSACTests() {
    // These are being hardcoded due to various classpath issues with Maven 2 Testing
    // String log4jprops = "uk/org/ponder/rsac/test/log4j.test.properties";
    // URL url = this.getClass().getClassLoader().getResource(log4jprops);
    // PropertyConfigurator.configure(url);
    
    Properties props = new Properties();
    props.put("log4j.rootCategory", "warn");
    props.put("log4j.rootLogger", "warn, stdout");
    props.put("log4j.logger.org.springframework", "warn");
    props.put("log4j.logger.PonderUtilCore", "info");
    props.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
    props.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
    props.put("log4j.appender.stdout.layout.ConversionPattern", "%d %p (%F:%L) - <%m>%n");
    
    PropertyConfigurator.configure(props);
   }
   
   /** Override this method to determine whether this test should consist of a 
    * single RSAC cycle - if it returns <code>true</code>, the RSAC will be 
    * started and stopped during setUp() and tearDown().
    */
   
   protected boolean isSingleShot() {
     return true;
   }
   
   protected ConfigurableApplicationContext loadContextLocations(
       String[] locations) {
     final LocalRSACResourceLocator resourceLocator = new LocalRSACResourceLocator();
     resourceLocator.setConfigLocations(getRequestConfigLocations());
 
     final ConfigurableApplicationContext cac = new ClassPathXmlApplicationContext(
         locations, false) {
       protected void postProcessBeanFactory(
           ConfigurableListableBeanFactory beanFactory) {
         resourceLocator.setApplicationContext(this);
         beanFactory.registerSingleton("RSACResourceLocator", resourceLocator);
       }
     };
 
     cac.refresh();
     return cac;
   }
 
   public RSACBeanLocator getRSACBeanLocator() {
     return rsacbl;
   }
   
   /** Locates a particular request bean, assuming that a request is currently active.
    * That is, this is either a singleshot test, or that getRequestLauncher() has been
    * invoked without disposing the current RSAC.
    */
   public Object locateRequestBean(String name) {
     return getRSACBeanLocator().getBeanLocator().locateBean(name);
   }
   
   protected void onSetUp() throws Exception {
     rsacbl = (RSACBeanLocator) applicationContext.getBean("RSACBeanLocator");
     if (isSingleShot()) {
       getRSACBeanLocator().startRequest();
     }
   }
 
   protected void onTearDown() throws Exception {
     if (isSingleShot()) {
       rsacbl.endRequest();
     }
   }
 }
