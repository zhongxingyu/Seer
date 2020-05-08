 package org.jbpm.gd.jpdl.prefs;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jbpm.gd.jpdl.Plugin;
 
 public class Initializer extends AbstractPreferenceInitializer implements PreferencesConstants {
 
 	@Override
 	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Plugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(SERVER_NAME, "localhost");
		preferenceStore.setValue(SERVER_PORT, "8080");
		preferenceStore.setValue(SERVER_DEPLOYER, "/jbpm-console/upload");
		preferenceStore.setValue(USE_CREDENTIALS, false);
		preferenceStore.setValue(USER_NAME, "user name");
		preferenceStore.setValue(PASSWORD, "password");
 	}
 
 }
