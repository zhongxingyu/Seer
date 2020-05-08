 package org.blacklight.android.flexibleprofiles.environment;
 
 import com.google.common.eventbus.EventBus;
 
 public class MockEnvironment implements Environment {
 	private static Environment instance;
 	private final static EventBus eventBus = new EventBus();
 
 	public static Environment getInstance() {
 		if (instance == null) {
			instance = new AppEnvironment();
 		}
 		
 		return instance;
 	}
 	
 	public String getConfigurationFilePath() {
 		return "src/test/resources/profiles_script.xml";
 	}
 
 	public static EventBus getEventBus() {
 		return eventBus;
 	}
 
 
 }
