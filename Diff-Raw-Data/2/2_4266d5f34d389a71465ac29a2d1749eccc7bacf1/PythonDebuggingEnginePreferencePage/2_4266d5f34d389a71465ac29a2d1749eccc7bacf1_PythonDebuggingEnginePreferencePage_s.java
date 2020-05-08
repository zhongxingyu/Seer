 package org.eclipse.dltk.python.internal.debug.ui.preferences;
 
 import org.eclipse.core.resources.IProject;
 
 import org.eclipse.dltk.debug.ui.preferences.AbstractDebuggingEngineOptionsBlock;
 import org.eclipse.dltk.python.core.PythonNature;
 import org.eclipse.dltk.python.internal.debug.PythonDebugConstants;
 import org.eclipse.dltk.python.internal.debug.PythonDebugPlugin;
 import org.eclipse.dltk.ui.PreferencesAdapter;
 import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage;
 import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
 import org.eclipse.dltk.ui.preferences.PreferenceKey;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 /**
  * Python debugging engine preference page
  */
 public class PythonDebuggingEnginePreferencePage extends
 		AbstractConfigurationBlockPropertyAndPreferencePage {
 
 	static PreferenceKey DEBUGGING_ENGINE = new PreferenceKey(
 			PythonDebugPlugin.PLUGIN_ID,
 			PythonDebugConstants.DEBUGGING_ENGINE_ID_KEY);
 
 	private static String PREFERENCE_PAGE_ID = "org.eclipse.dltk.python.preferences.debug.engines";
	private static String PROPERTY_PAGE_ID = "org.eclipse.dltk.python.debug.propertyPage.debug.engines";
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#createOptionsBlock(org.eclipse.dltk.ui.util.IStatusChangeListener,
 	 *      org.eclipse.core.resources.IProject,
 	 *      org.eclipse.ui.preferences.IWorkbenchPreferenceContainer)
 	 */
 	protected AbstractOptionsBlock createOptionsBlock(
 			IStatusChangeListener newStatusChangedListener, IProject project,
 			IWorkbenchPreferenceContainer container) {
 		return new AbstractDebuggingEngineOptionsBlock(
 				newStatusChangedListener, project, getKeys(), container) {
 
 			protected String getNatureId() {
 				return PythonNature.NATURE_ID;
 			}
 
 			protected PreferenceKey getSavedContributionKey() {
 				return DEBUGGING_ENGINE;
 			}
 
 		};
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#getHelpId()
 	 */
 	protected String getHelpId() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageId()
 	 */
 	protected String getPreferencePageId() {
 		return PREFERENCE_PAGE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#getProjectHelpId()
 	 */
 	protected String getProjectHelpId() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageId()
 	 */
 	protected String getPropertyPageId() {
 		return PROPERTY_PAGE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#setDescription()
 	 */
 	protected void setDescription() {
 		setDescription(PythonDebugPreferencesMessages.PythonDebugEnginePreferencePage_description);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#setPreferenceStore()
 	 */
 	protected void setPreferenceStore() {
 		setPreferenceStore(new PreferencesAdapter(PythonDebugPlugin
 				.getDefault().getPluginPreferences()));
 	}
 
 	private PreferenceKey[] getKeys() {
 		return new PreferenceKey[] { DEBUGGING_ENGINE };
 	}
 }
