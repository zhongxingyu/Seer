 package jkind.xtext.ui.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import jkind.xtext.ui.internal.JKindActivator;
 
 /**
  * Class used to initialize default preference values.
  */
 public class PreferenceInitializer extends AbstractPreferenceInitializer {
 	@Override
     public void initializeDefaultPreferences() {
 		IPreferenceStore store = JKindActivator.getInstance().getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_SOLVER, PreferenceConstants.SOLVER_YICES);
 		store.setDefault(PreferenceConstants.PREF_INDUCT_CEX, true);
 		store.setDefault(PreferenceConstants.PREF_SMOOTH_CEX, false);
 		store.setDefault(PreferenceConstants.PREF_DEPTH, 200);
 		store.setDefault(PreferenceConstants.PREF_TIMEOUT,	100);
 	}
 }
