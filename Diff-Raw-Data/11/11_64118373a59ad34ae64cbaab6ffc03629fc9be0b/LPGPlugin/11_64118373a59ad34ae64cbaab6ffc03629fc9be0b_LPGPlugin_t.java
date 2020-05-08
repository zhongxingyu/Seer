 package org.jikespg.uide;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.uide.runtime.UIDEPluginBase;
 import org.jikespg.uide.preferences.JikesPGPreferenceCache;
 import org.jikespg.uide.preferences.PreferenceConstants;
 import org.osgi.framework.BundleContext;
 
 public class JikesPGPlugin extends UIDEPluginBase {
     public static final String kPluginID= "org.jikespg.uide";
 
    /**
     * The unique instance of this plugin class
     */
    protected static JikesPGPlugin sPlugin;

    public static JikesPGPlugin getInstance() {
        return sPlugin;
    }

     public JikesPGPlugin() {
 	super();
	sPlugin= this;
     }
 
     public void start(BundleContext context) throws Exception {
         super.start(context);
 
 	// Initialize the JikesPGPreferences fields with the preference store data.
 	IPreferenceStore prefStore= getPreferenceStore();
 
 	JikesPGPreferenceCache.builderEmitMessages= prefStore.getBoolean(PreferenceConstants.P_EMIT_MESSAGES);
 	JikesPGPreferenceCache.useDefaultExecutable= prefStore.getBoolean(PreferenceConstants.P_USE_DEFAULT_EXEC);
 	JikesPGPreferenceCache.jikesPGExecutableFile= prefStore.getString(PreferenceConstants.P_JIKESPG_EXEC_PATH);
 	JikesPGPreferenceCache.useDefaultTemplates= prefStore.getBoolean(PreferenceConstants.P_USE_DEFAULT_TEMPLATE_DIR);
 	JikesPGPreferenceCache.jikesPGTemplateDir= prefStore.getString(PreferenceConstants.P_JIKESPG_TEMPLATE_DIR);
 
 	fEmitInfoMessages= JikesPGPreferenceCache.builderEmitMessages;
     }
 
     public String getID() {
 	return kPluginID;
     }
 }
