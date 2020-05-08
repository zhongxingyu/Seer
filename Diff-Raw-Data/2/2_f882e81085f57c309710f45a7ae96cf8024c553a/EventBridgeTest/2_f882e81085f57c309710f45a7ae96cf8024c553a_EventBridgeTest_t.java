 /**
  * 
  */
 
 package com.manning.sdmia.ch11.event;
 
 import org.osgi.framework.Bundle;
 import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
 
 /**
  * @author Thierry Templier
  */
 public class EventBridgeTest extends AbstractConfigurableBundleCreatorTests {
 	private TestEventHandler testEventHandler;
 
 	private Bundle findBundle(String symbolicName) {
 		for (Bundle bundle : bundleContext.getBundles()) {
 			if (bundle.getSymbolicName().equals(symbolicName)) {
 				return bundle;
 			}
 		}
 		return null;
 	}
 	
 	public void testEventBridge() throws Exception {
		Bundle bundle = findBundle("com.manning.sdmia.springdm-sample");
 		if (bundle!=null) {
 			bundle.update();
 		}
 		
 		//assertTrue(testEventHandler.isCalled());
 	}
 
 	@Override
 	protected String[] getTestBundlesNames() {
 		return new String [] {
 			"org.apache.felix, org.apache.felix.eventadmin, 1.0.0",
 			"com.manning.sdmia.ch11, ch11-event-bridge, 1.0.0",
 			"com.manning.sdmia, springdm-sample, 1.0-SNAPSHOT"
 		};
 	}
 
 	@Override
 	protected String getManifestLocation() {
 		return "classpath:/com/manning/sdmia/ch11/event/event-bridge.mf";
 	}
 
 	@Override
 	protected String[] getConfigLocations() {
 		return new String [] {"/com/manning/sdmia/ch11/event/event-bridge-context.xml"};
 	}
 
 	public TestEventHandler getTestEventHandler() {
 		return testEventHandler;
 	}
 
 	public void setTestEventHandler(TestEventHandler testEventHandler) {
 		this.testEventHandler = testEventHandler;
 	}
 
 }
