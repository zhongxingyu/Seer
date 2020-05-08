 package org.dawnsci.plotting.system;
 
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.osgi.framework.BundleContext;
 
 public class PlottingSystemActivator extends AbstractUIPlugin {
 
 	private final static String ID = "org.dawnsci.plotting.system";
 
 	private static IPreferenceStore plottingPreferenceStore;
 
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(ID, path);
 	}
 
 	public static Image getImage(String path) {
 		return getImageDescriptor(path).createImage();
 	}
 
 	public static IPreferenceStore getPlottingPreferenceStore() {
 		if (plottingPreferenceStore == null)
 			plottingPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		return plottingPreferenceStore;
 	}
 
	private static PlottingSystemActivator activator;
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		activator = this;
 	}
 
 	public static IPreferenceStore getLocalPreferenceStore() {
 		return activator.getPreferenceStore();
 	}
 }
