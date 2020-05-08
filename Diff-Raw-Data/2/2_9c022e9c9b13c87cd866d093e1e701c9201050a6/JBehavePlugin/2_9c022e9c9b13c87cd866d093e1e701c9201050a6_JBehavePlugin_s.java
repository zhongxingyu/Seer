 package org.jbehave.plugin.eclipse;
 
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.plugin.*;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class JBehavePlugin extends AbstractUIPlugin {
 
 	private static final String ID = "org.jbehave.plugin.eclipse";
	public static final String BEHAVIOR_INTERFACE_NAME = "jbehave.core.behaviour.Behaviours";
 	//The shared instance.
 	private static JBehavePlugin plugin;
 	
 	/**
 	 * The constructor.
 	 */
 	public JBehavePlugin() {
 		plugin = this;
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
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static JBehavePlugin getDefault() {
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
 		return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
 	}
 	
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public static Shell getActiveWorkbenchShell() {
 		IWorkbenchWindow workBenchWindow= getActiveWorkbenchWindow();
 		if (workBenchWindow == null)
 			return null;
 		return workBenchWindow.getShell();
 	}
 
 	/**
 	 * Returns the active workbench window
 	 * 
 	 * @return the active workbench window
 	 */
 	public static IWorkbenchWindow getActiveWorkbenchWindow() {
 		if (plugin == null)
 			return null;
 		IWorkbench workBench= plugin.getWorkbench();
 		if (workBench == null)
 			return null;
 		return workBench.getActiveWorkbenchWindow();
 	}
 
 }
