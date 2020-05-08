 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class is used to initialize R4E preferences
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 
 /**
  * Class used to initialize default preference values.
  * 
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class PreferenceInitializer extends AbstractPreferenceInitializer {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method initializeDefaultPreferences.
 	 * 
 	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
 	 */
 	@Override
 	public void initializeDefaultPreferences() {
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 
 		//Set default User ID if none already stored in preferences
 		store.setDefault(PreferenceConstants.P_USER_ID, System.getProperty("user.name").toLowerCase());
 		PreferenceConstants.setUserEmailDefaultPreferences();
 		store.setDefault(PreferenceConstants.P_USE_DELTAS, true);
		store.setDefault(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER, true);
 		store.setDefault(PreferenceConstants.P_HIDE_DELTAS_FILTER, true);
 	}
 }
