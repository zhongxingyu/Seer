 package de.javadesign.cdi.extension.spring;
 
 import de.javadesign.cdi.extension.spring.context.ApplicationContextProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.web.context.ContextLoader;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.spi.Extension;
 import javax.enterprise.inject.spi.ProcessAnnotatedType;
 import java.text.MessageFormat;
 
 /**
  * This CDI Extension observes ProcessAnnotatedType events fired for every found bean during bean
  * discovery phase to prevent Spring Beans from being deployed again if found by the CDI XML or
  * class path scanner.
  *
  */
 public class SpringBeanVetoExtension implements Extension {
 
     private static final Logger LOG = LoggerFactory.getLogger("de.javadesign.cdi.extension");
 
     private boolean applicationContextFound = false;
     private boolean initialized = false;
 
     private ConfigurableListableBeanFactory beanFactory;
 
     public SpringBeanVetoExtension() {
         LOG.info(MessageFormat.format("{0} created.", this.getClass().getSimpleName()));        
     }
 
     public void vetoSpringBeans(@Observes ProcessAnnotatedType event) {
         if (!initialized) {
             init();
         }
         if (!applicationContextFound) {
             return;
         }
         if (beanForClassExists(event.getAnnotatedType().getJavaClass())) {
             // Do not deploy this class to the CDI context.
             event.veto();
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Vetoing " + event.getAnnotatedType().getJavaClass().getCanonicalName());
             }
         }
     }
 
     public void init() {
         initialized = true;
         AbstractApplicationContext applicationContext = (AbstractApplicationContext) ContextLoader
                 .getCurrentWebApplicationContext();
 
         if (applicationContext==null) {
             LOG.warn("No Web Spring-ApplicationContext found, try to resolve via application context provider.");
             applicationContext = (AbstractApplicationContext) ApplicationContextProvider.getApplicationContext();
         }
 
         if (null != applicationContext) {
             LOG.info("ApplicationContext found.");
             applicationContextFound = true;
             beanFactory = applicationContext.getBeanFactory();
         } else {
             LOG.warn("No Spring-ApplicationContext found.");
         }
     }
 
     /**
      * Uses the Spring BeanFactory to see if there is any Spring Bean matching
      * the given Class and returns true in the case such a bean is found and
      * false otherwise.
      *
      * @param clazz The Class currently observed by the CDI container.
      * @return True if there is a Spring Bean matching the given Class, false otherwise
      */
     private boolean beanForClassExists(Class<?> clazz) {
         // Lookup
         if (0 != beanFactory.getBeanNamesForType(clazz).length) {
             return true;
         }
         // Workaround if interfaces in combination with component scanning is used.
         // Spring framework automatically detects interface and implementation pairs
         // but only returns bean names for interface types then.
         // We loop all known bean definitions to find out if the given class is
         // a spring bean.
         String[] names = beanFactory.getBeanDefinitionNames();
         for (String name : names) {
             BeanDefinition definition = beanFactory.getBeanDefinition(name);
            if (definition.getBeanClassName().equals(clazz.getCanonicalName())) {
                 return true;
             }
         }
         return false;
     }
 
 }
