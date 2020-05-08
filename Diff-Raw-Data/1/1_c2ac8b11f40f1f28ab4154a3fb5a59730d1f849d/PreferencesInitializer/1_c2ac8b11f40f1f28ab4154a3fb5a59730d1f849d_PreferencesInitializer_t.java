 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.internal.preferences;
 
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
 
 
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
 		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
 		preferenceStore.setDefault(PREF_INTERVAL_GRADES, DEFAULT_INTERVAL_GRADES);
 		preferenceStore.setDefault(PREF_INTERVAL_MRU_COUNT, DEFAULT_INTERVAL_MRU_COUNT);
 	}
 
 	/**
 	 * Update the most recently  used interval adding
 	 * a new interval.
 	 *
 	 * @param interval The new interval.
 	 */
 	public static void addMRUInterval(int interval){
		if (interval <= 0 || DEFAULT_INTERVAL_GRADES.contains(":" + interval)) return; //$NON-NLS-1$
         IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
         String mruList = prefStore.getString(PREF_INTERVAL_MRU_LIST);
         if (mruList == null || mruList.trim().length() == 0) {
         	mruList = "" + interval; //$NON-NLS-1$
         }else{
         	StringTokenizer st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
         	int maxCount = prefStore.getInt(PREF_INTERVAL_MRU_COUNT);
         	boolean found = false;
         	while (st.hasMoreTokens()) {
         		String token = st.nextToken();
         		try {
         			int s = Integer.parseInt(token);
         			if(s == interval ) {
         				found = true;
         				break;
         			}
         		}
         		catch (NumberFormatException nfe) {
         		}
         	}
         	if(!found) {
         		mruList = mruList + ":" + interval; //$NON-NLS-1$
         		st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
         		if(st.countTokens() > maxCount) {
         			int comma = mruList.indexOf(":"); //$NON-NLS-1$
         			if(comma != -1) {
         				mruList = mruList.substring(comma+1);
         			}
         		}
         	}
         }
         prefStore.setValue(PREF_INTERVAL_MRU_LIST, mruList);
     }
 }
