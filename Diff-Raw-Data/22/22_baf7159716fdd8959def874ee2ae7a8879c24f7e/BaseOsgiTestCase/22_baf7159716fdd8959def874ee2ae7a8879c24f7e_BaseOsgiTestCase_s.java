 package org.eclipse.swordfish.core.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
 import org.springframework.util.Assert;
 
 public class BaseOsgiTestCase extends AbstractConfigurableBundleCreatorTests {
     protected Logger LOG = LoggerFactory.getLogger(getClass());
    
     private List<ServiceRegistration> regirstrationsToCancel = new ArrayList<ServiceRegistration>();
     protected void addRegistrationToCancel(ServiceRegistration serviceRegistration) {
         regirstrationsToCancel.add(serviceRegistration);
     }
     @Override
     protected void onTearDown() throws Exception {
         Assert.notNull(regirstrationsToCancel);
         for(ServiceRegistration serviceRegistration : regirstrationsToCancel) {
             try {
                 serviceRegistration.unregister();
             } catch (IllegalStateException ex) {
                 LOG.error("Unregistering service exception", ex);
             }
         }
         regirstrationsToCancel.clear();
         super.onTearDown();
     }
 
 
     static {
         try {
            System.setProperty("org.osgi.vendor.framework",
                    "org.eclipse.swordfish.core.planner.test");
             System.setProperty("osgi.clean", "true");
             System.setProperty("osgi.console", "22763");
         } catch (Throwable t) {
         }
     }
        
     @Override
     protected String getManifestLocation() {
         // return "classpath:META-INF/MANIFEST.MF";
         return "classpath:org/eclipse/swordfish/core/planner/test/MANIFEST.MF";
     }
 
 
 }
