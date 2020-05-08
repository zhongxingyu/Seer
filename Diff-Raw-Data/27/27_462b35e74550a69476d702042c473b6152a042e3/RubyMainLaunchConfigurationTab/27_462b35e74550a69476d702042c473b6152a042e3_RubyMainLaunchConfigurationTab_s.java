 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  ******************************************************************************/
 package org.eclipse.dltk.ruby.internal.debug.ui.launchConfigurations;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.debug.core.DLTKDebugPreferenceConstants;
 import org.eclipse.dltk.debug.ui.launchConfigurations.MainLaunchConfigurationTab;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 
 /**
  * Main launch configuration tab for ruby scripts
  */
 public class RubyMainLaunchConfigurationTab extends MainLaunchConfigurationTab {
 
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
