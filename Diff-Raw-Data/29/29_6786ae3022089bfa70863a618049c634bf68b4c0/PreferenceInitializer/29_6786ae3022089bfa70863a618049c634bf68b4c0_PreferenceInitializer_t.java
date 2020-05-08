 package org.eclipse.stem.ui.populationmodels.preferences;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.stem.ui.populationmodels.Activator;
 
 /**
  * Class used to initialize default preference values.
  */
 public class PreferenceInitializer extends AbstractPreferenceInitializer {
 
 	public static long DEFAULT_REFERENCE_POPULATION = 16000000;
 	public static long DEFAULT_REFERENCE_DENSITY = 25000;
 	
 	/**
 	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
 	 */
 	public void initializeDefaultPreferences() {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		store.setDefault(PreferenceConstants.REFERENCE_POPULATION,
 						DEFAULT_REFERENCE_POPULATION);
 		store.setDefault(PreferenceConstants.REFERENCE_DENSITY,
 				DEFAULT_REFERENCE_DENSITY);
 	}	
 } // PreferenceInitializer
