 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.internal.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
 
 
 /**
  * The bundle's preference initializer implementation.
  */
 public class PreferencesInitializer extends AbstractPreferenceInitializer implements IPreferenceConsts {
 	/**
 	 * Constructor.
 	 */
 	public PreferencesInitializer() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
 	 */
 	@Override
 	public void initializeDefaultPreferences() {
 		IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
 		// [Hidden] Hide dynamic target discovery navigator content extension: default on
 		store.setDefault(PREF_HIDE_DYNAMIC_TARGET_DISCOVERY_EXTENSION, true);
		// [Hidden] Favorites category copy mode: default off
 		store.setDefault(IPreferenceConsts.PREF_FAVORITES_CATEGORY_MODE_LINK, false);
 	}
 }
