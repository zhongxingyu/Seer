 package ddth.dasp.framework.spring;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.osgi.context.BundleContextAware;
 
 import ddth.dasp.common.DaspGlobal;
 
 /**
  * This class provide an application-wide access to Spring's ApplicationContext.
  * Use {@link ApplicationContextProvider#getApplicationContext()} to obtain the
  * ApplicationContext instance.
  * <p>
  * Note: This class is a Spring bean and must be initialized in the
  * Spring-Configuration file:
  * </p>
  * 
  * <pre>
  * &lt;bean id="contextApplicationContextProvider" class="...ApplicationContextProvider" lazy-init="false" init-method="init" destroy-method="destroy" /&gt;
  * </pre>
  * <p>
  * In code:
  * </p>
  * 
  * <pre>
  * ApplicationContext appContext = ApplicationContextProvider.getApplicationContext(bundleId);
  * </pre>
  * 
  * @author NBThanh <btnguyen2k@gmail.com>
  */
 public class ApplicationContextProvider implements ApplicationContextAware, BundleContextAware {
 
    private static String calcKey(long bundleId) {
        return "SPRING_APP_CONTEXT_" + bundleId;
    }

    public static ApplicationContext getApplicationContext(Bundle bundle) {
        return getApplicationContext(bundle.getBundleId());
    }

    public static ApplicationContext getApplicationContext(long bundleId) {
        return (ApplicationContext) DaspGlobal.getGlobalVar(calcKey(bundleId));
    }

     private ApplicationContext applicationContext;
     private Bundle bundle;
     private String KEY;
 
     public void init() {
         DaspGlobal.setGlobalVar(KEY, applicationContext);
     }
 
     public void destroy() {
         DaspGlobal.removeGlobalVar(KEY);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setBundleContext(BundleContext bundleContext) {
         this.bundle = bundleContext.getBundle();
        KEY = calcKey(bundle.getBundleId());
     }
 }
