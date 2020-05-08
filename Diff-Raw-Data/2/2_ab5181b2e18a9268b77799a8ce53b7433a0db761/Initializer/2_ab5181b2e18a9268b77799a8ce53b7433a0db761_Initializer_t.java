 package org.jbpm.gd.jpdl.prefs;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 
 public class Initializer extends AbstractPreferenceInitializer implements PreferencesConstants {
 
 	@Override
 	public void initializeDefaultPreferences() {
		IEclipsePreferences preferenceStore = DefaultScope.INSTANCE.getNode("org.jbpm.gd.jpdl");
 		preferenceStore.put(SERVER_NAME, "localhost");
 		preferenceStore.put(SERVER_PORT, "8080");
 		preferenceStore.put(SERVER_DEPLOYER, "/jbpm-console/upload");
 		preferenceStore.putBoolean(USE_CREDENTIALS, false);
 		preferenceStore.put(USER_NAME, "user name");
 		preferenceStore.put(PASSWORD, "password");
 	}
 
 }
