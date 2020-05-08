 package org.eclipse.swordfish.core.test;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.swordfish.core.SwordfishContext;
 import org.eclipse.swordfish.core.test.util.OsgiSupport;
 import org.eclipse.swordfish.core.test.util.SwordfishTestCase;
 import org.osgi.framework.Bundle;
 import org.springframework.osgi.util.OsgiBundleUtils;
 
 /**
  * The central sentinel test that checks if platform has been started correctly. The test is launched after all bundles are started
  * and all spring application contexts created
  *
  */
 public class CentralSentinelTest extends SwordfishTestCase {
	
 	public CentralSentinelTest() {
 		super();
 	}
 
     public void test1SwordfishHasStarted() {
 		List<Pattern> startBundles = getStartBundlePatterns();
 		List<Pattern> excludedBundles = getExcludeBundlePatterns();
 
         for (Bundle bundle : bundleContext.getBundles()) {
         	String bundleName = bundle.getSymbolicName();
 
     		LOG.debug("Checking state of " + bundleName);
    		
         	if (OsgiBundleUtils.isFragment(bundle)) {
                 assertEquals(String.format("Fragment bundle [%s] should be in the RESOLVED state", bundle.getSymbolicName()), Bundle.RESOLVED, bundle.getState());

         	} else {
            	if (bundleNeedsToBeStarted(bundle, startBundles, excludedBundles)) {
             		assertEquals(String.format("Bundle [%s] should be in the ACTIVE state", bundle.getSymbolicName()), Bundle.ACTIVE, bundle.getState());
            	} else {
                	LOG.debug(bundleName + " ignored in check (State = " + bundle.getState() + ")");
             	}
            }
         }
 
         SwordfishContext swordfishContext = OsgiSupport.getReference(bundleContext, SwordfishContext.class);
         assertNotNull(swordfishContext);
         assertNotNull(swordfishContext.getConfigurationService());
         assertNotNull(swordfishContext.getEventService());
     }
 
 }
