 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.internal.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 
 /**
  * Class for setting the default preferences of Eclipse Fedora Packager preferences.
  */
 public class FedoraPackagerPreferenceInitializer extends AbstractPreferenceInitializer {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
 	 */
 	@Override
 	public void initializeDefaultPreferences() {
 		// make sure fields get pre-filled.
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(PackagerPlugin.PLUGIN_ID);
 		// General
 		node.putBoolean(FedoraPackagerPreferencesConstants.PREF_DEBUG_MODE, FedoraPackagerPreferencesConstants.DEFAULT_DEBUG_MODE);
 		// Lookaside prefs
 		node.put(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL,
 				FedoraPackagerPreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL);
 		node.put(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL,
 				FedoraPackagerPreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL);
 		// Koji prefs
 		node.put(FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL, FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
 		node.put(FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL, FedoraPackagerPreferencesConstants.DEFAULT_KOJI_HUB_URL);
 		
 		// make sure default preferences (set to default button) work propperly
 		IPreferenceStore prefStore = PackagerPlugin.getDefault().getPreferenceStore();
 		prefStore.setDefault(FedoraPackagerPreferencesConstants.PREF_DEBUG_MODE, FedoraPackagerPreferencesConstants.DEFAULT_DEBUG_MODE);
 		// set default preferences for lookaside
 		prefStore.setDefault(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL,
 				FedoraPackagerPreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL);
 		prefStore.setDefault(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL,
 				FedoraPackagerPreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL);
 		// Koji prefs
 		prefStore.setDefault(FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL, FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
 		prefStore.setDefault(FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL, FedoraPackagerPreferencesConstants.DEFAULT_KOJI_HUB_URL);
 	}
 
 }
