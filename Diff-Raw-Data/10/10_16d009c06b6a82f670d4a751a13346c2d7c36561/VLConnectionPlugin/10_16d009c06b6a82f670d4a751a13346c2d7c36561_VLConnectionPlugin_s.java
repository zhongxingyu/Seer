 package gov.va.med.iss.connection;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.*;
 import org.osgi.framework.BundleContext;
 
 import java.util.*;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class VLConnectionPlugin extends AbstractUIPlugin {
 	//The shared instance.
 	private static VLConnectionPlugin plugin;
 	//Resource bundle.
 	private ResourceBundle resourceBundle;
 	
 	private static String PluginId = "gov.va.med.iss.connection";
	public static ImageDescriptor IMG_ERROR = AbstractUIPlugin.imageDescriptorFromPlugin(PluginId,"icons\\error.gif");
	public static ImageDescriptor IMG_SUCCESS = AbstractUIPlugin.imageDescriptorFromPlugin(PluginId,"icons\\yes1a.gif");
	public static ImageDescriptor IMG_POST_TEXT = AbstractUIPlugin.imageDescriptorFromPlugin(PluginId,"icons\\yes1a.gif");
	public static ImageDescriptor IMG_HELP = AbstractUIPlugin.imageDescriptorFromPlugin(PluginId,"icons\\helpbook07.gif");
	public static ImageDescriptor IMG_VA_LOGO = AbstractUIPlugin.imageDescriptorFromPlugin(PluginId,"icons\\VAlogo.gif");
 
 	
 	/**
 	 * The constructor.
 	 */
 	public VLConnectionPlugin() {
 		super();
 		plugin = this;
 		try {
 			resourceBundle = ResourceBundle.getBundle("gov.va.med.iss.connection.VLConnectionPluginResources");
 		} catch (MissingResourceException x) {
 			resourceBundle = null;
 		}
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static VLConnectionPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the string from the plugin's resource bundle,
 	 * or 'key' if not found.
 	 */
 	public static String getResourceString(String key) {
 		ResourceBundle bundle = VLConnectionPlugin.getDefault().getResourceBundle();
 		try {
 			return (bundle != null) ? bundle.getString(key) : key;
 		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	/**
 	 * Returns the plugin's resource bundle,
 	 */
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 }
