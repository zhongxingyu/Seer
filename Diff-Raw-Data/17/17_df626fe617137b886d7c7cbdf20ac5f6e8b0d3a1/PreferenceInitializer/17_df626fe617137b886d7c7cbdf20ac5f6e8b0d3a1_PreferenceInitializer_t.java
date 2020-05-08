 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
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
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.texteditor.AnnotationPreference;
 
 /**
  * Class used to initialize default preference values.
  * 
  * @author Sebastien Dubois
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
		store.setDefault(PreferenceConstants.P_USER_ID, System.getProperty("user.name").toLowerCase()); //$NON-NLS-1$
 		PreferenceConstants.setUserEmailDefaultPreferences();
		if (store.getDefaultString(PreferenceConstants.P_USER_EMAIL) == "") { //$NON-NLS-1$
			store.setDefault(PreferenceConstants.P_USER_EMAIL, store.getDefaultString(PreferenceConstants.P_USER_ID)
					+ "@localhost.com"); //$NON-NLS-1$
		}
 		store.setDefault(PreferenceConstants.P_USE_DELTAS, true);
 		store.setDefault(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER, true);
 		store.setDefault(PreferenceConstants.P_HIDE_DELTAS_FILTER, true);
 
 		//For some reason, some anomaly annotations default values are not set correctly as per the plugin.xml#markerAnnotationSpecification
 		//so we force it here
 		EditorsUI.getPreferenceStore().setValue(PreferenceConstants.OPEN_ANOMALY_ANNOTATION_TEXT, true);
 		EditorsUI.getPreferenceStore().setValue(PreferenceConstants.OPEN_ANOMALY_ANNOTATION_STYLE,
 				AnnotationPreference.STYLE_SQUIGGLES);
 		EditorsUI.getPreferenceStore().setValue(PreferenceConstants.CLOSED_ANOMALY_ANNOTATION_TEXT, true);
 		EditorsUI.getPreferenceStore().setValue(PreferenceConstants.CLOSED_ANOMALY_ANNOTATION_STYLE,
 				AnnotationPreference.STYLE_SQUIGGLES);
 		EditorsUI.getPreferenceStore().setValue(PreferenceConstants.DISABLED_ANOMALY_ANNOTATION_TEXT, true);
 		EditorsUI.getPreferenceStore().setValue(PreferenceConstants.DISABLED_ANOMALY_ANNOTATION_STYLE,
 				AnnotationPreference.STYLE_SQUIGGLES);
 	}
 }
