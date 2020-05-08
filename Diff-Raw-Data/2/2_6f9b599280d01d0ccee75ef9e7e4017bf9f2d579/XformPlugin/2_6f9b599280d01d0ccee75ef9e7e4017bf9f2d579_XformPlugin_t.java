 package org.eclipse.imp.xform;
 
 import org.eclipse.ui.plugin.*;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.osgi.framework.BundleContext;
 
 public class XformPlugin extends AbstractUIPlugin {
    public static final String kPluginID= "org.eclipse.imp.xform";
 
     // The shared instance.
     private static XformPlugin plugin;
 
     public XformPlugin() {
 	plugin= this;
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
 	plugin= null;
     }
 
     /**
      * Returns the shared instance.
      */
     public static XformPlugin getDefault() {
 	return plugin;
     }
 
     /**
      * Returns an image descriptor for the image file at the given
      * plug-in relative path.
      *
      * @param path the path
      * @return the image descriptor
      */
     public static ImageDescriptor getImageDescriptor(String path) {
 	return AbstractUIPlugin.imageDescriptorFromPlugin("com.ibm.watson.safari.xform", path);
     }
 }
