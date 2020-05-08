 package org.jbpm.gd.jpdl;
 
 import org.eclipse.ui.IStartup;
 import org.jbpm.gd.jpdl.prefs.Jbpm3PreferencesManager;
 
 public class StartupClass implements IStartup {
 
 	public void earlyStartup() {
 		// Reference the Jbpm3PreferencesManager to trigger the initialization.
//		Jbpm3PreferencesManager.INSTANCE.toString();
 	}
 
 }
