 package net.atos.optimus.m2m.engine.core.masks;
 
 import net.atos.optimus.m2m.engine.core.Activator;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 
 public class PreferencesTransformationMask implements ITransformationMask {
 
 	/**
 	 * Static internal class, in charge of holding the Singleton instance.
 	 * 
 	 * @generated XA Singleton Generator on 2013-07-10 15:28:48 CEST
 	 */
 	private static class SingletonHolder {
 		static PreferencesTransformationMask instance = new PreferencesTransformationMask();
 	}
 
 	/**
 	 * Returns the Singleton instance of this class.
 	 * 
 	 * @generated XA Singleton Generator on 2013-07-10 15:28:48 CEST
 	 */
 	public static PreferencesTransformationMask getInstance() {
 		return SingletonHolder.instance;
 	}
 
 	/**
 	 * Default constructor. Generated because used in singleton instanciation &
 	 * needs to be private
 	 * 
 	 * @generated XA Singleton Generator on 2013-07-10 15:28:48 CEST
 	 */
 	private PreferencesTransformationMask() {
 	}
 
 	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 
 	public boolean isTransformationEnabled(String id) {
 		String enablementKey = Activator.PLUGIN_ID + ".disabled." + id;
		return !this.preferenceStore.getBoolean(enablementKey);
 	}
 
 	public void setTransformationEnabled(String id, boolean enabled) {
 		String enablementKey = Activator.PLUGIN_ID + ".disabled." + id;
 		this.preferenceStore.setValue(enablementKey, !enabled);
 	}
 
 }
