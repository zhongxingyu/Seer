 package org.eclipse.dltk.debug.core;
 
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 
 public class DLTKDebugPluginPreferenceInitializer extends
 		AbstractPreferenceInitializer {
 
 	public DLTKDebugPluginPreferenceInitializer() {
 		super();
 	}
 
 	public void initializeDefaultPreferences() {
 		Preferences prefs = DLTKDebugPlugin.getDefault().getPluginPreferences();
 		prefs.setDefault(
 				DebugPreferenceConstants.PREF_DBGP_BREAK_ON_FIRST_LINE, false);
 
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_ENABLE_LOGGING,
 				false);
 
		// Connection
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_PORT, -1);
 
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_CONNECTION_TIMEOUT,
 				10000);
 
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_RESPONSE_TIMEOUT,
 				10000);
 
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_LOCAL,
 				true);
 
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_GLOBAL,
 				false);
 
 		prefs.setDefault(DebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_CLASS,
 				false);
 	}
 }
