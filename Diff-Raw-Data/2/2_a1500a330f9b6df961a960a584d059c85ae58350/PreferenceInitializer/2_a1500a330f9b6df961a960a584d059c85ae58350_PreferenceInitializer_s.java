 package com.versionone.common.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.versionone.common.Activator;
 
 public class PreferenceInitializer extends AbstractPreferenceInitializer {
 
     static final String DEFAULT_URL = "http://localhost/VersionOne/";
     static String DEFAULT_TASK_ATTRIBUTE_SELECTION = "Name,Description,Category,Customer,DetailEstimate,Estimate,LastVersion,Number,Owners,Parent,Reference,Scope,Source,Status,Timebox,ToDo,Actuals.Value.@Sum";
 
     @Override
     public void initializeDefaultPreferences() {
         IPreferenceStore store = Activator.getDefault().getPreferenceStore();
         store.setDefault(PreferenceConstants.P_ENABLED, false);
         store.setDefault(PreferenceConstants.P_URL, DEFAULT_URL);
         store.setDefault(PreferenceConstants.P_USER, "");
         store.setDefault(PreferenceConstants.P_PASSWORD, "");
         store.setDefault(PreferenceConstants.P_INTEGRATED_AUTH, false);
         store.setDefault(PreferenceConstants.P_REQUIRESVALIDATION, false);
         store.setDefault(PreferenceConstants.P_MEMBER_TOKEN, "");
        store.setDefault(PreferenceConstants.P_PROJECT_TOKEN, "");
         store.setDefault(PreferenceConstants.P_ATTRIBUTE_SELECTION, DEFAULT_TASK_ATTRIBUTE_SELECTION);
     }
 
 }
