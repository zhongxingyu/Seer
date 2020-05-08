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
 				DLTKDebugPreferenceConstants.PREF_DBGP_BREAK_ON_FIRST_LINE,
 				false);
 
 		prefs.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_ENABLE_LOGGING,
 				false);
 
 		// Connection
 		prefs.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_PORT,
 				DLTKDebugPreferenceConstants.DBGP_AUTODETECT_BIND_ADDRESS);
 
 		prefs.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_PORT, -1);
 
 		prefs.setDefault(
 				DLTKDebugPreferenceConstants.PREF_DBGP_CONNECTION_TIMEOUT,
 				10000);
 
 		prefs.setDefault(
 				DLTKDebugPreferenceConstants.PREF_DBGP_RESPONSE_TIMEOUT,
 				60 * 60 * 1000);
 
 		prefs.setDefault(
 				DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_LOCAL, true);
 
 		prefs
 				.setDefault(
 						DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_GLOBAL,
 						false);
 
 		prefs.setDefault(
 				DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_CLASS, false);

		prefs.setDefault(
				DLTKDebugPreferenceConstants.PREF_LAUNCH_CATCH_OUTPUT, false);
 	}
 }
