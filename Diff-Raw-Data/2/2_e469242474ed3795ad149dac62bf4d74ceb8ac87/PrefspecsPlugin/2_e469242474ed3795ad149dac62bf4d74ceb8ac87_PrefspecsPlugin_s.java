 package org.eclipse.imp.prefspecs;
 
 //import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.imp.runtime.SAFARIPluginBase;
 import org.osgi.framework.BundleContext;
 
 public class PrefspecsPlugin extends SAFARIPluginBase {
    public static final String kPluginID= "org.eclipse.uide.prefspecs";
 
     /**
      * The unique instance of this plugin class
      */
     protected static PrefspecsPlugin sPlugin;
 
     public static PrefspecsPlugin getInstance() {
         return sPlugin;
     }
 
     public PrefspecsPlugin() {
 	super();
 	sPlugin= this;
     }
 
     public void start(BundleContext context) throws Exception {
         super.start(context);
 
 	// Initialize the Preferences fields with the preference store data.
 //	IPreferenceStore prefStore= getPreferenceStore();
 
 //	PrefspecsPreferenceCache.builderEmitMessages= prefStore.getBoolean(PrefspecsPreferenceConstants.P_EMIT_MESSAGES);
 
 //	fEmitInfoMessages= PrefspecsPreferenceCache.builderEmitMessages;
     }
 
     public String getID() {
 	return kPluginID;
     }
 }
