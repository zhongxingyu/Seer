 package org.eclipse.dltk.ruby.internal.debug.ui.launchConfigurations;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.debug.core.DLTKDebugPreferenceConstants;
 import org.eclipse.dltk.debug.ui.launchConfigurations.RemoteLaunchConfigurationTab;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 
 /**
  * 'Connect' launch configuration tab for remote ruby scripts
  */
 public class RubyRemoteLaunchConfigurationTab extends
 		RemoteLaunchConfigurationTab {
 
	public RubyRemoteLaunchConfigurationTab(String mode) {
		super(mode);
	}

 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#breakOnFirstLinePrefEnabled(org.eclipse.dltk.core.PreferencesLookupDelegate)
 	 */
 	protected boolean breakOnFirstLinePrefEnabled(
 			PreferencesLookupDelegate delegate) {
 		return delegate.getBoolean(RubyDebugPlugin.PLUGIN_ID,
 				DLTKDebugPreferenceConstants.PREF_DBGP_BREAK_ON_FIRST_LINE);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#dbpgLoggingPrefEnabled(org.eclipse.dltk.core.PreferencesLookupDelegate)
 	 */
 	protected boolean dbpgLoggingPrefEnabled(PreferencesLookupDelegate delegate) {
 		return delegate.getBoolean(RubyDebugPlugin.PLUGIN_ID,
 				DLTKDebugPreferenceConstants.PREF_DBGP_ENABLE_LOGGING);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#getNatureID()
 	 */
 	protected String getNatureID() {
 		return RubyNature.NATURE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#isValidToolkit(org.eclipse.dltk.core.IDLTKLanguageToolkit)
 	 */
 	protected boolean isValidToolkit(IDLTKLanguageToolkit toolkit) {
 		return (toolkit instanceof RubyLanguageToolkit) ? true : false;
 	}
 }
